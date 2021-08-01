package dzuchun.wingx.trick;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dzuchun.wingx.capability.world.tricks.ActiveTricksProvider;
import dzuchun.wingx.capability.world.tricks.IActiveTricksCapability;
import dzuchun.wingx.client.render.gui.SeparateRenderers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.common.util.LazyOptional;

public abstract class AbstractInterruptablePlayerTrick extends AbstractPlayerCastedTrick
		implements IInterruptableTrick {
	private static final Logger LOG = LogManager.getLogger();
	@OnlyIn(value = Dist.CLIENT)
	private static final Object CLIENT_INSTANCES_LOCK = new Object();
	@OnlyIn(value = Dist.CLIENT)
	private static Collection<AbstractInterruptablePlayerTrick> clientInstances = new ArrayList<AbstractInterruptablePlayerTrick>(
			0);

	private static ArrayList<AbstractInterruptablePlayerTrick> res_tricks = new ArrayList<AbstractInterruptablePlayerTrick>(
			0);

	@SuppressWarnings("resource")
	@OnlyIn(value = Dist.CLIENT)
	@Nullable
	public static synchronized ArrayList<AbstractInterruptablePlayerTrick> getForMe() {
		res_tricks.clear();
		synchronized (CLIENT_INSTANCES_LOCK) {
			clientInstances.forEach(trick -> {
				if (trick.getCasterPlayer().equals(Minecraft.getInstance().player)) {
					res_tricks.add(trick);
				}
			});
		}
		return res_tricks;
	}

	@OnlyIn(value = Dist.CLIENT)
	public static void onRenderGameOverlay(RenderGameOverlayEvent.Post event) {
		if (event.getType() == ElementType.CROSSHAIRS) {
			synchronized (CLIENT_INSTANCES_LOCK) {
				clientInstances.forEach(trick -> {
					trick.getDrawFunction().accept(event);
				});
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
		if (interruptCondition != null) {
			this.interruptCondition = interruptCondition;
			interruptCondition.reset();
		}
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
		this.beginTime = buf.readLong();
		this.endTime = buf.readLong();
		return super.readFromBuf(buf);
	}

	@Override
	public ITrick writeToBuf(PacketBuffer buf) {
		buf.writeInt(this.duration);
		buf.writeInt(this.interruptCondition.toInt());
		buf.writeLong(this.beginTime);
		buf.writeLong(this.endTime);
		return super.writeToBuf(buf);
	}

	@Override
	public void tick() {
		PlayerEntity caster = this.getCasterPlayer();
		if (this.hasCasterPlayer()) {
			if (this.interruptCondition.condition().test(caster)) {
				this.interrupt();
				LOG.debug("Interrupting cast: condition failed");
				if (!this.hasCasterPlayer()) {
					LOG.warn("Caster disappeared!");
				}
				return;
			}
		} else {
			this.interrupt();
			LOG.warn("No player caster for trick.");
		}
	}

	protected long beginTime = 0;
	protected long endTime = 0;

	@Override
	public void executeCommon() {
		if (!this.hasCasterPlayer()) {
			throw new NoCasterException(this);
		}
		@SuppressWarnings("unused")
		PlayerEntity caster = this.getCasterPlayer();
		if (this.status == 0) {
			this.beginCast();
			LOG.warn("Begining cast of {}", this);
		} else {
			LOG.warn("Trick's not succesfull, not executing");
		}
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public void executeClient() {
		super.executeClient();
		if (this.status != 0) {
			return;
		}
		synchronized (CLIENT_INSTANCES_LOCK) {
			clientInstances.add(this);
		}
	}

	@Override
	public void executeServer() {
		super.executeServer();
		if (this.status != 0) {
			return;
		}
		this.casterWorld.getCapability(ActiveTricksProvider.ACTIVE_TRICKS, null).ifPresent(cap -> {
			cap.addActiveTrick(this);
		});
	}

	@Deprecated
	protected boolean interrupted = false; // TODO replace with status

	// TODO doc
	@Override
	public void interrupt() {
		this.interrupted = true;
	}

	private static List<AbstractInterruptablePlayerTrick> toRemove_1 = new ArrayList<AbstractInterruptablePlayerTrick>(
			0);

	// TODO change to ITrick method
	// TODO add tricks a unique ID
	public static synchronized void removeSimilar(AbstractInterruptablePlayerTrick trick) {
		toRemove_1.clear();
		synchronized (CLIENT_INSTANCES_LOCK) {
			clientInstances.forEach(instance -> {
				if (instance.casterUniqueId.equals(trick.casterUniqueId)
						&& instance.getClass().equals(trick.getClass())) {
					toRemove_1.add(instance);
				} else {
					LOG.debug("Trick {} and {} are not similar", trick, instance);
				}
			});
			clientInstances.removeAll(toRemove_1);
		}
	}

	@Override
	public void onTrickEndCommon() throws NoCasterException {
		assertHasCaster(this);
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public void onTrickEndClient() throws NoCasterException {
		IInterruptableTrick.super.onTrickEndClient();
		removeSimilar(this);
	}

	@Override
	public void onTrickEndServer() throws NoCasterException {
		IInterruptableTrick.super.onTrickEndServer();
		LOG.debug("Ending cast on server");
	}

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
		},
		MOVED_CONDITION {
			private Vector3d prevPos;
			private Vector2f prevRotation;

			@Override
			public int toInt() {
				return 1;
			}

			@Override
			public Predicate<PlayerEntity> condition() {
				return (PlayerEntity player) -> {
					if ((this.prevPos == null) || (this.prevRotation == null)) {
						this.prevRotation = player.getPitchYaw();
						this.prevPos = player.getPositionVec();
						return false;
					}
					return (!this.prevPos.equals(player.getPositionVec())
							|| !this.prevRotation.equals(player.getPitchYaw()));
				};
			}

			@Override
			public void reset() {
				super.reset();
				this.prevPos = null;
				this.prevRotation = null;
			}
		},
		CHANGED_ITEM_CONDITION {

			@Override
			public int toInt() {
				return 2;
			}

			Item item;

			@Override
			public Predicate<PlayerEntity> condition() {
				return (PlayerEntity player) -> {
					if (this.item == null) {
						this.item = player.getHeldItemMainhand().getItem();
						return false;
					}
					return !this.item.equals(player.getHeldItemMainhand().getItem());
				};
			}

			@Override
			public void reset() {
				this.item = null;
				super.reset();
			}

		};

		public Predicate<PlayerEntity> condition() {
			return (PlayerEntity entity) -> false;
		}

		public abstract int toInt();

		public void reset() {

		}

		@Nullable
		public static InterruptCondition getFromInt(int i) {
			switch (i) {
			case 0:
				return NO_CONDITION;
			case 1:
				return MOVED_CONDITION;
			case 2:
				return CHANGED_ITEM_CONDITION;
			default:
				return null;
			}
		}
	}

	private static int res_int_1;

	public static synchronized int playerBusyFor(PlayerEntity casterPlayer) {
		World world = casterPlayer.world;
		if (world instanceof ClientWorld) {
			return 0;
		}
		LazyOptional<IActiveTricksCapability> capOptional = world.getCapability(ActiveTricksProvider.ACTIVE_TRICKS);
		if (capOptional.isPresent()) {
			res_int_1 = 0;
			// TODO rewrite using stream!!
			capOptional.ifPresent(cap -> {
				cap.getActiveTricks().forEach(trick -> {
					if ((trick.hasCaster() && trick.getCaster().getUniqueID().equals(casterPlayer.getUniqueID()))
							&& (res_int_1 < trick.timeLeft())) {
						res_int_1 = trick.timeLeft();
					}
				});
			});
			return res_int_1;
		} else {
			LOG.warn("World {} doesn't have a active tricks capability", world);
			return 0;
		}
	}

	@Override
	public boolean castEndedNaturally() {
		assertHasCasterInfo(this);
//		LOG.debug("Now {}, endTime {}, returning {}", this.casterWorld.getGameTime(), this.endTime,
//				this.casterWorld.getGameTime() >= this.endTime);
		return this.casterWorld.getGameTime() >= this.endTime;
	}

	@Override
	public void beginCast() throws NoCasterException {
		assertHasCasterInfo(this);
		this.beginTime = this.casterWorld.getGameTime();
		this.endTime = this.casterWorld.getGameTime() + this.duration;
		LOG.debug("Beginning cast of {} trick with hashcode {}. Duration: {}, end time: {}, now: {}",
				this.getClass().getName(), this.hashCode(), this.duration, this.endTime,
				this.casterWorld.getGameTime());
	}

	@Override
	public boolean keepExecuting() {
		if (this.interrupted) {
			return false;
		}
		assertHasCasterInfo(this);
		return this.endTime >= this.casterWorld.getGameTime();
	}

	@Override
	public int timeLeft() {
		assertHasCasterInfo(this);
		return (int) (this.endTime - this.casterWorld.getGameTime());
	}

	@Override
	public String toString() {
		return String.format("AbstractInterruptablePlayerTrick[type = %s, end time = %s, caster uuid = %s]",
				this.getClass().getName(), this.endTime, this.casterUniqueId);
	}
}
