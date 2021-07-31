package dzuchun.wingx;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ibm.icu.impl.Pair;

import dzuchun.wingx.capability.entity.wings.IWingsCapability;
import dzuchun.wingx.capability.entity.wings.WingsCapability;
import dzuchun.wingx.capability.entity.wings.WingsProvider;
import dzuchun.wingx.capability.entity.wings.storage.AgilData;
import dzuchun.wingx.capability.entity.wings.storage.BasicData;
import dzuchun.wingx.capability.entity.wings.storage.HastyData;
import dzuchun.wingx.capability.entity.wings.storage.Serializers;
import dzuchun.wingx.capability.world.tricks.ActiveTricksProvider;
import dzuchun.wingx.capability.world.tricks.IActiveTricksCapability;
import dzuchun.wingx.client.render.overlay.AbstractOverlay;
import dzuchun.wingx.client.render.overlay.AbstractTickingOverlay;
import dzuchun.wingx.command.impl.WingxComand;
import dzuchun.wingx.entity.misc.WingsEntity;
import dzuchun.wingx.init.Items;
import dzuchun.wingx.net.TrickPerformedMessage;
import dzuchun.wingx.net.WingxPacketHandler;
import dzuchun.wingx.trick.AbstractInterruptablePlayerTrick;
import dzuchun.wingx.trick.AgilPlayerTrick;
import dzuchun.wingx.trick.HastyPlayerTrick;
import dzuchun.wingx.trick.meditation.MeditationUtil;
import dzuchun.wingx.util.animation.AnimationHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.projectile.ProjectileHelper;
import net.minecraft.server.management.PlayerInteractionManager;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent.LoggedInEvent;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent.LoggedOutEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Post;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.event.TickEvent.WorldTickEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
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

	private static Random tmp_random = new Random();

	@SuppressWarnings("resource")
	@SubscribeEvent
	public static void onPlayerTick(final PlayerTickEvent event) {
		if (Minecraft.getInstance().world == null) {
			return;
		}
		event.player.getCapability(WingsProvider.WINGS, null).ifPresent((IWingsCapability wings) -> {
			PlayerEntity player = event.player;
			World world = player.world;
			BasicData basicData = wings.getDataManager().getOrAddDefault(Serializers.BASIC_SERIALIZER);
			if (basicData.wingsActive) {
				if (event.side == LogicalSide.CLIENT) {
					((ClientWorld) world).getAllEntities().forEach((Entity entity) -> {
						if ((entity instanceof WingsEntity)
								&& ((WingsEntity) entity).getUniqueID().equals(basicData.wingsUniqueId)) {
							((WingsEntity) entity).realSetPosAndUpdate();
						}
					});
				} else {
					((ServerWorld) world).getEntities().forEach((Entity entity) -> {
						if ((entity instanceof WingsEntity)
								&& ((WingsEntity) entity).getUniqueID().equals(basicData.wingsUniqueId)) {
							((WingsEntity) entity).realSetPosAndUpdate();
						}
					});
				}
			}
			if (event.side == LogicalSide.SERVER) {
				// TODO move to hasty
				ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
				HastyData hastyData = wings.getDataManager().getOrAddDefault(Serializers.HASTY_SERIALIZER);
				long currentTime = serverPlayer.world.getGameTime();
				PlayerInteractionManager interaction = serverPlayer.interactionManager;
				if (hastyData.isActive && interaction.isDestroyingBlock
						&& (world.getBlockState(interaction.destroyPos).getBlockHardness(world,
								interaction.destroyPos) > 0)
						&& ((currentTime - hastyData.lastProc) > hastyData.cooldown)
						&& (tmp_random.nextDouble() < hastyData.probability)) {
					hastyData.lastProc = currentTime;
					WingxPacketHandler.INSTANCE.send(
							PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> serverPlayer),
							new TrickPerformedMessage(
									new HastyPlayerTrick(serverPlayer, hastyData, interaction.destroyPos)));
					// TODO add stat (hasty)
				}
				// TODO move to agil
				// TODO fix entity bugs
				if (player.ticksSinceLastSwing == 0) {
					AgilData agilData = wings.getDataManager().getOrAddDefault(Serializers.AGIL_SERIALIZER);
					if (agilData.isActive && ((world.getGameTime() - agilData.lastProc) >= agilData.cooldown)) {
						// TODO specify reach distance
						EntityRayTraceResult entityRayTrace = ProjectileHelper.rayTraceEntities(world, player,
								player.getEyePosition(1.0f),
								player.getPositionVec().add(serverPlayer.getForward().scale(5.0d)),
								player.getBoundingBox().grow(5.0d), entity -> entity instanceof LivingEntity);
						if ((entityRayTrace != null)
								&& serverPlayer.equals(
										((LivingEntity) entityRayTrace.getEntity()).getLastDamageSource() == null ? null
												: ((LivingEntity) entityRayTrace.getEntity()).getLastDamageSource()
														.getTrueSource())
								&& (tmp_random.nextDouble() <= agilData.probability)) {
							agilData.lastProc = player.world.getGameTime();
							player.ticksSinceLastSwing = 1000;
							Entity target = entityRayTrace.getEntity();
							target.hurtResistantTime = 0;
							WingxPacketHandler.INSTANCE.send(
									PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player),
									new TrickPerformedMessage(new AgilPlayerTrick(player, target, agilData)));
							// TODO add stat (agil)
						}
					}
				}
			}
		});
	}

	private static List<Pair<AttackEntityEvent, WingsCapability>> attackEventsToProcess = new ArrayList<Pair<AttackEntityEvent, WingsCapability>>();

	@SubscribeEvent
	public static void onWorldTick(final WorldTickEvent event) {
		World world = event.world;
		if (event.phase == Phase.END) {
			// Ticking active tricks
			if (event.side == LogicalSide.SERVER) {
				world.getCapability(ActiveTricksProvider.ACTIVE_TRICKS, null)
						.ifPresent((IActiveTricksCapability cap) -> {
							cap.onWorldTick(world);
						});
				AnimationHandler.onServerTick(event);
				for (Pair<AttackEntityEvent, WingsCapability> attackEvent : attackEventsToProcess) {
					processAttackEntityEvent(attackEvent.first, attackEvent.second);
				}
				attackEventsToProcess.clear();
			}
			if (!world.isRemote) {
				((ServerWorld) world).getEntities().filter(e -> (e.getType() == EntityType.ITEM))
						.filter(e -> (((ItemEntity) e).getItem().getItem() == Items.SOULSWORD.get()))
						.forEach(Entity::remove); // TODO make better
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

	// Client-only!
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

	// Client-only!

	@SubscribeEvent
	public static void onLogInEvent(final LoggedInEvent event) {
		LOG.warn("Logged in event");
//		AbillityNodes.loadAbillityNodes();
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

	@SubscribeEvent
	public static void onRenderWorldLast(final RenderWorldLastEvent event) {
		AbstractOverlay.onRenderWorldLast(event);
	}

	@SubscribeEvent
	public static void onAttackEntityEvent(final AttackEntityEvent event) {

	}

	private static void processAttackEntityEvent(AttackEntityEvent event, WingsCapability cap) {
		LOG.info("Processing event");
	}

}
