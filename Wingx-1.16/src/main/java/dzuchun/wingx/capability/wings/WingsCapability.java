package dzuchun.wingx.capability.wings;

import java.util.UUID;

public class WingsCapability implements IWingsCapability {

	private boolean active = false;
	private UUID wingsUniqueId;

	@Override
	public boolean setActive(boolean active) {
		this.active = active;
		return true;
	}

	@Override
	public boolean isActive() {
		return active;
	}

	@Override
	public UUID getWingsUniqueId() {
		return wingsUniqueId;
	}

	@Override
	public void setWingsUniqueId(UUID wingsUniqueId) {
		this.wingsUniqueId = wingsUniqueId;
	}

}