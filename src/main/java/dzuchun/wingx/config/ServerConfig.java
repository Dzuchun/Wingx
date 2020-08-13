package dzuchun.wingx.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraftforge.common.ForgeConfigSpec;

public class ServerConfig {
	private static final Logger LOG = LogManager.getLogger();
	private static ServerConfig instance = null;

	public static ServerConfig get() {
		return instance;
	}

	public static void set(ServerConfig conf) {
		if (instance == null) {
			instance = conf;
		} else {
			LOG.warn("Can't set server config twice!");
		}
	}

	public final ForgeConfigSpec.BooleanValue exampleBoolean;

	public ServerConfig(final ForgeConfigSpec.Builder builder) {
		builder.push("general");
		exampleBoolean = builder.comment("An example of boolean in server config")
				.translation("wingx.config.server.example_boolean").define("serverExampleBolean", false);
		builder.pop();
	}
}
