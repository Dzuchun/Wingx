package dzuchun.wingx.client.render.overlay;

import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.base.Function;

import dzuchun.wingx.client.render.gui.SeparateRenderers;
import dzuchun.wingx.util.MathHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.vector.Vector4f;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.TickEvent.ClientTickEvent;

public class FadingScreenOverlay extends AbstractTickingOverlay {
	public static FadingScreenOverlay instance;
	private static final Logger LOG = LogManager.getLogger();

	public static class Color {
		public static final Vector4f ZERO = new Vector4f(0.0f, 0.0f, 0.0f, 0.0f);
		public static final Vector4f BLACK = new Vector4f(0.0f, 0.0f, 0.0f, 1.0f);
		public static final Vector4f WHITE = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);
	}

	public interface FadeFunction extends Function<Double, Double> {
		FadeFunction LINEAR = (Double d) -> {
			return d;
		};
	}

	public static Consumer<Boolean> DO_NOTHING = (successfull) -> {
	};

	private boolean isActive = false;
	private Vector4f beginColor;
	private Vector4f endColor;
	private Vector4f lastColor;
	private double ticksDuration;
	private double beginTime;
	private double endTime;
	private Consumer<Boolean> onClose;
	private FadeFunction fadeFunction;

	public FadingScreenOverlay(Vector4f beginColor, Vector4f endColor, double ticksDuration,
			Consumer<Boolean> onDeactivate, FadeFunction fadeFunction) {
		this.beginColor = beginColor;
		this.lastColor = beginColor;
		this.endColor = endColor;
		this.ticksDuration = ticksDuration;
		this.onClose = onDeactivate;
		this.fadeFunction = fadeFunction;
	}

	public FadingScreenOverlay(Vector4f beginColor, Vector4f endColor, double ticksDuration,
			Consumer<Boolean> onClose) {
		this(beginColor, endColor, ticksDuration, onClose, FadeFunction.LINEAR);
	}

	public FadingScreenOverlay(Vector4f beginColor, Vector4f endColor, double ticksDuration) {
		this(beginColor, endColor, ticksDuration, DO_NOTHING, FadeFunction.LINEAR);
	}

	@SuppressWarnings("resource")
	@Override
	public void onClienTick(ClientTickEvent event) {
		if (this.endTime < Minecraft.getInstance().world.getGameTime()) {
			this.isActive = false;
		}
	}

	@Override
	boolean conflicts(AbstractOverlay other) {
		return other instanceof FadingScreenOverlay;
	}

	@Override
	public boolean isActive() {
		return this.isActive;
	}

	@Override
	void renderGameOverlay(RenderGameOverlayEvent event) {
		@SuppressWarnings("resource")
		double realGameTime = Minecraft.getInstance().world.getGameTime() + event.getPartialTicks();
		double partPassed = (realGameTime - this.beginTime) / this.ticksDuration;
		if (partPassed > 1 || partPassed < 0) {
			return;
		}
		double partColorPassed = this.fadeFunction.apply(partPassed);
		this.lastColor = MathHelper.lerpVector4f(this.beginColor, this.endColor, partColorPassed);
//		LOG.debug("Rendering screen with color {}", currentColor.toString());
		SeparateRenderers.renderColorScreen(event, this.lastColor);
	}

	@SuppressWarnings("resource")
	@Override
	public boolean activate() {
		LOG.debug("Activating overlay {}", toString());
		if (!super.activate()) {
			LOG.warn("Could not activate overlay of {} type", this.getClass().getName());
			return false;
		}
		this.isActive = true;
		instance = this;
		this.beginTime = Minecraft.getInstance().world.getGameTime();
		this.endTime = this.beginTime + this.ticksDuration;
		return true;
	}

	@SuppressWarnings("resource")
	@Override
	public void deactivate() {
		LOG.debug("Deactivating overlay {}", toString());
		if (this.onClose != null) {
			this.onClose.accept(Minecraft.getInstance().world.getGameTime() >= this.endTime);
		}
		this.isActive = false;
		instance = null;
		super.deactivate();
	}

	public Vector4f getCurrentColor() {
		return this.lastColor;
	}
}
