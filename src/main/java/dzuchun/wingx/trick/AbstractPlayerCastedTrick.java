package dzuchun.wingx.trick;

import javax.annotation.Nullable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.Color;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class AbstractPlayerCastedTrick extends AbstractCastedTrick {
	@SuppressWarnings("unused")
	private static final Logger LOG = LogManager.getLogger();
	protected static final Style ERROR_STYLE = Style.EMPTY.setColor(Color.fromInt(0xFFFF0000));
	protected static final Style SUCCESS_STYLE = Style.EMPTY.setColor(Color.fromInt(0xFF22BB00));
	protected static final Style NEUTRAL_STYLE = Style.EMPTY.setColor(Color.fromInt(0xFF888888));

	public AbstractPlayerCastedTrick(@Nullable PlayerEntity caster) {
		super(caster);
	}

	@OnlyIn(Dist.CLIENT)
	public boolean iAmCaster() {
		return (this.casterUniqueId != null)
				&& this.casterUniqueId.equals(Minecraft.getInstance().player.getUniqueID());
	}

	/**
	 * @return Caster that is guaranteed to be a PlayerEntity or null.
	 */
	@Nullable
	public PlayerEntity getCasterPlayer() {
		Entity caster = this.getCaster();
		return (caster != null) && (caster instanceof PlayerEntity) ? (PlayerEntity) caster : null;
	}

	/**
	 * @return If trick has caster that is Player.
	 */
	@Nullable
	public boolean hasCasterPlayer() {
		return this.getCasterPlayer() != null;
	}

	public ITextComponent getStateMessage() {
		return this.state.getStateMessage(this);
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public void reportState() {
		if (this.iAmCaster()) {
			this.getCasterPlayer().sendStatusMessage(this.getStateMessage(), true);
		}
	}

	public abstract static class TrickType<T extends AbstractPlayerCastedTrick>
			extends AbstractCastedTrick.TrickType<T> {
	}
}
