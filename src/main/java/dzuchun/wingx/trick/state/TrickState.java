package dzuchun.wingx.trick.state;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.Nonnull;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dzuchun.wingx.Wingx;
import dzuchun.wingx.trick.ITrick;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.Color;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TranslationTextComponent;

public class TrickState {
	private static final Logger LOG = LogManager.getLogger();
	private static final Map<ResourceLocation, TrickState> REGISTRY = new LinkedHashMap<ResourceLocation, TrickState>(
			0);

	/**
	 * Registers passed state in the local register
	 *
	 * @param state
	 * @return
	 */
	public static TrickState registerState(TrickState state) {
		REGISTRY.put(state.name, state);
		return state;
	}

	public static Collection<TrickState> getStates() {
		final ArrayList<TrickState> res = new ArrayList<TrickState>(0);
		REGISTRY.forEach((name, state) -> res.add(state));
		return res;
	}

	public static final TrickState UNKNOWN_STATE = new TrickState("unknown", true, false);

	public static final Style ERROR_STYLE = Style.EMPTY.setColor(Color.fromInt(0xFFFF0000));
	public static final Style SUCCESS_STYLE = Style.EMPTY.setColor(Color.fromInt(0xFF22BB00));
	public static final Style NORMAL_STYLE = Style.EMPTY.setColor(Color.fromInt(0xFF888888));

	protected final ResourceLocation name;
	protected final boolean isSuccess;
	protected final boolean isError;

	public TrickState(String nameIn, boolean isError, boolean isSuccess) {
		this(new ResourceLocation(Wingx.MOD_ID, nameIn), isError, isSuccess);
	}

	public TrickState(ResourceLocation nameIn, boolean isErrorIn, boolean isSuccessIn) {
		this.name = nameIn;
		this.isError = isErrorIn;
		this.isSuccess = isSuccessIn;
	}

	public boolean isSuccess() {
		return this.isSuccess;
	}

	public boolean isError() {
		return this.isError;
	}

	private static TrickState getForName(ResourceLocation nameIn) {
		TrickState state = REGISTRY.get(nameIn);
		if (state == null) {
			LOG.warn(
					"Could not find written trick state {} in registry. Some server-client pair must have differrent Wingx mod or it's addons versions.",
					nameIn);
		}
		return state == null ? UNKNOWN_STATE : state;
	}

	public void writeState(PacketBuffer buf) {
		buf.writeResourceLocation(this.name);
	}

	/**
	 *
	 * @param buf buffer to read from
	 * @return state that was readed, or {@link TrickState.UNKNOWN_STATE} if name is
	 *         uknown to execution side
	 */
	@Nonnull
	public static TrickState readState(PacketBuffer buf) {
		ResourceLocation name = buf.readResourceLocation();
		return getForName(name);
	}

	private static final String NAME_TAG = "registry_name";

	public CompoundNBT writeState() {
		CompoundNBT res = new CompoundNBT();
		res.putString(NAME_TAG, this.name.toString());
		return res;
	}

	public static TrickState readState(CompoundNBT nbt) {
		ResourceLocation name = new ResourceLocation(nbt.getString(NAME_TAG));
		return getForName(name);
	}

	/**
	 * @param trick Trick that message belongs to
	 * @return message to display at current state
	 */
	public ITextComponent getStateMessage(ITrick trick) {
		ResourceLocation trickName = trick.getType().getRegistryName();
		ResourceLocation stateName = this.name;
		String stateType = String.format(this.isError ? "error" : "normal");
		String trickKey = String.format("%s.trick.%s.%s", trickName.getNamespace(), trickName.getPath(), stateType);
		String stateKey = String.format("%s.trick_state.%s", stateName.getNamespace(), stateName.getPath(), stateType);
		Style messageStyle = this.isError ? ERROR_STYLE : (this.isSuccess ? SUCCESS_STYLE : NORMAL_STYLE);
		return new TranslationTextComponent(trickKey, new TranslationTextComponent(stateKey)).setStyle(messageStyle);
	}

	@Override
	public String toString() {
		return String.format("TrickState:%s", this.name.toString());
	}

}
