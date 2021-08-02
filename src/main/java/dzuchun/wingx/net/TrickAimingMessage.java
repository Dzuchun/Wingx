package dzuchun.wingx.net;

import java.util.function.Supplier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dzuchun.wingx.init.Tricks;
import dzuchun.wingx.trick.IAimingTrick;
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

public class TrickAimingMessage {

	private static final Logger LOG = LogManager.getLogger();

	public IAimingTrick trick = null;

	protected TrickAimingMessage(IAimingTrick trickIn) {
		this.trick = trickIn;
	}

	public void encode(PacketBuffer buf) {
		NetworkHelper.writeRegisteredTrick(buf, this.trick);
	}

	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ctx.get().setPacketHandled(true);
	}

	public static class Client extends TrickAimingMessage {

		public Client(IAimingTrick trickIn) {
			super(trickIn);
		}

		public static TrickAimingMessage.Client decode(PacketBuffer buf) {
			ITrick trick = NetworkHelper.readRegisteredTrick(Tricks.getRegistry(), buf);
			if (!(trick instanceof IAimingTrick)) {
				LOG.warn("Trick readed is not instance of IAimingTrick, resulting message will be empty.");
				return new TrickAimingMessage.Client(null);
			}
			return new TrickAimingMessage.Client((IAimingTrick) trick);
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
					this.trick.beginAimClient();
					this.trick.showMessage();
				} else {
					LOG.warn("Trick is null, ignoring message");
				}
			});
			super.handle(ctx);
		}

	}

	public static class Server extends TrickAimingMessage {

		public Server(IAimingTrick trickIn) {
			super(trickIn);
		}

		public static TrickAimingMessage.Server decode(PacketBuffer buf) {
			ITrick trick = NetworkHelper.readRegisteredTrick(Tricks.getRegistry(), buf);
			if (!(trick instanceof IAimingTrick)) {
				LOG.warn("Trick readed is not instance of IAimingTrick, resulting message will be empty.");
				return new TrickAimingMessage.Server(null);
			}
			return new TrickAimingMessage.Server((IAimingTrick) trick);
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
					this.trick.beginAimServer();
					PacketTarget target = this.trick.getAimBackTarget();
					if (target != null) {
						WingxPacketHandler.INSTANCE.send(target, new TrickAimingMessage.Client(this.trick));
					} else {
						LOG.warn("{} trick returned null back aim target", this.trick);
					}
				} else {
					LOG.warn("Trick is null, ignoring message");
				}
			});
			super.handle(ctx);
		}

	}

}
