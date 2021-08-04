package dzuchun.wingx.damage;

import java.util.Map;
import java.util.Map.Entry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.Maps;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;

public class WingxDamageMap implements IWingxDamageMap {
	private static final Logger LOG = LogManager.getLogger();

	private final Map<WingxDamageType, Double> damages = Maps.newConcurrentMap();

	@Override
	public double getOfType(WingxDamageType type) {
		return this.damages.getOrDefault(type, 0.0d);
	}

	@Override
	public void setType(WingxDamageType type, double damage) {
		this.damages.replace(type, damage);
	}

	public void add(WingxDamageType type, double addition) {
		this.damages.compute(type, (tp, value) -> (value == null) ? addition : value + addition);
	}

	@Override
	public double getPassedDamage(IWingxDamageShield shield) {
		return this.damages.entrySet().stream()
				.mapToDouble(entry -> shield.getPassedDamage(entry.getKey(), entry.getValue())).sum();
	}

	@Override
	public double getTotalDamage() {
		return this.damages.values().stream().mapToDouble(damage -> damage).sum();
	}

	@Override
	public CompoundNBT writeToNBT() {
		CompoundNBT res = new CompoundNBT();
		for (Entry<WingxDamageType, Double> type : this.damages.entrySet()) {
			res.putDouble(type.getKey().name.toString(), type.getValue());
		}
		return res;
	}

	@Override
	public void readDataFromNBT(CompoundNBT nbt) {
		this.damages.clear();
		for (String name : nbt.keySet()) {
			WingxDamageType type = WingxDamageType.getForName(new ResourceLocation(name));
			if (type == null) {
				LOG.warn("Uknown damage type found in NBT: {}", name);
				continue;
			}
			double damage = nbt.getDouble(name);
			this.damages.put(type, damage);
		}
	}

	public static WingxDamageMap readDapageMap(CompoundNBT nbt) {
		WingxDamageMap res = new WingxDamageMap();
		res.readDataFromNBT(nbt);
		return res;
	}

}
