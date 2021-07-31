package dzuchun.wingx.entity.projectile;

import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dzuchun.wingx.capability.entity.wings.IWingsCapability;
import dzuchun.wingx.capability.entity.wings.WingsProvider;
import dzuchun.wingx.capability.entity.wings.storage.FireballData;
import dzuchun.wingx.capability.entity.wings.storage.Serializers;
import dzuchun.wingx.init.EntityTypes;
import dzuchun.wingx.trick.NoWingsException;
import dzuchun.wingx.util.WorldHelper;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.Pose;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.network.NetworkHooks;

//TODO fix no damage bug
public class FireballEntity extends Entity implements IEntityAdditionalSpawnData {
	private static final Logger LOG = LogManager.getLogger();
	private static final EntityType<FireballEntity> TYPE = EntityTypes.fireball_entity_type.get();

	private UUID ownerUniqueId;
	private double initialSpeed;
	public boolean isDebug = true;
	public int packedColor;
	public float mainDamage;

	/**
	 * Used to create fireball that was casted, on server
	 */
	public FireballEntity(PlayerEntity caster) throws NoWingsException {
		this(caster.world);
		this.isDebug = false;
		IWingsCapability cap = caster.getCapability(WingsProvider.WINGS, null)
				.orElseThrow(() -> new NoWingsException(caster));
		this.ownerUniqueId = caster.getUniqueID();
		FireballData data = cap.getDataManager().getOrAddDefault(Serializers.FIREBALL_SERIALIZER);
		this.initialSpeed = data.initialSpeed;
		this.packedColor = data.packedColor;
		this.mainDamage = data.damage;
		this.setMotion(caster.getMotion()
				.add(Vector3d.fromPitchYaw(caster.getPitchYaw()).normalize().scale(this.initialSpeed)));
		this.setPositionAndRotation(caster.getPosX(), (caster.getPosY() + caster.getEyeHeight()) - 0.2d,
				caster.getPosZ(), caster.rotationYawHead, caster.rotationPitch);
		this.recalculateSize();
	}

	public FireballEntity(World worldIn) {
		this(TYPE, worldIn);
	}

	public FireballEntity(EntityType<? extends FireballEntity> type, World worldIn) {
		super(type, worldIn);
		LOG.debug("Creating new fireball");
	}

	@Override
	protected void registerData() {
	}

	private static final String OWNER_TAG = "wingx_fireball_owner";
	private static final String INITIAL_SPEED_TAG = "wingx_initial_speed";
	private static final String COLOR_TAG = "wingx_color";
	private static final String MAIN_DAMAGE_TAG = "wingx_main_damage";

	@Override
	protected void readAdditional(CompoundNBT compound) {
		if (compound.contains(OWNER_TAG)) {
			this.ownerUniqueId = compound.getUniqueId(OWNER_TAG);
		} else {
			LOG.debug("{} tag not found for {}", OWNER_TAG, this);
		}
		this.isDebug = this.ownerUniqueId == null;
		if (compound.contains(INITIAL_SPEED_TAG)) {
			this.initialSpeed = compound.getDouble(INITIAL_SPEED_TAG);
		} else {
			LOG.debug("{} tag not found for {}", INITIAL_SPEED_TAG, this);
		}
		if (compound.contains(COLOR_TAG)) {
			this.packedColor = compound.getInt(COLOR_TAG);
		} else {
			LOG.debug("{} tag not found for {}", COLOR_TAG, this);
		}
		if (compound.contains(MAIN_DAMAGE_TAG)) {
			this.mainDamage = compound.getFloat(MAIN_DAMAGE_TAG);
		} else {
			LOG.debug("{} tag not found for {}", MAIN_DAMAGE_TAG, this);
		}
	}

	@Override
	protected void writeAdditional(CompoundNBT compound) {
		if (this.ownerUniqueId != null) {
			compound.putUniqueId(OWNER_TAG, this.ownerUniqueId);
		}
		compound.putDouble(INITIAL_SPEED_TAG, this.initialSpeed);
		compound.putInt(COLOR_TAG, this.packedColor);
		compound.putFloat(MAIN_DAMAGE_TAG, this.mainDamage);
	}

	@Override
	public IPacket<?> createSpawnPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}

	@Override
	public void tick() {
		super.tick();
		this.collideEntities();
		if (!this.isDebug && (this.ticksExisted > (20 + 10))) {
			this.remove(false);
			return;
		}
//		if (this.ticksExisted > 10) {
//			this.setMotion(getMotion().scale(0.8));
//		}
		this.applyGravity();
		this.turnToMovingDirection();
		this.move(MoverType.SELF, this.getMotion());
	}

	private void collideEntities() {
//		if (this.world.isRemote) {
//			return;
//		}
		this.world.getEntitiesInAABBexcluding(this, this.getBoundingBox(), entity -> true).forEach(entity -> {
			if (!entity.getUniqueID().equals(this.ownerUniqueId)) {
				this.applyEntityCollision(entity);
			}
		});
	}

	@Override
	public void applyEntityCollision(Entity entityIn) {
		if (this.isDebug) {
			return;
		}
		entityIn.attackEntityFrom(this.getDamageSource(), this.mainDamage);
		this.onInsideBlock(Blocks.STONE.getDefaultState()); // TODO get block
	}

	@Override
	protected void onInsideBlock(BlockState blockStateIn) {
		if (this.isDebug) {
			return;
		}
		if (!blockStateIn.equals(Blocks.AIR.getDefaultState())) {
			WorldHelper.getEntitiesWithin(this.world, this.getPositionVec(), 1.0d).forEach(entity -> {
				if ((this.ownerUniqueId != null) && !entity.getUniqueID().equals(this.ownerUniqueId)
						&& !this.getUniqueID().equals(entity.getUniqueID())) {
					entity.attackEntityFrom(this.getDamageSource(), this.mainDamage);
				}
			});
			this.remove();
		}
	}

	public float getAlpha() {
		return this.ticksExisted > (10 + 20) ? 0.0f
				: (this.ticksExisted < 10 ? 1.0f : (30 - this.ticksExisted) / (float) 20);
	}

	protected void turnToMovingDirection() {
		Vector3d move = this.getMotion();
		if (move.lengthSquared() > 0.0001d) {
			this.rotationYaw = (float) ((Math.atan2(move.z, move.x) / Math.PI) * 180.0f) - 90.0f;
			this.rotationPitch = (float) ((Math.atan2(-move.y, Math.sqrt((move.x * move.x) + (move.z * move.z)))
					/ Math.PI) * 180.0f);
		}
	}

	protected void applyGravity() {
		if (this.ticksExisted < 30) {
			this.addMotion(this.getGravity());
			this.markVelocityChanged();
		}
	}

	public Vector3d getGravity() {
		// TODO parametrize gravity affection
		return new Vector3d(0.0d, -this.initialSpeed / 50f, 0.0d);
	}

	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {
		if (this.isDebug) {
			if (source.getTrueSource() instanceof PlayerEntity) {
				this.addMotion(Vector3d.fromPitchYaw(source.getTrueSource().getPitchYaw()).scale(0.5f));
			}
			this.ticksExisted = 0;
			this.initialSpeed = this.getMotion().length();
		}
		return super.attackEntityFrom(source, amount);
	}

	public void addMotion(Vector3d addition) {
		this.setMotion(this.getMotion().add(addition));
		this.markVelocityChanged();
	}

	public void addMotion(double x, double y, double z) {
		this.addMotion(new Vector3d(x, y, z));
	}

	@Override
	public boolean canBeCollidedWith() {
//		return this.isDebug;
		return true;
	}

	@Override
	protected float getEyeHeight(Pose poseIn, EntitySize sizeIn) {
		return sizeIn.height * 0.5f;
	}

	@Override
	public void writeSpawnData(PacketBuffer buffer) {
		CompoundNBT res = new CompoundNBT();
		this.writeAdditional(res);
		buffer.writeCompoundTag(res);
	}

	@Override
	public void readSpawnData(PacketBuffer additionalData) {
		this.readAdditional(additionalData.readCompoundTag());
	}

	@Override
	public boolean isSilent() {
		return true;
	}

	public DamageSource getDamageSource() {
		return new EntityDamageSource("fireball",
				WorldHelper.getEntityFromWorldByUniqueId(this.world, this.ownerUniqueId));
	}
}