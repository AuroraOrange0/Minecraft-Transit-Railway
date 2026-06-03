package org.mtr.registry;

import com.mojang.serialization.Codec;
import net.minecraft.component.ComponentType;
import net.minecraft.util.math.BlockPos;
import org.mtr.MTR;

public final class DataComponentTypes {

	public static final ObjectHolder<ComponentType<BlockPos>> START_POS = RegistryServer.registerDataComponentType("start_pos", () -> ComponentType.<BlockPos>builder().codec(BlockPos.CODEC).build());
	public static final ObjectHolder<ComponentType<String>> TRANSPORT_MODE = RegistryServer.registerDataComponentType("transport_mode", () -> ComponentType.<String>builder().codec(Codec.STRING).build());
	public static final ObjectHolder<ComponentType<Integer>> BLOCK_ID = RegistryServer.registerDataComponentType("block_id", () -> ComponentType.<Integer>builder().codec(Codec.INT).build());
	public static final ObjectHolder<ComponentType<Long>> DEPOT_ID = RegistryServer.registerDataComponentType("depot_id", () -> ComponentType.<Long>builder().codec(Codec.LONG).build());
	public static final ObjectHolder<ComponentType<Long>> EXPIRY_TIME = RegistryServer.registerDataComponentType("expiry_time", () -> ComponentType.<Long>builder().codec(Codec.LONG).build());

	public static void init() {
		MTR.LOGGER.info("Registering Minecraft Transit Railway data component types");
	}
}
