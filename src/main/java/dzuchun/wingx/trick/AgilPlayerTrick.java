package dzuchun.wingx.trick;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.ImmutableList;

import dzuchun.wingx.Wingx;
import dzuchun.wingx.capability.entity.wings.storage.AgilData;
import dzuchun.wingx.capability.entity.wings.storage.Serializers;
import dzuchun.wingx.client.render.overlay.FadingScreenOverlay;
import dzuchun.wingx.client.render.overlay.FadingScreenOverlay.FadeFunction;
import dzuchun.wingx.client.render.overlay.RadiantFadingScreenOverlay;
import dzuchun.wingx.init.SoundEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.vector.Vector4f;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.PacketDistributor.PacketTarget;

public class AgilPlayerTrick extends AbstractTargetedPlayerTrick {
	private static final ResourceLocation REGISTRY_NAME = new ResourceLocation(Wingx.MOD_ID, "agil_player_trick");
	private static final Logger LOG = LogManager.getLogger();

	public AgilPlayerTrick() {
		super();
	}

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
		if (this.amICaster()) {
			// Adding animation
			new RadiantFadingScreenOverlay(new Vector4f(0.0f, 1.0f, 0.0f, 1.0f), new Vector4f(0.0f, 0.0f, 0.0f, 0.0f),
					100, FadingScreenOverlay.DO_NOTHING, FadeFunction.LINEAR).activate();
		}
		LOG.warn("Agil proced!!");
	}

	@Override
	public PacketTarget getBackPacketTarget() {
		return null;
	}

	@Override
	public ITrick newEmpty() {
		return new AgilPlayerTrick();
	}

	@Override
	protected void setRegistryName() {
		this.registryName = REGISTRY_NAME;
	}

	@Override
	public ITrick readFromBuf(PacketBuffer buf) {
		this.data = Serializers.AGIL_SERIALIZER.read(buf);
		return super.readFromBuf(buf);
	}

	@Override
	public ITrick writeToBuf(PacketBuffer buf) {
		Serializers.AGIL_SERIALIZER.write(buf, this.data);
		return super.writeToBuf(buf);
	}

	private static final ImmutableList<ITextComponent> MESSAGES = ImmutableList
			.of(new TranslationTextComponent("wingx.trick.agil.proc").setStyle(PROC_STYLE));

	@Override
	protected ImmutableList<ITextComponent> getMessages() {
		return MESSAGES;
	}
}