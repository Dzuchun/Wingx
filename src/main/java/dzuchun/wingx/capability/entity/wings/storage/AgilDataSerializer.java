package dzuchun.wingx.capability.entity.wings.storage;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;

public class AgilDataSerializer extends Serializer<AgilData> {
	private static final String NAME = "agil";

	private static final String ACIVE_TAG = "active";
	private static final String COOLDOWN_TAG = "cooldown";
	private static final String PROBABILITY_TAG = "probability";

	@Override
	public AgilData read(CompoundNBT nbt) {
		AgilData res = new AgilData();
		res.isActive = nbt.getBoolean(ACIVE_TAG);
		res.cooldown = nbt.getInt(COOLDOWN_TAG);
		res.probability = nbt.getDouble(PROBABILITY_TAG);
		return res;
	}

	@Override
	public void write(CompoundNBT nbt, AgilData data) {
		nbt.putBoolean(ACIVE_TAG, data.isActive);
		nbt.putInt(COOLDOWN_TAG, data.cooldown);
		nbt.putDouble(PROBABILITY_TAG, data.probability);
	}

	@Override
	public AgilData read(PacketBuffer buf) {
		return this.read(buf.readCompoundTag());
	}

	@Override
	public void write(PacketBuffer buf, AgilData data) {
		CompoundNBT nbt = new CompoundNBT();
		this.write(nbt, data);
		buf.writeCompoundTag(nbt);
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public AgilData getDefault() {
		return new AgilData();
	}

}
