package dzuchun.wingx.net;

import java.util.UUID;
import java.util.function.Supplier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dzuchun.wingx.capability.entity.wings.IWingsCapability;
import dzuchun.wingx.capability.entity.wings.WingsProvider;
import dzuchun.wingx.entity.misc.WingsEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;

public class ToggleWingsMessage {

	private static final Logger LOG = LogManager.getLogger();

	private boolean state;

	public ToggleWingsMessage(boolean state) {
		this.state = state;
	}

	public static ToggleWingsMessage decode(PacketBuffer buf) {
		return new ToggleWingsMessage(buf.readBoolean());
	}

	public void encode(PacketBuffer buf) {
		buf.writeBoolean(state);
	}

	private static Entity foundEntity;

	public static void handle(ToggleWingsMessage msg, Supplier<NetworkEvent.Context> ctx) {
		NetworkEvent.Context context = ctx.get();
		if (context.getDirection().equals(NetworkDirection.PLAY_TO_SERVER)) {
			context.enqueueWork(() -> {
				LOG.warn("Handling on server");
				ServerPlayerEntity sender = context.getSender();
				ServerWorld world = (ServerWorld) sender.world;
				sender.getCapability(WingsProvider.WINGS, null).ifPresent((IWingsCapability wingsCap) -> {
					if (wingsCap.isActive()) {
						UUID targetUniqueId = wingsCap.getWingsUniqueId();
						if (targetUniqueId == null) {
							LOG.warn("Wings active for {}, but no UUID specified, deactivating",
									sender.getGameProfile().getName());
							wingsCap.setActive(false);
							return;
						}
						world.getEntities().forEach((Entity entity) -> {
							if (entity.getUniqueID().equals(targetUniqueId)) {
								foundEntity = entity;
							}
						});
						if (foundEntity == null) {
							LOG.warn("{}'s wings UUID specified, but entity not present",
									sender.getGameProfile().getName());
						} else {
							if (foundEntity instanceof WingsEntity) {
								WingsEntity wings = (WingsEntity) foundEntity;
								world.removeEntity(wings);
								WingxPacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> sender),
										new ToggleWingsMessage(false));
								wingsCap.setActive(false);
							} else {
								LOG.warn("Entity with UUID {} should be wings, but it is {}. Deactivating wings.",
										targetUniqueId, foundEntity.getClass().getName());
								wingsCap.setActive(false);
							}
						}
					} else {
						WingsEntity wings = new WingsEntity(world);
						wings.setOwner(sender.getUniqueID(), true);
						wings.setPosition(sender.getPosX(), sender.getPosY(), sender.getPosZ());
						world.summonEntity(wings);
						wingsCap.setWingsUniqueId(wings.getUniqueID());
						if (!wingsCap.isActive()) {
							wingsCap.setActive(true);
						}
						WingxPacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> sender),
								new ToggleWingsMessage(true));
					}
					foundEntity = null;
				});
			});
		} else if (context.getDirection().equals(NetworkDirection.PLAY_TO_CLIENT)) {
			context.enqueueWork(() -> {
				LOG.warn("Handling on client");
				Minecraft minecraft = Minecraft.getInstance();
				if (msg.state) {
					minecraft.player.sendStatusMessage(new TranslationTextComponent("wings.summoned")
							.func_230530_a_(Style.field_240709_b_.func_240712_a_(TextFormatting.AQUA)), true);
				} else {
					minecraft.player.sendStatusMessage(new TranslationTextComponent("wings.desummoned")
							.func_230530_a_(Style.field_240709_b_.func_240712_a_(TextFormatting.DARK_RED)), true);
				}
			});
		}
	}
}
