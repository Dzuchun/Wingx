package dzuchun.wingx.init;

import dzuchun.wingx.Wingx;
import dzuchun.wingx.trick.AbstractTrick;
import dzuchun.wingx.trick.AgilPlayerTrick;
import dzuchun.wingx.trick.DashPlayerTrick;
import dzuchun.wingx.trick.FireballCastPlayerTrick;
import dzuchun.wingx.trick.HastyPlayerTrick;
import dzuchun.wingx.trick.HomingFireballCastTargetedPlayerTrick;
import dzuchun.wingx.trick.PunchPlayerTrick;
import dzuchun.wingx.trick.SmashPlayerTrick;
import dzuchun.wingx.trick.SummonSwordPlayerTrick;
import dzuchun.wingx.trick.SwapPlayerTrick;
import dzuchun.wingx.trick.TemplateCastPlayerTrick;
import dzuchun.wingx.trick.meditation.MeditationPlayerTrick;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;

public class Tricks {

	private static final DeferredRegister<AbstractTrick> TRICKS_REGISTER = DeferredRegister.create(AbstractTrick.class,
			Wingx.MOD_ID);

	public static final RegistryObject<AbstractTrick> DASH_TRICK = register(DashPlayerTrick.class);
	public static final RegistryObject<AbstractTrick> SMASH_TRICK = register(SmashPlayerTrick.class);
	public static final RegistryObject<AbstractTrick> PUNCH_TRICK = register(PunchPlayerTrick.class);
	public static final RegistryObject<AbstractTrick> TEMPLATE_CAST_TRICK = register(TemplateCastPlayerTrick.class);
	public static final RegistryObject<AbstractTrick> SWAP_TRICK = register(SwapPlayerTrick.class);
	public static final RegistryObject<AbstractTrick> MEDITATION_TRICK = register(MeditationPlayerTrick.class);
	public static final RegistryObject<AbstractTrick> FIREBALL_CAST_TRICK = register(FireballCastPlayerTrick.class);
	public static final RegistryObject<AbstractTrick> HASTY_TRICK = register(HastyPlayerTrick.class);
	public static final RegistryObject<AbstractTrick> AGIL_TRICK = register(AgilPlayerTrick.class);
	public static final RegistryObject<AbstractTrick> HOMING_FIREBALL_CAST_TRICK = register(
			HomingFireballCastTargetedPlayerTrick.class);
	public static final RegistryObject<AbstractTrick> SUMMON_SOULSWORD_TRICK = register(SummonSwordPlayerTrick.class);

	private static RegistryObject<AbstractTrick> register(Class<? extends AbstractTrick> trickClass) {
		AbstractTrick trick;
		try {
			trick = trickClass.newInstance();
			return TRICKS_REGISTER.register(trick.getRegistryName().getPath(), () -> (AbstractTrick) trick.newEmpty());
		} catch (InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void registerTricks(final IEventBus bus) {
		TRICKS_REGISTER.register(bus);
	}
}
