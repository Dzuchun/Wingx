package dzuchun.wingx.trick.meditation;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dzuchun.wingx.Wingx;
import dzuchun.wingx.capability.entity.wings.IWingsCapability;
import dzuchun.wingx.capability.entity.wings.WingsProvider;
import dzuchun.wingx.client.render.overlay.FadingScreenOverlay;
import dzuchun.wingx.net.MeditationGuiMessage;
import dzuchun.wingx.net.WingxPacketHandler;
import dzuchun.wingx.trick.AbstractInterruptablePlayerTrick;
import dzuchun.wingx.trick.ITrick;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.PacketDistributor.PacketTarget;

public class MeditationPlayerTrick extends AbstractInterruptablePlayerTrick {
	private static final Logger LOG = LogManager.getLogger();

	private static final ResourceLocation REGISTRY_NAME = new ResourceLocation(Wingx.MOD_ID, "meditation_player_trick");
	private static final int MEDITATION_DURATION = 100; // TODO parameterize
	private static final int MEDITATION_FAIL_DURATION = 10;

	public MeditationPlayerTrick() {
		super();
	}

	public MeditationPlayerTrick(PlayerEntity caster) {
		super(caster, MEDITATION_DURATION, InterruptCondition.MOVED_CONDITION);
	}

	@Override
	public PacketTarget getEndPacketTarget() {
		// TODO redefine, when animate!
		return hasCasterPlayer() ? PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) getCasterPlayer()) : null;
	}

	@Override
	public PacketTarget getBackPacketTarget() {
		return hasCasterPlayer() ? PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) getCasterPlayer()) : null;
	}

	@Override
	public ITrick newEmpty() {
		return new MeditationPlayerTrick();
	}

	@Override
	protected void setRegistryName() {
		this.registryName = REGISTRY_NAME;
	}

	@Override
	public void execute(LogicalSide side) {
		if (side == LogicalSide.SERVER) {
			if (hasCasterPlayer()) {
				PlayerEntity caster = getCasterPlayer();
				LazyOptional<IWingsCapability> optionalCap = caster.getCapability(WingsProvider.WINGS, null);
				if (optionalCap.isPresent()) {
					optionalCap.ifPresent((cap) -> {
						if (MeditationUtil.getMeditationScore(caster) >= cap.getMeditationScore()) {
							this.succesfull = true;
						} else {
							this.succesfull = false;
						}
					});
				} else {
					LOG.warn("Caster doesn't have wings capability. Meditation won't be performed.");
					this.succesfull = false;
				}
			} else {
				LOG.warn("No caster found. Meditation won't be performed.");
				this.succesfull = false;
			}
		} else {
			if (this.succesfull) {
				boolean b = new FadingScreenOverlay(FadingScreenOverlay.Color.ZERO, FadingScreenOverlay.Color.BLACK,
						MEDITATION_DURATION + 2).activate();
				if (!b) {
					LOG.warn("Could not activate overlay!!");
				}
			}
		}
		super.execute(side);
	}

	@Override
	public void onCastEnd(LogicalSide side) {
		if (side == LogicalSide.SERVER) {
			assertHasCaster(this);
			if (castEndedNaturally()) {
				LazyOptional<IWingsCapability> optionalCap = getCasterPlayer().getCapability(WingsProvider.WINGS, null);
				if (optionalCap.isPresent()) {
					optionalCap.ifPresent((cap) -> {
						LOG.info("Opening meditation gui");
						WingxPacketHandler.INSTANCE.send(
								PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> getCasterPlayer()),
								new MeditationGuiMessage(cap));
					});
				} else {
					LOG.warn("Caster does not have a wings capability");
				}
			}
		} else {
			Minecraft minecraft = Minecraft.getInstance();
			if (amICaster()) {
				if (castEndedNaturally()) {
					minecraft.player.sendStatusMessage(new TranslationTextComponent("wingx.trick.meditate.success")
							.func_230530_a_(Style.EMPTY.setFormatting(TextFormatting.BOLD)), true);
				} else {
					minecraft.player.sendStatusMessage(new TranslationTextComponent("wingx.trick.meditate.fail")
							.func_230530_a_(Style.EMPTY.setFormatting(TextFormatting.RED)), true);
				}
				if (!castEndedNaturally()) {
					FadingScreenOverlay overlay = FadingScreenOverlay.instance;
					if (overlay == null) {
						LOG.warn("There is no overlay, but cast ended unnaturaly");
					}
					overlay.deactivate();
					boolean res = new FadingScreenOverlay(overlay.getCurrentColor(), FadingScreenOverlay.Color.ZERO,
							MEDITATION_FAIL_DURATION).activate();
					if (!res) {
						LOG.warn("Could not activate fail overlay");
					}
				}
			}
		}
		super.onCastEnd(side);
	}
}