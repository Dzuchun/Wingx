package dzuchun.wingx.config;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dzuchun.wingx.client.abillity.AbillityNode;
import dzuchun.wingx.client.abillity.AbillityNodes;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;

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

	public final ConfigValue<List<? extends AbillityNode>> ABILLITY_NODES;

	public ServerConfig(final ForgeConfigSpec.Builder builder) {
		this.ABILLITY_NODES = builder.comment(
				"All meditation screen nodes should be listed below in the following format: [<id>, <x position>, <y position>, <sprite number>, <parent id> (-1 for root nodes), <internal node id> (-1 for no such node), <name localization key>, <description localization key>, <unlock conditions> (example: \"node~1\" means unlock condition of node with id 1, \"stat_<stat_type>~<stat_name>~<number>\" means unlock once player has at least <number> of <stat_name> stat, \"data_<type>~<serializer_name>-<data_name>~<value>\" means to unlock once described player's capability reaches <value>.)]")
				.defineList("meditation nodes", AbillityNodes.DEFAULT_NODES, e -> true);
	}
}
