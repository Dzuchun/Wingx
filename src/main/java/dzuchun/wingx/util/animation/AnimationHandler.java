package dzuchun.wingx.util.animation;

import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dzuchun.wingx.capability.entity.wings.IWingsCapability;
import dzuchun.wingx.capability.entity.wings.WingsProvider;
import dzuchun.wingx.capability.entity.wings.storage.Serializers;
import dzuchun.wingx.net.AnimationStateMessage;
import dzuchun.wingx.net.WingxPacketHandler;
import dzuchun.wingx.util.Util;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.TickEvent.WorldTickEvent;
import net.minecraftforge.fml.network.PacketDistributor;

public class AnimationHandler {
	private static final Logger LOG = LogManager.getLogger();

	private static final List<AnimationState> DEFAULT_WINGS = Arrays.asList(
			new AnimationState(0, FadeFunction.LINEAR, 0, 0.0f, 28.0f, 0.0f, 0.0f, 0.0f, 0.0f),
			new AnimationState(0, FadeFunction.LINEAR, 0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f),
			new AnimationState(0, FadeFunction.LINEAR, 0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f));
	private static final List<AnimationState> EXTENDED_WINGS = Arrays.asList(
			new AnimationState(0, FadeFunction.LINEAR, 0, 0.0f, 27.0f, 0.0f, 0.0f, 0.0f, 0.0f),
			new AnimationState(0, FadeFunction.LINEAR, 0, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, -1.5f),
			new AnimationState(0, FadeFunction.LINEAR, 0, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 1.5f));

	public static void onServerTick(final WorldTickEvent event) {
		World world = event.world;
		long time = world.getGameTime();
		((ServerWorld) world).getEntities().forEach(entity -> {
			IWingsCapability cap = entity.getCapability(WingsProvider.WINGS, null).orElse(null);
			if (cap != null && cap.getDataManager().getOrAddDefault(Serializers.BASIC_SERIALIZER).wingsActive) {
//				LOG.debug("Ticking for active wings {}, time {}", cap, time);
				if (time % 80 == 0) {
					LOG.debug("Sending wings move cycle");
					WingxPacketHandler.INSTANCE
							.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity),
									new AnimationStateMessage(
											Util.computeNewArrayList(DEFAULT_WINGS,
													state -> new AnimationState(state, time + 35, 0)),
											entity.getUniqueID()));
					WingxPacketHandler.INSTANCE
							.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity),
									new AnimationStateMessage(
											Util.computeNewArrayList(EXTENDED_WINGS,
													state -> new AnimationState(state, time + 75, 0)),
											entity.getUniqueID()));
				}
			}
		});
	}
}
