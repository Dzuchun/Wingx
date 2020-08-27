package dzuchun.wingx.trick;

import javax.annotation.Nullable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.ImmutableList;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.Color;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class AbstractPlayerCastedTrick extends AbstractCastedTrick {
	@SuppressWarnings("unused")
	private static final Logger LOG = LogManager.getLogger();
	protected static final Style ERROR_STYLE = Style.EMPTY.setColor(Color.func_240743_a_(0xFFFF0000));
	protected static final Style SUCCESS_STYLE = Style.EMPTY.setColor(Color.func_240743_a_(0xFF22BB00));
	protected static final Style PROC_STYLE = Style.EMPTY.setColor(Color.func_240743_a_(0xFF888888));

	public AbstractPlayerCastedTrick() {
		super();
	}

	public AbstractPlayerCastedTrick(@Nullable PlayerEntity caster) {
		super(caster);
	}

	@OnlyIn(Dist.CLIENT)
	public boolean amICaster() {
		return this.casterUniqueId != null && this.casterUniqueId.equals(Minecraft.getInstance().player.getUniqueID());
	}

	/**
	 * @return Caster that is guaranteed to be a player.
	 */
	@Nullable
	public PlayerEntity getCasterPlayer() {
		Entity caster = getCaster();
		return caster != null && caster instanceof PlayerEntity ? (PlayerEntity) caster : null;
	}

	/**
	 * @return If trick has caster that is Player.
	 */
	@Nullable
	public boolean hasCasterPlayer() {
		return getCasterPlayer() != null;
	}

	private static final ImmutableList<ITextComponent> MESSAGES = ImmutableList.of(
			new TranslationTextComponent("wingx.trick.default_success").setStyle(SUCCESS_STYLE),
			new TranslationTextComponent("wingx.trick.default_error").setStyle(ERROR_STYLE),
			new TranslationTextComponent("wingx.trick.default_proc").setStyle(PROC_STYLE),
			new TranslationTextComponent("wingx.trick.default_cast_end").setStyle(SUCCESS_STYLE));

	protected ImmutableList<ITextComponent> getMessages() {
		return MESSAGES;
	}

	@OnlyIn(value = Dist.CLIENT)
	@Override
	public void showMessage() {
		if (amICaster()) {
			getCasterPlayer().sendStatusMessage(getMessages().get(this.status), true);
		}
	}
}
