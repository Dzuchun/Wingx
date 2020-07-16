package dzuchun.wingx;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@Mod.EventBusSubscriber(bus = Bus.MOD, modid = Wingx.MOD_ID)
public class ModBusEventListener {
	@SuppressWarnings("unused")
	private static final Logger LOG = LogManager.getLogger();
	public static void init() {}
}
