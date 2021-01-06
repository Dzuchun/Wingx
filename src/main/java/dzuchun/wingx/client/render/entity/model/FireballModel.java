package dzuchun.wingx.client.render.entity.model;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import dzuchun.wingx.entity.projectile.FireballEntity;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;

public class FireballModel extends EntityModel<FireballEntity> {
	@SuppressWarnings("unused")
	private static final Logger LOG = LogManager.getLogger();

	private final ModelRenderer body;
	private final ModelRenderer main;
	private final ModelRenderer second;
	private final ModelRenderer particles;
	private final ModelRenderer part1;
	private final ModelRenderer part2;
	private final ModelRenderer part3;

	public FireballModel() {
		super(RenderType::getEntityTranslucentCull);

		this.textureWidth = 32;
		this.textureHeight = 32;

		this.body = new ModelRenderer(this);
		this.body.setRotationPoint(0.0F, 0.0f, 0.0F);

		this.main = new ModelRenderer(this);
		this.main.setRotationPoint(0.0F, 0.0F, 10.0F);
		this.body.addChild(this.main);
		this.main.setTextureOffset(0, 0).addBox(-3.0F, -3.0F, -13.0F, 6.0F, 6.0F, 6.0F, 0.0F, false);

		this.second = new ModelRenderer(this);
		this.second.setRotationPoint(0.0F, 0.0F, 6.0F);
		this.body.addChild(this.second);
		this.setRotationAngle(this.second, 0.0F, 0.0F, -0.5236F);
		this.second.setTextureOffset(0, 12).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 4.0F, 4.0F, 0.0F, false);

		this.particles = new ModelRenderer(this);
		this.particles.setRotationPoint(0.0F, 0.0F, 9.5F);
		this.body.addChild(this.particles);

		this.part1 = new ModelRenderer(this);
		this.part1.setRotationPoint(0.0F, 0.0F, 0.5F);
		this.particles.addChild(this.part1);
		this.setRotationAngle(this.part1, 0.2182F, 1.1781F, 0.0F);
		this.part1.setTextureOffset(0, 0).addBox(0.0F, -2.0F, -1.0F, 1.0F, 1.0F, 1.0F, 0.0F, false);

		this.part2 = new ModelRenderer(this);
		this.part2.setRotationPoint(0.0F, 0.0F, 0.5F);
		this.particles.addChild(this.part2);
		this.setRotationAngle(this.part2, 0.0F, -0.2182F, 0.3054F);
		this.part2.setTextureOffset(0, 4).addBox(-2.0F, 1.0F, 0.0F, 1.0F, 1.0F, 1.0F, 0.0F, false);

		this.part3 = new ModelRenderer(this);
		this.part3.setRotationPoint(0.0F, 0.0F, -0.5F);
		this.particles.addChild(this.part3);
		this.setRotationAngle(this.part3, 0.0F, -1.4835F, -0.6981F);
		this.part3.setTextureOffset(0, 2).addBox(1.0F, 1.0F, -1.0F, 1.0F, 1.0F, 1.0F, 0.0F, false);
	}

	@Override
	public void setRotationAngles(FireballEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks,
			float netHeadYaw, float headPitch) {
		this.setRotationAngle(this.main, 0.0f, 0.0f, getAngle(100.0F, ageInTicks));
		this.setRotationAngle(this.second, 0.0f, 0.0f, -getAngle(58.5F, ageInTicks));
		this.setRotationAngle(this.particles,
				convertToRadians(15.0F) * (float) Math.cos(1.634F + getAngle(40.0F, ageInTicks)),
				convertToRadians(21.0F) * (float) Math.cos(0.346F + getAngle(40.0F, ageInTicks)),
				getAngle(25.0F, ageInTicks));
	}

	@Override
	public void render(MatrixStack matrixStackIn, IVertexBuilder bufferIn, int packedLightIn, int packedOverlayIn,
			float red, float green, float blue, float alpha) {
		this.body.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
	}

	public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
		modelRenderer.rotateAngleX = x;
		modelRenderer.rotateAngleY = y;
		modelRenderer.rotateAngleZ = z;
	}

	private static float getAngle(float period, float existed) {
		return (float) ((existed / period) * Math.PI * 2.0f);
	}

	private static float convertToRadians(float angle) {
		return (float) ((angle / 180.0f) * Math.PI);
	}

}
