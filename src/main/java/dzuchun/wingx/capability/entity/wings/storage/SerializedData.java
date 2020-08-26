package dzuchun.wingx.capability.entity.wings.storage;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javax.annotation.Nonnull;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import dzuchun.wingx.capability.entity.wings.WingsProvider;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.StringTextComponent;

public abstract class SerializedData {
	private static final Logger LOG = LogManager.getLogger();

	public abstract <T extends SerializedData> Serializer<T> getSerializer();

	protected abstract List<CommandLiteral<? extends SerializedData, ?>> getCommandLiterals();

	@SuppressWarnings("rawtypes")
	private ArgumentBuilder argumentBuilder = null;

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Nonnull
	private ArgumentBuilder getArgumentBuilderInner() {
		ArgumentBuilder res = Commands.literal(getSerializer().getName());
		for (CommandLiteral<?, ?> par : getCommandLiterals()) {
			res.then(Commands.literal(par.name)
					.then(Commands.argument(par.name, par.type).executes((CommandContext<CommandSource> source) -> {
//						LOG.debug("Executing something");
						par.set(source);
						return 0;
					})));
		}
		return res;
	}

	@Nonnull
	@SuppressWarnings("rawtypes")
	public ArgumentBuilder getArgumentBuilder() {
		if (argumentBuilder == null) {
			argumentBuilder = getArgumentBuilderInner();
		}
		return argumentBuilder;
	}

	protected class CommandLiteral<V extends SerializedData, U> {
		public CommandLiteral(String nameIn, ArgumentType<U> typeIn, BiConsumer<V, U> setterIn, Class<U> classIn) {
			name = nameIn;
			type = typeIn;
			setter = setterIn;
			classField = classIn;
		}

		private final String name;
		private final ArgumentType<U> type;
		private final BiConsumer<V, U> setter;
		private Class<U> classField;

		public void set(CommandContext<CommandSource> source) {
			try {
				LOG.debug("Executing set");
				U arg = source.getArgument(name, classField);
				LOG.debug("Setting {}-{} to {} for {}", getSerializer().getName(), name, arg,
						source.getSource().asPlayer().getGameProfile().getName());
				setter.accept(source.getSource().asPlayer().getCapability(WingsProvider.WINGS).orElse(null)
						.getDataManager().getOrAddDefault(getSerializer()), arg);
//				source.getSource().sendFeedback(new StringTextComponent("a"), true);
			} catch (CommandSyntaxException | NullPointerException | IllegalArgumentException e) {
				e.printStackTrace();
			}
		}
	}
}