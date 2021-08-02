package dzuchun.wingx.capability.world.tricks;

import java.util.Collection;

import dzuchun.wingx.trick.IInterruptableTrick;
import net.minecraft.world.World;

public interface IActiveTricksCapability {

	boolean addActiveTrick(IInterruptableTrick trick);

	void addActiveTricks(Collection<IInterruptableTrick> tricks);

	boolean removeActiveTrick(IInterruptableTrick trick);

	boolean removeActiveTricks(Collection<IInterruptableTrick> tricks);

	void clearActiveTricks();

	void onWorldTick(World worldIn);

	Collection<IInterruptableTrick> getActiveTricks();
}