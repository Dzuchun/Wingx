package dzuchun.wingx.trick;

import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dzuchun.wingx.Wingx;
import dzuchun.wingx.capability.entity.wings.IWingsCapability;
import dzuchun.wingx.capability.entity.wings.WingsProvider;
import dzuchun.wingx.capability.entity.wings.storage.Serializers;
import dzuchun.wingx.capability.entity.wings.storage.SoulswordData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.PacketDistributor.PacketTarget;

public class SummonSwordPlayerTrick extends AbstractInterruptablePlayerTrick implements ITimeredTrick {
	private static final ResourceLocation REGISTRY_NAME = new ResourceLocation(Wingx.MOD_ID,
			"summon_sword_player_trick");
	@SuppressWarnings("unused")
	private static final Logger LOG = LogManager.getLogger();

	public SummonSwordPlayerTrick() {
		super();
	}

	SummonSwordPlayerTrick(PlayerEntity caster) {
		super(caster, 0, null);
		caster.getCapability(WingsProvider.WINGS, null).ifPresent((IWingsCapability wings) -> {
			SoulswordData data = wings.getDataManager().getOrAddDefault(Serializers.SOULSWORD_SERIALIZER);
			this.duration = data.summonDurationTicks;
		});
	}

	@Override
	public PacketTarget getEndPacketTarget() {
		return hasCasterPlayer() ? PacketDistributor.TRACKING_ENTITY_AND_SELF.with(this::getCasterPlayer) : null;
	}

	@Override
	public PacketTarget getBackPacketTarget() {
		return getEndPacketTarget();
	}

	@Override
	public ITrick newEmpty() {
		return new SummonSwordPlayerTrick();
	}

	@Override
	public int timeFull() throws NoCasterException {
		return this.duration;
	}

	@Override
	public double partLeft() throws NoCasterException {
		assertHasCaster(this);
		long time = casterWorld.getGameTime();
		return ((double) (endTime - time)) / (duration);
	}

	@Override
	protected void setRegistryName() {
		this.registryName = REGISTRY_NAME;
	}

	@Override
	protected Consumer<RenderGameOverlayEvent> getDrawFunction() {
		// TODO probablt should override draw function
		return super.getDrawFunction();
	}

	@Override
	public void execute(LogicalSide side) {
		super.execute(side);
		if (side == LogicalSide.SERVER) {
			if (hasCasterPlayer() && getCasterPlayer().getCapability(WingsProvider.WINGS, null).isPresent()
					&& AbstractInterruptablePlayerTrick.playerBusyFor(getCasterPlayer()) == 0) {
				IWingsCapability cap = getCasterPlayer().getCapability(WingsProvider.WINGS, null).orElse(null);
				// Cap is nonnul
				
			}
		} else {
			
		}
	}

}
