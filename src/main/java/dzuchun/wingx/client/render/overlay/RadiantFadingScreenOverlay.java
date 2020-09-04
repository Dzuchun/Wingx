package dzuchun.wingx.client.render.overlay;

import java.util.function.Consumer;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import dzuchun.wingx.util.MathHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector4f;
import net.minecraftforge.client.event.RenderGameOverlayEvent;

public class RadiantFadingScreenOverlay extends FadingScreenOverlay {

	public RadiantFadingScreenOverlay(Vector4f beginColor, Vector4f endColor, double ticksDuration,
			Consumer<Boolean> onDeactivate, FadeFunction fadeFunction) {
		super(beginColor, endColor, ticksDuration, onDeactivate, fadeFunction);
	}

	@Override
	void renderGameOverlay(RenderGameOverlayEvent event) {
		@SuppressWarnings("resource")
		double realGameTime = Minecraft.getInstance().world.getGameTime() + event.getPartialTicks();
		double partPassed = (realGameTime - this.beginTime) / this.ticksDuration;
		if (partPassed > 1 || partPassed < 0) {
			return;
		}
		double partColorPassed = this.fadeFunction.apply(partPassed);
		this.lastColor = MathHelper.lerpVector4f(this.beginColor, this.endColor, partColorPassed);
		// TODO fix overlay!!!
//		renderRadiantColorScreen(event, lastColor);
	}

	@SuppressWarnings("unused")
	private static void renderRadiantColorScreen(RenderGameOverlayEvent event, Vector4f color) {
		renderRadiantColorScreen(event.getMatrixStack(), color);
	}

	@SuppressWarnings("deprecation")
	private static void renderRadiantColorScreen(MatrixStack matrixStackIn, Vector4f color) {
		if (!Minecraft.isGuiEnabled()) {
			return;
		}

		int scaledScreenWidth = Minecraft.getInstance().getMainWindow().getScaledWidth();
		int scaledScreenHeight = Minecraft.getInstance().getMainWindow().getScaledHeight();
		RenderSystem.disableTexture();
		RenderSystem.disableDepthTest();
		RenderSystem.depthMask(false);
		RenderSystem.defaultBlendFunc();
//		GL11.glShadeModel(GL11.GL_FLAT);
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuffer();
		Matrix4f matrix = matrixStackIn.getLast().getMatrix();
		float r = color.getX();
		float g = color.getY();
		float b = color.getZ();
		float a = color.getW();
		GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
		bufferbuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

		bufferbuilder.pos(matrix, 0.0f, 0.0f, 0.0f).color(r, g, b, a).endVertex();
		bufferbuilder.pos(matrix, 0.0f, scaledScreenHeight / 2.0f, 0.0f).color(r / 2.0f, g / 2.0f, b / 2.0f, a / 2.0f)
				.endVertex();
		bufferbuilder.pos(matrix, scaledScreenWidth / 2.0f, scaledScreenHeight / 2.0f, 0.0f).color(0, 0, 0, 0)
				.endVertex();
		bufferbuilder.pos(matrix, scaledScreenWidth / 2.0f, 0.0f, 0.0f).color(r / 2.0f, g / 2.0f, b / 2.0f, a / 2.0f)
				.endVertex();

		bufferbuilder.pos(matrix, scaledScreenWidth / 2.0f, 0.0f, 0.0f).color(r / 2.0f, g / 2.0f, b / 2.0f, a / 2.0f)
				.endVertex();
		bufferbuilder.pos(matrix, scaledScreenWidth / 2.0f, scaledScreenHeight / 2.0f, 0.0f)
				.color(0.0f, 0.0f, 0.0f, 0.0f).endVertex();
		bufferbuilder.pos(matrix, scaledScreenWidth, scaledScreenHeight / 2.0f, 0.0f)
				.color(r / 2.0f, g / 2.0f, b / 2.0f, a / 2.0f).endVertex();
		bufferbuilder.pos(matrix, scaledScreenWidth, 0.0f, 0.0f).color(r, g, b, a).endVertex();

		bufferbuilder.pos(matrix, 0.0f, scaledScreenHeight / 2.0f, 0.0f).color(r / 2.0f, g / 2.0f, b / 2.0f, a / 2.0f)
				.endVertex();
		bufferbuilder.pos(matrix, 0.0f, scaledScreenHeight, 0.0f).color(r, g, b, a).endVertex();
		bufferbuilder.pos(matrix, scaledScreenWidth / 2.0f, scaledScreenHeight, 0.0f)
				.color(r / 2.0f, g / 2.0f, b / 2.0f, a / 2.0f).endVertex();
		bufferbuilder.pos(matrix, scaledScreenWidth / 2.0f, scaledScreenHeight / 2.0f, 0.0f)
				.color(0.0f, 0.0f, 0.0f, 0.0f).endVertex();

		bufferbuilder.pos(matrix, scaledScreenWidth / 2.0f, scaledScreenHeight / 2.0f, 0.0f)
				.color(0.0f, 0.0f, 0.0f, 0.0f).endVertex();
		bufferbuilder.pos(matrix, scaledScreenWidth / 2.0f, scaledScreenHeight, 0.0f)
				.color(r / 2.0f, g / 2.0f, b / 2.0f, a / 2.0f).endVertex();
		bufferbuilder.pos(matrix, scaledScreenWidth, scaledScreenHeight, 0.0f).color(r, g, b, a).endVertex();
		bufferbuilder.pos(matrix, scaledScreenWidth, scaledScreenHeight / 2.0f, 0.0f)
				.color(r / 2.0f, g / 2.0f, b / 2.0f, a / 2.0f).endVertex();

		tessellator.draw();

		RenderSystem.depthMask(true);
		RenderSystem.enableDepthTest();
		GL11.glShadeModel(GL11.GL_SMOOTH);
		RenderSystem.enableAlphaTest();
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.enableTexture();
	}
}
