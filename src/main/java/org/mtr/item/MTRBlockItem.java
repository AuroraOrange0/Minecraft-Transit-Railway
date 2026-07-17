package org.mtr.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.Block;
import org.mtr.block.BlockTooltipProvider;

import java.util.ArrayList;
import java.util.function.Consumer;

public class MTRBlockItem extends BlockItem {
	public MTRBlockItem(Block block, Item.Properties properties) {
		super(block, properties);
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag options) {
		super.appendHoverText(stack, context, display, tooltip, options);
		if (getBlock() instanceof BlockTooltipProvider provider) {
			final ArrayList<Component> components = new ArrayList<>();
			provider.appendHoverText(stack, context, components, options);
			components.forEach(tooltip);
		}
	}
}
