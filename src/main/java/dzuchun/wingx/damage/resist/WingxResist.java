package dzuchun.wingx.damage.resist;

import java.util.Map;
import java.util.function.Function;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.Maps;

import dzuchun.wingx.Wingx;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;

public abstract class WingxResist {
	private static final Logger LOG = LogManager.getLogger();

	public WingxResist(WingxResist.RessistType<?> typeIn) {
		this.type = typeIn;
	}

	private final RessistType<?> type;

	public abstract double getPassed(double rawDamage);

	public RessistType<?> getType() {
		return this.type;
	}

	@Override
	public String toString() {
		return String.format("WingxResist \"%s\"", this.getType().name.toString());
	}

	public static class RessistType<T extends WingxResist> {

		public static void init() {
		}

		private static final Map<ResourceLocation, WingxResist.RessistType<?>> REGISTRY = Maps.newConcurrentMap();

		public static <T extends WingxResist> WingxResist.RessistType<T> register(
				WingxResist.RessistType<T> resistType) {
			REGISTRY.putIfAbsent(resistType.name, resistType);
			return resistType;
		}

		public static WingxResist.RessistType<?> getForName(ResourceLocation name) {
			return REGISTRY.get(name);
		}

		private static final String NAME_TAG = "name";
		private static final String RESIST_TAG = "resist";

		public static <T extends WingxResist> T read(CompoundNBT nbt) {
			ResourceLocation loc = new ResourceLocation(nbt.getString(NAME_TAG));
			@SuppressWarnings("unchecked")
			RessistType<T> resistType = (RessistType<T>) getForName(loc);
			return resistType.readNBT.apply(nbt.getCompound(RESIST_TAG));
		}

		public final ResourceLocation name;
		protected final Function<T, CompoundNBT> writeNBT;
		protected final Function<CompoundNBT, T> readNBT;

		RessistType(String nameIn, Function<T, CompoundNBT> writeNBT, Function<CompoundNBT, T> readNBT) {
			this(new ResourceLocation(Wingx.MOD_ID, nameIn), writeNBT, readNBT);
		}

		public RessistType(ResourceLocation nameIn, Function<T, CompoundNBT> writeNBTIn,
				Function<CompoundNBT, T> readNBTIn) {
			this.name = nameIn;
			this.writeNBT = writeNBTIn;
			this.readNBT = readNBTIn;
		}

		@SuppressWarnings("unchecked")
		public CompoundNBT writeChecked(WingxResist resist) {
			try {
				return this.write((T) resist);
			} catch (ClassCastException e) {
				LOG.warn("Attempted to write inappropriate resist: {} in {} type", resist, this);
				return new CompoundNBT();
			}
		}

		public CompoundNBT write(T resist) {
			CompoundNBT res = new CompoundNBT();
			res.putString(NAME_TAG, this.name.toString());
			res.put(RESIST_TAG, this.writeNBT.apply(resist));
			return res;
		}

	}
}
