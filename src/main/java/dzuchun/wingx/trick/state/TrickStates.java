package dzuchun.wingx.trick.state;

import dzuchun.wingx.capability.entity.wings.IWingsCapability;
import dzuchun.wingx.trick.AgilPlayerTrick;
import dzuchun.wingx.trick.SmashPlayerTrick;

//TODO make with config instead
public class TrickStates {

	public static final TrickState OK = TrickState.registerState(new TrickState("ok", false, false));
	/**
	 * Abstract success state; usually grated at the end of execution
	 */
	public static final TrickState SUCCESS = TrickState.registerState(new TrickState("success", false, true));
	/**
	 * Trick has activated aim protocol, but target is not selected yet
	 */
	public static final TrickState AIMING = TrickState.registerState(new TrickState("aiming", false, false));
	/**
	 * Trick has activated aim protocol and caster did pick a target
	 */
	public static final TrickState AIMED = TrickState.registerState(new TrickState("aimed", false, false));
	/**
	 * Trick began casing and not interrupted or finished yet
	 */
	public static final TrickState RUN = TrickState.registerState(new TrickState("run", false, false));
	/**
	 * Trick was interrupted, but it's a valid action, for exaple:
	 * {@link SmashPlayerTrick}
	 */
	public static final TrickState FAST_FORWARD = TrickState.registerState(new TrickState("fast_forward", false, true));
	/**
	 * Trick finished it's cast uninterrupted
	 */
	public static final TrickState RUN_ENDED = TrickState.registerState(new TrickState("run_ended", false, true));
	/**
	 * Trick is perk-type and effect activated, for example:
	 * {@link AgilPlayerTrick}}
	 */
	public static final TrickState PROCED = TrickState.registerState(new TrickState("proced", false, false));
	// Errors
	/**
	 * Abstract error state
	 */
	public static final TrickState ERROR_UNKNOWN = errorReason("error_unknown");
	/**
	 * Caster's hand was busy, so trick could not proceed
	 */
	public static final TrickState HAND_BUSY = errorReason("hand_busy");
	/**
	 * Caster has conflicting trick instance running, so trick could not proceed
	 */
	public static final TrickState CASTER_BUSY = errorReason("caster_busy");
	/**
	 * Valid caster was not found (usually by UUID), so trick could not proceed
	 */
	public static final TrickState NO_CASTER = errorReason("no_caster");
	/**
	 * Target was not found (usually by UUID), so trick could not proceed
	 */
	public static final TrickState NO_TARGET = errorReason("no_target");
	/**
	 * Caster has no {@link IWingsCapability}} attached to him, so trick could not
	 * proceed
	 */
	public static final TrickState NO_WINGS = errorReason("no_wings");
	/**
	 * Trick was interrupted and it was not valid action, so trick could not proceed
	 */
	public static final TrickState CAST_INTERRUPTED = errorReason("cast_interrupted");
	/**
	 * Abstract overlay problem state
	 */
	public static final TrickState OVERLAY_ERROR = errorReason("overlay_error");
	/**
	 * While setting up an overlay, conflicting overlay encountered, so trick could
	 * not proceed
	 */
	public static final TrickState OVERLAY_CONFLICT = errorReason("overlay_conflict");
	/**
	 * Caster was not in End dimension (required for execution), so trick could not
	 * proceed
	 */
	public static final TrickState NOT_IN_END = errorReason("not_in_end");
	/**
	 * Caster had not enough meditation points
	 * {@link MeditationUtil::getMeditationPoints}, so trick could not proceed
	 */
	public static final TrickState NOT_ENOUGH_MEDITATION_POINTS = errorReason("not_enough_meditation_points");

	private static TrickState errorReason(String name) {
		return TrickState.registerState(new TrickState(name, true, false));
	}

	public static void init() {
	}
}
