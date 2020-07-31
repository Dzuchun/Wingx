package dzuchun.wingx.capability.entity.wings;

import java.util.UUID;

import net.minecraft.network.PacketBuffer;

/**
 * Capability that stores wings data.
 *
 * @author Dzuchun
 */
public interface IWingsCapability {

	/**
	 * Sets wings active
	 *
	 * @param active Status to set.
	 * @return If status set.
	 */
	boolean setActive(boolean active);

	/**
	 * @return If wings are active.
	 */
	boolean isActive();

	/**
	 * @return WingsEntity unique id.
	 */
	UUID getWingsUniqueId();

	/**
	 * @param wingsUniqueId WingsEntity unique id.
	 */
	void setWingsUniqueId(UUID wingsUniqueId);

	/**
	 * @return Minimum meditation score to perform meditation.
	 */
	double getMeditationScore();

	/**
	 * @param score Minimum meditation score to perform meditation.
	 */
	void setMeditationScore(double score);

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
}