package dzuchun.wingx.capability.entity.wings.storage;

import java.util.Arrays;
import java.util.List;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;

import dzuchun.wingx.damage.WingxDamageMap;
import dzuchun.wingx.damage.WingxDamageType;
import dzuchun.wingx.trick.AbstractInterruptablePlayerTrick.InterruptCondition;

public class FireballData extends SerializedData {

	public int castDuration;
	public int packedColor;
	public InterruptCondition interruptCondition;
	public double initialSpeed;
	public boolean isUnlocked;
	public boolean homingUnlocked;
	public double homingForce;
	public int timesCasted;
	public WingxDamageMap damageMap;
	// TODO add parameters - fading speed, duration time, gravity amount

	public FireballData() {
		this.castDuration = 10;
		this.packedColor = 0x0000FFFF;
		this.interruptCondition = InterruptCondition.NO_CONDITION;
		this.initialSpeed = 0.7d;
		this.isUnlocked = false;
		this.homingUnlocked = false;
		this.homingForce = 0.02d;
		this.timesCasted = 0;
		this.damageMap = new WingxDamageMap();
		// TODO remove folowing, used for debug
		this.damageMap.add(WingxDamageType.H, 1.0d);
		this.damageMap.add(WingxDamageType.G, 3.0d);
		this.damageMap.add(WingxDamageType.F, 5.0d);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Serializer<FireballData> getSerializer() {
		return Serializers.FIREBALL_SERIALIZER;
	}

	@Override
	protected List<CommandLiteral<?, ?>> getCommandLiterals() {
		return Arrays.asList(
				new CommandLiteral<FireballData, Integer>("cast_duration", IntegerArgumentType.integer(),
						(data, i) -> data.castDuration = i, Integer.class),
				new CommandLiteral<FireballData, Integer>("packed_color", IntegerArgumentType.integer(),
						(data, i) -> data.packedColor = i, Integer.class),
				new CommandLiteral<FireballData, Integer>("interrupt_condition", IntegerArgumentType.integer(),
						(data, i) -> data.interruptCondition = InterruptCondition.getFromInt(i), Integer.class),
				new CommandLiteral<FireballData, Float>("damage-s", FloatArgumentType.floatArg(),
						(data, f) -> data.damageMap.setType(WingxDamageType.S, f), Float.class),
				new CommandLiteral<FireballData, Float>("damage-p", FloatArgumentType.floatArg(),
						(data, f) -> data.damageMap.setType(WingxDamageType.P, f), Float.class),
				new CommandLiteral<FireballData, Float>("damage-d", FloatArgumentType.floatArg(),
						(data, f) -> data.damageMap.setType(WingxDamageType.D, f), Float.class),
				new CommandLiteral<FireballData, Float>("damage-f", FloatArgumentType.floatArg(),
						(data, f) -> data.damageMap.setType(WingxDamageType.F, f), Float.class),
				new CommandLiteral<FireballData, Float>("damage-g", FloatArgumentType.floatArg(),
						(data, f) -> data.damageMap.setType(WingxDamageType.G, f), Float.class),
				new CommandLiteral<FireballData, Float>("damage-h", FloatArgumentType.floatArg(),
						(data, f) -> data.damageMap.setType(WingxDamageType.H, f), Float.class),
				new CommandLiteral<FireballData, Double>("initial_speed", DoubleArgumentType.doubleArg(),
						(data, d) -> data.initialSpeed = d, Double.class),
				new CommandLiteral<FireballData, Boolean>("is_unlocked", BoolArgumentType.bool(),
						(data, b) -> data.isUnlocked = b, Boolean.class),
				new CommandLiteral<FireballData, Boolean>("homing_unlocked", BoolArgumentType.bool(),
						(data, b) -> data.homingUnlocked = b, Boolean.class),
				new CommandLiteral<FireballData, Double>("homing_force", DoubleArgumentType.doubleArg(),
						(data, d) -> data.homingForce = d, Double.class),
				new CommandLiteral<FireballData, Integer>("times_casted", IntegerArgumentType.integer(),
						(data, i) -> data.timesCasted = i, Integer.class));
	}

}
