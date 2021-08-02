package dzuchun.wingx.trick;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dzuchun.wingx.client.input.KeyEvents.WingxKey;
import dzuchun.wingx.client.render.overlay.LivingEntitySelectOverlay;
import dzuchun.wingx.init.Tricks;
import dzuchun.wingx.trick.state.TrickStates;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.PacketDistributor.PacketTarget;

public class SwapPlayerTrick extends AbstractTargetedPlayerTrick implements IAimingTrick {
	private static final Logger LOG = LogManager.getLogger();

	private double radius = 0;

	public SwapPlayerTrick(PlayerEntity caster) {
		super(null, caster);
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public void executeClient() {
		super.executeClient();
		// We are on client
		if ((!this.state.isError()) && this.iAmCaster()) {
			Minecraft minecraft = Minecraft.getInstance();
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
			this.state = this.hasCasterPlayer() ? TrickStates.NO_TARGET : TrickStates.NO_CASTER;
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
		this.state = TrickStates.SUCCESS;
	}

	@Override
	public PacketTarget getBackPacketTarget() {
		return this.hasCasterPlayer() ? PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) this.getCasterPlayer())
				: null;
	}

	@Override
	public PacketTarget getAimBackTarget() {
		return this.hasCasterPlayer() ? PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) this.getCasterPlayer())
				: null;
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public void beginAimClient() {
		if (this.state.isError()) {
			return;
		}
		new LivingEntitySelectOverlay(this.radius, true,
				entity -> !entity.getUniqueID().equals(Minecraft.getInstance().player.getUniqueID()));
		LivingEntitySelectOverlay.getInstance().activate();
		if (!LivingEntitySelectOverlay.getInstance().isActive()) {
			LOG.warn("Can't create overlay, trick is failed now.");
			this.state = TrickStates.OVERLAY_ERROR;
			LivingEntitySelectOverlay.getInstance().deactivate();
		} else {
			this.state = TrickStates.AIMING; // Aiming
			WingxKey.SWAP.setTrick(this);
		}
	}

	@Override
	public void beginAimServer() {
		// TODO check if swap can be performed
		this.radius = 10.0d; // TODO parametrize
		this.state = TrickStates.OK;
	}

	/**
	 * Called whenever player decides aim is done
	 */
	@Override
	@OnlyIn(value = Dist.CLIENT)
	public void endAim() {
		if (this.state.isError()) {
			return;
		}
		if ((LivingEntitySelectOverlay.getInstance() == null) || !LivingEntitySelectOverlay.getInstance().isActive()) {
			LOG.warn("Can't aim: overlay is not active.");
			this.state = TrickStates.OVERLAY_ERROR;
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
		this.state = TrickStates.AIMED;
		return;
	}

	public static class TrickType extends AbstractTargetedPlayerTrick.TrickType<SwapPlayerTrick>
			implements IAimingTrick.TrickType<SwapPlayerTrick> {

		@Override
		public SwapPlayerTrick writeToBuf(SwapPlayerTrick trick, PacketBuffer buf) {
			buf.writeDouble(trick.radius);
			return super.writeToBuf(trick, buf);
		}

		@Override
		protected SwapPlayerTrick readFromBufInternal(SwapPlayerTrick trick, PacketBuffer buf) {
			trick.radius = buf.readDouble();
			return super.readFromBufInternal(trick, buf);
		}

		@Override
		public SwapPlayerTrick newEmpty() {
			return new SwapPlayerTrick(null);
		}

	}

	@SuppressWarnings("unchecked")
	@Override
	public SwapPlayerTrick.TrickType getType() {
		return Tricks.SWAP_TRICK.get();
	}
}
