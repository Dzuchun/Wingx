package dzuchun.wingx.trick;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dzuchun.wingx.Wingx;
import dzuchun.wingx.capability.entity.wings.IWingsCapability;
import dzuchun.wingx.capability.entity.wings.WingsProvider;
import dzuchun.wingx.capability.entity.wings.storage.FireballData;
import dzuchun.wingx.capability.entity.wings.storage.Serializers;
import dzuchun.wingx.entity.projectile.FireballEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.PacketDistributor.PacketTarget;

public class FireballCastPlayerTrick extends AbstractInterruptablePlayerTrick implements ITimeredTrick {
	private static final ResourceLocation REGISTRY_NAME = new ResourceLocation(Wingx.MOD_ID,
			"fireball_cast_player_trick");
	private static final Logger LOG = LogManager.getLogger();

	public FireballCastPlayerTrick() {
		super();
	}

	public FireballCastPlayerTrick(PlayerEntity caster) {
		super(caster, 1, InterruptCondition.NO_CONDITION);
	}

	@Override
	public PacketTarget getEndPacketTarget() {
		return hasCasterPlayer() ? PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) getCasterPlayer()) : null;
	}

	@Override
	public PacketTarget getBackPacketTarget() {
		return hasCasterPlayer() ? PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) getCasterPlayer()) : null;
	}

	@Override
	public ITrick newEmpty() {
		return new FireballCastPlayerTrick();
	}

	@Override
	protected void setRegistryName() {
		this.registryName = REGISTRY_NAME;
	}

	@Override
	public void execute(LogicalSide side) {
		if (side == LogicalSide.SERVER) {
			if (hasCasterPlayer() && getCasterPlayer().getCapability(WingsProvider.WINGS, null).isPresent()
					&& AbstractInterruptablePlayerTrick.playerBusyFor(getCasterPlayer()) == 0) {
				IWingsCapability cap = getCasterPlayer().getCapability(WingsProvider.WINGS, null).orElse(null);
				// Cap is nonnul
				FireballData data = cap.getDataManager().getOrAddDefault(Serializers.FIREBALL_SERIALIZER);
				this.interruptCondition = data.interruptCondition;
				this.duration = data.castDuration;
				this.interruptCondition.reset();
				this.succesfull = true;
			} else {
				this.succesfull = false;
				LOG.warn("Caster does not exist, has no capability or caster is busy");
			}
		} else {
			if (amICaster()) {
				Minecraft minecraft = Minecraft.getInstance();
				minecraft.player.sendStatusMessage(
						this.succesfull ? new TranslationTextComponent("wingx.trick.fireball.starting_execute")
								: new TranslationTextComponent("wingx.trick.fireball.fail_execute"),
						true);
			}
		}
		super.execute(side);
	}

	@SuppressWarnings("resource")
	@Override
	public void onCastEnd(LogicalSide side) {
		super.onCastEnd(side);
		if (side == LogicalSide.SERVER) {
			if (castEndedNaturally()) {
				((ServerWorld) this.casterWorld).summonEntity(new FireballEntity(getCasterPlayer()));
			}
		} else {
			if (amICaster()) {
				ClientPlayerEntity player = Minecraft.getInstance().player;
				player.sendStatusMessage(this.succesfull ? new TranslationTextComponent("wingx.trick.fireball.executed")
						: new TranslationTextComponent("wingx.trick.fireball.fail"), true);
				if (this.succesfull) {
					player.resetCooldown();
					player.swingArm(Hand.MAIN_HAND);
					// TODO play sound
				}
			}
		}
	}

	@Override
	public int timeFull() throws NoCasterException {
		return this.duration;
	}

	@Override
	public double partLeft() throws NoCasterException {
		long current = this.casterWorld.getGameTime();
		return (this.endTime - current) / (float) this.duration;
	}

}
