package dzuchun.wingx.util.animation;

public class AnimationValue implements Comparable<AnimationValue> {
	public long time;
	public float value;
	public int priority;

	public AnimationValue(long timeIn, float valueIn, int priority) {
		this.time = timeIn;
		this.value = valueIn;
		this.priority = priority;
	}

	@Override
	public int compareTo(AnimationValue o) {
		return (int) (this.time - o.time);
	}

	@Override
	public String toString() {
		return String.format("AnimationValue[time=%s, value=%s, priority=%s]", this.time + "", this.value + "",
				this.priority + "");
	}
}
