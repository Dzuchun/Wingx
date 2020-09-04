package dzuchun.wingx.util;

import java.util.Iterator;
import java.util.UUID;

import javax.annotation.Nullable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class WorldHelper {
	private static final Logger LOG = LogManager.getLogger();
	private static Entity res;

	@Nullable
	public static synchronized Entity getEntityFromWorldByUniqueId(World worldIn, UUID uniqueId) {
		if (uniqueId == null) {
			return null;
		}
		Iterator<Entity> allEntities = null; // Or all loaded, depends on context
		if (worldIn instanceof ClientWorld) {
			allEntities = ((ClientWorld) worldIn).getAllEntities().iterator();
		} else if (worldIn instanceof ServerWorld) {
			allEntities = ((ServerWorld) worldIn).getEntities().iterator();
		} else {
			LOG.warn("Parsed world that is not ServerWorld and not ClientWorld. Can't get entity, returning null.");
			return null;
		}
		res = null;
		allEntities.forEachRemaining((Entity entity) -> {
			if (entity.getUniqueID().equals(uniqueId)) {
				res = entity;
			}
		});
//		LOG.debug("Returning {}", res == null ? "null" : res);
		return res;
	}

	public static Iterable<Entity> getEntitiesWithin(World world, Vector3d pos, double radius) {
		if (world instanceof ServerWorld) {
			return chooseEntitiesWithin(((ServerWorld) world).getEntities().iterator(), pos, radius);
		} else {
			return chooseEntitiesWithin(((ClientWorld) world).getAllEntities().iterator(), pos, radius);
		}
	}

	public static Iterable<Entity> chooseEntitiesWithin(Iterator<Entity> iterable, Vector3d pos, double radius) {
		double radiusSq = radius * radius;
		return Util.chooseFromIterable(iterable, e -> e.getDistanceSq(pos) <= radiusSq);
	}
}
