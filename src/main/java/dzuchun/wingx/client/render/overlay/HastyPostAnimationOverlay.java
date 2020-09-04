package dzuchun.wingx.client.render.overlay;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import dzuchun.wingx.Wingx;
import dzuchun.wingx.capability.entity.wings.storage.HastyData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector4f;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.TickEvent.ClientTickEvent;

public class HastyPostAnimationOverlay extends AbstractTickingOverlay {
	private static final Logger LOG = LogManager.getLogger();
	private static final int DURATION = 15;
	private static final ResourceLocation TEXTURE = new ResourceLocation(Wingx.MOD_ID,
			"textures/block/hasty_overlay.png");
//	private static final ResourceLocation FANCY_MARK = new ResourceLocation(Wingx.MOD_ID,
//			"textures/block/hasty_overlay_fancy_mask.png");
	// TOOD is unused, delete texture
	private static final VertexFormat VERTEX_FORMAT = new VertexFormat(ImmutableList
			.of(DefaultVertexFormats.POSITION_3F, DefaultVertexFormats.COLOR_4UB, DefaultVertexFormats.TEX_2F));

	private long endTime;

	private long beginTime;

	private BlockPos blockPos;
	@SuppressWarnings("unused")
	private HastyData data;

	public HastyPostAnimationOverlay(BlockPos blockPos, HastyData data) {
		this.blockPos = blockPos;
	}

	@Override
	boolean conflicts(AbstractOverlay other) {
		return false;
	}

	@SuppressWarnings("resource")
	@Override
	public void onClienTick(ClientTickEvent event) {
		if (this.endTime < Minecraft.getInstance().world.getGameTime()) {
			this.active = false;
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void renderWorldLast(RenderWorldLastEvent event) {
		MatrixStack matrixStack = event.getMatrixStack();
		Minecraft minecraft = Minecraft.getInstance();
		ClientPlayerEntity player = minecraft.player;
		float partialTicks = event.getPartialTicks();
		Vector3d eyePos = player.getEyePosition(partialTicks);
		double x = this.blockPos.getX() - eyePos.x;
		double y = this.blockPos.getY() - eyePos.y;
		double z = this.blockPos.getZ() - eyePos.z;
		matrixStack.push();
		matrixStack.translate(x, y, z);
		float partPassed = Math.min((partialTicks + minecraft.world.getGameTime() - this.beginTime) / (DURATION), 1.0f);
		float oneMinusPartPassed = 1.0f - partPassed;
		minecraft.textureManager.bindTexture(TEXTURE);
		RenderSystem.enableAlphaTest();
		RenderSystem.defaultBlendFunc();
		RenderSystem.enableBlend();
		Tessellator tesselator = Tessellator.getInstance();
		BufferBuilder builder = tesselator.getBuffer();
		Matrix4f matrix = matrixStack.getLast().getMatrix();
		builder.begin(GL11.GL_QUADS, VERTEX_FORMAT);
		// TODO add light
//		ClientWorld world = minecraft.world;
//		world.getLight(pos);

		renderLayer(builder, matrix, 0.01f,
				dzuchun.wingx.util.MathHelper.packColor(1.0f, 1.0f, 1.0f, oneMinusPartPassed));
		if (Minecraft.isFancyGraphicsEnabled() || true) {
			// TODO remove true
			float alphaPerBlock = -10.0f;
			for (float d = 0.02f; d < 0.1f; d += 0.01f) {
				// TODO parametrize!!!
				renderLayer(builder, matrix, d, dzuchun.wingx.util.MathHelper.packColor(1.0f, 1.0f, 1.0f,
						Math.max(oneMinusPartPassed + alphaPerBlock * d, 0.0f)));
			}
		}

		tesselator.draw();
		matrixStack.pop();
		RenderSystem.disableBlend();
	}

	private static void renderLayer(BufferBuilder builder, Matrix4f matrix, float d, int color) {
		float min = 0.0f - d;
		float max = 1.0f + d;
		myAddQuad(builder, matrix, min, min, max, min, max, min, color);// x
		myAddQuad(builder, matrix, max, min, min, max, max, max, color);

		myAddQuad(builder, matrix, min, min, max, max, min, min, color);// y
		myAddQuad(builder, matrix, min, max, min, max, max, max, color);

		myAddQuad(builder, matrix, min, max, min, max, min, min, color);// z
		myAddQuad(builder, matrix, min, min, max, max, max, max, color);
	}

	private static void myAddQuad(BufferBuilder builder, Matrix4f matrix, float xBegin, float yBegin, float zBegin,
			float xEnd, float yEnd, float zEnd, int packedColor) {
		Vector4f color = dzuchun.wingx.util.MathHelper.unpackColor(packedColor);
		float r = color.getX();
		float g = color.getY();
		float b = color.getZ();
		float a = color.getW();
		if (xBegin == xEnd) {
			builder.pos(matrix, xBegin, yBegin, zBegin).color(r, g, b, a).tex(0.0f, 0.0f).endVertex();
			builder.pos(matrix, xBegin, yEnd, zBegin).color(r, g, b, a).tex(1.0f, 0.0f).endVertex();
			builder.pos(matrix, xBegin, yEnd, zEnd).color(r, g, b, a).tex(1.0f, 1.0f).endVertex();
			builder.pos(matrix, xBegin, yBegin, zEnd).color(r, g, b, a).tex(0.0f, 1.0f).endVertex();
		} else if (yBegin == yEnd) {
			builder.pos(matrix, xBegin, yBegin, zBegin).color(r, g, b, a).tex(0.0f, 0.0f).endVertex();
			builder.pos(matrix, xBegin, yBegin, zEnd).color(r, g, b, a).tex(1.0f, 0.0f).endVertex();
			builder.pos(matrix, xEnd, yBegin, zEnd).color(r, g, b, a).tex(1.0f, 1.0f).endVertex();
			builder.pos(matrix, xEnd, yBegin, zBegin).color(r, g, b, a).tex(0.0f, 1.0f).endVertex();
		} else if (zBegin == zEnd) {
			builder.pos(matrix, xBegin, yBegin, zBegin).color(r, g, b, a).tex(0.0f, 0.0f).endVertex();
			builder.pos(matrix, xEnd, yBegin, zBegin).color(r, g, b, a).tex(1.0f, 0.0f).endVertex();
			builder.pos(matrix, xEnd, yEnd, zBegin).color(r, g, b, a).tex(1.0f, 1.0f).endVertex();
			builder.pos(matrix, xBegin, yEnd, zBegin).color(r, g, b, a).tex(0.0f, 1.0f).endVertex();
		} else {
			LOG.warn("Cannot add quad, not alighned to axis: from [{}, {}, {}] to [{}, {}, {}]", xBegin, yBegin, zBegin,
					xEnd, yEnd, zEnd);
			return;
		}
	}

	@SuppressWarnings("resource")
	@Override
	public boolean activate() {
		LOG.debug("Activating overlay {}", toString());
		if (!super.activate()) {
			LOG.warn("Could not activate overlay of {} type", this.getClass().getName());
			return false;
		}
		this.active = true;
		this.beginTime = Minecraft.getInstance().world.getGameTime();
		this.endTime = this.beginTime + DURATION;
		return true;
	}

	@Override
	public String toString() {
		return String.format("%s[blockPos=%s]", this.getClass().getName(), this.blockPos.toString());
	}
}
