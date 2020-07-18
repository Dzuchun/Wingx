package dzuchun.wingx.capability.entity.wings;

import java.util.UUID;

public interface IWingsCapability {

	// TODO doc
	boolean setActive(boolean active);

	// TODO doc
	boolean isActive();

	UUID getWingsUniqueId();

	void setWingsUniqueId(UUID wingsUniqueId);
}