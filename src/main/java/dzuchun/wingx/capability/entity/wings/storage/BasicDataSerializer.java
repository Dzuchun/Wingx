package dzuchun.wingx.capability.entity.wings.storage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;

public class BasicDataSerializer extends Serializer<BasicData> {
	@SuppressWarnings("unused")
	private static final Logger LOG = LogManager.getLogger();
	private static final String NAME = "basic";

	private static final String IS_ACTIVE_TAG = "is_active";
	private static final String WINGS_UUID_TAG = "wings_uuid";
	private static final String MIN_MEDITATION_SCORE_TAG = "min_meditation_score";
	private static final String NEEDS_END_TAG = "needs_end";

	@Override
	public BasicData read(CompoundNBT nbt) {
		BasicData res = new BasicData();
		res.wingsActive = nbt.getBoolean(IS_ACTIVE_TAG);
		if (nbt.contains(WINGS_UUID_TAG)) {
			res.wingsUniqueId = nbt.getUniqueId(WINGS_UUID_TAG);
		}
		res.requiredMeditationScore = nbt.getDouble(MIN_MEDITATION_SCORE_TAG);
		res.needsEnd = nbt.getBoolean(NEEDS_END_TAG);
		return res;
	}

	@Override
	public void write(CompoundNBT nbt, BasicData data) {
		nbt.putBoolean(IS_ACTIVE_TAG, data.wingsActive);
		if (data.wingsUniqueId != null) {
			nbt.putUniqueId(WINGS_UUID_TAG, data.wingsUniqueId);
		}
		nbt.putDouble(MIN_MEDITATION_SCORE_TAG, data.requiredMeditationScore);
		nbt.putBoolean(NEEDS_END_TAG, data.needsEnd);
//		LOG.debug("Writing {} data: active {}, wings uuid {}, min meditation score {}, needs end {}", getName(),
//				data.wingsActive, data.wingsUniqueId, data.requiredMeditationScore, data.needsEnd);
	}

	@Override
	public BasicData read(PacketBuffer buf) {
		BasicData res = getDefault();
		res.wingsActive = buf.readBoolean();
		if (buf.readBoolean()) {
			res.wingsUniqueId = buf.readUniqueId();
		}
		res.requiredMeditationScore = buf.readDouble();
		res.needsEnd = buf.readBoolean();
//		LOG.debug("Readed {} data: active {}, wings uuid {}, min meditation score {}, needs end {}", getName(),
//				res.wingsActive, res.wingsUniqueId, res.requiredMeditationScore, res.needsEnd);
		return res;
	}

	@Override
	public void write(PacketBuffer buf, BasicData data) {
		buf.writeBoolean(data.wingsActive);
		buf.writeBoolean(data.wingsUniqueId != null);
		if (data.wingsUniqueId != null) {
			buf.writeUniqueId(data.wingsUniqueId);
		}
		buf.writeDouble(data.requiredMeditationScore);
		buf.writeBoolean(data.needsEnd);
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public BasicData getDefault() {
		return new BasicData();
	}

}
