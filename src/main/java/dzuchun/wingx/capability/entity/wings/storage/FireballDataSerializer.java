package dzuchun.wingx.capability.entity.wings.storage;

import dzuchun.wingx.trick.AbstractInterruptablePlayerTrick.InterruptCondition;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;

public class FireballDataSerializer extends Serializer<FireballData> {
	private static final String NAME = "fireball";

	private static final String CAST_DURATION_TAG = "cast_duration";
	private static final String COLOR_TAG = "color";
	private static final String INTERRUPT_CONDITION_TAG = "interrupt_condition";
	private static final String DAMAGE_TAG = "damage";
	private static final String INITIAL_SPEED_TAG = "initial_speed";

	private static final String IS_UNLOCKED_TAG = "unlocked_tag";

	@Override
	public FireballData read(CompoundNBT nbt) {
		FireballData data = getDefault();
		data.castDuration = nbt.getInt(CAST_DURATION_TAG);
		data.packedColor = nbt.getInt(COLOR_TAG);
		data.interruptCondition = InterruptCondition.getFromInt(nbt.getInt(INTERRUPT_CONDITION_TAG));
		data.damage = nbt.getFloat(DAMAGE_TAG);
		data.initialSpeed = nbt.getDouble(INITIAL_SPEED_TAG);
		data.isUnlocked = nbt.getBoolean(IS_UNLOCKED_TAG);
		return data;
	}

	@Override
	public void write(CompoundNBT nbt, FireballData data) {
		nbt.putInt(CAST_DURATION_TAG, data.castDuration);
		nbt.putInt(COLOR_TAG, data.packedColor);
		nbt.putInt(INTERRUPT_CONDITION_TAG, data.interruptCondition.toInt());
		nbt.putFloat(DAMAGE_TAG, data.damage);
		nbt.putDouble(INITIAL_SPEED_TAG, data.initialSpeed);
		nbt.putBoolean(IS_UNLOCKED_TAG, data.isUnlocked);
	}

	@Override
	public FireballData read(PacketBuffer buf) {
		FireballData data = getDefault();
		data.castDuration = buf.readInt();
		data.packedColor = buf.readInt();
		data.interruptCondition = InterruptCondition.getFromInt(buf.readInt());
		data.damage = buf.readFloat();
		data.initialSpeed = buf.readDouble();
		data.isUnlocked = buf.readBoolean();
		return data;
	}

	@Override
	public void write(PacketBuffer buf, FireballData data) {
		buf.writeInt(data.castDuration);
		buf.writeInt(data.packedColor);
		buf.writeInt(data.interruptCondition.toInt());
		buf.writeFloat(data.damage);
		buf.writeDouble(data.initialSpeed);
		buf.writeBoolean(data.isUnlocked);
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public FireballData getDefault() {
		return new FireballData();
	}

}
