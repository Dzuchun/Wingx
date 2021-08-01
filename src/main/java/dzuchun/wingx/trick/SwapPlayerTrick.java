package dzuchun.wingx.trick;

import java.util.function.Predicate;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.ImmutableList;

import dzuchun.wingx.Wingx;
import dzuchun.wingx.client.render.overlay.LivingEntitySelectOverlay;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.PacketDistributor.PacketTarget;

public class SwapPlayerTrick extends AbstractTargetedPlayerTrick {
	private static final ResourceLocation REGISTRY_NAME = new ResourceLocation(Wingx.MOD_ID, "swap_player_trick");
	private static final Logger LOG = LogManager.getLogger();
	private State state;

	public SwapPlayerTrick() {
		super();
	}

	public SwapPlayerTrick(PlayerEntity caster, double aimRadius, Predicate<LivingEntity> otherCondition) {
		super(null, caster);
		new LivingEntitySelectOverlay(aimRadius, true, otherCondition);
		LivingEntitySelectOverlay.getInstance().activate();
		if (!LivingEntitySelectOverlay.getInstance().isActive()) {
			LOG.warn("Can't create overlay now, trick is failed now.");
			this.state = State.FAILED;
			LivingEntitySelectOverlay.getInstance().deactivate();
		} else {
			this.state = State.AIMING;
		}
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public void executeClient() {
		super.executeClient();
		// We are on client
		Minecraft minecraft = Minecraft.getInstance();
		if ((this.status == 0) && this.amICaster()) {
			Vector2f look = minecraft.player.getPitchYaw();
			// TODO do something with pitch
			minecraft.player.prevRotationYaw = look.y + 180f;
			minecraft.player.rotationYaw = look.y + 180f;
		}
	}

	@Override
	public void executeServer() {
		super.executeServer();
		// We are on server
		if (!this.hasCasterPlayer() || !this.hasTarget()) {
			this.status = this.hasCasterPlayer() ? 2 : 1;
			return;
		}
		PlayerEntity caster = this.getCasterPlayer();
		Entity target = this.getTarget();
		Vector3d casterPos = caster.getPositionVec();
		Vector3d targetPos = target.getPositionVec();
		LOG.debug("Performing swap: caster at {}, target at: {}", casterPos, targetPos);
		caster.setPositionAndUpdate(targetPos.x, targetPos.y, targetPos.z);
		target.setPositionAndUpdate(casterPos.x, casterPos.y, casterPos.z);
		// TODO Check if swap negation is unlocked
		caster.fallDistance = 0;
		// TODO check if safeswap enabled
//		target.fallDistance = 0;
		this.status = 0;
	}

	@Override
	public PacketTarget getBackPacketTarget() {
		return this.hasCasterPlayer() ? PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) this.getCasterPlayer())
				: null;
	}

	@Override
	public ITrick newEmpty() {
		return new SwapPlayerTrick();
	}

	/**
	 * Called whenever player decides aim is done
	 */
	@OnlyIn(value = Dist.CLIENT)
	public boolean aimed() {
		if ((LivingEntitySelectOverlay.getInstance() == null) || !LivingEntitySelectOverlay.getInstance().isActive()) {
			LOG.warn("Can't aim: overlay is not active.");
		}
		LivingEntitySelectOverlay overlay = LivingEntitySelectOverlay.getInstance();
		if (overlay == null) {
			LOG.warn("Can't aim: overlay not active");
			return false;
		}
		overlay.deactivate();
		LivingEntity target = overlay.getSelectedEnttity();
		if (target == null) {
			LOG.debug("No entity aimed");
			this.state = State.FAILED;
			return false;
		}
		this.setTarget(target);
		this.state = State.WAIT_FOR_EXECUTION;
		return true;
	}

	public enum State {
		AIMING, WAIT_FOR_EXECUTION, FAILED
	}

	public State getState() {
		return this.state;
	}

	@Override
	protected void setRegistryName() {
		this.registryName = REGISTRY_NAME;
	}

	private static final ImmutableList<ITextComponent> MESSAGES = ImmutableList.of(
			new TranslationTextComponent("wingx.trick.swap.success").setStyle(SUCCESS_STYLE),
			new TranslationTextComponent("wingx.trick.swap.error",
					new TranslationTextComponent("wingx.trick.error_reason.no_caster")).setStyle(ERROR_STYLE),
			new TranslationTextComponent("wingx.trick.swap.error",
					new TranslationTextComponent("wingx.trick.error_reason.no_target")).setStyle(ERROR_STYLE));

	@Override
	protected ImmutableList<ITextComponent> getMessages() {
		return MESSAGES;
	}
}
