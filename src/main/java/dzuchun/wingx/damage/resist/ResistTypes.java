package dzuchun.wingx.damage.resist;

import dzuchun.wingx.damage.resist.WingxResist.RessistType;

public class ResistTypes {

	public static void init() {
	}

	public static final WingxResist.RessistType<NoResist> NO_RESIST = WingxResist.RessistType
			.register(new RessistType<NoResist>("no_resist", NoResist::write, NoResist::read));

	public static final WingxResist.RessistType<ScaleResist> SCALE_RESIST = WingxResist.RessistType
			.register(new RessistType<ScaleResist>("scale", ScaleResist::write, ScaleResist::read));
}
