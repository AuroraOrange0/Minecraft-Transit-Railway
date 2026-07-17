package org.mtr.model;

import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import org.joml.Matrix4f;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

/**
 * One drawable mesh — a single texture, a single GPU vertex buffer, one draw call.
 *
 * <p>Instances are created during model build (see
 * {@link NewOptimizedModelGroup#build(VertexFormat.Mode)}) and live for the rest of
 * the parent loader's lifetime. The buffer is uploaded once via
 * {@link VertexBuffer#uploadStatic}; subsequent frames just call {@link #render} and
 * the GPU re-uses the stored vertices.</p>
 *
 * <p>If the supplied {@code callback} is {@code null}, no buffer is allocated and both
 * {@link #begin} and {@link #render} no-op. This is the path for empty groups.</p>
 *
 * <p><b>Naming note:</b> the "New" prefix is historical — there is no non-{@code New}
 * counterpart any more. See {@code docs/MIGRATIONS.md} §4 for the planned rename.</p>
 */
public final class NewOptimizedModel {

	public final Identifier texture;
	@Nullable
	private final MTRMesh mesh;
	/**
	 * Pre-built {@code colorModulator} arrays for every value of {@code lightMultiplier}
	 * the renderer ever emits. The renderer derives {@code lightMultiplier} from a 4-bit
	 * lightmap value (16 levels), or sets {@code 1.0f} for full-bright stages. We pack
	 * that into a 17-entry table — index {@code 0..15} for {@code i / 15f}, index
	 * {@code 16} for the full-bright case. Callers that supply a different value (e.g.
	 * fractional dimming for cinematic fades) fall back to a freshly allocated array.
	 * See {@code docs/PERFORMANCE.md} §3.2 item 1.
	 */
	private static final float[][] COLOR_MODULATORS;

	static {
		COLOR_MODULATORS = new float[17][];
		for (int i = 0; i < 16; i++) {
			final float v = i / 15f;
			COLOR_MODULATORS[i] = new float[]{v, v, v, 1f};
		}
		COLOR_MODULATORS[16] = new float[]{1f, 1f, 1f, 1f};
	}

	/**
	 * @param texture  the texture this mesh draws with
	 * @param drawMode the GL primitive type ({@code TRIANGLES} for OBJ, {@code QUADS} for
	 *                 Blockbench)
	 * @param callback the vertex-emitter callback invoked exactly once to populate the
	 *                 buffer, or {@code null} to skip buffer creation entirely
	 */
	public NewOptimizedModel(Identifier texture, VertexFormat.Mode drawMode, @Nullable Consumer<VertexConsumer> callback) {
		this.mesh = callback == null ? null : createMesh(drawMode, DefaultVertexFormat.NEW_ENTITY, callback);
		this.texture = texture;
	}

	/**
	 * Issue the draw call through Minecraft's current render type.
	 *
	 * @param matrix4f        the per-instance model matrix multiplied into the active view matrix
	 * @param lightMultiplier brightness scale in {@code [0, 1]}; {@code 1} for full-bright stages
	 * @param renderType      the render pipeline and texture state for this stage
	 */
	public void render(Matrix4f matrix4f, float lightMultiplier, RenderType renderType) {
		if (mesh != null) {
			mesh.draw(renderType, matrix4f, lightMultiplier, lightMultiplier, lightMultiplier, 1);
		}
	}

	public static MTRMesh createMesh(VertexFormat.Mode drawMode, VertexFormat vertexFormat, Consumer<VertexConsumer> callback) {
		return new MTRMesh(drawMode, vertexFormat, callback);
	}

	private static float[] colorModulatorFor(float lightMultiplier) {
		// Map [0, 1] → 16-step quantised table when possible; otherwise allocate.
		if (lightMultiplier >= 1f) {
			return COLOR_MODULATORS[16];
		}
		if (lightMultiplier <= 0f) {
			return COLOR_MODULATORS[0];
		}
		final int index = Math.round(lightMultiplier * 15f);
		final float quantised = index / 15f;
		// Only return the cached array when the requested value rounds cleanly — otherwise
		// allocate to preserve exact appearance for callers passing arbitrary floats.
		//noinspection FloatingPointEquality
		if (quantised == lightMultiplier) {
			return COLOR_MODULATORS[index];
		}
		return new float[]{lightMultiplier, lightMultiplier, lightMultiplier, 1f};
	}

}
