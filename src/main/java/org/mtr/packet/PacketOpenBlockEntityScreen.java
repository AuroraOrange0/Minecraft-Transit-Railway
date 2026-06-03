package org.mtr.packet;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.mtr.client.MinecraftClientData;
import org.mtr.core.operation.ListDataResponse;
import org.mtr.core.serializer.JsonReader;
import org.mtr.core.serializer.ReaderBase;
import org.mtr.core.serializer.SerializedDataBase;
import org.mtr.core.serializer.WriterBase;
import org.mtr.core.servlet.OperationProcessor;
import org.mtr.libraries.com.google.gson.JsonObject;

public final class PacketOpenBlockEntityScreen extends PacketRequestResponseBase {

	private final BlockPos blockPos;

	public PacketOpenBlockEntityScreen(PacketBufferReceiver packetBufferReceiver) {
		super(packetBufferReceiver);
		blockPos = BlockPos.fromLong(packetBufferReceiver.readLong());
	}

	private PacketOpenBlockEntityScreen(String content, BlockPos blockPos) {
		super(content);
		this.blockPos = blockPos;
	}

	@Override
	public void write(PacketBufferSender packetBufferSender) {
		super.write(packetBufferSender);
		packetBufferSender.writeLong(blockPos.asLong());
	}

	@Override
	protected void runClientInbound(JsonReader jsonReader) {
		new ListDataResponse(jsonReader, MinecraftClientData.getDashboardInstance()).write();
		ClientPacketHelper.openBlockEntityScreen(blockPos);
	}

	@Override
	protected PacketRequestResponseBase getInstance(String content) {
		return new PacketOpenBlockEntityScreen(content, blockPos);
	}

	@Override
	protected SerializedDataBase getDataInstance(JsonReader jsonReader) {
		return new SerializedDataBase() {
			@Override
			public void updateData(ReaderBase readerBase) {
			}

			@Override
			public void serializeData(WriterBase writerBase) {
			}
		};
	}

	@Override
	protected String getKey() {
		return OperationProcessor.LIST_DATA;
	}

	@Override
	protected PacketRequestResponseBase.ResponseType responseType() {
		return PacketRequestResponseBase.ResponseType.PLAYER;
	}

	public static void sendDirectlyToServer(ServerWorld serverWorld, ServerPlayerEntity serverPlayerEntity, BlockPos blockPos) {
		new PacketOpenBlockEntityScreen(new JsonObject().toString(), blockPos).runServerOutbound(serverWorld, serverPlayerEntity);
	}
}
