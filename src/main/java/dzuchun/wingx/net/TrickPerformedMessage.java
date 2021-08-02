package dzuchun.wingx.net;

import java.util.function.Supplier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dzuchun.wingx.init.Tricks;
import dzuchun.wingx.trick.ICastedTrick;
import dzuchun.wingx.trick.ITargetedTrick;
import dzuchun.wingx.trick.ITrick;
import dzuchun.wingx.util.NetworkHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor.PacketTarget;
import net.minecraftforge.registries.IForgeRegistry;

public class TrickPerformedMessage {

	private static final Logger LOG = LogManager.getLogger();

	public ITrick trick = null;

	public TrickPerformedMessage(ITrick trick) {
		this.trick = trick;
	}

	public static TrickPerformedMessage decode(PacketBuffer buf) {
		IForgeRegistry<ITrick.ITrickType<?>> registry = Tricks.getRegistry();
		return new TrickPerformedMessage(NetworkHelper.readRegisteredTrick(registry, buf));
	}

	public void encode(PacketBuffer buf) {
		NetworkHelper.writeRegisteredTrick(buf, this.trick);
	}

	public static void handle(TrickPerformedMessage msg, Supplier<NetworkEvent.Context> ctx) {
		if (msg != null) {
			ctx.get().enqueueWork(() -> {
				ITrick trick = msg.trick;
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
						trick.executeClient();
						trick.showMessage();
					} else if (ctx.get().getDirection() == NetworkDirection.PLAY_TO_SERVER) {
						ServerWorld world = ctx.get().getSender().getServerWorld();
						if (trick instanceof ICastedTrick) {
							((ICastedTrick) trick).setWorld(world);
						}
						if (trick instanceof ITargetedTrick) {
							((ITargetedTrick) trick).setTargetWorld(world);
						}
						trick.executeServer();
						PacketTarget target = trick.getBackPacketTarget();
						if (target != null) {
							WingxPacketHandler.INSTANCE.send(target, msg);
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