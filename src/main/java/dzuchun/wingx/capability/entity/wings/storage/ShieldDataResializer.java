package dzuchun.wingx.capability.entity.wings.storage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dzuchun.wingx.damage.WingxDamageType;
import dzuchun.wingx.damage.resist.WingxResist;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;

public class ShieldDataResializer extends Serializer<ShieldData> {
	@SuppressWarnings("unused")
	private static final Logger LOG = LogManager.getLogger();
	private static final String NAME = "shield";

	@Override
	public ShieldData read(CompoundNBT nbt) {
		final ShieldData res = this.getDefault();
		for (String name : nbt.keySet()) {
			ResourceLocation loc = new ResourceLocation(name);
			WingxDamageType damageType = WingxDamageType.getForName(loc);
			if (damageType != null) {
				CompoundNBT resistNBT = nbt.getCompound(name);
				WingxResist resist = WingxResist.RessistType.read(resistNBT);
				res.shield.addResist(damageType, resist);
			}
		}
		return res;
	}

	@Override
	public void write(CompoundNBT nbt, ShieldData data) {
		data.shield
				.forEachResist((type, resist) -> nbt.put(type.name.toString(), resist.getType().writeChecked(resist)));
	}

	@Override
	public ShieldData read(PacketBuffer buf) {
		return this.read(buf.readCompoundTag());
	}

	@Override
	public void write(PacketBuffer buf, ShieldData data) {
		CompoundNBT nbt = new CompoundNBT();
		this.write(nbt, data);
		buf.writeCompoundTag(nbt);
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public ShieldData getDefault() {
		return new ShieldData();
	}

}
