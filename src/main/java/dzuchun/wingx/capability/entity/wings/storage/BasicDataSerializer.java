package dzuchun.wingx.capability.entity.wings.storage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;

public class BasicDataSerializer extends Serializer<BasicData> {
	private static final Logger LOG = LogManager.getLogger();
	private static final String NAME = "basic";

	private static final String IS_ACTIVE_TAG = "wings_active";
	private static final String WINGS_UUID_TAG = "wings_uuid";
	private static final String MEDITATION_LENGTH = "meditation_length";
	private static final String MIN_MEDITATION_SCORE_TAG = "min_meditation_score";
	private static final String NEEDS_END_TAG = "needs_end";
	private static final String STAGE_FLAGS_TAG = "stage_flags";

	@Override
	public BasicData read(CompoundNBT nbt) {
		BasicData res = new BasicData();
		res.wingsActive = nbt.getBoolean(IS_ACTIVE_TAG);
		if (nbt.contains(WINGS_UUID_TAG)) {
			res.wingsUniqueId = nbt.getUniqueId(WINGS_UUID_TAG);
		}
		res.meditationLength = nbt.getInt(MEDITATION_LENGTH);
		res.requiredMeditationScore = nbt.getDouble(MIN_MEDITATION_SCORE_TAG);
		res.needsEnd = nbt.getBoolean(NEEDS_END_TAG);
		res.stageFlags = nbt.getInt(STAGE_FLAGS_TAG);
		LOG.debug("Readed {} data: active {}, wings uuid {}, min meditation score {}, needs end {}", this.getName(),
				res.wingsActive, res.wingsUniqueId, res.requiredMeditationScore, res.needsEnd);
		return res;
	}

	@Override
	public void write(CompoundNBT nbt, BasicData data) {
		nbt.putBoolean(IS_ACTIVE_TAG, data.wingsActive);
		if (data.wingsUniqueId != null) {
			nbt.putUniqueId(WINGS_UUID_TAG, data.wingsUniqueId);
		}
		nbt.putInt(MEDITATION_LENGTH, data.meditationLength);
		nbt.putDouble(MIN_MEDITATION_SCORE_TAG, data.requiredMeditationScore);
		nbt.putBoolean(NEEDS_END_TAG, data.needsEnd);
//		LOG.debug("Writing {} data: active {}, wings uuid {}, min meditation score {}, needs end {}", getName(),
//				data.wingsActive, data.wingsUniqueId, data.requiredMeditationScore, data.needsEnd);
		nbt.putInt(STAGE_FLAGS_TAG, data.stageFlags);
	}

	@Override
	public BasicData read(PacketBuffer buf) {
		BasicData res = this.getDefault();
		res.wingsActive = buf.readBoolean();
		if (buf.readBoolean()) {
			res.wingsUniqueId = buf.readUniqueId();
		}
		res.meditationLength = buf.readInt();
		res.requiredMeditationScore = buf.readDouble();
		res.needsEnd = buf.readBoolean();
		res.stageFlags = buf.readInt();
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
		buf.writeInt(data.meditationLength);
		buf.writeDouble(data.requiredMeditationScore);
		buf.writeBoolean(data.needsEnd);
		buf.writeInt(data.stageFlags);
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
