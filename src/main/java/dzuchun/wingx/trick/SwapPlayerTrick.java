package dzuchun.wingx.trick;

import java.util.function.Predicate;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.PacketDistributor.PacketTarget;

public class SwapPlayerTrick extends TargetedPlayerTrick {
	private static final ResourceLocation REGISTRY_NAME = new ResourceLocation(Wingx.MOD_ID, "swap_player_trick");
	private static final Logger LOG = LogManager.getLogger();
	private State state;

	public SwapPlayerTrick() {
		super();
		setRegistryName(REGISTRY_NAME);
	}

	public SwapPlayerTrick(PlayerEntity caster, double aimRadius, Predicate<LivingEntity> otherCondition) {
		super(null, caster);
		setRegistryName(REGISTRY_NAME);
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

	@Override
	public void execute(LogicalSide side, World worldIn) {
		if (side == LogicalSide.CLIENT) {
			Minecraft minecraft = Minecraft.getInstance();
			if (succesfull) {
				Vector2f look = minecraft.player.getPitchYaw();
				minecraft.player.prevRotationPitch = -look.x;
				minecraft.player.rotationPitch = -look.x;
				minecraft.player.prevRotationYaw = look.y + 180f;
				minecraft.player.rotationYaw = look.y + 180f;
				minecraft.player.sendStatusMessage(new TranslationTextComponent("wingx.swap.successfull"), true);
			} else {
				minecraft.player.sendStatusMessage(new TranslationTextComponent("wingx.swap.fail"), true);
			}
		} else {
			ServerWorld world = (ServerWorld) worldIn;
			if (!hasCaster(worldIn) || !hasTarget(world)) {
				succesfull = false;
				return;
			}
			PlayerEntity caster = getCaster(world);
			Entity target = getTarget(world);
			Vector2f look = caster.getPitchYaw();
			Vector3d casterPos = caster.getPositionVec();
			Vector3d targetPos = target.getPositionVec();
			LOG.debug("Performing swap: caster at {}, target at: {}", casterPos, targetPos);
			caster.setPositionAndUpdate(targetPos.x, targetPos.y, targetPos.z);
//			caster.rotateTowards(look.y + 180f, -look.x); //I can't use that here at all:(
			target.setPositionAndUpdate(casterPos.x, casterPos.y, casterPos.z);
			succesfull = true;
		}
	}

	@Override
	public PacketTarget getBackPacketTarget(World worldIn) {
		return hasCaster(worldIn) ? PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) getCaster(worldIn)) : null;
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
		if (LivingEntitySelectOverlay.getInstance() == null || !LivingEntitySelectOverlay.getInstance().isActive()) {
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
		setTarget(target);
		this.state = State.WAIT_FOR_EXECUTION;
		return true;
	}

	public enum State {
		AIMING, WAIT_FOR_EXECUTION, FAILED
	}

	public State getState() {
		return this.state;
	}
}
