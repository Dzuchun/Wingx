package dzuchun.wingx.entity.misc;

import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dzuchun.wingx.init.EntityTypes;
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
		} else {
			LOG.info("Assigning wigns to {}", newOwner.getGameProfile().getName());
		}

		owner = newOwner;
		this.moveToOwner();
	}

	@Override
	protected void registerData() {

	}

	@Override
	public IPacket<?> createSpawnPacket() {
		LOG.info("creating network packet");
		return NetworkHooks.getEntitySpawningPacket(this);
	}

	@Override
	public void tick() {
		super.tick();
		if (owner != null) {
			this.moveToOwner();
			if (this.getEntityWorld().getGameTime() % 40 == 0) {
				LOG.info("Ticked for {}'s wings, current position - ({},  {}, {})", owner.getGameProfile().getName(),
						this.getPosX(), this.getPosY(), this.getPosZ());
			}
		}
	}

	private final static String OWNER_UUID_KEY = "owner_uuid";

	@Override
	protected void readAdditional(CompoundNBT compound) {
	}

	@Override
	protected void writeAdditional(CompoundNBT compound) {
	}

	public void moveToOwner() {
		this.setRawPosition(owner.getPosX(), owner.getPosY(), owner.getPosZ());
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
			this.owner = null;
		} else {
			UUID uuid;
			PlayerEntity owner = ((ClientWorld) this.world).getPlayerByUuid(uuid = additionalData.readUniqueId());
			if (owner == null) {
				LOG.warn("No player with such UUID in the world now: {}, leaving owner unchanged", uuid.toString());
			} else {
				this.owner = owner;
				LOG.info("Wings owner set to {}", this.owner.getGameProfile().getName());
			}
		}
	}
//	
//	@Override
//	public Vector3d getPositionVec() {
//		return (owner != null) ? owner.getPositionVec() : super.getPositionVec();
//	}
}
