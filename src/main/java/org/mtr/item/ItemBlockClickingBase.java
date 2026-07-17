package org.mtr.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import org.mtr.generated.lang.TranslationProvider;
import org.mtr.registry.DataComponentTypes;

import java.util.List;
import java.util.ArrayList;
import java.util.function.Consumer;

public abstract class ItemBlockClickingBase extends Item {

	public ItemBlockClickingBase(Item.Properties settings) {
		super(settings.stacksTo(1));
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		if (!context.getLevel().isClientSide()) {
			if (clickCondition(context)) {
				final BlockPos startPos = context.getItemInHand().get(DataComponentTypes.START_POS.get());

				if (startPos == null) {
					context.getItemInHand().set(DataComponentTypes.START_POS.get(), context.getClickedPos());
					onStartClick(context);
				} else {
					onEndClick(context, startPos);
					context.getItemInHand().remove(DataComponentTypes.START_POS.get());
				}

				return InteractionResult.SUCCESS;
			} else {
				return InteractionResult.FAIL;
			}
		} else {
			return super.useOn(context);
		}
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag type) {
		final ArrayList<Component> components = new ArrayList<>();
		appendHoverText(stack, context, components, type);
		components.forEach(tooltip);
	}

	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag type) {
		final BlockPos blockPos = stack.get(DataComponentTypes.START_POS.get());
		if (blockPos != null) {
			tooltip.add(TranslationProvider.TOOLTIP_MTR_SELECTED_BLOCK.getMutableText(blockPos.toShortString()).withStyle(ChatFormatting.GOLD));
		}
	}

	protected abstract void onStartClick(UseOnContext context);

	protected abstract void onEndClick(UseOnContext context, BlockPos posEnd);

	protected abstract boolean clickCondition(UseOnContext context);
}
