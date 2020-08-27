package dzuchun.wingx.command.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import dzuchun.wingx.capability.entity.wings.storage.WingsDataManager;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.util.text.Style;

public class WingxComand {

	@SuppressWarnings("unused")
	private static final Logger LOG = LogManager.getLogger();

	public static final String TARGET_PAR_NAME = "command_target";

	public static final Style SUCCEESS_STYLE = Style.EMPTY
			.setColor(net.minecraft.util.text.Color.func_240743_a_(0xFF00BB22));
	public static final Style ERROR_STYLE = Style.EMPTY
			.setColor(net.minecraft.util.text.Color.func_240743_a_(0xFFFF0000));

	@SuppressWarnings("unchecked")
	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		LiteralArgumentBuilder<CommandSource> res = Commands.literal("wingx");
		res = res.requires(source -> source.hasPermissionLevel(2));
		res.then(addCommand(Commands.argument(TARGET_PAR_NAME, EntityArgument.entities())));
		res.then(addCommand(Commands.literal("self")));
		dispatcher.register(res);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static ArgumentBuilder addCommand(ArgumentBuilder literal) {
		return literal.then(WingsDataManager.getResetCommand()).then(WingsDataManager.getModifyCommand());
	}
}
