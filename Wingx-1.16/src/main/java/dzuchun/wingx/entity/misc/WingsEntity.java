package dzuchun.wingx.entity.misc;

import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dzuchun.wingx.init.EntityTypes;
import dzuchun.wingx.net.OwnerDataMessage;
import dzuchun.wingx.net.WingxPacketHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.fml.network.PacketDistributor;

public class WingsEntity extends Entity implements IEntityAdditionalSpawnData {
	public static final EntityType<WingsEntity> TYPE = EntityTypes.WINGS_ENTITY_TYPE.get();
	private static final Logger LOG = LogManager.getLogger();
	private PlayerEntity owner;
	private UUID ownerUniqueId;

	public WingsEntity(World worldIn) {
		super(TYPE, worldIn);
		LOG.info("Creating wings");
		this.setInvulnerable(true);
		this.setNoGravity(true);
	}

	public boolean setOwner(UUID newOwnerUUID, boolean force) {
		if (force) {
			this.ownerUniqueId = newOwnerUUID;
		}
		PlayerEntity newOwner = world.getPlayerByUuid(newOwnerUUID);
		if (newOwner == null) {
			LOG.warn("Can't find owner with UUID {}", newOwnerUUID);
			return false;
		} else {
			if (owner != null) {
				if (owner.equals(newOwner)) {
					return true;
				}
				LOG.warn("Reassigning {}'s wigns to {}", owner.getGameProfile().getName(),
						newOwner.getGameProfile().getName());
				owner.stopRiding();
			} else {
				LOG.info("Assigning wigns to {}", newOwner.getGameProfile().getName());
			}
			owner = newOwner;
			this.setPosition(owner.getPosX(), owner.getPosY(), owner.getPosZ());
			ownerUniqueId = null;
			return true;
		}
	}

	@Override
	public IPacket<?> createSpawnPacket() {
		LOG.info("Creating network packet");
		return NetworkHooks.getEntitySpawningPacket(this);
	}

	@Override
	public void tick() {
//		LOG.info("Tick!");
		if (!world.isRemote && world.getGameTime() % 40 == 0) {
			if (ownerUniqueId != null) {
				if (owner == null || (owner != null && !owner.getUniqueID().equals(ownerUniqueId))) {
					LOG.info("Trying to find my owner");
					if (setOwner(ownerUniqueId, true)) {
						LOG.info("Here he is - {}", owner.getGameProfile().getName());
					}
				}
			}
		}

		if (hasOwner() && !world.isRemote) {

//			PlayerEntity owner = world.getPlayerByUuid(this.owner.getUniqueID());
			this.setPositionAndUpdate(owner.getPosX(), owner.getPosY(), owner.getPosZ());
//			LOG.info("Setting wings position to {}", getPositionVec());
			WingxPacketHandler.INSTANCE.send(
					PacketDistributor.TRACKING_CHUNK.with(() -> world.getChunk(chunkCoordX, chunkCoordZ)),
					new OwnerDataMessage(this));
		}
		super.tick();
	}

	public PlayerEntity getOwner() {
		return this.owner;
	}

	@Override
	public void writeSpawnData(PacketBuffer buffer) {
		LOG.info("Writing wings additional spawn data");
		if (this.owner == null) {
			LOG.warn("Writing data of the wings with no owner");
			buffer.writeBoolean(false);
		} else {
			buffer.writeBoolean(true);
			buffer.writeUniqueId(owner.getUniqueID());
		}
		buffer.writeUniqueId(getUniqueID());
	}

	@Override
	public void readSpawnData(PacketBuffer additionalData) {
		LOG.info("Reading wings additional data");
		Boolean hasOwner = additionalData.readBoolean();
		if (!hasOwner) {
			LOG.warn("Readed data of the wings with no owner, leaving owner unchanged");
//			this.owner = null;
		} else {
			if (setOwner(additionalData.readUniqueId(), true)) {
				this.setPosition(owner.lastTickPosX, owner.lastTickPosY, owner.lastTickPosZ);
			}
		}
		this.setUniqueId(additionalData.readUniqueId());
		LOG.info("Setting UUID {} to wings", getUniqueID());
		this.setInvulnerable(true);
		this.setNoGravity(true);
	}

	@Override
	protected void registerData() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void readAdditional(CompoundNBT compound) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void writeAdditional(CompoundNBT compound) {
		// TODO Auto-generated method stub

	}

	public boolean hasOwner() {
		return this.owner != null; // TODO rewrite
	}

	private double realLastX = 0, realLastY = 0, realLastZ = 0;
	private double realLastXV = 0, realLastYV = 0, realLastZV = 0;
	private float realLastYaw = 0, realLastYawV = 0;

	public void realSetPosAndUpdate() {
		if (noTimeX == 0 && noTimeY == 0 && noTimeZ == 0 && noTimeYaw == 0) {
			return;
		}
		if (realLastX == 0 && realLastY == 0 && realLastZ == 0 && realLastYaw == 0) {
			realLastX = noTimeX;
			realLastY = noTimeY;
			realLastZ = noTimeZ;
			realLastYaw = noTimeYaw;
			return;
		}
		realLastXV = noTimeX - realLastX;
		realLastYV = noTimeY - realLastY;
		realLastZV = noTimeZ - realLastZ;
		realLastYawV = noTimeYaw - realLastYaw;
		realLastX = noTimeX;
		realLastY = noTimeY;
		realLastZ = noTimeZ;
		realLastYaw = noTimeYaw;
		this.setPosition(realLastX, realLastY, realLastZ);
		this.setRotation(realLastYaw, 0);
		this.setMotion(realLastXV, realLastYV, realLastZV);
	}

	private double noTimeX, noTimeY, noTimeZ;
	private float noTimeYaw;

	public void realSetPosAndUpdateNoTime(double x, double y, double z, float yaw) {
		LOG.debug("realSettingPosAndUpdating for args: x={}, y={}, z={}, yaw={}", x, y, z, yaw);
		noTimeX = x;
		noTimeY = y;
		noTimeZ = z;
		noTimeYaw = yaw;
	}

	public Vector3d getRealMotion() {
		return new Vector3d(realLastXV, realLastYV, realLastZV);
	}

	public float getRealYawSpeed() {
		return realLastYawV;
	}

	public Vector3d getRealPos() {
		return new Vector3d(realLastX, realLastY, realLastZ);
	}
	
	public float getRealYaw() {
		return this.realLastYaw;
	}

	private static final String OWNER_UUID_TAG = "owner_uuid";

	@Override
	public CompoundNBT writeWithoutTypeId(CompoundNBT compound) {
		// TODO write mine
		if (hasOwner()) {
			compound.putUniqueId(OWNER_UUID_TAG, owner.getUniqueID());
		}
		return super.writeWithoutTypeId(compound);
	}

	@Override
	public void read(CompoundNBT compound) {
		// TODO read mine
		if (compound.hasUniqueId(OWNER_UUID_TAG)) {
			setOwner(compound.getUniqueId(OWNER_UUID_TAG), true);
		} else {
			LOG.warn("Wings object with UUID {} has no owner tag", getUniqueID());
		}
		super.read(compound);
	}

//	@Override
//	public void remove(boolean b) {
//		LOG.info("Removed from:\n{}", new Object() {
//			@Override
//			public String toString() {
//				String res = "";
//				for (StackTraceElement e : Thread.currentThread().getStackTrace()) {
//					res += e.toString() + "\n";
//				}
//				return res;
//			}
//		});
//		super.remove(b);
//	}
//
//	@Override
//	public void setPosition(double x, double y, double z) {
//		LOG.info("Setting entity position to ({}, {}, {}) from\n{}", x, y, z, new Object() {
//			@Override
//			public String toString() {
//				String res = "";
//				for (StackTraceElement e : Thread.currentThread().getStackTrace()) {
//					res += e.toString() + "\n";
//				}
//				return res;
//			}
//		});
//		super.setPosition(x, y, z);
//	}
}
