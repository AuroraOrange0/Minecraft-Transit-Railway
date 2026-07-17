package org.mtr.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

/**
 * This implementation of a {@link BlockEntityRenderer} provides pre-transformed matrices and other helpful parameters.
 *
 * @param <T> the block entity type
 */
public abstract class BlockEntityRendererExtension<T extends BlockEntity> implements BlockEntityRenderer<T, BlockEntityRendererExtension.MTRBlockEntityRenderState<T>> {

	@Override
	public final MTRBlockEntityRenderState<T> createRenderState() {
		return new MTRBlockEntityRenderState<>();
	}

	@Override
	public final void extractRenderState(T blockEntity, MTRBlockEntityRenderState<T> renderState, float tickDelta, Vec3 cameraPosition, ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
		BlockEntityRenderState.extractBase(blockEntity, renderState, crumblingOverlay);
		renderState.blockEntity = blockEntity;
		renderState.tickDelta = tickDelta;
	}

	@Override
	public final void submit(MTRBlockEntityRenderState<T> renderState, PoseStack matrixStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
		final T blockEntity = renderState.blockEntity;
		if (blockEntity == null) {
			return;
		}
		final Level world = blockEntity.getLevel();
		final Minecraft minecraft = Minecraft.getInstance();
		final LocalPlayer clientPlayerEntity = minecraft.player;
		if (world instanceof ClientLevel clientWorld && clientPlayerEntity != null) {
			matrixStack.pushPose();
			matrixStack.translate(0.5, 0, 0.5);
			render(blockEntity, matrixStack, minecraft.renderBuffers().bufferSource(), clientWorld, clientPlayerEntity, renderState.tickDelta, renderState.lightCoords, OverlayTexture.NO_OVERLAY);
			matrixStack.popPose();
		}
	}

	/**
	 * A better implementation of the render method with helpful parameters.
	 *
	 * @param blockEntity            the {@link BlockEntity}
	 * @param matrixStack            a pre-transformed {@link PoseStack} centred at (0.5, 0, 0.5) of the block
	 * @param vertexConsumerProvider the provided {@link MultiBufferSource}
	 * @param clientWorld            the {@link ClientLevel} guaranteed to be non-null
	 * @param clientPlayerEntity     the {@link LocalPlayer} guaranteed to be non-null
	 * @param tickDelta              the number of ticks elapsed
	 * @param light                  the light level of the block
	 * @param overlay                the texture overlay
	 */
	public abstract void render(T blockEntity, PoseStack matrixStack, MultiBufferSource vertexConsumerProvider, ClientLevel clientWorld, LocalPlayer clientPlayerEntity, float tickDelta, int light, int overlay);

	public static final class MTRBlockEntityRenderState<T extends BlockEntity> extends BlockEntityRenderState {

		private T blockEntity;
		private float tickDelta;
	}
}
