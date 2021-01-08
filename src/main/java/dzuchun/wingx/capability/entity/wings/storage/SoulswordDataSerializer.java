package dzuchun.wingx.capability.entity.wings.storage;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;

public class SoulswordDataSerializer extends Serializer<SoulswordData> {
	private static final String NAME = "soulsword";

	private static final String HAS_COLORS_TAG = "has_colors";
	private static final String COLOR_1_TAG = "color_1";
	private static final String COLOR_2_TAG = "color_2";
	private static final String COLOR_3_TAG = "color_3";
	private static final String COLOR_4_TAG = "color_4";
	private static final String SUMMON_DURATION_TAG = "summon_duration";
	private static final String UNLOCKED_TAG = "unlocked";

	@Override
	public SoulswordData read(CompoundNBT nbt) {
		SoulswordData res = this.getDefault();
		if (nbt.getBoolean(HAS_COLORS_TAG)) {
			res.colors[0] = nbt.getInt(COLOR_1_TAG);
			res.colors[1] = nbt.getInt(COLOR_2_TAG);
			res.colors[2] = nbt.getInt(COLOR_3_TAG);
			res.colors[3] = nbt.getInt(COLOR_4_TAG);
			res.summonDurationTicks = nbt.getInt(SUMMON_DURATION_TAG);
			res.isUnlocked = nbt.getBoolean(UNLOCKED_TAG);
		}
		return res;
	}

	@Override
	public void write(CompoundNBT nbt, SoulswordData data) {
		nbt.putBoolean(HAS_COLORS_TAG, data.hasColors);
		if (data.hasColors) {
			nbt.putInt(COLOR_1_TAG, data.colors[0]);
			nbt.putInt(COLOR_2_TAG, data.colors[1]);
			nbt.putInt(COLOR_3_TAG, data.colors[2]);
			nbt.putInt(COLOR_4_TAG, data.colors[3]);
			nbt.putInt(SUMMON_DURATION_TAG, data.summonDurationTicks);
			nbt.putBoolean(UNLOCKED_TAG, data.isUnlocked);
		}
	}

	@Override
	public SoulswordData read(PacketBuffer buf) {
		SoulswordData res = this.getDefault();
		if (buf.readBoolean()) {
			res.colors[0] = buf.readInt();
			res.colors[1] = buf.readInt();
			res.colors[2] = buf.readInt();
			res.colors[3] = buf.readInt();
			res.summonDurationTicks = buf.readInt();
			res.isUnlocked = buf.readBoolean();
		}
		return res;
	}

	@Override
	public void write(PacketBuffer buf, SoulswordData data) {
		buf.writeBoolean(data.hasColors);
		if (data.hasColors) {
			buf.writeInt(data.colors[0]);
			buf.writeInt(data.colors[1]);
			buf.writeInt(data.colors[2]);
			buf.writeInt(data.colors[3]);
			buf.writeInt(data.summonDurationTicks);
			buf.writeBoolean(data.isUnlocked);
		}
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public SoulswordData getDefault() {
		return new SoulswordData();
	}

}
