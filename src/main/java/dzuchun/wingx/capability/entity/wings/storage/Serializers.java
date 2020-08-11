package dzuchun.wingx.capability.entity.wings.storage;

import java.util.Arrays;
import java.util.List;

public class Serializers {
	public static final Serializer<FireballData> FIREBALL_SERIALIZER = new FireballDataSerializer();
	public static final Serializer<BasicData> BASIC_SERIALIZER = new BasicDataSerializer();

	public static final List<Serializer<?>> DEFAULT_SERIALIZERS = Arrays.asList(FIREBALL_SERIALIZER, BASIC_SERIALIZER);

	public static void init() {
		for (Serializer<?> serializer : DEFAULT_SERIALIZERS) {
			WingsDataManager.register(serializer);
		}
	}
}