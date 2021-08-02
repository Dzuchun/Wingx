package dzuchun.wingx.trick;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dzuchun.wingx.init.Tricks;
import dzuchun.wingx.trick.state.TrickState;
import dzuchun.wingx.trick.state.TrickStates;
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
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.PacketDistributor.PacketTarget;

public class SmashPlayerTrick extends AbstractInterruptablePlayerTrick implements IPersistableTrick {
	private static final Logger LOG = LogManager.getLogger();

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
		this.direction = (direction != null) ? direction.normalize() : null;
	}

	@Override
	public void executeServer() {
		super.executeServer();
		if (this.state.isError()) {
			return;
		}
		// We are on server
		if (this.hasCasterPlayer()) {
			this.state = TrickStates.OK; // Ok
			this.damagedEntities = new ArrayList<Entity>(0);
		} else {
			this.state = TrickStates.NO_CASTER; // No caster
		}
	}

	@Override
	public boolean keepExecuting() {
		AbstractCastedTrick.assertHasCasterInfo(this);

		if ((this.casterWorld.getGameTime() >= this.endTime) || !this.hasCasterPlayer()) {
			LOG.info("End term expired or no caster exists. Stopping execution.");
			this.state = TrickStates.RUN_ENDED;
			return false;
		}
		PlayerEntity caster = this.getCasterPlayer();
		if (caster.collidedHorizontally || caster.collidedVertically) {
			this.state = TrickStates.FAST_FORWARD;
			return false; // Trick was interrupted (which is ok for this one))
		} else {
			return true; // Keep excecuting
		}
	}

	@Override
	public void onTrickEndCommon() throws NoCasterException {
		super.onTrickEndCommon();
		AbstractCastedTrick.assertHasCasterInfo(this);
		if (!this.hasCasterPlayer()) {
			LOG.warn("No caster found");
			this.state = TrickStates.NO_CASTER;
			return;
		}
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public void onTrickEndClient() throws NoCasterException {
		super.onTrickEndClient();
		// We are on client
		Minecraft minecraft = Minecraft.getInstance();
		// Make additional variable then!
		this.casterWorld.playSound(minecraft.player, minecraft.player.getPosX(), minecraft.player.getPosY(),
				minecraft.player.getPosZ(), SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.PLAYERS, 1.0f, 1.0f);
	}

	@Override
	public void onTrickEndServer() throws NoCasterException {
		super.onTrickEndServer(); // We are on server
		if (this.hasCasterPlayer()) {
			PlayerEntity caster = this.getCasterPlayer();
			if (this.state != TrickStates.RUN_ENDED) {
				// TODO parametrize radius
				this.casterWorld
						.getEntitiesInAABBexcluding(caster, caster.getBoundingBox().grow(4.0d), (Entity entity) -> true)
						.forEach((Entity entity) -> {
							entity.attackEntityFrom(this.getDamageSource(), this.mainDamage);
						});
			}
		} else {
			LOG.warn("No caster found, can't perform onCastEnd");
			this.state = TrickStates.NO_CASTER;
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
	private static final String STATE_TAG = "state";
	private static final String END_TIME_TAG = "end_time";
	private static final String DIRECTION_TAG = "direction";

	@Override
	public PacketTarget getBackPacketTarget() {
		return this.hasCasterPlayer() ? PacketDistributor.TRACKING_ENTITY_AND_SELF.with(this::getCasterPlayer) : null;
	}

	@Override
	public PacketTarget getEndPacketTarget() {
		return this.hasCasterPlayer() ? PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) this.getCasterPlayer())
				: null;
	}

	public static class TrickType extends AbstractInterruptablePlayerTrick.TrickType<SmashPlayerTrick>
			implements IPersistableTrick.TrickType<SmashPlayerTrick> {

		@Override
		public SmashPlayerTrick writeToBuf(SmashPlayerTrick trick, PacketBuffer buf) {
			buf.writeInt(trick.duration);
			buf.writeDouble(trick.speed);
			buf.writeFloat(trick.sideDamage);
			buf.writeFloat(trick.mainDamage);
			buf.writeDouble(trick.direction.x);
			buf.writeDouble(trick.direction.y);
			buf.writeDouble(trick.direction.z);
			buf.writeLong(trick.endTime);
			return super.writeToBuf(trick, buf);
		}

		@Override
		protected SmashPlayerTrick readFromBufInternal(SmashPlayerTrick trick, PacketBuffer buf) {
			trick.duration = buf.readInt();
			trick.speed = buf.readDouble();
			trick.sideDamage = buf.readFloat();
			trick.mainDamage = buf.readFloat();
			trick.direction = new Vector3d(buf.readDouble(), buf.readDouble(), buf.readDouble());
			trick.endTime = buf.readLong();
			return super.readFromBufInternal(trick, buf);
		}

		@Override
		public SmashPlayerTrick readFromNBT(INBT nbt) {
			CompoundNBT compound = (CompoundNBT) nbt;
			if (!compound.contains(HAS_CASTER_TAG) || !compound.contains(DURATION_TAG) || !compound.contains(SPEED_TAG)
					|| !compound.contains(SIDE_DAMAGE_TAG) || !compound.contains(MAIN_DAMAGE_TAG)
					|| !compound.contains(STATE_TAG) || !compound.contains(END_TIME_TAG)
					|| !compound.contains(DIRECTION_TAG)) {
				LOG.warn("NBT data is corrupted or lost, so trick will be readed.");
				return null;
			}
			SmashPlayerTrick res = this.newEmpty();
			try {
				res.direction = NBTHelper.readVector3d(compound.getCompound(DIRECTION_TAG));
			} catch (NBTReadingException e) {
				LOG.warn("NBT data is corrupted or lost, so trick will be readed.");
				return null;
			}
			if (compound.getBoolean(HAS_CASTER_TAG)) {
				res.casterUniqueId = compound.getUniqueId(CASTER_UUID_TAG);
			}
			res.duration = compound.getInt(DURATION_TAG);
			res.speed = compound.getDouble(SPEED_TAG);
			res.sideDamage = compound.getFloat(SIDE_DAMAGE_TAG);
			res.mainDamage = compound.getFloat(MAIN_DAMAGE_TAG);
			res.state = TrickState.readState(compound.getCompound(STATE_TAG));
			res.endTime = compound.getLong(END_TIME_TAG);
			return res;
		}

		@Override
		public INBT writeToNBT(SmashPlayerTrick trick) {
			CompoundNBT res = new CompoundNBT();
			if (trick.hasCasterPlayer()) {
				res.putBoolean(HAS_CASTER_TAG, true);
				res.putUniqueId(CASTER_UUID_TAG, trick.casterUniqueId);
			} else {
				res.putBoolean(HAS_CASTER_TAG, false);
			}
			res.putInt(DURATION_TAG, trick.duration);
			res.putDouble(SPEED_TAG, trick.speed);
			res.putFloat(SIDE_DAMAGE_TAG, trick.sideDamage);
			res.putFloat(MAIN_DAMAGE_TAG, trick.mainDamage);
			res.put(STATE_TAG, trick.state.writeState());
			res.putLong(END_TIME_TAG, trick.endTime);
			res.put(DIRECTION_TAG, NBTHelper.writeVector3d(trick.direction));
			return res;
		}

		@Override
		public SmashPlayerTrick newEmpty() {
			return new SmashPlayerTrick(null, 0, 0.0d, 0.0f, 0.0f, null);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public SmashPlayerTrick.TrickType getType() {
		return Tricks.SMASH_TRICK.get();
	}

	@Override
	public boolean conflicts(IInterruptableTrick other) {
		return (other.getType().equals(this.getType())) && (other.getCaster().equals(this.getCaster()));
	}

}
