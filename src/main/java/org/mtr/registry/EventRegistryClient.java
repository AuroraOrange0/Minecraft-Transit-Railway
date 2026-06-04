package org.mtr.registry;

//? if fabric {
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudLayerRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
//? }

//? if neoforge {
/*import org.mtr.neoforge.MainEventBusClient;
import org.mtr.neoforge.ModEventBusClient;
*///? }

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraft.world.chunk.Chunk;
import org.mtr.MTR;
import org.mtr.MTRClient;

import java.util.Random;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public final class EventRegistryClient {

	public static void registerStartClientTick(Runnable runnable) {
		//? if fabric {
		ClientTickEvents.START_CLIENT_TICK.register(minecraftServer -> runnable.run());
		//? }

		//? if neoforge {
		/*MainEventBusClient.startClientTickRunnable = runnable;
		*///? }
	}

	public static void registerEndClientTick(Runnable runnable) {
		//? if fabric {
		ClientTickEvents.END_CLIENT_TICK.register(minecraftServer -> runnable.run());
		//? }

		//? if neoforge {
		/*MainEventBusClient.endClientTickRunnable = runnable;
		*///? }
	}

	public static void registerStartWorldTick(Consumer<ClientWorld> consumer) {
		//? if fabric {
		ClientTickEvents.START_WORLD_TICK.register(consumer::accept);
		//? }

		//? if neoforge {
		/*MainEventBusClient.startWorldTickRunnable = consumer;
		*///? }
	}

	public static void registerEndWorldTick(Consumer<ClientWorld> consumer) {
		//? if fabric {
		ClientTickEvents.END_WORLD_TICK.register(consumer::accept);
		//? }

		//? if neoforge {
		/*MainEventBusClient.endWorldTickRunnable = consumer;
		*///? }
	}

	public static void registerClientJoin(Runnable runnable) {
		//? if fabric {
		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> runnable.run());
		//? }

		//? if neoforge {
		/*MainEventBusClient.clientJoinRunnable = runnable;
		*///? }
	}

	public static void registerClientDisconnect(Runnable runnable) {
		//? if fabric {
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> runnable.run());
		//? }

		//? if neoforge {
		/*MainEventBusClient.clientDisconnectRunnable = runnable;
		*///? }
	}

	public static void registerChunkLoad(BiConsumer<ClientWorld, Chunk> consumer) {
		//? if fabric {
		ClientChunkEvents.CHUNK_LOAD.register(consumer::accept);
		//? }

		//? if neoforge {
		/*ModEventBusClient.chunkLoadConsumer = consumer;
		*///? }
	}

	public static void registerChunkUnload(BiConsumer<ClientWorld, Chunk> consumer) {
		//? if fabric {
		ClientChunkEvents.CHUNK_UNLOAD.register(consumer::accept);
		//? }

		//? if neoforge {
		/*ModEventBusClient.chunkUnloadConsumer = consumer;
		*///? }
	}

	public static void registerResourceReloadEvent(Runnable runnable) {
		//? if fabric {
		final Identifier identifier = Identifier.of(Integer.toHexString(new Random().nextInt()), "resource");
		ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
			@Override
			public Identifier getFabricId() {
				return identifier;
			}

			@Override
			public void reload(ResourceManager manager) {
				runnable.run();
			}
		});
		//? }

		//? if neoforge {
		/*ModEventBusClient.resourceReloadRunnable = runnable;
		*///? }
	}

	public static void registerWorldRenderEvent(MTRClient.WorldRenderCallback worldRenderCallback) {
		//? if fabric {
		WorldRenderEvents.AFTER_ENTITIES.register(worldRenderContext -> {
			final MatrixStack matrixStack = worldRenderContext.matrixStack();
			final VertexConsumerProvider vertexConsumerProvider = worldRenderContext.consumers();
			if (matrixStack != null && vertexConsumerProvider != null) {
				worldRenderCallback.accept(matrixStack, vertexConsumerProvider, worldRenderContext.camera().getPos());
			}
		});
		//? }

		//? if neoforge {
		/*MainEventBusClient.worldRenderCallback = worldRenderCallback;
		*///? }
	}

	public static void registerHudLayerRenderEvent(BiConsumer<DrawContext, RenderTickCounter> hudLayerRenderCallback) {
		//? if fabric {
		HudLayerRegistrationCallback.EVENT.register(layeredDrawerWrapper -> layeredDrawerWrapper.attachLayerBefore(IdentifiedLayer.CHAT, Identifier.of(MTR.MOD_ID, "gui"), hudLayerRenderCallback::accept));
		//? }

		//? if neoforge {
		/*MainEventBusClient.hudLayerRenderCallback = hudLayerRenderCallback;
		*///? }
	}
}
