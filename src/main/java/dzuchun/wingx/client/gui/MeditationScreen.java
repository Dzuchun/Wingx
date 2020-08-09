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
import dzuchun.wingx.client.abillity.ExternalAbillityNode;
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
	private static final int SELECTION_WIDTH = 2; // TODO parametrize

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
					if (!this.isInsideNode && this.selectedNode != null && this.selectedNode.isUnlocked()
							&& ((ExternalAbillityNode) this.selectedNode).getInternal() != null) {
						// Getting in
						LOG.debug("Getting in");
						this.isInsideNode = true;
						this.inside = (ExternalAbillityNode) this.selectedNode;
						this.currentRoot = this.inside.getInternal();
						this.selectedNode = this.currentRoot;
						button.setMessage(new TranslationTextComponent("wingx.gui.goout")); // TODO parametrize
					} else if (this.isInsideNode) {
						// Going out
						LOG.debug("Getting out");
						this.isInsideNode = false;
						this.selectedNode = this.inside;
						this.currentRoot = AbillityNodes.WINGX;
						button.setMessage(new TranslationTextComponent("wingx.gui.gointo")); // TODO parametrize
					}
					if (this.selectedNode != null) {
						this.xCenter = -this.selectedNode.getXCenterPos();
						this.yCenter = -this.selectedNode.getYCenterPos();
					} else {
						this.xCenter = 0;
						this.yCenter = 0;
					}
					updateRenderedNodes();
				}));
		this.currentRoot = AbillityNodes.WINGX;
		this.inside = null;
		this.isInsideNode = false;
		updateRenderedNodes();
		updateUnlocked();
		this.xCenter = 0;
		this.yCenter = 0;
	}

	private void updateUnlocked() {
		for (AbillityNode node : this.renderedNodes) {
			node.setUnlocked(this.capability);
		}
	}

	@Override
	public void render(MatrixStack matrixStackIn, int p_230430_2_, int p_230430_3_, float p_230430_4_) {
		this.renderBackground(matrixStackIn, 0);
		renderNodes(matrixStackIn);
		renderHud(matrixStackIn);
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
			this.xCenter += xMove;
			this.yCenter += yMove;
		}
		return super.mouseDragged(xPos, yPos, buttonIn, xMove, yMove);
	}

	private AbillityNode selectedNode = null;
	private AbillityNode currentRoot = null;

	@Override
	public boolean mouseClicked(double xPos, double yPos, int buttonIn) {
		if (buttonIn == 0) {
			if (canBeClicked(xPos, yPos)) {
				boolean flag = selectNode(xPos, yPos);
				if (flag) {
					// TODO update description
				}
			} else {
				// TODO click
			}
		}
		return super.mouseClicked(xPos, yPos, buttonIn);
	}

	private boolean canBeClicked(double xPos, double yPos) {
		double xReal = xPos / this.width;
		@SuppressWarnings("unused")
		double yReal = yPos / this.height;
		if (xReal < 0.1d || xReal > 0.75d) {
			return false;
		}
		return true;
	}

	private boolean selectNode(double xPos, double yPos) {
		double xCoord = xPos - this.xCenter - this.width / 2;
		double yCoord = yPos - this.yCenter - this.height / 2;
		LOG.debug("Trying to select node at [{}, {}]", xCoord, yCoord);
		for (AbillityNode node : this.renderedNodes) {
			if (Math.abs(xCoord - node.getXCenterPos()) <= AbillityNode.SIZE / 2
					&& Math.abs(yCoord - node.getYCenterPos()) <= AbillityNode.SIZE / 2) {
				this.selectedNode = node;
				LOG.debug("Selecting node {}", node);
				return true;
			}
		}
		return false;
	}

	@SuppressWarnings("unused")
	private Button goIntoButton;

	private boolean isInsideNode = false;
	private ExternalAbillityNode inside = null;

	private void updateRenderedNodes() {
		this.renderedNodes.clear();
		if (this.currentRoot == null) {
			return;
		}
		ArrayList<AbillityNode> nodesToCheck = new ArrayList<AbillityNode>();
		nodesToCheck.add(this.currentRoot);
		while (!nodesToCheck.isEmpty()) {
			for (AbillityNode node : new ArrayList<AbillityNode>(nodesToCheck)) {
				nodesToCheck.addAll(node.getChildren());
				nodesToCheck.remove(node);
				this.renderedNodes.add(node);
			}
		}
		LOG.debug("Updated rendered nodes to {}", Util.iterableToString(this.renderedNodes));
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
		matrixStackIn.translate(this.xCenter + this.width / 2, this.yCenter + this.height / 2, 0);
		// Rendering lines
		for (AbillityNode node : this.renderedNodes) {
			for (AbillityNode child : node.getChildren()) {
				SeparateRenderers.drawLine(matrixStackIn, LINES_PACKED_COLOR, LINES_WIDTH, node.getXCenterPos(),
						node.getYCenterPos(), child.getXCenterPos(), child.getYCenterPos());
			}
		}
		// Rendering nodes
		for (AbillityNode node : this.renderedNodes) {
			node.render(matrixStackIn);
		}
		if (this.selectedNode != null) {
			int x = this.selectedNode.getXCenterPos() - AbillityNode.SIZE / 2 - SELECTION_WIDTH;
			int y = this.selectedNode.getYCenterPos() - AbillityNode.SIZE / 2 - SELECTION_WIDTH;
			int size = AbillityNode.SIZE + SELECTION_WIDTH * 2 - 1;
			int length = 5; // TODO parametrize
			int color = 0xFF00FF00; // TODO parametrize

			hLine(matrixStackIn, x, x + length, y, color);
			hLine(matrixStackIn, x, x + length, y + size, color);
			hLine(matrixStackIn, x + size - length, x + size, y, color);
			hLine(matrixStackIn, x + size - length, x + size, y + size, color);

			vLine(matrixStackIn, x, y, y + length, color);
			vLine(matrixStackIn, x, y + size - length, y + size, color);
			vLine(matrixStackIn, x + size, y, y + length, color);
			vLine(matrixStackIn, x + size, y + size - length, y + size, color);
		}
		matrixStackIn.pop();
	}

	public void renderHud(MatrixStack matrixStackIn) {
		this.minecraft.getTextureManager().bindTexture(HUD);
		SeparateRenderers.myBlit(matrixStackIn, 0, 0, this.width, this.height, 0.0f, 0.0f, 1.0f, 1.0f, this.minecraft,
				HUD);
	}
}
