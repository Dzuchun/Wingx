package dzuchun.wingx.capability.entity.wings.storage;

import java.util.Arrays;
import java.util.List;

import dzuchun.wingx.damage.IWingxDamageShield;
import dzuchun.wingx.damage.WingxDamageShield;
import dzuchun.wingx.damage.WingxDamageType;
import dzuchun.wingx.damage.resist.ScaleResist;

public class ShieldData extends SerializedData {
	public IWingxDamageShield shield;

	public ShieldData() {
		this.shield = new WingxDamageShield();
		// TODO remove folowing, used for debug
		this.shield.addResist(WingxDamageType.H, new ScaleResist(0.5d));
		this.shield.addResist(WingxDamageType.G, new ScaleResist(0.25d));
		this.shield.addResist(WingxDamageType.F, new ScaleResist(0.1d));
	}

	@SuppressWarnings("unchecked")
	@Override
	public Serializer<ShieldData> getSerializer() {
		return Serializers.SHIELD_SERIALIZER;
	}

	@Override
	protected List<CommandLiteral<? extends SerializedData, ?>> getCommandLiterals() {
		return Arrays.asList();
	}
}
