package dzuchun.wingx.client.render.entity.model.util;

import java.util.function.Supplier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.SortedArraySet;
import net.minecraft.util.math.MathHelper;

public class Animator {
	private static final Logger LOG = LogManager.getLogger();
	private final SortedArraySet<AnimationState> states = SortedArraySet.newSet(0);
	private final Object states_lock = new Object();
	private ModelRenderer renderer;
	private Supplier<Long> currentTimeSupplier;
	private AnimationState lastState;

	public Animator(ModelRenderer rendererIn, Supplier<Long> currentTimeSupplierIn) {
		this.renderer = rendererIn;
		this.currentTimeSupplier = currentTimeSupplierIn;
	}

	public void animate() {
		synchronized (this.states_lock) {
//			LOG.debug("Animating... current states: {}", Util.iterableToString(states));
			long currentTime = this.currentTimeSupplier.get();
			int executingTo = 0;
			for (AnimationState state : this.states) {
				if (state.time > currentTime) {
					break;
				}
				executingTo++;
			}
			for (int i = 0; i < executingTo; i++) {
				AnimationState state = this.states.getSmallest();
				this.states.remove(state);
				LOG.debug("Removing {}", state);
				this.lastState = state;
			}
			if (this.states.isEmpty()) {
				return;
			}
			if (this.lastState == null) {
				this.lastState = getCurrentState();
			}
//			LOG.debug("Current states: {}", Util.iterableToString(states));
			AnimationState executingToState = this.states.getSmallest();
			float stage = getTimeStage(currentTime);
			this.renderer.rotationPointX = MathHelper.lerp(
					executingToState.fadeFunction.get(stage, AnimationParameter.X_POS), this.lastState.x,
					executingToState.x);
			this.renderer.rotationPointY = MathHelper.lerp(
					executingToState.fadeFunction.get(stage, AnimationParameter.Y_POS), this.lastState.y,
					executingToState.y);
			this.renderer.rotationPointZ = MathHelper.lerp(
					executingToState.fadeFunction.get(stage, AnimationParameter.Z_POS), this.lastState.z,
					executingToState.z);
			this.renderer.rotateAngleX = MathHelper.lerp(
					executingToState.fadeFunction.get(stage, AnimationParameter.X_ROT), this.lastState.xRot,
					executingToState.xRot);
			this.renderer.rotateAngleY = MathHelper.lerp(
					executingToState.fadeFunction.get(stage, AnimationParameter.Y_ROT), this.lastState.yRot,
					executingToState.yRot);
			this.renderer.rotateAngleZ = MathHelper.lerp(
					executingToState.fadeFunction.get(stage, AnimationParameter.Z_ROT), this.lastState.zRot,
					executingToState.zRot);
		}
	}

	private float getTimeStage(long currentTime) {
		AnimationState state = this.states.getSmallest();
		return state.time == this.lastState.time ? 1.0f
				: ((float) (currentTime - this.lastState.time)) / ((float) (state.time - this.lastState.time));
	}

	private AnimationState getCurrentState() {
		return new AnimationState(this.currentTimeSupplier.get(), FadeFunction.LINEAR, false, this.renderer);
	}

	public void addCurrentState() {
		synchronized (this.states_lock) {
			this.states.add(getCurrentState());
		}
	}

	public boolean addState(AnimationState stateIn) {
		if (stateIn == null || stateIn.time < this.currentTimeSupplier.get()) {
			return false;
		}
		synchronized (this.states_lock) {
			if (stateIn.interrupts) {
				addCurrentState();
				this.states.removeIf(state -> state.time < stateIn.time);
			}
			return this.states.add(stateIn);
		}
	}
}
