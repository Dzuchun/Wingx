package dzuchun.wingx.client.render.entity.model;

import java.util.List;
import java.util.function.Supplier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import dzuchun.wingx.entity.misc.WingsEntity;
import dzuchun.wingx.util.animation.AnimationState;
import dzuchun.wingx.util.animation.Animator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(value = Dist.CLIENT)
public class WingsModel<T extends Entity> extends EntityModel<WingsEntity> {
	@SuppressWarnings("unused")
	private static final Logger LOG = LogManager.getLogger();

	private ModelRenderer base, left, right;
	private Animator baseAni, leftAni, rightAni;

	public WingsModel() {
		super();

		this.base = new ModelRenderer(this);
		this.base.setRotationPoint(0.0F, 2.0F, 0.0F);

		this.left = new ModelRenderer(this);
		this.left.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.base.addChild(this.left);
		this.left.setTextureOffset(0, 0).addBox(-12.0F, 0.0F, 0.0F, 12.0F, 22.0F, 1.0f, 0.0F, false);

		this.right = new ModelRenderer(this);
		this.right.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.base.addChild(this.right);
		this.right.setTextureOffset(0, 0).addBox(0.F, 0.0F, 0.0F, 12.0F, 22.0F, 1.0F, 0.0F, false);
		this.right.mirror = true;
	}

	@Override
	public void render(MatrixStack matrixStackIn, IVertexBuilder bufferIn, int packedLightIn, int packedOverlayIn,
			float red, float green, float blue, float alpha) {
		this.base.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
	}

	@Override
	public void setRotationAngles(WingsEntity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks,
			float netHeadYaw, float headPitch) {
		@SuppressWarnings("unused")
		Minecraft minecraft = Minecraft.getInstance();
		if (this.baseAni == null) {
			@SuppressWarnings("resource")
			Supplier<Long> currentTimeSupplier = Minecraft.getInstance().world::getGameTime;

			this.baseAni = new Animator(this.base, currentTimeSupplier);
			this.leftAni = new Animator(this.left, currentTimeSupplier);
			this.rightAni = new Animator(this.right, currentTimeSupplier);
		}
		synchronized (entityIn.upcomingStates_lock) {
			List<List<AnimationState>> upcomingStates = entityIn.getUpcomingStates();
			for (List<AnimationState> states : upcomingStates) {
//				LOG.debug("Handling adding {}", Util.iterableToString(states));
				if (states != null) {
					if (addStateIfPresent(this.baseAni, states.get(0))) {
						states.set(0, null);
					}
					if (addStateIfPresent(this.leftAni, states.get(1))) {
						states.set(1, null);
					}
					if (addStateIfPresent(this.rightAni, states.get(2))) {
						states.set(2, null);
					}
				}
			}
			upcomingStates.clear();
		}
		this.baseAni.animate();
		this.leftAni.animate();
		this.rightAni.animate();
	}

	private boolean addStateIfPresent(Animator animator, AnimationState stateIn) {
		return animator.addState(stateIn) == null ? false : true;
	}

	public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
		modelRenderer.rotateAngleX = x;
		modelRenderer.rotateAngleY = y;
		modelRenderer.rotateAngleZ = z;
	}
}
