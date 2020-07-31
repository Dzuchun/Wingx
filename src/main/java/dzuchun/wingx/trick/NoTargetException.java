package dzuchun.wingx.trick;

public class NoTargetException extends IllegalStateException {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	public NoTargetException(ITargetedTrick trick) {
		super("Cannot execute operation without caster at " + trick);
	}
}
