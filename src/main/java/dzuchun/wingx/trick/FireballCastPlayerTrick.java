package dzuchun.wingx.trick;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.ImmutableList;

import dzuchun.wingx.capability.entity.wings.IWingsCapability;
import dzuchun.wingx.capability.entity.wings.WingsProvider;
import dzuchun.wingx.capability.entity.wings.storage.FireballData;
import dzuchun.wingx.capability.entity.wings.storage.Serializers;
import dzuchun.wingx.entity.projectile.FireballEntity;
import dzuchun.wingx.init.Tricks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
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
			this.status = 0;
		} else {
			LOG.warn("Caster does not exist, has no capability or caster is busy");
			if (!this.hasCasterPlayer()) {
				this.status = 1;
			} else if (!this.getCasterPlayer().getCapability(WingsProvider.WINGS).isPresent()) {
				this.status = 2;
			} else if (AbstractInterruptablePlayerTrick.playerBusyFor(this.getCasterPlayer()) != 0) {
				this.status = 3;
			}
		}
		super.executeServer();
	}

	@Override
	public void onTrickEndServer() throws NoCasterException {
		super.onTrickEndServer();
		// We are on server
		if (this.castEndedNaturally()) {
			((ServerWorld) this.casterWorld).summonEntity(new FireballEntity(this.getCasterPlayer()));
		}
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public void onTrickEndClient() throws NoCasterException {
		super.onTrickEndClient();
		// We are on client
		if (this.amICaster() && (this.status == 0)) {
			@SuppressWarnings("resource")
			ClientPlayerEntity player = Minecraft.getInstance().player;
			player.resetCooldown();
			player.swingArm(Hand.MAIN_HAND);
			this.status = 4;
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

	private static final ImmutableList<ITextComponent> MESSAGES = ImmutableList.of(
			new TranslationTextComponent("wingx.trick.fireball.start").setStyle(SUCCESS_STYLE),
			new TranslationTextComponent("wingx.trick.fireball.error",
					new TranslationTextComponent("wingx.trick.error_reason.no_caster")).setStyle(ERROR_STYLE),
			new TranslationTextComponent("wingx.trick.fireball.error",
					new TranslationTextComponent("wingx.trick.error_reason.no_wings")).setStyle(ERROR_STYLE),
			new TranslationTextComponent("wingx.trick.fireball.error",
					new TranslationTextComponent("wingx.trick.error_reason.caster_busy")).setStyle(ERROR_STYLE),
			new TranslationTextComponent("wingx.trick.fireball.success").setStyle(SUCCESS_STYLE));

	@Override
	protected ImmutableList<ITextComponent> getMessages() {
		return MESSAGES;
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
