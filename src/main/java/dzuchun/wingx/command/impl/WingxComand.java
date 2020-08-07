package dzuchun.wingx.command.impl;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ibm.icu.impl.Pair;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;

import dzuchun.wingx.capability.entity.wings.IWingsCapability;
import dzuchun.wingx.capability.entity.wings.WingsProvider;
import dzuchun.wingx.capability.entity.wings.storage.BasicData;
import dzuchun.wingx.capability.entity.wings.storage.Serializers;
import dzuchun.wingx.capability.entity.wings.storage.WingsDataManager;
import dzuchun.wingx.entity.misc.WingsEntity;
import dzuchun.wingx.net.ToggleWingsMessage;
import dzuchun.wingx.net.WingxPacketHandler;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.network.PacketDistributor;

public class WingxComand {

	private static final Logger LOG = LogManager.getLogger();

	private static final DynamicCommandExceptionType ERROR_UNKNOWN = new DynamicCommandExceptionType(object -> {
		return new TranslationTextComponent("wingx.commands.error.unknown");
	});

	private static final DynamicCommandExceptionType ERROR_NO_WINGS = new DynamicCommandExceptionType(object -> {
		return new TranslationTextComponent("wingx.commands.error.no_wings", object);
	});

	private static final DynamicCommandExceptionType ERROR_NOT_ENOUGH_ARGUMENTS = new DynamicCommandExceptionType(
			object -> {
				return new TranslationTextComponent("wingx.commands.error.not_enough_arguments");
			});

	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(Commands.literal("wingx").requires(source -> source.hasPermissionLevel(2))
				.then(Commands.literal("reset").executes(source -> {
					return execute(source.getSource(), Arrays.asList(source.getSource().asPlayer()), Action.RESET,
							true);
				})).then(Commands.literal("modify").then(Commands.literal("active")
						.then(Commands.argument("isActive", BoolArgumentType.bool()).executes(source -> {
							return execute(source.getSource(), Arrays.asList(source.getSource().asPlayer()),
									Action.SET_ACTIVE, true, BoolArgumentType.getBool(source, "isActive"));
						}))).then(Commands.literal("needs_end")
								.then(Commands.argument("needs", BoolArgumentType.bool()).executes(source -> {
									return execute(source.getSource(), Arrays.asList(source.getSource().asPlayer()),
											Action.SET_NEEDS_END, true, BoolArgumentType.getBool(source, "needs"));
								})))));
	}

	private static int execute(CommandSource source, List<PlayerEntity> targets, Action action, boolean throwError,
			Object... args) throws CommandSyntaxException {
		return execute(source, targets, action, throwError, player -> true, args);
	}

	private static Pair<Integer, String> res_1;

	private static synchronized int execute(CommandSource source, List<PlayerEntity> targets, Action action,
			boolean throwError, Predicate<PlayerEntity> predicate, Object... args) throws CommandSyntaxException {
		targets.forEach(target -> {
			if (predicate.test(target)) {
				Pair<Integer, String> res = action.execute(source, target, args);
				if (res.first != 1 && throwError) {
					res_1 = res;
					// TODO add debug
				} else {
					source.sendFeedback(action.getFeedBack(target), true);
				}
			}
		});
		switch (res_1.first) {
		case 0:
			break;
		case 2:
			throw ERROR_NO_WINGS.create(res_1.second);
		case 3:
			throw ERROR_NOT_ENOUGH_ARGUMENTS.create(null);
		default:
		case 1:
			throw ERROR_UNKNOWN.create(null);
		}
		return res_1.first;
	}

	private enum Action {
		RESET {

			@Override
			public Pair<Integer, String> execute(CommandSource source, PlayerEntity target, Object... args) {
				LazyOptional<IWingsCapability> lazyCap = target.getCapability(WingsProvider.WINGS, null);
				if (!lazyCap.isPresent()) {
					return Pair.of(2, target.getGameProfile().getName());
				}
				lazyCap.ifPresent(cap -> {
					WingsDataManager manager = cap.getDataManager();
					manager.replace(manager.getOrAddDefault(Serializers.BASIC_SERIALIZER),
							Serializers.BASIC_SERIALIZER.getDefault());
				});
				return Pair.of(0, "");
			}

			@Override
			public ITextComponent getFeedBack(PlayerEntity target) {
				return new TranslationTextComponent("wingx.commands.reset", target.getGameProfile().getName());
			}
		},
		SET_ACTIVE {
			private Entity foundEntity;

			@Override
			public synchronized Pair<Integer, String> execute(CommandSource source, PlayerEntity target,
					Object... args) {
				LazyOptional<IWingsCapability> lazyCap = target.getCapability(WingsProvider.WINGS, null);
				if (!lazyCap.isPresent()) {
					return Pair.of(1, target.getGameProfile().getName());
				}
				if (args.length < 1) {
					return Pair.of(3, "");
				}
				lazyCap.ifPresent(cap -> {
					ServerWorld world = (ServerWorld) target.world;
					boolean flag = (boolean) args[0];
//					cap.setActive((boolean) args[0]);
					BasicData data = cap.getDataManager().getOrAddDefault(Serializers.BASIC_SERIALIZER);
					if (data.wingsActive && !flag) {
						UUID targetUniqueId = data.wingsUniqueId;
						if (targetUniqueId == null) {
							LOG.warn("Wings active for {}, but no UUID specified, deactivating",
									target.getGameProfile().getName());
							data.wingsActive = false;
							return;
						}
						this.foundEntity = null;
						world.getEntities().forEach((Entity entity) -> {
							if (entity.getUniqueID().equals(targetUniqueId)) {
								this.foundEntity = entity;
							}
						});
						if (this.foundEntity == null) {
							LOG.warn("{}'s wings UUID specified, but entity not present",
									target.getGameProfile().getName());
						} else {
							if (this.foundEntity instanceof WingsEntity) {
								WingsEntity wings = (WingsEntity) this.foundEntity;
								world.removeEntity(wings);
								WingxPacketHandler.INSTANCE.send(
										PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) target),
										new ToggleWingsMessage(false));
								data.wingsActive = false;
							} else {
								LOG.warn("Entity with UUID {} should be wings, but it is {}. Deactivating wings.",
										targetUniqueId, this.foundEntity.getClass().getName());
								data.wingsActive = false;
							}
						}
					} else if (!data.wingsActive && flag) {
						WingsEntity wings = new WingsEntity(world);
						wings.setOwner(target.getUniqueID(), true);
						wings.setPosition(target.getPosX(), target.getPosY(), target.getPosZ());
						world.summonEntity(wings);
						data.wingsUniqueId = wings.getUniqueID();
						if (!data.wingsActive) {
							data.wingsActive = true;
						}
						WingxPacketHandler.INSTANCE.send(
								PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) target),
								new ToggleWingsMessage(true));
					}
				});
				return Pair.of(0, "");
			}

			@Override
			public ITextComponent getFeedBack(PlayerEntity target) {
				return new TranslationTextComponent("wingx.commands.setActive", target.getGameProfile().getName());
			}
		},
		SET_NEEDS_END {

			@Override
			public synchronized Pair<Integer, String> execute(CommandSource source, PlayerEntity target,
					Object... args) {
				LazyOptional<IWingsCapability> lazyCap = target.getCapability(WingsProvider.WINGS, null);
				if (!lazyCap.isPresent()) {
					return Pair.of(1, target.getGameProfile().getName());
				}
				if (args.length < 1) {
					return Pair.of(3, "");
				}
				lazyCap.ifPresent(cap -> {
					boolean flag = (boolean) args[0];
					BasicData data = cap.getDataManager().getOrAddDefault(Serializers.BASIC_SERIALIZER);
					if (data.needsEnd != flag) {
						data.needsEnd = flag;
					}
				});
				return Pair.of(0, "");
			}

			@Override
			public ITextComponent getFeedBack(PlayerEntity target) {
				return new TranslationTextComponent("wingx.commands.setActive", target.getGameProfile().getName());
			}
		};

		public abstract Pair<Integer, String> execute(CommandSource source, PlayerEntity target, Object... args);

		public abstract ITextComponent getFeedBack(PlayerEntity target);
	}
}
