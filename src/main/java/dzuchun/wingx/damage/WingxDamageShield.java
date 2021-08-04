package dzuchun.wingx.damage;

import java.util.Map;
import java.util.function.BiConsumer;

import com.google.common.collect.Maps;

import dzuchun.wingx.damage.resist.NoResist;
import dzuchun.wingx.damage.resist.WingxResist;

public class WingxDamageShield implements IWingxDamageShield {
	protected final Map<WingxDamageType, WingxResist> resists = Maps.newConcurrentMap();

	@Override
	public double getPassedDamage(WingxDamageType type, double rawDamage) {
		return this.resists.getOrDefault(type, new NoResist()).getPassed(rawDamage);
	}

	@Override
	public void addResist(WingxDamageType type, WingxResist resist) {
		this.resists.putIfAbsent(type, resist);
	}

	@Override
	public void forEachResist(BiConsumer<WingxDamageType, WingxResist> action) {
		this.resists.forEach((type, resist) -> action.accept(type, resist));
	}

}
