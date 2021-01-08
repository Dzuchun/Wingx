package dzuchun.wingx.net;

import java.util.function.Supplier;

import javax.annotation.Nonnull;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dzuchun.wingx.capability.entity.wings.IWingsCapability;
import dzuchun.wingx.capability.entity.wings.WingsCapability;
import dzuchun.wingx.capability.entity.wings.storage.BasicData;
import dzuchun.wingx.capability.entity.wings.storage.Serializers;
import dzuchun.wingx.client.gui.MeditationScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.network.NetworkEvent;

public class MeditationGuiMessage {
	private static final ITextComponent MEDITATION_SCREEN_TITLE = new TranslationTextComponent(
			"wingx.screen.meditation");

	@SuppressWarnings("unused")
	private static final Logger LOG = LogManager.getLogger();

	IWingsCapability capability;

	public MeditationGuiMessage(@Nonnull IWingsCapability capabilityIn) {
		this.capability = capabilityIn;
	}

	public static MeditationGuiMessage decode(PacketBuffer buf) {
		IWingsCapability capability = new WingsCapability();
		capability.readFromBuffer(buf);
		return new MeditationGuiMessage(capability);
	}

	public void encode(PacketBuffer buf) {
		this.capability.writeToBuffer(buf);
	}

	@SuppressWarnings("resource")
	public static void handle(MeditationGuiMessage msg, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			BasicData basicData = msg.capability.getDataManager().getOrAddDefault(Serializers.BASIC_SERIALIZER);
			if (!basicData.getStageFlags(BasicData.MEDITATED_IN_END_MASK)) {
				basicData.setStageFlags(BasicData.MEDITATED_IN_END_MASK, true);
				// TODO user's first meditation
				LOG.warn("{}'s first  meditation! [Insert greet message here]",
						Minecraft.getInstance().player.getGameProfile().getName());
			}
			Minecraft.getInstance().displayGuiScreen(new MeditationScreen(MEDITATION_SCREEN_TITLE, msg.capability));
		});
		ctx.get().setPacketHandled(true);
	}
}
