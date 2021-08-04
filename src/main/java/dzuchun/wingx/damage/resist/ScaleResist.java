package dzuchun.wingx.damage.resist;

import net.minecraft.nbt.CompoundNBT;

public class ScaleResist extends WingxResist {

	private double scale;

	public ScaleResist(double scaleIn) {
		super(ResistTypes.SCALE_RESIST);
		this.scale = scaleIn;
	}

	@Override
	public double getPassed(double rawDamage) {
		return rawDamage * (1 - this.scale);
	}

	private static final String SCALE_TAG = "scale";

	public static CompoundNBT write(ScaleResist resist) {
		CompoundNBT res = new CompoundNBT();
		res.putDouble(SCALE_TAG, resist.scale);
		return res;
	}

	public static ScaleResist read(CompoundNBT nbt) {
		double scale;
		if (nbt.contains(SCALE_TAG)) {
			scale = nbt.getDouble(SCALE_TAG);
		} else {
			scale = 0.0d; // Default value
		}
		return new ScaleResist(scale);
	}

}
