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
			this.setPosition(owner.lastTickPosX, owner.lastTickPosY, owner.lastTickPosZ);
			ownerUniqueId = null;
			return true;
		}
	}

	@Override
	public IPacket<?> createSpawnPacket() {
		LOG.info("creating network packet");
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
		
		if (owner != null && !world.isRemote) {

//			PlayerEntity owner = world.getPlayerByUuid(this.owner.getUniqueID());
			this.setPosition(owner.getPosX(), owner.getPosY(), owner.getPosZ());
			WingxPacketHandler.INSTANCE.send(
					PacketDistributor.TRACKING_CHUNK.with(() -> world.getChunk(chunkCoordX, chunkCoordZ)),
					new OwnerDataMessage(this));
		}
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

	public void realSetPosAndUpdate(double x, double y, double z, float yaw) {
		realLastXV = x - realLastX;
		realLastYV = y - realLastY;
		realLastZV = z - realLastZ;
		realLastYawV = yaw - realLastYaw;
		realLastX = x;
		realLastY = y;
		realLastZ = z;
		realLastYaw = yaw;
		this.setPositionAndRotationDirect(x, y, z, yaw, 0, 0, false);
		this.setMotion(realLastXV, realLastYV, realLastZV);
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
	
	private static final String OWNER_UUID_TAG = "owner_uuid";
	
	@Override 
	public CompoundNBT writeWithoutTypeId(CompoundNBT compound) {
		//TODO write mine
		if (hasOwner()) {
			compound.putUniqueId(OWNER_UUID_TAG, owner.getUniqueID());
		}
		return super.writeWithoutTypeId(compound);
	}
	
	@Override
	public void read(CompoundNBT compound) {
		//TODO read mine
		if (compound.hasUniqueId(OWNER_UUID_TAG)) {
			setOwner(compound.getUniqueId(OWNER_UUID_TAG), true);
		} else {
			LOG.warn("Wings object with UUID {} has no owner tag", getUniqueID());
		}
		super.read(compound);
	}
}
