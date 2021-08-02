package dzuchun.wingx.client.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.matrix.MatrixStack;

import dzuchun.wingx.Wingx;
import dzuchun.wingx.capability.entity.wings.IWingsCapability;
import dzuchun.wingx.capability.entity.wings.storage.Serializers;
import dzuchun.wingx.client.render.gui.SeparateRenderers;
import dzuchun.wingx.client.render.overlay.FadingScreenOverlay;
import dzuchun.wingx.config.abillity.AbillityNode;
import dzuchun.wingx.config.abillity.AbillityNodes;
import dzuchun.wingx.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector4f;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(value = Dist.CLIENT)
public class MeditationScreen extends Screen {
	// TODO fix blinking on server delay
	private static final Logger LOG = LogManager.getLogger();

	private static final ResourceLocation HUD = new ResourceLocation(Wingx.MOD_ID,
			"textures/gui/meditation/meditation_hud.png");
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
	private static AbillityNode inside = null;

	private static long openTime = 0;
	private static long openGuiTime = 0;

	// TODO actually, we can avoid passing capability via including sesessary data
	// into a requiredData list unconditionaly
	private IWingsCapability capability;
	private Map<String, Integer> requiredStats;
	private Map<String, Object> requiredData;

	public MeditationScreen(ITextComponent titleIn, IWingsCapability capabilityIn, Map<String, Integer> requiredStatsIn,
			Map<String, Object> requiredDataIn) {
		super(titleIn);
		this.capability = capabilityIn;
		this.requiredStats = requiredStatsIn;
		this.requiredData = requiredDataIn;
		LOG.debug("Passed stats: {}", Util.iterableToString(this.requiredStats.entrySet(),
				e -> String.format("(%s, %d)", e.getKey(), e.getValue())));
		LOG.debug("Passed data: {}", Util.iterableToString(this.requiredData.entrySet(),
				e -> String.format("(%s, %d)", e.getKey(), e.getValue())));
		this.minecraft = Minecraft.getInstance();
		openTime = System.currentTimeMillis();
		openGuiTime = System.currentTimeMillis();
	}

	@Override
	protected void init() {
		super.init();
//		this.minecraft.getConnection().sendPacket(new CClientStatusPacket(CClientStatusPacket.State.REQUEST_STATS));
		if ((AbillityNodes.nodes == null) || (AbillityNodes.requiredStats == null)) {
			AbillityNodes.loadAbillityNodes();
		}
		AbillityNodes.markNodesDirty();
		this.addButton(this.goIntoButton = new Button((int) (this.width * 0.75d), (int) (this.height * 0.95d),
				(int) (this.width * 0.25d), (int) (this.height * 0.05d),
				new TranslationTextComponent(MeditationScreen.isInsideNode ? "wingx.gui.goout" : "wingx.gui.gointo"),
				(Button button) -> {
					if (!MeditationScreen.isInsideNode && (MeditationScreen.selectedNode != null)
							&& MeditationScreen.selectedNode.isUnlocked()
							&& ((MeditationScreen.selectedNode).getInternal() != null)) {
						// Getting in
						LOG.debug("Getting in");
						MeditationScreen.isInsideNode = true;
						MeditationScreen.inside = MeditationScreen.selectedNode;
						MeditationScreen.currentRoot = MeditationScreen.inside.getInternal();
						MeditationScreen.selectedNode = MeditationScreen.currentRoot;
						button.setMessage(new TranslationTextComponent("wingx.gui.goout"));
						openTime = System.currentTimeMillis();
					} else if (MeditationScreen.isInsideNode) {
						// Going out
						LOG.debug("Getting out");
						MeditationScreen.isInsideNode = false;
						MeditationScreen.selectedNode = MeditationScreen.inside;
						MeditationScreen.currentRoot = AbillityNodes.nodes.get(0);
						button.setMessage(new TranslationTextComponent("wingx.gui.gointo"));
						openTime = System.currentTimeMillis();
					}
					if (MeditationScreen.selectedNode != null) {
						this.xCenter = -MeditationScreen.selectedNode.xCenterPos();
						this.yCenter = -MeditationScreen.selectedNode.yCenterPos();
					} else {
						this.xCenter = 0;
						this.yCenter = 0;
					}
					this.updateRenderedNodes();
					this.updateRenderedDescription();
				}));
		MeditationScreen.currentRoot = MeditationScreen.currentRoot == null ? AbillityNodes.nodes.get(0) : currentRoot;
		// TODO reset theese on world rejoin
		this.updateRenderedNodes();
		this.xCenter = 0;
		this.yCenter = 0;
	}

	@Override
	public void render(MatrixStack matrixStackIn, int p_230430_2_, int p_230430_3_, float p_230430_4_) {
		this.renderBackground(matrixStackIn, 0);
		this.renderNodes(matrixStackIn);
		int alpha = 255;
		float ticksPassed = (System.currentTimeMillis() - openGuiTime) / 50.0f;
		if (ticksPassed < OPEN_ANIMATION_DURATION) {
			alpha = (int) ((ticksPassed / OPEN_ANIMATION_DURATION) * 255);
		}
		this.renderHud(matrixStackIn, alpha);
		super.render(matrixStackIn, p_230430_2_, p_230430_3_, p_230430_4_);
	}

	@Override
	public void renderBackground(MatrixStack matrixStackIn, int p_238651_2_) {
		if (this.minecraft.world != null) {
			SeparateRenderers.renderColorScreen(matrixStackIn, BACKGROUND_COLOR);
			net.minecraftforge.common.MinecraftForge.EVENT_BUS
					.post(new net.minecraftforge.client.event.GuiScreenEvent.BackgroundDrawnEvent(this, matrixStackIn));
		} else {
			this.renderDirtBackground(p_238651_2_);
		}

	}

	private float xCenter, yCenter;

	@Override
	public boolean mouseDragged(double xPos, double yPos, int buttonIn, double xMove, double yMove) {
//		LOG.debug("Mouse dragged at ({}, {}), speed ({},{})", xPos, yPos, xMove, yMove);
		if (this.canBeClicked(xPos, yPos)) {
			this.xCenter += xMove;
			this.yCenter += yMove;
			return true;
		} else {
			return super.mouseDragged(xPos, yPos, buttonIn, xMove, yMove);
		}
	}

	@Override
	public boolean mouseClicked(double xPos, double yPos, int buttonIn) {
		if ((buttonIn == 0) && !this.isDragging()) {
			if (this.canBeClicked(xPos, yPos)) {
				boolean flag = this.selectNode(xPos, yPos);
				if (flag) {
					this.updateRenderedDescription();
				}
			} else {
				// TODO sidepanel click
			}
		}
		return super.mouseClicked(xPos, yPos, buttonIn);
	}

	private void updateRenderedDescription() {
		// TODO fix bug with getting out of bounds
		int currentWord = 0;
		renderedDescription.clear();
		String descString = new TranslationTextComponent(selectedNode.getDescLoc()).getString();
		String[] descWords = descString.split(" ");
		int wordsCount = descWords.length;
		if (descWords.length > 0) {
			String line = descWords[currentWord];
			currentWord++;
			int descWidth = this.width / 4;
			while (currentWord < wordsCount) {
				if (this.font.getStringWidth(line) >= descWidth) {
					if (line.split(" ").length > 1) {
						currentWord--;
					}
					line = line.substring(0, Math.max(line.lastIndexOf(descWords[currentWord]) - 1, 0));
					renderedDescription.add(line);
					line = descWords[currentWord];
				} else {
					line += " " + descWords[currentWord];
				}
				currentWord++;
			}
			renderedDescription.add(line);
		}
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
		double xCoord = xPos - this.xCenter - (this.width / 2);
		double yCoord = yPos - this.yCenter - (this.height / 2);
		LOG.debug("Trying to select node at [{}, {}]", xCoord, yCoord);
		for (AbillityNode node : this.renderedNodes) {
			if ((Math.abs(xCoord - node.xCenterPos()) <= (AbillityNode.NODE_SIZE / 2))
					&& (Math.abs(yCoord - node.yCenterPos()) <= (AbillityNode.NODE_SIZE / 2))) {
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
				nodesToCheck.addAll(node.children);
				nodesToCheck.remove(node);
				this.renderedNodes.add(node);
			}
		}
		LOG.debug("Updated rendered nodes to {}", Util.iterableToString(this.renderedNodes));
	}

	@Override
	public void onClose() {
		new FadingScreenOverlay(FadingScreenOverlay.Color.BLACK, FadingScreenOverlay.Color.ZERO,
				this.capability.getDataManager().getOrAddDefault(Serializers.BASIC_SERIALIZER).meditationLength / 10)
						.activate();
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
		matrixStackIn.translate(this.xCenter + (this.width / 2), this.yCenter + (this.height / 2), 0);
		this.minecraft.textureManager.bindTexture(NODES_ATLAS);
		float ticksPassed = (System.currentTimeMillis() - openTime) / 50.0f;
		// Rendering nodes and lines
		for (AbillityNode node : this.renderedNodes) {
			// Updating unlocked status
			node.updateUnlocked(this.requiredStats, this.requiredData);

			float alpha = 1.0f;
			float scaleFactor = 1.0f;
			double distance = Math.sqrt(
					Math.pow(node.xCenterPos() + this.xCenter, 2) + Math.pow(node.yCenterPos() + this.yCenter, 2));
			if ((ticksPassed - (OPEN_ANIMATION_DELAY_OVER_DISTANCE * distance)) < OPEN_ANIMATION_DURATION) {
				// TODO optimize!
				float semiTicksPassed = (float) (ticksPassed - (OPEN_ANIMATION_DELAY_OVER_DISTANCE * distance));
				semiTicksPassed = MathHelper.clamp(semiTicksPassed, 0.0f, OPEN_ANIMATION_DURATION);
				alpha = semiTicksPassed / OPEN_ANIMATION_DURATION;
				scaleFactor = (float) (OPEN_ANIMATION_SCALE - (OPEN_ANIMATION_SCALE_OVER_TICK * semiTicksPassed));
			}
			alpha = MathHelper.clamp(alpha, 0.0f, 1.0f);
			// Rendering lines
			if (!node.children.isEmpty()) {
				int linesColor = LINES_PACKED_COLOR + Math.round(alpha * 255);
				for (AbillityNode child : node.children) {
					SeparateRenderers.drawLine(matrixStackIn, linesColor, LINES_WIDTH, node.xCenterPos(),
							node.yCenterPos(), child.xCenterPos(), child.yCenterPos());
				}
			}
			// Rendering node
			matrixStackIn.push();
			matrixStackIn.translate(node.xCenterPos(), node.yCenterPos(), 0);
			matrixStackIn.scale(scaleFactor, scaleFactor, 1.0f);
			node.render(matrixStackIn, alpha);
			matrixStackIn.pop();
		}
		if (MeditationScreen.selectedNode != null) {
			int x = MeditationScreen.selectedNode.xCenterPos() - (AbillityNode.NODE_SIZE / 2) - SELECTION_WIDTH;
			int y = MeditationScreen.selectedNode.yCenterPos() - (AbillityNode.NODE_SIZE / 2) - SELECTION_WIDTH;
			int size = (AbillityNode.NODE_SIZE + (SELECTION_WIDTH * 2)) - 1;
			int length = 5; // TODO parametrize
			int color = 0xFF00FF00; // TODO parametrize

			this.hLine(matrixStackIn, x + 1, x + length, y, color);
			this.hLine(matrixStackIn, x + 1, x + length, y + size, color);
			this.hLine(matrixStackIn, (x + size) - length, (x + size) - 1, y, color);
			this.hLine(matrixStackIn, (x + size) - length, (x + size) - 1, y + size, color);

			// TODO get why should I "+-1" randomly
			this.vLine(matrixStackIn, x, y, y + length + 1, color);
			this.vLine(matrixStackIn, x, (y + size) - length - 1, y + size, color);
			this.vLine(matrixStackIn, x + size, y, y + length + 1, color);
			this.vLine(matrixStackIn, x + size, (y + size) - length - 1, y + size, color);
		}

		GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
		matrixStackIn.pop();
	}

	private static final ArrayList<String> renderedDescription = new ArrayList<String>(0);

	public void renderHud(MatrixStack matrixStackIn, int alphaIn) {
		// TODO add scaling
		this.minecraft.getTextureManager().bindTexture(HUD);
		int hudColor = 0xFFFFFF00 + alphaIn; // TODO parametrize (?)
		SeparateRenderers.myBlit(matrixStackIn, 0, 0, this.width, this.height, 0.0f, 0.0f, 1.0f, 1.0f, hudColor,
				this.minecraft, HUD);
		if (selectedNode != null) {
			ITextComponent name = new TranslationTextComponent(selectedNode.getNameLoc());
			// TODO set style
			int fontHeiht = this.font.FONT_HEIGHT;
			int width = (int) (this.width * 0.25) - 3;
			matrixStackIn.push();
			matrixStackIn.translate(this.width - width, 0, 0);
			matrixStackIn.push();
			int nameHeight = (int) (this.height * 0.05);
			int textColor = 0xFFFFFF00 + alphaIn;
			this.font.drawText(matrixStackIn, name, (width - this.font.getStringPropertyWidth(name)) / 2.0f,
					(nameHeight - fontHeiht) / 2.0f, textColor);
			SeparateRenderers.drawLine(matrixStackIn, -1, 1, 0, nameHeight, width, nameHeight);
			matrixStackIn.translate(0, nameHeight + 2, 0);
			matrixStackIn.push();
			// TODO check if it fits place
			int descColor = 0x00CCCCCC + (alphaIn << 24);
			Style descStyle = new TranslationTextComponent(selectedNode.getDescLoc()).getStyle();
			// TODO add description
			for (String row : renderedDescription) {
				IFormattableTextComponent rowComponent = new StringTextComponent(row).setStyle(descStyle);
				this.font.drawText(matrixStackIn, rowComponent, width - this.font.getStringPropertyWidth(rowComponent),
						0, descColor);
				matrixStackIn.translate(0, fontHeiht + 1, 0);
			}
			matrixStackIn.pop();
			matrixStackIn.pop();
			// TODO draw composite skills
			matrixStackIn.pop();
		}
	}
}
