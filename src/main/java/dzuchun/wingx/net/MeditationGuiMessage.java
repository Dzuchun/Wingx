package dzuchun.wingx.net;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dzuchun.wingx.capability.entity.wings.IWingsCapability;
import dzuchun.wingx.capability.entity.wings.WingsCapability;
import dzuchun.wingx.client.gui.MeditationScreen;
import dzuchun.wingx.util.NetworkHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.network.NetworkEvent;

public class MeditationGuiMessage {
	private static final ITextComponent MEDITATION_SCREEN_TITLE = new TranslationTextComponent(
			"wingx.screen.meditation");

	private static final Logger LOG = LogManager.getLogger();

	IWingsCapability capability;
	Map<String, Integer> stats;
	Map<String, Object> data;

	public MeditationGuiMessage(@Nonnull IWingsCapability capabilityIn, Map<String, Integer> statsIn,
			Map<String, Object> dataIn) {
		this.capability = capabilityIn;
		this.stats = statsIn;
		this.data = dataIn;
	}

	public static MeditationGuiMessage decode(PacketBuffer buf) {
		IWingsCapability capability = new WingsCapability();
		capability.readFromBuffer(buf);
		// Reading stats
		Map<String, Integer> stats = new LinkedHashMap<String, Integer>(0);
		int size = buf.readInt();
		for (int i = 0; i < size; i++) {
			stats.put(NetworkHelper.readString(buf), buf.readInt());
		}
		LOG.debug("Readed {} stat entries", size);
		// Reading data
		Map<String, Object> data = new LinkedHashMap<String, Object>(0);
		size = buf.readInt();
		for (int i = 0; i < size; i++) {
			String key = buf.readString();
			Object dataValue;
			switch (buf.readString()) {
			case "D":
				dataValue = buf.readDouble();
				break;
			case "I":
				dataValue = buf.readInt();
				break;
			case "B":
				dataValue = buf.readBoolean();
				break;
			case "U":
			default:
				LOG.warn("Unknown type written to buf. Likely the game will crash.");
				dataValue = null;
			}
			data.put(key, dataValue);
		}
		LOG.debug("Readed {} data entries", size);
		return new MeditationGuiMessage(capability, stats, data);
	}

	public void encode(PacketBuffer buf) {
		this.capability.writeToBuffer(buf);
		buf.writeInt(this.stats.size());
		for (Entry<String, Integer> e : this.stats.entrySet()) {
			NetworkHelper.writeString(buf, e.getKey());
			buf.writeInt(e.getValue());
		}
		buf.writeInt(this.data.size());
		for (Entry<String, Object> e : this.data.entrySet()) {
			buf.writeString(e.getKey());
			Object dataValue = e.getValue();
			if (dataValue instanceof Double) {
				buf.writeString("D");
				buf.writeDouble((double) dataValue);
			} else if (dataValue instanceof Integer) {
				buf.writeString("I");
				buf.writeInt((int) dataValue);
			} else if (dataValue instanceof Boolean) {
				buf.writeString("B");
				buf.writeBoolean((boolean) dataValue);
			} else {
				LOG.warn("Datavalue {} has unknown type {}, skipping", dataValue, dataValue.getClass().getName());
				buf.writeString("U");
			}
		}
	}

	public static void handle(MeditationGuiMessage msg, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			Minecraft.getInstance().displayGuiScreen(
					new MeditationScreen(MEDITATION_SCREEN_TITLE, msg.capability, msg.stats, msg.data));
		});
		ctx.get().setPacketHandled(true);
	}
}
