package dzuchun.wingx.client.gui;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mojang.blaze3d.matrix.MatrixStack;

import dzuchun.wingx.Wingx;
import dzuchun.wingx.capability.entity.wings.IWingsCapability;
import dzuchun.wingx.client.abillity.AbillityNode;
import dzuchun.wingx.client.abillity.AbillityNodes;
import dzuchun.wingx.client.abillity.InternalAbillityNode;
import dzuchun.wingx.client.render.gui.SeparateRenderers;
import dzuchun.wingx.client.render.overlay.FadingScreenOverlay;
import dzuchun.wingx.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector4f;
import net.minecraft.util.text.Color;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(value = Dist.CLIENT)
public class MeditationScreen extends Screen {
	private static final Logger LOG = LogManager.getLogger();

	private static final ResourceLocation HUD = new ResourceLocation(Wingx.MOD_ID,
			"textures/gui/meditation/meditation_hud.png");
	private static final Vector4f BACKGROUND_COLOR = new Vector4f(0.0f, 0.0f, 0.0f, 1.0f);
	private static final int LINES_PACKED_COLOR = 0xFFFF00FF; // TODO parametrize
	private static final float LINES_WIDTH = 1.5f; // TODO parametrize

	private IWingsCapability capability;

	public MeditationScreen(ITextComponent titleIn, IWingsCapability capabilityIn) {
		super(titleIn);
		this.capability = capabilityIn;
		this.minecraft = Minecraft.getInstance();
	}

	@Override
	protected void init() {
		super.init();
		this.addButton(this.goIntoButton = new Button((int) (this.width * 0.75d), (int) (this.height * 0.95d),
				(int) (this.width * 0.25d), (int) (this.height * 0.05d),
				new TranslationTextComponent("wingx.gui.gointo"), (Button button) -> {
					// TODO go into :)
					updateRenderedNodes();
				}));
		currentRoot = AbillityNodes.WINGX;
		inside = null;
		isInsideNode = false;
		updateRenderedNodes();
		updateUnlocked();
		xCenter = 0;
		yCenter = 0;
	}

	private void updateUnlocked() {
		for (AbillityNode node : renderedNodes) {
			node.setUnlocked(capability);
		}
	}

	@Override
	public void render(MatrixStack matrixStackIn, int p_230430_2_, int p_230430_3_, float p_230430_4_) {
		this.renderBackground(matrixStackIn, 0);
		renderNodes(matrixStackIn);
//		renderHud(matrixStackIn);
		String nodeName = I18n.format("wingx.gui.node.name");
		StringTextComponent nodeNameComponent = new StringTextComponent(nodeName);
		nodeNameComponent
				.func_230530_a_(Style.EMPTY.setBold(true).setColor(Color.func_240744_a_(TextFormatting.GREEN)));
		matrixStackIn.push();
		float scale = 1f;
		matrixStackIn.scale(scale, scale, scale);
		this.font.func_238407_a_(matrixStackIn, nodeNameComponent,
				this.width / scale - this.font.func_238414_a_(nodeNameComponent), 0.0f / scale, 0xFFFFFFFF);
		matrixStackIn.pop();
		super.render(matrixStackIn, p_230430_2_, p_230430_3_, p_230430_4_);
	}

	@Override
	public void renderBackground(MatrixStack matrixStackIn, int p_238651_2_) {
		if (this.minecraft.world != null) {
			SeparateRenderers.renderColorScreen(matrixStackIn, BACKGROUND_COLOR);
			net.minecraftforge.common.MinecraftForge.EVENT_BUS
					.post(new net.minecraftforge.client.event.GuiScreenEvent.BackgroundDrawnEvent(this, matrixStackIn));
		} else {
			renderDirtBackground(p_238651_2_);
		}

	}

	private int xCenter;
	private int yCenter;

	@Override
	public boolean mouseDragged(double xPos, double yPos, int buttonIn, double xMove, double yMove) {
//		LOG.debug("Dragged mouse at [{}, {}] for [{}, {}]. Button - {}", xPos, yPos, xMove, yMove, buttonIn);
		if (canBeClicked(xPos, yPos)) {
			xCenter += xMove;
			yCenter += yMove;
		}
		return super.mouseDragged(xPos, yPos, buttonIn, xMove, yMove);
	}

	private AbillityNode selectedNode = null;
	private AbillityNode currentRoot = null;

	@Override
	public boolean mouseClicked(double xPos, double yPos, int buttonIn) {
		if (super.mouseClicked(xPos, yPos, buttonIn) && buttonIn != 0) {
			return true;
		}
		if (canBeClicked(xPos, yPos)) {
			// TODO ?
			return true;
		} else {
			// TODO click
			return false;
		}
	}

	private boolean canBeClicked(double xPos, double yPos) {
		double xReal = xPos / width;
		double yReal = yPos / height;
		if (xReal < 0.1d || xReal > 0.75d) {
			return false;
		}
		return true;
	}

	@SuppressWarnings("unused")
	private Button goIntoButton;

	private boolean isInsideNode = false;
	private InternalAbillityNode inside = null;

	private void updateRenderedNodes() {
		renderedNodes.clear();
		if (currentRoot == null) {
			return;
		}
		ArrayList<AbillityNode> nodesToCheck = new ArrayList<AbillityNode>();
		nodesToCheck.add(currentRoot);
		while (!nodesToCheck.isEmpty()) {
			for (AbillityNode node : new ArrayList<AbillityNode>(nodesToCheck)) {
				nodesToCheck.addAll(node.getChildren());
				nodesToCheck.remove(node);
				renderedNodes.add(node);
			}
		}
		LOG.debug("Updated rendered nodes to {}", Util.iterableToString(renderedNodes));
	}

	@Override
	public void onClose() {
		new FadingScreenOverlay(FadingScreenOverlay.Color.BLACK, FadingScreenOverlay.Color.ZERO, 10).activate();
		super.onClose();
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	private List<AbillityNode> renderedNodes = new ArrayList<AbillityNode>();

	private void renderNodes(MatrixStack matrixStackIn) {
		// TODO optimize! (render only visible)
		matrixStackIn.push();
		matrixStackIn.translate(xCenter + this.width / 2, yCenter + this.height / 2, 0);
		// Rendering lines
		for (AbillityNode node : renderedNodes) {
			for (AbillityNode child : node.getChildren()) {
				SeparateRenderers.drawLine(matrixStackIn, LINES_PACKED_COLOR, LINES_WIDTH, node.getXCenterPos(),
						node.getYCenterPos(), child.getXCenterPos(), child.getYCenterPos());
			}
		}
		// Rendering nodes
		for (AbillityNode node : renderedNodes) {
			node.render(matrixStackIn);
		}
		matrixStackIn.pop();
	}

	public void renderHud(MatrixStack matrixStackIn) {
		this.minecraft.getTextureManager().bindTexture(HUD);
		SeparateRenderers.myBlit(matrixStackIn, 0, 0, this.width, this.height, 0.0f, 0.0f, 1.0f, 1.0f, this.minecraft,
				HUD);
	}
}
