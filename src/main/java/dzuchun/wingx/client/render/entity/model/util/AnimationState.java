package dzuchun.wingx.client.render.entity.model.util;

import javax.annotation.Nullable;

import net.minecraft.client.renderer.model.ModelRenderer;

public class AnimationState implements Comparable<AnimationState> {
	public final long time;
	public final FadeFunction fadeFunction;
	public final Float x, y, z, xRot, yRot, zRot;
	public boolean interrupts;

	public AnimationState(AnimationState stateIn, long time, boolean interruptsIn) {
		this(time, stateIn.fadeFunction, interruptsIn, stateIn.x, stateIn.y, stateIn.z, stateIn.xRot, stateIn.yRot,
				stateIn.zRot);
	}

	public AnimationState(long timeIn, @Nullable FadeFunction fadeFunctionIn, boolean interruptsIn,
			ModelRenderer rednererIn) {
		this(timeIn, fadeFunctionIn, interruptsIn, rednererIn.rotationPointX, rednererIn.rotationPointY,
				rednererIn.rotationPointZ, rednererIn.rotateAngleX, rednererIn.rotateAngleY, rednererIn.rotateAngleZ);
	}

	public AnimationState(long timeIn, @Nullable FadeFunction fadeFunctionIn, boolean interruptsIn, @Nullable Float x,
			@Nullable Float y, @Nullable Float z, @Nullable Float xRot, @Nullable Float yRot, @Nullable Float zRot) {
		this.time = timeIn;
		this.fadeFunction = fadeFunctionIn == null ? FadeFunction.LINEAR : fadeFunctionIn;
		this.interrupts = interruptsIn;
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
