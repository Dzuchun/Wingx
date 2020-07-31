package dzuchun.wingx.trick;

import java.util.UUID;

import dzuchun.wingx.util.WorldHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class AbstractTargetedPlayerTrick extends AbstractPlayerCastedTrick implements ITargetedTrick {
	public AbstractTargetedPlayerTrick() {
		super();
	}

	public AbstractTargetedPlayerTrick(Entity target, PlayerEntity caster) {
		super(caster);
		if (target != null) {
			this.targetUniqueId = target.getUniqueID();
		}
	}

	protected World targetWorld = null;
	protected UUID targetUniqueId = null;

	@Override
	public boolean hasTarget() {
		return this.targetUniqueId != null && this.targetWorld != null
				&& WorldHelper.getEntityFromWorldByUniqueId(this.targetWorld, this.targetUniqueId) != null;
	}

	@Override
	public Entity getTarget() {
		return this.targetWorld == null || this.targetUniqueId == null ? null
				: WorldHelper.getEntityFromWorldByUniqueId(this.targetWorld, this.targetUniqueId);
	}

	@Override
	public void setTarget(Entity entityIn) {
		if (entityIn != null) {
			this.targetUniqueId = entityIn.getUniqueID();
			this.targetWorld = entityIn.world;
		}
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

	@Override
	public void setTargetWorld(World worldIn) {
		this.targetWorld = worldIn;
	}
}
