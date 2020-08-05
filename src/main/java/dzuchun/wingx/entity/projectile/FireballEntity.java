package dzuchun.wingx.entity.projectile;

import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dzuchun.wingx.capability.entity.wings.IWingsCapability;
import dzuchun.wingx.capability.entity.wings.WingsProvider;
import dzuchun.wingx.init.EntityTypes;
import dzuchun.wingx.trick.NoWingsException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.Pose;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

public class FireballEntity extends Entity {
	private static final Logger LOG = LogManager.getLogger();
	private static final EntityType<FireballEntity> TYPE = EntityTypes.fireball_entity_type.get();

	private UUID ownerUniqueId;
	private double initialSpeed;
	public boolean isDebug = true;
	public int packedColor;

	/**
	 * Used to create fireball that was casted, on server
	 */
	public FireballEntity(PlayerEntity caster) throws NoWingsException {
		this(caster.world);
		this.isDebug = false;
		IWingsCapability cap = caster.getCapability(WingsProvider.WINGS, null)
				.orElseThrow(() -> new NoWingsException(caster));
		this.ownerUniqueId = caster.getUniqueID();
		this.initialSpeed = cap.fireballInitialSpeed();
		this.packedColor = cap.fireballColor();
		setMotion(caster.getMotion()
				.add(Vector3d.fromPitchYaw(caster.getPitchYaw()).normalize().scale(this.initialSpeed)));
		setPositionAndRotation(caster.getPosX(), caster.getPosY() + caster.getEyeHeight() - 0.2d, caster.getPosZ(),
				caster.rotationYawHead, caster.rotationPitch);
		recalculateSize();
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

	@Override
	protected void readAdditional(CompoundNBT compound) {
		if (compound.contains(OWNER_TAG)) {
			this.ownerUniqueId = compound.getUniqueId(OWNER_TAG);
		} else {
			LOG.debug("{} tag not found for {}", OWNER_TAG, this);
		}
		if (compound.hasUniqueId(INITIAL_SPEED_TAG)) {
			this.initialSpeed = compound.getDouble(INITIAL_SPEED_TAG);
		} else {
			LOG.debug("{} tag not found for {}", INITIAL_SPEED_TAG, this);
		}
		this.isDebug = this.ownerUniqueId == null;
	}

	@Override
	protected void writeAdditional(CompoundNBT compound) {
		if (this.ownerUniqueId != null) {
			compound.putUniqueId(OWNER_TAG, this.ownerUniqueId);
		}
		compound.putDouble(INITIAL_SPEED_TAG, this.initialSpeed);
	}

	@Override
	public IPacket<?> createSpawnPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}

	@Override
	public void tick() {
		super.tick();
		if (!this.isDebug && this.ticksExisted > 20 + 10) {
			remove(false);
			return;
		}
		if (this.ticksExisted > 10) {
			this.setMotion(getMotion().scale(0.8));
		}
		applyGravity();
		turnToMovingDirection();
		move(MoverType.SELF, getMotion());
	}

	public float getAlpha() {
		return this.ticksExisted > (10 + 20) ? 0.0f
				: (this.ticksExisted < 10 ? 1.0f : (30 - this.ticksExisted) / (float) 20);
	}

	protected void turnToMovingDirection() {
		Vector3d move = getMotion();
		if (move.lengthSquared() > 0.0001d) {
			this.rotationYaw = (float) (Math.atan2(move.z, move.x) / Math.PI * 180.0f) - 90.0f;
			this.rotationPitch = (float) (Math.atan2(-move.y, Math.sqrt(move.x * move.x + move.z * move.z)) / Math.PI
					* 180.0f);
		}
	}

	protected void applyGravity() {
		if (this.ticksExisted < 30) {
			addMotion(getGravity());
			markVelocityChanged();
		}
	}

	public Vector3d getGravity() {
		return new Vector3d(0.0d, -0.02d, 0.0d);
	}

	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {
		if (this.isDebug) {
			if (source.getTrueSource() instanceof PlayerEntity) {
				addMotion(Vector3d.fromPitchYaw(source.getTrueSource().getPitchYaw()).scale(0.5f));
			}
			this.ticksExisted = 0;
			this.initialSpeed = getMotion().length();
		}
		return super.attackEntityFrom(source, amount);
	}

	public void addMotion(Vector3d addition) {
		setMotion(getMotion().add(addition));
		markVelocityChanged();
	}

	public void addMotion(double x, double y, double z) {
		addMotion(new Vector3d(x, y, z));
	}

	@Override
	public boolean canBeCollidedWith() {
		return this.isDebug;
	}

	@Override
	protected float getEyeHeight(Pose poseIn, EntitySize sizeIn) {
		return sizeIn.height * 0.5f;
	}
}