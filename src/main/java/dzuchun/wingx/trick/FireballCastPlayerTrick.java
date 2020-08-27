package dzuchun.wingx.trick;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.ImmutableList;

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
import net.minecraft.util.text.ITextComponent;
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
			// We are on server
			if (hasCasterPlayer() && getCasterPlayer().getCapability(WingsProvider.WINGS, null).isPresent()
					&& AbstractInterruptablePlayerTrick.playerBusyFor(getCasterPlayer()) == 0) {
				IWingsCapability cap = getCasterPlayer().getCapability(WingsProvider.WINGS, null).orElse(null);
				// Cap is nonnul
				FireballData data = cap.getDataManager().getOrAddDefault(Serializers.FIREBALL_SERIALIZER);
				this.interruptCondition = data.interruptCondition;
				this.duration = data.castDuration;
				this.interruptCondition.reset();
				this.status = 0;
			} else {
				LOG.warn("Caster does not exist, has no capability or caster is busy");
				if (!hasCasterPlayer()) {
					this.status = 1;
				} else if (!getCasterPlayer().getCapability(WingsProvider.WINGS).isPresent()) {
					this.status = 2;
				} else if (AbstractInterruptablePlayerTrick.playerBusyFor(getCasterPlayer()) != 0) {
					this.status = 3;
				}
			}
		}
		super.execute(side);
	}

	@SuppressWarnings("resource")
	@Override
	public void onCastEnd(LogicalSide side) {
		super.onCastEnd(side);
		if (side == LogicalSide.SERVER) {
			// We are on server
			if (castEndedNaturally()) {
				((ServerWorld) this.casterWorld).summonEntity(new FireballEntity(getCasterPlayer()));
			}
		} else {
			// We are on client
			if (amICaster() && this.status == 0) {
				ClientPlayerEntity player = Minecraft.getInstance().player;
				player.resetCooldown();
				player.swingArm(Hand.MAIN_HAND);
				this.status = 4;
				// TODO play sound
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

}
