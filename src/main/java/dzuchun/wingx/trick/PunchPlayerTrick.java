package dzuchun.wingx.trick;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.ImmutableList;

import dzuchun.wingx.capability.entity.wings.storage.PunchData;
import dzuchun.wingx.capability.entity.wings.storage.Serializers;
import dzuchun.wingx.client.input.KeyEvents;
import dzuchun.wingx.client.render.overlay.LivingEntitySelectOverlay;
import dzuchun.wingx.init.Tricks;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
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
		if (this.status != 0) {
			return;
		}
		Entity target = this.getTarget();
		// TODO add caster check
		if (target == null) {
			LOG.warn("No target found");
			this.status = 3; // No target
			return;
		}
		target.setMotion(target.getMotion().add(this.direction.scale(this.data.force)));
		target.velocityChanged = true;
		this.status = 0; // Ok
	}

	@Override
	public void beginAimServer() {
		// TODO check if enough mana
		this.status = 0; // Ok
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public void beginAimClient() {
		if (this.status == 0) {
			new LivingEntitySelectOverlay(this.data.radius, true,
					entity -> !entity.getUniqueID().equals(Minecraft.getInstance().player.getUniqueID()));
			LivingEntitySelectOverlay.getInstance().activate();
			if (!LivingEntitySelectOverlay.getInstance().isActive()) {
				LOG.warn("Can't create overlay, trick is failed now.");
				this.status = 4; // Overlay error
				LivingEntitySelectOverlay.getInstance().deactivate(); // TODO optimize
			} else {
				this.status = 1; // Aiming
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
			this.status = 4;
			return;
		}
		LivingEntitySelectOverlay overlay = LivingEntitySelectOverlay.getInstance();
		if (overlay == null) {
			LOG.warn("Can't aim: overlay don't exist");
			this.status = 4; // Overalay error
			return;
		}
		overlay.deactivate();
		LivingEntity target = overlay.getSelectedEnttity();
		if (target == null) {
			LOG.debug("No entity aimed");
			this.status = 3; // No target
			return;
		}
		this.setTarget(target);
		if (this.casterUniqueId == null) {
			LOG.warn("No caster found, so punch will be empty");
			this.status = 5; // No caster;
			return;
		} else {
			this.direction = Minecraft.getInstance().player.getForward().normalize();
		}
		this.status = 0; // Aimed, trick ok
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

	private static final ImmutableList<ITextComponent> MESSAGES = ImmutableList.of(
			new TranslationTextComponent("wingx.trick.punch.success").setStyle(SUCCESS_STYLE),
			new TranslationTextComponent("wingx.trick.default_aiming").setStyle(NEUTRAL_STYLE),
			new TranslationTextComponent("wingx.trick.default_error").setStyle(ERROR_STYLE),
			new TranslationTextComponent("wingx.trick.punch.error",
					new TranslationTextComponent("wingx.trick.error_reason.no_target")).setStyle(ERROR_STYLE),
			new TranslationTextComponent("wingx.trick.punch.error",
					new TranslationTextComponent("wingx.trick.error_reason.overlay_unknown")).setStyle(ERROR_STYLE),
			new TranslationTextComponent("wingx.trick.punch.error",
					new TranslationTextComponent("wingx.trick.error_reason.no_caster")).setStyle(ERROR_STYLE));

	@Override
	protected ImmutableList<ITextComponent> getMessages() {
		return MESSAGES;
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
