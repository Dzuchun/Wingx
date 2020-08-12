package dzuchun.wingx.trick;

import javax.annotation.Nullable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dzuchun.wingx.Wingx;
import dzuchun.wingx.util.Facing;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.PacketDistributor.PacketTarget;

public class DashPlayerTrick extends AbstractPlayerCastedTrick {
	private static final ResourceLocation REGISTRY_NAME = new ResourceLocation(Wingx.MOD_ID, "dash_player_trick");
	private static final Logger LOG = LogManager.getLogger();

	private Facing facing = null;
	private double strength = 0.0d;
	private boolean nullifiesSpeed = false;

	public DashPlayerTrick() {
		super();
	}

	/**
	 * @param caster         Player that casts dash.
	 * @param facing         Facing of cast.
	 * @param strength        Speed modification absolute value.
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
	public void execute(LogicalSide side) {
		if (side == LogicalSide.SERVER) {
			assertHasCaster(this);
			if (hasCasterPlayer()) {
				PlayerEntity caster = getCasterPlayer();
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
			} else {
				LOG.warn("No caster found");
				this.succesfull = false;
			}
		} else if (amICaster()) {
			Minecraft minecraft = Minecraft.getInstance();
			if (this.succesfull) {
				minecraft.player.sendStatusMessage(new TranslationTextComponent("wingx.trick.dash.success")
						.func_230530_a_(Style.EMPTY.setFormatting(TextFormatting.AQUA)), true);
			} else {
				minecraft.player.sendStatusMessage(new TranslationTextComponent("wingx.trick.dash.fail")
						// TODO add reason
						.func_230530_a_(Style.EMPTY.setFormatting(TextFormatting.DARK_RED)), true);
				// TODO specify reason
			}
		}
	}

	@Override
	public ITrick readFromBuf(PacketBuffer buf) {
		this.facing = Facing.getByInt(buf.readInt());
		this.strength = buf.readDouble();
		this.nullifiesSpeed = buf.readBoolean();
		return super.readFromBuf(buf);
	}

	@Override
	public ITrick writeToBuf(PacketBuffer buf) {
		buf.writeInt(this.facing.toInt());
		buf.writeDouble(this.strength);
		buf.writeBoolean(this.nullifiesSpeed);
		return super.writeToBuf(buf);
	}

	@Override
	public PacketTarget getBackPacketTarget() {
		return hasCasterPlayer() ? PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> getCasterPlayer()) : null;
	}

	@Override
	public ITrick newEmpty() {
		return new DashPlayerTrick();
	}

	@Override
	protected void setRegistryName() {
		this.registryName = REGISTRY_NAME;
	}

}
