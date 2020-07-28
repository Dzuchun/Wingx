package dzuchun.wingx.trick;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;

public interface IInterruptableTrick extends ITrick {
	void interrupt(Entity caster);

	/**
	 * @return Time left in ticks.
	 */
	int timeLeft(World worldIn);

	/**
	 * @return Full cast time in ticks.
	 */
	int timeFull(World worldIn);

	/**
	 * @return Part left casting.
	 */
	double partLeft(World worldIn);

	/**
	 * Invoked every tick, used for any sort of processing.
	 */
	void tick(Entity caster);

	void beginCast(Entity caster);

	void onCastEnd(Entity caster);
}