package dzuchun.wingx.trick;

import javax.annotation.Nullable;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;

public abstract class AbstractTrick implements ITrick {

	protected ResourceLocation registryName = null;
	protected int status = 0;

	public AbstractTrick() {
		this.setRegistryName();
	}

	@Override
	public AbstractTrick setRegistryName(ResourceLocation name) {
		this.registryName = name;
		return this;
	}

	@Nullable
	@Override
	public ResourceLocation getRegistryName() {
		return this.registryName;
	}

	@Override
	public Class<AbstractTrick> getRegistryType() {
		return AbstractTrick.class;
	}

	@Override
	public int getStatus() {
		return this.status;
	}

	@Override
	public ITrick readFromBuf(PacketBuffer buf) {
		this.status = buf.readInt();
		return this;
	}

	@Override
	public ITrick writeToBuf(PacketBuffer buf) {
		buf.writeInt(this.status);
		return this;
	}

	protected abstract void setRegistryName();
}
