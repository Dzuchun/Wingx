package dzuchun.wingx.trick;

import java.util.UUID;

import javax.annotation.Nullable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dzuchun.wingx.util.WorldHelper;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;

public abstract class AbstractCastedTrick extends AbstractTrick implements ICastedTrick {
	private static final Logger LOG = LogManager.getLogger();
	protected World casterWorld = null;
	protected UUID casterUniqueId = null;

	public static void assertHasCasterInfo(AbstractCastedTrick trick) throws NoCasterException {
		if ((trick.casterUniqueId == null) || (trick.casterWorld == null)) {
			throw new NoCasterException(trick);
		}
	}

	public static void assertHasCaster(ICastedTrick trick) throws NoCasterException {
		if (!trick.hasCaster()) {
			throw new NoCasterException(trick);
		}
	}

	public AbstractCastedTrick() {
		super();
	}

	public AbstractCastedTrick(@Nullable Entity caster) {
		super();
		if (caster != null) {
			this.setCaster(caster);
		}
	}

	@Override
	public void setWorld(World worldIn) {
		this.casterWorld = worldIn;
	}

	@Override
	public void setCaster(Entity entityIn) {
		LOG.debug("Setting caster of {} to {}", this, entityIn);
		this.casterUniqueId = entityIn.getUniqueID();
		this.casterWorld = entityIn.world;
	}

	@Nullable
	@Override
	public Entity getCaster() {
//		LOG.info("Getting trick caster: word {}, uuid {}", casterWorld, casterUniqueId);
		return (this.casterUniqueId == null) || (this.casterWorld == null) ? null
				: WorldHelper.getEntityFromWorldByUniqueId(this.casterWorld, this.casterUniqueId);
	}

	@Override
	public boolean hasCaster() {
		return (this.casterUniqueId == null) || (this.casterWorld == null) ? false : this.getCaster() != null;
	}

	@Override
	public ITrick readFromBuf(PacketBuffer buf) {
		if (buf.readBoolean()) {
			this.casterUniqueId = buf.readUniqueId();
		} else {
			LOG.warn("No caster found");
			this.casterUniqueId = null;
		}
		return super.readFromBuf(buf);
	}

	@Override
	public ITrick writeToBuf(PacketBuffer buf) {
		if (!this.hasCaster()) {
			buf.writeBoolean(false);
		} else {
			buf.writeBoolean(true);
			buf.writeUniqueId(this.casterUniqueId);
		}
		return super.writeToBuf(buf);
	}
}
