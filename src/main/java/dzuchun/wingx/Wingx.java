package dzuchun.wingx;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dzuchun.wingx.capability.entity.wings.CapabilityWings;
import dzuchun.wingx.capability.entity.wings.WingsProvider;
import dzuchun.wingx.capability.entity.wings.storage.Serializers;
import dzuchun.wingx.capability.world.tricks.ActiveTricksProvider;
import dzuchun.wingx.capability.world.tricks.CapabilityActiveTricks;
import dzuchun.wingx.client.input.KeyEvents;
import dzuchun.wingx.client.render.entity.FireballRenderer;
import dzuchun.wingx.client.render.entity.WingsRenderer;
import dzuchun.wingx.client.render.overlay.AbstractOverlay;
import dzuchun.wingx.init.EntityTypes;
import dzuchun.wingx.net.WingxPacketHandler;
import net.minecraft.util.ResourceLocation;
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
	public static final ResourceLocation TRICKS_REGISTRY_NAME = new ResourceLocation(MOD_ID, "trick");

	private static final Logger LOG = LogManager.getLogger();

	public Wingx() {
		LOG.debug("Wingx awakened!");

		LOG.debug("Initing");
		final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		EntityTypes.registerEntityTypes(modEventBus);
		LOG.debug("Inited");
	}

	@SubscribeEvent
	public static void commonSetup(final FMLCommonSetupEvent event) {
		LOG.info("Performing common setup");

		LOG.debug("Initing listeners");
		ForgeBusEventListener.init();
		ModBusEventListener.init();
		LOG.debug("Inited listeners");

		LOG.debug("Registering net channels");
		WingxPacketHandler.init();
		LOG.debug("Registered net channels");

		LOG.debug("Registering capabilities");
		CapabilityWings.register();
		WingsProvider.init();
		CapabilityActiveTricks.register();
		ActiveTricksProvider.init();
		Serializers.init();
		LOG.debug("Registered capabilities");
	}

	@SubscribeEvent
	public static void clientSetup(final FMLClientSetupEvent event) {
		LOG.info("Performing client setup");

		LOG.debug("Binding renderers");
		RenderingRegistry.registerEntityRenderingHandler(EntityTypes.wings_entity_type.get(), WingsRenderer::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityTypes.fireball_entity_type.get(), FireballRenderer::new);
		LOG.debug("Binded renderers");

		LOG.debug("Registering key bindings");
		KeyEvents.init();
		LOG.debug("Registered key bindings");

		LOG.debug("Initing overlays");
		AbstractOverlay.init();
		LOG.debug("Inited overlays");
	}
}
