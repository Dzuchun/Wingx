package dzuchun.wingx.net;

import java.util.function.Supplier;

import dzuchun.wingx.entity.misc.WingsEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkEvent;

public class ToggleWingsMessage {

	private boolean state;
	public ToggleWingsMessage(boolean state) {
		this.state = state;
	}
	
	public static ToggleWingsMessage decode(PacketBuffer buf) {
		return new ToggleWingsMessage(buf.readBoolean());
	}

	public void encode(PacketBuffer buf) {
		buf.writeBoolean(state);
	}

	public static void handle(ToggleWingsMessage msg, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			// Work that needs to be threadsafe (most work)
			ServerPlayerEntity sender = ctx.get().getSender(); // the client that sent this packet
			ServerWorld world = (ServerWorld)sender.world;
			WingsEntity wings = new WingsEntity(world);
			wings.setOwner(sender.getUniqueID(), true);
			world.summonEntity(wings);
		});
		ctx.get().setPacketHandled(true);
	}
}

class ToggleWingsMessageResponse {
	public static ToggleWingsMessageResponse decode(PacketBuffer buf) {
		return null;
	}

	public void encode(PacketBuffer buf) {

	}

	public static void handle(ToggleWingsMessageResponse msg, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			// Work that needs to be threadsafe (most work)
			ServerPlayerEntity sender = ctx.get().getSender(); // the client that sent this packet
			sender.fallDistance = 0f;
		});
		ctx.get().setPacketHandled(true);
	}
}
