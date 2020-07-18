package dzuchun.wingx.trick;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.registries.IForgeRegistryEntry;

public interface ITrick extends IForgeRegistryEntry<AbstractTrick> {
	/**
	 * Reads trick data from buffer.
	 * 
	 * @param <A> Trick class.
	 * @param buf Buffer to read from.
	 * @return Trick that was readed (You may just "return this").
	 */
	ITrick readFromBuf(PacketBuffer buf); // TODO move to IServerTrick

	/**
	 * Writes trick data to buffer.
	 * 
	 * @param <A> Trick class.
	 * @param buf Buffer write to.
	 * @return Trick that was writen (You may just "return this").
	 */
	ITrick writeToBuf(PacketBuffer buf); // TODO move to IServerTrick
}
