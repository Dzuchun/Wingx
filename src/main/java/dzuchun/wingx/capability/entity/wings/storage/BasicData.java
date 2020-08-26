package dzuchun.wingx.capability.entity.wings.storage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;

import net.minecraft.command.arguments.UUIDArgument;

public class BasicData extends SerializedData {

	public boolean wingsActive;
	public UUID wingsUniqueId;
	public double requiredMeditationScore;
	public boolean needsEnd;

	public BasicData() {
		this.wingsActive = false;
		this.wingsUniqueId = null;
		this.requiredMeditationScore = 1.0d;
		this.needsEnd = true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Serializer<BasicData> getSerializer() {
		return Serializers.BASIC_SERIALIZER;
	}

	@Override
	protected List<CommandLiteral<?, ?>> getCommandLiterals() {
		return Arrays.asList(
				new CommandLiteral<BasicData, Boolean>("wings_active", BoolArgumentType.bool(),
						(data, b) -> data.wingsActive = b, Boolean.class),
				new CommandLiteral<BasicData, UUID>("wings_uuid", UUIDArgument.func_239194_a_(),
						(data, uuid) -> data.wingsUniqueId = uuid, UUID.class),
				new CommandLiteral<BasicData, Double>("required_meditation_score", DoubleArgumentType.doubleArg(),
						(data, d) -> data.requiredMeditationScore = d, Double.class),
				new CommandLiteral<BasicData, Boolean>("needs_end", BoolArgumentType.bool(),
						(data, b) -> data.needsEnd = b, Boolean.class));
	}

}
