package dzuchun.wingx.capability.world.tricks;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dzuchun.wingx.Wingx;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Wingx.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ActiveTricksProvider implements ICapabilitySerializable<INBT> {

	public static void init() {
	}

	private static final Logger LOG = LogManager.getLogger();

	@CapabilityInject(IActiveTricksCapability.class)
	public static final Capability<IActiveTricksCapability> ACTIVE_TRICKS = null;

	private IActiveTricksCapability instance = ACTIVE_TRICKS.getDefaultInstance();

	@SuppressWarnings("unchecked")
	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
		if (cap.equals(ACTIVE_TRICKS)) {
			return LazyOptional.of(() -> (T) instance);
		} else {
			return null;
		}
	}

	@Override
	public INBT serializeNBT() {
		return ACTIVE_TRICKS.getStorage().writeNBT(ACTIVE_TRICKS, instance, null);
	}

	@Override
	public void deserializeNBT(INBT nbt) {
		ACTIVE_TRICKS.getStorage().readNBT(ACTIVE_TRICKS, instance, null, nbt);
	}

	private static final ResourceLocation LOCATION = new ResourceLocation(Wingx.MOD_ID, "active_tricks");

	@SubscribeEvent
	public static void attachCapability(final AttachCapabilitiesEvent<World> event) {
		LOG.debug("Ataching caps to world.");
		if (!event.getObject().getCapability(ACTIVE_TRICKS, null).isPresent()) {
			event.addCapability(LOCATION, new ActiveTricksProvider());
		}
	}

}