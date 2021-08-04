package dzuchun.wingx.damage;

import java.util.function.BiConsumer;

import dzuchun.wingx.damage.resist.WingxResist;

public interface IWingxDamageShield {
	double getPassedDamage(WingxDamageType type, double rawDamage);

	void forEachResist(BiConsumer<WingxDamageType, WingxResist> action);

	void addResist(WingxDamageType type, WingxResist resist);
}
