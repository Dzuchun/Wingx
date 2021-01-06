package dzuchun.wingx.net;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dzuchun.wingx.capability.entity.wings.IWingsCapability;
import dzuchun.wingx.capability.entity.wings.WingsProvider;
import dzuchun.wingx.capability.entity.wings.storage.Serializers;
import dzuchun.wingx.entity.misc.WingsEntity;
import dzuchun.wingx.util.NetworkHelper;
import dzuchun.wingx.util.Util;
import dzuchun.wingx.util.WorldHelper;
import dzuchun.wingx.util.animation.AnimationState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class AnimationStateMessage {

	private static final Logger LOG = LogManager.getLogger();

	public List<AnimationState> states;
	public UUID ownerUniqueId;

	public AnimationStateMessage(List<AnimationState> statesIn, UUID ownerUniqueIdIn) {
		this.states = statesIn;
		this.ownerUniqueId = ownerUniqueIdIn;
	}

	public static AnimationStateMessage decode(PacketBuffer buf) {
		return new AnimationStateMessage(NetworkHelper.readArray(buf, NetworkHelper::readAnimationState),
				buf.readUniqueId());
	}

	public void encode(PacketBuffer buf) {
		NetworkHelper.writeArray(buf, this.states, NetworkHelper::writeAnimationState);
		buf.writeUniqueId(this.ownerUniqueId);
	}

	@Override
	public String toString() {
		return String.format("AnimationStateMessage[states: %s, ownweUniqueId: %s]", Util.iterableToString(this.states),
				this.ownerUniqueId);
	}

	public static void handle(AnimationStateMessage msg, Supplier<NetworkEvent.Context> ctx) {
		NetworkEvent.Context context = ctx.get();
		context.enqueueWork(() -> {
			LOG.debug("Handling {}", msg.toString());
			Minecraft minecraft = Minecraft.getInstance();
			Entity entity = WorldHelper.getEntityFromWorldByUniqueId(minecraft.world, msg.ownerUniqueId);
			IWingsCapability cap = entity.getCapability(WingsProvider.WINGS, null).orElse(null);
			if (cap != null) {
				Entity wings = WorldHelper.getEntityFromWorldByUniqueId(minecraft.world,
						cap.getDataManager().getOrAddDefault(Serializers.BASIC_SERIALIZER).wingsUniqueId);
				if ((wings != null) && (wings instanceof WingsEntity)) {
					WingsEntity realWingsEntity = (WingsEntity) wings;
					synchronized (realWingsEntity.upcomingStates_lock) {
						realWingsEntity.setUpcomingStates(msg.states);
					}
				}
			}
		});
	}
}
