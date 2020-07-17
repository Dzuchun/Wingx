package dzuchun.wingx.trick;

import javax.annotation.Nullable;

import net.minecraft.util.ResourceLocation;

public abstract class AbstractTrick implements ITrick {
	
	private ResourceLocation registryName = null;
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
}
