package dzuchun.wingx.trick;

import dzuchun.wingx.trick.state.TrickState;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.PacketDistributor.PacketTarget;
import net.minecraftforge.registries.IForgeRegistryEntry;

public interface ITrick {

	default void executeCommon() {
	}

	default void executeClient() {
		this.executeCommon();
	}

	default void executeServer() {
		this.executeCommon();
	}

	TrickState getState();

	PacketTarget getBackPacketTarget();

	default void reportState() {
	}

	public abstract <U extends ITrick, T extends ITrickType<U>> T getType();

	public interface ITrickType<T extends ITrick> extends IForgeRegistryEntry<ITrickType<T>> {
		/**
		 * Reads trick data from buffer.
		 *
		 * @param buf Buffer to read from.
		 * @return Trick that was readed.
		 */
		T readFromBuf(PacketBuffer buf);

		/**
		 * Writes trick data to buffer.
		 *
		 * @param buf Buffer write to.
		 * @return Trick that was writen (You may just "return this").
		 */
		T writeToBuf(T trick, PacketBuffer buf);

		T newEmpty();
	}
}
