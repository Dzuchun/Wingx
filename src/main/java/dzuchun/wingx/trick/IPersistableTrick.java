package dzuchun.wingx.trick;

import net.minecraft.nbt.INBT;

public interface IPersistableTrick extends IInterruptableTrick {

	public static interface TrickType<T extends IPersistableTrick> extends IInterruptableTrick.ITrickType<T> {

		T readFromNBT(INBT nbt);

		INBT writeToNBT(T trick);
	}

}
