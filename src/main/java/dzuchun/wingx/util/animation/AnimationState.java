package dzuchun.wingx.util.animation;

import javax.annotation.Nullable;

import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class AnimationState implements Comparable<AnimationState> {
	public final long time;
	public final FadeFunction fadeFunction;
	public final Float x, y, z, xRot, yRot, zRot;
	public int priority;

	public AnimationState(AnimationState stateIn, long time, int priorityIn) {
		this(time, stateIn.fadeFunction, priorityIn, stateIn.x, stateIn.y, stateIn.z, stateIn.xRot, stateIn.yRot,
				stateIn.zRot);
	}

	@OnlyIn(Dist.CLIENT)
	public AnimationState(long timeIn, @Nullable FadeFunction fadeFunctionIn, int priorityIn,
			ModelRenderer rednererIn) {
		this(timeIn, fadeFunctionIn, priorityIn, rednererIn.rotationPointX, rednererIn.rotationPointY,
				rednererIn.rotationPointZ, rednererIn.rotateAngleX, rednererIn.rotateAngleY, rednererIn.rotateAngleZ);
	}

	public AnimationState(long timeIn, @Nullable FadeFunction fadeFunctionIn, int priorityIn, @Nullable Float x,
			@Nullable Float y, @Nullable Float z, @Nullable Float xRot, @Nullable Float yRot, @Nullable Float zRot) {
		this.time = timeIn;
		this.fadeFunction = fadeFunctionIn == null ? FadeFunction.LINEAR : fadeFunctionIn;
		this.priority = priorityIn;
		this.x = x;
		this.y = y;
		this.z = z;
		this.xRot = xRot;
		this.yRot = yRot;
		this.zRot = zRot;
	}

	@Override
	public int compareTo(AnimationState o) {
		return (int) (this.time - o.time);
	}
}
