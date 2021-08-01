package dzuchun.wingx.capability.world.tricks;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dzuchun.wingx.net.TrickFinishMessage;
import dzuchun.wingx.net.WingxPacketHandler;
import dzuchun.wingx.trick.IInterruptableTrick;
import net.minecraft.world.World;

public class ActiveTricksCapability implements IActiveTricksCapability {
	private static final Logger LOG = LogManager.getLogger();

	ArrayList<IInterruptableTrick> active_tricks = new ArrayList<IInterruptableTrick>(0);
	private final Object ACTIVE_TRICKS_LOCK = new Object();

	@Override
	public void addActiveTrick(IInterruptableTrick trick) {
		synchronized (this.ACTIVE_TRICKS_LOCK) {
			LOG.info("Adding {} to active tricks", trick);
			this.active_tricks.add(trick);
		}
	}

	@Override
	public void addActiveTricks(Collection<IInterruptableTrick> tricks) {
		if (tricks.isEmpty()) {
			return;
		}
		synchronized (this.ACTIVE_TRICKS_LOCK) {
			LOG.info("Adding {} to active tricks", tricks);
			this.active_tricks.addAll(tricks);
		}
	}

	@Override
	public boolean removeActiveTrick(IInterruptableTrick trick) {
		synchronized (this.ACTIVE_TRICKS_LOCK) {
			WingxPacketHandler.INSTANCE.send(trick.getEndPacketTarget(), new TrickFinishMessage(trick));
			LOG.info("Removing {} from active tricks", trick);
			return this.active_tricks.remove(trick);
		}
	}

	@Override
	public boolean removeActiveTricks(Collection<IInterruptableTrick> tricks) {
		if (tricks.isEmpty()) {
			return true;
		}
		synchronized (this.ACTIVE_TRICKS_LOCK) {
			tricks.forEach(trick -> {
				trick.onTrickEndServer();
				WingxPacketHandler.INSTANCE.send(trick.getEndPacketTarget(), new TrickFinishMessage(trick));
			});
			LOG.info("Removing {} from active tricks", tricks);
			return this.active_tricks.removeAll(tricks);
		}
	}

	@Override
	public void onWorldTick(World worldIn) {
		synchronized (this.ACTIVE_TRICKS_LOCK) {
			Collection<IInterruptableTrick> ended_tricks = new ArrayList<IInterruptableTrick>(0);
			this.active_tricks.forEach(trick -> {
				trick.tick();
				if (!trick.keepExecuting()) {
					ended_tricks.add(trick);
				}
			});
			if (!ended_tricks.isEmpty()) {
				this.removeActiveTricks(ended_tricks);
				this.active_tricks.trimToSize();
			}
		}
	}

	@Override
	public Collection<IInterruptableTrick> getActiveTricks() {
		return this.active_tricks;
	}

	@Override
	public void clearActiveTricks() {
		synchronized (this.ACTIVE_TRICKS_LOCK) {
			this.active_tricks.clear();
		}
	}

}