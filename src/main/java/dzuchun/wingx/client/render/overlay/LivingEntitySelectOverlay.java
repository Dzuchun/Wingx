package dzuchun.wingx.client.render.overlay;

import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import dzuchun.wingx.client.render.entity.model.WingsModel;
import dzuchun.wingx.entity.misc.WingsEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLivingEvent;

@OnlyIn(Dist.CLIENT)
/**
 * Overlay used to select some LivingEntity within specified distance.
 *
 * @author Dzuchun
 */
public class LivingEntitySelectOverlay extends AbstractOverlay {
	private static final float MAX_ANGLE = 20;
	private static final double MAX_ANGLE_COS = MathHelper.cos((float) ((MAX_ANGLE / 180) * Math.PI));
	private static LivingEntitySelectOverlay instance = null;

	public static LivingEntitySelectOverlay getInstance() {
		return instance;
	}

	private static final ResourceLocation PIG_TEXTURES = new ResourceLocation("textures/entity/pig/pig.png");
	protected final WingsModel<WingsEntity> modelAll = new WingsModel<WingsEntity>();
	private double radiusSq = 0.0d;
	private boolean mustSee = true;
	private Predicate<LivingEntity> additionalCondition = (LivingEntity entity) -> true;

	public LivingEntitySelectOverlay(double radius, boolean mustSee, @Nullable Predicate<LivingEntity> other) {
		// TODO check if player unlocked targeting
		this.radiusSq = radius * radius;
		this.mustSee = mustSee;
		if (other != null) {
			this.additionalCondition = other;
		}
		if ((instance == null) || (instance.active == false)) {
			instance = this;
		}
	}

	@Override
	boolean conflicts(AbstractOverlay other) {
		return other instanceof LivingEntitySelectOverlay ? true : false;
	}

	private LivingEntity target = null;

	public LivingEntity getTarget() {
		return this.target;
	}

	private long prevTime = 0l;
	private double selectedAngleCos = -1.0d;
	private LivingEntity selectedEntity = null;

	@Override
	public void renderLiving(@SuppressWarnings("rawtypes") RenderLivingEvent event) {
		LivingEntity entity = event.getEntity();
		float partialTicks = event.getPartialRenderTick();
		Minecraft minecraft = Minecraft.getInstance();
		double d0 = minecraft.player.getDistanceSq(entity);
		if ((d0 > this.radiusSq) || (this.mustSee && !minecraft.player.canEntityBeSeen(entity))
				|| !this.additionalCondition.test(entity)) {
			return;
		}
		float red = 1.0f;
		float green = 0.9f;
		float blue = 0.5f;
		if (minecraft.world.getGameTime() != this.prevTime) {
			this.prevTime = minecraft.world.getGameTime();
			this.selectedEntity = null;
			this.selectedAngleCos = -1.0d;
			minecraft.world.getAllEntities().forEach((Entity entityCurrent) -> {
				if ((minecraft.player.getDistanceSq(entityCurrent) <= this.radiusSq)
						&& (entityCurrent instanceof LivingEntity)
						&& (!this.mustSee || minecraft.player.canEntityBeSeen(entityCurrent))) {
					float playerYaw = (float) (((minecraft.player.rotationYawHead / 180.0f) * Math.PI)
							+ (Math.PI / 2.0d));
					float playerPitch = (float) ((-minecraft.player.rotationPitch / 180.f) * Math.PI);
					Vector3d playerEyesPos = minecraft.player.getEyePosition(partialTicks);
					double xDelta = entityCurrent.getPosX() - playerEyesPos.getX();
					double yDelta = entityCurrent.getPosY() - playerEyesPos.getY();
					double zDelta = entityCurrent.getPosZ() - playerEyesPos.getZ();
					double xZDelta = Math.sqrt((xDelta * xDelta) + (zDelta * zDelta));
					float entityPitch = (float) Math.atan(yDelta / xZDelta);
					float entityYaw;
					if (xDelta > 0) {
						entityYaw = (float) Math.atan(zDelta / xDelta);
					} else {
						entityYaw = (float) (Math.atan(zDelta / xDelta) + Math.PI);
					}
					float deltaYaw = entityYaw - playerYaw;
					float entityPitch_ = (float) ((Math.PI / 2) - entityPitch);
					float playerPitch_ = (float) ((Math.PI / 2) - playerPitch);
					double currentCos = (MathHelper.cos(playerPitch_) * MathHelper.cos(entityPitch_))
							+ (MathHelper.sin(playerPitch_) * MathHelper.sin(entityPitch_) * MathHelper.cos(deltaYaw));
					if ((currentCos >= MAX_ANGLE_COS) && (currentCos > this.selectedAngleCos)) {
						this.selectedEntity = (LivingEntity) entityCurrent;
						this.selectedAngleCos = currentCos;
					}
				}
			});
		}
		if ((this.selectedEntity != null) && entity.equals(this.selectedEntity)) {
			red = 0.0F;
			green = 1.0F;
			blue = 0.0F;
		}
		IRenderTypeBuffer buffer = event.getBuffers();
		IVertexBuilder vertexBuilder = buffer.getBuffer(this.modelAll.getRenderType(PIG_TEXTURES));
		// TODO include trasparency
		MatrixStack matrixStack = event.getMatrixStack();
		int packedLight = event.getLight();
		matrixStack.push();
		matrixStack.translate(-0.5d, 1.0d, -0.5d);
		this.modelAll.render(matrixStack, vertexBuilder, packedLight, OverlayTexture.NO_OVERLAY, red, green, blue,
				1.0f);
		// TODO instead use MathHelper.useFadeOut(0.0f, 1.0f, 0.9d, 0.0d, radiusSq, d0)
		matrixStack.pop();
	}

	@Override
	public void deactivate() {
		deactivate(this);
		this.active = false;
		instance = null;
	}

	public LivingEntity getSelectedEnttity() {
		return this.selectedEntity;
	}

	@Override
	public boolean activate() {
		this.active = activate(this);
		return this.active;
	}
}
