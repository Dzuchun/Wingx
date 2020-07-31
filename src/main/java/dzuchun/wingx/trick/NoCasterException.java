package dzuchun.wingx.trick;

public class NoCasterException extends IllegalStateException {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	public NoCasterException(ICastedTrick trick) {
		super("Cannot execute operation without caster at " + trick);
	}
}
