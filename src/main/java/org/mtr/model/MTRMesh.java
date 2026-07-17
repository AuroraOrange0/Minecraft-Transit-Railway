package org.mtr.model;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;

import java.util.function.Consumer;

/**
 * Compatibility mesh for Minecraft's extraction/drawing renderer.
 *
 * <p>This keeps the original vertex recipe and lets {@link RenderType} own the
 * 1.21.11 render pass. A later optimization can replace the per-draw upload with
 * a persistent {@code GpuBuffer} without changing model or map callers.</p>
 */
public final class MTRMesh implements AutoCloseable {

	private final VertexFormat.Mode drawMode;
	private final VertexFormat vertexFormat;
	private final Consumer<VertexConsumer> vertexWriter;

	public MTRMesh(VertexFormat.Mode drawMode, VertexFormat vertexFormat, Consumer<VertexConsumer> vertexWriter) {
		this.drawMode = drawMode;
		this.vertexFormat = vertexFormat;
		this.vertexWriter = vertexWriter;
	}

	public void draw(RenderType renderType, Matrix4f matrix, float red, float green, float blue, float alpha) {
		final Matrix4fStack modelViewStack = RenderSystem.getModelViewStack();
		modelViewStack.pushMatrix().mul(matrix);
		try {
			final BufferBuilder bufferBuilder = Tesselator.getInstance().begin(drawMode, vertexFormat);
			vertexWriter.accept(new TintingVertexConsumer(bufferBuilder, red, green, blue, alpha));
			final MeshData meshData = bufferBuilder.build();
			if (meshData != null) {
				renderType.draw(meshData);
			}
		} finally {
			modelViewStack.popMatrix();
		}
	}

	@Override
	public void close() {
		// No persistent GPU allocation yet.
	}

	private record TintingVertexConsumer(VertexConsumer delegate, float red, float green, float blue, float alpha) implements VertexConsumer {

		@Override
		public VertexConsumer addVertex(float x, float y, float z) {
			delegate.addVertex(x, y, z);
			return this;
		}

		@Override
		public VertexConsumer setColor(int red, int green, int blue, int alpha) {
			delegate.setColor(scale(red, this.red), scale(green, this.green), scale(blue, this.blue), scale(alpha, this.alpha));
			return this;
		}

		@Override
		public VertexConsumer setColor(int color) {
			return setColor(color >> 16 & 0xFF, color >> 8 & 0xFF, color & 0xFF, color >>> 24);
		}

		@Override
		public VertexConsumer setUv(float u, float v) {
			delegate.setUv(u, v);
			return this;
		}

		@Override
		public VertexConsumer setUv1(int u, int v) {
			delegate.setUv1(u, v);
			return this;
		}

		@Override
		public VertexConsumer setUv2(int u, int v) {
			delegate.setUv2(u, v);
			return this;
		}

		@Override
		public VertexConsumer setNormal(float x, float y, float z) {
			delegate.setNormal(x, y, z);
			return this;
		}

		@Override
		public VertexConsumer setLineWidth(float width) {
			delegate.setLineWidth(width);
			return this;
		}

		private static int scale(int value, float multiplier) {
			return Math.clamp(Math.round(value * multiplier), 0, 0xFF);
		}
	}
}
