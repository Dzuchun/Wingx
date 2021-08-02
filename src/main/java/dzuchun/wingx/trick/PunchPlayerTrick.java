package dzuchun.wingx.trick;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dzuchun.wingx.capability.entity.wings.storage.PunchData;
import dzuchun.wingx.capability.entity.wings.storage.Serializers;
import dzuchun.wingx.client.input.KeyEvents;
import dzuchun.wingx.client.render.overlay.LivingEntitySelectOverlay;
import dzuchun.wingx.init.Tricks;
import dzuchun.wingx.trick.state.TrickStates;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.PacketDistributor.PacketTarget;

public class PunchPlayerTrick extends AbstractTargetedPlayerTrick implements IAimingTrick {
	private static final Logger LOG = LogManager.getLogger();

	private Vector3d direction = Vector3d.ZERO;
	private PunchData data = new PunchData();

	@OnlyIn(Dist.CLIENT)
	public PunchPlayerTrick(PlayerEntity caster) {
		super(null, caster);
	}

	@Override
	public void executeServer() {
		super.executeServer();
		if (this.state.isError()) {
			return;
		}
		Entity target = this.getTarget();
		// TODO add caster check
		if (target == null) {
			LOG.warn("No target found");
			this.state = TrickStates.NO_TARGET; // No target
			return;
		}
		target.setMotion(target.getMotion().add(this.direction.scale(this.data.force)));
		target.velocityChanged = true;
		this.state = TrickStates.SUCCESS; // Ok
	}

	@Override
	public void beginAimServer() {
		// TODO check if enough mana
		this.state = TrickStates.OK;
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public void beginAimClient() {
		if (!this.state.isError()) {
			new LivingEntitySelectOverlay(this.data.radius, true,
					entity -> !entity.getUniqueID().equals(Minecraft.getInstance().player.getUniqueID()));
			LivingEntitySelectOverlay.getInstance().activate();
			if (!LivingEntitySelectOverlay.getInstance().isActive()) {
				LOG.warn("Can't create overlay, trick is failed now.");
				this.state = TrickStates.OVERLAY_ERROR;
				LivingEntitySelectOverlay.getInstance().deactivate(); // TODO optimize
			} else {
				this.state = TrickStates.AIMING; // Aiming
				KeyEvents.WingxKey.PUNCH.setTrick(this);
			}
		}
	}

	/**
	 * Called whenever player decides aim is done
	 */
	@SuppressWarnings("resource")
	@Override
	@OnlyIn(value = Dist.CLIENT)
	public void endAim() {
		if ((LivingEntitySelectOverlay.getInstance() == null) || !LivingEntitySelectOverlay.getInstance().isActive()) {
			LOG.warn("Can't aim: overlay is not active.");
			this.state = TrickStates.OVERLAY_ERROR;
			return;
		}
		LivingEntitySelectOverlay overlay = LivingEntitySelectOverlay.getInstance();
		if (overlay == null) {
			LOG.warn("Can't aim: overlay don't exist");
			this.state = TrickStates.OVERLAY_ERROR;
			return;
		}
		overlay.deactivate();
		LivingEntity target = overlay.getSelectedEnttity();
		if (target == null) {
			LOG.debug("No entity aimed");
			this.state = TrickStates.NO_TARGET;
			return;
		}
		this.setTarget(target);
		if (this.casterUniqueId == null) {
			LOG.warn("No caster found, so punch will be empty");
			this.state = TrickStates.NO_CASTER;
			return;
		} else {
			this.direction = Minecraft.getInstance().player.getForward().normalize();
		}
		this.state = TrickStates.AIMED;
	}

	@Override
	public PacketTarget getBackPacketTarget() {
		return this.hasCasterPlayer() ? PacketDistributor.TRACKING_ENTITY_AND_SELF.with(this::getCasterPlayer) : null;
	}

	@Override
	public PacketTarget getAimBackTarget() {
		return this.hasCasterPlayer() ? PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) this.getCasterPlayer())
				: null;
	}

	public static class TrickType extends AbstractTargetedPlayerTrick.TrickType<PunchPlayerTrick> {

		@Override
		protected PunchPlayerTrick readFromBufInternal(PunchPlayerTrick trick, PacketBuffer buf) {
			trick.data = Serializers.PUNCH_SERIALIZER.read(buf);
			trick.direction = new Vector3d(buf.readDouble(), buf.readDouble(), buf.readDouble());
			return super.readFromBufInternal(trick, buf);
		}

		@Override
		public PunchPlayerTrick writeToBuf(PunchPlayerTrick trick, PacketBuffer buf) {
			Serializers.PUNCH_SERIALIZER.write(buf, trick.data);
			buf.writeDouble(trick.direction.x);
			buf.writeDouble(trick.direction.y);
			buf.writeDouble(trick.direction.z);
			return super.writeToBuf(trick, buf);
		}

		@Override
		public PunchPlayerTrick newEmpty() {
			return new PunchPlayerTrick(null);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public PunchPlayerTrick.TrickType getType() {
		return Tricks.PUNCH_TRICK.get();
	}

}
