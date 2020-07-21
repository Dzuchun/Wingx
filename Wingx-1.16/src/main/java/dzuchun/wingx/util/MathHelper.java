package dzuchun.wingx.util;

public class MathHelper {
	public static float useFadeOut(float min, float max, double fadeOutRelativeCoord, double minValue, double maxValue,
			double valueIn) {
		double d0 = minValue + fadeOutRelativeCoord * (maxValue - minValue);
		if (valueIn <= d0) {
			return max;
		}
		if (valueIn > maxValue) {
			return 0.0f;
		}
		return (float) (max - (max - min) * (valueIn - d0) / (maxValue - d0));
	}
}
