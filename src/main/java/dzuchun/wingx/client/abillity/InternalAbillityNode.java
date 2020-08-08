package dzuchun.wingx.client.abillity;

import javax.annotation.Nullable;

public abstract class InternalAbillityNode extends AbillityNode {

	public InternalAbillityNode(int xCenterPos, int yCenterPos) {
		this(xCenterPos, yCenterPos, null);
	}

	public InternalAbillityNode(int xCenterPos, int yCenterPos, @Nullable InternalAbillityNode parent) {
		super(xCenterPos, yCenterPos, parent);
	}

	@Override
	public InternalAbillityNode getParent() {
		return (InternalAbillityNode) parent;
	}

}