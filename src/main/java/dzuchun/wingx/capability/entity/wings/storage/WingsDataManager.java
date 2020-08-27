package dzuchun.wingx.capability.entity.wings.storage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import dzuchun.wingx.capability.entity.wings.IWingsCapability;
import dzuchun.wingx.capability.entity.wings.WingsProvider;
import dzuchun.wingx.command.impl.WingxComand;
import dzuchun.wingx.util.Util;
import io.netty.util.internal.ConcurrentSet;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntitySelector;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.util.LazyOptional;

public class WingsDataManager {
	private static final Logger LOG = LogManager.getLogger();

	private static final LinkedHashMap<String, Serializer<?>> registry = new LinkedHashMap<String, Serializer<?>>(0);

	private final Object DATA_LOCK = new Object();
	private final Set<SerializedData> data = new ConcurrentSet<SerializedData>();

	@SuppressWarnings("unchecked")
	public static <T extends SerializedData> Serializer<T> register(Serializer<T> serializer) {
		return (Serializer<T>) registry.putIfAbsent(serializer.getName(), serializer);
	}

	public static Collection<Serializer<?>> getRegisteredSerializers() {
		return new ArrayList<Serializer<?>>(registry.values());
	}

	private static final String MODIFY_COMMAND_LITERAL = "modify";

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static ArgumentBuilder getModifyCommand() {
		ArgumentBuilder res = Commands.literal(MODIFY_COMMAND_LITERAL);
		for (Serializer<?> ser : registry.values()) {
			res.then(ser.getDefault().getArgumentBuilder());
		}
		return res;
	}

	private static final String RESET_COMMAND_LITERAL = "reset";

	public static LiteralArgumentBuilder<CommandSource> getResetCommand() {
		LiteralArgumentBuilder<CommandSource> res = Commands.literal(RESET_COMMAND_LITERAL);
		res.executes(source -> {
			try {
				List<? extends Entity> targetList;
				try {
					targetList = source.getArgument(WingxComand.TARGET_PAR_NAME, EntitySelector.class)
							.select(source.getSource());
				} catch (IllegalArgumentException e) {
					targetList = Arrays.asList(source.getSource().asPlayer());
				}
				for (Entity target : targetList) {
					LazyOptional<IWingsCapability> cap = target.getCapability(WingsProvider.WINGS, null);
					if (cap.isPresent()) {
						IWingsCapability wings = cap.orElse(null);
						WingsDataManager manager = wings.getDataManager();
						for (Serializer<?> serializer : registry.values()) {
							manager.replace(manager.getOrAddDefault(serializer), serializer.getDefault());
						}
						source.getSource()
								.sendFeedback(new TranslationTextComponent("wingx.command.success.reset",
										target instanceof ServerPlayerEntity
												? ((ServerPlayerEntity) target).getGameProfile().getName()
												: new TranslationTextComponent("wingx.command.non_player_target_cap"),
										Util.iterableToString(Util.computeNewArrayList(wings.getDataManager().data,
												data -> data.getSerializer().getName())))
														.setStyle(WingxComand.SUCCEESS_STYLE),
										true);
					} else {
						source.getSource().sendErrorMessage(
								new TranslationTextComponent("wingx.command.error.reset.target_no_wings")
										.setStyle(WingxComand.ERROR_STYLE));
					}
				}
				return 0;
			} catch (CommandSyntaxException e) {
				e.printStackTrace();
				return 1;
			}
		});
		return res;
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
//		LOG.debug("Current data {}", Util.iterableToString(data));
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