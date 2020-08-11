package dzuchun.wingx;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dzuchun.wingx.capability.entity.wings.IWingsCapability;
import dzuchun.wingx.capability.entity.wings.WingsProvider;
import dzuchun.wingx.capability.entity.wings.storage.BasicData;
import dzuchun.wingx.capability.entity.wings.storage.Serializers;
import dzuchun.wingx.capability.world.tricks.ActiveTricksProvider;
import dzuchun.wingx.capability.world.tricks.IActiveTricksCapability;
import dzuchun.wingx.client.render.entity.model.util.AnimationState;
import dzuchun.wingx.client.render.entity.model.util.FadeFunction;
import dzuchun.wingx.client.render.overlay.AbstractOverlay;
import dzuchun.wingx.client.render.overlay.AbstractTickingOverlay;
import dzuchun.wingx.command.impl.WingxComand;
import dzuchun.wingx.entity.misc.WingsEntity;
import dzuchun.wingx.net.AnimationStateMessage;
import dzuchun.wingx.net.WingxPacketHandler;
import dzuchun.wingx.trick.AbstractInterruptablePlayerTrick;
import dzuchun.wingx.trick.meditation.MeditationUtil;
import dzuchun.wingx.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
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
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.network.PacketDistributor;

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
			BasicData data = wings.getDataManager().getOrAddDefault(Serializers.BASIC_SERIALIZER);
			if (data.wingsActive) {
				if (event.side == LogicalSide.CLIENT) {
					((ClientWorld) event.player.world).getAllEntities().forEach((Entity entity) -> {
						if (entity instanceof WingsEntity
								&& ((WingsEntity) entity).getUniqueID().equals(data.wingsUniqueId)) {
							((WingsEntity) entity).realSetPosAndUpdate();
						}
					});
				} else {
					((ServerWorld) event.player.world).getEntities().forEach((Entity entity) -> {
						if (entity instanceof WingsEntity
								&& ((WingsEntity) entity).getUniqueID().equals(data.wingsUniqueId)) {
							((WingsEntity) entity).realSetPosAndUpdate();
						}
					});
				}
			}
		});
	}

	private static final List<AnimationState> DEFAULT_WINGS = Arrays.asList(
			new AnimationState(0, FadeFunction.LINEAR, false, 0.0f, 2.0f, 0.0f, 0.0f, 0.0f, 0.0f),
			new AnimationState(0, FadeFunction.LINEAR, false, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f),
			new AnimationState(0, FadeFunction.LINEAR, false, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f));
	private static final List<AnimationState> EXTENDED_WINGS = Arrays.asList(
			new AnimationState(0, FadeFunction.LINEAR, false, 0.0f, 2.0f, 0.0f, 0.0f, 0.0f, 0.0f),
			new AnimationState(0, FadeFunction.LINEAR, false, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f),
			new AnimationState(0, FadeFunction.LINEAR, false, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f));

	@SubscribeEvent
	public static void onWorldTick(final WorldTickEvent event) {
		World world = event.world;
		if (event.phase == Phase.END) {
			// Ticking active tricks
			if (event.side == LogicalSide.SERVER) { // TODO check if needed
				world.getCapability(ActiveTricksProvider.ACTIVE_TRICKS, null)
						.ifPresent((IActiveTricksCapability cap) -> {
							cap.onWorldTick(world);
						});
				// Ticking for wings animation
				long time = world.getGameTime();
				((ServerWorld) world).getEntities().forEach(entity -> {
					IWingsCapability cap = entity.getCapability(WingsProvider.WINGS, null).orElse(null);
					if (cap != null && cap.getDataManager().getOrAddDefault(Serializers.BASIC_SERIALIZER).wingsActive) {
//						LOG.debug("Ticking for active wings {}, time {}", cap, time);
						if (time % 80 == 0) {
							LOG.debug("Sending wings to move default");
							WingxPacketHandler.INSTANCE.send(
									PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity),
									new AnimationStateMessage(
											Util.computeNewList(DEFAULT_WINGS,
													state -> new AnimationState(state, time + 35, false)),
											entity.getUniqueID()));
						}
						if (time % 80 == 40) {
							LOG.debug("Sending wings to move extended");
							WingxPacketHandler.INSTANCE.send(
									PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity),
									new AnimationStateMessage(
											Util.computeNewList(EXTENDED_WINGS,
													state -> new AnimationState(state, time + 35, false)),
											entity.getUniqueID()));
						}
					}
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
//		if (Minecraft.getInstance().objectMouseOver != null) {
//			LOG.debug("Looking at {}", Minecraft.getInstance().objectMouseOver.getType().toString());
//		}
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

	@SubscribeEvent
	public static void onServerStart(final FMLServerStartingEvent event) {
		LOG.debug("Registering wingx command");
		WingxComand.register(event.getServer().getCommandManager().getDispatcher());
		LOG.debug("Registered wingx command");
	}
}
