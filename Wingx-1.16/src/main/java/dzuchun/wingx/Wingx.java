package dzuchun.wingx;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dzuchun.wingx.capability.wings.CapabilityWings;
import dzuchun.wingx.capability.wings.WingsProvider;
import dzuchun.wingx.client.input.KeyEvents;
import dzuchun.wingx.client.render.entity.WingsRenderer;
import dzuchun.wingx.init.EntityTypes;
import dzuchun.wingx.net.WingxPacketHandler;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(value = Wingx.MOD_ID)
@Mod.EventBusSubscriber(bus = Bus.MOD, modid = Wingx.MOD_ID)
public class Wingx {
	public static final String MOD_ID = "wingx";

	private static final Logger LOG = LogManager.getLogger();

	public Wingx() {
		LOG.info("Wingx awakened!");

		LOG.info("Initing");
		final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		EntityTypes.init(modEventBus);
		LOG.info("Inited");
	}

	@SubscribeEvent
	public static void commonSetup(final FMLCommonSetupEvent event) {
		LOG.info("Performing common setup");

		LOG.info("Registering net channels");
		WingxPacketHandler.init();
		LOG.info("Registered net channels");
		
		LOG.info("Registering capabilities");
		CapabilityWings.register();
		WingsProvider.init();
		LOG.info("Registered capabilities");
	}

	@SubscribeEvent
	public static void clientSetup(final FMLClientSetupEvent event) {
		LOG.info("Performing client setup");

		LOG.info("Binding renderers");
		RenderingRegistry.registerEntityRenderingHandler(EntityTypes.WINGS_ENTITY_TYPE.get(), WingsRenderer::new);
		LOG.info("Binded renderers");

		LOG.info("Registering key bindings");
		KeyEvents.init();
		LOG.info("Registered key bindings");
	}
}
