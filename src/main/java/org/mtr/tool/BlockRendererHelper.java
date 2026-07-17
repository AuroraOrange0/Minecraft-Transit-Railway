package org.mtr.tool;

import gg.essential.universal.UMatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.mtr.libraries.it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.mtr.widget.ImageComponentBase;

import java.awt.*;
import java.util.List;

public final class BlockRendererHelper {

	private static final Object2ObjectOpenHashMap<String, ReleasedDynamicTextureRegistry.Holder> BLOCK_TEXTURE_MAP = new Object2ObjectOpenHashMap<>();
	public static void renderBlock(UMatrixStack matrixStack, BlockState blockState, long renderKey, double x, double y, double z, double brightness) {
		for (final net.minecraft.client.renderer.block.model.BlockModelPart modelPart : Minecraft.getInstance().getBlockRenderer().getBlockModel(blockState).collectParts(RandomSource.create())) {
			for (int i = -1; i < Direction.values().length; i++) {
				final List<BakedQuad> bakedQuads = modelPart.getQuads(i < 0 ? null : Direction.values()[i]);

				for (int j = 0; j < bakedQuads.size(); j++) {
					final BakedQuad bakedQuad = bakedQuads.get(j);
					final TextureAtlasSprite sprite = bakedQuad.sprite();
					final Identifier tempIdentifier = sprite.contents().name();
				final Identifier newIdentifier = Identifier.fromNamespaceAndPath(tempIdentifier.getNamespace(), String.format("textures/%s.png", tempIdentifier.getPath()));
				final String newRenderKey = String.format("%s_%s_%s_%s", tempIdentifier, i, j, renderKey);

				ImageComponentBase.drawTexture(BLOCK_TEXTURE_MAP.computeIfAbsent(newIdentifier.toString(), key -> ReleasedDynamicTextureRegistry.INSTANCE.create(newIdentifier)).get(), vertexConsumer -> {
					for (int k = 0; k < BakedQuad.VERTEX_COUNT; k++) {
						final org.joml.Vector3fc position = bakedQuad.position(k);
						final long packedUv = bakedQuad.packedUV(k);
						final int color = Math.clamp((int) Math.floor(255 * brightness), 0, 255);
						vertexConsumer.pos(
							matrixStack,
							x + position.x(),
							y + position.y(),
							z + position.z()
						).tex(
							(Float.intBitsToFloat((int) packedUv) - sprite.getU0()) / (sprite.getU1() - sprite.getU0()),
							(Float.intBitsToFloat((int) (packedUv >>> 32)) - sprite.getV0()) / (sprite.getV1() - sprite.getV0())
						).color(new Color(color, color, color)).endVertex();
					}
				});
				}
			}
		}
	}
}
