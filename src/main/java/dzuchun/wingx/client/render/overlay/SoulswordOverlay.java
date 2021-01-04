package dzuchun.wingx.client.render.overlay;

import dzuchun.wingx.capability.entity.wings.WingsProvider;
import dzuchun.wingx.capability.entity.wings.storage.Serializers;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLivingEvent;

@OnlyIn(Dist.CLIENT)
public class SoulswordOverlay extends AbstractOverlay {

	private ClientPlayerEntity caster;

	public SoulswordOverlay(ClientPlayerEntity casterIn) {
		this.caster = casterIn;
	}

	@Override
	boolean conflicts(AbstractOverlay other) {
		return false;
	}

	private long beginTime;
	private long endTime;

	@Override
	protected boolean activate() {
		if (activate(this)) {
			beginTime = caster.world.getGameTime();
			endTime = beginTime + caster.getCapability(WingsProvider.WINGS, null).orElse(null).getDataManager()
					.getOrAddDefault(Serializers.SOULSWORD_SERIALIZER).summonDurationTicks;
			return true;
		} else {
			return false;
		}
	}

	@Override
	protected void deactivate() {
		deactivate(this);
	}

	@Override
	void renderLiving(@SuppressWarnings("rawtypes") RenderLivingEvent event) {
		if (caster != event.getEntity()) {
			return;
		}
		
		super.renderLiving(event);
	}
	
}
