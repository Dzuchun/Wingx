package dzuchun.wingx;

import org.apache.commons.lang3.tuple.Pair;
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
import dzuchun.wingx.config.ClientConfig;
import dzuchun.wingx.config.ServerConfig;
import dzuchun.wingx.config.abillity.AbillityNodes;
import dzuchun.wingx.init.EntityTypes;
import dzuchun.wingx.init.Items;
import dzuchun.wingx.init.SoundEvents;
import dzuchun.wingx.init.Tricks;
import dzuchun.wingx.item.Soulsword;
import dzuchun.wingx.net.WingxPacketHandler;
import dzuchun.wingx.trick.state.TrickStates;
import dzuchun.wingx.util.animation.FadeFunction;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.item.ItemModelsProperties;
import net.minecraft.stats.StatType;
import net.minecraft.stats.Stats;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.ParallelDispatchEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;

@Mod(value = Wingx.MOD_ID)
@Mod.EventBusSubscriber(bus = Bus.MOD, modid = Wingx.MOD_ID)
public class Wingx {

	private static final Logger LOG = LogManager.getLogger();

	public static final String MOD_ID = "wingx";

	private static IEventBus MOD_EVENT_BUS;
	private static IEventBus FORGE_EVENT_BUS;

	public Wingx() {
		LOG.debug("A wild Wingx appeared!"); // Credits to brandon3055 {@link
												// https://www.curseforge.com/members/brandon3055/} for this idea
		MOD_EVENT_BUS = FMLJavaModLoadingContext.get().getModEventBus();
		FORGE_EVENT_BUS = MinecraftForge.EVENT_BUS;

		MOD_EVENT_BUS.addListener(Wingx::createRegistries);
		MOD_EVENT_BUS.addListener(Wingx::registerModelOverrides);

		LOG.debug("Initing");
		Items.registerItems(MOD_EVENT_BUS);
		EntityTypes.registerEntityTypes(MOD_EVENT_BUS);
		Tricks.registerTricks(MOD_EVENT_BUS);
		SoundEvents.registerSoundEvents(MOD_EVENT_BUS);
		LOG.debug("Inited");
	}

	@SubscribeEvent
	public static void commonSetup(final FMLCommonSetupEvent event) {
		LOG.info("Performing common setup");

		LOG.info("Initing listeners");
		ForgeBusEventListener.init();
//		ModBusEventListener.init();
		LOG.debug("Inited listeners");

		LOG.info("Registering net channels");
		WingxPacketHandler.init();
		LOG.debug("Registered net channels");

		LOG.info("Initing capabilities");
		CapabilityWings.register();
		WingsProvider.init();
		CapabilityActiveTricks.register();
		ActiveTricksProvider.init();
		Serializers.init();
		LOG.debug("Inited capabilities");

		LOG.info("Initing trick states");
		TrickStates.init();
		LOG.info("Inited trick states");

		LOG.info("Initing animation stuff");
		FadeFunction.init();
		LOG.debug("Inited animation stuff");

		// TODO check if it really should be here:
		LOG.info("Registering server config");
		Pair<ServerConfig, ForgeConfigSpec> serverConfigPair = new ForgeConfigSpec.Builder()
				.configure(ServerConfig::new);
		ModLoadingContext.get().registerConfig(Type.SERVER, serverConfigPair.getRight());
		ServerConfig.set(serverConfigPair.getLeft());
		LOG.debug("Registered server config");

		LOG.debug("Registering stat getters");
		AbillityNodes.addStatGetter(Stats.CUSTOM, StatType::get);
		AbillityNodes.addStatGetter(Stats.BLOCK_MINED,
				(type, resource) -> type.get(ForgeRegistries.BLOCKS.getValue(resource)));
		LOG.debug("Registered stat getters");

		LOG.debug("Finished common setup");
	}

	@SubscribeEvent
	public static void clientSetup(final FMLClientSetupEvent event) {
		LOG.info("Performing client setup");

		FORGE_EVENT_BUS.addListener(Wingx::onTextureStitchPre);

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

		LOG.info("Registering client config");
		Pair<ClientConfig, ForgeConfigSpec> clientConfigPair = new ForgeConfigSpec.Builder()
				.configure(ClientConfig::new);
		ModLoadingContext.get().registerConfig(Type.CLIENT, clientConfigPair.getRight());
		ClientConfig.set(clientConfigPair.getLeft());
		LOG.debug("Registered client config");

		LOG.debug("Finished client setup");
	}

	public static void createRegistries(RegistryEvent.NewRegistry event) {
		LOG.debug("Creating tricks registry");
		Tricks.createTricksRegistry(event);
	}

	public static void onTextureStitchPre(TextureStitchEvent.Pre event) {
		if (event.getMap().getTextureLocation().equals(PlayerContainer.LOCATION_BLOCKS_TEXTURE)) {
			LOG.info("Adding block textures");
			// TODO add block textures
//			event.addSprite(GUI_INGAME_COOLDOWN_HORIZONTAL_TEXTURE);
			LOG.info("Added block textures");
		}
	}

	public static void registerModelOverrides(ParallelDispatchEvent event) {
		event.enqueueWork(() -> {
			ItemModelsProperties.registerProperty(Items.SOULSWORD.get(), Soulsword.ANIMATION_PROPERTY_LOCATION,
					Soulsword.ANIMATION_PROPERTY_GETTER);
			ItemModelsProperties.registerProperty(Items.SOULSWORD.get(), Soulsword.SUMMONED_PROPERTY_LOCATION,
					Soulsword.SUMMONED_PROPERTY_GETTER);
		});
	}
}
