package dzuchun.wingx.capability.entity.wings.storage;

public abstract class SerializedData {
	public abstract <T extends SerializedData> Serializer<T> getSerializer();
}