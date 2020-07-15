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

	public WingsEntity(World worldIn) {
		super(TYPE, worldIn);
		LOG.info("Creating wings");
		this.setInvulnerable(true);
		this.setNoGravity(true);
	}

	public void setOwner(PlayerEntity newOwner) {
		if (newOwner == null) {
			LOG.warn("Owner can't be null!");
			return;
		}
		if (owner != null) {
			LOG.warn("Reassigning {}'s wigns to {}", owner.getGameProfile().getName(),
					newOwner.getGameProfile().getName());
			owner.stopRiding();
		} else {
			LOG.info("Assigning wigns to {}", newOwner.getGameProfile().getName());
		}
		owner = newOwner;
		this.setPosition(owner.lastTickPosX, owner.lastTickPosY, owner.lastTickPosZ);
	}

	@Override
	public IPacket<?> createSpawnPacket() {
		LOG.info("creating network packet");
		return NetworkHooks.getEntitySpawningPacket(this);
	}

	@Override
	public void tick() {
//		LOG.info("Tick!");
		if (owner != null && !world.isRemote) {
			
//			PlayerEntity owner = world.getPlayerByUuid(this.owner.getUniqueID());
			this.setPosition(owner.getPosX(), owner.getPosY(), owner.getPosZ());
			WingxPacketHandler.INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(()-> world.getChunk(chunkCoordX, chunkCoordZ)), new OwnerDataMessage(this));
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
			UUID uuid;
			PlayerEntity owner = ((ClientWorld) this.world).getPlayerByUuid(uuid = additionalData.readUniqueId());
			if (owner == null) {
				LOG.warn("No player with such UUID in the world now: {}, leaving owner unchanged", uuid.toString());
			} else {
				this.owner = owner;
				LOG.info("Wings owner set to {}", this.owner.getGameProfile().getName());
			}
			this.setPosition(owner.lastTickPosX, owner.lastTickPosY, owner.lastTickPosZ);
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
		return this.owner != null; //TODO rewrite
	}
	
	private double realLastX=0, realLastY=0, realLastZ=0;
	private double realLastXV=0, realLastYV=0, realLastZV=0;
	private float realLastYaw=0, realLastYawV=0;
	public void realSetPosAndUpdate(double x, double y, double z, float yaw) {
		realLastXV = x-realLastX;
		realLastYV = y-realLastY;
		realLastZV = z-realLastZ;
		realLastYawV = yaw-realLastYaw;
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
}
