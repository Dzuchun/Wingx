package dzuchun.wingx;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraftforge.fml.common.Mod;

@Mod(value = Wingx.MOD_ID)
public class Wingx {
	public static final String MOD_ID = "wingx";
	
	private static final Logger LOG = LogManager.getLogger();
	
	public Wingx() {
		LOG.info("Hello from wingx mod!!!");
	}
}
