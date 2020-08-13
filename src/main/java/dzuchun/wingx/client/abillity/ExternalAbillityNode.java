package dzuchun.wingx.client.abillity;

import javax.annotation.Nullable;

public abstract class ExternalAbillityNode extends AbillityNode {
	private InternalAbillityNode internal;

	public ExternalAbillityNode(int xCenterPos, int yCenterPos, int spriteNoIn) {
		this(xCenterPos, yCenterPos, spriteNoIn, null);
	}

	public ExternalAbillityNode(int xCenterPos, int yCenterPos, int spriteNoIn,
			@Nullable InternalAbillityNode internalIn) {
		this(xCenterPos, yCenterPos, spriteNoIn, internalIn, null);
	}

	public ExternalAbillityNode(int xCenterPos, int yCenterPos, int spriteNoIn,
			@Nullable InternalAbillityNode internalIn, @Nullable ExternalAbillityNode parent) {
		super(xCenterPos, yCenterPos, spriteNoIn, parent);
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