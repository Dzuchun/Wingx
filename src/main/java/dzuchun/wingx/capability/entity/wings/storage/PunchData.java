package dzuchun.wingx.capability.entity.wings.storage;

import java.util.Arrays;
import java.util.List;

import com.mojang.brigadier.arguments.DoubleArgumentType;

public class PunchData extends SerializedData {

	public double force;
	public double radius;
	// TODO add flags to save (dis)allowed targets

	public PunchData() {
		this.force = 1.0d;
		this.radius = 10.0d;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Serializer<PunchData> getSerializer() {
		return Serializers.PUNCH_SERIALIZER;
	}

	@Override
	protected List<CommandLiteral<? extends SerializedData, ?>> getCommandLiterals() {
		return Arrays.asList(
				new CommandLiteral<PunchData, Double>("force", DoubleArgumentType.doubleArg(),
						(data, d) -> data.force = d, Double.class),
				new CommandLiteral<PunchData, Double>("radius", DoubleArgumentType.doubleArg(),
						(data, d) -> data.radius = d, Double.class));
	}

}
