package dzuchun.wingx.init;

import java.util.function.Supplier;

import dzuchun.wingx.Wingx;
import dzuchun.wingx.trick.AgilPlayerTrick;
import dzuchun.wingx.trick.DashPlayerTrick;
import dzuchun.wingx.trick.FireballCastPlayerTrick;
import dzuchun.wingx.trick.HastyPlayerTrick;
import dzuchun.wingx.trick.HomingFireballCastTargetedPlayerTrick;
import dzuchun.wingx.trick.ITrick;
import dzuchun.wingx.trick.PunchPlayerTrick;
import dzuchun.wingx.trick.SmashPlayerTrick;
import dzuchun.wingx.trick.SummonSwordPlayerTrick;
import dzuchun.wingx.trick.SwapPlayerTrick;
import dzuchun.wingx.trick.TemplateCastPlayerTrick;
import dzuchun.wingx.trick.meditation.MeditationPlayerTrick;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;

public class Tricks {
	public static final ResourceLocation TRICKS_REGISTRY_NAME = new ResourceLocation(Wingx.MOD_ID, "trick");
	private static IForgeRegistry<ITrick.ITrickType<?>> REGISTRY;

	public static IForgeRegistry<ITrick.ITrickType<?>> getRegistry() {
		return REGISTRY;
	}

	@SuppressWarnings("unchecked")
	private static final DeferredRegister<ITrick.ITrickType<?>> TRICK_TYPE_REGISTER = DeferredRegister
			.create(ITrick.ITrickType.class, Wingx.MOD_ID);

	public static final RegistryObject<DashPlayerTrick.TrickType> DASH_TRICK = register(DashPlayerTrick.TrickType::new,
			"dash_player_trick");
	public static final RegistryObject<SmashPlayerTrick.TrickType> SMASH_TRICK = register(
			SmashPlayerTrick.TrickType::new, "smash_player_trick");
	public static final RegistryObject<PunchPlayerTrick.TrickType> PUNCH_TRICK = register(
			PunchPlayerTrick.TrickType::new, "punch_player_trick");
	public static final RegistryObject<TemplateCastPlayerTrick.TrickType> TEMPLATE_CAST_TRICK = register(
			TemplateCastPlayerTrick.TrickType::new, "template_cast_player_trick");
	public static final RegistryObject<SwapPlayerTrick.TrickType> SWAP_TRICK = register(SwapPlayerTrick.TrickType::new,
			"swap_player_trick");
	public static final RegistryObject<MeditationPlayerTrick.TrickType> MEDITATION_TRICK = register(
			MeditationPlayerTrick.TrickType::new, "meditation_player_trick");
	public static final RegistryObject<FireballCastPlayerTrick.TrickType> FIREBALL_CAST_TRICK = register(
			FireballCastPlayerTrick.TrickType::new, "fireball_cast_player_trick");
	public static final RegistryObject<HastyPlayerTrick.TrickType> HASTY_TRICK = register(
			HastyPlayerTrick.TrickType::new, "hasty_player_trick");
	public static final RegistryObject<AgilPlayerTrick.TrickType> AGIL_TRICK = register(AgilPlayerTrick.TrickType::new,
			"agil_player_trick");
	public static final RegistryObject<HomingFireballCastTargetedPlayerTrick.TrickType> HOMING_FIREBALL_CAST_TRICK = register(
			HomingFireballCastTargetedPlayerTrick.TrickType::new, "homing_firefall_player_trick");
	public static final RegistryObject<SummonSwordPlayerTrick.TrickType> SUMMON_SOULSWORD_TRICK = register(
			SummonSwordPlayerTrick.TrickType::new, "summon_sword_player_trick");

	private static <T extends ITrick.ITrickType<?>> RegistryObject<T> register(Supplier<T> factory, String name) {
		return TRICK_TYPE_REGISTER.register(name, factory);
	}

	public static void registerTricks(final IEventBus bus) {
		TRICK_TYPE_REGISTER.register(bus);
	}

	public static void createTricksRegistry(RegistryEvent.NewRegistry event) {
		RegistryBuilder<ITrick.ITrickType<?>> builder = new RegistryBuilder<ITrick.ITrickType<?>>();
		builder.setName(TRICKS_REGISTRY_NAME);
		@SuppressWarnings("unchecked")
		Class<ITrick.ITrickType<?>> clazz = (Class<ITrick.ITrickType<?>>) (Object) ITrick.ITrickType.class;
		builder.setType(clazz);
		REGISTRY = builder.create();
//		(new RegistryBuilder<AbstractTrick.TrickType<? extends AbstractTrick>>()).setType(AbstractTrick.TrickType.class)
//				.setName(Wingx.TRICKS_REGISTRY_NAME).create();
//		Tricks.registerTricks(FMLJavaModLoadingContext.get().getModEventBus());
	}
}
