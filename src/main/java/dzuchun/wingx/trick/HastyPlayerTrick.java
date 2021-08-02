package dzuchun.wingx.trick;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dzuchun.wingx.capability.entity.wings.storage.HastyData;
import dzuchun.wingx.capability.entity.wings.storage.Serializers;
import dzuchun.wingx.client.render.overlay.HastyPostAnimationOverlay;
import dzuchun.wingx.init.SoundEvents;
import dzuchun.wingx.init.Tricks;
import dzuchun.wingx.trick.state.TrickStates;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.multiplayer.PlayerController;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.PacketDistributor.PacketTarget;

public class HastyPlayerTrick extends AbstractPlayerCastedTrick {
	private static final Logger LOG = LogManager.getLogger();

	private HastyData data;
	private BlockPos blocksPos;

	public HastyPlayerTrick(PlayerEntity player, HastyData data, BlockPos blockPos) {
		super(player);
		this.data = data;
		this.blocksPos = blockPos;
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public void executeClient() {
		super.executeClient();
		if (this.state.isError()) {
			return;
		}
		// We are on client
		// TODO fix block not-dropping
		Entity caster = this.getCaster();
		if (caster instanceof ClientPlayerEntity) {
			Minecraft minecraft = Minecraft.getInstance();
			PlayerController controller = minecraft.playerController;
			if (controller.isHittingBlock) {
				if (this.iAmCaster()) {
					if ((1.0f - controller.curBlockDamageMP) > this.data.jump) {
						LOG.info("Executing hasty trick on client. Is hitting block={}, current damage={}",
								controller.isHittingBlock, controller.curBlockDamageMP);
						controller.curBlockDamageMP += Math.min(this.data.jump, 1.0f - controller.curBlockDamageMP);
					}
				}
				// Performing animation
				new HastyPostAnimationOverlay(this.blocksPos, this.data).activate();
				// Playing sound
				minecraft.world.playSound(minecraft.player, this.blocksPos, SoundEvents.HASTY_PROC.get(),
						SoundCategory.PLAYERS, 1.0f, 1.0f);
				// Setting state
				this.state = TrickStates.PROCED;
			}
		}
	}

	@Override
	public PacketTarget getBackPacketTarget() {
		return null;
	}

	public static class TrickType extends AbstractPlayerCastedTrick.TrickType<HastyPlayerTrick> {

		@Override
		protected HastyPlayerTrick readFromBufInternal(HastyPlayerTrick trick, PacketBuffer buf) {
			trick.data = Serializers.HASTY_SERIALIZER.read(buf);
			trick.blocksPos = buf.readBlockPos();
			return super.readFromBufInternal(trick, buf);
		}

		@Override
		public HastyPlayerTrick writeToBuf(HastyPlayerTrick trick, PacketBuffer buf) {
			Serializers.HASTY_SERIALIZER.write(buf, trick.data);
			buf.writeBlockPos(trick.blocksPos);
			return super.writeToBuf(trick, buf);
		}

		@Override
		public HastyPlayerTrick newEmpty() {
			return new HastyPlayerTrick(null, null, null);
		}

	}

	@SuppressWarnings("unchecked")
	@Override
	public HastyPlayerTrick.TrickType getType() {
		return Tricks.HASTY_TRICK.get();
	}

}
