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
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class OwnerDataMessage {

	private static final Logger LOG = LogManager.getLogger();
	private boolean hasOwner;
	private double x, y, z;
	private float yaw;
	private UUID uuid;
	private UUID ownerUniqueId;

	public OwnerDataMessage(WingsEntity entityIn) {
		if (entityIn.hasOwner()) {
			this.hasOwner = true;
			PlayerEntity owner = entityIn.getOwner();
			this.x = owner.lastTickPosX;
			this.y = owner.lastTickPosY;
			this.z = owner.lastTickPosZ;
			this.yaw = owner.rotationYaw;
			this.ownerUniqueId = owner.getUniqueID();
		} else {
			this.hasOwner = false;
		}
		this.uuid = entityIn.getUniqueID();
	}

	private OwnerDataMessage(boolean hasOwner, UUID ownerUniqueId, double x, double y, double z, float yaw, UUID uuid) {
		this.hasOwner = hasOwner;
		this.x = x;
		this.y = y;
		this.z = z;
		this.yaw = yaw;
		this.uuid = uuid;
		this.ownerUniqueId = ownerUniqueId;
	}

	private OwnerDataMessage(UUID uuid) {
		this.hasOwner = false;
		this.uuid = uuid;
	}

	public static OwnerDataMessage decode(PacketBuffer buf) {
		boolean hasOwner = buf.readBoolean();
		if (hasOwner) {
			return new OwnerDataMessage(true, buf.readUniqueId(), buf.readDouble(), buf.readDouble(), buf.readDouble(),
					buf.readFloat(), buf.readUniqueId());
		} else {
			return new OwnerDataMessage(buf.readUniqueId());
		}
	}

	public void encode(PacketBuffer buf) {
		if (this.hasOwner) {
			buf.writeBoolean(true);
			buf.writeUniqueId(this.ownerUniqueId);
			buf.writeDouble(this.x);
			buf.writeDouble(this.y);
			buf.writeDouble(this.z);
			buf.writeFloat(this.yaw);
		} else {
			buf.writeBoolean(false);
		}
		buf.writeUniqueId(this.uuid);
	}

	private static Entity entity;

	@SuppressWarnings("resource")
	public static void handle(OwnerDataMessage msg, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			Minecraft.getInstance().world.getAllEntities().forEach((Entity entityI) -> {
				if (entityI.getUniqueID().equals(msg.uuid)) {
					entity = entityI;
				} else {
					if (entityI instanceof WingsEntity) {
						LOG.warn("Hit wings with UUID {}, but we need {}", entityI.getUniqueID(), msg.uuid);
					}
				}
			});
			if (entity == null) {
				LOG.debug("No entity found with UUID {}, saaad", msg.uuid);
				return;
			}
			if (entity instanceof WingsEntity) {
				WingsEntity wings = (WingsEntity) entity;
				if (msg.hasOwner) {
					// TODO OPTIMIZE!!
					LOG.info("Setting position at client");
					wings.realSetPosAndUpdateNoTime(msg.x, msg.y, msg.z, msg.yaw);
					wings.setOwner(msg.ownerUniqueId, true);
					wings.getOwner().getCapability(WingsProvider.WINGS, null).ifPresent((IWingsCapability wingsCap) -> {
						BasicData data = wingsCap.getDataManager().getOrAddDefault(Serializers.BASIC_SERIALIZER);
						if (!data.wingsActive) {
							data.wingsActive = true;
						}
						data.wingsUniqueId = wings.getUniqueID();
					});
				}
			} else {
				LOG.error("UUID is not unique");
			}
			entity = null;
		});
		ctx.get().setPacketHandled(true);
	}
}
