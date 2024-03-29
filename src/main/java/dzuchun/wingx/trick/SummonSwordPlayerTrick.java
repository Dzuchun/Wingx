package dzuchun.wingx.trick;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dzuchun.wingx.capability.entity.wings.IWingsCapability;
import dzuchun.wingx.capability.entity.wings.WingsProvider;
import dzuchun.wingx.capability.entity.wings.storage.Serializers;
import dzuchun.wingx.capability.entity.wings.storage.SoulswordData;
import dzuchun.wingx.client.render.overlay.AbstractOverlay;
import dzuchun.wingx.client.render.overlay.SoulswordOverlay;
import dzuchun.wingx.init.Items;
import dzuchun.wingx.init.Tricks;
import dzuchun.wingx.item.Soulsword;
import dzuchun.wingx.trick.state.TrickStates;
import dzuchun.wingx.util.NetworkHelper;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.Color;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.PacketDistributor.PacketTarget;

public class SummonSwordPlayerTrick extends AbstractInterruptablePlayerTrick implements ITimeredTrick {
	private static final Logger LOG = LogManager.getLogger();
	private SoulswordData data;

	public SummonSwordPlayerTrick(PlayerEntity caster) {
		super(caster, 0, InterruptCondition.CHANGED_ITEM_CONDITION);
		if (caster != null) {
			caster.getCapability(WingsProvider.WINGS).ifPresent((IWingsCapability wings) -> {
				SoulswordData data = wings.getDataManager().getOrAddDefault(Serializers.SOULSWORD_SERIALIZER);
				this.duration = data.summonDurationTicks;
			});
		}
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
	public int timeFull() throws NoCasterException {
		return this.duration;
	}

	@Override
	public double partLeft() throws NoCasterException {
		assertHasCaster(this);
		long time = this.casterWorld.getGameTime();
		return ((double) (this.endTime - time)) / (this.duration);
	}

//	@Override
//	protected Consumer<RenderGameOverlayEvent> getDrawFunction() {
//		// TODO probably should override draw function
//		return super.getDrawFunction();
//	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public void executeClient() {
		super.executeClient();
		// We are on client
		if (this.hasCasterPlayer() && this.getCasterPlayer().getCapability(WingsProvider.WINGS, null).isPresent()) {
			ClientPlayerEntity caster = (ClientPlayerEntity) this.getCasterPlayer();
			SoulswordOverlay overlay = new SoulswordOverlay(caster);
			if (!overlay.activate()) {
//				status = 5; // Some unknown shit
				LOG.warn("Can't activate overlay: {}", overlay);
			}
		}
	}

	@Override
	public void executeServer() {
		if (this.hasCasterPlayer() && this.getCasterPlayer().getCapability(WingsProvider.WINGS, null).isPresent()) {
			@SuppressWarnings("unused")
			IWingsCapability cap = this.getCasterPlayer().getCapability(WingsProvider.WINGS, null).orElse(null);
			// Cap is nonnul
			PlayerEntity caster = this.getCasterPlayer();

			// TODO check if trick can be used here
			int busy = AbstractInterruptablePlayerTrick.playerBusyFor(caster);
			if (busy != 0) {
				LOG.warn("Found that caster {} is busy for {} more tick, trick {} won't be casted", caster, busy, this);
				this.state = TrickStates.CASTER_BUSY; // Cant be casted - player busy
				return;
			}
			if (!caster.getHeldItemMainhand().isEmpty()) {
				this.state = TrickStates.HAND_BUSY; // Cant be casted - item held
				return;
			}
			// TODO some visual stuff
			ItemStack stack = Items.SOULSWORD.get().getDefaultInstance();
			stack.getOrCreateTag().putBoolean(Soulsword.SUMMONED_TAG, false);
			caster.setItemStackToSlot(EquipmentSlotType.MAINHAND, stack);
//			//Trick succesfull
		}
		super.executeServer();
	}

	private static final Style EPIC_STYLE = Style.EMPTY.setColor(Color.fromInt(0xFFAA00AA));

	@Override
	public ITextComponent getStateMessage() {
		if (this.state == TrickStates.SUCCESS) {
			IFormattableTextComponent mes = (IFormattableTextComponent) super.getStateMessage();
			return mes.setStyle(EPIC_STYLE);
		} else {
			return super.getStateMessage();
		}
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public void onTrickEndClient() throws NoCasterException {
		super.onTrickEndClient();
		final ClientPlayerEntity caster = (ClientPlayerEntity) this.getCasterPlayer();
		AbstractOverlay.getActiveOverlays().stream()
				.filter(over -> (over instanceof SoulswordOverlay) && (((SoulswordOverlay) over).caster == caster))
				.forEach(over -> ((SoulswordOverlay) over).markSummoned());
		if (this.state == TrickStates.SUCCESS) {
			// TODO perform some sounds / effects on caster
		}
	}

	@Override
	public void onTrickEndServer() throws NoCasterException {
		super.onTrickEndServer();
		PlayerEntity caster = this.getCasterPlayer();
		if (caster == null) {
			this.state = TrickStates.NO_CASTER;
		}
		if (!this.state.isError()) {
			ItemStack stack = caster.getHeldItemMainhand();
			if (stack.isItemEqual(Items.SOULSWORD.get().getDefaultInstance())) {
				stack.getOrCreateTag().putBoolean(Soulsword.SUMMONED_TAG, true);
			}
			this.state = TrickStates.SUCCESS;
		}
	}

	public static class TrickType extends AbstractInterruptablePlayerTrick.TrickType<SummonSwordPlayerTrick> {

		@Override
		public SummonSwordPlayerTrick writeToBuf(SummonSwordPlayerTrick trick, PacketBuffer buf) {
			NetworkHelper.writeChecked(buf, trick.data, Serializers.SOULSWORD_SERIALIZER::write);
			return super.writeToBuf(trick, buf);
		}

		@Override
		protected SummonSwordPlayerTrick readFromBufInternal(SummonSwordPlayerTrick trick, PacketBuffer buf) {
			trick.data = NetworkHelper.readChecked(buf, Serializers.SOULSWORD_SERIALIZER::read);
			return super.readFromBufInternal(trick, buf);
		}

		@Override
		public SummonSwordPlayerTrick newEmpty() {
			return new SummonSwordPlayerTrick(null);
		}

	}

	@SuppressWarnings("unchecked")
	@Override
	public SummonSwordPlayerTrick.TrickType getType() {
		return Tricks.SUMMON_SOULSWORD_TRICK.get();
	}

}
