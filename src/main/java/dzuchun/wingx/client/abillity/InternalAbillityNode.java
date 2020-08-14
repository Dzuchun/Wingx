package dzuchun.wingx.client.abillity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.util.text.ITextComponent;

public abstract class InternalAbillityNode extends AbillityNode {

	public InternalAbillityNode(int xCenterPos, int yCenterPos, int spriteNoIn, @Nonnull ITextComponent displayNameIn,
			@Nonnull ITextComponent displayDescriptionIn) {
		this(xCenterPos, yCenterPos, spriteNoIn, displayNameIn, displayDescriptionIn, null);
	}

	public InternalAbillityNode(int xCenterPos, int yCenterPos, int spriteNoIn, @Nonnull ITextComponent displayNameIn,
			@Nonnull ITextComponent displayDescriptionIn, @Nullable InternalAbillityNode parent) {
		super(xCenterPos, yCenterPos, spriteNoIn, displayNameIn, displayDescriptionIn, parent);
	}

	@Override
	public InternalAbillityNode getParent() {
		return (InternalAbillityNode) this.parent;
	}

}