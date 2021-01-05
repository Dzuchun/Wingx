package dzuchun.wingx.command.impl;

import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;

import dzuchun.wingx.capability.entity.wings.storage.WingsDataManager;
import dzuchun.wingx.client.render.overlay.AbstractOverlay;
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

	public static final Style SUCCEESS_STYLE = Style.EMPTY.setColor(Color.func_240743_a_(0xFF00BB22));
	public static final Style ERROR_STYLE = Style.EMPTY.setColor(Color.func_240743_a_(0xFFFF0000));

	@SuppressWarnings("unchecked")
	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		LiteralArgumentBuilder<CommandSource> res = Commands.literal("wingx");
		res = res.requires(source -> source.hasPermissionLevel(2));
		res.then(addCommand(Commands.argument(TARGET_PAR_NAME, EntityArgument.entities())));
		res.then(addCommand(Commands.literal("self")));
		res.then(debug());
		dispatcher.register(res);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static ArgumentBuilder addCommand(ArgumentBuilder literal) {
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
				})));
	}
}
