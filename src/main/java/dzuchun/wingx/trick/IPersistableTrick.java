package dzuchun.wingx.trick;

import net.minecraft.nbt.INBT;

public interface IPersistableTrick extends ITrick {

	void readFromNBT(INBT nbt);

	INBT writeToNBT();

}
