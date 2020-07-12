package dzuchun.wingx.entity.misc;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dzuchun.wingx.init.EntityTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

public class WingsEntity extends Entity {
	public static final EntityType<WingsEntity> TYPE = EntityTypes.WINGS_ENTITY_TYPE.get();
	private static final Logger LOG = LogManager.getLogger();
	private PlayerEntity owner;

	public WingsEntity(World worldIn) {
		super(TYPE, worldIn);
		LOG.info("Creating wings");
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
		this.owner = this.world.getPlayerByUuid(compound.getUniqueId(OWNER_UUID_KEY));
		if (owner == null) {
			LOG.warn("Owner is null, do something about it!");
		} else {
			LOG.info("Succesfully readed wings owner, which is {}", owner.getGameProfile().getName());
		}
	}

	@Override
	protected void writeAdditional(CompoundNBT compound) {
		compound.putUniqueId(OWNER_UUID_KEY, owner.getUniqueID());
	}

	public void moveToOwner() {
		this.setPositionAndUpdate(owner.getPosX(), owner.getPosY(), owner.getPosZ());
		this.setMotion(owner.getMotion());
	}

	public PlayerEntity getOwner() {
		return this.owner;
	}
}
