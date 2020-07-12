package dzuchun.wingx.client.render.entity;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import dzuchun.wingx.client.render.entity.model.WingsModel;
import dzuchun.wingx.entity.misc.WingsEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;
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

	@OnlyIn(value = Dist.CLIENT)
	@Override
	public void render(WingsEntity entityIn, float entityYaw, float partialTicks, MatrixStack matrixStackIn,
			IRenderTypeBuffer bufferIn, int packedLightIn) {
		PlayerEntity owner = entityIn.getOwner();
		if (owner != null) {
			matrixStackIn.push();
			Vector3d move = owner.getPositionVec().add(entityIn.getPositionVec().scale(-1));
			matrixStackIn.translate(move.x, move.y, move.z);
			LOG.info("Moving matrix a bit: {}", move.toString());
			model.setRotationAngles(entityIn, partialTicks, 0.0F, -0.1F, 0.0F, 0.0F);
			IVertexBuilder ivertexbuilder = bufferIn.getBuffer(model.getRenderType(this.getEntityTexture(entityIn)));
			model.render(matrixStackIn, ivertexbuilder, packedLightIn, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F,
					1.0F);

			matrixStackIn.pop();
		} else {
			LOG.info("Owner is null, not rendering");
		}
		super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
	}
}