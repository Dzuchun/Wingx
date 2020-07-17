package dzuchun.wingx.net;

import java.util.function.Supplier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dzuchun.wingx.trick.AbstractTrick;
import dzuchun.wingx.trick.IExecutableTrick;
import dzuchun.wingx.trick.IServerTrick;
import dzuchun.wingx.trick.ITrick;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryManager;

public class TrickPerformedMessage {

	private static final Logger LOG = LogManager.getLogger(); 
	
	private ITrick trick = null;

	public TrickPerformedMessage(ITrick trick) {
		this.trick = trick;
	}

	public static TrickPerformedMessage decode(PacketBuffer buf) {
		if (buf.readBoolean()) {
			IForgeRegistry<AbstractTrick> registry = RegistryManager.ACTIVE.getRegistry(AbstractTrick.class); //TODO specify!!
			if (registry != null) {
				String registryName = buf.readString(buf.readInt());
				ITrick trick = registry.getValue(new ResourceLocation(registryName));
				if (trick != null) {
					LOG.debug("While decoding found trick from {} class", trick.getClass().getName());
					trick.readFromBuf(buf);
					return new TrickPerformedMessage(trick);
				} else {
					LOG.warn("While decoding found no registered trick with registry name {}, setting trick to null", registryName);
					return new TrickPerformedMessage(null);
				}
				
			} else {
				LOG.warn("No registry found for {}, setting trick to null", AbstractTrick.class.getName());
				return new TrickPerformedMessage(null);
			}
		} else {
			LOG.warn("Recieved empty message, setting trick to null");
			return new TrickPerformedMessage(null);
		}
	}

	public void encode(PacketBuffer buf) {
		if (trick != null) {
			if (trick.getRegistryName() != null) {
				buf.writeBoolean(true);
				String trickRegistryName = trick.getRegistryName().toString();
				buf.writeInt(trickRegistryName.length());
				buf.writeString(trickRegistryName);
				trick.writeToBuf(buf);
			} else {
				LOG.warn("No registry name found for {} class. Message will be empty", trick.getClass().getName());
				buf.writeBoolean(false);
			}
		} else {
			LOG.warn("Trick is null, so message will be empty");
			buf.writeBoolean(false);
		}
	}

	public static void handle(TrickPerformedMessage msg, Supplier<NetworkEvent.Context> ctx) {
		if (msg != null) {
			ctx.get().enqueueWork(() -> {
				ITrick trick = msg.trick;
				if (trick != null) {
					if (ctx.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
						//We are on client
						@SuppressWarnings("resource")
						ClientWorld world = Minecraft.getInstance().world;
						if (trick instanceof IExecutableTrick) {
							((IExecutableTrick)trick).execute(LogicalSide.CLIENT, world);
						}	
					} else if (ctx.get().getDirection() == NetworkDirection.PLAY_TO_SERVER) {
						//We are on server
						if (trick instanceof IServerTrick) {
							ServerWorld world = ctx.get().getSender().getServerWorld();
							if (trick instanceof IExecutableTrick) {
								((IExecutableTrick)trick).execute(LogicalSide.SERVER, world);
							} else {
								LOG.warn("Unknown direction, ignoring message");
							}
						} else {
							LOG.debug("Caught not server-related trick of server, ignoring");
						}
					}
				} else {
					LOG.warn("Trick is null, ignoring message");
				}
			});
		}
		ctx.get().setPacketHandled(true);
	}
}