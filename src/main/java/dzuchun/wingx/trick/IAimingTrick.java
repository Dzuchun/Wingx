package dzuchun.wingx.trick;

import net.minecraftforge.fml.network.PacketDistributor.PacketTarget;

public interface IAimingTrick extends ITrick {
	/**
	 * This method is invoked on client to activate overlays, animations, etc
	 * relating to aiming stage of a trick
	 */
	public void beginAimClient();

	/**
	 * This method is invoked on server to check if trick aim can begin (e.g. enough
	 * mana, no disables) and ensure that trick's status signalizes valid execution.
	 */
	public void beginAimServer();

	/**
	 * Invoked on client, once user aimed, or doesn't want to aim anymore.
	 */
	public void endAim();

	public PacketTarget getAimBackTarget();

	public static interface TrickType<T extends IAimingTrick> extends ITrick.ITrickType<T> {
	}
}
