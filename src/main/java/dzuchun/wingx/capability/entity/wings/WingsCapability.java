package dzuchun.wingx.capability.entity.wings;

import java.util.UUID;

import dzuchun.wingx.trick.AbstractInterruptablePlayerTrick.InterruptCondition;
import net.minecraft.network.PacketBuffer;

public class WingsCapability implements IWingsCapability {

	private boolean active = false;

	@Override
	public boolean setActive(boolean active) {
		this.active = active;
		return true;
	}

	@Override
	public boolean isActive() {
		return this.active;
	}

	private UUID wingsUniqueId;

	@Override
	public UUID getWingsUniqueId() {
		return this.wingsUniqueId;
	}

	@Override
	public void setWingsUniqueId(UUID wingsUniqueId) {
		this.wingsUniqueId = wingsUniqueId;
	}

	private double meditationScoreRequired = 1.0f;

	@Override
	public double getMeditationScore() {
		return this.meditationScoreRequired;
	}

	@Override
	public void setMeditationScore(double score) {
		this.meditationScoreRequired = score;
	}

	@Override
	public void readFromBuffer(PacketBuffer buf) {
		this.active = buf.readBoolean();
		if (buf.readBoolean()) {
			this.wingsUniqueId = buf.readUniqueId();
		}
		this.meditationScoreRequired = buf.readDouble();
		this.needsEnd = buf.readBoolean();
	}

	@Override
	public void writeToBuffer(PacketBuffer buf) {
		buf.writeBoolean(this.active);
		if (this.wingsUniqueId != null) {
			buf.writeBoolean(true);
			buf.writeUniqueId(this.wingsUniqueId);
		} else {
			buf.writeBoolean(false);
		}
		buf.writeDouble(this.meditationScoreRequired);
		buf.writeBoolean(this.needsEnd);
	}

	private boolean needsEnd = true;

	@Override
	public void setNeedsEndToMeditate(boolean needsEndIn) {
		this.needsEnd = needsEndIn;
	}

	@Override
	public boolean needsEndForMeditation() {
		return this.needsEnd;
	}

	@Override
	public int fireballCastDuration() {
		return 10;
	}

	@Override
	public int fireballColor() {
		return 0xFFFFFFFF;
	}

	@Override
	public float fireballDamage() {
		return 5.0f;
	}

	@Override
	public InterruptCondition fireballInterruptCondition() {
		return InterruptCondition.NO_CONDITION;
	}

	@Override
	public double fireballInitialSpeed() {
		return 1.0d;
	}

}