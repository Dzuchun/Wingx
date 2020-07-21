package dzuchun.wingx;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dzuchun.wingx.trick.AbstractTrick;
import dzuchun.wingx.trick.DashPlayerTrick;
import dzuchun.wingx.trick.PunchPlayerTrick;
import dzuchun.wingx.trick.SmashPlayerTrick;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.registries.RegistryBuilder;

@Mod.EventBusSubscriber(bus = Bus.MOD, modid = Wingx.MOD_ID)
public class ModBusEventListener {
	private static final Logger LOG = LogManager.getLogger();

	public static void init() {
	}

	@SubscribeEvent
	public static void createResistries(RegistryEvent.NewRegistry event) {
		LOG.debug("Creating tricks registry");
		(new RegistryBuilder<AbstractTrick>()).setType(AbstractTrick.class).setName(Wingx.TRICKS_REGISTRY_NAME)
				.create();
//		Tricks.registerTricks(FMLJavaModLoadingContext.get().getModEventBus());
	}

	@SubscribeEvent
	public static void registerTricks(final RegistryEvent.Register<AbstractTrick> event) {
		event.getRegistry().register(new DashPlayerTrick());
		event.getRegistry().register(new SmashPlayerTrick());
		event.getRegistry().register(new PunchPlayerTrick());
	}
}
