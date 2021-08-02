package dzuchun.wingx.trick;

import net.minecraft.nbt.INBT;

public interface IPersistableTrick extends ITrick {

	public static interface TrickType<T extends IPersistableTrick> extends ITrick.ITrickType<T> {

		T readFromNBT(INBT nbt);

		INBT writeToNBT(T trick);
	}

}
