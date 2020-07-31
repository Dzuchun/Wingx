package dzuchun.wingx.trick;

public interface ITimeredTrick extends IInterruptableTrick {

	/**
	 * @return Full cast time in ticks.
	 * @throws NoCasterException If method needs caster-related information, but
	 *                           caster not set.
	 */
	int timeFull() throws NoCasterException;

	/**
	 * @return Part left casting.
	 * @throws NoCasterException If method needs caster-related information, but
	 *                           caster not set.
	 */
	double partLeft() throws NoCasterException;
}
