package dzuchun.wingx.trick;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;

public interface ITargetedTrick extends ITrick {

	void setTargetWorld(World worldIn);

	Entity getTarget();

	void setTarget(Entity entityIn);

	boolean hasTarget();
}
