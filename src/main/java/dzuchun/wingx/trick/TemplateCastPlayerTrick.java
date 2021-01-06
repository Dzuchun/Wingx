package dzuchun.wingx.trick;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dzuchun.wingx.Wingx;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.PacketDistributor.PacketTarget;

/**
 * Class used mostly for debug.
 *
 * @author Dzuchun
 *
 */
public class TemplateCastPlayerTrick extends AbstractInterruptablePlayerTrick implements ITimeredTrick {
	private static final ResourceLocation REGISTRY_NAME = new ResourceLocation(Wingx.MOD_ID,
			"template_cast_player_trick");
	@SuppressWarnings("unused")
	private static final Logger LOG = LogManager.getLogger();

	public TemplateCastPlayerTrick() {
		super();
	}

	public TemplateCastPlayerTrick(PlayerEntity caster, int duration) {
		super(caster, duration, InterruptCondition.NO_CONDITION);
	}

	@Override
	public void execute(LogicalSide side) {
		if (side == LogicalSide.SERVER) {
			if (this.hasCasterPlayer()
					&& (AbstractInterruptablePlayerTrick.playerBusyFor(this.getCasterPlayer()) == 0)) {
				this.status = 0;
			} else {
				this.status = 1;
			}
		} else {
			if (this.status != 0) {
				Minecraft minecraft = Minecraft.getInstance();
				minecraft.player
						.sendStatusMessage(new TranslationTextComponent("wingx.trick.interrubtable.template.fail")
								.setStyle(Style.EMPTY.setFormatting(TextFormatting.GRAY)), true);
			}
		}
		super.execute(side);
	}

	@Override
	public void onCastEnd(LogicalSide side) {
		this.status = 3;
		super.onCastEnd(side);
	}

	@Override
	public PacketTarget getBackPacketTarget() {
		return this.hasCasterPlayer() ? PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) this.getCasterPlayer())
				: null;
	}

	@Override
	public ITrick newEmpty() {
		return new TemplateCastPlayerTrick();
	}

	@Override
	protected void setRegistryName() {
		this.registryName = REGISTRY_NAME;
	}

	@Override
	public PacketTarget getEndPacketTarget() {
		return this.hasCasterPlayer() ? PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) this.getCasterPlayer())
				: null;
	}

	@Override
	public int timeFull() throws NoCasterException {
		return 0;
	}

	@Override
	public double partLeft() throws NoCasterException {
		return (double) this.timeLeft() / (double) this.duration;
	}

}
