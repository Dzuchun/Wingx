package dzuchun.wingx.trick;

import javax.annotation.Nullable;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;

public abstract class AbstractTrick implements ITrick {

	protected ResourceLocation registryName = null;
	protected boolean succesfull = true;

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
	public boolean executedSuccesfully() {
		return this.succesfull;
	}

	@Override
	public ITrick readFromBuf(PacketBuffer buf) {
		this.succesfull = buf.readBoolean();
		return this;
	}

	@Override
	public ITrick writeToBuf(PacketBuffer buf) {
		buf.writeBoolean(this.succesfull);
		return this;
	}

	protected abstract void setRegistryName();
}
