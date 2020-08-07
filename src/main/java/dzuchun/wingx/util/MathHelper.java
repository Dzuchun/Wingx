package dzuchun.wingx.util;

import net.minecraft.util.math.vector.Vector4f;

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

	public static Vector4f lerpVector4f(Vector4f begin, Vector4f end, double partPassed) {
		return new Vector4f(net.minecraft.util.math.MathHelper.lerp((float) partPassed, begin.getX(), end.getX()),
				net.minecraft.util.math.MathHelper.lerp((float) partPassed, begin.getY(), end.getY()),
				net.minecraft.util.math.MathHelper.lerp((float) partPassed, begin.getZ(), end.getZ()),
				net.minecraft.util.math.MathHelper.lerp((float) partPassed, begin.getW(), end.getW()));
	}

	public static Vector4f unpackColor(int packedColor) {
		int r = packedColor >> 24;
		int g = packedColor & 0x00FF0000 >> 16;
		int b = packedColor & 0x0000FF00 >> 8;
		int a = packedColor & 0x000000FF;
		return new Vector4f(r/256f, g/256f, b/256f, a/256f);
	}
}
