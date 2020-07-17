package dzuchun.wingx.net;

import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dzuchun.wingx.Wingx;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public class WingxPacketHandler {
	@SuppressWarnings("unused")
	private static final Logger LOG = LogManager.getLogger();
	public static final String PROTOCOL_VERSION = "1";
	public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
			new ResourceLocation(Wingx.MOD_ID, "main"), () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals,
			PROTOCOL_VERSION::equals);

	public static final int TOGGLE_MESSAGE_INDEX = 355;
	public static final int OWNER_DATA_INDEX = 357;
	public static final int TRICK_PERFORMED_INDEX = 359;

	public static void init() {
		
		//Registering toggle wings
		INSTANCE.registerMessage(TOGGLE_MESSAGE_INDEX, ToggleWingsMessage.class, ToggleWingsMessage::encode, ToggleWingsMessage::decode,
				ToggleWingsMessage::handle);
//		INSTANCE.registerMessage(TOGGLE_MESSAGE_INDEX+1, ToggleWingsMessageResponse.class, ToggleWingsMessageResponse::encode, ToggleWingsMessageResponse::decode,
//				ToggleWingsMessageResponse::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
		
		//Registering data transfer to clients
		INSTANCE.registerMessage(OWNER_DATA_INDEX, OwnerDataMessage.class, OwnerDataMessage::encode, OwnerDataMessage::decode,
				OwnerDataMessage::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));

		//Registering trick performed message
		INSTANCE.registerMessage(TRICK_PERFORMED_INDEX, TrickPerformedMessage.class, TrickPerformedMessage::encode, TrickPerformedMessage::decode,
				TrickPerformedMessage::handle);
	}
}
