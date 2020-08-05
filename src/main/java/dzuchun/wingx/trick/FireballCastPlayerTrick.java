package dzuchun.wingx.trick;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dzuchun.wingx.Wingx;
import dzuchun.wingx.capability.entity.wings.IWingsCapability;
import dzuchun.wingx.capability.entity.wings.WingsProvider;
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
		registryName = REGISTRY_NAME;
	}

	@Override
	public void execute(LogicalSide side) {
		if (side == LogicalSide.SERVER) {
			if (hasCasterPlayer() && getCasterPlayer().getCapability(WingsProvider.WINGS, null).isPresent()
					&& AbstractInterruptablePlayerTrick.playerBusyFor(getCasterPlayer()) == 0) {
				IWingsCapability cap = getCasterPlayer().getCapability(WingsProvider.WINGS, null).orElse(null);
				// Cap is nonnul
				duration = cap.fireballCastDuration();
				interruptCondition = cap.fireballInterruptCondition();
				interruptCondition.reset();
				succesfull = true;
			} else {
				succesfull = false;
				LOG.warn("Caster does not exist, has no capability or caster is busy");
			}
		} else {
			if (amICaster()) {
				Minecraft minecraft = Minecraft.getInstance();
				minecraft.player
						.sendStatusMessage(succesfull ? new TranslationTextComponent("wingx.fireball.starting_execute")
								: new TranslationTextComponent("wingx.fireball.failt_execute"), true);
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
				((ServerWorld) casterWorld).summonEntity(new FireballEntity(getCasterPlayer()));
			}
		} else {
			if (amICaster()) {
				ClientPlayerEntity player = Minecraft.getInstance().player;
				player.sendStatusMessage(succesfull ? new TranslationTextComponent("wingx.fireball.executed")
						: new TranslationTextComponent("wingx.fireball.fail"), true);
				if (succesfull) {
					player.resetCooldown();
					player.swingArm(Hand.MAIN_HAND);
					// TODO play sound
				}
			}
		}
	}

	@Override
	public int timeFull() throws NoCasterException {
		return duration;
	}

	@Override
	public double partLeft() throws NoCasterException {
		long current = casterWorld.getGameTime();
		return (endTime - current) / (float) duration;
	}

}
