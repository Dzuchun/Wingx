package dzuchun.wingx.capability.entity.wings.storage;

public class AgilData extends SerializedData {

	// Serailized
	public boolean isActive;
	public int cooldown;
	public double probability;

	// Unserialized
	public long lastProc;

	public AgilData() {
		this.isActive = true;
		this.cooldown = 100;
		this.probability = 0.2d;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Serializer<AgilData> getSerializer() {
		return Serializers.AGIL_SERIALIZER;
	}

}
