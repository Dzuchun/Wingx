package dzuchun.wingx;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dzuchun.wingx.capability.entity.wings.IWingsCapability;
import dzuchun.wingx.capability.entity.wings.WingsProvider;
import dzuchun.wingx.capability.world.tricks.ActiveTricksProvider;
import dzuchun.wingx.capability.world.tricks.IActiveTricksCapability;
import dzuchun.wingx.client.render.overlay.AbstractOverlay;
import dzuchun.wingx.client.render.overlay.AbstractTickingOverlay;
import dzuchun.wingx.entity.misc.WingsEntity;
import dzuchun.wingx.trick.AbstractInterruptablePlayerTrick;
import dzuchun.wingx.trick.meditation.MeditationUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent.LoggedOutEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Post;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.event.TickEvent.WorldTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

/**
 * Used to subscribe needed methods to event bus. Will need to change that later
 * to manual subscription.
 *
 * @author Dzuchun
 *
 */
@Mod.EventBusSubscriber(bus = Bus.FORGE, modid = Wingx.MOD_ID)
public class ForgeBusEventListener {
	private static final Logger LOG = LogManager.getLogger();

	public static void init() {
	}

	@SuppressWarnings("resource")
	@SubscribeEvent
	public static void onPlayerTick(final PlayerTickEvent event) {
		if (Minecraft.getInstance().world == null) {
			return;
		}
		event.player.getCapability(WingsProvider.WINGS, null).ifPresent((IWingsCapability wings) -> {
			if (wings.isActive()) {
				if (event.side == LogicalSide.CLIENT) {
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
	public static void onServerWorldTick(final WorldTickEvent event) {
		World world = event.world;
		if (event.phase == Phase.END) {
			if (event.side == LogicalSide.SERVER) {
				world.getCapability(ActiveTricksProvider.ACTIVE_TRICKS, null)
						.ifPresent((IActiveTricksCapability cap) -> {
							cap.onWorldTick(world);
						});
			}
		}
	}

	@SuppressWarnings("resource")
	@SubscribeEvent
	public static void onClientTick(final ClientTickEvent event) {
		if (Minecraft.getInstance().world == null) {
			return;
		}
		AbstractTickingOverlay.onClientTick(event);
	}

	@SubscribeEvent
	public static void onRenderGameOverlay(RenderGameOverlayEvent event) {
		if (event instanceof RenderGameOverlayEvent.Post) {
			try {
				AbstractInterruptablePlayerTrick.onRenderGameOverlay((Post) event);
				AbstractOverlay.onRenderGameOverlay((Post) event);
			} catch (NullPointerException e) {
				LOG.info("Got some null pointer exception, nevermind: {}", e.getMessage());
				e.printStackTrace();
			}
		}
	}

	@SubscribeEvent
	public static void onLogOut(LoggedOutEvent event) {
		AbstractTickingOverlay.onDisconnect(event);
		AbstractOverlay.onDisconnect(event);
	}

	@SuppressWarnings("resource")
	@SubscribeEvent
	public static void onRenderGameOverlayText(RenderGameOverlayEvent.Text event) {
		if (Minecraft.getInstance().gameSettings.showDebugInfo) { // Otherwise string is displayed all the time
			event.getLeft().add("");
			event.getLeft().add(String.format("Meditation points available: %s",
					MeditationUtil.getMeditationScore(Minecraft.getInstance().player)));
		}
	}
}
