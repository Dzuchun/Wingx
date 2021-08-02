package dzuchun.wingx.trick;

import net.minecraftforge.fml.network.PacketDistributor.PacketTarget;

public interface IInterruptableTrick extends ICastedTrick {

	/**
	 * Invoked once at cast begin, to write beginning time.
	 *
	 * @throws NoCasterException If method needs caster-related information, but
	 *                           caster not set.
	 */
	void beginCast() throws NoCasterException;

	/**
	 * @return Time left before trick ends in ticks.
	 * @throws NoCasterException If method needs caster-related information, but
	 *                           caster not set.
	 */
	int timeLeft() throws NoCasterException;

	/**
	 * Invoked every tick, used for any sort of processing.
	 *
	 * @throws NoCasterException If method needs caster-related information, but
	 *                           caster not set.
	 */
	void tick() throws NoCasterException;

	/**
	 * Invoked once at cast end, for some post-processing.
	 *
	 * @throws NoCasterException If method needs caster-related information, but
	 *                           caster not set.
	 */
	default void onTrickEndClient() throws NoCasterException {
		this.onTrickEndCommon();
	}

	default void onTrickEndServer() throws NoCasterException {
		this.onTrickEndCommon();
	}

	default void onTrickEndCommon() throws NoCasterException {
	}

	/**
	 * @return PacketTarget to send TrickFinished message.
	 */
	PacketTarget getEndPacketTarget();

	/**
	 * Defines if trick should be keep in memory.
	 *
	 * @return If trick should be keep till next tick.
	 */
	boolean keepExecuting();

	/**
	 * Defines trick's allowed concurrent executions
	 *
	 * @param other
	 * @return Whether passe trick disallowes current execution or not.
	 */
	default boolean conflicts(IInterruptableTrick other) {
		return false;
	}

	public static interface TrickType<T extends IInterruptableTrick> extends ICastedTrick.TrickType<T> {
	}

}