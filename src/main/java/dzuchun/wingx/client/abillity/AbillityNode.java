package dzuchun.wingx.client.abillity;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mojang.blaze3d.matrix.MatrixStack;

import dzuchun.wingx.capability.entity.wings.IWingsCapability;
import dzuchun.wingx.util.Util;
import net.minecraft.client.gui.AbstractGui;

public abstract class AbillityNode {
	private static final Logger LOG = LogManager.getLogger();
	public static final int size = 20; // TODO add config
	protected boolean isRoot;

	public boolean isRoot() {
		return isRoot;
	}

	protected AbillityNode parent;

	protected AbillityNode(int xCenterPosIn, int yCenterPosIn, AbillityNode parentIn) {
		xCenterPos = xCenterPosIn;
		yCenterPos = yCenterPosIn;
		if (parentIn == null) {
			isRoot = true;
		} else {
			isRoot = false;
			parentIn.children.add(this);
		}
//		LOG.debug("Creating node {} at [{}, {}] with parent {}, siblings - {}", this, xCenterPosIn, yCenterPosIn,
//				parentIn, parentIn == null ? "[]" : Util.iterableToString(parentIn.children));
	}

	public abstract AbillityNode getParent();

	protected List<AbillityNode> children = new ArrayList<AbillityNode>(0);

	public List<AbillityNode> getChildren() {
		return new ArrayList<>(children);
	}

	protected int xCenterPos;

	public int getXCenterPos() {
		return xCenterPos;
	}

	protected int yCenterPos;

	public int getYCenterPos() {
		return yCenterPos;
	}

	protected boolean isUnlocked;

	public void render(MatrixStack matrixStackIn) {
		AbstractGui.fill(matrixStackIn, xCenterPos - size / 2, yCenterPos - size / 2, xCenterPos + size / 2,
				yCenterPos + size / 2, isUnlocked ? 0xFF00FF00 : 0xFFFF0000);
	}

	public abstract void setUnlocked(IWingsCapability capabilityIn);

}
