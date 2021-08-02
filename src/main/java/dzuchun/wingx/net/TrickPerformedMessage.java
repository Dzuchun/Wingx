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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor.PacketTarget;
import net.minecraftforge.registries.IForgeRegistry;

public class TrickPerformedMessage {

	private static final Logger LOG = LogManager.getLogger();

	public ITrick trick = null;

	protected TrickPerformedMessage(ITrick trick) {
		this.trick = trick;
	}

	public void encode(PacketBuffer buf) {
		NetworkHelper.writeRegisteredTrick(buf, this.trick);
	}

	public static class Client extends TrickPerformedMessage {
		public Client(ITrick trick) {
			super(trick);
		}

		@OnlyIn(Dist.CLIENT)
		@Override
		public void handle(Supplier<NetworkEvent.Context> ctx) {
			ctx.get().enqueueWork(() -> {
				if (this.trick != null) {
					@SuppressWarnings("resource")
					ClientWorld world = Minecraft.getInstance().world;
					if (this.trick instanceof ICastedTrick) {
						((ICastedTrick) this.trick).setWorld(world);
					}
					if (this.trick instanceof ITargetedTrick) {
						((ITargetedTrick) this.trick).setTargetWorld(world);
					}
					this.trick.executeClient();
					this.trick.reportState();
				} else {
					LOG.warn("Trick is null, ignoring message");
				}
			});
			super.handle(ctx);
		}

		public static TrickPerformedMessage.Client decode(PacketBuffer buf) {
			IForgeRegistry<ITrick.ITrickType<?>> registry = Tricks.getRegistry();
			return new TrickPerformedMessage.Client(NetworkHelper.readRegisteredTrick(registry, buf));
		}

	}

	public static class Server extends TrickPerformedMessage {
		public Server(ITrick trick) {
			super(trick);
		}

		@Override
		public void handle(Supplier<NetworkEvent.Context> ctx) {
			ctx.get().enqueueWork(() -> {
				if (this.trick != null) {
					ServerWorld world = ctx.get().getSender().getServerWorld();
					if (this.trick instanceof ICastedTrick) {
						((ICastedTrick) this.trick).setWorld(world);
					}
					if (this.trick instanceof ITargetedTrick) {
						((ITargetedTrick) this.trick).setTargetWorld(world);
					}
					this.trick.executeServer();
					PacketTarget target = this.trick.getBackPacketTarget();
					if (target != null) {
						WingxPacketHandler.INSTANCE.send(target, new TrickPerformedMessage.Client(this.trick));
					}
				} else {
					LOG.warn("Trick is null, ignoring message");
				}
			});
			super.handle(ctx);
		}

		public static TrickPerformedMessage.Server decode(PacketBuffer buf) {
			IForgeRegistry<ITrick.ITrickType<?>> registry = Tricks.getRegistry();
			return new TrickPerformedMessage.Server(NetworkHelper.readRegisteredTrick(registry, buf));
		}

	}

	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ctx.get().setPacketHandled(true);
	}
}