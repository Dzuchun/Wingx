package dzuchun.wingx.config.abillity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnegative;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mojang.blaze3d.matrix.MatrixStack;

import dzuchun.wingx.client.render.gui.SeparateRenderers;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class AbillityNode extends ArrayList<Object> {
	private static final long serialVersionUID = 1L;
	private static final Logger LOG = LogManager.getLogger();
	private static final int SPRITE_SIZE = 32;
	private static final int ATLAS_SIZE = 512;
	private static final int SPRITES_IN_ROW = ATLAS_SIZE / SPRITE_SIZE;
	private static final float SPRITE_UV_SIZE = ((float) SPRITE_SIZE) / (float) ATLAS_SIZE;
	public static final int NODE_SIZE = 20; // TODO add config

	public ArrayList<AbillityNode> children = new ArrayList<AbillityNode>(0);
	public AbillityNode parent = null, internal = null;

	public AbillityNode getParent() {
		return this.parent;
	}

	public AbillityNode getInternal() {
		return this.internal;
	}

	boolean unlockedDirty = true;
	boolean isUnlocked = false;

	public void updateUnlocked(Map<String, Integer> statsIn, Map<String, Object> dataIn) {
		if (this.unlockedDirty) {
			String unlockCondition = this.getUnlockCondition();
			this.isUnlocked = true;
			String[] conditions = unlockCondition.split(" ");
			for (String condition : conditions) {
				if ((condition.length() > 0) && !AbillityNodes.evaluateCondition(condition, statsIn, dataIn)) {
					this.isUnlocked = false;
					break;
				}
			}
			this.unlockedDirty = false;
		}
	}

	public boolean isUnlocked() {
		return this.isUnlocked;
	}

	public AbillityNode(List<Object> list) {
		this((int) list.get(0), (int) list.get(1), (int) list.get(2), (int) list.get(3), (int) list.get(4),
				(int) list.get(5), (String) list.get(6), (String) list.get(7), (String) list.get(8));
	}

	public AbillityNode(int id, int xCenterPosIn, int yCenterPosIn, @Nonnegative int spriteNoIn, int parentIdIn,
			int internalIdIn, String nameLocIn, String descLocIn, String unclockConditionIn) {
		super(0);
		this.add(id);// 0
		this.add(xCenterPosIn);// 1
		this.add(yCenterPosIn);// 2
		this.add(spriteNoIn);// 3
		if (spriteNoIn < 0) {
			LOG.warn("{} uses invalid sprite number: {}", this, spriteNoIn);
			// TODO check for max sprite no
		}
		this.add(parentIdIn);// 4
		this.add(internalIdIn);// 5
		this.add(nameLocIn);// 6
		this.add(descLocIn);// 7
		this.add(unclockConditionIn); // 8
	}

	public int getId() {
		return (int) this.get(0);
	}

	public int xCenterPos() {
		return (int) this.get(1);
	}

	public int yCenterPos() {
		return (int) this.get(2);
	}

	public int getSpriteNo() {
		return (int) this.get(3);
	}

	public int getParentId() {
		return (int) this.get(4);
	}

	public int getInternalId() {
		return (int) this.get(5);
	}

	public String getNameLoc() {
		return (String) this.get(6);
	}

	public String getDescLoc() {
		return (String) this.get(7);
	}

	public String getUnlockCondition() {
		return (String) this.get(8);
	}

	public boolean hasParent() {
		return (int) this.get(4) != -1;
	}

	public boolean hasInternal() {
		return (int) this.get(5) != -1;
	}

	@OnlyIn(Dist.CLIENT)
	public void render(MatrixStack matrixStackIn, float alphaIn) {
		int packedColor = this.isUnlocked() ? 0xFFFFFF00 : 0x88888800;
		packedColor += Math.round(alphaIn * 255);
		matrixStackIn.push();
		matrixStackIn.scale(1.5f, 1.5f, 1.5f);
		matrixStackIn.push();
		drawNumeredSprite(matrixStackIn, 0, packedColor);
		matrixStackIn.pop();
		matrixStackIn.pop();
		matrixStackIn.push();
		drawNumeredSprite(matrixStackIn, (int) this.get(3), packedColor);
		matrixStackIn.pop();
	}

	/**
	 * Requires atlas to be binded previously and matrix on a center of node.
	 */
	@OnlyIn(Dist.CLIENT)
	private static void drawNumeredSprite(MatrixStack matrixStackIn, int spriteNoIn, int packedColor) {
		if (spriteNoIn == -1) {
			LOG.warn("Tried to draw node with unexisted atlas: {}", spriteNoIn);
			return;
		}
		int x = (spriteNoIn % SPRITES_IN_ROW) * SPRITE_SIZE;
		int y = (spriteNoIn / SPRITES_IN_ROW) * SPRITE_SIZE;
		float u = ((float) x) / ((float) ATLAS_SIZE);
		float v = ((float) y) / ((float) ATLAS_SIZE);
		SeparateRenderers.myBlit(matrixStackIn, -NODE_SIZE / 2, -NODE_SIZE / 2, NODE_SIZE, NODE_SIZE, u, v,
				SPRITE_UV_SIZE, SPRITE_UV_SIZE, packedColor);
	}

}
