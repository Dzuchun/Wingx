package dzuchun.wingx.trick;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.PacketDistributor.PacketTarget;

public class SmashPlayerTrick extends PlayerTrick implements ITickableTrick, IServerTrick {
	private static final ResourceLocation REGISTRY_NAME = new ResourceLocation(Wingx.MOD_ID, "smash_player_trick");
	private static final Logger LOG = LogManager.getLogger();

	public SmashPlayerTrick() {
		super();
		setRegistryName(REGISTRY_NAME);
	}

	// Parameters:
	private int duration;
	private double speed;
	private float sideDamage;
	private float mainDamage;
	private Vector3d direction;

	/**
	 * Set on server. Defines if trick was performed.
	 */
	private boolean succesfull = true;
	/**
	 * Set on server. Defines trick end time.
	 */
	private long endTime = 0L;

	// Server-only fields:
	/**
	 * Set on server. Stores damaged entities.
	 */
	private Collection<Entity> damagedEntities;

	public SmashPlayerTrick(PlayerEntity caster, int duration, double speed, float sideDamage, float mainDamage,
			Vector3d direction) {
		super(caster);
		setRegistryName(REGISTRY_NAME);
		this.duration = duration;
		this.speed = speed;
		this.sideDamage = sideDamage;
		this.mainDamage = mainDamage;
		this.direction = direction.normalize();
	}

	@Override
	public void execute(LogicalSide side, World worldIn) {
		if (side == LogicalSide.SERVER) {
			if (hasCaster(worldIn)) {
				this.endTime = worldIn.getGameTime() + duration;
				succesfull = true;
				damagedEntities = new ArrayList<Entity>(0);
			} else {
				succesfull = false;
			}
		}
		if (side == LogicalSide.CLIENT) {
			Minecraft minecraft = Minecraft.getInstance();
			if (succesfull) {
				this.endTime = worldIn.getGameTime() + duration;
				minecraft.player.sendStatusMessage(new TranslationTextComponent("smash.succesfull")
						.func_230530_a_(Style.field_240709_b_.func_240712_a_(TextFormatting.LIGHT_PURPLE)), true);
			} else {
				minecraft.player.sendStatusMessage(new TranslationTextComponent("smash.fail")
						.func_230530_a_(Style.field_240709_b_.func_240712_a_(TextFormatting.RED)), true);
			}
		}
	}

	@Override
	public boolean executedSuccesfully() {
		return succesfull;
	}

	@Override
	public boolean keepExecuting(World worldIn) {
		if (worldIn.getGameTime() >= endTime) {
			return false;
		}
		if (!hasCaster(worldIn)) {
			return false;
		}
		PlayerEntity caster = getCaster(worldIn);
		return caster.collidedHorizontally || caster.collidedVertically ? false : true;
	}

	@Override
	public void stopExecute(LogicalSide side, World worldIn) {
		if (!hasCaster(worldIn)) {
			LOG.warn("No caster found");
			return;
		}
		if (side == LogicalSide.CLIENT) {
			Minecraft minecraft = Minecraft.getInstance();
			minecraft.player.sendStatusMessage(new TranslationTextComponent("smash.completed")
					.func_230530_a_(Style.field_240709_b_.func_240712_a_(TextFormatting.BOLD)), true);
			// Make additional variable then!
			worldIn.playSound(minecraft.player, minecraft.player.getPosX(), minecraft.player.getPosY(),
					minecraft.player.getPosZ(), SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.PLAYERS, 1.0f, 1.0f);
		} else {
			if (hasCaster(worldIn)) {
				PlayerEntity caster = getCaster(worldIn);
				if (caster.collidedHorizontally || caster.collidedVertically) {
					worldIn.getEntitiesInAABBexcluding(caster, caster.getBoundingBox().grow(3.0d),
							(Entity entity) -> true).forEach((Entity entity) -> {
								entity.attackEntityFrom(getDamageSource(worldIn), mainDamage);
							});
				}
			}
		}
	}

	@Override
	public void tick(World worldIn) {
		if (!hasCaster(worldIn)) {
			// TODO check if caster teleported to another dimension.
			endTime++;
			return;
		}
		PlayerEntity caster = getCaster(worldIn);
		caster.fallDistance = 0;
		worldIn.getEntitiesInAABBexcluding(caster, caster.getBoundingBox().grow(0.5d),
				(Entity entity) -> !damagedEntities.contains(entity)).forEach((Entity entity) -> {
					if (entity.attackEntityFrom(getDamageSource(worldIn), sideDamage)) {
						damagedEntities.add(entity);
					}
				});
		caster.setMotion(direction.add(0d, -1d, 0d).scale(speed / 2d));
		caster.velocityChanged = true;
	}

	protected DamageSource getDamageSource(World worldIn) {
		return hasCaster(worldIn) ? new EntityDamageSource("smash", getCaster(worldIn)) : null;
	}

	private static final String HAS_CASTER_TAG = "has_owner";
	private static final String CASTER_UUID_TAG = "owner_uuid";
	private static final String DURATION_TAG = "duration";
	private static final String SPEED_TAG = "speed";
	private static final String SIDE_DAMAGE_TAG = "side_damage";
	private static final String MAIN_DAMAGE_TAG = "main_damage";
	private static final String SUCCESFULL_TAG = "succesfull";
	private static final String END_TIME_TAG = "end_time";
	private static final String DIRECTION_TAG = "direction";

	@Override
	public void readFromNBT(INBT nbt) {
		CompoundNBT compound = (CompoundNBT) nbt;
		if (!compound.contains(HAS_CASTER_TAG) || !compound.contains(DURATION_TAG) || !compound.contains(SPEED_TAG)
				|| !compound.contains(SIDE_DAMAGE_TAG) || !compound.contains(MAIN_DAMAGE_TAG)
				|| !compound.contains(SUCCESFULL_TAG) || !compound.contains(END_TIME_TAG)
				|| !compound.contains(DIRECTION_TAG)) {
			LOG.warn("NBT data is corrupted or lost, contact someone who understand what NBT is.");
			return;
		}
		try {
			direction = NBTHelper.readVector3d(compound.getCompound(DIRECTION_TAG));
		} catch (NBTReadingException e) {
			LOG.warn("NBT data is corrupted or lost, contact someone who understand what NBT is.");
			return;
		}
		if (compound.getBoolean(HAS_CASTER_TAG)) {
			setCaster(compound.getUniqueId(CASTER_UUID_TAG));
		}
		duration = compound.getInt(DURATION_TAG);
		speed = compound.getDouble(SPEED_TAG);
		sideDamage = compound.getFloat(SIDE_DAMAGE_TAG);
		mainDamage = compound.getFloat(MAIN_DAMAGE_TAG);
		succesfull = compound.getBoolean(SUCCESFULL_TAG);
		endTime = compound.getLong(END_TIME_TAG);
	}

	@Override
	public INBT writeToNBT() {
		CompoundNBT res = new CompoundNBT();
		if (getCasterUniUuid() != null) {
			res.putBoolean(HAS_CASTER_TAG, true);
			res.putUniqueId(CASTER_UUID_TAG, getCasterUniUuid());
		} else {
			res.putBoolean(HAS_CASTER_TAG, false);
		}
		res.putInt(DURATION_TAG, duration);
		res.putDouble(SPEED_TAG, speed);
		res.putFloat(SIDE_DAMAGE_TAG, sideDamage);
		res.putFloat(MAIN_DAMAGE_TAG, mainDamage);
		res.putBoolean(SUCCESFULL_TAG, succesfull);
		res.putLong(END_TIME_TAG, endTime);
		res.put(DIRECTION_TAG, NBTHelper.writeVector3d(direction));

		return res;
	}

	@Override
	public ITrick readFromBuf(PacketBuffer buf) {
		duration = buf.readInt();
		speed = buf.readDouble();
		sideDamage = buf.readFloat();
		mainDamage = buf.readFloat();
		direction = new Vector3d(buf.readDouble(), buf.readDouble(), buf.readDouble());
		succesfull = buf.readBoolean();
		endTime = buf.readLong();

		return super.readFromBuf(buf);
	}

	@Override
	public ITrick writeToBuf(PacketBuffer buf) {
		buf.writeInt(duration);
		buf.writeDouble(speed);
		buf.writeFloat(sideDamage);
		buf.writeFloat(mainDamage);
		buf.writeDouble(direction.x);
		buf.writeDouble(direction.y);
		buf.writeDouble(direction.z);
		buf.writeBoolean(succesfull);
		buf.writeLong(endTime);

		return super.writeToBuf(buf);
	}

	@Override
	public PacketTarget getBackPacketTarget(World worldIn) {
		return hasCaster(worldIn) ? PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> getCaster(worldIn)) : null;
	}

	@Override
	public PacketTarget getEndPacketTarget(World worldIn) {
		return hasCaster(worldIn) ? PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) getCaster(worldIn)) : null;
	}

}
