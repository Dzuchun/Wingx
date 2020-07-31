package dzuchun.wingx.net;

import java.util.function.Supplier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dzuchun.wingx.trick.AbstractTrick;
import dzuchun.wingx.trick.ICastedTrick;
import dzuchun.wingx.trick.ITargetedTrick;
import dzuchun.wingx.trick.ITrick;
import dzuchun.wingx.util.NetworkHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryManager;

public class TrickPerformedMessage {

	private static final Logger LOG = LogManager.getLogger();

	public ITrick trick = null;

	public TrickPerformedMessage(ITrick trick) {
		this.trick = trick;
	}

	public static TrickPerformedMessage decode(PacketBuffer buf) {
		IForgeRegistry<AbstractTrick> registry = RegistryManager.ACTIVE.getRegistry(AbstractTrick.class);
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
					if (ctx.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
						@SuppressWarnings("resource")
						ClientWorld world = Minecraft.getInstance().world;
						if (trick instanceof ICastedTrick) {
							((ICastedTrick) trick).setWorld(world);
						}
						if (trick instanceof ITargetedTrick) {
							((ITargetedTrick) trick).setTargetWorld(world);
						}
						trick.execute(LogicalSide.CLIENT);
					} else if (ctx.get().getDirection() == NetworkDirection.PLAY_TO_SERVER) {
						ServerWorld world = ctx.get().getSender().getServerWorld();
						if (trick instanceof ICastedTrick) {
							((ICastedTrick) trick).setWorld(world);
						}
						if (trick instanceof ITargetedTrick) {
							((ITargetedTrick) trick).setTargetWorld(world);
						}
						trick.execute(LogicalSide.SERVER);
						WingxPacketHandler.INSTANCE.send(trick.getBackPacketTarget(), msg);

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