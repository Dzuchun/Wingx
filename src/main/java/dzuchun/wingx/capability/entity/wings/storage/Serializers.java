package dzuchun.wingx.capability.entity.wings.storage;

import java.util.Arrays;
import java.util.List;

public class Serializers {
	public static final Serializer<FireballData> FIREBALL_SERIALIZER = new FireballDataSerializer();
	public static final Serializer<BasicData> BASIC_SERIALIZER = new BasicDataSerializer();
	public static final Serializer<HastyData> HASTY_SERIALIZER = new HastyDataSerializer();
	public static final Serializer<AgilData> AGIL_SERIALIZER = new AgilDataSerializer();
	public static final Serializer<SoulswordData> SOULSWORD_SERIALIZER = new SoulswordDataSerializer();
	public static final Serializer<PunchData> PUNCH_SERIALIZER = new PunchDataSerializer();
	public static final Serializer<ShieldData> SHIELD_SERIALIZER = new ShieldDataResializer();

	public static final List<Serializer<?>> DEFAULT_SERIALIZERS = Arrays.asList(FIREBALL_SERIALIZER, BASIC_SERIALIZER,
			HASTY_SERIALIZER, AGIL_SERIALIZER, SOULSWORD_SERIALIZER, PUNCH_SERIALIZER, SHIELD_SERIALIZER);

	public static void init() {
		for (Serializer<?> serializer : DEFAULT_SERIALIZERS) {
			WingsDataManager.register(serializer);
		}
	}
}
