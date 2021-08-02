package dzuchun.wingx.trick;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dzuchun.wingx.capability.entity.wings.storage.AgilData;
import dzuchun.wingx.capability.entity.wings.storage.Serializers;
import dzuchun.wingx.client.render.overlay.FadingScreenOverlay;
import dzuchun.wingx.client.render.overlay.FadingScreenOverlay.FadeFunction;
import dzuchun.wingx.client.render.overlay.RadiantFadingScreenOverlay;
import dzuchun.wingx.init.SoundEvents;
import dzuchun.wingx.init.Tricks;
import dzuchun.wingx.trick.state.TrickStates;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.vector.Vector4f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.PacketDistributor.PacketTarget;

//TODO remake, force to use server side
public class AgilPlayerTrick extends AbstractTargetedPlayerTrick {
	private static final Logger LOG = LogManager.getLogger();

	private AgilData data;

	public AgilPlayerTrick(PlayerEntity player, Entity target, AgilData data) {
		super(target, player);
		this.data = data;
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public void executeClient() {
		super.executeClient();
		// We are on client
		Minecraft minecraft = Minecraft.getInstance();
		ClientPlayerEntity caster = (ClientPlayerEntity) this.getCasterPlayer();
		Entity target = this.getTarget();
		caster.ticksSinceLastSwing = 1000;
		if (target != null) {
			target.hurtResistantTime = 0;
		}
		// Playing sound
		minecraft.world.playSound(minecraft.player, caster.getPosX(), caster.getPosY(), caster.getPosZ(),
				SoundEvents.AGIL_PROC.get(), SoundCategory.PLAYERS, 1.0f, 1.0f);
		if (this.iAmCaster()) {
			// Adding animation
			new RadiantFadingScreenOverlay(new Vector4f(0.0f, 1.0f, 0.0f, 1.0f), new Vector4f(0.0f, 0.0f, 0.0f, 0.0f),
					100, FadingScreenOverlay.DO_NOTHING, FadeFunction.LINEAR).activate();
		}
		LOG.warn("Agil proced!!");
		this.state = TrickStates.PROCED;
	}

	@Override
	public PacketTarget getBackPacketTarget() {
		return null;
	}

	public static class TrickType extends AbstractTargetedPlayerTrick.TrickType<AgilPlayerTrick> {

		@Override
		protected AgilPlayerTrick readFromBufInternal(AgilPlayerTrick trick, PacketBuffer buf) {
			trick.data = Serializers.AGIL_SERIALIZER.read(buf);
			return super.readFromBufInternal(trick, buf);
		}

		@Override
		public AgilPlayerTrick writeToBuf(AgilPlayerTrick trick, PacketBuffer buf) {
			Serializers.AGIL_SERIALIZER.write(buf, trick.data);
			return super.writeToBuf(trick, buf);
		}

		@Override
		public AgilPlayerTrick newEmpty() {
			return new AgilPlayerTrick(null, null, null);
		}

	}

	@SuppressWarnings("unchecked")
	@Override
	public AgilPlayerTrick.TrickType getType() {
		return Tricks.AGIL_TRICK.get();
	}
}