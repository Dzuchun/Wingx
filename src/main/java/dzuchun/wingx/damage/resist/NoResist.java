package dzuchun.wingx.damage.resist;

import net.minecraft.nbt.CompoundNBT;

public class NoResist extends WingxResist {

	public NoResist() {
		super(ResistTypes.NO_RESIST);
	}

	@Override
	public double getPassed(double rawDamage) {
		return rawDamage;
	}

	public static CompoundNBT write(NoResist resist) {
		return new CompoundNBT();
	}

	public static NoResist read(CompoundNBT nbt) {
		return new NoResist();
	}
}
