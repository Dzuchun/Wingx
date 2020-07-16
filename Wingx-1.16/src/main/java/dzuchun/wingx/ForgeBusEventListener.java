package dzuchun.wingx;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dzuchun.wingx.capability.wings.IWingsCapability;
import dzuchun.wingx.capability.wings.WingsProvider;
import dzuchun.wingx.entity.misc.WingsEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@Mod.EventBusSubscriber(bus = Bus.FORGE, modid = Wingx.MOD_ID)
public class ForgeBusEventListener {
	private static final Logger LOG = LogManager.getLogger();

	public static void init() {
	}

	@SubscribeEvent
	public static void onPlayerTick(PlayerTickEvent event) {
		// LOG.debug("Player tick event end");
		event.player.getCapability(WingsProvider.WINGS, null).ifPresent((IWingsCapability wings) -> {
			if (wings.isActive()) {
				if (event.side == LogicalSide.CLIENT) {
//					LOG.info("Wings active on client");
					((ClientWorld) event.player.world).getAllEntities().forEach((Entity entity) -> {
						if (entity instanceof WingsEntity
								&& ((WingsEntity) entity).getUniqueID().equals(wings.getWingsUniqueId())) {
							((WingsEntity) entity).realSetPosAndUpdate();
						}
					});
				} else {
					((ServerWorld) event.player.world).getEntities().forEach((Entity entity) -> {
						if (entity instanceof WingsEntity
								&& ((WingsEntity) entity).getUniqueID().equals(wings.getWingsUniqueId())) {
							((WingsEntity) entity).realSetPosAndUpdate();
						}
					});
				}
			}
		});
	}
}
