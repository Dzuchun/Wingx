package dzuchun.wingx.client.render.overlay;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLivingEvent;

@OnlyIn(value = Dist.CLIENT)
public class LivingEntityTargetOverlay extends AbstractOverlay {

	private static final Map<UUID, LivingEntityTargetOverlay> activeInstances = new LinkedHashMap<UUID, LivingEntityTargetOverlay>(
			0);
	private static final Object ACTIVE_INSTANCES_LOCK = new Object();

	@Nullable
	public static LivingEntityTargetOverlay getOverlayForTarget(Entity target) {
		return activeInstances.get(target.getUniqueID());
	}

	protected LivingEntity target;

	public LivingEntityTargetOverlay(LivingEntity entity) { // TODO add constructor with specified model to render and
															// fade function
		this.target = entity;
	}

	@Override
	boolean conflicts(AbstractOverlay other) {
		return false;
	}

	@Override
	public boolean activate() {
		if (target != null && AbstractOverlay.activate(this)) {
			synchronized (ACTIVE_INSTANCES_LOCK) {
				activeInstances.put(target.getUniqueID(), this);
			}
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void deactivate() {
		AbstractOverlay.deactivate(this);
		synchronized (ACTIVE_INSTANCES_LOCK) {
			activeInstances.remove(target.getUniqueID());
		}
	}

	@Override
	void renderLiving(@SuppressWarnings("rawtypes") RenderLivingEvent event) {
		super.renderLiving(event);
		if (event.getEntity().equals(target)) {
//			IRenderTypeBuffer buffer = event.getBuffers();
			GL11.glPointSize(10.0f);
			GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
			MatrixStack matrixStack = event.getMatrixStack();
			Tessellator tesselator = Tessellator.getInstance();
			BufferBuilder builder = tesselator.getBuffer();
			builder.begin(GL11.GL_POINTS, DefaultVertexFormats.POSITION_COLOR);
			builder.pos(matrixStack.getLast().getMatrix(), 0, 1.0f, 0).color(1.0f, 1.0f, 1.0f, 1.0f).endVertex();
			// TODO render
			tesselator.draw();
		}
	}

}
