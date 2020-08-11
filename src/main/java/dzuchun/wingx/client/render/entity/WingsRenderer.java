package dzuchun.wingx.client.render.entity;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import dzuchun.wingx.Wingx;
import dzuchun.wingx.client.render.entity.model.WingsModel;
import dzuchun.wingx.entity.misc.WingsEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.culling.ClippingHelper;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class WingsRenderer extends EntityRenderer<WingsEntity> {
	@SuppressWarnings("unused")
	private static final Logger LOG = LogManager.getLogger();
	private static final ResourceLocation TEXTURE = new ResourceLocation(Wingx.MOD_ID,
			"textures/entity/misc/wings_texture.png");
	protected final WingsModel<WingsEntity> model = new WingsModel<WingsEntity>();

	public WingsRenderer(EntityRendererManager renderManagerIn) {
		super(renderManagerIn);
	}

	@Override
	public ResourceLocation getEntityTexture(WingsEntity entity) {
		return TEXTURE;
	}

	@Override
	public void render(WingsEntity entityIn, float entityYaw, float partialTicks, MatrixStack matrixStackIn,
			IRenderTypeBuffer bufferIn, int packedLightIn) {
		matrixStackIn.push();
		Vector3d move = entityIn.getRealPos().add(entityIn.getPositionVec().scale(-1))
				.add(entityIn.getMotion().scale(partialTicks + 1.0f));
		matrixStackIn.translate(move.x, move.y, move.z);
//		LOG.debug("Moving entity a bit: {}, entity at {}, real pos: {}, motion: {}", move, entityIn.getPositionVec(),
//				entityIn.getRealPos(), entityIn.getMotion());
		matrixStackIn.rotate(
				new Quaternion(Vector3f.YN, entityIn.getRealYaw() + entityIn.getRealYawSpeed() * partialTicks, true));
		this.model.setRotationAngles(entityIn, partialTicks, 0.0F, -0.1F, 0.0F, 0.0F); // TODO describe
		IVertexBuilder ivertexbuilder = bufferIn.getBuffer(this.model.getRenderType(getEntityTexture(entityIn)));
		this.model.render(matrixStackIn, ivertexbuilder, packedLightIn, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F,
				1.0F);
		matrixStackIn.pop();
		super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
	}

	@Override
	public boolean shouldRender(WingsEntity livingEntityIn, ClippingHelper camera, double camX, double camY,
			double camZ) {
		return true;
	}
}