package dzuchun.wingx.damage;

import java.util.Map;

import com.google.common.collect.Maps;

import dzuchun.wingx.Wingx;
import net.minecraft.util.ResourceLocation;

public class WingxDamageType {
	private static final Map<ResourceLocation, WingxDamageType> REGISTRY = Maps.newHashMap();

	public static final WingxDamageType S = registerWingx("s");
	public static final WingxDamageType P = registerWingx("p");
	public static final WingxDamageType D = registerWingx("d");
	public static final WingxDamageType F = registerWingx("f");
	public static final WingxDamageType G = registerWingx("g");
	public static final WingxDamageType H = registerWingx("h");

	private static WingxDamageType registerWingx(String name) {
		ResourceLocation loc = new ResourceLocation(Wingx.MOD_ID, name);
		return register(loc, new WingxDamageType(loc));
	}

	public static WingxDamageType register(ResourceLocation name, WingxDamageType type) {
		REGISTRY.putIfAbsent(name, type);
		return type;
	}

	public static WingxDamageType getForName(ResourceLocation name) {
		return REGISTRY.get(name);
	}

	public final ResourceLocation name;

	public WingxDamageType(ResourceLocation nameIn) {
		this.name = nameIn;
	}

	@Override
	public String toString() {
		return String.format("WingxDamageType ", this.name.toString());
	}
}
