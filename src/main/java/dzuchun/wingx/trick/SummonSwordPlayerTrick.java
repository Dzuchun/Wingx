package dzuchun.wingx.trick;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.ImmutableList;

import dzuchun.wingx.Wingx;
import dzuchun.wingx.capability.entity.wings.IWingsCapability;
import dzuchun.wingx.capability.entity.wings.WingsProvider;
import dzuchun.wingx.capability.entity.wings.storage.Serializers;
import dzuchun.wingx.capability.entity.wings.storage.SoulswordData;
import dzuchun.wingx.client.render.overlay.AbstractOverlay;
import dzuchun.wingx.client.render.overlay.SoulswordOverlay;
import dzuchun.wingx.init.Items;
import dzuchun.wingx.item.Soulsword;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.Color;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.PacketDistributor.PacketTarget;

public class SummonSwordPlayerTrick extends AbstractInterruptablePlayerTrick implements ITimeredTrick {
	private static final ResourceLocation REGISTRY_NAME = new ResourceLocation(Wingx.MOD_ID,
			"summon_sword_player_trick");
	private static final Logger LOG = LogManager.getLogger();

	public SummonSwordPlayerTrick() {
		super();
	}

	public SummonSwordPlayerTrick(PlayerEntity caster) {
		super(caster, 0, InterruptCondition.CHANGED_ITEM_CONDITION);
		caster.getCapability(WingsProvider.WINGS, null).ifPresent((IWingsCapability wings) -> {
			SoulswordData data = wings.getDataManager().getOrAddDefault(Serializers.SOULSWORD_SERIALIZER);
			this.duration = data.summonDurationTicks;
			this.status = 0;
		});
	}

	@Override
	public PacketTarget getEndPacketTarget() {
		return this.hasCasterPlayer() ? PacketDistributor.TRACKING_ENTITY_AND_SELF.with(this::getCasterPlayer) : null;
	}

	@Override
	public PacketTarget getBackPacketTarget() {
		return this.hasCasterPlayer() ? PacketDistributor.TRACKING_ENTITY_AND_SELF.with(this::getCasterPlayer) : null;
	}

	@Override
	public ITrick newEmpty() {
		return new SummonSwordPlayerTrick();
	}

	@Override
	public int timeFull() throws NoCasterException {
		return this.duration;
	}

	@Override
	public double partLeft() throws NoCasterException {
		assertHasCaster(this);
		long time = this.casterWorld.getGameTime();
		return ((double) (this.endTime - time)) / (this.duration);
	}

	@Override
	protected void setRegistryName() {
		this.registryName = REGISTRY_NAME;
	}

//	@Override
//	protected Consumer<RenderGameOverlayEvent> getDrawFunction() {
//		// TODO probably should override draw function
//		return super.getDrawFunction();
//	}

	@Override
	public void execute(LogicalSide side) {
		if (side == LogicalSide.SERVER) {
			if (this.hasCasterPlayer() && this.getCasterPlayer().getCapability(WingsProvider.WINGS, null).isPresent()) {
				@SuppressWarnings("unused")
				IWingsCapability cap = this.getCasterPlayer().getCapability(WingsProvider.WINGS, null).orElse(null);
				// Cap is nonnul
				PlayerEntity caster = this.getCasterPlayer();

				// TODO check if trick can be used here
				int busy = AbstractInterruptablePlayerTrick.playerBusyFor(caster);
				if (busy != 0) {
					LOG.warn("Found that caster {} is busy for {} more tick, trick {} won't be casted", caster, busy,
							this);
					this.status = 2; // Cant be casted - player busy (?)
					return;
				}
				if (!caster.getHeldItemMainhand().isEmpty()) {
					this.status = 1; // Cant be casted - item held
					return;
				}
				// TODO some visual stuff
				ItemStack stack = Items.SOULSWORD.get().getDefaultInstance();
				stack.getOrCreateTag().putBoolean(Soulsword.SUMMONED_TAG, false);
				caster.setItemStackToSlot(EquipmentSlotType.MAINHAND, stack);
//				//Trick succesfull
			}
		} else {
			if (this.hasCasterPlayer() && this.getCasterPlayer().getCapability(WingsProvider.WINGS, null).isPresent()) {
				ClientPlayerEntity caster = (ClientPlayerEntity) this.getCasterPlayer();
				SoulswordOverlay overlay = new SoulswordOverlay(caster);
				if (!overlay.activate()) {
//					status = 5; // Some unknown shit
					LOG.warn("Can't activate overlay: {}", overlay);
				}
			}
		}
		super.execute(side);
	}

	private static final Style EPIC_STYLE = Style.EMPTY.setColor(Color.fromInt(0xFFAA00AA));

	private static final ImmutableList<ITextComponent> MESSAGES = ImmutableList.of(
//			new TranslationTextComponent("wingx.trick.default_success").setStyle(SUCCESS_STYLE),
			new TranslationTextComponent("wingx.trick.summon_soulsword.start").setStyle(PROC_STYLE),
			new TranslationTextComponent("wingx.trick.summon_soulsword.error",
					new TranslationTextComponent("wingx.trick.error_reason.hand_busy")).setStyle(ERROR_STYLE),
			new TranslationTextComponent("wingx.trick.summon_soulsword.error",
					new TranslationTextComponent("wingx.trick.error_reason.caster_busy")).setStyle(ERROR_STYLE),
			new TranslationTextComponent("wingx.trick.summon_soulsword.error",
					new TranslationTextComponent("wingx.trick.error_reason.interrupt_cast")).setStyle(ERROR_STYLE),
			new TranslationTextComponent("wingx.trick.summon_soulsword.error",
					new TranslationTextComponent("wingx.trick.error_reason.overlay_unknown")).setStyle(ERROR_STYLE),
			new TranslationTextComponent("wingx.trick.summon_soulsword.success").setStyle(EPIC_STYLE));

	@Override
	protected ImmutableList<ITextComponent> getMessages() {
		return MESSAGES;
	}

	@Override
	public void onCastEnd(LogicalSide side) {
		super.onCastEnd(side);
		if (side == LogicalSide.SERVER) {
			PlayerEntity caster = this.getCasterPlayer();
			if (caster == null) {
				this.status = 4;
			}
			if (!this.castEndedNaturally()) {
				this.status = 3;
			} else if (this.status == 0) {
				ItemStack stack = caster.getHeldItemMainhand();
				if (stack.isItemEqual(Items.SOULSWORD.get().getDefaultInstance())) {
					stack.getOrCreateTag().putBoolean(Soulsword.SUMMONED_TAG, true);
				}
				this.status = 5;
			}
		} else {
			final ClientPlayerEntity caster = (ClientPlayerEntity) this.getCasterPlayer();
			AbstractOverlay.getActiveOverlays().stream()
					.filter(over -> (over instanceof SoulswordOverlay) && (((SoulswordOverlay) over).caster == caster))
					.forEach(over -> ((SoulswordOverlay) over).markSummoned());
			if (this.castEndedNaturally()) {
				// TODO perform some sounds / effects on caster
			}

		}
	}

}
