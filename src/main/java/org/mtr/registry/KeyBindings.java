package org.mtr.registry;

import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;
import org.mtr.MTR;
import org.mtr.generated.lang.TranslationProvider;

public final class KeyBindings {
	private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(Identifier.fromNamespaceAndPath(MTR.MOD_ID, "keybinding"));

	static {
		LIFT_MENU = registerKeyBinding(TranslationProvider.KEY_MTR_LIFT_MENU.key, GLFW.GLFW_KEY_Z, TranslationProvider.CATEGORY_MTR_KEYBINDING.key);
		TRAIN_ACCELERATE = registerKeyBinding(TranslationProvider.KEY_MTR_TRAIN_ACCELERATE.key, GLFW.GLFW_KEY_UP, TranslationProvider.CATEGORY_MTR_KEYBINDING.key);
		TRAIN_BRAKE = registerKeyBinding(TranslationProvider.KEY_MTR_TRAIN_BRAKE.key, GLFW.GLFW_KEY_DOWN, TranslationProvider.CATEGORY_MTR_KEYBINDING.key);
		TRAIN_TOGGLE_DOORS = registerKeyBinding(TranslationProvider.KEY_MTR_TRAIN_TOGGLE_DOORS.key, GLFW.GLFW_KEY_LEFT, TranslationProvider.CATEGORY_MTR_KEYBINDING.key);
	}

	public static final KeyMapping LIFT_MENU;
	public static final KeyMapping TRAIN_ACCELERATE;
	public static final KeyMapping TRAIN_BRAKE;
	public static final KeyMapping TRAIN_TOGGLE_DOORS;

	public static void init() {
		MTR.LOGGER.info("Registering Minecraft Transit Railway key bindings");
	}

	private static KeyMapping registerKeyBinding(String translationKey, int code, String category) {
		final KeyMapping keyBinding = new KeyMapping(translationKey, code, CATEGORY);
		RegistryClient.registerKeyBinding(keyBinding);
		return keyBinding;
	}
}
