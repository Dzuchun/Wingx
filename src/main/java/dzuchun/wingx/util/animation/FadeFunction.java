package dzuchun.wingx.util.animation;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

import javax.annotation.Nullable;

import dzuchun.wingx.Wingx;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

/**
 * @author Dzuchun
 *
 */
public class FadeFunction {

	private static final ConcurrentHashMap<ResourceLocation, FadeFunction> registry = new ConcurrentHashMap<ResourceLocation, FadeFunction>(
			0);

	public static final FadeFunction LINEAR = create(null, new ResourceLocation(Wingx.MOD_ID, "linear"));
	public static final FadeFunction EASE_IN = create((f, p) -> f * f, new ResourceLocation(Wingx.MOD_ID, "ease_in"));
	public static final FadeFunction EASE_OUT = create((f, p) -> 2f - f * f,
			new ResourceLocation(Wingx.MOD_ID, "ease_out"));
	public static final FadeFunction EASE_IN_OUT = create((f, p) -> f < 0.5f ? 2 * f * f : 4 * f - 2 * f * f - 1,
			new ResourceLocation(Wingx.MOD_ID, "ease_in_out"));

	public static void init() {
	}

	public static FadeFunction create(BiFunction<Float, AnimationParameter, Float> func, ResourceLocation nameIn) {
		FadeFunction res = new FadeFunction();
		res.innerGet = func == null ? (d, p) -> d : func;
		res.name = nameIn;
		registry.put(nameIn, res);
		return res;
	}

	@Nullable
	public static FadeFunction getByName(ResourceLocation name) {
		return registry.get(name);
	}

	private FadeFunction() {
	}

	private BiFunction<Float, AnimationParameter, Float> innerGet;
	private ResourceLocation name;

	/**
	 * @param d [0.0, 1.0] number representing time of animation
	 * @return [0.0, 1.0] number representing stage of animation
	 */
	public final float get(float d, AnimationParameter p) {
		return MathHelper.clamp(this.innerGet.apply(MathHelper.clamp(d, 0.0f, 1.0f), p), 0.0f, 1.0f);
	}

	public ResourceLocation getName() {
		return this.name;
	}

}
