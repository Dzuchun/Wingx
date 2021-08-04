package dzuchun.wingx.trick;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dzuchun.wingx.capability.entity.wings.IWingsCapability;
import dzuchun.wingx.capability.entity.wings.WingsProvider;
import dzuchun.wingx.capability.entity.wings.storage.FireballData;
import dzuchun.wingx.capability.entity.wings.storage.Serializers;
import dzuchun.wingx.entity.projectile.FireballEntity;
import dzuchun.wingx.init.Tricks;
import dzuchun.wingx.trick.state.TrickStates;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.PacketDistributor.PacketTarget;

public class FireballCastPlayerTrick extends AbstractInterruptablePlayerTrick implements ITimeredTrick {
	private static final Logger LOG = LogManager.getLogger();

	public FireballCastPlayerTrick(PlayerEntity caster) {
		super(caster, 1, InterruptCondition.NO_CONDITION);
	}

	@Override
	public PacketTarget getEndPacketTarget() {
		return this.hasCasterPlayer() ? PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) this.getCasterPlayer())
				: null;
	}

	@Override
	public PacketTarget getBackPacketTarget() {
		return this.hasCasterPlayer() ? PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) this.getCasterPlayer())
				: null;
	}

	@Override
	public void executeServer() {
		// We are on server
		if (this.hasCasterPlayer() && this.getCasterPlayer().getCapability(WingsProvider.WINGS, null).isPresent()
				&& (AbstractInterruptablePlayerTrick.playerBusyFor(this.getCasterPlayer()) == 0)) {
			IWingsCapability cap = this.getCasterPlayer().getCapability(WingsProvider.WINGS, null).orElse(null);
			// Cap is nonnul
			FireballData data = cap.getDataManager().getOrAddDefault(Serializers.FIREBALL_SERIALIZER);
			this.interruptCondition = data.interruptCondition;
			this.duration = data.castDuration;
			this.interruptCondition.reset();
			this.state = TrickStates.RUN;
		} else {
			LOG.warn("Caster does not exist, has no capability or caster is busy");
			if (!this.hasCasterPlayer()) {
				this.state = TrickStates.NO_CASTER;
			} else if (!this.getCasterPlayer().getCapability(WingsProvider.WINGS).isPresent()) {
				this.state = TrickStates.NO_WINGS;
			} else if (AbstractInterruptablePlayerTrick.playerBusyFor(this.getCasterPlayer()) != 0) {
				this.state = TrickStates.CASTER_BUSY;
			}
		}
		super.executeServer();
	}

	@Override
	public void onTrickEndServer() throws NoCasterException {
		super.onTrickEndServer();
		// We are on server
		if (!this.state.isError()) {
			PlayerEntity caster = this.getCasterPlayer();
			((ServerWorld) this.casterWorld).summonEntity(new FireballEntity(caster, false, false));
			caster.getCapability(WingsProvider.WINGS).ifPresent(cap -> {
				// Increment times casted
				cap.getDataManager().getOrAddDefault(Serializers.FIREBALL_SERIALIZER).timesCasted++;
			});
		}
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public void onTrickEndClient() throws NoCasterException {
		super.onTrickEndClient();
		// We are on client
		if (this.iAmCaster() && !this.state.isError()) {
			@SuppressWarnings("resource")
			ClientPlayerEntity player = Minecraft.getInstance().player;
			player.resetCooldown();
			player.swingArm(Hand.MAIN_HAND);
			this.state = TrickStates.SUCCESS;
			// TODO play sound
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

	public static class TrickType extends AbstractInterruptablePlayerTrick.TrickType<FireballCastPlayerTrick>
			implements ITimeredTrick.TrickType<FireballCastPlayerTrick> {

		@Override
		public FireballCastPlayerTrick newEmpty() {
			return new FireballCastPlayerTrick(null);
		}

	}

	@SuppressWarnings("unchecked")
	@Override
	public FireballCastPlayerTrick.TrickType getType() {
		return Tricks.FIREBALL_CAST_TRICK.get();
	}

}
