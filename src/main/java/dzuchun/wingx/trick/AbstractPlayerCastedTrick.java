package dzuchun.wingx.trick;

import javax.annotation.Nullable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class AbstractPlayerCastedTrick extends AbstractCastedTrick {
	@SuppressWarnings("unused")
	private static final Logger LOG = LogManager.getLogger();

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
}
