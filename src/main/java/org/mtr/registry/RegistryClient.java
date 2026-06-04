package org.mtr.registry;

//? if fabric {
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
//? }

//? if neoforge {
/*import org.mtr.neoforge.ModEventBus;
import org.mtr.neoforge.ModEventBusClient;
*///? }

import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.color.block.BlockColorProvider;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import org.mtr.MTR;
import org.mtr.fabric.MTRFabric;
import org.mtr.packet.CustomPacketC2S;
import org.mtr.packet.PacketBufferReceiver;
import org.mtr.packet.PacketBufferSender;
import org.mtr.packet.PacketHandler;

import java.util.Arrays;
import java.util.function.Function;

public final class RegistryClient {

	public static <T extends BlockEntity, U extends T> void registerBlockEntityRenderer(ObjectHolder<BlockEntityType<U>> blockEntityType, BlockEntityRendererFactory<T> factory) {
		//? if fabric {
		BlockEntityRendererFactories.register(blockEntityType.get(), factory);
		//? }

		//? if neoforge {
		/*ModEventBusClient.BLOCK_ENTITY_RENDERERS.add(event -> event.registerBlockEntityRenderer(blockEntityType.get(), factory));
		*///?}
	}

	public static <T extends Entity, U extends T> void registerEntityRenderer(ObjectHolder<EntityType<U>> entityType, EntityRendererFactory<T> factory) {
		//? if fabric {
		EntityRendererRegistry.register(entityType.get(), factory);
		//? }

		//? if neoforge {
		/*ModEventBusClient.BLOCK_ENTITY_RENDERERS.add(event -> event.registerEntityRenderer(entityType.get(), factory));
		*///?}
	}

	public static void registerBlockRenderType(RenderLayer renderLayer, ObjectHolder<Block> block) {
		//? if fabric {
		BlockRenderLayerMap.INSTANCE.putBlock(block.get(), renderLayer);
		//? }

		//? if neoforge {
		/*ModEventBusClient.CLIENT_OBJECTS_TO_REGISTER.add(() -> RenderLayers.setRenderLayer(block.get(), renderLayer));
		*///?}
	}

	public static void registerKeyBinding(KeyBinding keyBinding) {
		//? if fabric {
		KeyBindingHelper.registerKeyBinding(keyBinding);
		//? }

		//? if neoforge {
		/*ModEventBusClient.KEY_BINDINGS.add(keyBinding);
		*///?}
	}

	@SafeVarargs
	public static void registerBlockColors(BlockColorProvider blockColorProvider, ObjectHolder<Block>... blocks) {
		//? if fabric {
		ColorProviderRegistry.BLOCK.register(blockColorProvider, Arrays.stream(blocks).map(ObjectHolder::get).toArray(Block[]::new));
		//? }

		//? if neoforge {
		/*ModEventBusClient.BLOCK_COLORS.add(event -> event.getBlockColors().registerColorProvider(blockColorProvider, Arrays.stream(blocks).map(ObjectHolder::get).toArray(Block[]::new)));
		*///?}
	}

	public static void setupPackets() {
		//? if fabric {
		ClientPlayNetworking.registerGlobalReceiver(MTR.PACKET_IDENTIFIER_S2C, (customPacketS2C, context) -> PacketBufferReceiver.receive(customPacketS2C.buffer(), packetBufferReceiver -> {
			final Function<PacketBufferReceiver, ? extends PacketHandler> getInstance = MTRFabric.PACKETS.get(packetBufferReceiver.readString());
			if (getInstance != null) {
				getInstance.apply(packetBufferReceiver).runClient();
			}
		}, MinecraftClient.getInstance()::execute));
		//? }

		//? if neoforge {
		/*ModEventBus.PAYLOAD_HANDLERS.add(payloadRegistrar -> payloadRegistrar.playBidirectional(MTR.PACKET_IDENTIFIER_S2C, PacketCodec.tuple(PacketCodecs.BYTE_ARRAY, CustomPacketS2C::buffer, CustomPacketS2C::new), new DirectionalPayloadHandler<>((customPacketS2C, context) -> PacketBufferReceiver.receive(customPacketS2C.buffer(), packetBufferReceiver -> {
			final Function<PacketBufferReceiver, ? extends PacketHandler> getInstance = ModEventBus.PACKETS.get(packetBufferReceiver.readString());
			if (getInstance != null) {
				getInstance.apply(packetBufferReceiver).runClient();
			}
		}, MinecraftClient.getInstance()::execute), (customPacketS2C, context) -> {
		})));
		*///?}
	}

	public static <T extends PacketHandler> void sendPacketToServer(T data) {
		//? if fabric {
		final PacketBufferSender packetBufferSender = new PacketBufferSender();
		packetBufferSender.writeString(data.getClass().getName());
		data.write(packetBufferSender);
		packetBufferSender.send(bytes -> ClientPlayNetworking.send(new CustomPacketC2S(bytes)), MinecraftClient.getInstance()::execute);
		//? }

		//? if neoforge {
		/*final PacketBufferSender packetBufferSender = new PacketBufferSender();
		packetBufferSender.writeString(data.getClass().getName());
		data.write(packetBufferSender);
		packetBufferSender.send(bytes -> PacketDistributor.sendToServer(new CustomPacketC2S(bytes)), MinecraftClient.getInstance()::execute);
		*///?}
	}
}
