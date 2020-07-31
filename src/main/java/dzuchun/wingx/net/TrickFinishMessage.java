package dzuchun.wingx.net;

import java.util.function.Supplier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dzuchun.wingx.trick.AbstractTrick;
import dzuchun.wingx.trick.ICastedTrick;
import dzuchun.wingx.trick.IInterruptableTrick;
import dzuchun.wingx.trick.ITargetedTrick;
import dzuchun.wingx.trick.ITrick;
import dzuchun.wingx.util.NetworkHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.registries.RegistryManager;

public class TrickFinishMessage {

	@SuppressWarnings("unused")
	private static final Logger LOG = LogManager.getLogger();

	public IInterruptableTrick trick;

	public TrickFinishMessage(IInterruptableTrick trick) {
		this.trick = trick;
	}

	public static TrickFinishMessage decode(PacketBuffer buf) {
		return new TrickFinishMessage((IInterruptableTrick) NetworkHelper
				.readRegisteredTrick(RegistryManager.ACTIVE.getRegistry(AbstractTrick.class), buf));
	}

	public void encode(PacketBuffer buf) {
		NetworkHelper.writeRegisteredTrick(buf, this.trick);
	}

	@SuppressWarnings("resource")
	public static void handle(TrickFinishMessage msg, Supplier<NetworkEvent.Context> ctx) {
		NetworkEvent.Context context = ctx.get();
		context.enqueueWork(() -> {
			ITrick trick = msg.trick;
			ClientWorld world = Minecraft.getInstance().world;
			if (trick instanceof ICastedTrick) {
				((ICastedTrick) trick).setWorld(world);
			}
			if (trick instanceof ITargetedTrick) {
				((ITargetedTrick) trick).setTargetWorld(world);
			}
			msg.trick.onCastEnd(LogicalSide.CLIENT);
		});
	}
}
