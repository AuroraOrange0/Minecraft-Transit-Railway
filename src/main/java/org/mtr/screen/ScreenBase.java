package org.mtr.screen;

import gg.essential.universal.UMinecraft;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.jspecify.annotations.Nullable;
import org.mtr.widget.ClickableWidgetBase;

public abstract class ScreenBase extends Screen {

	@Nullable
	private final WindowBase previousScreen;
	@Nullable
	private final Screen previousScreenLegacy;

	public ScreenBase(@Nullable WindowBase previousScreen) {
		super(Text.empty());
		this.previousScreen = previousScreen;
		this.previousScreenLegacy = null;
	}

	public ScreenBase(@Nullable Screen previousScreenLegacy) {
		super(Text.empty());
		this.previousScreen = null;
		this.previousScreenLegacy = previousScreenLegacy;
	}

	public ScreenBase() {
		this((Screen) null);
	}

	@Override
	protected final <T extends Element & Drawable & Selectable> T addDrawableChild(T drawableElement) {
		if (drawableElement instanceof ClickableWidgetBase) {
			((ClickableWidgetBase) drawableElement).init(this::addSelectableChild);
		}
		return super.addDrawableChild(drawableElement);
	}

	@Override
	protected final <T extends Element & Selectable> T addSelectableChild(T drawableElement) {
		if (drawableElement instanceof ClickableWidgetBase) {
			((ClickableWidgetBase) drawableElement).init(this::addSelectableChild);
		}
		return super.addSelectableChild(drawableElement);
	}

	@Override
	public void close() {
		super.close();
		if (previousScreen != null) {
			UMinecraft.setCurrentScreenObj(previousScreen);
		} else {
			MinecraftClient.getInstance().setScreen(previousScreenLegacy);
		}
	}

	@Override
	public final boolean shouldPause() {
		return false;
	}
}
