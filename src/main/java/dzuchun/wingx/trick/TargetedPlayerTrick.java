package dzuchun.wingx.trick;

import java.util.UUID;

import dzuchun.wingx.util.WorldHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class TargetedPlayerTrick extends PlayerTrick {
	public TargetedPlayerTrick() {
		super();
	}

	public TargetedPlayerTrick(Entity target, PlayerEntity caster) {
		super(caster);
		if (target != null) {
			this.targetUniqueId = target.getUniqueID();
		}
	}

	protected UUID targetUniqueId = null;

	public boolean hasTarget(ServerWorld worldIn) {
		return this.targetUniqueId != null
				&& WorldHelper.getEntityFromWorldByUniqueId(worldIn, this.targetUniqueId) != null;
	}

	public Entity getTarget(ServerWorld worldIn) {
		return WorldHelper.getEntityFromWorldByUniqueId(worldIn, this.targetUniqueId);
	}

	protected void setTarget(UUID targetUniqueId) {
		this.targetUniqueId = targetUniqueId;
	}

	protected void setTarget(Entity entity) {
		if (entity != null) {
			setTarget(entity.getUniqueID());
		}
	}

	public UUID getTargetUniqueId() {
		return this.targetUniqueId;
	}

	@Override
	public ITrick readFromBuf(PacketBuffer buf) {
		if (buf.readBoolean()) {
			this.targetUniqueId = buf.readUniqueId();
		} else {
			this.targetUniqueId = null;
		}
		return super.readFromBuf(buf);
	}

	@Override
	public ITrick writeToBuf(PacketBuffer buf) {
		if (this.targetUniqueId == null) {
			buf.writeBoolean(false);
		} else {
			buf.writeBoolean(true);
			buf.writeUniqueId(this.targetUniqueId);
		}
		return super.writeToBuf(buf);
	}

	@OnlyIn(Dist.CLIENT)
	public boolean amITarget() {
		return this.targetUniqueId != null && this.targetUniqueId.equals(Minecraft.getInstance().player.getUniqueID());
	}
}
