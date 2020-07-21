package dzuchun.wingx.trick;

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
import net.minecraft.world.World;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.PacketDistributor.PacketTarget;

public class DashPlayerTrick extends PlayerTrick {
	private static final ResourceLocation REGISTRY_NAME = new ResourceLocation(Wingx.MOD_ID, "dash_player_trick");
	private static final Logger LOG = LogManager.getLogger();

	private Facing facing = null;
	private double strength = 0.0d;
	private boolean nullifiesSpeed = false;
	private boolean succesfull = true;

	public DashPlayerTrick() {
		super();
		setRegistryName(REGISTRY_NAME);
	}

	/**
	 * @param caster         Player that casts dash.
	 * @param facing         Facing of cast.
	 * @param strenth        Speed modification absolute value.
	 * @param nullifiesSpeed Defines if speed should be nullified before dash.
	 */
	public DashPlayerTrick(PlayerEntity caster, Facing facing, double strength, boolean nullifiesSpeed) {
		super(caster);
		setRegistryName(REGISTRY_NAME);
		if (facing != null) {
			this.facing = facing;
		} else {
			this.facing = Facing.FORWARD;
		}
		this.strength = strength;
		this.nullifiesSpeed = nullifiesSpeed;
	}

	@Override
	public void execute(LogicalSide side, World worldIn) {
		if (this.succesfull || side == LogicalSide.SERVER) {
			if (hasCaster(worldIn)) {
				PlayerEntity caster = getCaster(worldIn);
				caster.fallDistance = 0.0f;
				Vector3d motionChange = caster.getForward().scale(this.strength);
				motionChange = this.facing.transform(motionChange);
				if (!this.nullifiesSpeed) {
					motionChange = motionChange.add(caster.getMotion());
				}
				caster.velocityChanged = true;
				caster.setMotion(motionChange.x, motionChange.y, motionChange.z);
				if (side == LogicalSide.CLIENT && Minecraft.getInstance().player.equals(caster)) {
					Minecraft minecraft = Minecraft.getInstance();
					if (this.succesfull) {
						minecraft.player.sendStatusMessage(new TranslationTextComponent("dash.success")
								.func_230530_a_(Style.field_240709_b_.func_240712_a_(TextFormatting.AQUA)), true);
						worldIn.playSound(caster, caster.getPosX(), caster.getPosY(), caster.getPosZ(),
								SoundEvents.ENTITY_ENDER_DRAGON_FLAP, SoundCategory.PLAYERS, 1.0f, 1.0f);
					} else {
						minecraft.player.sendStatusMessage(new TranslationTextComponent("dash.fail")
								.func_230530_a_(Style.field_240709_b_.func_240712_a_(TextFormatting.DARK_RED)), true);
						// TODO specify reason
					}
				}
			} else {
				LOG.warn("No caster found");
			}
		}
	}

	@Override
	public ITrick readFromBuf(PacketBuffer buf) {
		this.facing = Facing.getByInt(buf.readInt());
		this.strength = buf.readDouble();
		this.nullifiesSpeed = buf.readBoolean();
		this.succesfull = buf.readBoolean();

		return super.readFromBuf(buf);
	}

	@Override
	public ITrick writeToBuf(PacketBuffer buf) {
		buf.writeInt(this.facing.toInt());
		buf.writeDouble(this.strength);
		buf.writeBoolean(this.nullifiesSpeed);
		buf.writeBoolean(this.succesfull);

		return super.writeToBuf(buf);
	}

	@Override
	public boolean executedSuccesfully() {
		return this.succesfull;
	}

	@Override
	public PacketTarget getBackPacketTarget(World worldIn) {
		return hasCaster(worldIn) ? PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> getCaster(worldIn)) : null;
	}

}
