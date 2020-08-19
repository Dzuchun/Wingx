package dzuchun.wingx.capability.entity.wings.storage;

public class HastyData extends SerializedData {

	// Serialized
	public boolean isActive;
	public double probability;
	public float jump;
	public int cooldown;
	public boolean unlocked;

	// Not serialized
	public long lastProc;

	public HastyData() {
		this.isActive = false;
		this.probability = 0.01d;
		this.jump = 0.1f;
		this.cooldown = 100;
		this.unlocked = false;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Serializer<HastyData> getSerializer() {
		return Serializers.HASTY_SERIALIZER;
	}

}
