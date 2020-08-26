package dzuchun.wingx.capability.entity.wings.storage;

import java.util.Arrays;
import java.util.List;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;

public class HastyData extends SerializedData {

	// Serialized
	public boolean isActive;
	public double probability;
	public float jump;
	public int cooldown;
	public boolean unlocked;

	// Not serialized
	public long lastProc;

	public HastyData() {
		this.isActive = false;
		this.probability = 0.01d;
		this.jump = 0.1f;
		this.cooldown = 100;
		this.unlocked = false;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Serializer<HastyData> getSerializer() {
		return Serializers.HASTY_SERIALIZER;
	}

	@Override
	protected List<CommandLiteral<?, ?>> getCommandLiterals() {
		return Arrays.asList(
				new CommandLiteral<HastyData, Boolean>("active", BoolArgumentType.bool(),
						(data, b) -> data.isActive = b, Boolean.class),
				new CommandLiteral<HastyData, Double>("probability", DoubleArgumentType.doubleArg(),
						(data, d) -> data.probability = d, Double.class),
				new CommandLiteral<HastyData, Float>("jump", FloatArgumentType.floatArg(), (data, f) -> data.jump = f,
						Float.class),
				new CommandLiteral<HastyData, Integer>("cooldown", IntegerArgumentType.integer(),
						(data, i) -> data.cooldown = i, Integer.class),
				new CommandLiteral<HastyData, Boolean>("unlocked", BoolArgumentType.bool(),
						(data, b) -> data.unlocked = b, Boolean.class));
	}

}
