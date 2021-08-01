package dzuchun.wingx.trick;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.PacketDistributor.PacketTarget;
import net.minecraftforge.registries.IForgeRegistryEntry;

public interface ITrick extends IForgeRegistryEntry<AbstractTrick> {
	/**
	 * Reads trick data from buffer.
	 *
	 * @param buf Buffer to read from.
	 * @return Trick that was readed (You may just "return this").
	 */
	ITrick readFromBuf(PacketBuffer buf);

	/**
	 * Writes trick data to buffer.
	 *
	 * @param buf Buffer write to.
	 * @return Trick that was writen (You may just "return this").
	 */
	ITrick writeToBuf(PacketBuffer buf);

	default void executeCommon() {
	}

	default void executeClient() {
		this.executeCommon();
	}

	default void executeServer() {
		this.executeCommon();
	}

	int getStatus();

	PacketTarget getBackPacketTarget();

	ITrick newEmpty();

	default void showMessage() {
	}
}
