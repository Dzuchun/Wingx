package dzuchun.wingx.capability.entity.wings.storage;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;

public class SoulswordData extends SerializedData {

	public boolean hasColors;
	public int[] colors;
	public int summonDurationTicks;

	public SoulswordData() {
		this.hasColors = false;
		this.colors = new int[4];
		this.summonDurationTicks = 160;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Serializer<SoulswordData> getSerializer() {
		return Serializers.SOULSWORD_SERIALIZER;
	}

	@Override
	protected List<CommandLiteral<? extends SerializedData, ?>> getCommandLiterals() {
		return Arrays
				.asList(new CommandLiteral<SoulswordData, Boolean>("has_colors", BoolArgumentType.bool(), (data, b) -> {
					data.hasColors = b;
					if (hasColors) {
						data.assignColors();
					}
				}, Boolean.class),
						new CommandLiteral<SoulswordData, Integer>("color_1", IntegerArgumentType.integer(),
								(data, c) -> data.colors[0] = c, Integer.class),
						new CommandLiteral<SoulswordData, Integer>("color_2", IntegerArgumentType.integer(),
								(data, c) -> data.colors[1] = c, Integer.class),
						new CommandLiteral<SoulswordData, Integer>("color_3", IntegerArgumentType.integer(),
								(data, c) -> data.colors[2] = c, Integer.class),
						new CommandLiteral<SoulswordData, Integer>("color_4", IntegerArgumentType.integer(),
								(data, c) -> data.colors[3] = c, Integer.class),
						new CommandLiteral<SoulswordData, Integer>("summon_duration", IntegerArgumentType.integer(),
								(data, n) -> data.summonDurationTicks = n, Integer.class));
	}

	void assignColors() {
		// TODO use other random instance, if can
		Random rand = new Random(); // TODO specify seed
		this.colors[0] = rand.nextInt();
		this.colors[1] = rand.nextInt();
		this.colors[2] = rand.nextInt();
		this.colors[3] = rand.nextInt();
	}

}
