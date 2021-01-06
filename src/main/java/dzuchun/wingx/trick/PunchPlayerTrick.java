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
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.PacketDistributor.PacketTarget;

public class PunchPlayerTrick extends AbstractTargetedPlayerTrick {
	private static final ResourceLocation REGISTRY_NAME = new ResourceLocation(Wingx.MOD_ID, "punch_player_trick");
	private static final Logger LOG = LogManager.getLogger();
	@Deprecated // TODO remove, replace with status
	private State state;

	public State getState() {
		return this.state;
	}

	private Vector3d direction = Vector3d.ZERO;
	private double force = 0.0d;

	public PunchPlayerTrick() {
		super();
	}

	public PunchPlayerTrick(PlayerEntity caster, double aimRadius, Predicate<LivingEntity> otherCondition,
			double force) {
		super(null, caster);
		this.force = force;
		new LivingEntitySelectOverlay(aimRadius, true, otherCondition);
		LivingEntitySelectOverlay.getInstance().activate();
		if (!LivingEntitySelectOverlay.getInstance().isActive()) {
			LOG.warn("Can't create overlay now, trick is failed now.");
			this.state = State.FAILED;
			LivingEntitySelectOverlay.getInstance().deactivate(); // TODO optimize
		} else {
			this.state = State.AIMING;
		}
	}

	@Override
	public void execute(LogicalSide side) {
		if (side == LogicalSide.SERVER) {
			Entity target = this.getTarget();
			// TODO add caster check
			if (target == null) {
				LOG.warn("No target found");
				this.status = 1;
				return;
			}
			target.setMotion(target.getMotion().add(this.direction.scale(this.force)));
			target.velocityChanged = true;
			this.status = 0;
		} else {
			Minecraft minecraft = Minecraft.getInstance();
			if (this.amICaster() && (this.status == 0)) {
				minecraft.player.sendStatusMessage(new TranslationTextComponent("wingx.trick.punch.success")
						.setStyle(Style.EMPTY.setFormatting(TextFormatting.YELLOW)), true);
			}
		}
	}

	/**
	 * Called whenever player decides aim is done
	 */
	@SuppressWarnings("resource")
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
		if (this.casterUniqueId == null) {
			LOG.warn("No caster found, so punch will be empty");
		} else {
			this.direction = Minecraft.getInstance().player.getForward().normalize();
		}
		this.state = State.WAIT_FOR_EXECUTION;
		return true;
	}

	@Override
	public PacketTarget getBackPacketTarget() {
		return this.hasCasterPlayer() ? PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> this.getCasterPlayer())
				: null;
	}

	@Override
	public ITrick readFromBuf(PacketBuffer buf) {
		this.force = buf.readDouble();
		this.direction = new Vector3d(buf.readDouble(), buf.readDouble(), buf.readDouble());
		return super.readFromBuf(buf);
	}

	@Override
	public ITrick writeToBuf(PacketBuffer buf) {
		buf.writeDouble(this.force);
		buf.writeDouble(this.direction.x);
		buf.writeDouble(this.direction.y);
		buf.writeDouble(this.direction.z);
		return super.writeToBuf(buf);
	}

	public enum State {
		AIMING, WAIT_FOR_EXECUTION, FAILED
	}

	@Override
	public ITrick newEmpty() {
		return new PunchPlayerTrick();
	}

	@Override
	protected void setRegistryName() {
		this.registryName = REGISTRY_NAME;
	}

	private static final ImmutableList<ITextComponent> MESSAGES = ImmutableList.of(
			new TranslationTextComponent("wingx.trick.punch.success").setStyle(SUCCESS_STYLE),
			new TranslationTextComponent("wingx.trick.punch.error",
					new TranslationTextComponent("wingx.trick.error_reason.no_target")).setStyle(ERROR_STYLE));

	@Override
	protected ImmutableList<ITextComponent> getMessages() {
		return MESSAGES;
	}

}
