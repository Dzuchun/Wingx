package dzuchun.wingx.trick.meditation;

import java.util.ConcurrentModificationException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ibm.icu.impl.Pair;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceContext.BlockMode;
import net.minecraft.util.math.RayTraceContext.FluidMode;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class MeditationUtil {
	public static final long MEDITATION_MINIMUM_CALCULATION_INTERVAL = 200l;
	public static final double MEDITATION_MAX_DISTANCE = 20.0f;
	public static final double MEDITATION_MAX_DISTANCE_SR = MEDITATION_MAX_DISTANCE * MEDITATION_MAX_DISTANCE;
	// TODO parametrize
	public static final Function<Double, Double> MEDITATION_DISTANCE_WEIGHT = d -> d;
	// TODO parametrize
	public static final Function<BlockState, Double> MEDITATION_BLOCK_WEIGHT = blockstate -> 1.0d;
	// TODO parametrize
	public static final Function<FluidState, Double> MEDITATION_FLUID_WEIGHT = fluidstate -> 1.0d;

	public static final float MEDITATION_MAX_YAW_DEGREES = 15f;
	public static final float MEDITATION_MAX_YAW = (float) ((MEDITATION_MAX_YAW_DEGREES / 180.0d) * Math.PI);
	public static final int MEDITATION_YAW_ITERATIONS = 3;
	public static final float MEDITATION_YAW_STEP = MEDITATION_MAX_YAW / MEDITATION_YAW_ITERATIONS;
	public static final float MEDITATION_YAW_STEP_DEGREES = MEDITATION_MAX_YAW_DEGREES / (MEDITATION_YAW_ITERATIONS);

	public static final float MEDITATION_MAX_PITCH_DEGREES = 15f;
	public static final float MEDITATION_MAX_PITCH = (float) ((MEDITATION_MAX_YAW_DEGREES / 180.0d) * Math.PI);
	public static final int MEDITATION_PITCH_ITERATIONS = 3;
	public static final float MEDITATION_PITCH_STEP = MEDITATION_MAX_YAW / MEDITATION_PITCH_ITERATIONS;
	public static final float MEDITATION_PITCH_STEP_DEGREES = MEDITATION_MAX_PITCH_DEGREES
			/ (MEDITATION_PITCH_ITERATIONS);

	private static final Map<UUID, Pair<Long, Double>> meditationLastCalculations = new LinkedHashMap<UUID, Pair<Long, Double>>(
			0);
	@SuppressWarnings("unused")
	private static final Logger LOG = LogManager.getLogger();

	public static double getMeditationPoints(Entity entity) {
		UUID uuid = entity.getUniqueID();
		World world = entity.world;
		Long currentTime = world.getGameTime();
		if (!meditationLastCalculations.containsKey(uuid)) {
			meditationLastCalculations.put(uuid, Pair.of(0L, 0.0d));
		}
		Pair<Long, Double> data = meditationLastCalculations.get(uuid);
		if (data == null) {
			throw (new ConcurrentModificationException("I've just put this value in map, and now it's gone!!"));
		}
		double res = data.second;
		if ((currentTime - data.first) >= MEDITATION_MINIMUM_CALCULATION_INTERVAL) {
			res = getMeditationPointsInner(entity);
			meditationLastCalculations.replace(uuid, Pair.of(currentTime, res));
		}
		return res;
	}

	private static double getMeditationPointsInner(Entity entity) {
		double res = 0d;
		for (float yaw = 0f; yaw <= MEDITATION_MAX_YAW_DEGREES; yaw += MEDITATION_YAW_STEP_DEGREES) {
			for (float pitch = 0f; pitch <= MEDITATION_MAX_PITCH_DEGREES; pitch += MEDITATION_PITCH_STEP_DEGREES) {
				res += getMeditationPointsInner(entity, yaw, pitch);
				if (yaw != 0) {
					res += getMeditationPointsInner(entity, -yaw, pitch);
				}
				if (pitch != 0) {
					res += getMeditationPointsInner(entity, yaw, -pitch);
					if (yaw != 0) {
						res += getMeditationPointsInner(entity, -yaw, -pitch);
					}
				}
			}
		}
		return (res / (((double) MEDITATION_YAW_ITERATIONS * 2) + 1)
				/ (((double) MEDITATION_PITCH_ITERATIONS * 2) + 1));
	}

	private static double getMeditationPointsInner(Entity entity, float yawDelta, float pitchDelta) {
		float yaw = entity.rotationYaw + yawDelta;
		float pitch = entity.rotationPitch + pitchDelta;
		Vector3d begin = entity.getEyePosition(0);
		Vector3d end = begin.add(Vector3d.fromPitchYaw(pitch, yaw).scale(MEDITATION_MAX_DISTANCE));
		World world = entity.world;
		RayTraceResult result = world
				.rayTraceBlocks(new RayTraceContext(begin, end, BlockMode.OUTLINE, FluidMode.NONE, entity));
		// TODO implement fluids
		double res = 0d;
		if (result.getType() == Type.BLOCK) {
			Vector3d hitVec = result.getHitVec();
			BlockState blockState = world.getBlockState(new BlockPos(hitVec));
			res = MEDITATION_BLOCK_WEIGHT.apply(blockState)
					* MEDITATION_DISTANCE_WEIGHT.apply(hitVec.add(begin.scale(-1)).length());
		}
		return res;
	}
}
