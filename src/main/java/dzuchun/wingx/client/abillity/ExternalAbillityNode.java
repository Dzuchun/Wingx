package dzuchun.wingx.client.abillity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.util.text.ITextComponent;

public abstract class ExternalAbillityNode extends AbillityNode {
	private InternalAbillityNode internal;

	public ExternalAbillityNode(int xCenterPos, int yCenterPos, int spriteNoIn, @Nonnull ITextComponent displayNameIn,
			@Nonnull ITextComponent displayDescriptionIn) {
		this(xCenterPos, yCenterPos, spriteNoIn, displayNameIn, displayDescriptionIn, null);
	}

	public ExternalAbillityNode(int xCenterPos, int yCenterPos, int spriteNoIn, @Nonnull ITextComponent displayNameIn,
			@Nonnull ITextComponent displayDescriptionIn, @Nullable InternalAbillityNode internalIn) {
		this(xCenterPos, yCenterPos, spriteNoIn, displayNameIn, displayDescriptionIn, internalIn, null);
	}

	public ExternalAbillityNode(int xCenterPos, int yCenterPos, int spriteNoIn, @Nonnull ITextComponent displayNameIn,
			@Nonnull ITextComponent displayDescriptionIn, @Nullable InternalAbillityNode internalIn,
			@Nullable ExternalAbillityNode parent) {
		super(xCenterPos, yCenterPos, spriteNoIn, displayNameIn, displayDescriptionIn, parent);
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