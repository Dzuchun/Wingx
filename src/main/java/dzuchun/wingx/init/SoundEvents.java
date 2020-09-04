package dzuchun.wingx.init;

import dzuchun.wingx.Wingx;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class SoundEvents {
	private static final DeferredRegister<SoundEvent> SOUNDS_REGISTER = DeferredRegister
			.create(ForgeRegistries.SOUND_EVENTS, Wingx.MOD_ID);

	public static final RegistryObject<SoundEvent> AMBIENT_END = register(
			new ResourceLocation(Wingx.MOD_ID, "ambient.wingx.end"));
	public static final RegistryObject<SoundEvent> HASTY_PROC = register(
			new ResourceLocation(Wingx.MOD_ID, "random.hasty_proc"));
	public static final RegistryObject<SoundEvent> AGIL_PROC = register(
			new ResourceLocation(Wingx.MOD_ID, "random.agil_proc"));

	private static RegistryObject<SoundEvent> register(ResourceLocation location) {
		return SOUNDS_REGISTER.register(location.getPath(), () -> new SoundEvent(location));
	}

	public static void registerSoundEvents(IEventBus bus) {
		SOUNDS_REGISTER.register(bus);
	}

}
