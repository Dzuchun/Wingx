package dzuchun.wingx.trick;

import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.ImmutableList;

import dzuchun.wingx.Wingx;
import dzuchun.wingx.client.render.overlay.LivingEntitySelectOverlay;
import dzuchun.wingx.client.render.overlay.LivingEntityTargetOverlay;
import dzuchun.wingx.entity.projectile.HomingFireballEntity;
import dzuchun.wingx.util.NetworkHelper;
import dzuchun.wingx.util.WorldHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
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
		implements ITargetedTrick, ITimeredTrick {
	private static final Logger LOG = LogManager.getLogger();

	public HomingFireballCastTargetedPlayerTrick() {
		super();
	}

	@OnlyIn(value = Dist.CLIENT)
	public HomingFireballCastTargetedPlayerTrick(PlayerEntity caster) {
		super(caster, 10, InterruptCondition.NO_CONDITION); // Dummy
		if (LivingEntitySelectOverlay.getInstance() == null) {
			new LivingEntitySelectOverlay(10.0f, true, e -> true);
			if (!LivingEntitySelectOverlay.getInstance().activate()) {
				this.status = 1; // Unknown activation error
			} else {
				this.status = 3; // Aiming
			}
		} else {
			this.status = 2; // Overlay conflict
		}
	}

	@Override
	public PacketTarget getBackPacketTarget() {
		return this.hasCasterPlayer() ? PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) this.getCasterPlayer())
				: null;
	}

	@Override
	public ITrick newEmpty() {
		return new HomingFireballCastTargetedPlayerTrick();
	}

	@Override
	public PacketTarget getEndPacketTarget() {
		return this.hasCasterPlayer() ? PacketDistributor.TRACKING_ENTITY_AND_SELF.with(this::getCasterPlayer) : null;
	}

	public boolean aimed() {
		if ((LivingEntitySelectOverlay.getInstance() == null) || !LivingEntitySelectOverlay.getInstance().isActive()) {
			LOG.warn("Can't aim: overlay is not active.");
			this.status = 1; // Unknown activation error
		}
		LivingEntitySelectOverlay overlay = LivingEntitySelectOverlay.getInstance();
		if (overlay == null) {
			LOG.warn("Can't aim: overlay not active");
			this.status = 1; // Unknown activation error
			return false;
		}
		overlay.deactivate();
		LivingEntity target = overlay.getSelectedEnttity();
		if (target == null) {
			LOG.debug("No entity aimed");
			this.status = 4; // No target
			return false;
		}
		this.setTarget(target);
		this.status = 5; // Aimed
		return true;
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

	private static final ResourceLocation REGISTRY_NAME = new ResourceLocation(Wingx.MOD_ID,
			"homing_firefall_player_trick");

	@Override
	protected void setRegistryName() {
		this.registryName = REGISTRY_NAME;
	}

	@Override
	public ITrick writeToBuf(PacketBuffer buf) {
		NetworkHelper.writeChecked(buf, this.targetUniqueId, PacketBuffer::writeUniqueId);
		return super.writeToBuf(buf);
	}

	@Override
	public ITrick readFromBuf(PacketBuffer buf) {
		this.targetUniqueId = NetworkHelper.readChecked(buf, PacketBuffer::readUniqueId);
		return super.readFromBuf(buf);
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
			new TranslationTextComponent("wingx.trick.default_aimed").setStyle(SUCCESS_STYLE));

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
}
