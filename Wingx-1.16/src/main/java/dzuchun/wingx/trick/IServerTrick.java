package dzuchun.wingx.trick;

import net.minecraft.world.World;
import net.minecraftforge.fml.network.PacketDistributor.PacketTarget;

/**
 * Instances of this interface will be send to server for update.
 * 
 * @author dzu
 */
public interface IServerTrick extends ITrick {
	boolean executedSuccesfully();

	PacketTarget getBackPacketTarget(World worldIn);
}
