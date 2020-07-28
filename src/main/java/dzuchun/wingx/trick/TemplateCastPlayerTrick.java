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
import net.minecraft.world.World;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.PacketDistributor.PacketTarget;

/**
 * Class used mostly for debug.
 *
 * @author Dzuchun
 *
 */
public class TemplateCastPlayerTrick extends AbstractInterruptablePlayerTrick {
	private static final ResourceLocation REGISTRY_NAME = new ResourceLocation(Wingx.MOD_ID,
			"template_cast_player_trick");
	@SuppressWarnings("unused")
	private static final Logger LOG = LogManager.getLogger();

	public TemplateCastPlayerTrick() {
		super();
		setRegistryName(REGISTRY_NAME);
	}

	public TemplateCastPlayerTrick(PlayerEntity caster, int duration) {
		super(caster, duration, InterruptCondition.NO_CONDITION);
		setRegistryName(REGISTRY_NAME);
	}

	@Override
	public void execute(LogicalSide side, World worldIn) {
		if (side == LogicalSide.SERVER) {
			if (hasCaster(worldIn) && AbstractInterruptablePlayerTrick.playerBusy(getCaster(worldIn)) == 0) {
				beginCast(getCaster(worldIn));
				this.succesfull = true;
			} else {
				this.succesfull = false;
			}
		} else {
			if (this.succesfull && hasCaster(worldIn)) {
				beginCast(getCaster(worldIn));
			} else {
				Minecraft minecraft = Minecraft.getInstance();
				minecraft.player
						.sendStatusMessage(new TranslationTextComponent("wingx.trick.interrubtable.template.fail")
								.func_230530_a_(Style.EMPTY.setFormatting(TextFormatting.GRAY)), true);
			}
		}
	}

	@Override
	public PacketTarget getBackPacketTarget(World worldIn) {
		return hasCaster(worldIn) ? PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) getCaster(worldIn)) : null;
	}

	@Override
	public ITrick newEmpty() {
		return new TemplateCastPlayerTrick();
	}

//	@Override
//	public Consumer<RenderGameOverlayEvent> getDrawFunction() {
//		return (RenderGameOverlayEvent event) -> {
//			SeparateRenderers.renderColorScreen(event, FadingScreenOverlay.Color.BLACK);
//		};
//	}

}
