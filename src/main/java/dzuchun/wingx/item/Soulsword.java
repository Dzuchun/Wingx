package dzuchun.wingx.item;

import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableMultimap.Builder;
import com.google.common.collect.Multimap;

import dzuchun.wingx.Wingx;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Rarity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

public class Soulsword extends Item {

	@SuppressWarnings("unused")
	private static final Logger LOG = LogManager.getLogger();

	public static final ResourceLocation ANIMATION_PROPERTY_LOCATION = new ResourceLocation(Wingx.MOD_ID,
			"soulsword_animation_stage");
	public static final float ANIMATION_DURATION = 10.0f;
	public static final IItemPropertyGetter ANIMATION_PROPERTY_GETTER = (stackIn, worldIn, entityIn) -> {
		World world = null;
		if (worldIn != null) {
			world = worldIn;
		} else if (entityIn != null) {
			world = entityIn.world;
		}
		return world == null ? 0.0f : (world.getGameTime() % ANIMATION_DURATION) / ANIMATION_DURATION;
	};

	public static final ResourceLocation SUMMONED_PROPERTY_LOCATION = new ResourceLocation(Wingx.MOD_ID,
			"soulsword_summoned");
	public static final String SUMMONED_TAG = SUMMONED_PROPERTY_LOCATION.getPath();
	public static final IItemPropertyGetter SUMMONED_PROPERTY_GETTER = (stackIn, worldIn,
			entityIn) -> stackIn.getOrCreateTag().getBoolean(SUMMONED_TAG) ? 1 : 0;

	public static final UUID MOVEMENT_SPEED_MODIFIER = UUID.fromString("3dc58d65-c4a7-4a0c-8eaa-fce7019a9a32");
	public static final UUID JUMP_HEIGHT_MODIFIER = UUID.fromString("e78facde-0bfc-4e9a-9b88-6a5eef8306b8");

	private static ImmutableMultimap<Attribute, AttributeModifier> EMPTY_MAP = ImmutableMultimap.of();
	private final Multimap<Attribute, AttributeModifier> ATTRIBUTE_MODIFIERS;

	public Soulsword() {
		super(new Item.Properties().group(ItemGroup.COMBAT).maxStackSize(1));
		Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
		final double attack_damage = 12.0d; // TODO parametrize
		builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Soulsword modifier",
				attack_damage, AttributeModifier.Operation.ADDITION));
		final double attack_speed = 1.0d; // TODO parametrize
		builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(ATTACK_SPEED_MODIFIER, "Soulsword modifier",
				attack_speed, AttributeModifier.Operation.ADDITION));
		final double movement_speed = 0.05d; // TODO parametrize
		builder.put(Attributes.MOVEMENT_SPEED, new AttributeModifier(MOVEMENT_SPEED_MODIFIER, "Soulsword modifier",
				movement_speed, AttributeModifier.Operation.ADDITION));
		// TODO add armor, toughness, health, etc
		this.ATTRIBUTE_MODIFIERS = builder.build();
	}

	@Override
	public boolean onDroppedByPlayer(ItemStack item, PlayerEntity player) {
		item.setCount(0);
		return false;
	}

	@Override
	public void inventoryTick(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
		super.inventoryTick(stack, worldIn, entityIn, itemSlot, isSelected);
		if (!isSelected) {
			stack.setCount(0);
		}
	}

	@Override
	public boolean showDurabilityBar(ItemStack stack) {
		return false;
	}

	@Override
	public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
		return false;
	}

	@Override
	public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlotType slot, ItemStack stack) {
		return ((slot == EquipmentSlotType.MAINHAND) && (stack.getOrCreateTag().getBoolean(SUMMONED_TAG)))
				? this.ATTRIBUTE_MODIFIERS
				: EMPTY_MAP;
	}

	// TODO make method to check if sword summoned

	@Override
	public ITextComponent getDisplayName(ItemStack stack) {
		return stack.getOrCreateTag().getBoolean(SUMMONED_TAG) ? new TranslationTextComponent("item.wingx.soulsword")
				: new TranslationTextComponent("item.wingx.summoning_soulsword");
		// TODO cache theese objects, no need to reinit them (?)
	}

	@Override
	public Rarity getRarity(ItemStack stack) {
		return stack.getOrCreateTag().getBoolean(SUMMONED_TAG) ? Rarity.EPIC : Rarity.UNCOMMON;
	}
}
