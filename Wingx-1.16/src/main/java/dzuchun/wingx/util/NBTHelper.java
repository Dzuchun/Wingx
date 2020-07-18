package dzuchun.wingx.util;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.vector.Vector3d;

public class NBTHelper {
	public static CompoundNBT writeVector3d(Vector3d vector) {
		CompoundNBT nbt = new CompoundNBT();
		nbt.putDouble("x", vector.x);
		nbt.putDouble("y", vector.y);
		nbt.putDouble("z", vector.z);
		return nbt;
	}

	public static Vector3d readVector3d(CompoundNBT nbt) throws NBTReadingException {
		if (!nbt.contains("x") || !nbt.contains("y") || !nbt.contains("z")) {
			throw new NBTReadingException();
		}
		return new Vector3d(nbt.getDouble("x"), nbt.getDouble("y"), nbt.getDouble("z"));
	}
}