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
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.PacketDistributor.PacketTarget;

public class PunchPlayerTrick extends TargetedPlayerTrick {
	private static final ResourceLocation REGISTRY_NAME = new ResourceLocation(Wingx.MOD_ID, "punch_player_trick");
	private static final Logger LOG = LogManager.getLogger();
	private State state;

	public State getState() {
		return this.state;
	}

	private Vector3d direction = Vector3d.ZERO;
	private double force = 0.0d;
	private boolean successfull = false;

	public PunchPlayerTrick() {
		super();
		setRegistryName(REGISTRY_NAME);
	}

	public PunchPlayerTrick(PlayerEntity caster, double aimRadius, Predicate<LivingEntity> otherCondition,
			double force) {
		super(null, caster);
		setRegistryName(REGISTRY_NAME);
		this.force = force;
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
		if (side == LogicalSide.SERVER) {
			Entity target = getTarget((ServerWorld) worldIn);
			if (target == null) {
				LOG.warn("No target found");
				this.successfull = false;
				return;
			}
			target.setMotion(target.getMotion().add(this.direction.scale(this.force)));
			target.velocityChanged = true;
			this.successfull = true;
		} else {
			Minecraft minecraft = Minecraft.getInstance();
			if (amICaster()) {
				if (this.successfull) {
					minecraft.player.sendStatusMessage(new TranslationTextComponent("wingx.punch.success")
							.func_230530_a_(Style.field_240709_b_.func_240712_a_(TextFormatting.YELLOW)), true);
				} else {
					minecraft.player.sendStatusMessage(new TranslationTextComponent("wingx.punch.fail")
							.func_230530_a_(Style.field_240709_b_.func_240712_a_(TextFormatting.GRAY)), true);
				}
			}
		}
	}

	/**
	 * Called whenever player decides aim is done
	 */
	@SuppressWarnings("resource")
	@OnlyIn(value = Dist.CLIENT)
	public boolean aimed() {
		if (LivingEntitySelectOverlay.getInstance() == null || !LivingEntitySelectOverlay.getInstance().isActive()) {
			LOG.warn("Can't aim: overlay is not active.");
		}
		LivingEntity target = LivingEntitySelectOverlay.getInstance().deactivate();
		if (target == null) {
			LOG.debug("No entity aimed");
			this.state = State.FAILED;
			return false;
		}
		setTarget(target);
		if (getCasterUniUuid() == null) {
			LOG.warn("No caster found, so punch will be empty");
		} else {
			this.direction = Minecraft.getInstance().player.getForward().normalize();
		}
		this.state = State.WAIT_FOR_EXECUTION;
		return true;
	}

	@Override
	public boolean executedSuccesfully() {
		return this.successfull;
	}

	@Override
	public PacketTarget getBackPacketTarget(World worldIn) {
		return hasCaster(worldIn) ? PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> getCaster(worldIn)) : null;
	}

	@Override
	public ITrick readFromBuf(PacketBuffer buf) {
		this.force = buf.readDouble();
		this.direction = new Vector3d(buf.readDouble(), buf.readDouble(), buf.readDouble());
		this.successfull = buf.readBoolean();
		return super.readFromBuf(buf);
	}

	@Override
	public ITrick writeToBuf(PacketBuffer buf) {
		buf.writeDouble(this.force);
		buf.writeDouble(this.direction.x);
		buf.writeDouble(this.direction.y);
		buf.writeDouble(this.direction.z);
		buf.writeBoolean(this.successfull);
		LOG.debug("Writing to buffer {}", this.successfull);
		return super.writeToBuf(buf);
	}

	public enum State {
		AIMING, WAIT_FOR_EXECUTION, FAILED
	}
}
