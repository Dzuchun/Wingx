package dzuchun.wingx.trick;

import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dzuchun.wingx.client.input.KeyEvents.WingxKey;
import dzuchun.wingx.client.render.overlay.LivingEntitySelectOverlay;
import dzuchun.wingx.client.render.overlay.LivingEntityTargetOverlay;
import dzuchun.wingx.entity.projectile.HomingFireballEntity;
import dzuchun.wingx.init.Tricks;
import dzuchun.wingx.trick.state.TrickStates;
import dzuchun.wingx.util.NetworkHelper;
import dzuchun.wingx.util.WorldHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.PacketDistributor.PacketTarget;

public class HomingFireballCastTargetedPlayerTrick extends AbstractInterruptablePlayerTrick
		implements ITargetedTrick, ITimeredTrick, IAimingTrick {
	private static final Logger LOG = LogManager.getLogger();

	@OnlyIn(value = Dist.CLIENT)
	public HomingFireballCastTargetedPlayerTrick(PlayerEntity caster) {
		super(caster, 0, InterruptCondition.NO_CONDITION); // Dummy
	}

	@Override
	public PacketTarget getAimBackTarget() {
		return this.hasCasterPlayer() ? PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) this.getCasterPlayer())
				: null;
	}

	@Override
	public PacketTarget getBackPacketTarget() {
		return this.hasCasterPlayer() ? PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) this.getCasterPlayer())
				: null;
	}

	@Override
	public PacketTarget getEndPacketTarget() {
		return this.hasCasterPlayer() ? PacketDistributor.TRACKING_ENTITY_AND_SELF.with(this::getCasterPlayer) : null;
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public void beginAimClient() {
		if (this.state.isError()) {
			return;
		}
		if (LivingEntitySelectOverlay.getInstance() == null) {
			// TODO rethink overlay system
			// TODO parametrize aim range
			new LivingEntitySelectOverlay(10.0f, true,
					entity -> !entity.getUniqueID().equals(Minecraft.getInstance().player.getUniqueID()));
			if (!LivingEntitySelectOverlay.getInstance().activate()) {
				this.state = TrickStates.OVERLAY_ERROR; // Overlay error
			} else {
				this.state = TrickStates.AIMING; // Aiming
				WingxKey.FIREBALL_HOMING.setTrick(this);
			}
		} else {
			this.state = TrickStates.OVERLAY_CONFLICT; // Overlay conflict
		}
	}

	@Override
	public void beginAimServer() {
		// TODO check for conditions(caster exist, target exist, caster free, homing
		// unlocked, enough mana), set parameters
		PlayerEntity caster = this.getCasterPlayer();
		if ((caster == null) || (AbstractInterruptablePlayerTrick.playerBusyFor(caster) != 0)) {
			this.state = TrickStates.CASTER_BUSY; // Caster busy
			return;
		}
		this.duration = 10;
		this.state = TrickStates.OK;// Ok
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void endAim() {
		if ((LivingEntitySelectOverlay.getInstance() == null) || !LivingEntitySelectOverlay.getInstance().isActive()) {
			LOG.warn("Can't aim: overlay is not active.");
			this.state = TrickStates.OVERLAY_ERROR; // Overlay error
		}
		LivingEntitySelectOverlay overlay = LivingEntitySelectOverlay.getInstance();
		if (overlay == null) {
			LOG.warn("Can't aim: overlay not active");
			this.state = TrickStates.OVERLAY_ERROR; // Overlay error
			return;
		}
		overlay.deactivate();
		LivingEntity target = overlay.getSelectedEnttity();
		if (target == null) {
			LOG.debug("No entity aimed");
			this.state = TrickStates.NO_TARGET; // No target
			return;
		}
		this.setTarget(target);
		this.state = TrickStates.AIMED; // Aimed
		return;
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public void onTrickEndClient() throws NoCasterException {
		super.onTrickEndClient();
		LivingEntityTargetOverlay overlay = LivingEntityTargetOverlay.getOverlayForTarget(this.getTarget());
		if (overlay != null) {
			overlay.deactivate();
		}
	}

	@Override
	public void onTrickEndServer() throws NoCasterException {
		super.onTrickEndServer();
		if (!this.state.isError()) {
			((ServerWorld) this.casterWorld)
					.summonEntity(new HomingFireballEntity(this.getCasterPlayer(), this.getTarget()));
			LOG.warn("Summoning homing fireball of {} to {}", this.getCasterPlayer(), this.getTarget());
			this.state = TrickStates.SUCCESS;
		} else {
			LOG.warn("Unknown error happened");
		}
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public void executeClient() {
		super.executeClient();
		if (this.state.isError()) {
			return;
		}
		LOG.warn("Executing on client, status - {}", this.state.toString());
		new LivingEntityTargetOverlay((LivingEntity) this.getTarget()).activate();
		// TODO check if activated (usually it will)
	}

	@Override
	public void executeServer() {
		if (this.state.isError()) {
			return;
		}
		// TODO check for conditions(caster exist, target exist, caster free, homing
		// unlocked, enough mana), set parameters
		super.executeServer();
	}

	protected World targetWorld;
	protected UUID targetUniqueId;

	@Override
	public void setTargetWorld(World worldIn) {
		this.targetWorld = worldIn;
	}

	@Override
	public boolean hasTarget() {
		return (this.targetUniqueId != null) && (this.targetWorld != null)
				&& (WorldHelper.getEntityFromWorldByUniqueId(this.targetWorld, this.targetUniqueId) != null);
	}

	@Override
	public Entity getTarget() {
		return (this.targetWorld == null) || (this.targetUniqueId == null) ? null
				: WorldHelper.getEntityFromWorldByUniqueId(this.targetWorld, this.targetUniqueId);
	}

	@Override
	public void setTarget(Entity entityIn) {
		if (entityIn != null) {
			this.targetUniqueId = entityIn.getUniqueID();
			this.targetWorld = entityIn.world;
		}
	}

	@Override
	public int timeFull() throws NoCasterException {
		return this.duration;
	}

	@Override
	public double partLeft() throws NoCasterException {
		assertHasCaster(this);
		long time = this.casterWorld.getGameTime();
		return ((double) (time - this.beginTime)) / (this.duration);
	}

	public static class TrickType
			extends AbstractInterruptablePlayerTrick.TrickType<HomingFireballCastTargetedPlayerTrick>
			implements IAimingTrick.TrickType<HomingFireballCastTargetedPlayerTrick>,
			ITimeredTrick.TrickType<HomingFireballCastTargetedPlayerTrick>,
			ITargetedTrick.TrickType<HomingFireballCastTargetedPlayerTrick> {

		@Override
		protected HomingFireballCastTargetedPlayerTrick readFromBufInternal(HomingFireballCastTargetedPlayerTrick trick,
				PacketBuffer buf) {
			trick.targetUniqueId = NetworkHelper.readChecked(buf, PacketBuffer::readUniqueId);
			return super.readFromBufInternal(trick, buf);
		}

		@Override
		public HomingFireballCastTargetedPlayerTrick writeToBuf(HomingFireballCastTargetedPlayerTrick trick,
				PacketBuffer buf) {
			NetworkHelper.writeChecked(buf, trick.targetUniqueId, PacketBuffer::writeUniqueId);
			return super.writeToBuf(trick, buf);
		}

		@Override
		public HomingFireballCastTargetedPlayerTrick newEmpty() {
			return new HomingFireballCastTargetedPlayerTrick(null);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public HomingFireballCastTargetedPlayerTrick.TrickType getType() {
		return Tricks.HOMING_FIREBALL_CAST_TRICK.get();
	}
}
