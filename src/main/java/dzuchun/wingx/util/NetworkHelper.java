package dzuchun.wingx.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

import javax.annotation.Nullable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dzuchun.wingx.trick.AbstractTrick;
import dzuchun.wingx.trick.ITrick;
import dzuchun.wingx.util.animation.AnimationState;
import dzuchun.wingx.util.animation.FadeFunction;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;

public class NetworkHelper {
	private static final Logger LOG = LogManager.getLogger();

	public static ITrick readRegisteredTrick(IForgeRegistry<ITrick.ITrickType<?>> registry, PacketBuffer buf) {
		if (buf.readBoolean()) {
			if (registry != null) {
				String registryName = NetworkHelper.readString(buf);
				ITrick.ITrickType<?> trickType = registry.getValue(new ResourceLocation(registryName));
				if (trickType != null) {
					LOG.debug("While decoding found trick type of {} class", trickType.getClass().getName());
					return trickType.readFromBuf(buf);
				} else {
					LOG.warn(
							"While decoding found no registered trick type with registry name {}, setting trick to null",
							registryName);
					return null;
				}

			} else {
				LOG.warn("No registry found for {}, setting trick to null", AbstractTrick.TrickType.class.getName());
				return null;
			}
		} else {
			LOG.warn("Recieved empty message, setting trick to null");
			return null;
		}
	}

	public static <T extends ITrick, U extends ITrick.ITrickType<T>> void writeRegisteredTrick(PacketBuffer buf,
			T trick) {
		if (trick != null) {
			U type = trick.getType();
			ResourceLocation loc = type.getRegistryName();
			if (loc != null) {
				buf.writeBoolean(true);
				String trickRegistryName = loc.toString();
				NetworkHelper.writeString(buf, trickRegistryName);
				type.writeToBuf(trick, buf);
			} else {
				LOG.warn("No registry name found for {} class. Message will be empty", trick.getClass().getName());
				buf.writeBoolean(false);
			}
		} else {
			LOG.warn("Trick is null, so buffer will be empty");
			buf.writeBoolean(false);
		}
	}

	public static void writeString(PacketBuffer buf, String string) {
		int length = string.length();
		buf.writeInt(length);
		buf.writeString(string, length);
	}

	public static String readString(PacketBuffer buf) {
		int length = buf.readInt();
		return buf.readString(length);
	}

	public static void writeAnimationState(PacketBuffer buf, AnimationState state) {
//		LOG.debug("Writing {}", state);
		buf.writeLong(state.time);
		writeString(buf, state.fadeFunction.getName().toString());
		buf.writeInt(state.priority);
		writeChecked(buf, state.x, PacketBuffer::writeFloat);
		writeChecked(buf, state.y, PacketBuffer::writeFloat);
		writeChecked(buf, state.z, PacketBuffer::writeFloat);
		writeChecked(buf, state.xRot, PacketBuffer::writeFloat);
		writeChecked(buf, state.yRot, PacketBuffer::writeFloat);
		writeChecked(buf, state.zRot, PacketBuffer::writeFloat);
	}

	public static AnimationState readAnimationState(PacketBuffer buf) {
		AnimationState res = new AnimationState(buf.readLong(),
				FadeFunction.getByName(new ResourceLocation(readString(buf))), buf.readInt(),
				readChecked(buf, PacketBuffer::readFloat), readChecked(buf, PacketBuffer::readFloat),
				readChecked(buf, PacketBuffer::readFloat), readChecked(buf, PacketBuffer::readFloat),
				readChecked(buf, PacketBuffer::readFloat), readChecked(buf, PacketBuffer::readFloat));
//		LOG.debug("Readed {}", res);
		return res;
	}

	public static <T> void writeArray(PacketBuffer buf, List<T> arrayIn, BiConsumer<PacketBuffer, T> writingFunction) {
		int length = arrayIn.size();
		buf.writeInt(length);
		for (int i = 0; i < length; i++) {
			writingFunction.accept(buf, arrayIn.get(i));
		}
	}

	public static <T> List<T> readArray(PacketBuffer buf, Function<PacketBuffer, T> readFunction) {
		int length = buf.readInt();
		List<T> res = new ArrayList<T>(0);
		for (int i = 0; i < length; i++) {
			res.add(readFunction.apply(buf));
		}
		return res;
	}

	public static <T> void writeChecked(PacketBuffer buf, @Nullable T objectIn, BiConsumer<PacketBuffer, T> writer) {
		if (objectIn == null) {
			buf.writeBoolean(false);
		} else {
			buf.writeBoolean(true);
			writer.accept(buf, objectIn);
		}
	}

	public static <T> T readChecked(PacketBuffer buf, Function<PacketBuffer, T> reader) {
		return buf.readBoolean() ? reader.apply(buf) : null;
	}
}
