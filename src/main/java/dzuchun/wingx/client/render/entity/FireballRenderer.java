package dzuchun.wingx.client.render.entity;

import com.mojang.blaze3d.matrix.MatrixStack;

import dzuchun.wingx.Wingx;
import dzuchun.wingx.client.render.entity.model.FireballModel;
import dzuchun.wingx.entity.projectile.FireballEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3f;

public class FireballRenderer extends EntityRenderer<FireballEntity> {

	private static final ResourceLocation TEXTURE = new ResourceLocation(Wingx.MOD_ID,
			"textures/entity/projectile/fireball_texture.png");

	private FireballModel model;

	public FireballRenderer(EntityRendererManager renderManager) {
		super(renderManager);
		this.shadowSize = 1.0f;
		this.model = new FireballModel();
	}

	@Override
	public ResourceLocation getEntityTexture(FireballEntity entity) {
		return TEXTURE;
	}

	@Override
	public void render(FireballEntity entityIn, float entityYaw, float partialTicks, MatrixStack matrixStackIn,
			IRenderTypeBuffer bufferIn, int packedLightIn) {
		matrixStackIn.push();
		matrixStackIn.translate(0.0f, 3f / 16f, 0.0f);
		matrixStackIn.rotate(Vector3f.YN.rotationDegrees(entityYaw + 180.0f));
		matrixStackIn.rotate(Vector3f.XN.rotationDegrees(entityIn.getPitch(partialTicks)));
		matrixStackIn.push();
		this.model.setRotationAngles(entityIn, 0.0f, 0.0f, entityIn.ticksExisted + partialTicks,
				entityIn.getYaw(partialTicks), entityIn.getPitch(partialTicks));
		this.model.render(matrixStackIn, bufferIn.getBuffer(this.model.getRenderType(TEXTURE)), packedLightIn,
				OverlayTexture.NO_OVERLAY, 1.0f, 1.0f, 1.0f, entityIn.getAlpha());
		matrixStackIn.pop();
		matrixStackIn.pop();
		super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
	}

}
