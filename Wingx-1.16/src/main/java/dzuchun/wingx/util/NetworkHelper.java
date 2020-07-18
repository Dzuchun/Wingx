package dzuchun.wingx.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dzuchun.wingx.trick.AbstractTrick;
import dzuchun.wingx.trick.ITrick;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;

public class NetworkHelper {
	private static final Logger LOG = LogManager.getLogger();

	public static <T extends ITrick> ITrick readRegisteredTrick(IForgeRegistry<AbstractTrick> registry,
			PacketBuffer buf) {
		if (buf.readBoolean()) {
			if (registry != null) {
				String registryName = buf.readString(buf.readInt());
				ITrick trick = registry.getValue(new ResourceLocation(registryName));
				if (trick != null) {
					LOG.debug("While decoding found trick from {} class", trick.getClass().getName());
					trick.readFromBuf(buf);
					return trick;
				} else {
					LOG.warn("While decoding found no registered trick with registry name {}, setting trick to null",
							registryName);
					return null;
				}

			} else {
				LOG.warn("No registry found for {}, setting trick to null", AbstractTrick.class.getName());
				return null;
			}
		} else {
			LOG.warn("Recieved empty message, setting trick to null");
			return null;
		}
	}

	public static void writeRegisteredTrick(PacketBuffer buf, ITrick trick) {
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
}
