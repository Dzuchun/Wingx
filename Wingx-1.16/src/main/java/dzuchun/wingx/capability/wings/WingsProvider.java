package dzuchun.wingx.capability.wings;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dzuchun.wingx.Wingx;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Wingx.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class WingsProvider implements ICapabilitySerializable<INBT>{
	
	public static void init() {}
	
	@SuppressWarnings("unused")
	private static final Logger LOGGER = LogManager.getLogger();

	@CapabilityInject(IWingsCapability.class)
	public static final Capability<IWingsCapability> WINGS = null;
	
	private IWingsCapability instance = WINGS.getDefaultInstance();
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
		if (cap.equals(WINGS)) {
			return LazyOptional.of(() -> (T)instance);
		} else {
			return null;
		}
	}

	@Override
	public INBT serializeNBT() {
		return WINGS.getStorage().writeNBT(WINGS, instance, null);
	}

	@Override
	public void deserializeNBT(INBT nbt) {
		WINGS.getStorage().readNBT(WINGS, instance, null, nbt);
	}
	
	
	private static final ResourceLocation LOCATION = new ResourceLocation(Wingx.MOD_ID, "wings");
	@SubscribeEvent
	public static void attachCapability(final AttachCapabilitiesEvent<Entity> event) {
		if (event.getObject() instanceof PlayerEntity) {
			event.addCapability(LOCATION, new WingsProvider());
		}
	}
	

}