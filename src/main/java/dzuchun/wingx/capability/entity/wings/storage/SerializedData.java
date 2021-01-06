package dzuchun.wingx.capability.entity.wings.storage;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;

import javax.annotation.Nonnull;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import dzuchun.wingx.capability.entity.wings.WingsProvider;
import dzuchun.wingx.command.impl.WingxComand;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntitySelector;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.TranslationTextComponent;

public abstract class SerializedData {
	private static final Logger LOG = LogManager.getLogger();

	public abstract <T extends SerializedData> Serializer<T> getSerializer();

	protected abstract List<CommandLiteral<? extends SerializedData, ?>> getCommandLiterals();

	@SuppressWarnings("rawtypes")
	private ArgumentBuilder argumentBuilder = null;

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Nonnull
	private ArgumentBuilder getArgumentBuilderInner() {
		ArgumentBuilder res = Commands.literal(this.getSerializer().getName());
		for (CommandLiteral<?, ?> par : this.getCommandLiterals()) {
			res.then(Commands.literal(par.name).then(Commands.argument(par.name, par.type)
					.executes((CommandContext<CommandSource> source) -> par.set(source))));
		}
		return res;
	}

	@Nonnull
	@SuppressWarnings("rawtypes")
	public ArgumentBuilder getArgumentBuilder() {
		if (this.argumentBuilder == null) {
			this.argumentBuilder = this.getArgumentBuilderInner();
		}
		return this.argumentBuilder;
	}

	protected class CommandLiteral<V extends SerializedData, U> {
		public CommandLiteral(String nameIn, ArgumentType<U> typeIn, BiConsumer<V, U> setterIn, Class<U> classIn) {
			this.name = nameIn;
			this.type = typeIn;
			this.setter = setterIn;
			this.classField = classIn;
		}

		private final String name;
		private final ArgumentType<U> type;
		private final BiConsumer<V, U> setter;
		private Class<U> classField;

		public int set(CommandContext<CommandSource> source) {
			try {
				LOG.debug("Executing set");
				U arg = source.getArgument(this.name, this.classField);
				List<? extends Entity> targetList;
				try {
					targetList = source.getArgument(WingxComand.TARGET_PAR_NAME, EntitySelector.class)
							.select(source.getSource());
				} catch (IllegalArgumentException e) {
//					e.printStackTrace();
					targetList = Arrays.asList(source.getSource().asPlayer());
				}
//				LOG.debug("Setting {}-{} to {} for {}", getSerializer().getName(), this.name, arg, target);
				for (Entity target : targetList) {
					if (target.getCapability(WingsProvider.WINGS).isPresent()) {
						this.setter.accept(target.getCapability(WingsProvider.WINGS).orElse(null).getDataManager()
								.getOrAddDefault(SerializedData.this.getSerializer()), arg);
						source.getSource().sendFeedback(new TranslationTextComponent("wingx.command.success.modify",
								target instanceof PlayerEntity ? ((PlayerEntity) target).getGameProfile().getName()
										: new TranslationTextComponent("wingx.commmand.non_player_target_cap"),
								String.format("%s-%s", SerializedData.this.getSerializer().getName(), this.name), arg)
										.setStyle(WingxComand.SUCCEESS_STYLE),
								true);
					} else {
						source.getSource().sendErrorMessage(
								new TranslationTextComponent("wingx.command.error.modify.target_no_wings")
										.setStyle(WingxComand.ERROR_STYLE));
					}
				}
				return 0;
			} catch (CommandSyntaxException e1) {
				e1.printStackTrace();
				return 1;
			}
		}
	}
}