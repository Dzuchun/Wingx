package dzuchun.wingx.client.input;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dzuchun.wingx.Wingx;
import dzuchun.wingx.net.ToggleWingsMessage;
import dzuchun.wingx.net.TrickPerformedMessage;
import dzuchun.wingx.net.WingxPacketHandler;
import dzuchun.wingx.trick.DashPlayerTrick;
import dzuchun.wingx.trick.SmashPlayerTrick;
import dzuchun.wingx.util.Facing;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
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
public class KeyEvents {
	@SuppressWarnings("unused")
	private static final Logger LOG = LogManager.getLogger();

	public static void init() {
		for (WingxKey key : WingxKey.values()) {
			key.register();
		}
	}

	@SubscribeEvent
	public static void onKeyPressed(InputEvent.KeyInputEvent event) {
		for (WingxKey key : WingxKey.values()) {
			if (key.isPressed()) {
				key.execute();
			}
		}
	}
}

enum WingxKey {
	SUMMON_WINGS {

		@Override
		public void execute() {
			WingxPacketHandler.INSTANCE.sendToServer(new ToggleWingsMessage(true));
		}

		@Override
		public boolean isPressed() {
			return this.key.isPressed();
		}

		@Override
		public void register() {
			this.key = new KeyBinding("key.wingx.summon_wings", KeyConflictContext.IN_GAME, KeyModifier.NONE,
					InputMappings.Type.KEYSYM.getOrMakeInput(-1), SECTION_NAME);
			super.register();
		}
	},
	MEDITATE {

		@SuppressWarnings("resource")
		@Override
		public void execute() {
			Minecraft.getInstance().player.sendStatusMessage(new TranslationTextComponent("wingx.meditating")
					.func_230530_a_(Style.field_240709_b_.func_240712_a_(TextFormatting.DARK_GREEN)), true);
		}

		@Override
		public boolean isPressed() {
			if (this.key != null) {
				return this.key.isPressed();
			} else {
				return super.isPressed();
			}
		}

		@Override
		public void register() {
			this.key = new KeyBinding("key.wingx.meditate", KeyConflictContext.IN_GAME, KeyModifier.NONE,
					InputMappings.Type.KEYSYM.getOrMakeInput(-1), SECTION_NAME);
			super.register();
		}
	},
	DASH {

		@SuppressWarnings("resource")
		@Override
		public void execute() {
			WingxPacketHandler.INSTANCE.sendToServer(new TrickPerformedMessage(
					new DashPlayerTrick(Minecraft.getInstance().player, Facing.UP, 1.0d, true)));
		}

		@Override
		public boolean isPressed() {
			if (this.key != null) {
				return this.key.isPressed();
			} else {
				return super.isPressed();
			}
		}

		@Override
		public void register() {
			this.key = new KeyBinding("key.wingx.dash", KeyConflictContext.IN_GAME, KeyModifier.NONE,
					InputMappings.Type.KEYSYM.getOrMakeInput(-1), SECTION_NAME);
			super.register();
		}
	},
	SMASH {

		@SuppressWarnings("resource")
		@Override
		public void execute() {
			WingxPacketHandler.INSTANCE
					.sendToServer(new TrickPerformedMessage(new SmashPlayerTrick(Minecraft.getInstance().player, 20,
							1.0d, 1.0f, 5.0f, Minecraft.getInstance().player.getForward())));
		}

		@Override
		public boolean isPressed() {
			if (this.key != null) {
				return this.key.isPressed();
			} else {
				return super.isPressed();
			}
		}

		@Override
		public void register() {
			this.key = new KeyBinding("key.wingx.smash", KeyConflictContext.IN_GAME, KeyModifier.NONE,
					InputMappings.Type.KEYSYM.getOrMakeInput(-1), SECTION_NAME);
			super.register();
		}
	},
	TEMPLATE {

		@Override
		public void execute() {
			// TODO specify execute
		}

		@Override
		public boolean isPressed() {
			if (this.key != null) {
				return this.key.isPressed();
			} else {
				return super.isPressed();
			}
		}

		@Override
		public void register() {
//			this.key = new KeyBinding("key.wingx.meditate", KeyConflictContext.IN_GAME, KeyModifier.NONE,
//					InputMappings.Type.KEYSYM.getOrMakeInput(-1), SECTION_NAME);
			super.register();
		}
	};

	private static final Logger LOG = LogManager.getLogger();
	private static final String SECTION_NAME = "Wingx mod";

	protected KeyBinding key;

	public void register() {
		if (key != null) {
			ClientRegistry.registerKeyBinding(key);
		} else {
			LOG.warn("Tried to register null keybinding for {} wingx key", this.toString());
		}
	}

	public boolean isPressed() {
		return false;
	}

	public abstract void execute();
}