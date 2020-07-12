package dzuchun.wingx.init;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dzuchun.wingx.Wingx;
import dzuchun.wingx.entity.misc.WingsEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class EntityTypes {
	private static final Logger LOG = LogManager.getLogger();

	public static final String WINGS_NAME = "wings";

	public static RegistryObject<EntityType<WingsEntity>> WINGS_ENTITY_TYPE;

	public static void init(IEventBus bus) {

		final DeferredRegister<EntityType<? extends Entity>> REGISTER = DeferredRegister
				.create(ForgeRegistries.ENTITIES, Wingx.MOD_ID);
		WINGS_ENTITY_TYPE = REGISTER.register("wings",
				() -> EntityType.Builder
						.<WingsEntity>create(
								(EntityType<WingsEntity> entityType, World worldIn) -> new WingsEntity(worldIn),
								EntityClassification.MISC)
						.setCustomClientFactory((spawnEntity, world) -> new WingsEntity(world))
						.build(new ResourceLocation(Wingx.MOD_ID, WINGS_NAME).toString()));
		LOG.info("Registered wings entity type in deferred register");

		REGISTER.register(bus);
	}
}
