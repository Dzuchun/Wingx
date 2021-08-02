package dzuchun.wingx.trick;

import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.ImmutableList;

import dzuchun.wingx.client.input.KeyEvents.WingxKey;
import dzuchun.wingx.client.render.overlay.LivingEntitySelectOverlay;
import dzuchun.wingx.client.render.overlay.LivingEntityTargetOverlay;
import dzuchun.wingx.entity.projectile.HomingFireballEntity;
import dzuchun.wingx.init.Tricks;
import dzuchun.wingx.util.NetworkHelper;
import dzuchun.wingx.util.WorldHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.PacketDistributor.PacketTarget;

//TODO add integration with interrupt messages
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
		if (this.status != 0) {
			return;
		}
		if (LivingEntitySelectOverlay.getInstance() == null) {
			// TODO rethink overlay system
			// TODO parametrize aim range
			new LivingEntitySelectOverlay(10.0f, true, e -> true);
			if (!LivingEntitySelectOverlay.getInstance().activate()) {
				this.status = 1; // Unknown activation error
			} else {
				this.status = 3; // Aiming
				WingxKey.FIREBALL_HOMING.setTrick(this);
			}
		} else {
			this.status = 2; // Overlay conflict
		}
	}

	@Override
	public void beginAimServer() {
		// TODO check for conditions(caster exist, target exist, caster free, homing
		// unlocked, enough mana), set parameters
		PlayerEntity caster = this.getCasterPlayer();
		if ((caster == null) || (AbstractInterruptablePlayerTrick.playerBusyFor(caster) != 0)) {
			this.status = 6; // Caster busy
			return;
		}
		this.duration = 10;
		this.status = 0;// Ok
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void endAim() {
		if ((LivingEntitySelectOverlay.getInstance() == null) || !LivingEntitySelectOverlay.getInstance().isActive()) {
			LOG.warn("Can't aim: overlay is not active.");
			this.status = 1; // Unknown activation error
		}
		LivingEntitySelectOverlay overlay = LivingEntitySelectOverlay.getInstance();
		if (overlay == null) {
			LOG.warn("Can't aim: overlay not active");
			this.status = 1; // Unknown activation error
			return;
		}
		overlay.deactivate();
		LivingEntity target = overlay.getSelectedEnttity();
		if (target == null) {
			LOG.debug("No entity aimed");
			this.status = 4; // No target
			return;
		}
		this.setTarget(target);
		this.status = 5; // Aimed
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

	@SuppressWarnings("deprecation")
	@Override
	public void onTrickEndServer() throws NoCasterException {
		super.onTrickEndServer();
		if ((this.status == 0) && (this.interrupted == false)) {
			((ServerWorld) this.casterWorld)
					.summonEntity(new HomingFireballEntity(this.getCasterPlayer(), this.getTarget()));
			LOG.warn("Summoning homing fireball of {} to {}", this.getCasterPlayer(), this.getTarget());
		} else {
			LOG.warn("Unknown error happened");
		}
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public void executeClient() {
		super.executeClient();
		LOG.warn("Executing on client, status - {}", this.status);
		new LivingEntityTargetOverlay((LivingEntity) this.getTarget()).activate();
		// TODO check if activated (usually it will)
	}

	@Override
	public void executeServer() {
		// TODO check for conditions(caster exist, target exist, caster free, homing
		// unlocked, enough mana), set parameters
		this.status = 0; // Executing
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

	private static final ImmutableList<ITextComponent> MESSAGES = ImmutableList.of(
			new TranslationTextComponent("wingx.trick.fireball.start").setStyle(SUCCESS_STYLE),
			new TranslationTextComponent("wingx.trick.fireball.error",
					new TranslationTextComponent("wingx.trick.error_reason.overlay_unknown")).setStyle(ERROR_STYLE),
			new TranslationTextComponent("wingx.trick.fireball.error",
					new TranslationTextComponent("wingx.trick.error_reason.overlay_conflict")).setStyle(ERROR_STYLE),
			new TranslationTextComponent("wingx.trick.default_aiming").setStyle(SUCCESS_STYLE),
			new TranslationTextComponent("wingx.trick.fireball.error",
					new TranslationTextComponent("wingx.trick.error_reason.no_target")).setStyle(ERROR_STYLE),
			new TranslationTextComponent("wingx.trick.default_aimed").setStyle(SUCCESS_STYLE),
			new TranslationTextComponent("wingx.trick.fireball.error",
					new TranslationTextComponent("wingx.trick.error_reason.caster_busy")).setStyle(ERROR_STYLE));

	@Override
	protected ImmutableList<ITextComponent> getMessages() {
		return MESSAGES;
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
