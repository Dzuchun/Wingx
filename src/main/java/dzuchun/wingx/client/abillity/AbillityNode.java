package dzuchun.wingx.client.abillity;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mojang.blaze3d.matrix.MatrixStack;

import dzuchun.wingx.capability.entity.wings.IWingsCapability;
import net.minecraft.client.gui.AbstractGui;

public abstract class AbillityNode {
	@SuppressWarnings("unused")
	private static final Logger LOG = LogManager.getLogger();
	public static final int SIZE = 20; // TODO add config
	protected boolean isRoot;

	public boolean isRoot() {
		return this.isRoot;
	}

	protected AbillityNode parent;

	protected AbillityNode(int xCenterPosIn, int yCenterPosIn, AbillityNode parentIn) {
		this.xCenterPos = xCenterPosIn;
		this.yCenterPos = yCenterPosIn;
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

	public void render(MatrixStack matrixStackIn) {
		AbstractGui.fill(matrixStackIn, this.xCenterPos - SIZE / 2, this.yCenterPos - SIZE / 2,
				this.xCenterPos + SIZE / 2, this.yCenterPos + SIZE / 2, this.isUnlocked ? 0xFF00FF00 : 0xFFFF0000);
	}

	public abstract void setUnlocked(IWingsCapability capabilityIn);

	public boolean isUnlocked() {
		return this.isUnlocked;
	}

}
