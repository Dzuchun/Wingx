package dzuchun.wingx.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraftforge.common.ForgeConfigSpec;

public class ClientConfig {
	private static final Logger LOG = LogManager.getLogger();
	private static ClientConfig instance = null;

	public static ClientConfig get() {
		return instance;
	}

	public static void set(ClientConfig conf) {
		if (instance == null) {
			instance = conf;
		} else {
			LOG.warn("Can't set client config twice!");
		}
	}

	public final ForgeConfigSpec.BooleanValue exampleBoolean;

	public ClientConfig(final ForgeConfigSpec.Builder builder) {
		builder.push("general");
		this.exampleBoolean = builder.comment("An example of boolean in client config")
				.translation("wingx.config.client.example_boolean").define("clientExampleBolean", false);
		builder.pop();
	}
}
