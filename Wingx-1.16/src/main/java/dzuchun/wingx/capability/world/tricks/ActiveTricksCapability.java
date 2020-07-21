package dzuchun.wingx.capability.world.tricks;

import java.util.ArrayList;
import java.util.Collection;

import dzuchun.wingx.net.TrickFinishMessage;
import dzuchun.wingx.net.WingxPacketHandler;
import dzuchun.wingx.trick.IPersisableTrick;
import dzuchun.wingx.trick.ITickableTrick;
import net.minecraft.world.World;
import net.minecraftforge.fml.LogicalSide;

public class ActiveTricksCapability implements IActiveTricksCapability {

	ArrayList<IPersisableTrick> active_tricks = new ArrayList<IPersisableTrick>(0);
	private final Object ACTIVE_TRICKS_LOCK = new Object();

	@Override
	public void addActiveTrick(IPersisableTrick trick) {
		synchronized (this.ACTIVE_TRICKS_LOCK) {
			this.active_tricks.add(trick);
		}
	}

	@Override
	public void addActiveTricks(Collection<IPersisableTrick> tricks) {
		synchronized (this.ACTIVE_TRICKS_LOCK) {
			this.active_tricks.addAll(tricks);
		}
	}

	@Override
	public void onWorldTick(World worldIn) {
		synchronized (this.ACTIVE_TRICKS_LOCK) {
			Collection<IPersisableTrick> ended_tricks = new ArrayList<IPersisableTrick>(0);
			this.active_tricks.forEach((IPersisableTrick trick) -> {
				if (trick.keepExecuting(worldIn) && trick instanceof ITickableTrick) {
					((ITickableTrick) trick).tick(worldIn);
				} else {
					ended_tricks.add(trick);
				}
			});
			this.active_tricks.removeAll(ended_tricks);
			ended_tricks.forEach((IPersisableTrick trick) -> {
				trick.stopExecute(LogicalSide.SERVER, worldIn);
				WingxPacketHandler.INSTANCE.send(trick.getEndPacketTarget(worldIn), new TrickFinishMessage(trick));
			});
			this.active_tricks.trimToSize();
		}
	}

	@Override
	public Collection<IPersisableTrick> getActiveTricks() {
		return this.active_tricks;
	}

}