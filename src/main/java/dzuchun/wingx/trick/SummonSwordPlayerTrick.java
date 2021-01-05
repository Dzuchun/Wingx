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
			status = 0;
		});
	}

	@Override
	public PacketTarget getEndPacketTarget() {
		return hasCasterPlayer() ? PacketDistributor.TRACKING_ENTITY_AND_SELF.with(this::getCasterPlayer) : null;
	}

	@Override
	public PacketTarget getBackPacketTarget() {
		return hasCasterPlayer() ? PacketDistributor.TRACKING_ENTITY_AND_SELF.with(this::getCasterPlayer) : null;
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
		long time = casterWorld.getGameTime();
		return ((double) (endTime - time)) / (duration);
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
			if (hasCasterPlayer() && getCasterPlayer().getCapability(WingsProvider.WINGS, null).isPresent()) {
				@SuppressWarnings("unused")
				IWingsCapability cap = getCasterPlayer().getCapability(WingsProvider.WINGS, null).orElse(null);
				// Cap is nonnul
				PlayerEntity caster = getCasterPlayer();

				// TODO check if trick can be used here
				int busy = AbstractInterruptablePlayerTrick.playerBusyFor(caster);
				if (busy != 0) {
					LOG.warn("Found that caster {} is busy for {} more tick, trick {} won't be casted", caster, busy,
							this);
					status = 2; // Cant be casted - player busy (?)
					return;
				}
				if (!caster.getHeldItemMainhand().isEmpty()) {
					status = 1; // Cant be casted - item held
					return;
				}
				// TODO some visual stuff
				caster.setItemStackToSlot(EquipmentSlotType.MAINHAND,
						Items.SUMMONING_SOULSWORD.get().getDefaultInstance());
//				//Trick succesfull
			}
		} else {
			if (hasCasterPlayer() && getCasterPlayer().getCapability(WingsProvider.WINGS, null).isPresent()) {
				ClientPlayerEntity caster = (ClientPlayerEntity) getCasterPlayer();
				SoulswordOverlay overlay = new SoulswordOverlay(caster);
				if (!overlay.activate()) {
//					status = 5; // Some unknown shit
					LOG.warn("Can't activate overlay: {}", overlay);
				}
			}
		}
		super.execute(side);
	}

	private static final Style EPIC_STYLE = Style.EMPTY.setColor(Color.func_240743_a_(0xFFAA00AA));

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
			PlayerEntity caster = getCasterPlayer();
			if (caster == null) {
				status = 4;
			}
			if (!castEndedNaturally()) {
				status = 3;
			} else if (status == 0) {
				ItemStack stack = caster.getHeldItemMainhand();
				if (stack.isItemEqual(Items.SUMMONING_SOULSWORD.get().getDefaultInstance())) {
					stack.setCount(0);
					caster.setItemStackToSlot(EquipmentSlotType.MAINHAND,
							Items.REAL_SOULSWORD.get().getDefaultInstance());
				}
				status = 5;
			}
		} else {
			final ClientPlayerEntity caster = (ClientPlayerEntity) getCasterPlayer();
			AbstractOverlay.getActiveOverlays().stream()
					.filter(over -> (over instanceof SoulswordOverlay) && (((SoulswordOverlay) over).caster == caster))
					.forEach(over -> ((SoulswordOverlay) over).markSummoned());
			if (castEndedNaturally()) {
				// TODO perform some sounds / effects on caster
			}

		}
	}

}
