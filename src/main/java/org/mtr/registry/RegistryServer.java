package org.mtr.registry;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.component.ComponentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jspecify.annotations.Nullable;
import org.mtr.MTR;
import org.mtr.fabric.MTRFabric;
import org.mtr.libraries.it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.mtr.libraries.it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.mtr.neoforge.MTRNeoForge;
import org.mtr.neoforge.ModEventBus;
import org.mtr.packet.*;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public final class RegistryServer {

	//? if fabric {
	private static final Object2ObjectOpenHashMap<String, ObjectArrayList<Supplier<ItemConvertible>>> ITEM_GROUP_ENTRIES = new Object2ObjectOpenHashMap<>();
	private static final ObjectArrayList<Runnable> OBJECTS_TO_REGISTER = new ObjectArrayList<>();
	//? }

	public static void init() {
		//? if fabric {
		OBJECTS_TO_REGISTER.forEach(Runnable::run);
		//? }
	}

	public static ObjectHolder<Block> registerBlock(String registryName, Function<AbstractBlock.Settings, Block> factory) {
		//? if fabric {
		return register(Registries.BLOCK, RegistryKeys.BLOCK, registryName, dataRegistryKey -> factory.apply(AbstractBlock.Settings.create().registryKey(dataRegistryKey)));
		//? }

		//? if neoforge {
		return new ObjectHolder<>(MTRNeoForge.BLOCKS.register(registryName, identifier -> factory.apply(AbstractBlock.Settings.create().registryKey(RegistryKey.of(RegistryKeys.BLOCK, identifier)))));
		//? }
	}

	public static ObjectHolder<Item> registerItem(String registryName, Function<Item.Settings, Item> factory, @Nullable String itemGroupRegistryName) {
		//? if fabric {
		final ObjectHolder<Item> objectHolder = register(Registries.ITEM, RegistryKeys.ITEM, registryName, dataRegistryKey -> factory.apply(new Item.Settings().registryKey(dataRegistryKey)));
		if (itemGroupRegistryName != null) {
			ITEM_GROUP_ENTRIES.computeIfAbsent(itemGroupRegistryName, key -> new ObjectArrayList<>()).add(objectHolder::get);
		}
		return objectHolder;
		//? }

		//? if neoforge {
		final ObjectHolder<Item> objectHolder = new ObjectHolder<>(MTRNeoForge.ITEMS.register(registryName, identifier -> factory.apply(new Item.Settings().registryKey(RegistryKey.of(RegistryKeys.ITEM, identifier)))));
		if (itemGroupRegistryName != null) {
			ITEM_GROUP_ENTRIES.computeIfAbsent(itemGroupRegistryName, key -> new ObjectArrayList<>()).add(objectHolder::get);
		}
		return objectHolder;
		//? }
	}

	public static <T extends BlockEntity> ObjectHolder<BlockEntityType<T>> registerBlockEntityType(String registryName, BiFunction<BlockPos, BlockState, T> factory, Supplier<Block> blockSupplier) {
		//? if fabric {
		return register(Registries.BLOCK_ENTITY_TYPE, RegistryKeys.BLOCK_ENTITY_TYPE, registryName, dataRegistryKey -> FabricBlockEntityTypeBuilder.create(factory::apply, blockSupplier.get()).build());
		//? }

		//? if neoforge {
		return new ObjectHolder<>(MTRNeoForge.BLOCK_ENTITY_TYPES.register(registryName, () -> new BlockEntityType<>(factory::apply, blockSupplier.get())));
		//? }
	}

	public static <T extends Entity> ObjectHolder<EntityType<T>> registerEntityType(String registryName, BiFunction<EntityType<T>, World, T> factory, float width, float height) {
		//? if fabric {
		return register(Registries.ENTITY_TYPE, RegistryKeys.ENTITY_TYPE, registryName, dataRegistryKey -> EntityType.Builder.create(factory::apply, SpawnGroup.MISC).dimensions(width, height).build(dataRegistryKey));
		//? }

		//? if neoforge {
		return new ObjectHolder<>(MTRNeoForge.ENTITY_TYPES.register(registryName, identifier -> EntityType.Builder.create(factory::apply, SpawnGroup.MISC).dimensions(width, height).build(RegistryKey.of(RegistryKeys.ENTITY_TYPE, Identifier.of(MTR.MOD_ID, registryName)))));
		//? }
	}

	public static String registerItemGroup(String registryName, Supplier<ItemStack> iconSupplier) {
		//? if fabric {
		register(Registries.ITEM_GROUP, RegistryKeys.ITEM_GROUP, registryName, dataRegistryKey -> FabricItemGroup.builder().icon(iconSupplier).displayName(Text.translatable(String.format("itemGroup.%s.%s", MTR.MOD_ID, registryName))).entries((displayContext, entries) -> ITEM_GROUP_ENTRIES.getOrDefault(registryName, new ObjectArrayList<>()).forEach(itemSupplier -> entries.add(itemSupplier.get()))).build());
		return registryName;
		//? }

		//? if neoforge {
		MTRNeoForge.ITEM_GROUPS.register(registryName, () -> ItemGroup.builder().icon(iconSupplier).displayName(Text.translatable(String.format("itemGroup.%s.%s", MTR.MOD_ID, registryName))).entries((displayContext, entries) -> ITEM_GROUP_ENTRIES.getOrDefault(registryName, new ObjectArrayList<>()).forEach(itemSupplier -> entries.add(itemSupplier.get()))).build());
		return registryName;
		//? }
	}

	public static ObjectHolder<SoundEvent> registerSoundEvent(String registryName, Supplier<SoundEvent> supplier) {
		//? if fabric {
		return register(Registries.SOUND_EVENT, RegistryKeys.SOUND_EVENT, registryName, dataRegistryKey -> supplier.get());
		//? }

		//? if neoforge {
		return new ObjectHolder<>(MTRNeoForge.SOUND_EVENTS.register(registryName, supplier));
		//? }
	}

	public static void registerCommands(Consumer<CommandDispatcher<ServerCommandSource>> consumer) {
		//? if fabric {
		CommandRegistrationCallback.EVENT.register((dispatcher, commandRegistryAccess, environment) -> consumer.accept(dispatcher));
		//? }

		//? if neoforge {
		MainEventBus.commandConsumer = consumer;
		//? }
	}

	public static <T> ObjectHolder<ComponentType<T>> registerDataComponentType(String registryName, Supplier<ComponentType<T>> supplier) {
		//? if fabric {
		return register(Registries.DATA_COMPONENT_TYPE, RegistryKeys.DATA_COMPONENT_TYPE, registryName, dataRegistryKey -> supplier.get());
		//? }

		//? if neoforge {
		return new ObjectHolder<>(MTRNeoForge.DATA_COMPONENT_TYPES.register(registryName, supplier));
		//? }
	}

	public static void setupPackets() {
		//? if fabric {
		PayloadTypeRegistry.playS2C().register(MTR.PACKET_IDENTIFIER_S2C, PacketCodec.tuple(PacketCodecs.BYTE_ARRAY, CustomPacketS2C::buffer, CustomPacketS2C::new));
		PayloadTypeRegistry.playC2S().register(MTR.PACKET_IDENTIFIER_C2S, PacketCodec.tuple(PacketCodecs.BYTE_ARRAY, CustomPacketC2S::buffer, CustomPacketC2S::new));
		ServerPlayNetworking.registerGlobalReceiver(MTR.PACKET_IDENTIFIER_C2S, (customPacketC2S, context) -> PacketBufferReceiver.receive(customPacketC2S.buffer(), packetBufferReceiver -> {
			final Function<PacketBufferReceiver, ? extends PacketHandler> getInstance = MTRFabric.PACKETS.get(packetBufferReceiver.readString());
			if (getInstance != null) {
				getInstance.apply(packetBufferReceiver).runServer(context.player().server, context.player());
			}
		}, context.player().server::execute));
		//? }

		//? if neoforge {
		ModEventBus.PAYLOAD_HANDLERS.add(payloadRegistrar -> payloadRegistrar.playBidirectional(MTR.PACKET_IDENTIFIER_C2S, PacketCodec.tuple(PacketCodecs.BYTE_ARRAY, CustomPacketC2S::buffer, CustomPacketC2S::new), new DirectionalPayloadHandler<>((customPacketC2S, context) -> {
		}, (customPacketC2S, context) -> {
			final PlayerEntity player = context.player();
			if (player instanceof ServerPlayerEntity) {
				PacketBufferReceiver.receive(customPacketC2S.buffer(), packetBufferReceiver -> {
					final Function<PacketBufferReceiver, ? extends PacketHandler> getInstance = ModEventBus.PACKETS.get(packetBufferReceiver.readString());
					if (getInstance != null) {
						getInstance.apply(packetBufferReceiver).runServer(((ServerPlayerEntity) player).server, (ServerPlayerEntity) player);
					}
				}, ((ServerPlayerEntity) player).server::execute);
			}
		})));
		//? }
	}

	public static <T extends PacketHandler> void registerPacket(Class<T> classObject, Function<PacketBufferReceiver, T> getInstance) {
		//? if fabric {
		MTRFabric.PACKETS.put(classObject.getName(), getInstance);
		//? }

		//? if neoforge {
		ModEventBus.PACKETS.put(classObject.getName(), getInstance);
		//? }
	}

	public static <T extends PacketHandler> void sendPacketToClient(ServerPlayerEntity serverPlayerEntity, T data) {
		//? if fabric {
		final PacketBufferSender packetBufferSender = new PacketBufferSender();
		packetBufferSender.writeString(data.getClass().getName());
		data.write(packetBufferSender);
		packetBufferSender.send(bytes -> ServerPlayNetworking.send(serverPlayerEntity, new CustomPacketS2C(bytes)), serverPlayerEntity.server::execute);
		//? }

		//? if neoforge {
		final PacketBufferSender packetBufferSender = new PacketBufferSender();
		packetBufferSender.writeString(data.getClass().getName());
		data.write(packetBufferSender);
		packetBufferSender.send(bytes -> PacketDistributor.sendToPlayer(serverPlayerEntity, new CustomPacketS2C(bytes)), serverPlayerEntity.server::execute);
		//? }
	}

	//? if fabric {
	private static <T extends U, U> ObjectHolder<T> register(Registry<U> registry, RegistryKey<Registry<U>> registryKey, String registryName, Function<RegistryKey<U>, T> factory) {
		final RegistryKey<U> dataRegistryKey = RegistryKey.of(registryKey, Identifier.of(MTR.MOD_ID, registryName));
		final T data = Registry.register(registry, dataRegistryKey, factory.apply(dataRegistryKey));
		final ObjectHolder<T> objectHolder = new ObjectHolder<>(() -> data);
		OBJECTS_TO_REGISTER.add(objectHolder::get);
		return objectHolder;
	}
	//? }
}
