package dzuchun.wingx.item;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Rarity;
import net.minecraft.world.World;

public class Soulsword extends Item {

	public Soulsword() {
		super(new Item.Properties().rarity(Rarity.EPIC).group(ItemGroup.COMBAT).maxStackSize(1));
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
			// TODO disable rendering some fancy stuff
		} else {
			if (worldIn.isRemote) {
				// TODO render some fancy stuff (that shouldn't be here?)
			}
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

}
