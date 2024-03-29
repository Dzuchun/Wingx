package dzuchun.wingx.capability.entity.wings;

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
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Wingx.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class WingsProvider implements ICapabilitySerializable<INBT> {

	public static void init() {
	}

	private static final Logger LOG = LogManager.getLogger();

	@CapabilityInject(IWingsCapability.class)
	public static final Capability<IWingsCapability> WINGS = null;

	private IWingsCapability instance = WINGS.getDefaultInstance();

	@SuppressWarnings("unchecked")
	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
		if (cap.equals(WINGS)) {
			return LazyOptional.of(() -> (T) this.instance);
		} else {
			return null;
		}
	}

	@Override
	public INBT serializeNBT() {
		return WINGS.getStorage().writeNBT(WINGS, this.instance, null);
	}

	@Override
	public void deserializeNBT(INBT nbt) {
		WINGS.getStorage().readNBT(WINGS, this.instance, null, nbt);
	}

	private static final ResourceLocation LOCATION = new ResourceLocation(Wingx.MOD_ID, "wings");

	@SubscribeEvent
	public static void attachCapability(final AttachCapabilitiesEvent<Entity> event) {
		if ((event.getObject() instanceof PlayerEntity) && !event.getObject().getCapability(WINGS, null).isPresent()) {
			event.addCapability(LOCATION, new WingsProvider());
			// event.addListener(listener); TODO check if I need to invalidate
		}
	}

	@SubscribeEvent
	public static void onPlayerClone(final PlayerEvent.Clone event) {
		if (event.isWasDeath()) {
			IWingsCapability originalCap = event.getOriginal().getCapability(WingsProvider.WINGS).orElse(null);
			if (originalCap != null) {
				// Copying capability to new body
				// TODO react player death
				IWingsCapability copyCap = event.getEntity().getCapability(WingsProvider.WINGS).orElse(null);
				if (copyCap == null) {
					LOG.warn("Copied player has no wings capability, but original did");
					return;
				}
				copyCap.copyFrom(originalCap);
			}
		}
	}

}