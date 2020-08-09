package dzuchun.wingx.client.render.gui;

import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import dzuchun.wingx.Wingx;
import dzuchun.wingx.trick.AbstractInterruptablePlayerTrick;
import dzuchun.wingx.trick.ITimeredTrick;
import dzuchun.wingx.util.MathHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector4f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGameOverlayEvent;

@OnlyIn(value = Dist.CLIENT)
@SuppressWarnings("deprecation")
public class SeparateRenderers {
	@SuppressWarnings("unused")
	private static final Logger LOG = LogManager.getLogger();
	protected static final ResourceLocation COOLDOWN_BAR_HORIZONTAL_PATH = new ResourceLocation(Wingx.MOD_ID,
			"textures/gui/ingame/cooldown_bar_horizontal.png");
	private static final int defaultScreenWidth = 1920;
	private static final int defaultScreenHeight = 1080;
	private static final int defaultWidth = 256;
	private static final int defaultHeight = 32;
	private static final double xOffset = 5.0d;
	private static final double yOffset = 5.0d;

//	// Texture sprite
//	public static TextureAtlasSprite GUI_INGAME_COOLDOWN_HORIZONTAL_SPRITE;

	public static void defaultDrawCastingOverlay(RenderGameOverlayEvent event) {
		if (!Minecraft.isGuiEnabled()) {
			return;
		}

		ArrayList<AbstractInterruptablePlayerTrick> tricks = AbstractInterruptablePlayerTrick.getForMe();
		if (tricks == null) {
			return;
		}
		tricks.forEach((trick) -> {
			if (trick instanceof ITimeredTrick) {
//				LOG.debug("Drawing overlay for trick {}, cast ended - {}", trick, trick.castEndedNaturally());
				defaultDrawCastingOverlayInner(event, (ITimeredTrick) trick);
			}
		});
	}

	private static void defaultDrawCastingOverlayInner(RenderGameOverlayEvent event, ITimeredTrick trick) {
		Minecraft minecraft = Minecraft.getInstance();
		double partLeft = trick.partLeft();

//		LOG.debug("Rendering bar, part completed - {}", partLeft);

		int scaledScreenWidth = Minecraft.getInstance().getMainWindow().getScaledWidth();
		int scaledScreenHeight = Minecraft.getInstance().getMainWindow().getScaledHeight();
		double scaledWidth = (double) defaultWidth * (double) scaledScreenWidth / defaultScreenWidth;
		double scaledHeight = (double) defaultHeight * (double) scaledScreenHeight / defaultScreenHeight;
//		LOG.debug("Scaled width: {}, scaled height: {}", scaledWidth, scaledHeight);
		double xMin = scaledScreenWidth - xOffset - scaledWidth;
		double xMax = scaledScreenWidth - xOffset;
		double yMin = scaledScreenHeight - yOffset - scaledHeight;
		double yMax = scaledScreenHeight - yOffset;

		RenderSystem.disableDepthTest();
		RenderSystem.depthMask(false);
		RenderSystem.defaultBlendFunc();
		RenderSystem.disableAlphaTest();
		minecraft.getTextureManager().bindTexture(COOLDOWN_BAR_HORIZONTAL_PATH);
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuffer();

		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
		bufferbuilder.pos(xMin, yMax, 0.0D).tex(0.0F, 0.5F).endVertex();
		bufferbuilder.pos(xMax, yMax, 0.0D).tex(1.0f, 0.5F).endVertex();
		bufferbuilder.pos(xMax, yMin, 0.0D).tex(1.0f, 0.0F).endVertex();
		bufferbuilder.pos(xMin, yMin, 0.0D).tex(0.0F, 0.0F).endVertex();
		tessellator.draw();

		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 0.5F);
		bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
		bufferbuilder.pos(xMin, yMax, 0.0D).tex(0.0F, 1.0f).endVertex();
		bufferbuilder.pos(xMin + scaledWidth * partLeft, yMax, 0.0D).tex((float) (1.0f * partLeft), 1.0F).endVertex();
		bufferbuilder.pos(xMin + scaledWidth * partLeft, yMin, 0.0D).tex((float) (1.0f * partLeft), 0.5F).endVertex();
		bufferbuilder.pos(xMin, yMin, 0.0D).tex(0.0F, 0.5F).endVertex();

		tessellator.draw();

		RenderSystem.depthMask(true);
		RenderSystem.enableDepthTest();
		RenderSystem.enableAlphaTest();
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
	}

	public static void renderColorScreen(RenderGameOverlayEvent event, Vector4f color) {
		renderColorScreen(event.getMatrixStack(), color);
	}

	public static void renderColorScreen(MatrixStack matrixStackIn, Vector4f color) {
		if (!Minecraft.isGuiEnabled()) {
			return;
		}

//		Damn, that's so stupid, that I'm gonna leave it here.
//		(With this uncommented entire function does not work, if no active AbstractPlayerTrick exist)
//		(may contain deprecated or non-existent now methods)
//		AbstractInterruptablePlayerTrick trick = AbstractInterruptablePlayerTrick.getForMe();
//		if (trick == null) {
//			return;
//		}

		int scaledScreenWidth = Minecraft.getInstance().getMainWindow().getScaledWidth();
		int scaledScreenHeight = Minecraft.getInstance().getMainWindow().getScaledHeight();
//		Matrix4f matrix = event.getMatrixStack().getLast().getMatrix();

		RenderSystem.disableTexture();
		RenderSystem.disableDepthTest();
		RenderSystem.depthMask(false);
		RenderSystem.defaultBlendFunc();
		RenderSystem.shadeModel(GL11.GL_FLAT);
		RenderSystem.disableAlphaTest();
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuffer();
		Matrix4f matrix = matrixStackIn.getLast().getMatrix();
		float r = color.getX();
		float g = color.getY();
		float b = color.getZ();
		float a = color.getW();
		RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
		bufferbuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

		bufferbuilder.pos(matrix, 0.0f, 0.0f, 0.0f).color(r, g, b, a).endVertex();
		bufferbuilder.pos(matrix, 0.0f, scaledScreenHeight, 0.0f).color(r, g, b, a).endVertex();
		bufferbuilder.pos(matrix, scaledScreenWidth, scaledScreenHeight, 0.0f).color(r, g, b, a).endVertex();
		bufferbuilder.pos(matrix, scaledScreenWidth, 0.0f, 0.0f).color(r, g, b, a).endVertex();

		tessellator.draw();

		RenderSystem.depthMask(true);
		RenderSystem.enableDepthTest();
		RenderSystem.shadeModel(GL11.GL_SMOOTH);
		RenderSystem.enableAlphaTest();
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.enableTexture();
	}

	public static void myBlit(MatrixStack matrixStackIn, int xMin, int yMin, int width, int height, float uMin,
			float vMin, float uWidth, float vHeight, Minecraft mc, ResourceLocation texture) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThread);
		mc.getTextureManager().bindTexture(texture);

		RenderSystem.disableDepthTest();
		RenderSystem.depthMask(false);
		RenderSystem.defaultBlendFunc();
		RenderSystem.enableBlend();
		RenderSystem.shadeModel(GL11.GL_FLAT);
//		RenderSystem.disableAlphaTest();
		RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);

		Matrix4f matrix = matrixStackIn.getLast().getMatrix();
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder builder = tessellator.getBuffer();
		builder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		builder.pos(matrix, xMin, yMin + height, 0).tex(uMin, vMin + vHeight).endVertex();
		builder.pos(matrix, xMin + width, yMin + height, 0).tex(uMin + uWidth, vMin + vHeight).endVertex();
		builder.pos(matrix, xMin + width, yMin, 0).tex(uMin + uWidth, vMin).endVertex();
		builder.pos(matrix, xMin, yMin, 0).tex(uMin, vMin).endVertex();
		tessellator.draw();

		RenderSystem.depthMask(true);
		RenderSystem.enableDepthTest();
		RenderSystem.enableAlphaTest();
		RenderSystem.shadeModel(GL11.GL_SMOOTH);
		RenderSystem.disableBlend();
	}

	public static void drawLine(MatrixStack matrixStackIn, int packedColorIn, float width, double xBegin, double yBegin,
			double xEnd, double yEnd) {
		drawLine(matrixStackIn, packedColorIn, width, xBegin, yBegin, 0d, xEnd, yEnd, 0d);
	}

	public static void drawLine(MatrixStack matrixStackIn, int packedColorIn, float width, double xBegin, double yBegin,
			double zBegin, double xEnd, double yEnd, double zEnd) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThread);
		Vector4f color = MathHelper.unpackColor(packedColorIn);

		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.color4f(0.0F, 1.0F, 0.0F, 0.75F);
		RenderSystem.disableTexture();

		Matrix4f matrix = matrixStackIn.getLast().getMatrix();
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder builder = tessellator.getBuffer();
		builder.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
		GlStateManager.lineWidth(width);
//		LOG.debug("Rendering line from [{}, {}, {}] to [{}, {}, {}], with color {}", xBegin, yBegin, zBegin, xEnd, yEnd,
//				zEnd, color);
		builder.pos(matrix, (int) (xBegin), (int) (yBegin), (int) (zBegin))
				.color(color.getX(), color.getY(), color.getZ(), color.getW()).endVertex();
		builder.pos(matrix, (int) (xEnd), (int) (yEnd), (int) (zEnd))
				.color(color.getX(), color.getY(), color.getZ(), color.getW()).endVertex();
		tessellator.draw();

		RenderSystem.enableTexture();
		RenderSystem.disableBlend();
	}
}
