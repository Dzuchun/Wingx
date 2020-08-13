package dzuchun.wingx.client.abillity;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnegative;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mojang.blaze3d.matrix.MatrixStack;

import dzuchun.wingx.capability.entity.wings.IWingsCapability;
import dzuchun.wingx.client.render.gui.SeparateRenderers;

public abstract class AbillityNode {
	private static final Logger LOG = LogManager.getLogger();
	private static final int SPRITE_SIZE = 32;
	private static final int ATLAS_SIZE = 512;
	private static final int SPRITES_IN_ROW = ATLAS_SIZE / SPRITE_SIZE;
	private static final float SPRITE_UV_SIZE = ((float) SPRITE_SIZE) / (float) ATLAS_SIZE;
	public static final int NODE_SIZE = 20; // TODO add config
	protected boolean isRoot;

	public boolean isRoot() {
		return this.isRoot;
	}

	protected AbillityNode parent;

	protected AbillityNode(int xCenterPosIn, int yCenterPosIn, @Nonnegative int spriteNoIn, AbillityNode parentIn) {
		this.xCenterPos = xCenterPosIn;
		this.yCenterPos = yCenterPosIn;
		this.spriteNo = spriteNoIn < 0 ? -1 : spriteNoIn;
		if (this.spriteNo < 0) {
			LOG.warn("{} uses invalid sprite number: {}", this, spriteNoIn);
		}
		if (parentIn == null) {
			this.isRoot = true;
		} else {
			this.isRoot = false;
			parentIn.children.add(this);
		}
	}

	public abstract AbillityNode getParent();

	protected List<AbillityNode> children = new ArrayList<AbillityNode>(0);

	public List<AbillityNode> getChildren() {
		return new ArrayList<>(this.children);
	}

	protected int xCenterPos;

	public int getXCenterPos() {
		return this.xCenterPos;
	}

	protected int yCenterPos;

	public int getYCenterPos() {
		return this.yCenterPos;
	}

	protected boolean isUnlocked;

	public void render(MatrixStack matrixStackIn, float alphaIn) {
		int packedColor = this.isUnlocked ? 0xFFFFFF00 : 0x88888800;
		packedColor += Math.round(alphaIn * 255);
//		LOG.debug("Rendering node with sprite number {}", spriteNo);
		matrixStackIn.push();
		matrixStackIn.scale(1.5f, 1.5f, 1.5f);
		matrixStackIn.push();
		drawNumeredSprite(matrixStackIn, 0, packedColor);
		matrixStackIn.pop();
		matrixStackIn.pop();
		matrixStackIn.push();
		drawNumeredSprite(matrixStackIn, this.spriteNo, packedColor);
		matrixStackIn.pop();
	}

	public abstract void setUnlocked(IWingsCapability capabilityIn);

	public boolean isUnlocked() {
		return this.isUnlocked;
	}

	private int spriteNo;

	/**
	 * Requires atlas to be binded previously and matrix on a centaer of node.
	 */
	private static void drawNumeredSprite(MatrixStack matrixStackIn, int spriteNoIn, int packedColor) {
		if (spriteNoIn == -1) {
			LOG.warn("Tried to draw node with unexisted atlas: {}", spriteNoIn);
			return;
		}
		int x = spriteNoIn % SPRITES_IN_ROW * SPRITE_SIZE;
		int y = spriteNoIn / SPRITES_IN_ROW * SPRITE_SIZE;
		float u = ((float) x) / ((float) ATLAS_SIZE);
		float v = ((float) y) / ((float) ATLAS_SIZE);
		SeparateRenderers.myBlit(matrixStackIn, -NODE_SIZE / 2, -NODE_SIZE / 2, NODE_SIZE, NODE_SIZE, u, v,
				SPRITE_UV_SIZE, SPRITE_UV_SIZE, packedColor);
	}

}
