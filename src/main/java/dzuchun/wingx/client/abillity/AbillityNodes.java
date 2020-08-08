package dzuchun.wingx.client.abillity;

import dzuchun.wingx.capability.entity.wings.IWingsCapability;
import dzuchun.wingx.capability.entity.wings.storage.Serializers;

public class AbillityNodes {
	public static final ExternalAbillityNode WINGX = new ExternalAbillityNode(0, 0) {

		@Override
		public void setUnlocked(IWingsCapability capabilityIn) {
			isUnlocked = true;
		}
	};
	public static final ExternalAbillityNode FIREBALL = new ExternalAbillityNode(-40, -10, null, WINGX) {
//TODO change constructors
		@Override
		public void setUnlocked(IWingsCapability capabilityIn) {
			isUnlocked = capabilityIn.getDataManager().getOrAddDefault(Serializers.FIREBALL_SERIALIZER).isUnlocked;
		}
	};
}
