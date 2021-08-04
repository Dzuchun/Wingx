package dzuchun.wingx.damage;

import dzuchun.wingx.capability.entity.wings.IWingsCapability;
import dzuchun.wingx.capability.entity.wings.WingsProvider;
import dzuchun.wingx.capability.entity.wings.storage.Serializers;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundNBT;

public interface IWingxDamageMap {
	double getOfType(WingxDamageType type);

	void setType(WingxDamageType type, double damage);

	double getPassedDamage(IWingxDamageShield shield);

	double getTotalDamage();

	CompoundNBT writeToNBT();

	void readDataFromNBT(CompoundNBT nbt);

	default double getDamageTo(Entity entity) {
		IWingsCapability cap = entity.getCapability(WingsProvider.WINGS).orElse(null);
		if (cap == null) {
			return this.getTotalDamage();
		}
		IWingxDamageShield shield = cap.getDataManager().getOrAddDefault(Serializers.SHIELD_SERIALIZER).shield;
		return this.getPassedDamage(shield);
	}
}