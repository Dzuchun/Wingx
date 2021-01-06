package dzuchun.wingx.trick.meditation;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.ImmutableList;

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
import net.minecraft.util.text.ITextComponent;
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
		return this.hasCasterPlayer() ? PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) this.getCasterPlayer())
				: null;
	}

	@Override
	public PacketTarget getBackPacketTarget() {
		return this.hasCasterPlayer() ? PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) this.getCasterPlayer())
				: null;
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
			if (this.hasCasterPlayer()) {
				PlayerEntity caster = this.getCasterPlayer();
				LazyOptional<IWingsCapability> optionalCap = caster.getCapability(WingsProvider.WINGS, null);
				if (optionalCap.isPresent()) {
					optionalCap.ifPresent(cap -> {
						BasicData data = cap.getDataManager().getOrAddDefault(Serializers.BASIC_SERIALIZER);
						if (data.needsEnd && (caster.world.getDimensionKey() != World.THE_END)) {
							LOG.debug("Player requires end to meditate, but is not in end now.");
							this.status = 1;
							return;
						}

						if (MeditationUtil.getMeditationScore(caster) <= data.requiredMeditationScore) {
							this.status = 2;
							LOG.debug("Player has not enough meditation points to perform meditation.");
							return;
						}
						this.tmp_boolean_1 = true;
						caster.world.getCapability(ActiveTricksProvider.ACTIVE_TRICKS, null).ifPresent((worldCap) -> {
							worldCap.getActiveTricks().forEach((trick) -> {
								if (trick instanceof ICastedTrick) {
									if ((trick instanceof MeditationPlayerTrick)
											&& trick.getCaster().getUniqueID().equals(caster.getUniqueID())) {
										this.tmp_boolean_1 = false;
									}
								}
							});
						});
						if (!this.tmp_boolean_1) {
							this.status = 3;
							return;
						}
						this.status = 0;
					});
				} else {
					LOG.warn("Caster doesn't have wings capability. Meditation won't be performed.");
					this.status = 4;
				}
			} else {
				LOG.warn("No caster found. Meditation won't be performed.");
				this.status = 5;
			}
		} else {
			Minecraft minecraft = Minecraft.getInstance();
			if (this.status == 0) {
				if (FadingScreenOverlay.instance != null) {
					FadingScreenOverlay.instance.deactivate();
				}
				boolean b = new FadingScreenOverlay(FadingScreenOverlay.Color.ZERO, FadingScreenOverlay.Color.BLACK,
						MEDITATION_DURATION + 2).activate();
				if (!b) {
					LOG.warn("Could not activate overlay!!");
				}
				minecraft.player.sendStatusMessage(new TranslationTextComponent("wingx.trick.meditate.start")
						.setStyle(Style.EMPTY.setFormatting(TextFormatting.DARK_GREEN)), true);
			}
		}
		super.execute(side);
	}

	@Override
	public void onCastEnd(LogicalSide side) {
		if (side == LogicalSide.SERVER) {
			// We are on server
			assertHasCaster(this);
			if (this.castEndedNaturally()) {
				LazyOptional<IWingsCapability> optionalCap = this.getCasterPlayer().getCapability(WingsProvider.WINGS,
						null);
				if (optionalCap.isPresent()) {
					optionalCap.ifPresent(cap -> {
						LOG.info("Opening meditation gui");
						WingxPacketHandler.INSTANCE.send(
								PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> this.getCasterPlayer()),
								new MeditationGuiMessage(cap));
					});
				} else {
					LOG.warn("Caster does not have a wings capability");
				}
			}
		} else {
			// We are on client
			if (this.amICaster()) {
				if (!this.castEndedNaturally()) {
					FadingScreenOverlay overlay = FadingScreenOverlay.instance;
					if (overlay == null) {
						LOG.warn("There is no overlay, but cast ended unnaturaly");
					} else {
						overlay.deactivate();
						boolean res = new FadingScreenOverlay(overlay.getCurrentColor(), FadingScreenOverlay.Color.ZERO,
								MEDITATION_FAIL_DURATION).activate();
						if (!res) {
							LOG.warn("Could not activate fail overlay");
						}
					}
					this.status = 6;
				} else {
					this.status = 7;
				}
			}
		}
		super.onCastEnd(side);
	}

	private static ImmutableList<ITextComponent> MESSAGES = ImmutableList.of(
			new TranslationTextComponent("wingx.trick.meditation.start").setStyle(SUCCESS_STYLE),
			new TranslationTextComponent("wingx.trick.meditation.error",
					new TranslationTextComponent("wingx.trick.error_reason.not_in_end")).setStyle(ERROR_STYLE),
			new TranslationTextComponent("wingx.trick.meditation.error",
					new TranslationTextComponent("wingx.trick.error_reason.not_enough_meditation_points"))
							.setStyle(ERROR_STYLE),
			new TranslationTextComponent("wingx.trick.meditation.error",
					new TranslationTextComponent("wingx.trick.error_reason.already_meditating")).setStyle(ERROR_STYLE),
			new TranslationTextComponent("wingx.trick.meditation.error",
					new TranslationTextComponent("wingx.trick.error_reason.no_wings")).setStyle(ERROR_STYLE),
			new TranslationTextComponent("wingx.trick.meditation.error",
					new TranslationTextComponent("wingx.trick.error_reason.no_caster")).setStyle(ERROR_STYLE),
			new TranslationTextComponent("wingx.trick.meditation.interrupt").setStyle(ERROR_STYLE),
			new TranslationTextComponent("wings.trick.meditation.success").setStyle(SUCCESS_STYLE));

	@Override
	protected ImmutableList<ITextComponent> getMessages() {
		return MESSAGES;
	}
}