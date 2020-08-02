package dzuchun.wingx;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dzuchun.wingx.trick.AbstractTrick;
import dzuchun.wingx.trick.DashPlayerTrick;
import dzuchun.wingx.trick.PunchPlayerTrick;
import dzuchun.wingx.trick.SmashPlayerTrick;
import dzuchun.wingx.trick.SwapPlayerTrick;
import dzuchun.wingx.trick.TemplateCastPlayerTrick;
import dzuchun.wingx.trick.meditation.MeditationPlayerTrick;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.TextureStitchEvent;
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
		event.getRegistry().registerAll(new DashPlayerTrick(), new SmashPlayerTrick(), new PunchPlayerTrick(),
				new TemplateCastPlayerTrick(), new SwapPlayerTrick(), new MeditationPlayerTrick());
	}

	// Texture locations
//	@OnlyIn(value = Dist.CLIENT)
//	public static final ResourceLocation GUI_INGAME_COOLDOWN_HORIZONTAL_TEXTURE = new ResourceLocation(Wingx.MOD_ID,
//			"gui/ingame/cooldown_bar_horizontal");

	private static final ResourceLocation AMBIENT_END_LOCATION = new ResourceLocation(Wingx.MOD_ID,
			"ambient.wingx.end");

	@SubscribeEvent
	public static void registerSounds(final RegistryEvent.Register<SoundEvent> event) {
		event.getRegistry().registerAll(new SoundEvent(AMBIENT_END_LOCATION).setRegistryName(AMBIENT_END_LOCATION));
	}

	@OnlyIn(value = Dist.CLIENT)
	@SubscribeEvent
	public static void onTextureStitchPre(TextureStitchEvent.Pre event) {
		if (event.getMap().getTextureLocation().equals(PlayerContainer.LOCATION_BLOCKS_TEXTURE)) {
			LOG.info("Adding block textures");
			// TODO add block textures
//			event.addSprite(GUI_INGAME_COOLDOWN_HORIZONTAL_TEXTURE);
			LOG.info("Added block textures");
		}
	}
}
