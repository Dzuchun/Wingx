package dzuchun.wingx.trick;

import dzuchun.wingx.trick.state.TrickState;
import dzuchun.wingx.trick.state.TrickStates;
import dzuchun.wingx.util.NetworkHelper;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.registries.ForgeRegistryEntry;

public abstract class AbstractTrick implements ITrick {
	protected TrickState state = TrickStates.OK;

	/**
	 * Returns {@link TrickState} objectm representing current execution state
	 */
	@Override
	public TrickState getState() {
		return this.state;
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
			// TODO remove?
			trick.state = NetworkHelper.readChecked(buf, TrickState::readState);
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
			// TODO remove?
			NetworkHelper.writeChecked(buf, trick.state, (b, st) -> st.writeState(b));
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
