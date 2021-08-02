package dzuchun.wingx.net;

import java.util.function.Supplier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dzuchun.wingx.init.Tricks;
import dzuchun.wingx.trick.IAimingTrick;
import dzuchun.wingx.trick.ICastedTrick;
import dzuchun.wingx.trick.ITargetedTrick;
import dzuchun.wingx.util.NetworkHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor.PacketTarget;

public class TrickAimingMessage {

	private static final Logger LOG = LogManager.getLogger();

	public IAimingTrick trick = null;

	public TrickAimingMessage(IAimingTrick trickIn) {
		this.trick = trickIn;
	}

	public static TrickAimingMessage decode(PacketBuffer buf) {
		// TODO check cast
		return new TrickAimingMessage((IAimingTrick) NetworkHelper.readRegisteredTrick(Tricks.getRegistry(), buf));
	}

	public void encode(PacketBuffer buf) {
		NetworkHelper.writeRegisteredTrick(buf, this.trick);
	}

	public static void handle(TrickAimingMessage msg, Supplier<NetworkEvent.Context> ctx) {
		if (msg != null) {
			ctx.get().enqueueWork(() -> {
				IAimingTrick trick = msg.trick;
				if (trick != null) {
					// TODO SEPARATE MESSAGES
					if (ctx.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
						@SuppressWarnings("resource")
						ClientWorld world = Minecraft.getInstance().world;
						if (trick instanceof ICastedTrick) {
							((ICastedTrick) trick).setWorld(world);
						}
						if (trick instanceof ITargetedTrick) {
							((ITargetedTrick) trick).setTargetWorld(world);
						}
						trick.beginAimClient();
						trick.showMessage();
					} else if (ctx.get().getDirection() == NetworkDirection.PLAY_TO_SERVER) {
						ServerWorld world = ctx.get().getSender().getServerWorld();
						if (trick instanceof ICastedTrick) {
							((ICastedTrick) trick).setWorld(world);
						}
						if (trick instanceof ITargetedTrick) {
							((ITargetedTrick) trick).setTargetWorld(world);
						}
						trick.beginAimServer();
						PacketTarget target = trick.getAimBackTarget();
						if (target != null) {
							WingxPacketHandler.INSTANCE.send(target, msg);
						} else {
							LOG.warn("{} trick returned null back aim target", trick);
						}
					} else {
						LOG.warn("Unknown direction, ignoring message");
					}
				} else {
					LOG.warn("Trick is null, ignoring message");
				}
			});
		}
		ctx.get().setPacketHandled(true);
	}

}
