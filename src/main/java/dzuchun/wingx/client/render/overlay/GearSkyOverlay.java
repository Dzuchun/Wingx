package dzuchun.wingx.client.render.overlay;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import dzuchun.wingx.Wingx;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.client.event.RenderWorldLastEvent;

public class GearSkyOverlay extends AbstractOverlay {
	public static GearSkyOverlay instance = null;
	private static final ResourceLocation GEAR_TEXTURE = new ResourceLocation(Wingx.MOD_ID,
			"textures/environment/gear.png");

	@Override
	boolean conflicts(AbstractOverlay other) {
		return other instanceof GearSkyOverlay;
	}

	@Override
	public boolean activate() {
		if (activate(this)) {
			this.active = true;
			instance = this;
		}
		return this.active;
	}

	@Override
	public void deactivate() {
		deactivate(this);
		instance = null;
	}

	@SuppressWarnings("deprecation")
	@Override
	void renderWorldLast(RenderWorldLastEvent event) {
		super.renderWorldLast(event);
		Minecraft minecraft = Minecraft.getInstance();
		MatrixStack matrixStack = event.getMatrixStack();
		float partialTicks = event.getPartialTicks();
		ClientWorld world = minecraft.world;
		float f11 = 1.0F - world.getRainStrength(partialTicks);

		FogRenderer.applyFog();
		RenderSystem.enableDepthTest();
		RenderSystem.depthMask(false);
		RenderSystem.enableFog();
		RenderSystem.disableAlphaTest();
		RenderSystem.enableBlend();
		RenderSystem.enableTexture();
		RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE,
				GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, f11);

		minecraft.textureManager.bindTexture(GEAR_TEXTURE);
		Tessellator tesselator = Tessellator.getInstance();
		BufferBuilder builder = tesselator.getBuffer();
		matrixStack.push();
		matrixStack.rotate(Vector3f.YP.rotationDegrees(-30.0F));
//		matrixStack.push();
		matrixStack.rotate(Vector3f.ZP.rotationDegrees((world.getGameTime()) + (partialTicks / 360.0f)));
		matrixStack.scale(1.0f, -1.0f, 1.0f);
		Matrix4f matrix4f1 = matrixStack.getLast().getMatrix();
		float gearSize = 60.0f / 2.0f;
		builder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		builder.pos(matrix4f1, -gearSize, -gearSize, 100.0F).tex(0.0F, 0.0F).endVertex();
		builder.pos(matrix4f1, gearSize, -gearSize, 100.0F).tex(1.0F, 0.0F).endVertex();
		builder.pos(matrix4f1, gearSize, gearSize, 100.0F).tex(1.0F, 1.0F).endVertex();
		builder.pos(matrix4f1, -gearSize, gearSize, 100.0F).tex(0.0F, 1.0F).endVertex();
		tesselator.draw();

//		matrixStack.pop();
		matrixStack.pop();

		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.disableBlend();
		RenderSystem.enableAlphaTest();
		RenderSystem.enableFog();
		RenderSystem.depthMask(true);
		RenderSystem.disableFog();
	}
}
