package dzuchun.wingx.trick;

import javax.annotation.Nullable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dzuchun.wingx.init.Tricks;
import dzuchun.wingx.trick.state.TrickStates;
import dzuchun.wingx.util.Facing;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.PacketDistributor.PacketTarget;

public class DashPlayerTrick extends AbstractPlayerCastedTrick {
	private static final Logger LOG = LogManager.getLogger();

	private Facing facing = null;
	private double strength = 0.0d;
	private boolean nullifiesSpeed = false;

	/**
	 * @param caster         Player that casts dash.
	 * @param facing         Facing of cast.
	 * @param strength       Speed modification absolute value.
	 * @param nullifiesSpeed Defines if speed should be nullified before dash.
	 */
	public DashPlayerTrick(PlayerEntity caster, @Nullable Facing facing, double strength, boolean nullifiesSpeed) {
		super(caster);
		if (facing != null) {
			this.facing = facing;
		} else {
			this.facing = Facing.FORWARD;
		}
		this.strength = strength;
		this.nullifiesSpeed = nullifiesSpeed;
	}

	@Override
	public void executeServer() {
		super.executeServer();
		// We are on server
		if (this.state.isError()) {
			return;
		}
		assertHasCaster(this);
		if (this.hasCasterPlayer()) {
			PlayerEntity caster = this.getCasterPlayer();
			caster.fallDistance = 0.0f;
			Vector3d motionChange = caster.getForward().scale(this.strength);
			motionChange = this.facing.transform(motionChange);
			if (!this.nullifiesSpeed) {
				motionChange = motionChange.add(caster.getMotion());
			}
			caster.velocityChanged = true;
			caster.setMotion(motionChange.x, motionChange.y, motionChange.z);
			this.casterWorld.playSound(caster, caster.getPosX(), caster.getPosY(), caster.getPosZ(),
					SoundEvents.ENTITY_ENDER_DRAGON_FLAP, SoundCategory.PLAYERS, 1.0f, 1.0f);
			this.state = TrickStates.SUCCESS;
		} else {
			LOG.warn("No caster found");
			this.state = TrickStates.NO_CASTER;
		}
	}

	@Override
	public PacketTarget getBackPacketTarget() {
		return this.hasCasterPlayer() ? PacketDistributor.TRACKING_ENTITY_AND_SELF.with(this::getCasterPlayer) : null;
	}

	public static class TrickType extends AbstractPlayerCastedTrick.TrickType<DashPlayerTrick> {

		@Override
		protected DashPlayerTrick readFromBufInternal(DashPlayerTrick trick, PacketBuffer buf) {
			trick.facing = Facing.getByInt(buf.readInt());
			trick.strength = buf.readDouble();
			trick.nullifiesSpeed = buf.readBoolean();
			return super.readFromBufInternal(trick, buf);
		}

		@Override
		public DashPlayerTrick writeToBuf(DashPlayerTrick trick, PacketBuffer buf) {
			buf.writeInt(trick.facing.toInt());
			buf.writeDouble(trick.strength);
			buf.writeBoolean(trick.nullifiesSpeed);
			return super.writeToBuf(trick, buf);
		}

		@Override
		public DashPlayerTrick newEmpty() {
			return new DashPlayerTrick(null, null, 0.0d, false);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public DashPlayerTrick.TrickType getType() {
		return Tricks.DASH_TRICK.get();
	}

}
