package dzuchun.wingx.util.animation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dzuchun.wingx.util.Util;
import io.netty.util.internal.shaded.org.jctools.queues.MessagePassingQueue.Consumer;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.SortedArraySet;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
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
//					LOG.debug("Last value {}, lerping to {}", flow.lastValue, value);
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
		return new AnimationState(this.currentTimeSupplier.get(), FadeFunction.LINEAR, 0, this.renderer);
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
		value = new AnimationValue(this.currentTimeSupplier.get(), rendererValueSupplier(type).get(), 0);
		return value;
	}

	public AnimationState addState(AnimationState stateIn) {
		synchronized (this.states_lock) {
//			LOG.debug("Adding {} to {}", stateIn, this);
			return stateIn == null ? null : addStateUnchecked(stateIn);
		}
	}

	private AnimationState addStateUnchecked(@Nonnull AnimationState stateIn) {
		return new AnimationState(stateIn.time, stateIn.fadeFunction, stateIn.priority,
				addValueUnchecked(0, new AnimationValue(stateIn.time, stateIn.x, stateIn.priority)) ? stateIn.x : null,
				addValueUnchecked(1, new AnimationValue(stateIn.time, stateIn.y, stateIn.priority)) ? stateIn.y : null,
				addValueUnchecked(2, new AnimationValue(stateIn.time, stateIn.z, stateIn.priority)) ? stateIn.z : null,
				addValueUnchecked(3, new AnimationValue(stateIn.time, stateIn.xRot, stateIn.priority)) ? stateIn.xRot
						: null,
				addValueUnchecked(4, new AnimationValue(stateIn.time, stateIn.yRot, stateIn.priority)) ? stateIn.yRot
						: null,
				addValueUnchecked(5, new AnimationValue(stateIn.time, stateIn.zRot, stateIn.priority)) ? stateIn.zRot
						: null);
	}

	public boolean addValue(int typeIn, float valueIn, boolean delay, long time, int priorityIn) {
		return addValue(typeIn,
				new AnimationValue(delay ? this.currentTimeSupplier.get() + time : time, valueIn, priorityIn));
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
		if (upcoming.isEmpty()) {
			upcoming.add(valueIn);
//			LOG.debug("Added value {}. Current flow: {}", valueIn, upcoming);
			return true;
		}
		AnimationValue check = upcoming.getSmallest();
		upcoming.add(valueIn);
		int currentPriority = 0;
		ArrayList<AnimationValue> array = Util.computeNewArrayList(upcoming, v -> v);
		int size = upcoming.size();
		for (int i = size - 1; i >= 0; i--) {
			AnimationValue value = array.get(i);
			if (value.priority < currentPriority) {
				upcoming.remove(value);
			}
			if (value.priority > currentPriority) {
				currentPriority = value.priority;
			}
		}
		if (!upcoming.getSmallest().equals(check)) {
			upcoming.add(getCurrentValue(type));
		}
//		LOG.debug("Added value {}. Current flow: {}", valueIn, upcoming);
		return upcoming.contains(valueIn);
	}
}
