package dzuchun.wingx.capability.entity.wings;

import java.util.UUID;

public interface IWingsCapability {

	boolean setActive(boolean active);

	boolean isActive();

	UUID getWingsUniqueId();

	void setWingsUniqueId(UUID wingsUniqueId);
}