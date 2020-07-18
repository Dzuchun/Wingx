package dzuchun.wingx.trick;

import net.minecraft.world.World;

public interface ITickableTrick extends IPersisableTrick {
	/**
	 * Called every tick on server.
	 * 
	 * @param worldIn
	 */
	void tick(World worldIn);
}
