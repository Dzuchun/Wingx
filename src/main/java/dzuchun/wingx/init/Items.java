package dzuchun.wingx.init;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dzuchun.wingx.Wingx;
import dzuchun.wingx.item.Soulsword;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.Rarity;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class Items {
	@SuppressWarnings("unused")
	private static final Logger LOG = LogManager.getLogger();

	private static final DeferredRegister<Item> ITEMS_REGISTRY = DeferredRegister.create(ForgeRegistries.ITEMS,
			Wingx.MOD_ID);

	public static final RegistryObject<Item> SUMMONING_SOULSWORD = ITEMS_REGISTRY.register("summoning_soulsword",
			() -> new Item(new Item.Properties().rarity(Rarity.RARE).group(ItemGroup.COMBAT).maxStackSize(1)));
	public static final RegistryObject<Item> REAL_SOULSWORD = ITEMS_REGISTRY.register("real_soulsword", Soulsword::new);

	public static void registerItems(IEventBus bus) {
		ITEMS_REGISTRY.register(bus);
	}

}
