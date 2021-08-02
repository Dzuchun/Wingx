package dzuchun.wingx.capability.world.tricks;

import java.util.Collection;
import java.util.Collections;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dzuchun.wingx.init.Tricks;
import dzuchun.wingx.trick.IInterruptableTrick;
import dzuchun.wingx.trick.IPersistableTrick;
import dzuchun.wingx.trick.ITrick;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.registries.IForgeRegistry;

public class CapabilityActiveTricks { // TOOD move to capability class

	private static final Logger LOG = LogManager.getLogger();

	public static void register() {
		CapabilityManager.INSTANCE.register(IActiveTricksCapability.class,
				new Capability.IStorage<IActiveTricksCapability>() {

					private static final String AMOUNT_TAG = "number_of_tricks";
					private static final String REGISTRY_NAME_TAG = "registry_name";
					private static final String TRICK_TAG = "trick";

					private int i;

					@Override
					public INBT writeNBT(Capability<IActiveTricksCapability> capability,
							IActiveTricksCapability instance, Direction side) {
						CompoundNBT compound = new CompoundNBT();
						Collection<IInterruptableTrick> active_tricks = instance.getActiveTricks();
						compound.putInt(AMOUNT_TAG, active_tricks.size());
						this.i = 0;
						active_tricks.forEach(trick -> {
							ITrick.ITrickType<?> type = trick.getType();
							if ((type.getRegistryName() != null) && (trick instanceof IPersistableTrick)) {
								String registryName = type.getRegistryName().toString();
								CompoundNBT tmp = new CompoundNBT();
								tmp.putString(REGISTRY_NAME_TAG, registryName);
								@SuppressWarnings({ "unused", "rawtypes", "unchecked" })
								INBT dummy = tmp.put(TRICK_TAG,
										((IPersistableTrick.TrickType) type).writeToNBT((IPersistableTrick) trick));
								compound.put(this.i + "", tmp);
								this.i++;
							}
						});
						return compound;
					}

					@Override
					public void readNBT(Capability<IActiveTricksCapability> capability,
							IActiveTricksCapability instance, Direction side, INBT nbt) {
						CompoundNBT compound = (CompoundNBT) nbt;
						Collection<IInterruptableTrick> active_tricks = Collections.emptyList();

						IForgeRegistry<ITrick.ITrickType<?>> registry = Tricks.getRegistry(); // TODO
						// optimize
						if (registry == null) {
							LOG.error("Tricks registry does not exist!");
							return;
						}
						if (compound.contains(AMOUNT_TAG)) {
							for (this.i = compound.getInt(AMOUNT_TAG) - 1; this.i >= 0; this.i--) {
								if (compound.contains(this.i + "")) {
									CompoundNBT tmp = compound.getCompound(this.i + "");
									if (tmp.contains(REGISTRY_NAME_TAG) && tmp.contains(TRICK_TAG)) {
										String registryName = tmp.getString(REGISTRY_NAME_TAG);
										ResourceLocation resLoc = new ResourceLocation(registryName);
										if (registry.containsKey(resLoc)) {
											ITrick.ITrickType<?> type = registry.getValue(resLoc);
											if (type instanceof IPersistableTrick.TrickType) {
												if (type instanceof IInterruptableTrick.TrickType) {
													@SuppressWarnings("unchecked")
													IInterruptableTrick persistableTrick = (IInterruptableTrick) ((IPersistableTrick.TrickType<? extends IPersistableTrick>) type)
															.readFromNBT(tmp.get(TRICK_TAG));
													active_tricks.add(persistableTrick);
												} else { // TODO despagetify
													LOG.info(
															"Found not interruptable trick in NBT. Looks like developer's stupidity (trick will not persist)");
												}
											} else {
												LOG.warn(
														"{} registry name should be for IPersistabeTrick-implemented type, but it is for {}",
														resLoc, type.getClass().getName());
											}
										} else {
											LOG.warn("Can't find registry object for {}", registryName);
										}
									} else {
										LOG.warn(
												"NBT data is corrupted or lost, contact someone who understand what NBT is.");
									}
								} else {
									LOG.warn(
											"Can't load Trick no. {}. NBT data is corrupted or lost, contact someone who understand what NBT is.",
											this.i);
								}
							}
							instance.addActiveTricks(active_tricks);
						} else {
							LOG.warn(
									"Cant find AMOUNT_TAG=\"{}\" in saved data. NBT data is corrupted or lost, contact someone who understand what NBT is.",
									AMOUNT_TAG);
						}
					}
				}, ActiveTricksCapability::new);
	}

}