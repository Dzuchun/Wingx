package dzuchun.wingx.trick;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.ImmutableList;

import dzuchun.wingx.Wingx;
import dzuchun.wingx.util.NBTHelper;
import dzuchun.wingx.util.NBTReadingException;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.PacketDistributor.PacketTarget;

public class SmashPlayerTrick extends AbstractInterruptablePlayerTrick implements IPersistableTrick {
	private static final ResourceLocation REGISTRY_NAME = new ResourceLocation(Wingx.MOD_ID, "smash_player_trick");
	private static final Logger LOG = LogManager.getLogger();

	public SmashPlayerTrick() {
		super();
	}

	// Parameters:
	private int duration;
	private double speed;
	private float sideDamage;
	private float mainDamage;
	private Vector3d direction;
	/**
	 * Set on server. Stores damaged entities.
	 */
	private Collection<Entity> damagedEntities;

	public Collection<Entity> getDamaged() {
		return this.damagedEntities;
	}

	public boolean wasDamaged(Entity entityIn) {
		return this.damagedEntities.contains(entityIn);
	}

	public SmashPlayerTrick(PlayerEntity caster, int duration, double speed, float sideDamage, float mainDamage,
			Vector3d direction) {
		super(caster, duration, InterruptCondition.NO_CONDITION);
		this.duration = duration;
		this.speed = speed;
		this.sideDamage = sideDamage;
		this.mainDamage = mainDamage;
		this.direction = direction.normalize();
	}

	@Override
	public void execute(LogicalSide side) {
		super.execute(side);
		if (side == LogicalSide.SERVER) {
			// We are on server
			if (this.hasCasterPlayer()) {
				this.status = 0;
				this.damagedEntities = new ArrayList<Entity>(0);
			} else {
				this.status = 1; // No caster
			}
		}
	}

	@Override
	public boolean keepExecuting() {
		AbstractCastedTrick.assertHasCasterInfo(this);

		if ((this.casterWorld.getGameTime() >= this.endTime) || !this.hasCasterPlayer()) {
			LOG.info("End term expired or no caster exists. Stopping execute.");
			return false;
		}
		PlayerEntity caster = this.getCasterPlayer();
		return caster.collidedHorizontally || caster.collidedVertically ? false : true; // TODO repair collision checks
	}

	@Override
	public void onCastEnd(LogicalSide side) {
		super.onCastEnd(side);
		AbstractCastedTrick.assertHasCasterInfo(this);
		if (!this.hasCasterPlayer()) {
			LOG.warn("No caster found");
			return;
		}
		if (side == LogicalSide.CLIENT) {
			// We are on client
			Minecraft minecraft = Minecraft.getInstance();
			// Make additional variable then!
			this.casterWorld.playSound(minecraft.player, minecraft.player.getPosX(), minecraft.player.getPosY(),
					minecraft.player.getPosZ(), SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.PLAYERS, 1.0f, 1.0f);
		} else {
			// We are on server
			if (this.hasCasterPlayer()) {
				PlayerEntity caster = this.getCasterPlayer();
				if (!this.castEndedNaturally()) {
					this.status = 2;// Interrupted
					this.casterWorld.getEntitiesInAABBexcluding(caster, caster.getBoundingBox().grow(3.0d),
							(Entity entity) -> true).forEach((Entity entity) -> {
								entity.attackEntityFrom(this.getDamageSource(), this.mainDamage);
							});
				} else {
					this.status = 3; // Cast ended naturally
				}
			} else {
				LOG.warn("No caster found, can't perform onCastEnd");
			}
		}
	}

	@Override
	public void tick() {
		super.tick();
		AbstractCastedTrick.assertHasCasterInfo(this);
		if (!this.hasCasterPlayer()) {
			// TODO check if caster teleported to another dimension.
			return;
		}
		PlayerEntity caster = this.getCasterPlayer();
		caster.fallDistance = 0;
		this.casterWorld.getEntitiesInAABBexcluding(caster, caster.getBoundingBox().grow(0.5d),
				(Entity entity) -> !this.damagedEntities.contains(entity)).forEach((Entity entity) -> {
					if (entity.attackEntityFrom(this.getDamageSource(), this.sideDamage)) {
						this.damagedEntities.add(entity);
					}
				});
		caster.setMotion(this.direction.scale(this.speed));
		caster.velocityChanged = true;
	}

	protected DamageSource getDamageSource() {
		return this.hasCasterPlayer() ? new EntityDamageSource("smash", this.getCaster()) : null;
	}

	private static final String HAS_CASTER_TAG = "has_owner";
	private static final String CASTER_UUID_TAG = "owner_uuid";
	private static final String DURATION_TAG = "duration";
	private static final String SPEED_TAG = "speed";
	private static final String SIDE_DAMAGE_TAG = "side_damage";
	private static final String MAIN_DAMAGE_TAG = "main_damage";
	private static final String STATUS_TAG = "status";
	private static final String END_TIME_TAG = "end_time";
	private static final String DIRECTION_TAG = "direction";

	@Override
	public void readFromNBT(INBT nbt) {
		CompoundNBT compound = (CompoundNBT) nbt;
		if (!compound.contains(HAS_CASTER_TAG) || !compound.contains(DURATION_TAG) || !compound.contains(SPEED_TAG)
				|| !compound.contains(SIDE_DAMAGE_TAG) || !compound.contains(MAIN_DAMAGE_TAG)
				|| !compound.contains(STATUS_TAG) || !compound.contains(END_TIME_TAG)
				|| !compound.contains(DIRECTION_TAG)) {
			LOG.warn("NBT data is corrupted or lost, contact someone who understand what NBT is.");
			return;
		}
		try {
			this.direction = NBTHelper.readVector3d(compound.getCompound(DIRECTION_TAG));
		} catch (NBTReadingException e) {
			LOG.warn("NBT data is corrupted or lost, contact someone who understand what NBT is.");
			return;
		}
		if (compound.getBoolean(HAS_CASTER_TAG)) {
			this.casterUniqueId = compound.getUniqueId(CASTER_UUID_TAG);
		}
		this.duration = compound.getInt(DURATION_TAG);
		this.speed = compound.getDouble(SPEED_TAG);
		this.sideDamage = compound.getFloat(SIDE_DAMAGE_TAG);
		this.mainDamage = compound.getFloat(MAIN_DAMAGE_TAG);
		this.status = compound.getInt(STATUS_TAG);
		this.endTime = compound.getLong(END_TIME_TAG);
	}

	@Override
	public INBT writeToNBT() {
		CompoundNBT res = new CompoundNBT();
		if (this.hasCasterPlayer()) {
			res.putBoolean(HAS_CASTER_TAG, true);
			res.putUniqueId(CASTER_UUID_TAG, this.casterUniqueId);
		} else {
			res.putBoolean(HAS_CASTER_TAG, false);
		}
		res.putInt(DURATION_TAG, this.duration);
		res.putDouble(SPEED_TAG, this.speed);
		res.putFloat(SIDE_DAMAGE_TAG, this.sideDamage);
		res.putFloat(MAIN_DAMAGE_TAG, this.mainDamage);
		res.putInt(STATUS_TAG, this.status);
		res.putLong(END_TIME_TAG, this.endTime);
		res.put(DIRECTION_TAG, NBTHelper.writeVector3d(this.direction));

		return res;
	}

	@Override
	public ITrick readFromBuf(PacketBuffer buf) {
		this.duration = buf.readInt();
		this.speed = buf.readDouble();
		this.sideDamage = buf.readFloat();
		this.mainDamage = buf.readFloat();
		this.direction = new Vector3d(buf.readDouble(), buf.readDouble(), buf.readDouble());
		this.endTime = buf.readLong();

		return super.readFromBuf(buf);
	}

	@Override
	public ITrick writeToBuf(PacketBuffer buf) {
		buf.writeInt(this.duration);
		buf.writeDouble(this.speed);
		buf.writeFloat(this.sideDamage);
		buf.writeFloat(this.mainDamage);
		buf.writeDouble(this.direction.x);
		buf.writeDouble(this.direction.y);
		buf.writeDouble(this.direction.z);
		buf.writeLong(this.endTime);

		return super.writeToBuf(buf);
	}

	@Override
	public PacketTarget getBackPacketTarget() {
		return this.hasCasterPlayer() ? PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> this.getCasterPlayer())
				: null;
	}

	@Override
	public PacketTarget getEndPacketTarget() {
		return this.hasCasterPlayer() ? PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) this.getCasterPlayer())
				: null;
	}

	@Override
	public ITrick newEmpty() {
		return new SmashPlayerTrick();
	}

	@Override
	protected void setRegistryName() {
		this.registryName = REGISTRY_NAME;
	}

	@Override
	public boolean castEndedNaturally() {
		assertHasCaster(this);
		PlayerEntity caster = this.getCasterPlayer();
		return !caster.collidedHorizontally && !caster.collidedVertically;
	}

	private static final ImmutableList<ITextComponent> MESSAGES = ImmutableList.of(
			new TranslationTextComponent("wingx.trick.smash.start").setStyle(SUCCESS_STYLE),
			new TranslationTextComponent("wingx.trick.smash.error",
					new TranslationTextComponent("wingx.trick.error_reason.no_caster")).setStyle(ERROR_STYLE),
			new TranslationTextComponent("wingx.trick.smash.interrupt").setStyle(SUCCESS_STYLE),
			new TranslationTextComponent("wingx.trick.smash.success").setStyle(SUCCESS_STYLE));

	@Override
	protected ImmutableList<ITextComponent> getMessages() {
		return MESSAGES;
	}

}
