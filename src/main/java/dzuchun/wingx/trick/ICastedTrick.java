package dzuchun.wingx.trick;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;

public interface ICastedTrick extends ITrick {
	/**
	 * Sets the world to search for caster in.
	 *
	 * @param worldIn
	 */
	void setWorld(@Nonnull World worldIn);

	/**
	 * Sets a caster.
	 *
	 * @param caster
	 */
	void setCaster(@Nonnull Entity caster);

	/**
	 * @return Trick caster.
	 */
	@Nullable
	Entity getCaster();

	/**
	 * @return If caster present in set caster world.
	 */
	boolean hasCaster();
}
