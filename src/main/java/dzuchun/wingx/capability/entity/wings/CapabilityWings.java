package dzuchun.wingx.capability.entity.wings;

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

			private static final String DATA_TAG = "wings_data";

			@Override
			public INBT writeNBT(Capability<IWingsCapability> capability, IWingsCapability instance, Direction side) {
				CompoundNBT res = new CompoundNBT();
				CompoundNBT dataTag = new CompoundNBT();
				instance.getDataManager().write(dataTag);
				res.put(DATA_TAG, dataTag);
				LOG.debug("writing wings capability {} as {}", instance, res.toString());
				return res;
			}

			@Override
			public void readNBT(Capability<IWingsCapability> capability, IWingsCapability instance, Direction side,
					INBT nbt) {
				CompoundNBT cnbt = (CompoundNBT) nbt;
				LOG.debug("Reading wings capability {} as {}", instance, cnbt.toString());
				if (cnbt.contains(DATA_TAG)) {
					CompoundNBT dataTag = cnbt.getCompound(DATA_TAG);
					instance.getDataManager().read(dataTag);
				}
			}
		}, WingsCapability::new);
	}

}