package dzuchun.wingx.client.render.overlay;

import java.util.ArrayList;

import dzuchun.wingx.Wingx;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent.LoggedOutEvent;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@Mod.EventBusSubscriber(bus = Bus.FORGE, modid = Wingx.MOD_ID)
public abstract class AbstractTickingOverlay extends AbstractOverlay {

	private static final Object ACTIVE_OVERLAYS_LOCK = new Object();
	private static ArrayList<AbstractTickingOverlay> tickingOverlays = new ArrayList<AbstractTickingOverlay>(0);

	public static ArrayList<AbstractTickingOverlay> getTickingOverlays() {
		return tickingOverlays;
	}

	protected static boolean activateTicking(AbstractTickingOverlay overlay) {
		boolean res = true;
		synchronized (ACTIVE_OVERLAYS_LOCK) {
			res = tickingOverlays.add(overlay);

		}
		return res;
	}

	protected static void deactivateTicking(AbstractTickingOverlay overlay) {
		synchronized (ACTIVE_OVERLAYS_LOCK) {
			tickingOverlays.remove(overlay);
		}
	}

	public static void onClientTick(ClientTickEvent event) {
		ArrayList<AbstractTickingOverlay> toRemove = new ArrayList<AbstractTickingOverlay>(0);
		tickingOverlays.forEach((AbstractTickingOverlay overlay) -> {
			overlay.onClienTick(event);
			if (!overlay.isActive()) {
				toRemove.add(overlay);
			}
		});
		toRemove.forEach((AbstractTickingOverlay overlay) -> {
			overlay.deactivate();
		});
	}

	public abstract void onClienTick(ClientTickEvent event);

	/**
	 * Activates overlay. Should be invoked from any implementation.
	 */
	@Override
	protected boolean activate() {
		boolean activate = activate(this);
		if (!activate) {
			return false;
		}

		boolean activateTicking = activateTicking(this);
		if (!activateTicking) {
			deactivate(this);
			return false;
		}
		return true;
	}

	/**
	 * Deactivates overlay. Should be invoked from any implementation. Or you may
	 * just make isActive() return false, then overlay will be removed on next tick.
	 */
	@Override
	protected void deactivate() {
		deactivate(this);
		deactivateTicking(this);
	}

	public static void onDisconnect(LoggedOutEvent event) {
		if (event != null && event instanceof LoggedOutEvent) {
			synchronized (ACTIVE_OVERLAYS_LOCK) {
				tickingOverlays.clear();
			}
		}
	}
}
