package dzuchun.wingx.trick;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dzuchun.wingx.ModBusEventListener;
import dzuchun.wingx.Wingx;
import dzuchun.wingx.capability.entity.wings.storage.HastyData;
import dzuchun.wingx.capability.entity.wings.storage.Serializers;
import dzuchun.wingx.client.render.overlay.HastyPostAnimationOverlay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.multiplayer.PlayerController;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.PacketDistributor.PacketTarget;

public class HastyPlayerTrick extends AbstractPlayerCastedTrick {
	private static final ResourceLocation REGISTRY_NAME = new ResourceLocation(Wingx.MOD_ID, "hasty_player_trick");
	private static final Logger LOG = LogManager.getLogger();

	public HastyPlayerTrick() {
		super();
	}

	private HastyData data;
	private BlockPos blocksPos;

	public HastyPlayerTrick(PlayerEntity player, HastyData data, BlockPos blockPos) {
		super(player);
		this.data = data;
		this.blocksPos = blockPos;
	}

	@Override
	public void execute(LogicalSide side) {
		if (side == LogicalSide.CLIENT) {
			// TODO fix block not-dropping
			Entity caster = getCaster();
			if (caster instanceof ClientPlayerEntity) {
				Minecraft minecraft = Minecraft.getInstance();
				PlayerController controller = minecraft.playerController;
				if (controller.isHittingBlock) {
					if (amICaster()) {
						if ((1.0f - controller.curBlockDamageMP) > this.data.jump) {
							LOG.info("Executing hasty trick on client. Is hitting block={}, current damage={}",
									controller.isHittingBlock, controller.curBlockDamageMP);
							controller.curBlockDamageMP += this.data.jump;
						}
					}
					// Performing animation
					new HastyPostAnimationOverlay(this.blocksPos, this.data).activate();
					// Playing sound
					minecraft.world.playSound((PlayerEntity) caster, this.blocksPos,
							ModBusEventListener.HASTY_PROC_SOUND, SoundCategory.PLAYERS, 1.0f, 1.0f);
				}
			}
		}
	}

	@Override
	public PacketTarget getBackPacketTarget() {
		return null;
	}

	@Override
	public ITrick newEmpty() {
		return new HastyPlayerTrick();
	}

	@Override
	protected void setRegistryName() {
		this.registryName = REGISTRY_NAME;
	}

	@Override
	public ITrick readFromBuf(PacketBuffer buf) {
		this.data = Serializers.HASTY_SERIALIZER.read(buf);
		this.blocksPos = buf.readBlockPos();
		return super.readFromBuf(buf);
	}

	@Override
	public ITrick writeToBuf(PacketBuffer buf) {
		Serializers.HASTY_SERIALIZER.write(buf, this.data);
		buf.writeBlockPos(this.blocksPos);
		return super.writeToBuf(buf);
	}

}
