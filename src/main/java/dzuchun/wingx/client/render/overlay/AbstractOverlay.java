package dzuchun.wingx.client.render.overlay;

import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent.LoggedOutEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@OnlyIn(value = Dist.CLIENT)
public abstract class AbstractOverlay {
	private static final Logger LOG = LogManager.getLogger();

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
			if (res_1 && !activeOverlays.add(overlayIn)) {
				res_1 = false;
				LOG.warn("Could not add overlay {}", overlayIn.toString());
			}
		}
		return res_1;
	}

	protected static void deactivate(AbstractOverlay overlayIn) {
		synchronized (ACTIVE_OVERLAYS_LOCK) {
			LOG.debug("Removing overlay {}", overlayIn.toString());
			activeOverlays.remove(overlayIn);
		}
	}

	abstract boolean conflicts(AbstractOverlay other);

	public static void onRenderGameOverlay(RenderGameOverlayEvent.Post event) {
//		LOG.debug("Rendering gameOverlay part");
		if (event.getType() == ElementType.CROSSHAIRS) {
			synchronized (ACTIVE_OVERLAYS_LOCK) {
				activeOverlays.forEach((AbstractOverlay overlay) -> {
					// LOG.debug("Rendering {} overlay", overlay.toString());
					overlay.renderGameOverlay(event);
				});
			}
		}
	}

	public static void onDisconnect(LoggedOutEvent event) {
		if ((event != null) && (event instanceof LoggedOutEvent)) {
			synchronized (ACTIVE_OVERLAYS_LOCK) {
				activeOverlays.clear();
			}
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

	public static void onRenderWorldLast(RenderWorldLastEvent event) {
		synchronized (ACTIVE_OVERLAYS_LOCK) {
			activeOverlays.forEach((AbstractOverlay overlay) -> {
				overlay.renderWorldLast(event);
			});
		}
	}

	void renderWorldLast(RenderWorldLastEvent event) {
	}

	// TODO create default method!
	protected abstract boolean activate();

	// TODO create default method!
	protected abstract void deactivate();

	protected boolean active = false;

	public boolean isActive() {
		return this.active;
	}
}
