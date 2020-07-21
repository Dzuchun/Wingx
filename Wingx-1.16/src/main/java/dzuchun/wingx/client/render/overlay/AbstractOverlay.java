package dzuchun.wingx.client.render.overlay;

import java.util.ArrayList;

import dzuchun.wingx.Wingx;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@OnlyIn(value = Dist.CLIENT)
@Mod.EventBusSubscriber(bus = Bus.FORGE, modid = Wingx.MOD_ID)
public abstract class AbstractOverlay {
	public static void init() {
	}

	private static final Object ACTIVE_OVERLAYS_LOCK = new Object();
	private static ArrayList<AbstractOverlay> activeOverlays = new ArrayList<AbstractOverlay>(0);

	public static ArrayList<AbstractOverlay> getActiveOverlays() {
		return activeOverlays;
	}

	private static boolean res_1;

	protected static boolean activate(AbstractOverlay overlayIn) {
		res_1 = true;
		synchronized (ACTIVE_OVERLAYS_LOCK) {
			activeOverlays.forEach((AbstractOverlay overlay) -> {
				if (overlay.conflicts(overlayIn)) {
					res_1 = false;
				}
			});
			if (res_1) {
				activeOverlays.add(overlayIn);
			}
		}
		return res_1;
	}

	protected static void deactivate(AbstractOverlay overlayIn) {
		synchronized (ACTIVE_OVERLAYS_LOCK) {
			activeOverlays.remove(overlayIn);
		}
	}

	abstract boolean conflicts(AbstractOverlay other);

	@SubscribeEvent
	public static void onRenderGameOverlay(RenderGameOverlayEvent.Pre event) {
		synchronized (ACTIVE_OVERLAYS_LOCK) {
			activeOverlays.forEach((AbstractOverlay overlay) -> {
				overlay.renderGameOverlay(event);
			});
		}
	}

	void renderGameOverlay(RenderGameOverlayEvent event) {
	}

	@SubscribeEvent
	public static void onRenderLivingOverlay(@SuppressWarnings("rawtypes") RenderLivingEvent.Post event) {
		synchronized (ACTIVE_OVERLAYS_LOCK) {
			activeOverlays.forEach((AbstractOverlay overlay) -> {
				overlay.renderLiving(event);
			});
		}
	}

	void renderLiving(@SuppressWarnings("rawtypes") RenderLivingEvent event) {
	}
}
