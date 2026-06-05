package org.mtr.block;

import lombok.Getter;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
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
import java.util.List;
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
		return IBlock.checkHoldingBrush(world, player, () -> {
			final BlockPos newBlockPos = getBlockPosWithData.apply(world, pos);
			final BlockEntity entity = world.getBlockEntity(newBlockPos);
			if (entity instanceof BlockEntityBase) {
				PacketOpenBlockEntityScreen.sendDirectlyToServer((ServerLevel) world, (ServerPlayer) player, newBlockPos);
			}
		});
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
		private static final String KEY_MESSAGE = "message";
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
		}

		@Override
		protected void writeNbt(CompoundTag nbtCompound) {
			for (int i = 0; i < maxArrivals; i++) {
				nbtCompound.putString(KEY_MESSAGE + i, messages[i] == null ? "" : messages[i]);
				nbtCompound.putBoolean(KEY_HIDE_ARRIVAL + i, hideArrivalArray[i]);
			}
			nbtCompound.putLongArray(KEY_PLATFORM_IDS, new ArrayList<>(platformIds));
			nbtCompound.putInt(KEY_DISPLAY_PAGE, displayPage);
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

		public abstract boolean showArrivalNumber();

		public abstract boolean alternateLines();

		public abstract int textColorArrived();

		public abstract int textColor();
	}
}
