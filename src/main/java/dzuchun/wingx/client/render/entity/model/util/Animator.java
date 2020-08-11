package dzuchun.wingx.client.render.entity.model.util;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.netty.util.internal.shaded.org.jctools.queues.MessagePassingQueue.Consumer;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.SortedArraySet;
import net.minecraft.util.math.MathHelper;

public class Animator {
	@SuppressWarnings("unused")
	private static final Logger LOG = LogManager.getLogger();
	private final List<AnimationFlow> flows = Arrays.asList(new AnimationFlow(), new AnimationFlow(),
			new AnimationFlow(), new AnimationFlow(), new AnimationFlow(), new AnimationFlow());
	private final Object states_lock = new Object();
	private ModelRenderer renderer;
	private Supplier<Long> currentTimeSupplier;

	public Animator(ModelRenderer rendererIn, Supplier<Long> currentTimeSupplierIn) {
		this.renderer = rendererIn;
		this.currentTimeSupplier = currentTimeSupplierIn;
	}

	public void animate() {
		synchronized (this.states_lock) {
			long currentTime = this.currentTimeSupplier.get();
			for (int i = 0; i < 6; i++) {
				AnimationFlow flow = this.flows.get(i);
				int executingTo = 0;
				for (AnimationValue value : flow.upcomingValues) {
					if (value.time > currentTime) {
						break;
					}
					executingTo++;
				}
				for (int j = 0; j < executingTo; j++) {
					AnimationValue value = flow.upcomingValues.getSmallest();
					flow.upcomingValues.remove(value);
					flow.lastValue = value;
				}

				if (!flow.upcomingValues.isEmpty()) {

					if (flow.lastValue == null) {
						flow.lastValue = getCurrentValue(i);
					}

					float stage = getTimeStage(i);
					AnimationValue value = flow.upcomingValues.getSmallest();
					rendererValueSetter(i).accept(MathHelper.lerp(stage, flow.lastValue.value, value.value));
				}
			}
		}
	}

	private float getTimeStage(int type) {
		AnimationFlow flow = this.flows.get(type);
		AnimationValue value = flow.upcomingValues.getSmallest();
		AnimationValue last = flow.lastValue;
		return value.time == last.time ? 1.0f
				: ((float) (this.currentTimeSupplier.get() - last.time)) / ((float) (value.time - last.time));
	}

	private AnimationState getCurrentState() {
		return new AnimationState(this.currentTimeSupplier.get(), FadeFunction.LINEAR, false, this.renderer);
	}

	public void addCurrentState() {
		synchronized (this.states_lock) {
			addStateUnchecked(getCurrentState());
		}
	}

	private Supplier<Float> rendererValueSupplier(int type) {
		switch (type) {
		case 0:
			return () -> this.renderer.rotationPointX;
		case 1:
			return () -> this.renderer.rotationPointY;
		case 2:
			return () -> this.renderer.rotationPointZ;
		case 3:
			return () -> this.renderer.rotateAngleX;
		case 4:
			return () -> this.renderer.rotateAngleY;
		case 5:
			return () -> this.renderer.rotateAngleZ;
		}
		return () -> null;
	}

	private Consumer<Float> rendererValueSetter(int type) {
		switch (type) {
		case 0:
			return (v) -> this.renderer.rotationPointX = v;
		case 1:
			return (v) -> this.renderer.rotationPointY = v;
		case 2:
			return (v) -> this.renderer.rotationPointZ = v;
		case 3:
			return (v) -> this.renderer.rotateAngleX = v;
		case 4:
			return (v) -> this.renderer.rotateAngleY = v;
		case 5:
			return (v) -> this.renderer.rotateAngleZ = v;
		}
		return (v) -> {
		};
	}

	public AnimationValue getCurrentValue(int type) {
		AnimationValue value = null;
		value = new AnimationValue(this.currentTimeSupplier.get(), rendererValueSupplier(type).get(), false);
		return value;
	}

	public AnimationState addState(AnimationState stateIn) {
		synchronized (this.states_lock) {
			return stateIn == null ? null : addStateUnchecked(stateIn);
		}
	}

	private AnimationState addStateUnchecked(@Nonnull AnimationState stateIn) {
		return new AnimationState(stateIn.time, stateIn.fadeFunction, stateIn.interrupts,
				addValueUnchecked(0, new AnimationValue(stateIn.time, stateIn.x, stateIn.interrupts)) ? stateIn.x
						: null,
				addValueUnchecked(1, new AnimationValue(stateIn.time, stateIn.y, stateIn.interrupts)) ? stateIn.y
						: null,
				addValueUnchecked(2, new AnimationValue(stateIn.time, stateIn.z, stateIn.interrupts)) ? stateIn.z
						: null,
				addValueUnchecked(3, new AnimationValue(stateIn.time, stateIn.xRot, stateIn.interrupts)) ? stateIn.xRot
						: null,
				addValueUnchecked(4, new AnimationValue(stateIn.time, stateIn.yRot, stateIn.interrupts)) ? stateIn.yRot
						: null,
				addValueUnchecked(5, new AnimationValue(stateIn.time, stateIn.zRot, stateIn.interrupts)) ? stateIn.zRot
						: null);
	}

	public boolean addValue(int typeIn, float valueIn, boolean delay, long time, boolean interruptsIn) {
		return addValue(typeIn,
				new AnimationValue(delay ? this.currentTimeSupplier.get() + time : time, valueIn, interruptsIn));
	}

	public boolean addValue(int typeIn, AnimationValue valueIn) {
		if (valueIn == null) {
			return false;
		}
		synchronized (this.states_lock) {
			return addValueUnchecked(typeIn, valueIn);
		}
	}

	/**
	 * NOT thread-safe!
	 *
	 * @param type
	 * @param value
	 * @return If value was successfully added.
	 */
	private boolean addValueUnchecked(int type, @Nonnull AnimationValue valueIn) {
		AnimationFlow flow = this.flows.get(type);
		SortedArraySet<AnimationValue> upcoming = flow.upcomingValues;
		upcoming.add(valueIn);
		int executingTo = 0;
		for (AnimationValue value : upcoming) {
			if (value.interrupts) {
				break;
			}
			executingTo++;
		}
		if (executingTo == upcoming.size()) {
			return true;
		}
		for (int i = 0; i < executingTo; i++) {
			upcoming.remove(upcoming.getSmallest());
		}
		return upcoming.contains(valueIn);
	}
}
