package dzuchun.wingx.net;

import java.util.UUID;
import java.util.function.Supplier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dzuchun.wingx.capability.entity.wings.IWingsCapability;
import dzuchun.wingx.capability.entity.wings.WingsProvider;
import dzuchun.wingx.capability.entity.wings.storage.BasicData;
import dzuchun.wingx.capability.entity.wings.storage.Serializers;
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
		buf.writeBoolean(this.state);
	}

	private static Entity foundEntity;

	public static synchronized void handle(ToggleWingsMessage msg, Supplier<NetworkEvent.Context> ctx) {
		NetworkEvent.Context context = ctx.get();
		if (context.getDirection().equals(NetworkDirection.PLAY_TO_SERVER)) {
			context.enqueueWork(() -> {
				LOG.warn("Handling on server");
				ServerPlayerEntity sender = context.getSender();
				ServerWorld world = (ServerWorld) sender.world;
				sender.getCapability(WingsProvider.WINGS, null).ifPresent((IWingsCapability wingsCap) -> {
					BasicData data = wingsCap.getDataManager().getOrAddDefault(Serializers.BASIC_SERIALIZER);
					if (data.wingsActive) {
						UUID targetUniqueId = data.wingsUniqueId;
						if (targetUniqueId == null) {
							LOG.warn("Wings active for {}, but no UUID specified, deactivating",
									sender.getGameProfile().getName());
							data.wingsActive = false;
							return;
						}
						foundEntity = null;
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
								data.wingsActive = false;
							} else {
								LOG.warn("Entity with UUID {} should be wings, but it is {}. Deactivating wings.",
										targetUniqueId, foundEntity.getClass().getName());
								data.wingsActive = false;
							}
						}
					} else {
						WingsEntity wings = new WingsEntity(world);
						wings.setOwner(sender.getUniqueID(), true);
						wings.setPosition(sender.getPosX(), sender.getPosY(), sender.getPosZ());
						world.summonEntity(wings);
						data.wingsUniqueId = wings.getUniqueID();
						if (!data.wingsActive) {
							data.wingsActive = true;
						}
						WingxPacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> sender),
								new ToggleWingsMessage(true));
					}
				});
			});
		} else if (context.getDirection().equals(NetworkDirection.PLAY_TO_CLIENT)) {
			context.enqueueWork(() -> {
				LOG.warn("Handling on client");
				Minecraft minecraft = Minecraft.getInstance();
				if (msg.state) {
					minecraft.player.sendStatusMessage(new TranslationTextComponent("wings.summoned")
							.setStyle(Style.EMPTY.setFormatting(TextFormatting.AQUA)), true);
				} else {
					minecraft.player.sendStatusMessage(new TranslationTextComponent("wings.desummoned")
							.setStyle(Style.EMPTY.setFormatting(TextFormatting.DARK_RED)), true);
				}
				// TODO update upcoming states!!
			});
		}
	}
}
