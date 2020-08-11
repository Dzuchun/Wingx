package dzuchun.wingx.entity.misc;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dzuchun.wingx.client.render.entity.model.util.AnimationState;
import dzuchun.wingx.init.EntityTypes;
import dzuchun.wingx.net.OwnerDataMessage;
import dzuchun.wingx.net.WingxPacketHandler;
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
	public static final EntityType<WingsEntity> TYPE = EntityTypes.wings_entity_type.get();
	private static final Logger LOG = LogManager.getLogger();
	private PlayerEntity owner;
	private UUID ownerUniqueId;
	public List<AnimationState> upcomingStates;
	public final Object upcomingStates_lock = new Object();

	public WingsEntity(World worldIn) {
		super(TYPE, worldIn);
		LOG.info("Creating wings");
		setInvulnerable(true);
		setNoGravity(true);
		this.upcomingStates = Arrays.asList(null, null, null);
	}

	public boolean setOwner(UUID newOwnerUUID, boolean force) {
		if (force) {
			this.ownerUniqueId = newOwnerUUID;
		}
		PlayerEntity newOwner = this.world.getPlayerByUuid(newOwnerUUID);
		if (newOwner == null) {
			LOG.warn("Can't find owner with UUID {}", newOwnerUUID);
			return false;
		} else {
			if (this.owner != null) {
				if (this.owner.equals(newOwner)) {
					return true;
				}
				LOG.warn("Reassigning {}'s wigns to {}", this.owner.getGameProfile().getName(),
						newOwner.getGameProfile().getName());
				this.owner.stopRiding();
			} else {
				LOG.info("Assigning wigns to {}", newOwner.getGameProfile().getName());
			}
			this.owner = newOwner;
			setPosition(this.owner.getPosX(), this.owner.getPosY(), this.owner.getPosZ());
			this.ownerUniqueId = null;
			return true;
		}
	}

	@Override
	public IPacket<?> createSpawnPacket() {
		LOG.debug("Creating network packet");
		return NetworkHooks.getEntitySpawningPacket(this);
	}

	@Override
	public void tick() {
		if (!this.world.isRemote && this.world.getGameTime() % 40 == 0) {
			if (this.ownerUniqueId != null) {
				if (this.owner == null
						|| (this.owner != null && !this.owner.getUniqueID().equals(this.ownerUniqueId))) {
					LOG.debug("Trying to find my owner");
					if (setOwner(this.ownerUniqueId, true)) {
						LOG.debug("Here he is - {}", this.owner.getGameProfile().getName());
					}
				}
			}
		}

		if (hasOwner() && !this.world.isRemote) {

			setPositionAndUpdate(this.owner.getPosX(), this.owner.getPosY(), this.owner.getPosZ());
			WingxPacketHandler.INSTANCE.send(PacketDistributor.TRACKING_CHUNK
					.with(() -> this.world.getChunk(this.chunkCoordX, this.chunkCoordZ)), new OwnerDataMessage(this));
		}
		super.tick();
	}

	public PlayerEntity getOwner() {
		return this.owner;
	}

	@Override
	public void writeSpawnData(PacketBuffer buffer) {
		LOG.debug("Writing wings additional spawn data");
		if (this.owner == null) {
			LOG.warn("Writing data of the wings with no owner");
			buffer.writeBoolean(false);
		} else {
			buffer.writeBoolean(true);
			buffer.writeUniqueId(this.owner.getUniqueID());
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
				setPosition(this.owner.lastTickPosX, this.owner.lastTickPosY, this.owner.lastTickPosZ);
			}
		}
		setUniqueId(additionalData.readUniqueId());
		LOG.debug("Setting UUID {} to wings", getUniqueID());
		setInvulnerable(true);
		setNoGravity(true);
	}

	@Override
	protected void registerData() {
	}

	@Override
	protected void readAdditional(CompoundNBT compound) {
	}

	@Override
	protected void writeAdditional(CompoundNBT compound) {
	}

	public boolean hasOwner() {
		return this.owner != null; // TODO rewrite
	}

	private double realLastX = 0, realLastY = 0, realLastZ = 0;
	private double realLastXV = 0, realLastYV = 0, realLastZV = 0;
	private float realLastYaw = 0, realLastYawV = 0;

	public void realSetPosAndUpdate() {
		if (this.noTimeX == 0 && this.noTimeY == 0 && this.noTimeZ == 0 && this.noTimeYaw == 0) {
			return;
		}
		if (this.realLastX == 0 && this.realLastY == 0 && this.realLastZ == 0 && this.realLastYaw == 0) {
			this.realLastX = this.noTimeX;
			this.realLastY = this.noTimeY;
			this.realLastZ = this.noTimeZ;
			this.realLastYaw = this.noTimeYaw;
			return;
		}
		this.realLastXV = this.noTimeX - this.realLastX;
		this.realLastYV = this.noTimeY - this.realLastY;
		this.realLastZV = this.noTimeZ - this.realLastZ;
		this.realLastYawV = this.noTimeYaw - this.realLastYaw;
		this.realLastX = this.noTimeX;
		this.realLastY = this.noTimeY;
		this.realLastZ = this.noTimeZ;
		this.realLastYaw = this.noTimeYaw;
		setPosition(this.realLastX, this.realLastY, this.realLastZ);
		setRotation(this.realLastYaw, 0);
		this.setMotion(this.realLastXV, this.realLastYV, this.realLastZV);
	}

	private double noTimeX, noTimeY, noTimeZ;
	private float noTimeYaw;

	public void realSetPosAndUpdateNoTime(double x, double y, double z, float yaw) {
//		LOG.debug("realSettingPosAndUpdating for args: x={}, y={}, z={}, yaw={}", x, y, z, yaw);
		this.noTimeX = x;
		this.noTimeY = y;
		this.noTimeZ = z;
		this.noTimeYaw = yaw;
	}

	public Vector3d getRealMotion() {
		return new Vector3d(this.realLastXV, this.realLastYV, this.realLastZV);
	}

	public float getRealYawSpeed() {
		return this.realLastYawV;
	}

	public Vector3d getRealPos() {
		return new Vector3d(this.realLastX, this.realLastY, this.realLastZ);
	}

	public float getRealYaw() {
		return this.realLastYaw;
	}

	private static final String OWNER_UUID_TAG = "owner_uuid";

	@Override
	public CompoundNBT writeWithoutTypeId(CompoundNBT compound) {
		// TODO write mine
		if (hasOwner()) {
			compound.putUniqueId(OWNER_UUID_TAG, this.owner.getUniqueID());
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
}
