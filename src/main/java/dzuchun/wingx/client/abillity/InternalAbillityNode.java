package dzuchun.wingx.client.abillity;

import javax.annotation.Nullable;

public abstract class InternalAbillityNode extends AbillityNode {

	public InternalAbillityNode(int xCenterPos, int yCenterPos, int spriteNoIn) {
		this(xCenterPos, yCenterPos, spriteNoIn, null);
	}

	public InternalAbillityNode(int xCenterPos, int yCenterPos, int spriteNoIn, @Nullable InternalAbillityNode parent) {
		super(xCenterPos, yCenterPos, spriteNoIn, parent);
	}

	@Override
	public InternalAbillityNode getParent() {
		return (InternalAbillityNode) this.parent;
	}

}