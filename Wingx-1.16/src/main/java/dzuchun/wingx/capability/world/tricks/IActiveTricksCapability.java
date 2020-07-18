package dzuchun.wingx.capability.world.tricks;

import java.util.Collection;

import dzuchun.wingx.trick.IPersisableTrick;
import net.minecraft.world.World;

public interface IActiveTricksCapability {

	void addActiveTrick(IPersisableTrick trick);

	void addActiveTricks(Collection<IPersisableTrick> tricks);

	void onWorldTick(World worldIn);

	Collection<IPersisableTrick> getActiveTricks();
}