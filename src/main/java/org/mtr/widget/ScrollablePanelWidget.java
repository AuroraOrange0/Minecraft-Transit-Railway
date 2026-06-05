package org.mtr.widget;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractScrollArea;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import org.mtr.core.tool.Utilities;
import org.mtr.tool.GuiHelper;

public abstract class ScrollablePanelWidget extends AbstractScrollArea {

	public ScrollablePanelWidget() {
		super(0, 0, 0, 0, Component.empty());
	}

	@Override
	public final void onClick(double mouseX, double mouseY) {
		if (!updateScrolling(mouseX, mouseY, 0)) {
			onClickNew(mouseX, mouseY);
		}
	}

	@Override
	protected final void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
		setScrollAmount(Math.clamp(scrollAmount(), 0, Math.max(0, contentHeight() - height)));
		context.enableScissor(getX(), getY(), getX() + width, getY() + height);
		render(context, active ? mouseX : -1, active ? mouseY : -1);
		context.disableScissor();
		drawScrollbar(context, active ? mouseX : -1, active ? mouseY : -1);
	}

	@Override
	protected final boolean isValidClickButton(int button) {
		return active && visible && super.isValidClickButton(button);
	}

	@Override
	protected final double scrollRate() {
		return GuiHelper.DEFAULT_LINE_SIZE;
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput builder) {
	}

	/**
	 * Do not call this directly! Use {@link ScrollablePanelWidget#onClick} instead.
	 */
	protected void onClickNew(double mouseX, double mouseY) {
		super.onClick(mouseX, mouseY);
	}

	protected final int getScrollbarWidth() {
		return scrollbarVisible() ? SCROLLBAR_WIDTH : 0;
	}

	/**
	 * Do not call this directly! Use {@link ScrollablePanelWidget#renderWidget} instead.
	 */
	protected abstract void render(GuiGraphics context, int mouseX, int mouseY);

	private void drawScrollbar(GuiGraphics context, int mouseX, int mouseY) {
		if (scrollbarVisible()) {
			final int x1 = scrollBarX();
			final int y1 = scrollBarY();
			final int x2 = x1 + SCROLLBAR_WIDTH;
			final int y2 = y1 + scrollerHeight();
			context.fill(x1 + 1, y1 + 1, x2 - 1, y2 - 1, Utilities.isBetween(mouseX, x1, x2 - 1) && Utilities.isBetween(mouseY, y1, y2 - 1) ? GuiHelper.SCROLL_BAR_HOVER_COLOR : GuiHelper.SCROLL_BAR_COLOR);
		}
	}

	/**
	 * @deprecated This does nothing now!
	 */
	@Deprecated
	@Override
	protected final void renderScrollbar(GuiGraphics context) {
	}
}
