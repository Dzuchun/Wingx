package dzuchun.wingx.command.impl;

import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;

import dzuchun.wingx.capability.entity.wings.storage.WingsDataManager;
import dzuchun.wingx.client.abillity.AbillityNodes;
import dzuchun.wingx.client.render.overlay.AbstractOverlay;
import dzuchun.wingx.config.ServerConfig;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.util.text.Color;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;

public class WingxComand {

	@SuppressWarnings("unused")
	private static final Logger LOG = LogManager.getLogger();

	public static final String TARGET_PAR_NAME = "command_target";

	public static final Style SUCCEESS_STYLE = Style.EMPTY.setColor(Color.fromInt(0xFF00BB22));
	public static final Style ERROR_STYLE = Style.EMPTY.setColor(Color.fromInt(0xFFFF0000));

	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		LiteralArgumentBuilder<CommandSource> res = Commands.literal("wingx");
//		res = res.requires(source -> source.hasPermissionLevel(2));
//		res.then(addCapabilityDataCommand(Commands.argument(TARGET_PAR_NAME, EntityArgument.entities())));
//		res.then(addCapabilityDataCommand(Commands.literal("self")));
		res.then(capabilityDataCommand());
		res.then(debug());
		res.then(config());
		dispatcher.register(res);
	}

	@SuppressWarnings("unchecked")
	private static ArgumentBuilder<CommandSource, ?> capabilityDataCommand() {
		return Commands.literal("capability").requires(source -> source.hasPermissionLevel(2))
				.then(addCapabilityDataCommand(Commands.argument(TARGET_PAR_NAME, EntityArgument.entities())))
				.then(addCapabilityDataCommand(Commands.literal("self")));
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static ArgumentBuilder addCapabilityDataCommand(ArgumentBuilder literal) {
		return literal.then(WingsDataManager.getResetCommand()).then(WingsDataManager.getModifyCommand());
	}

	private static ArgumentBuilder<CommandSource, ?> debug() {
		return Commands.literal("debug")
				.then(Commands.literal("active_overlays").executes((CommandContext<CommandSource> source) -> {
					StringBuilder res = new StringBuilder();
					ArrayList<AbstractOverlay> activeOverlays = AbstractOverlay.getActiveOverlays();
					res.append("Getting active overlays:\n");
					for (AbstractOverlay o : activeOverlays) {
						res.append(o.toString().concat("\n"));
					}
					res.append(String.format("Total active overlays : %d\n", activeOverlays.size()));
					source.getSource().sendFeedback(new StringTextComponent(res.toString()), true);
					return 0;
				}).then(Commands.literal("clear").executes((CommandContext<CommandSource> source) -> {
					AbstractOverlay.getActiveOverlays().clear();
					source.getSource().sendFeedback(new StringTextComponent("Cleared"), true);
					return 0;
				}))).then(Commands.literal("loadnodes").executes((CommandContext<CommandSource> source) -> {
					// TODO doesn't work
					if (source.getSource().getEntity() instanceof ClientPlayerEntity) {
						AbillityNodes.loadAbillityNodes();
						source.getSource().sendFeedback(new StringTextComponent("Loaded abillity nodes"), true);
					} else {
						source.getSource().sendFeedback(
								new StringTextComponent("Command must be performed by a client player"), true);
					}
					return 0;
				}));
	}

	private static ArgumentBuilder<CommandSource, ?> config() {
		return Commands.literal("config").then(Commands.literal("nodes").then(Commands.literal("reset")
				.requires(source -> source.hasPermissionLevel(2)).executes((CommandContext<CommandSource> source) -> {
					ServerConfig.get().ABILLITY_NODES.set(AbillityNodes.DEFAULT_NODES); // TODO repair - funtions only
																						// after game restart
					source.getSource()
							.sendFeedback(new StringTextComponent("Abillity nodes config was reset to Default"), true);
					return 0;
				})));
	}
}
