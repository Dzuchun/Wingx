package dzuchun.wingx.capability.entity.wings.storage;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;

import net.minecraft.command.arguments.UUIDArgument;

public class BasicData extends SerializedData {

	public static final int MEDITATED_IN_END_FLAG = 0b0000000000000001;

	public boolean wingsActive;
	public UUID wingsUniqueId;
	public int meditationLength;
	public double requiredMeditationScore;
	public boolean needsEnd;
	public int stageFlags;

	public BasicData() {
		this.wingsActive = false;
		this.wingsUniqueId = null;
		this.meditationLength = 200;
		this.requiredMeditationScore = 1.0d;
		this.needsEnd = true;
		this.stageFlags = 0b0000000000000000;
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
				new CommandLiteral<BasicData, Integer>("meditation_length", IntegerArgumentType.integer(),
						(data, d) -> data.meditationLength = d, Integer.class),
				new CommandLiteral<BasicData, Double>("required_meditation_score", DoubleArgumentType.doubleArg(),
						(data, d) -> data.requiredMeditationScore = d, Double.class),
				new CommandLiteral<BasicData, Boolean>("needs_end", BoolArgumentType.bool(),
						(data, b) -> data.needsEnd = b, Boolean.class),
				new CommandLiteral<BasicData, Boolean>("meditated_in_end", BoolArgumentType.bool(),
						(data, b) -> data.setStageFlags(MEDITATED_IN_END_FLAG, b), Boolean.class));
	}

	public void setStageFlags(int flagMask, boolean b) {
		this.stageFlags = (b ? (this.stageFlags | flagMask) : (this.stageFlags & (~flagMask)));
	}

	public boolean getStageFlags(int flagMask) {
		return (this.stageFlags & flagMask) != 0;
	}

}
