package dzuchun.wingx.client.render.entity;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.system.MathUtil;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import dzuchun.wingx.client.render.entity.model.WingsModel;
import dzuchun.wingx.entity.misc.WingsEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class WingsRenderer extends EntityRenderer<WingsEntity> {
	private static final Logger LOG = LogManager.getLogger();
	private static final ResourceLocation PIG_TEXTURES = new ResourceLocation("textures/entity/pig/pig.png");
	protected final WingsModel<WingsEntity> model = new WingsModel<WingsEntity>();

	public WingsRenderer(EntityRendererManager renderManagerIn) {
		super(renderManagerIn);
	}

	/**
	 * Returns the location of an entity's texture.
	 */
	public ResourceLocation getEntityTexture(WingsEntity entity) {
		return PIG_TEXTURES;
	}

	private Long prevPosTime = 0L;
	private Vector3d prevPos = Vector3d.ZERO;
	@Override
	public void render(WingsEntity entityIn, float entityYaw, float partialTicks, MatrixStack matrixStackIn,
			IRenderTypeBuffer bufferIn, int packedLightIn) {
//		if (!prevPos.equals(entityIn.getRealPos())) {
//			Long currentTime = System.currentTimeMillis();
//			LOG.info("Render after {}ms at new position: {}", currentTime-prevPosTime, entityIn.getRealPos());
//			prevPosTime = currentTime;
//			prevPos = entityIn.getRealPos();
//		}
		matrixStackIn.push();
//		Vector3d move = entityIn.getRealMotion().scale(partialTicks).add(entityIn.getRealPos().add(entityIn.getPositionVec().scale(-1)));
		Vector3d move = entityIn.getRealPos().add(entityIn.getPositionVec().scale(-1)).add(entityIn.getMotion().scale(partialTicks+1.0f));
//		if (entityIn.getOwner().getDistanceSq(entityIn.getPositionVec().add(move)) > entityIn.getRealMotion().lengthSquared()) {
//			move.add(entityIn.getRealMotion());
//		}
		matrixStackIn.translate(move.x, move.y, move.z);
		LOG.info("Moving entity a bit: {}, entity at {}, real pos: {}, motion: {}", move, entityIn.getPositionVec(), entityIn.getRealPos(), entityIn.getMotion());
//		move = entityIn.getRealMotion().scale(partialTicks);
//		matrixStackIn.translate(move.x, move.y, move.z);
//		matrixStackIn.rotate(new Quaternion(Vector3f.YN, entityYaw + entityIn.getRealYawSpeed()*partialTicks, true));
		matrixStackIn.rotate(new Quaternion(Vector3f.YN, entityIn.getRealYaw() + entityIn.getRealYawSpeed() * partialTicks, true));
		model.setRotationAngles(entityIn, partialTicks, 0.0F, -0.1F, 0.0F, 0.0F); // TODO describe
		IVertexBuilder ivertexbuilder = bufferIn.getBuffer(model.getRenderType(this.getEntityTexture(entityIn)));
		model.render(matrixStackIn, ivertexbuilder, packedLightIn, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
		matrixStackIn.pop();
		super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
	}
}