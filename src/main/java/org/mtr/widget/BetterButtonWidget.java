package org.mtr.widget;

import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;
import org.joml.Matrix3x2fStack;
import org.mtr.tool.Drawing;
import org.mtr.tool.GuiHelper;

public final class BetterButtonWidget extends ClickableWidgetBase {

	@Setter
	private int backgroundColor = GuiHelper.BACKGROUND_COLOR;
	@Setter
	private int hoverColor = GuiHelper.HOVER_COLOR;
	@Setter
	private int textColor = GuiHelper.WHITE_COLOR;

	@Nullable
	private final Identifier icon;
	@Nullable
	private final String text;
	private final int textWidth;
	private final int fixedWidth;
	private final Runnable onPress;

	public BetterButtonWidget(@Nullable Identifier icon, @Nullable String text, int width, Runnable onPress) {
		this.icon = icon;
		this.text = text;
		textWidth = text == null ? 0 : Minecraft.getInstance().font.width(text);
		fixedWidth = width;
		this.onPress = onPress;
		setDimensions();
	}

	@Override
	protected void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
		setDimensions();
		final Matrix3x2fStack matrixStack = context.pose();

		// Draw background
		new Drawing(matrixStack, RenderTypes.debugQuads())
			.setVerticesWH(getX(), getY(), width, height)
			.setColor(isMouseOver(mouseX, mouseY) ? hoverColor : backgroundColor)
			.draw();

		// Draw icon
		if (icon != null) {
			new Drawing(matrixStack, GuiHelper.getGuiTexturedRenderType(icon))
				.setVerticesWH(getX() + (width - getContentWidth()) / 2F, getY() + GuiHelper.DEFAULT_PADDING / 2F, GuiHelper.DEFAULT_ICON_SIZE, GuiHelper.DEFAULT_ICON_SIZE)
				.setUv()
				.draw();
		}

		// Draw text
		GuiHelper.drawText(context, text, getX() + width - (width - getContentWidth()) / 2F - textWidth, getY() + GuiHelper.DEFAULT_PADDING, 0, active ? textColor : GuiHelper.DISABLED_TEXT_COLOR);
	}

	@Override
	public void onClick(net.minecraft.client.input.MouseButtonEvent event, boolean doubleClick) {
		onPress.run();
	}

	private void setDimensions() {
		setSize(fixedWidth <= 0 ? getContentWidth() + GuiHelper.DEFAULT_PADDING * (text == null ? 1 : 2) : fixedWidth, GuiHelper.DEFAULT_LINE_SIZE);
	}

	private int getContentWidth() {
		return (icon == null ? 0 : GuiHelper.DEFAULT_ICON_SIZE) + (icon == null || text == null ? 0 : GuiHelper.DEFAULT_PADDING) + textWidth;
	}
}
