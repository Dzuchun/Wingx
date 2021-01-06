package dzuchun.wingx.entity.projectile;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dzuchun.wingx.capability.entity.wings.IWingsCapability;
import dzuchun.wingx.capability.entity.wings.WingsProvider;
import dzuchun.wingx.capability.entity.wings.storage.FireballData;
import dzuchun.wingx.capability.entity.wings.storage.Serializers;
import dzuchun.wingx.trick.NoWingsException;
import dzuchun.wingx.util.WorldHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.common.util.LazyOptional;

public class HomingFireballEntity extends FireballEntity {
	private static final Logger LOG = LogManager.getLogger();

	protected double homingForce;
	protected Entity target;

	public HomingFireballEntity(PlayerEntity caster, Entity target) throws NoWingsException {
		super(caster);
		this.target = target;
		LazyOptional<IWingsCapability> wingsOptional = caster.getCapability(WingsProvider.WINGS, null);
		if (!wingsOptional.isPresent()) {
			throw (new NoWingsException(caster));
		}
		IWingsCapability wings = wingsOptional.orElse(null);
		FireballData data = wings.getDataManager().getOrAddDefault(Serializers.FIREBALL_SERIALIZER);
		this.homingForce = data.homingForce;
	}

	@Override
	public void tick() {
		super.tick();
		if (this.target != null) {
			// TODO find better algorythm
			Vector3d speed = this.getMotion();
			Vector3d targetRelPos = this.target.getPositionVec().add(this.getPositionVec().scale(-1.0d));
			Vector3d targetPosNormed = targetRelPos.normalize().scale(speed.length());
			Vector3d speedDiffer = targetPosNormed.add(speed.scale(-1.0d));
			Vector3d speedDelta = speedDiffer.normalize().scale(this.homingForce);
//			LOG.debug(
//					"Results: rel pos = {}; rel pos normed = {}; speedDiffer = {}; homing force = {}, speedDelta = {}",
//					targetRelPos, targetPosNormed, speedDiffer, homingForce, speedDelta);
			if (speedDelta.length() >= speedDiffer.length()) {
				double differSq = speedDiffer.lengthSquared();
				double accSq = (this.homingForce * this.homingForce) - differSq;
				speedDelta = speedDelta.normalize().scale(Math.sqrt(differSq))
						.add(targetRelPos.normalize().scale(Math.sqrt(accSq)));
			}
//			LOG.info("Targeting {}, so adding {} to speed", target, targetRelPos, speedDelta);
			this.addMotion(speedDelta);
		} else {
			LOG.warn("Target is null!!");
		}
	}

	private static final String HOMING_FORCE_TAG = "homing_force";
	private static final String TARGET_TAG = "target";

	@Override
	protected void writeAdditional(CompoundNBT compound) {
		compound.putDouble(HOMING_FORCE_TAG, this.homingForce);
		compound.putUniqueId(TARGET_TAG, this.entityUniqueID);
		super.writeAdditional(compound);
	}

	@Override
	protected void readAdditional(CompoundNBT compound) {
		this.homingForce = compound.getDouble(HOMING_FORCE_TAG);
		this.target = WorldHelper.getEntityFromWorldByUniqueId(this.world, compound.getUniqueId(TARGET_TAG));
		super.readAdditional(compound);
	}

}
