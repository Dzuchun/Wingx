package dzuchun.wingx.capability.entity.wings.storage;

import java.util.UUID;

public class BasicData extends SerializedData {

	public boolean wingsActive;
	public UUID wingsUniqueId;
	public double requiredMeditationScore;
	public boolean needsEnd;

	public BasicData() {
		this.wingsActive = false;
		this.wingsUniqueId = null;
		this.requiredMeditationScore = 1.0d;
		this.needsEnd = true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Serializer<BasicData> getSerializer() {
		return Serializers.BASIC_SERIALIZER;
	}

}
