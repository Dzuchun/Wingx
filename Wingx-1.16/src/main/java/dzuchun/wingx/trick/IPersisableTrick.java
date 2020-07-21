package dzuchun.wingx.trick;

import net.minecraft.nbt.INBT;
import net.minecraft.world.World;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.PacketDistributor.PacketTarget;

public interface IPersisableTrick extends ITrick {
	/**
	 * Defines if trick should be keep in memory.
	 *
	 * @param side
	 * @param worldIn
	 * @return If trick should be keep till next tick.
	 */
	boolean keepExecuting(World worldIn);

	/**
	 * Called, whenever keepExecuting returned false;
	 *
	 * @param side
	 * @param worldIn
	 */
	void stopExecute(LogicalSide side, World worldIn);

	void readFromNBT(INBT nbt);

	INBT writeToNBT();

	PacketTarget getEndPacketTarget(World worldIn);
}
