package dzuchun.wingx.client.gui;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

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
import net.minecraft.util.math.MathHelper;
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
	// TODO fix blinking on server delay
	private static final Logger LOG = LogManager.getLogger();

	private static final ResourceLocation HUD = new ResourceLocation(Wingx.MOD_ID,
			"textures/gui/meditation/meditation_hud.png");
	// TODO remove left side
	private static final ResourceLocation NODES_ATLAS = new ResourceLocation(Wingx.MOD_ID,
			"textures/gui/meditation/abillity_node_atlas.png");
	private static final Vector4f BACKGROUND_COLOR = new Vector4f(0.0f, 0.0f, 0.0f, 1.0f);
	private static final int LINES_PACKED_COLOR = 0xFFFF0000; // TODO parametrize
	private static final float LINES_WIDTH = 1.5f; // TODO parametrize
	private static final int SELECTION_WIDTH = 2; // TODO parametrize
	private static final long OPEN_ANIMATION_DURATION = 10; // TODO parametrize
	private static final double OPEN_ANIMATION_DELAY_OVER_DISTANCE = 0.05; // TODO parametrize
	private static final float OPEN_ANIMATION_SCALE = 2.0f;
	private static final double OPEN_ANIMATION_SCALE_OVER_TICK = (OPEN_ANIMATION_SCALE - 1.0d)
			/ OPEN_ANIMATION_DURATION;

	private static AbillityNode selectedNode = null;
	private static AbillityNode currentRoot = null;

	private static boolean isInsideNode = false;
	private static ExternalAbillityNode inside = null;

	private static long openTime = 0;
	private static long openGuiTime = 0;

	private IWingsCapability capability;

	public MeditationScreen(ITextComponent titleIn, IWingsCapability capabilityIn) {
		super(titleIn);
		this.capability = capabilityIn;
		this.minecraft = Minecraft.getInstance();
		openTime = System.currentTimeMillis();
		openGuiTime = System.currentTimeMillis();
	}

	@Override
	protected void init() {
		super.init();
		this.addButton(this.goIntoButton = new Button((int) (this.width * 0.75d), (int) (this.height * 0.95d),
				(int) (this.width * 0.25d), (int) (this.height * 0.05d),
				new TranslationTextComponent(MeditationScreen.isInsideNode ? "wingx.gui.goout" : "wingx.gui.gointo"),
				(Button button) -> {
					// TODO go into :)
					if (!MeditationScreen.isInsideNode && MeditationScreen.selectedNode != null
							&& MeditationScreen.selectedNode.isUnlocked()
							&& ((ExternalAbillityNode) MeditationScreen.selectedNode).getInternal() != null) {
						// Getting in
						LOG.debug("Getting in");
						MeditationScreen.isInsideNode = true;
						MeditationScreen.inside = (ExternalAbillityNode) MeditationScreen.selectedNode;
						MeditationScreen.currentRoot = MeditationScreen.inside.getInternal();
						MeditationScreen.selectedNode = MeditationScreen.currentRoot;
						button.setMessage(new TranslationTextComponent("wingx.gui.goout"));
						openTime = System.currentTimeMillis();
					} else if (MeditationScreen.isInsideNode) {
						// Going out
						LOG.debug("Getting out");
						MeditationScreen.isInsideNode = false;
						MeditationScreen.selectedNode = MeditationScreen.inside;
						MeditationScreen.currentRoot = AbillityNodes.WINGX;
						button.setMessage(new TranslationTextComponent("wingx.gui.gointo"));
						openTime = System.currentTimeMillis();
					}
					if (MeditationScreen.selectedNode != null) {
						this.xCenter = -MeditationScreen.selectedNode.getXCenterPos();
						this.yCenter = -MeditationScreen.selectedNode.getYCenterPos();
					} else {
						this.xCenter = 0;
						this.yCenter = 0;
					}
					updateRenderedNodes();
				}));
		MeditationScreen.currentRoot = MeditationScreen.currentRoot == null ? AbillityNodes.WINGX : currentRoot;
		// TODO reset theese on world rejoin
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
		int alpha = 255;
		float ticksPassed = (System.currentTimeMillis() - openGuiTime) / 50.0f;
		if (ticksPassed < OPEN_ANIMATION_DURATION) {
			alpha = (int) (ticksPassed / OPEN_ANIMATION_DURATION * 255);
		}
		renderHud(matrixStackIn, alpha);
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
		if (xReal > 0.75d) {
			return false;
		}
		return true;
	}

	private boolean selectNode(double xPos, double yPos) {
		double xCoord = xPos - this.xCenter - this.width / 2;
		double yCoord = yPos - this.yCenter - this.height / 2;
		LOG.debug("Trying to select node at [{}, {}]", xCoord, yCoord);
		for (AbillityNode node : this.renderedNodes) {
			if (Math.abs(xCoord - node.getXCenterPos()) <= AbillityNode.NODE_SIZE / 2
					&& Math.abs(yCoord - node.getYCenterPos()) <= AbillityNode.NODE_SIZE / 2) {
				MeditationScreen.selectedNode = node;
				LOG.debug("Selecting node {}", node);
				return true;
			}
		}
		return false;
	}

	@SuppressWarnings("unused")
	private Button goIntoButton;

	private void updateRenderedNodes() {
		this.renderedNodes.clear();
		if (MeditationScreen.currentRoot == null) {
			return;
		}
		ArrayList<AbillityNode> nodesToCheck = new ArrayList<AbillityNode>();
		nodesToCheck.add(MeditationScreen.currentRoot);
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

	@SuppressWarnings("deprecation")
	private void renderNodes(MatrixStack matrixStackIn) {
		// TODO optimize! (render only visible)
		matrixStackIn.push();
		matrixStackIn.translate(this.xCenter + this.width / 2, this.yCenter + this.height / 2, 0);
		this.minecraft.textureManager.bindTexture(NODES_ATLAS);
		float ticksPassed = (System.currentTimeMillis() - openTime) / 50.0f;
		// Rendering nodes and lines
		for (AbillityNode node : this.renderedNodes) {
			matrixStackIn.push();
			matrixStackIn.translate(node.getXCenterPos(), node.getYCenterPos(), 0);
			float alpha = 1.0f;
			float scaleFactor = 1.0f;
			double distance = Math.sqrt(Math.pow(node.getXCenterPos() + this.xCenter, 2)
					+ Math.pow(node.getYCenterPos() + this.yCenter, 2));
			if (ticksPassed - OPEN_ANIMATION_DELAY_OVER_DISTANCE * distance < OPEN_ANIMATION_DURATION) {
				// TODO optimize!
				float semiTicksPassed = (float) (ticksPassed - OPEN_ANIMATION_DELAY_OVER_DISTANCE * distance);
				semiTicksPassed = MathHelper.clamp(semiTicksPassed, 0.0f, OPEN_ANIMATION_DURATION);
				alpha = semiTicksPassed / OPEN_ANIMATION_DURATION;
				scaleFactor = (float) (OPEN_ANIMATION_SCALE - OPEN_ANIMATION_SCALE_OVER_TICK * semiTicksPassed);
			}
			alpha = MathHelper.clamp(alpha, 0.0f, 1.0f);
			// Rendering lines
			if (!node.getChildren().isEmpty()) {
				int linesColor = LINES_PACKED_COLOR + Math.round(alpha * 255);
				for (AbillityNode child : node.getChildren()) {
					SeparateRenderers.drawLine(matrixStackIn, linesColor, LINES_WIDTH, node.getXCenterPos(),
							node.getYCenterPos(), child.getXCenterPos(), child.getYCenterPos());
				}
			}
			// Rendering node
			matrixStackIn.push();
			matrixStackIn.scale(scaleFactor, scaleFactor, 1.0f);
			node.render(matrixStackIn, alpha);
			matrixStackIn.pop();
			matrixStackIn.pop();
		}
		if (MeditationScreen.selectedNode != null) {
			int x = MeditationScreen.selectedNode.getXCenterPos() - AbillityNode.NODE_SIZE / 2 - SELECTION_WIDTH;
			int y = MeditationScreen.selectedNode.getYCenterPos() - AbillityNode.NODE_SIZE / 2 - SELECTION_WIDTH;
			int size = AbillityNode.NODE_SIZE + SELECTION_WIDTH * 2 - 1;
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

		RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
		matrixStackIn.pop();
	}

	public void renderHud(MatrixStack matrixStackIn, int alphaIn) {
		this.minecraft.getTextureManager().bindTexture(HUD);
		SeparateRenderers.myBlit(matrixStackIn, 0, 0, this.width, this.height, 0.0f, 0.0f, 1.0f, 1.0f,
				0xFFFFFF00 + alphaIn, this.minecraft, HUD);
		String nodeName = I18n.format("wingx.gui.node.name");
		StringTextComponent nodeNameComponent = new StringTextComponent(nodeName);
		nodeNameComponent
				.func_230530_a_(Style.EMPTY.setBold(true).setColor(Color.func_240744_a_(TextFormatting.GREEN)));
		matrixStackIn.push();
		float scale = 1f;
		matrixStackIn.scale(scale, scale, scale);
		this.font.func_238407_a_(matrixStackIn, nodeNameComponent,
				this.width / scale - this.font.func_238414_a_(nodeNameComponent), 0.0f / scale, 0xFFFFFF00 + alphaIn);
		matrixStackIn.pop();
	}
}
