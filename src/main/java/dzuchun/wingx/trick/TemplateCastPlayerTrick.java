package dzuchun.wingx.trick;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dzuchun.wingx.init.Tricks;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.PacketDistributor.PacketTarget;

/**
 * Class used mostly for debug.
 *
 * @author Dzuchun
 *
 */
public class TemplateCastPlayerTrick extends AbstractInterruptablePlayerTrick implements ITimeredTrick {
	@SuppressWarnings("unused")
	private static final Logger LOG = LogManager.getLogger();

	public TemplateCastPlayerTrick(PlayerEntity caster, int duration) {
		super(caster, duration, InterruptCondition.NO_CONDITION);
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public void executeClient() {
		super.executeClient();
		if (this.status != 0) {
			Minecraft minecraft = Minecraft.getInstance();
			minecraft.player.sendStatusMessage(new TranslationTextComponent("wingx.trick.interrubtable.template.fail")
					.setStyle(Style.EMPTY.setFormatting(TextFormatting.GRAY)), true);
		}
	}

	@Override
	public void executeServer() {
		if (this.hasCasterPlayer() && (AbstractInterruptablePlayerTrick.playerBusyFor(this.getCasterPlayer()) == 0)) {
			this.status = 0;
		} else {
			this.status = 1;
		}
		super.executeServer();
	}

	@Override
	public void onTrickEndCommon() throws NoCasterException {
		this.status = 3;
		super.onTrickEndCommon();
	}

	@Override
	public PacketTarget getBackPacketTarget() {
		return this.hasCasterPlayer() ? PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) this.getCasterPlayer())
				: null;
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

	public static class TrickType extends AbstractInterruptablePlayerTrick.TrickType<TemplateCastPlayerTrick> {

		@Override
		public TemplateCastPlayerTrick newEmpty() {
			return new TemplateCastPlayerTrick(null, 0);
		}

	}

	@SuppressWarnings("unchecked")
	@Override
	public TemplateCastPlayerTrick.TrickType getType() {
		return Tricks.TEMPLATE_CAST_TRICK.get();
	}

}
