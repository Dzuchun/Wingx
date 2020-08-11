package dzuchun.wingx.client.abillity;

import javax.annotation.Nullable;

public abstract class ExternalAbillityNode extends AbillityNode {
	private InternalAbillityNode internal;

	public ExternalAbillityNode(int xCenterPos, int yCenterPos) {
		this(xCenterPos, yCenterPos, null);
	}

	public ExternalAbillityNode(int xCenterPos, int yCenterPos, @Nullable InternalAbillityNode internalIn) {
		this(xCenterPos, yCenterPos, internalIn, null);
	}

	public ExternalAbillityNode(int xCenterPos, int yCenterPos, @Nullable InternalAbillityNode internalIn,
			@Nullable ExternalAbillityNode parent) {
		super(xCenterPos, yCenterPos, parent);
		this.internal = internalIn;
	}

	public InternalAbillityNode getInternal() {
		return this.internal;
	}

	@Override
	public ExternalAbillityNode getParent() {
		return (ExternalAbillityNode) this.parent;
	}
}