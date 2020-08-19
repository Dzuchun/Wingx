package dzuchun.wingx.client.abillity;

import dzuchun.wingx.capability.entity.wings.IWingsCapability;
import dzuchun.wingx.capability.entity.wings.storage.Serializers;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TranslationTextComponent;

public class AbillityNodes {
	private static final Style NAME_STYLE = Style.EMPTY.setBold(true);

	public static final ExternalAbillityNode WINGX = new ExternalAbillityNode(0, 0, 1,
			new StringTextComponent("Wingx").setStyle(NAME_STYLE),
			new TranslationTextComponent("wingx.gui.node.desc.wingx")) {

		@Override
		public void setUnlocked(IWingsCapability capabilityIn) {
			this.isUnlocked = true;
		}
	};
	public static final InternalAbillityNode FIREBALL_INTERNAL = new InternalAbillityNode(0, 0, 2,
			new StringTextComponent("Fireball").setStyle(NAME_STYLE),
			new TranslationTextComponent("wingx.gui.node.desc.fireball_internal")) {

		@Override
		public void setUnlocked(IWingsCapability capabilityIn) {
			this.isUnlocked = true;
		}
	};
	public static final InternalAbillityNode FIREBALL_DISTANCE = new InternalAbillityNode(50, 10, 3,
			new StringTextComponent("Wingx").setStyle(NAME_STYLE),
			new TranslationTextComponent("wingx.gui.node.desc.fireball_internal.distance"), FIREBALL_INTERNAL) {

		@Override
		public void setUnlocked(IWingsCapability capabilityIn) {
			this.isUnlocked = false;
		}
	};
	public static final ExternalAbillityNode FIREBALL = new ExternalAbillityNode(-40, -10, 2,
			new StringTextComponent("Fireball").setStyle(NAME_STYLE),
			new TranslationTextComponent("wingx.gui.node.desc.fireball_external"), FIREBALL_INTERNAL, WINGX) {
		@Override
		public void setUnlocked(IWingsCapability capabilityIn) {
			this.isUnlocked = true;
		}
	};
	public static final ExternalAbillityNode HASTY = new ExternalAbillityNode(35, 0, 4,
			new StringTextComponent("Hasty").setStyle(NAME_STYLE),
			new TranslationTextComponent("wingx.gui.node.desc.hasty_external"), null, WINGX) {

		@Override
		public void setUnlocked(IWingsCapability capabilityIn) {
			this.isUnlocked = capabilityIn.getDataManager().getOrAddDefault(Serializers.HASTY_SERIALIZER).unlocked;
		}
	};
}
