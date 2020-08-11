package dzuchun.wingx.client.render.entity.model.util;

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

	public static final FadeFunction LINEAR = create(null, new ResourceLocation(Wingx.MOD_ID, "linear"));

	private static final ConcurrentHashMap<ResourceLocation, FadeFunction> registry = new ConcurrentHashMap<ResourceLocation, FadeFunction>(
			0);

	public static FadeFunction create(BiFunction<Float, AnimationParameter, Float> func, ResourceLocation nameIn) {
		FadeFunction res = new FadeFunction();
		res.innerGet = func == null ? (d, p) -> d : func;
		res.name = nameIn;
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
		return MathHelper.clamp(innerGet.apply(MathHelper.clamp(d, 0.0f, 1.0f), p), 0.0f, 1.0f);
	}

	public ResourceLocation getName() {
		return name;
	}
}
