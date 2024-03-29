package dzuchun.wingx.client.input;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

import dzuchun.wingx.Wingx;
import dzuchun.wingx.client.render.overlay.GearSkyOverlay;
import dzuchun.wingx.damage.WingxDamageMap;
import dzuchun.wingx.damage.WingxDamageType;
import dzuchun.wingx.net.ToggleWingsMessage;
import dzuchun.wingx.net.TrickAimingMessage;
import dzuchun.wingx.net.TrickPerformedMessage;
import dzuchun.wingx.net.WingxPacketHandler;
import dzuchun.wingx.trick.DashPlayerTrick;
import dzuchun.wingx.trick.FireballCastPlayerTrick;
import dzuchun.wingx.trick.HomingFireballCastTargetedPlayerTrick;
import dzuchun.wingx.trick.ITrick;
import dzuchun.wingx.trick.PunchPlayerTrick;
import dzuchun.wingx.trick.SmashPlayerTrick;
import dzuchun.wingx.trick.SummonSwordPlayerTrick;
import dzuchun.wingx.trick.SwapPlayerTrick;
import dzuchun.wingx.trick.TemplateCastPlayerTrick;
import dzuchun.wingx.trick.meditation.MeditationPlayerTrick;
import dzuchun.wingx.util.Facing;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.EntityPredicate;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;

@OnlyIn(value = Dist.CLIENT)
@Mod.EventBusSubscriber(modid = Wingx.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class KeyEvents { // TODO fix pressing issues
	@SuppressWarnings("unused")
	private static final Logger LOG = LogManager.getLogger();

	public static void init() {
		for (WingxKey key : WingxKey.values()) {
			key.register();
		}
	}

	@SubscribeEvent
	public static void onKeyPressed(InputEvent.KeyInputEvent event) {
		if (event.getAction() == GLFW.GLFW_PRESS) {
			for (WingxKey key : WingxKey.values()) {
				if (key.isPressed()) {
					key.execute();
				}
			}
		}
	}

	@OnlyIn(value = Dist.CLIENT)
	public static enum WingxKey {
		SUMMON_WINGS {

			@Override
			public void execute() {
				WingxPacketHandler.INSTANCE.sendToServer(new ToggleWingsMessage(true));
			}

			@Override
			public void register() {
				this.key = new KeyBinding("key.wingx.summon_wings", KeyConflictContext.IN_GAME, KeyModifier.NONE,
						InputMappings.Type.KEYSYM.getOrMakeInput(-1), SECTION_NAME.get());
				super.register();
			}
		},
		MEDITATE {

			@SuppressWarnings("resource")
			@Override
			public void execute() {
				Minecraft.getInstance().player.sendStatusMessage(new TranslationTextComponent("wingx.meditating")
						.setStyle(Style.EMPTY.setFormatting(TextFormatting.DARK_GREEN)), true);
				WingxPacketHandler.INSTANCE.sendToServer(
						new TrickPerformedMessage.Server(new MeditationPlayerTrick(Minecraft.getInstance().player)));
			}

			@Override
			public void register() {
				this.key = new KeyBinding("key.wingx.meditate", KeyConflictContext.IN_GAME, KeyModifier.NONE,
						InputMappings.Type.KEYSYM.getOrMakeInput(-1), SECTION_NAME.get());
				super.register();
			}
		},
		DASH {

			@SuppressWarnings("resource")
			@Override
			public void execute() {
				WingxPacketHandler.INSTANCE.sendToServer(new TrickPerformedMessage.Server(
						new DashPlayerTrick(Minecraft.getInstance().player, Facing.UP, 1.0d, true)));
			}

			@Override
			public void register() {
				this.key = new KeyBinding("key.wingx.dash", KeyConflictContext.IN_GAME, KeyModifier.NONE,
						InputMappings.Type.KEYSYM.getOrMakeInput(-1), SECTION_NAME.get());
				super.register();
			}
		},
		SMASH {

			// TODO paramertize
			private final WingxDamageMap mainDamage = new WingxDamageMap() {
				{
					this.add(WingxDamageType.G, 10.0d);
				}
			};

			private final WingxDamageMap sideDamage = new WingxDamageMap() {
				{
					this.add(WingxDamageType.H, 5.0d);
				}
			};

			@SuppressWarnings("resource")
			@Override
			public void execute() {
				WingxPacketHandler.INSTANCE.sendToServer(
						new TrickPerformedMessage.Server(new SmashPlayerTrick(Minecraft.getInstance().player, 20, 1.0d,
								this.sideDamage, this.mainDamage, Minecraft.getInstance().player.getForward())));
			}

			@Override
			public void register() {
				this.key = new KeyBinding("key.wingx.smash", KeyConflictContext.IN_GAME, KeyModifier.NONE,
						InputMappings.Type.KEYSYM.getOrMakeInput(-1), SECTION_NAME.get());
				super.register();
			}
		},
		TMP {

			@Override
			public void execute() {
				Minecraft minecraft = Minecraft.getInstance();
				if (!minecraft.getRenderViewEntity().getUniqueID().equals(minecraft.player.getUniqueID())) {
					minecraft.setRenderViewEntity(minecraft.player);
					return;
				}
				List<LivingEntity> livingEntities = new ArrayList<LivingEntity>(0);
				minecraft.world.getAllEntities().forEach(entity -> {
					if (entity instanceof LivingEntity) {
						livingEntities.add((LivingEntity) entity);
					}
				});
				LivingEntity newEntity = minecraft.world.getClosestEntity(livingEntities,
						new EntityPredicate().setCustomPredicate(
								entity -> !entity.getUniqueID().equals(minecraft.player.getUniqueID())),
						minecraft.player, minecraft.player.getPosX(), minecraft.player.getPosY(),
						minecraft.player.getPosZ());
				if (newEntity != null) {
					minecraft.setRenderViewEntity(newEntity);
				} else {
					LOG.warn("No living entity found to switch");
				}
			}

			@Override
			public void register() {
				this.key = new KeyBinding("key.wingx.tmp", KeyConflictContext.IN_GAME, KeyModifier.NONE,
						InputMappings.Type.KEYSYM.getOrMakeInput(-1), SECTION_NAME.get());
				super.register();
			}
		},
		PUNCH {
			private PunchPlayerTrick trick = null;

			@SuppressWarnings("resource")
			@Override
			public void execute() {
				if ((this.trick == null) || (this.trick.getState().isError())) {
					this.trick = new PunchPlayerTrick(Minecraft.getInstance().player);
					WingxPacketHandler.INSTANCE.sendToServer(new TrickAimingMessage.Server(this.trick));
				} else {
					this.trick.endAim();
					this.trick.reportState();
					WingxPacketHandler.INSTANCE.sendToServer(new TrickPerformedMessage.Server(this.trick));
					this.trick = null;
				}
			}

			@Override
			public void register() {
				this.key = new KeyBinding("key.wingx.punch", KeyConflictContext.IN_GAME, KeyModifier.NONE,
						InputMappings.Type.KEYSYM.getOrMakeInput(-1), SECTION_NAME.get());
				super.register();
			}

			@Override
			public void setTrick(ITrick trickIn) {
				if (trickIn instanceof PunchPlayerTrick) {
					this.trick = (PunchPlayerTrick) trickIn;
				}
			}

		},
		SWAP {
			private SwapPlayerTrick trick = null;

			@SuppressWarnings("resource")
			@Override
			public void execute() {
				if ((this.trick == null) || (this.trick.getState().isError())) {
					this.trick = new SwapPlayerTrick(Minecraft.getInstance().player);
					WingxPacketHandler.INSTANCE.sendToServer(new TrickAimingMessage.Server(this.trick));
				} else {
					this.trick.endAim();
					this.trick.reportState();
					WingxPacketHandler.INSTANCE.sendToServer(new TrickPerformedMessage.Server(this.trick));
					this.trick = null;
				}
			}

			@Override
			public void register() {
				this.key = new KeyBinding("key.wingx.swap", KeyConflictContext.IN_GAME, KeyModifier.NONE,
						InputMappings.Type.KEYSYM.getOrMakeInput(-1), SECTION_NAME.get());
				super.register();
			}

			@Override
			public void setTrick(ITrick trickIn) {
				if (trickIn instanceof SwapPlayerTrick) {
					this.trick = (SwapPlayerTrick) trickIn;
				}
			}
		},
		CASTING_TEMPLATE {

			@SuppressWarnings("resource")
			@Override
			public void execute() {
				WingxPacketHandler.INSTANCE.sendToServer(new TrickPerformedMessage.Server(
						new TemplateCastPlayerTrick(Minecraft.getInstance().player, 40)));
			}

			@Override
			public void register() {
				this.key = new KeyBinding("key.wingx.casting.template", KeyConflictContext.IN_GAME, KeyModifier.NONE,
						InputMappings.Type.KEYSYM.getOrMakeInput(-1), SECTION_NAME.get());
				super.register();
			}
		},
		FIREBALL {

			@SuppressWarnings("resource")
			@Override
			public void execute() {
				WingxPacketHandler.INSTANCE.sendToServer(
						new TrickPerformedMessage.Server(new FireballCastPlayerTrick(Minecraft.getInstance().player)));
			}

			@Override
			public void register() {
				this.key = new KeyBinding("key.wingx.fireball", KeyConflictContext.IN_GAME, KeyModifier.NONE,
						InputMappings.Type.KEYSYM.getOrMakeInput(-1), SECTION_NAME.get());
				super.register();
			}
		},
		FIREBALL_HOMING {
			private HomingFireballCastTargetedPlayerTrick trick = null;

			@SuppressWarnings("resource")
			@Override
			public void execute() {
				if ((this.trick == null) || (this.trick.getState().isError())) {
					this.trick = new HomingFireballCastTargetedPlayerTrick(Minecraft.getInstance().player);
					WingxPacketHandler.INSTANCE.sendToServer(new TrickAimingMessage.Server(this.trick));
				} else {
					this.trick.endAim();
					this.trick.reportState();
					WingxPacketHandler.INSTANCE.sendToServer(new TrickPerformedMessage.Server(this.trick));
					this.trick = null;
				}
			}

			@Override
			public void register() {
				this.key = new KeyBinding("key.wingx.fireball_homing", KeyConflictContext.IN_GAME, KeyModifier.NONE,
						InputMappings.Type.KEYSYM.getOrMakeInput(-1), SECTION_NAME.get());
				super.register();
			}

			@Override
			public void setTrick(ITrick trickIn) {
				if (trickIn instanceof HomingFireballCastTargetedPlayerTrick) {
					this.trick = (HomingFireballCastTargetedPlayerTrick) trickIn;
				}
			}
		},
		SKY {

			@Override
			public void execute() {
				GearSkyOverlay instance = GearSkyOverlay.instance;
				if (instance != null) {
					instance.deactivate();
				} else {
					new GearSkyOverlay().activate();
				}
			}

			@Override
			public void register() {
				this.key = new KeyBinding("key.wingx.toggle_sky", KeyConflictContext.IN_GAME, KeyModifier.NONE,
						InputMappings.Type.KEYSYM.getOrMakeInput(-1), SECTION_NAME.get());
				super.register();
			}
		},
		SUMMON_SWORD {

			@SuppressWarnings("resource")
			@Override
			public void execute() {
				WingxPacketHandler.INSTANCE.sendToServer(
						new TrickPerformedMessage.Server(new SummonSwordPlayerTrick(Minecraft.getInstance().player)));
			}

			@Override
			public void register() {
				this.key = new KeyBinding("key.wingx.summon_sword", KeyConflictContext.IN_GAME, KeyModifier.NONE,
						InputMappings.Type.KEYSYM.getOrMakeInput(-1), SECTION_NAME.get());
				super.register();
			}
		},
		TEMPLATE_KEY_EVENT {

			@Override
			public void execute() {
				// TODO specify execute
			}

			@Override
			public void register() {
//			this.key = new KeyBinding("key.wingx.meditate", KeyConflictContext.IN_GAME, KeyModifier.NONE,
//					InputMappings.Type.KEYSYM.getOrMakeInput(-1), SECTION_NAME.get());
				super.register();
			}
		};

		private static final Logger LOG = LogManager.getLogger();
		private static final Supplier<String> SECTION_NAME = () -> new TranslationTextComponent(
				"key.wingx.section_name").getString(); // TODO divide into two sections

		protected KeyBinding key = null;

		public void register() {
			if (this.key != null) {
				ClientRegistry.registerKeyBinding(this.key);
			} else {
				LOG.warn("Tried to register null keybinding for {} wingx key", this.toString());
			}
		}

		public boolean isPressed() {
			if (this.key != null) {
				return this.key.isPressed();
			} else {
				return false;
			}
		}

		public abstract void execute();

		public void setTrick(ITrick trickIn) {
		}
	}
}