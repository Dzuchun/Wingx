package dzuchun.wingx.init;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dzuchun.wingx.Wingx;
import dzuchun.wingx.entity.misc.WingsEntity;
import dzuchun.wingx.entity.projectile.FireballEntity;
import dzuchun.wingx.entity.projectile.HomingFireballEntity;
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
	public static final String FIREBALL_NAME = "fireball";
	public static final String HOMING_FIREBALL_NAME = "homing_fireball";

	public static RegistryObject<EntityType<WingsEntity>> wings_entity_type;
	public static RegistryObject<EntityType<FireballEntity>> fireball_entity_type;
	public static RegistryObject<EntityType<HomingFireballEntity>> homing_fireball_entity_type;

	public static void registerEntityTypes(IEventBus bus) {

		final DeferredRegister<EntityType<?>> register = DeferredRegister.create(ForgeRegistries.ENTITIES,
				Wingx.MOD_ID);
		wings_entity_type = register.register(WINGS_NAME, () -> EntityType.Builder
				.<WingsEntity>create((EntityType<WingsEntity> entityType, World worldIn) -> new WingsEntity(worldIn),
						EntityClassification.MISC)
				.setCustomClientFactory((spawnEntity, world) -> new WingsEntity(world)).setUpdateInterval(3)
				.immuneToFire().setShouldReceiveVelocityUpdates(false)
				.build(new ResourceLocation(Wingx.MOD_ID, WINGS_NAME).toString()));
		fireball_entity_type = register
				.register(FIREBALL_NAME,
						() -> EntityType.Builder
								.<FireballEntity>create(
										(EntityType<FireballEntity> entityType,
												World worldIn) -> new FireballEntity(worldIn),
										EntityClassification.MISC)
								.size(6f / 16f, 6f / 16f).immuneToFire()
								.build(new ResourceLocation(Wingx.MOD_ID, FIREBALL_NAME).toString()));
		homing_fireball_entity_type = register.register(HOMING_FIREBALL_NAME,
				() -> EntityType.Builder
						.<HomingFireballEntity>create(
								(EntityType<HomingFireballEntity> type,
										World world) -> (HomingFireballEntity) new FireballEntity(world),
								EntityClassification.MISC)
						.size(6f / 16f, 6f / 16f).immuneToFire()
						.build(new ResourceLocation(Wingx.MOD_ID, HOMING_FIREBALL_NAME).toString()));
		// TODO delete, just add parameters to normal fireball
		LOG.debug("Registered entity types in deferred register");
		register.register(bus);
	}
}
