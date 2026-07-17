package org.mtr.block;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.Arrays;

public abstract class BlockEntityExtension extends BlockEntity {

	public BlockEntityExtension(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Override
	public final Packet<ClientGamePacketListener> getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public final CompoundTag getUpdateTag(HolderLookup.Provider registries) {
		return saveWithoutMetadata(registries);
	}

	@Override
	protected final void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		readNbt(input);
	}

	@Override
	protected final void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		writeNbt(output);
	}

	protected void readNbt(ValueInput input) {
	}

	protected void writeNbt(ValueOutput output) {
	}

	protected static long[] getLongArray(ValueInput input, String key) {
		return input.read(key, Codec.LONG_STREAM).orElseGet(java.util.stream.LongStream::empty).toArray();
	}

	protected static void putLongArray(ValueOutput output, String key, long[] values) {
		output.store(key, Codec.LONG_STREAM, Arrays.stream(values));
	}
}
