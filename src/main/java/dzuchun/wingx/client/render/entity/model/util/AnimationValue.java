package dzuchun.wingx.client.render.entity.model.util;

public class AnimationValue implements Comparable<AnimationValue> {
	public long time;
	public float value;
	public boolean interrupts;

	public AnimationValue(long timeIn, float valueIn, boolean interruptsIn) {
		this.time = timeIn;
		this.value = valueIn;
		this.interrupts = interruptsIn;
	}

	@Override
	public int compareTo(AnimationValue o) {
		return (int) (this.time - o.time);
	}
}
