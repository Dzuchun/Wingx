package dzuchun.wingx.capability.entity.wings.storage;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;

public abstract class Serializer<T extends SerializedData> {

	public abstract T read(CompoundNBT nbt);

	public abstract void write(CompoundNBT nbt, T data);

	public abstract T read(PacketBuffer buf);

	public abstract void write(PacketBuffer buf, T data);

	public abstract String getName();

	public abstract T getDefault();
}