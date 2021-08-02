package dzuchun.wingx.capability.entity.wings.storage;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;

public class PunchDataSerializer extends Serializer<PunchData> {
	public static String NAME = "punch";

	public static String FORCE_TAG = "force";
	public static String RADIUS_TAG = "radius";

	@Override
	public PunchData read(CompoundNBT nbt) {
		PunchData res = new PunchData();
		res.force = nbt.getDouble(FORCE_TAG);
		res.radius = nbt.getDouble(RADIUS_TAG);
		return res;
	}

	@Override
	public void write(CompoundNBT nbt, PunchData data) {
		nbt.putDouble(FORCE_TAG, data.force);
		nbt.putDouble(RADIUS_TAG, data.radius);
	}

	@Override
	public PunchData read(PacketBuffer buf) {
		return this.read(buf.readCompoundTag());
	}

	@Override
	public void write(PacketBuffer buf, PunchData data) {
		CompoundNBT res = new CompoundNBT();
		this.write(res, data);
		buf.writeCompoundTag(res);
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public PunchData getDefault() {
		return new PunchData();
	}

}
