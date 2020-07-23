package dzuchun.wingx.trick;

import java.util.UUID;

import javax.annotation.Nonnull;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class PlayerTrick extends AbstractTrick {
	private static final Logger LOG = LogManager.getLogger();

	private UUID casterUniqueId = null; // TODO make protected!!

	public PlayerTrick() {
		super();
	}

	public PlayerTrick(@Nonnull PlayerEntity caster) {
		setCaster(caster);
	}

	public PlayerEntity getCaster(World worldIn) {
		return this.casterUniqueId == null ? null : worldIn.getPlayerByUuid(this.casterUniqueId);
	}

	public UUID getCasterUniUuid() {
		return this.casterUniqueId;
	}

	public boolean hasCaster(World worldIn) {
		return this.casterUniqueId != null && worldIn.getPlayerByUuid(this.casterUniqueId) != null ? true : false;
	}

	protected void setCaster(PlayerEntity caster) {
		if (caster == null) {
			LOG.warn("Tried to set caster null.");
		} else {
			setCaster(caster.getUniqueID());
		}
	}

	protected void setCaster(UUID uuid) {
		this.casterUniqueId = uuid;
	}

	@Override
	public ITrick readFromBuf(PacketBuffer buf) {
		if (buf.readBoolean()) {
			this.casterUniqueId = buf.readUniqueId();
		} else {
			LOG.warn("No caster found");
			this.casterUniqueId = null;
		}
		return this;
	}

	@Override
	public ITrick writeToBuf(PacketBuffer buf) {
		if (this.casterUniqueId == null) {
			buf.writeBoolean(false);
		} else {
			buf.writeBoolean(true);
			buf.writeUniqueId(this.casterUniqueId);
		}
		return this;
	}

	@OnlyIn(Dist.CLIENT)
	public boolean amICaster() {
		return this.casterUniqueId != null && this.casterUniqueId.equals(Minecraft.getInstance().player.getUniqueID());
	}
}
