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

	private static final String HOMING_UNLOCKED_TAG = "homing_unlocked";
	private static final String HOMING_FORCE_TAG = "homing_force";

	@Override
	public FireballData read(CompoundNBT nbt) {
		FireballData data = this.getDefault();
		data.castDuration = nbt.getInt(CAST_DURATION_TAG);
		data.packedColor = nbt.getInt(COLOR_TAG);
		data.interruptCondition = InterruptCondition.getFromInt(nbt.getInt(INTERRUPT_CONDITION_TAG));
		data.damage = nbt.getFloat(DAMAGE_TAG);
		data.initialSpeed = nbt.getDouble(INITIAL_SPEED_TAG);
		data.isUnlocked = nbt.getBoolean(IS_UNLOCKED_TAG);
		data.homingUnlocked = nbt.getBoolean(HOMING_UNLOCKED_TAG);
		data.homingForce = nbt.getDouble(HOMING_FORCE_TAG);
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
		nbt.putBoolean(HOMING_UNLOCKED_TAG, data.homingUnlocked);
		nbt.putDouble(HOMING_FORCE_TAG, data.homingForce);
	}

	@Override
	public FireballData read(PacketBuffer buf) {
		FireballData data = this.getDefault();
		data.castDuration = buf.readInt();
		data.packedColor = buf.readInt();
		data.interruptCondition = InterruptCondition.getFromInt(buf.readInt());
		data.damage = buf.readFloat();
		data.initialSpeed = buf.readDouble();
		data.isUnlocked = buf.readBoolean();
		data.homingUnlocked = buf.readBoolean();
		data.homingForce = buf.readDouble();
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
		buf.writeBoolean(data.homingUnlocked);
		buf.writeDouble(data.homingForce);
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
