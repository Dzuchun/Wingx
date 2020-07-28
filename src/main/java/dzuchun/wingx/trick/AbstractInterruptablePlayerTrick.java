package dzuchun.wingx.trick;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.Maps;

import dzuchun.wingx.client.render.gui.SeparateRenderers;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;

public abstract class AbstractInterruptablePlayerTrick extends PlayerTrick implements IInterruptableTrick {
	private static final Logger LOG = LogManager.getLogger();
	private static final Object INSTANCES_LOCK = new Object();
	private static Map<PlayerEntity, AbstractInterruptablePlayerTrick> instances = Maps.newConcurrentMap();
	@OnlyIn(value = Dist.CLIENT)
	private static final Object CLIENT_INSTANCES_LOCK = new Object();
	@OnlyIn(value = Dist.CLIENT)
	private static Map<PlayerEntity, AbstractInterruptablePlayerTrick> clientInstances = Maps.newConcurrentMap();
	// TODO change that all to every n-ticks synchronization with optional messages,
	// without client instances.

	public static void onWorldTick(World worldIn) {
		synchronized (INSTANCES_LOCK) {
			Map<PlayerEntity, AbstractInterruptablePlayerTrick> toRemove = Maps.newConcurrentMap();
			instances.forEach((PlayerEntity player, AbstractInterruptablePlayerTrick trick) -> {
				trick.tick(player);
				if (trick.removed) {
					toRemove.put(player, trick);
				}
			});
			// Removing after going through, because of ConcurrentModificationException
			toRemove.forEach((PlayerEntity player, AbstractInterruptablePlayerTrick trick) -> {
				if (instances.remove(player, trick)) {
					LOG.debug("Removed AbstractInterruptablePlayerTrick");
				} else {
					LOG.warn("Failed to remove AbstractInterruptablePlayerTrick");
				}
			});
		}
	}

	public static void onClientTick() {
		@SuppressWarnings({ "unused", "resource" })
		World world = Minecraft.getInstance().world;
		synchronized (CLIENT_INSTANCES_LOCK) {
			Map<PlayerEntity, AbstractInterruptablePlayerTrick> toRemove = Maps.newConcurrentMap();
			clientInstances.forEach((PlayerEntity player, AbstractInterruptablePlayerTrick trick) -> {
				trick.tick(player);
				if (trick.removed) {
					toRemove.put(player, trick);
				}
			});
			// Removing after going through, because of ConcurrentModificationException
			toRemove.forEach((PlayerEntity player, AbstractInterruptablePlayerTrick trick) -> {
				if (clientInstances.remove(player, trick)) {
					LOG.debug("Removed AbstractInterruptablePlayerTrick");
				} else {
					LOG.warn("Failed to remove AbstractInterruptablePlayerTrick");
				}
			});
		}
	}

	private static int res_int;

	public static synchronized int playerBusy(@Nonnull PlayerEntity playerIn) {
		res_int = 0;
		instances.forEach((PlayerEntity player, AbstractInterruptablePlayerTrick trick) -> {
			if (player.equals(playerIn) && res_int < trick.timeLeft(playerIn.world)) {
				res_int = trick.timeLeft(playerIn.world);
			}
		});
		return res_int;
	}

	private static AbstractInterruptablePlayerTrick res_trick;

	@SuppressWarnings("resource")
	@OnlyIn(value = Dist.CLIENT)
	@Nullable
	public static synchronized AbstractInterruptablePlayerTrick getForMe() {
		res_trick = null;
		instances.forEach((PlayerEntity caster, AbstractInterruptablePlayerTrick trick) -> {
			if (caster.equals(Minecraft.getInstance().player)) {
				res_trick = trick;
			}
		});
		return res_trick;
	}

	@OnlyIn(value = Dist.CLIENT)
	public static void onRenderGameOverlay(RenderGameOverlayEvent.Post event) {
		if (event.getType() == ElementType.CROSSHAIRS) {
			AbstractInterruptablePlayerTrick trick = getForMe();
			if (trick != null) {
				trick.getDrawFunction().accept(event);
			}
		}
	}

	public AbstractInterruptablePlayerTrick() {
		super();
	}

	protected int duration;
	protected InterruptCondition interruptCondition;

	public AbstractInterruptablePlayerTrick(PlayerEntity caster, int duration,
			@Nullable InterruptCondition interruptCondition) {
		super(caster);
		this.duration = duration;
		this.interruptCondition = interruptCondition;
	}

	@Override
	public ITrick readFromBuf(PacketBuffer buf) {
		this.duration = buf.readInt();
		InterruptCondition tmp = InterruptCondition.getFromInt(buf.readInt());
		if (tmp != null) {
			this.interruptCondition = tmp;
		} else {
			this.interruptCondition = InterruptCondition.NO_CONDITION;
		}
		return super.readFromBuf(buf);
	}

	@Override
	public ITrick writeToBuf(PacketBuffer buf) {
		buf.writeInt(this.duration);
		buf.writeInt(this.interruptCondition.toInt());
		return super.writeToBuf(buf);
	}

	@Override
	public int timeLeft(@Nonnull World worldIn) {
		return (int) (this.endTime - worldIn.getGameTime());
	}

	@Override
	public int timeFull(World worldIn) {
		return this.duration;
	}

	@Override
	public double partLeft(World worldIn) {
		return (this.endTime - worldIn.getGameTime()) / (double) this.duration;
	}

	@Override
	public void tick(Entity caster) {
		if (this.removed == true) {
			LOG.warn("Ticking already removed trick, returning");
			return;
		}
		if (caster instanceof PlayerEntity) {
			if (caster.world.getGameTime() >= this.endTime) {
				onCastEnd(caster);
				LOG.debug("Ending cast: end time: {}, now: {}", this.endTime, caster.world.getGameTime());
				return;
			}

			if (!hasCaster(caster.world) || this.interruptCondition.condition().test((PlayerEntity) caster)) {
				interrupt(caster);
				LOG.debug("Interrupting cast: caster no longer exist or condition failed");
				return;
			}
		} else {
			LOG.warn("Somehow not player was parsed here.");
		}
	}

	private long beginTime;
	private long endTime;

	@Override
	public void beginCast(@Nonnull Entity caster) {
		if (!(caster instanceof PlayerEntity)) {
			LOG.warn("Not player somehow was parsed here.");
			return;
		}
		this.beginTime = caster.world.getGameTime();
		this.endTime = this.beginTime + this.duration;
		this.removed = false;
		LOG.debug("Beginning cast of {} trick with hashcode {}. Duration: {}, end time: {}, now: {}",
				this.getClass().getName(), hashCode(), this.duration, this.endTime, caster.world.getGameTime());
		if (!caster.world.isRemote) {
			synchronized (INSTANCES_LOCK) {
				instances.put((PlayerEntity) caster, this);
			}
		} else {
			synchronized (CLIENT_INSTANCES_LOCK) {
				clientInstances.put((PlayerEntity) caster, this);
			}
		}
	}

	// TODO doc
	@Override
	public void interrupt(Entity caster) {
		if (caster.world.isRemote) {
			if (amICaster()) {
				Minecraft minecraft = Minecraft.getInstance();
				minecraft.player.sendStatusMessage(new TranslationTextComponent("wingx.trick.interrubtable.interrupt")
						.func_230530_a_(Style.EMPTY.setFormatting(TextFormatting.GRAY)), true);
			}
		} else {
			// We are on server
		}
		this.removed = true;
	}

	// TODO doc
	@Override
	public void onCastEnd(Entity caster) {
		if (!hasCaster(caster.world)) {
			LOG.warn("Ending trick without caster.");
		}
		if (caster.world.isRemote) {
			if (amICaster()) {
				Minecraft minecraft = Minecraft.getInstance();
				minecraft.player.sendStatusMessage(new TranslationTextComponent("wingx.trick.interrubtable.complete")
						.func_230530_a_(Style.EMPTY.setFormatting(TextFormatting.BOLD)), true);
			}
		} else {
			LOG.debug("Ending cast on server");
		}
		this.removed = true;
	}
	
	protected boolean removed = false;

	/**
	 * Returns function that should draw overlay corresponding to casting. Can be
	 * overridden.
	 *
	 * @return A function that should draw overlay corresponding to casting.
	 */
	@OnlyIn(value = Dist.CLIENT)
	protected Consumer<RenderGameOverlayEvent> getDrawFunction() {
		return SeparateRenderers::defaultDrawCastingOverlay;
	}

	public enum InterruptCondition {
		NO_CONDITION {
			@Override
			public int toInt() {
				return 0;
			}
		};

		public Predicate<PlayerEntity> condition() {
			return (PlayerEntity entity) -> false;
		}

		public abstract int toInt();

		@Nullable
		public static InterruptCondition getFromInt(int i) {
			switch (i) {
			case 0:
				return NO_CONDITION;
			default:
				return null;
			}
		}
	}
}
