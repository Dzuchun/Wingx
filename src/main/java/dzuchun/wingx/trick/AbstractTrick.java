package dzuchun.wingx.trick;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.registries.ForgeRegistryEntry;

public abstract class AbstractTrick implements ITrick {
	protected int status = 0;

	@Override
	public int getStatus() {
		return this.status;
	}

	public abstract static class TrickType<T extends AbstractTrick> extends ForgeRegistryEntry<ITrickType<T>>
			implements ITrick.ITrickType<T> {

		/**
		 * Reads trick data from buffer.
		 *
		 * @param buf Buffer to read from.
		 * @return Trick that was readed (You may just "return this").
		 */
		@Override
		public T readFromBuf(PacketBuffer buf) {
			return this.readFromBufInternal(this.newEmpty(), buf);
		}

		protected T readFromBufInternal(T trick, PacketBuffer buf) {
			trick.status = buf.readInt();
			return trick;
		}

		/**
		 * Writes trick data to buffer.
		 *
		 * @param buf Buffer write to.
		 * @return Trick that was writen (You may just "return this").
		 */
		@Override
		public T writeToBuf(T trick, PacketBuffer buf) {
			buf.writeInt(trick.getStatus());
			return trick;
		}

		@Override
		public abstract T newEmpty();

		@Override
		public String toString() {
			return this.getClass().getSimpleName();
		}
	}

}
