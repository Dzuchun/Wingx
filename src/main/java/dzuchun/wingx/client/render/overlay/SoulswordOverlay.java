package dzuchun.wingx.client.render.overlay;

import dzuchun.wingx.capability.entity.wings.WingsProvider;
import dzuchun.wingx.capability.entity.wings.storage.Serializers;
import dzuchun.wingx.init.Items;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.event.TickEvent.ClientTickEvent;

@OnlyIn(Dist.CLIENT)
public class SoulswordOverlay extends AbstractTickingOverlay {

	public final ClientPlayerEntity caster;
	@SuppressWarnings("unused")
	private boolean summoned;

	public SoulswordOverlay(ClientPlayerEntity casterIn) {
		this.caster = casterIn;
		this.summoned = false;
	}

	@Override
	boolean conflicts(AbstractOverlay other) {
		return ((other instanceof SoulswordOverlay) && ((SoulswordOverlay) other).caster.equals(this.caster));
	}

	private long beginTime;
	@SuppressWarnings("unused")
	private long endTime;

	@Override
	public boolean activate() {
		if (super.activate()) {
			this.beginTime = this.caster.world.getGameTime();
			this.endTime = this.beginTime + this.caster.getCapability(WingsProvider.WINGS, null).orElse(null)
					.getDataManager().getOrAddDefault(Serializers.SOULSWORD_SERIALIZER).summonDurationTicks;
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void deactivate() {
		super.deactivate();
	}

	@Override
	void renderLiving(@SuppressWarnings("rawtypes") RenderLivingEvent event) {
		super.renderLiving(event);
		if (this.caster != event.getEntity()) {
			return;
		}
		// TODO render some sword-summoning stuff
	}

	@Override
	void renderGameOverlay(RenderGameOverlayEvent event) {

		// TODO render some visual stuff
	}

	public void markSummoned() {
		this.summoned = true;
	}

	@Override
	public void onClienTick(ClientTickEvent event) {
		if (this.caster.getHeldItemMainhand().getItem() != Items.SOULSWORD.get()) {
			this.active = false;
		}
	}

}
