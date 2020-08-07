package dzuchun.wingx.capability.entity.wings.storage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.netty.util.internal.ConcurrentSet;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;

public class WingsDataManager {
	private static final Logger LOG = LogManager.getLogger();

	private static final LinkedHashMap<String, Serializer<?>> registry = new LinkedHashMap<String, Serializer<?>>(0);

	private final Object DATA_LOCK = new Object();
	private final Set<SerializedData> data = new ConcurrentSet<SerializedData>();

	public static Serializer<?> register(Serializer<?> serializer) {
		return registry.putIfAbsent(serializer.getName(), serializer);
	}

	public static Collection<Serializer<?>> getRegisteredSerializers() {
		return new ArrayList<Serializer<?>>(registry.values());
	}

	public void read(CompoundNBT nbt) {
		LOG.debug("Reading data from nbt {}", nbt);
		nbt.keySet().forEach(name -> {
			CompoundNBT dataNBT = nbt.getCompound(name);
			Serializer<?> serializer = registry.get(name);
			synchronized (this.DATA_LOCK) {
				if (!this.data.add(read(serializer, dataNBT))) {
					LOG.warn("\"{}\" part was not added, it already present", name);
				}
			}
		});
	}

	private <T extends SerializedData> T read(Serializer<T> serializer, CompoundNBT nbt) {
		return serializer.read(nbt);
	}

	public void write(CompoundNBT nbt) {
		synchronized (this.DATA_LOCK) {
			this.data.forEach(dataInstance -> {
				CompoundNBT res = new CompoundNBT();
				write(res, dataInstance);
				nbt.put(dataInstance.getSerializer().getName(), res);
			});
		}
		LOG.debug("Writen data to nbt {}", nbt);
	}

	private <T extends SerializedData> void write(CompoundNBT nbt, T data) {
		data.getSerializer().write(nbt, data);
	}

	public void read(PacketBuffer buf) {
		int size = buf.readInt();
		synchronized (this.DATA_LOCK) {
			for (int i = 0; i < size; i++) {
				int length = buf.readInt();
				String name = buf.readString(length);
				Serializer<?> serializer = registry.get(name);
				this.data.add(read(serializer, buf));
			}
		}
	}

	private <T extends SerializedData> T read(Serializer<T> serializer, PacketBuffer buf) {
		return serializer.read(buf);
	}

	public void write(PacketBuffer buf) {
		buf.writeInt(this.data.size());
		synchronized (this.DATA_LOCK) {
			this.data.forEach(dataInstance -> {
				String name = dataInstance.getSerializer().getName();
				buf.writeInt(name.length());
				buf.writeString(name, name.length());
				write(buf, dataInstance);
			});
		}
	}

	private <T extends SerializedData> void write(PacketBuffer buf, T data) {
		data.getSerializer().write(buf, data);
	}

	private SerializedData res;

	@SuppressWarnings("unchecked")
	public synchronized <T extends SerializedData> T getOrAddDefault(Serializer<T> serializer) {
		this.res = null;
		synchronized (this.DATA_LOCK) {
			this.data.forEach(dataInstance -> {
				if (dataInstance.getSerializer().equals(serializer)) {
					this.res = dataInstance;
				}
			});
			if (this.res == null) {
				LOG.debug("Creating default data for \"{}\"", serializer.getName());
				T defaultData = serializer.getDefault();
				this.data.add(defaultData);
				this.res = defaultData;
			}
		}
		return (T) this.res;
	}

	public synchronized <T extends SerializedData> void replace(T initial, T replace) {
		synchronized (this.DATA_LOCK) {
			if (this.data.remove(initial)) {
				this.data.add(replace);
			}
		}
	}

}