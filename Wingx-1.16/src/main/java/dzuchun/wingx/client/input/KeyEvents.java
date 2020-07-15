package dzuchun.wingx.client.input;

import java.awt.event.KeyEvent;
import java.util.LinkedHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dzuchun.wingx.Wingx;
import dzuchun.wingx.net.ToggleWingsMessage;
import dzuchun.wingx.net.WingxPacketHandler;
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

	private static enum KeyName {
		SUMMON_WINGS
	}

	private static final LinkedHashMap<KeyName, KeyBinding> keyBindings = new LinkedHashMap<KeyName, KeyBinding>() {
		private static final long serialVersionUID = 1L;
		{
			this.put(KeyName.SUMMON_WINGS, new KeyBinding("key.wingx.summon_wings", KeyConflictContext.IN_GAME,
					KeyModifier.NONE, InputMappings.Type.KEYSYM.getOrMakeInput(-1), "Wingx mod"));
		}
	};

	public static void init() {
		for (KeyBinding key : keyBindings.values()) {
			ClientRegistry.registerKeyBinding(key);
		}
	}

	@SuppressWarnings("resource")
	@SubscribeEvent
	public static void onKeyPressed(InputEvent.KeyInputEvent event) {
//		LOG.info("Input event detected");
		if (keyBindings.get(KeyName.SUMMON_WINGS).isPressed()) {
//			LOG.info("Wings summoning event detected");
//			Minecraft.getInstance().player.sendStatusMessage(new TranslationTextComponent("wings.summoned")
//					.func_230530_a_(Style.field_240709_b_.func_240712_a_(TextFormatting.AQUA)), true);
			WingxPacketHandler.INSTANCE.sendToServer(new ToggleWingsMessage(true));
		}
	}
}