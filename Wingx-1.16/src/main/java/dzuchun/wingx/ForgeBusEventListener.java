package dzuchun.wingx;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dzuchun.wingx.capability.entity.wings.IWingsCapability;
import dzuchun.wingx.capability.entity.wings.WingsProvider;
import dzuchun.wingx.capability.world.tricks.ActiveTricksProvider;
import dzuchun.wingx.capability.world.tricks.IActiveTricksCapability;
import dzuchun.wingx.entity.misc.WingsEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.event.TickEvent.WorldTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@Mod.EventBusSubscriber(bus = Bus.FORGE, modid = Wingx.MOD_ID)
public class ForgeBusEventListener {
	@SuppressWarnings("unused")
	private static final Logger LOG = LogManager.getLogger();

	public static void init() {
	}

	@SubscribeEvent
	public static void onPlayerTick(final PlayerTickEvent event) {
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

	@SubscribeEvent
	public static void onWorldTick(final WorldTickEvent event) {
		World world = event.world;
		if (event.phase == Phase.END) {
			if (world instanceof ServerWorld) {
				world.getCapability(ActiveTricksProvider.ACTIVE_TRICKS, null)
						.ifPresent((IActiveTricksCapability cap) -> {
							cap.onWorldTick(world);
						});
			}
		}
	}
}
