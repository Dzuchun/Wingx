package dzuchun.wingx.net;

import java.util.function.Supplier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dzuchun.wingx.trick.AbstractTrick;
import dzuchun.wingx.trick.IPersisableTrick;
import dzuchun.wingx.util.NetworkHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.registries.RegistryManager;

public class TrickFinishMessage {

	@SuppressWarnings("unused")
	private static final Logger LOG = LogManager.getLogger();

	private IPersisableTrick trick;

	public TrickFinishMessage(IPersisableTrick trick) {
		this.trick = trick;
	}

	public static TrickFinishMessage decode(PacketBuffer buf) {
		return new TrickFinishMessage((IPersisableTrick) NetworkHelper
				.readRegisteredTrick(RegistryManager.ACTIVE.getRegistry(AbstractTrick.class), buf));
	}

	public void encode(PacketBuffer buf) {
		NetworkHelper.writeRegisteredTrick(buf, trick);
	}

	@SuppressWarnings("resource")
	public static void handle(TrickFinishMessage msg, Supplier<NetworkEvent.Context> ctx) {
		NetworkEvent.Context context = ctx.get();
		context.enqueueWork(() -> {
			msg.trick.stopExecute(LogicalSide.CLIENT, Minecraft.getInstance().world);
		});
	}
}
