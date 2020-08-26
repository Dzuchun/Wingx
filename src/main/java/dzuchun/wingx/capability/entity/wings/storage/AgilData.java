package dzuchun.wingx.capability.entity.wings.storage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;

public class AgilData extends SerializedData {

	// Serailized
	public boolean isActive;
	public int cooldown;
	public double probability;

	// Unserialized
	public long lastProc;

	public AgilData() {
		this.isActive = true;
		this.cooldown = 100;
		this.probability = 0.2d;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Serializer<AgilData> getSerializer() {
		return Serializers.AGIL_SERIALIZER;
	}

	@Override
	protected List<CommandLiteral<?, ?>> getCommandLiterals() {
		return Arrays.asList(
				new CommandLiteral<AgilData, Boolean>("active", BoolArgumentType.bool(), (data, b) -> data.isActive = b,
						Boolean.class),
				new CommandLiteral<AgilData, Integer>("cooldown", IntegerArgumentType.integer(),
						(data, i) -> ((AgilData) data).cooldown = i, Integer.class),
				new CommandLiteral<AgilData, Double>("probability", DoubleArgumentType.doubleArg(),
						(data, d) -> ((AgilData) data).probability = d, Double.class));
	}

}
