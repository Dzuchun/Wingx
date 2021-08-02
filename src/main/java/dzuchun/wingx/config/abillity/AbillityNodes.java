package dzuchun.wingx.config.abillity;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dzuchun.wingx.capability.entity.wings.storage.SerializedData;
import dzuchun.wingx.config.ServerConfig;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;

public class AbillityNodes {
	private static final Logger LOG = LogManager.getLogger();
	public static ArrayList<AbillityNode> nodes;
	public static Map<String, Stat<?>> requiredStats;
	public static Map<String, Class<?>> requiredData;

	public static void loadAbillityNodes() {
		LOG.debug("Loading abillity nodes");
		List<? extends List<Object>> loadedNodes = ServerConfig.get().ABILLITY_NODES.get();
		int maxId = (int) loadedNodes.stream().max((n1, n2) -> (int) n1.get(0) - (int) n2.get(0)).get().get(0);
		LOG.debug("Max id found: {}", maxId);
		// TODO can'tjust initialize with capacity maxId, for some reason shrinks to 4
		nodes = new ArrayList<AbillityNode>(0);
		for (int i = 0; i <= maxId; i++) {
			nodes.add(null);
		}
		for (List<Object> nodeData : loadedNodes) {
			AbillityNode node = new AbillityNode(nodeData);
			nodes.set(node.getId(), node);
		}
		requiredStats = new LinkedHashMap<String, Stat<?>>(0);
		requiredData = new LinkedHashMap<String, Class<?>>(0);
		for (AbillityNode node : nodes) {
			if (node.hasParent()) {
				nodes.get(node.getParentId()).children.add(node);
				node.parent = nodes.get(node.getParentId());
			}
			if (node.hasInternal()) {
				node.internal = nodes.get(node.getInternalId());
			}
			// Adding required stats and data
			for (String condition : node.getUnlockCondition().split(" ")) {
				String[] words = condition.split("~");
				String[] type = words[0].split("_");
				if (type[0].equals("stat")) {
					StatType<?> statType = ForgeRegistries.STAT_TYPES.getValue(new ResourceLocation(type[1]));
					ResourceLocation requiredStat = new ResourceLocation(words[1]);
					if (!STAT_GETTERS.containsKey(statType)) {
						LOG.warn("Stat type {} has no registered stat getter. See AbillityNodes::addStatGetter",
								type[1]);
						continue;
					}
					final Stat<?> stat = STAT_GETTERS.get(statType).apply(statType, requiredStat);
					requiredStats.computeIfAbsent(
							String.format("%s~%s", statType.getRegistryName().toString(), requiredStat.toString()),
							str -> stat);
				} else if (type[0].equals("data")) {
					Class<?> clazz;
					switch (type[1]) {
					case "boolean":
						clazz = Boolean.class;
						break;
					case "double":
						clazz = Double.class;
					case "integer":
					case "integer-bit":
						clazz = Integer.class;
						break;
					default:
						LOG.warn(
								"Type name {} must be \"boolean\", \"double\" or \"integer\" - referring to a corresponding types.",
								type[1]);
						continue;
					}
					requiredData.put(words[1], clazz);
				}
			}
		}
		LOG.debug("Got following required stats: {}", requiredStats);
	}

	private static final Map<StatType<?>, BiFunction<StatType<?>, ResourceLocation, Stat<?>>> STAT_GETTERS = new LinkedHashMap<StatType<?>, BiFunction<StatType<?>, ResourceLocation, Stat<?>>>(
			0);

	@SuppressWarnings("unchecked")
	public static <T> void addStatGetter(StatType<T> type, BiFunction<StatType<T>, ResourceLocation, Stat<T>> getter) {
		STAT_GETTERS.computeIfAbsent(type, t -> (tp, r) -> getter.apply((StatType<T>) tp, r));
	}

	public static void markNodesDirty() {
		for (AbillityNode node : nodes) {
			node.unlockedDirty = true;
		}
		LOG.debug("Marcked abillity nodes dirty");
	}

	public static ArrayList<AbillityNode> DEFAULT_NODES = new ArrayList<AbillityNode>(0) {
		/**
		 *
		 */
		private static final long serialVersionUID = 1L;

		{
			this.add(createForName("wings", 0, 0, 0, 1, -1, -1, ""));
			this.add(createForName("fireball", 1, 0, 0, 2, -1, -1, "node~4"));// internal
			this.add(createForName("fireball_distance", 2, 50, 10, 3, 1, -1,
					"node~1 stat_minecraft:custom~minecraft:jump~230"));
			this.add(createForName("fireball_homing", 3, -50, 10, 6, 1, -1,
					"node~1 data_integer-bit_0000000000000001~basic-stage_flags"));
			this.add(createForName("fireball_ionization_1", 8, -10, -40, 8, 1, -1,
					"node~1 data_double~fireball-damage~10"));
			this.add(createForName("fireball", 4, -40, -10, 2, 0, 1, "node~0"));
			this.add(createForName("hasty", 5, 35, 0, 4, 0, -1, "node~0"));
			this.add(createForName("agil", 6, 0, 60, 5, 0, -1, "node~0"));
			this.add(createForName("soulsword", 7, 50, 70, 7, 6, -1, "node~6"));
			// Last id: 8, last sprite: 9 (0 - node background)
		}
	};

	private static AbillityNode createForName(String name, int id, int x, int y, int spriteNo, int parent, int internal,
			String condition) {
		return new AbillityNode(id, x, y, spriteNo, parent, internal, String.format("wingx.gui.node.%s.name", name),
				String.format("wingx.gui.node.%s.desc", name), condition);
	}

	@OnlyIn(Dist.CLIENT)
	public static <T extends SerializedData> boolean evaluateCondition(String condition, Map<String, Integer> stats,
			Map<String, Object> data) {
		String[] parts = condition.split("~");
		String[] type = parts[0].split("_");
		switch (type[0]) {
		case "stat":
			ResourceLocation statTypeLoc = new ResourceLocation(type[1]);
			ResourceLocation loc = new ResourceLocation(parts[1]);
			Integer containedInt = stats.get(String.format("%s~%s", statTypeLoc.toString(), loc.toString()));
			LOG.debug("Contained value for stattype {} (name {}) is {}", statTypeLoc, loc, containedInt);
			return containedInt >= Integer.parseInt(parts[2]);
		case "data":
			Object containedValue = data.get(parts[1]);
			LOG.debug("Contained value for data {} is {}", parts[1], containedValue);
			if (containedValue instanceof Integer) {
				if (type[1].equals("integer-bit")) {
					int mask = Integer.parseInt(type[2], 2);
					return ((int) containedValue & mask) != 0;
				}
				return (int) containedValue >= Integer.parseInt(parts[2]);
			} else if (containedValue instanceof Double) {
				return (double) containedValue >= Double.parseDouble(parts[2]);
			} else if (containedValue instanceof Boolean) {
				return (boolean) containedValue == Boolean.parseBoolean(parts[2]);
			}
		case "node":
			AbillityNode node = nodes.get(Integer.parseInt(parts[1]));
			return node.isUnlocked();
		default:
			LOG.warn("Unlock condition \"{}\" appears to be incorrect.", condition);
			return false;
		}
	}

}
