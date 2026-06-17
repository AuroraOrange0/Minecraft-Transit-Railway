package org.mtr.block;

import lombok.Getter;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;
import org.mtr.generated.lang.TranslationProvider;
import org.mtr.libraries.it.unimi.dsi.fastutil.longs.LongAVLTreeSet;
import org.mtr.packet.PacketOpenBlockEntityScreen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;

public abstract class BlockPIDSBase extends Block implements EntityBlock {

	public final int maxArrivals;
	public final BiPredicate<Level, BlockPos> canStoreData;
	public final BiFunction<Level, BlockPos, BlockPos> getBlockPosWithData;

	public BlockPIDSBase(BlockBehaviour.Properties settings, int maxArrivals, BiPredicate<Level, BlockPos> canStoreData, BiFunction<Level, BlockPos, BlockPos> getBlockPosWithData) {
		super(settings.lightLevel(blockState -> 5).noOcclusion());
		this.maxArrivals = maxArrivals;
		this.canStoreData = canStoreData;
		this.getBlockPosWithData = getBlockPosWithData;
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
		final BlockPos newBlockPos = getBlockPosWithData.apply(world, pos);
		final BlockEntity entity = world.getBlockEntity(newBlockPos);
		if (entity instanceof BlockEntityBase blockEntity) {
			InteractionResult brushResult = IBlock.checkHoldingBrush(world, player, () -> {
				PacketOpenBlockEntityScreen.sendDirectlyToServer((ServerLevel) world, (ServerPlayer) player, newBlockPos);
			});

			for(InteractionHand hand : InteractionHand.values()) {
				ItemStack stack = player.getItemInHand(hand);
				if(stack.getItem() instanceof DyeItem dyeItem) {
					DyeColor dyeColor = dyeItem.getDyeColor();
					blockEntity.customColor = PIDSColorHelper.convert(dyeColor);
					blockEntity.setChanged();
					player.playSound(SoundEvents.DYE_USE);
					if(!player.isCreative()) {
						stack.shrink(1);
					}
					return InteractionResult.SUCCESS;
				} else if(stack.getItem() == Items.MILK_BUCKET) {
					blockEntity.customColor = null;
					blockEntity.setChanged();
					player.playSound(SoundEvents.BUCKET_FILL_TADPOLE);
					if(!player.isCreative()) {
						player.setItemInHand(hand, new ItemStack(Items.BUCKET));
					}
					return InteractionResult.SUCCESS;
				}
			}
			return brushResult;
		}
		return InteractionResult.PASS;
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag options) {
		tooltip.add(TranslationProvider.TOOLTIP_MTR_ARRIVALS.getMutableText(maxArrivals).withStyle(ChatFormatting.GRAY));
	}

	public static abstract class BlockEntityBase extends BlockEntityExtension {

		public final int maxArrivals;
		public final BiPredicate<Level, BlockPos> canStoreData;
		public final BiFunction<Level, BlockPos, BlockPos> getBlockPosWithData;

		private final @Nullable String[] messages;
		private final boolean[] hideArrivalArray;
		@Getter
		private final LongAVLTreeSet platformIds = new LongAVLTreeSet();
		@Getter
		private int displayPage;
		@Getter
		private @Nullable Integer customColor;
		private static final String KEY_MESSAGE = "message";
		private static final String KEY_CUSTOM_COLOR = "custom_color";
		private static final String KEY_HIDE_ARRIVAL = "hide_arrival";
		private static final String KEY_PLATFORM_IDS = "platform_ids";
		private static final String KEY_DISPLAY_PAGE = "display_page";

		public BlockEntityBase(int maxArrivals, BiPredicate<Level, BlockPos> canStoreData, BiFunction<Level, BlockPos, BlockPos> getBlockPosWithData, BlockEntityType<?> type, BlockPos pos, BlockState state) {
			super(type, pos, state);
			this.maxArrivals = maxArrivals;
			this.canStoreData = canStoreData;
			this.getBlockPosWithData = getBlockPosWithData;
			messages = new String[maxArrivals];
			for (int i = 0; i < maxArrivals; i++) {
				messages[i] = "";
			}
			hideArrivalArray = new boolean[maxArrivals];
			customColor = null;
		}

		@Override
		protected void readNbt(CompoundTag nbtCompound) {
			for (int i = 0; i < maxArrivals; i++) {
				messages[i] = nbtCompound.getString(KEY_MESSAGE + i);
				hideArrivalArray[i] = nbtCompound.getBoolean(KEY_HIDE_ARRIVAL + i);
			}

			platformIds.clear();
			final long[] platformIdsArray = nbtCompound.getLongArray(KEY_PLATFORM_IDS);
			for (final long platformId : platformIdsArray) {
				platformIds.add(platformId);
			}

			displayPage = nbtCompound.getInt(KEY_DISPLAY_PAGE);
			customColor = PIDSColorHelper.parse(nbtCompound.getString(KEY_CUSTOM_COLOR));
		}

		@Override
		protected void writeNbt(CompoundTag nbtCompound) {
			for (int i = 0; i < maxArrivals; i++) {
				nbtCompound.putString(KEY_MESSAGE + i, messages[i] == null ? "" : messages[i]);
				nbtCompound.putBoolean(KEY_HIDE_ARRIVAL + i, hideArrivalArray[i]);
			}
			nbtCompound.putLongArray(KEY_PLATFORM_IDS, new ArrayList<>(platformIds));
			nbtCompound.putInt(KEY_DISPLAY_PAGE, displayPage);
			nbtCompound.putString(KEY_CUSTOM_COLOR, PIDSColorHelper.serialize(customColor));
		}

		public void setData(String[] messages, boolean[] hideArrivalArray, LongAVLTreeSet platformIds, int displayPage) {
			System.arraycopy(messages, 0, this.messages, 0, Math.min(messages.length, this.messages.length));
			System.arraycopy(hideArrivalArray, 0, this.hideArrivalArray, 0, Math.min(hideArrivalArray.length, this.hideArrivalArray.length));
			final LongAVLTreeSet platformIdsCopy = new LongAVLTreeSet(platformIds);
			this.platformIds.clear();
			this.platformIds.addAll(platformIdsCopy);
			this.displayPage = displayPage;
			setChanged();
		}

		public String getMessage(int index) {
			if (index >= 0 && index < maxArrivals) {
				if (messages[index] == null) {
					messages[index] = "";
				}
				return messages[index];
			} else {
				return "";
			}
		}

		public boolean getHideArrival(int index) {
			if (index >= 0 && index < maxArrivals) {
				return hideArrivalArray[index];
			} else {
				return false;
			}
		}

		public @Nullable Integer customizedColor() {
			return customColor;
		}

		public abstract boolean showArrivalNumber();

		public abstract boolean alternateLines();

		public abstract int textColorArrived();

		public abstract int textColor();
	}

	static class PIDSColorHelper {
		private static final Map<DyeColor, Integer> DYE_TO_PIDS_COLOR = new HashMap<>();

		static {
			DYE_TO_PIDS_COLOR.put(DyeColor.WHITE, 0xF9FFFE);
			DYE_TO_PIDS_COLOR.put(DyeColor.ORANGE, 0xFF9900);
			DYE_TO_PIDS_COLOR.put(DyeColor.MAGENTA, 0xEB4CDF);
			DYE_TO_PIDS_COLOR.put(DyeColor.LIGHT_BLUE, 0x4DB3F9);
			DYE_TO_PIDS_COLOR.put(DyeColor.YELLOW, 0xFED83D);
			DYE_TO_PIDS_COLOR.put(DyeColor.LIME, 0x98DB24);
			DYE_TO_PIDS_COLOR.put(DyeColor.PINK, 0xF87CC4);
			DYE_TO_PIDS_COLOR.put(DyeColor.GRAY, 0x6D7778);
			DYE_TO_PIDS_COLOR.put(DyeColor.LIGHT_GRAY, 0xA9A9A2);
			DYE_TO_PIDS_COLOR.put(DyeColor.CYAN, 0x09F6F6);
			DYE_TO_PIDS_COLOR.put(DyeColor.PURPLE, 0xBE4EFB);
			DYE_TO_PIDS_COLOR.put(DyeColor.BLUE, 0x6F79EC);
			DYE_TO_PIDS_COLOR.put(DyeColor.BROWN, 0xB3652D);
			DYE_TO_PIDS_COLOR.put(DyeColor.GREEN, 0x1FAD1F);
			DYE_TO_PIDS_COLOR.put(DyeColor.RED, 0xFF5555);
			DYE_TO_PIDS_COLOR.put(DyeColor.BLACK, 0x000000);
		}

		public static int convert(DyeColor color) {
			return DYE_TO_PIDS_COLOR.get(color);
		}

		/**
		 * Converts an int color to a hexadecimal string, empty string if null.
		 */
		public static String serialize(@Nullable Integer color) {
			return color == null ? "" : String.format("%06X", color);
		}

		/**
		 * Parse a hexadecimal string, returns null if parsing failed.
		 */
		public static @Nullable Integer parse(String str) {
			try {
				return Integer.parseInt(str, 16);
			} catch (NumberFormatException e) {
    			return null;
			}
		}
	}
}
