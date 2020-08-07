package dzuchun.wingx.capability.entity.wings;

import dzuchun.wingx.capability.entity.wings.storage.WingsDataManager;
import net.minecraft.network.PacketBuffer;

/**
 * Capability that stores wings data.
 *
 * @author Dzuchun
 */
public interface IWingsCapability {

	/**
	 * Reads data from PacketBuffer.
	 *
	 * @param buf
	 */
	void readFromBuffer(PacketBuffer buf);

	/**
	 * Writed data to packet buffer.
	 *
	 * @param buf
	 */
	void writeToBuffer(PacketBuffer buf);

	WingsDataManager getDataManager();
}