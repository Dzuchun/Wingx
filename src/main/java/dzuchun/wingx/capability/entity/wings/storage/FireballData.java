package dzuchun.wingx.capability.entity.wings.storage;

import dzuchun.wingx.trick.AbstractInterruptablePlayerTrick.InterruptCondition;

public class FireballData extends SerializedData {

	public int castDuration;
	public int packedColor;
	public InterruptCondition interruptCondition;
	public float damage;
	public double initialSpeed;

	public FireballData() {
		this.castDuration = 10;
		this.packedColor = 0xFFFFFFFF;
		this.damage = 5.0f;
		this.interruptCondition = InterruptCondition.NO_CONDITION;
		this.initialSpeed = 1.0d;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Serializer<FireballData> getSerializer() {
		return Serializers.FIREBALL_SERIALIZER;
	}

}
