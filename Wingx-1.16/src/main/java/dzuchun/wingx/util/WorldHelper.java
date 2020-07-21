package dzuchun.wingx.util;

import java.util.UUID;

import javax.annotation.Nullable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.entity.Entity;
import net.minecraft.world.server.ServerWorld;

public class WorldHelper {
	private static final Logger LOG = LogManager.getLogger();
	private static Entity res;

	@Nullable
	public static synchronized Entity getEntityFromWorldByUniqueId(ServerWorld worldIn, UUID uniqueId) {
		if (uniqueId == null) {
			return null;
		}
		res = null;
		worldIn.getEntities().forEach((Entity entity) -> {
			if (entity.getUniqueID().equals(uniqueId)) {
				res = entity;
			}
		});
		LOG.debug("Returning {}", res == null ? "null" : res);
		return res;
	}
}
