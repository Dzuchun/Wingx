package dzuchun.wingx.capability.entity.wings;

import dzuchun.wingx.capability.entity.wings.storage.WingsDataManager;
import net.minecraft.network.PacketBuffer;

public class WingsCapability implements IWingsCapability {

	@Override
	public void readFromBuffer(PacketBuffer buf) {
		this.dataManager.read(buf);
	}

	@Override
	public void writeToBuffer(PacketBuffer buf) {
		this.dataManager.write(buf);
	}

	private WingsDataManager dataManager = new WingsDataManager();

	@Override
	public WingsDataManager getDataManager() {
		return this.dataManager;
	}

	@Override
	public void copyFrom(IWingsCapability another) {
		this.dataManager = another.getDataManager().clone();
	}

}