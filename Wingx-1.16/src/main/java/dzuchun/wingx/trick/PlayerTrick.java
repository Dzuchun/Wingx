package dzuchun.wingx.trick;

import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;

public class PlayerTrick extends AbstractTrick {
	private static final Logger LOG = LogManager.getLogger();

	private UUID casterUniqueId = null;

	public PlayerTrick() {
		super();
	}
	
	public PlayerTrick(PlayerEntity caster) {
		setCaster(caster);
	}
	
	public PlayerEntity getCaster(World worldIn) {
		return casterUniqueId == null ? null : worldIn.getPlayerByUuid(casterUniqueId);
	}

	public boolean hasCaster(World worldIn) {
		return worldIn.getPlayerByUuid(casterUniqueId) != null ? true : false;
	}

	protected void setCaster(PlayerEntity caster) {
		if (caster == null) {
			LOG.warn("");
		} else {
			setCaster(caster.getUniqueID());
		}
	}

	protected void setCaster(UUID uuid) {
		casterUniqueId = uuid;
	}

	@Override
	public ITrick readFromBuf(PacketBuffer buf) {
		this.casterUniqueId = buf.readUniqueId();
		return this;
	}

	@Override
	public ITrick writeToBuf(PacketBuffer buf) {
		buf.writeUniqueId(casterUniqueId);
		return this;
	}
}
