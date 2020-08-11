package dzuchun.wingx.client.render.entity.model;

import java.util.List;
import java.util.function.Supplier;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import dzuchun.wingx.client.render.entity.model.util.AnimationState;
import dzuchun.wingx.client.render.entity.model.util.Animator;
import dzuchun.wingx.entity.misc.WingsEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(value = Dist.CLIENT)
public class WingsModel<T extends Entity> extends EntityModel<WingsEntity> {

	private ModelRenderer base, left, right;
	private Animator baseAni, leftAni, rightAni;

	public WingsModel() {
		super();

		base = new ModelRenderer(this);
		base.setRotationPoint(0.0F, 2.0F, 0.0F);

		left = new ModelRenderer(this);
		left.setRotationPoint(0.0F, 0.0F, 0.0F);
		base.addChild(left);
		left.setTextureOffset(0, 0).addBox(-12.0F, 0.0F, 0.0F, 12.0F, 22.0F, 1.0F, 0.0F, false);

		right = new ModelRenderer(this);
		right.setRotationPoint(0.0F, 0.0F, 0.0F);
		base.addChild(right);
		right.setTextureOffset(0, 0).addBox(0.F, 0.0F, 0.0F, 12.0F, 22.0F, 1.0F, 0.0F, false);
		right.mirror = true;
	}

	@Override
	public void render(MatrixStack matrixStackIn, IVertexBuilder bufferIn, int packedLightIn, int packedOverlayIn,
			float red, float green, float blue, float alpha) {
		base.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
	}

	@Override
	public void setRotationAngles(WingsEntity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks,
			float netHeadYaw, float headPitch) {
		Minecraft minecraft = Minecraft.getInstance();
		if (baseAni == null) {
			@SuppressWarnings("resource")
			Supplier<Long> currentTimeSupplier = Minecraft.getInstance().world::getGameTime;

			baseAni = new Animator(base, currentTimeSupplier);
			leftAni = new Animator(left, currentTimeSupplier);
			rightAni = new Animator(right, currentTimeSupplier);
		}
		synchronized (entityIn.upcomingStates_lock) {
			List<AnimationState> states = entityIn.upcomingStates;
			if (states != null) {
				if (addStateIfPresent(baseAni, states.get(0))) {
					states.set(0, null);
				}
				baseAni.animate();
				if (addStateIfPresent(leftAni, states.get(1))) {
					states.set(1, null);
				}
				leftAni.animate();
				if (addStateIfPresent(rightAni, states.get(2))) {
					states.set(2, null);
				}
				rightAni.animate();
			}
		}
	}

	private boolean addStateIfPresent(Animator animator, AnimationState state) {
		if (state != null) {
			return animator.addState(state);
		}
		return false;
	}

	public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
		modelRenderer.rotateAngleX = x;
		modelRenderer.rotateAngleY = y;
		modelRenderer.rotateAngleZ = z;
	}
}
