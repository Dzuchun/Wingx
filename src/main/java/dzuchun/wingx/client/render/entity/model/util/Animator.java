package dzuchun.wingx.client.render.entity.model.util;

import java.util.function.Supplier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dzuchun.wingx.util.Util;
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
		renderer = rendererIn;
		currentTimeSupplier = currentTimeSupplierIn;
	}

	public void animate() {
		synchronized (states_lock) {
//			LOG.debug("Animating... current states: {}", Util.iterableToString(states));
			long currentTime = currentTimeSupplier.get();
			int executingTo = 0;
			for (AnimationState state : states) {
				if (state.time > currentTime) {
					break;
				}
				executingTo++;
			}
			for (int i = 0; i < executingTo; i++) {
				AnimationState state = states.getSmallest();
				states.remove(state);
				LOG.debug("Removing {}", state);
				lastState = state;
			}
			if (states.isEmpty()) {
				return;
			}
			if (lastState == null) {
				lastState = getCurrentState();
			}
//			LOG.debug("Current states: {}", Util.iterableToString(states));
			AnimationState executingToState = states.getSmallest();
			float stage = getTimeStage(currentTime);
			renderer.rotationPointX = MathHelper.lerp(
					executingToState.fadeFunction.get(stage, AnimationParameter.X_POS), lastState.x,
					executingToState.x);
			renderer.rotationPointY = MathHelper.lerp(
					executingToState.fadeFunction.get(stage, AnimationParameter.Y_POS), lastState.y,
					executingToState.y);
			renderer.rotationPointZ = MathHelper.lerp(
					executingToState.fadeFunction.get(stage, AnimationParameter.Z_POS), lastState.z,
					executingToState.z);
			renderer.rotateAngleX = MathHelper.lerp(executingToState.fadeFunction.get(stage, AnimationParameter.X_ROT),
					lastState.xRot, executingToState.xRot);
			renderer.rotateAngleY = MathHelper.lerp(executingToState.fadeFunction.get(stage, AnimationParameter.Y_ROT),
					lastState.yRot, executingToState.yRot);
			renderer.rotateAngleZ = MathHelper.lerp(executingToState.fadeFunction.get(stage, AnimationParameter.Z_ROT),
					lastState.zRot, executingToState.zRot);
		}
	}

	private float getTimeStage(long currentTime) {
		AnimationState state = states.getSmallest();
		return state.time == lastState.time ? 1.0f
				: ((float)(currentTime - lastState.time)) / ((float)( state.time - lastState.time));
	}

	private AnimationState getCurrentState() {
		return new AnimationState(currentTimeSupplier.get(), FadeFunction.LINEAR, false, renderer);
	}

	public void addCurrentState() {
		synchronized (states_lock) {
			states.add(getCurrentState());
		}
	}

	public boolean addState(AnimationState stateIn) {
		if (stateIn == null || stateIn.time < currentTimeSupplier.get()) {
			return false;
		}
		synchronized (states_lock) {
			if (stateIn.interrupts) {
				this.addCurrentState();
				states.removeIf(state -> state.time < stateIn.time);
			}
			return states.add(stateIn);
		}
	}
}
