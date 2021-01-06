package dzuchun.wingx.capability.entity.wings.storage;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;

public class HastyDataSerializer extends Serializer<HastyData> {
	private static final String NAME = "hasty";

	private static final String IS_ACTIVE_TAG = "is_active";
	private static final String PROBABILITY_TAG = "probability";
	private static final String JUMP_TAG = "jump";
	private static final String COOLDOWN_TAG = "cooldown";
	private static final String UNLOCKED_TAG = "unlocked";

	@Override
	public HastyData read(CompoundNBT nbt) {
		HastyData res = this.getDefault();
		res.isActive = nbt.getBoolean(IS_ACTIVE_TAG);
		res.probability = nbt.getDouble(PROBABILITY_TAG);
		res.jump = nbt.getFloat(JUMP_TAG);
		res.cooldown = nbt.getInt(COOLDOWN_TAG);
		res.unlocked = nbt.getBoolean(UNLOCKED_TAG);
		return res;
	}

	@Override
	public void write(CompoundNBT nbt, HastyData data) {
		nbt.putBoolean(IS_ACTIVE_TAG, data.isActive);
		nbt.putDouble(PROBABILITY_TAG, data.probability);
		nbt.putFloat(JUMP_TAG, data.jump);
		nbt.putInt(COOLDOWN_TAG, data.cooldown);
		nbt.putBoolean(UNLOCKED_TAG, data.unlocked);
	}

	@Override
	public HastyData read(PacketBuffer buf) {
		return this.read(buf.readCompoundTag());
	}

	@Override
	public void write(PacketBuffer buf, HastyData data) {
		CompoundNBT res = new CompoundNBT();
		this.write(res, data);
		buf.writeCompoundTag(res);
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public HastyData getDefault() {
		return new HastyData();
	}

}
