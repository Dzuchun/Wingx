package dzuchun.wingx.trick;

import net.minecraft.world.World;
import net.minecraftforge.fml.LogicalSide;

public interface IExecutableTrick extends ITrick {
	/**
	 * 1-time executed code on logical side.
	 * @param side Side should executed on.
	 * @param worldIn World should be executed in.
	 */
	void execute(LogicalSide side, World worldIn);
	/**
	 * @return If trick execution was succesful at server
	 */
	boolean executedSuccesfully();
}
