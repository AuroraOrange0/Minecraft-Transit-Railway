package org.mtr.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;
import org.mtr.MTRClient;
import org.mtr.block.BlockNode;
import org.mtr.core.data.Rail;
import org.mtr.core.data.TransportMode;
import org.mtr.core.tool.Angle;
import org.mtr.generated.lang.TranslationProvider;
import org.mtr.registry.DataComponentTypes;

import java.util.List;

public abstract class ItemNodeModifierSelectableBlockBase extends ItemNodeModifierBase {

	private final boolean canSaveBlock;
	private final int height;
	private final int width;
	private final int radius;

	private static final String TAG_BLOCK_ID = "block_id";

	public ItemNodeModifierSelectableBlockBase(boolean canSaveBlock, int height, int width, Item.Properties settings) {
		super(true, false, false, true, settings);
		this.canSaveBlock = canSaveBlock;
		this.height = height;
		this.width = width;
		radius = width / 2;
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		if (canSaveBlock) {
			final Level world = context.getLevel();
			if (!world.isClientSide()) {
				final Player playerEntity = context.getPlayer();
				if (playerEntity != null && playerEntity.isShiftKeyDown()) {
					final BlockState state = world.getBlockState(context.getClickedPos());
					final BlockState neighborState;
					if (state.getBlock() instanceof BlockNode) {
						neighborState = Blocks.AIR.defaultBlockState();
					} else {
						neighborState = state;
					}
					playerEntity.displayClientMessage(TranslationProvider.TOOLTIP_MTR_SELECTED_MATERIAL.getText(Component.translatable(neighborState.getBlock().getDescriptionId()).getString()), true);
					context.getItemInHand().set(DataComponentTypes.BLOCK_ID.get(), Block.getId(neighborState));
					return InteractionResult.SUCCESS;
				}
			}
		}

		return super.useOn(context);
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag type) {
		if (height > 0) {
			tooltip.add(TranslationProvider.TOOLTIP_MTR_RAIL_ACTION_HEIGHT.getMutableText(height).withStyle(ChatFormatting.GRAY));
		}
		tooltip.add(TranslationProvider.TOOLTIP_MTR_RAIL_ACTION_WIDTH.getMutableText(width).withStyle(ChatFormatting.GRAY));

		if (canSaveBlock) {
			final BlockState state = getSavedState(stack);
			final String[] textSplit = (state.isAir() ? TranslationProvider.TOOLTIP_MTR_SHIFT_RIGHT_CLICK_TO_SELECT_MATERIAL : TranslationProvider.TOOLTIP_MTR_SHIFT_RIGHT_CLICK_TO_CLEAR).getString(MTRClient.getShiftText(), Component.translatable(org.mtr.registry.Blocks.RAIL_NODE.get().getDescriptionId())).split("\\|");
			for (String text : textSplit) {
				tooltip.add(Component.literal(text).withStyle(ChatFormatting.GRAY).withStyle(ChatFormatting.ITALIC));
			}
			tooltip.add(TranslationProvider.TOOLTIP_MTR_SELECTED_MATERIAL.getMutableText(Component.translatable(state.getBlock().getDescriptionId()).getString()).withStyle(ChatFormatting.GREEN));
		}

		super.appendHoverText(stack, context, tooltip, type);
	}

	@Override
	protected final void onConnect(Level world, ItemStack itemStack, TransportMode transportMode, BlockState stateStart, BlockState stateEnd, BlockPos posStart, BlockPos posEnd, Angle facingStart, Angle facingEnd, @Nullable ServerPlayer serverPlayerEntity) {
		if (serverPlayerEntity != null) {
			getRail(world, posStart, posEnd, serverPlayerEntity, rail -> onConnect(rail, serverPlayerEntity, itemStack, radius, height));
		}
	}

	@Override
	protected final void onRemove(Level world, BlockPos posStart, BlockPos posEnd, @Nullable ServerPlayer serverPlayerEntity) {
	}

	protected BlockState getSavedState(ItemStack stack) {
		final Integer blockId = stack.get(DataComponentTypes.BLOCK_ID.get());
		return blockId == null ? Blocks.AIR.defaultBlockState() : Block.stateById(blockId);
	}

	protected abstract void onConnect(Rail rail, ServerPlayer serverPlayerEntity, ItemStack itemStack, int radius, int height);
}
