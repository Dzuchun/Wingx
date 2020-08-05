package dzuchun.wingx.trick;

import net.minecraft.entity.player.PlayerEntity;

public class NoWingsException extends IllegalStateException {
	private static final long serialVersionUID = 1L;
	public NoWingsException(PlayerEntity caster) {
		super(String.format("Player %s has no wings capability", caster.getGameProfile().getName()));
	}
}
