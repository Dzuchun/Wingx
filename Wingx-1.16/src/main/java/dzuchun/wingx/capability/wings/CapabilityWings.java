package dzuchun.wingx.capability.wings;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;

public class CapabilityWings {

	private static final Logger LOG = LogManager.getLogger();
	
	public static void register() {
		CapabilityManager.INSTANCE.register(IWingsCapability.class, new Capability.IStorage<IWingsCapability>() {

			private static final String IS_ACTIVE_TAG = "is_active";
			private static final String WINGS_UUID_TAG = "wings_uuid";
			
			@Override
			public INBT writeNBT(Capability<IWingsCapability> capability, IWingsCapability instance, Direction side) {
				CompoundNBT res = new CompoundNBT();
				res.putBoolean(IS_ACTIVE_TAG, instance.isActive());
				if (instance.getWingsUniqueId() != null) { 
					res.putUniqueId(WINGS_UUID_TAG, instance.getWingsUniqueId());
				}
				return res;
			}

			@Override
			public void readNBT(Capability<IWingsCapability> capability, IWingsCapability instance, Direction side,
					INBT nbt) {
				CompoundNBT cnbt = (CompoundNBT)nbt;
				instance.setActive(cnbt.getBoolean(IS_ACTIVE_TAG));
				if (!cnbt.hasUniqueId(WINGS_UUID_TAG) && instance.isActive()) {
					LOG.warn("Wings are active, but no UUID specified");
				} else {
					instance.setWingsUniqueId(cnbt.getUniqueId(WINGS_UUID_TAG));
				}
			}
		}, WingsCapability::new);
	}

}