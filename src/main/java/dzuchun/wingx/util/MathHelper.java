package dzuchun.wingx.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.util.math.vector.Vector4f;

public class MathHelper {
	@SuppressWarnings("unused")
	private static final Logger LOG = LogManager.getLogger();

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

	// TODO change format!!
	public static Vector4f unpackColor(int packedColor) {
		int r = packedColor >> 24 & 255;
		int g = packedColor >> 16 & 255;
		int b = packedColor >> 8 & 255;
		int a = packedColor & 255;
		Vector4f color = new Vector4f(r / 255f, g / 255f, b / 255f, a / 255f);
		return color;
	}

	public static int packColor(float r, float g, float b, float a) {
		return packColor(r * 255, g * 255, b * 255, a * 255);
	}

	public static int packColor(int r, int g, int b, int a) {
		int res = r;
		res = res << 8;
		res = res | g;
		res = res << 8;
		res = res | b;
		res = res << 8;
		res = res | a;
		return res;
	}
}
