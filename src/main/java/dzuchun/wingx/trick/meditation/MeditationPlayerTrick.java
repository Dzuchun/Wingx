package dzuchun.wingx.trick.meditation;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dzuchun.wingx.Wingx;
import dzuchun.wingx.capability.entity.wings.IWingsCapability;
import dzuchun.wingx.capability.entity.wings.WingsProvider;
import dzuchun.wingx.capability.entity.wings.storage.BasicData;
import dzuchun.wingx.capability.entity.wings.storage.Serializers;
import dzuchun.wingx.capability.world.tricks.ActiveTricksProvider;
import dzuchun.wingx.client.render.overlay.FadingScreenOverlay;
import dzuchun.wingx.net.MeditationGuiMessage;
import dzuchun.wingx.net.WingxPacketHandler;
import dzuchun.wingx.trick.AbstractInterruptablePlayerTrick;
import dzuchun.wingx.trick.ICastedTrick;
import dzuchun.wingx.trick.ITrick;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
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

	private boolean tmp_boolean_1;

	@Override
	public void execute(LogicalSide side) {
		if (side == LogicalSide.SERVER) {
			if (hasCasterPlayer()) {
				PlayerEntity caster = getCasterPlayer();
				LazyOptional<IWingsCapability> optionalCap = caster.getCapability(WingsProvider.WINGS, null);
				if (optionalCap.isPresent()) {
					optionalCap.ifPresent((cap) -> {
						BasicData data = cap.getDataManager().getOrAddDefault(Serializers.BASIC_SERIALIZER);
						if (data.needsEnd && (caster.world.func_234923_W_() != World.field_234920_i_)) {
							LOG.debug("Player requires end to meditate, but is not in end now.");
							this.succesfull = false;
							return;
						}

						if (MeditationUtil.getMeditationScore(caster) <= data.requiredMeditationScore) {
							this.succesfull = false;
							LOG.debug("Player has not enough meditation points to perform meditation.");
							return;
						}
						this.tmp_boolean_1 = true;
						caster.world.getCapability(ActiveTricksProvider.ACTIVE_TRICKS, null).ifPresent((worldCap) -> {
							worldCap.getActiveTricks().forEach((trick) -> {
								if (trick instanceof ICastedTrick) {
									if (trick instanceof MeditationPlayerTrick
											&& trick.getCaster().getUniqueID().equals(caster.getUniqueID())) {
										this.tmp_boolean_1 = false;
									}
								}
							});
						});
						if (!this.tmp_boolean_1) {
							this.succesfull = false;
							return;
						}
						this.succesfull = true;
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
			Minecraft minecraft = Minecraft.getInstance();
			if (this.succesfull) {
				if (FadingScreenOverlay.instance != null) {
					FadingScreenOverlay.instance.deactivate();
				}
				boolean b = new FadingScreenOverlay(FadingScreenOverlay.Color.ZERO, FadingScreenOverlay.Color.BLACK,
						MEDITATION_DURATION + 2).activate();
				if (!b) {
					LOG.warn("Could not activate overlay!!");
				}
				minecraft.player.sendStatusMessage(new TranslationTextComponent("wingx.trick.meditate.start")
						.func_230530_a_(Style.EMPTY.setFormatting(TextFormatting.DARK_GREEN)), true);
			} else {
				minecraft.player.sendStatusMessage(new TranslationTextComponent("wingx.trick.meditate.fail")
						.func_230530_a_(Style.EMPTY.setFormatting(TextFormatting.RED)), true);
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
					minecraft.player.sendStatusMessage(new TranslationTextComponent("wingx.trick.meditate.interrupted")
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