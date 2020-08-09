package dzuchun.wingx.client.abillity;

import dzuchun.wingx.capability.entity.wings.IWingsCapability;

public class AbillityNodes {
	public static final ExternalAbillityNode WINGX = new ExternalAbillityNode(0, 0) {

		@Override
		public void setUnlocked(IWingsCapability capabilityIn) {
			this.isUnlocked = true;
		}
	};
	public static final InternalAbillityNode FIREBALL_INTERNAL = new InternalAbillityNode(0, 0) {

		@Override
		public void setUnlocked(IWingsCapability capabilityIn) {
			this.isUnlocked = true;
		}
	};
	public static final InternalAbillityNode FIREBALL_DAMAGE = new InternalAbillityNode(50, 10, FIREBALL_INTERNAL) {

		@Override
		public void setUnlocked(IWingsCapability capabilityIn) {
			this.isUnlocked = false;
		}
	};
	public static final ExternalAbillityNode FIREBALL = new ExternalAbillityNode(-40, -10, FIREBALL_INTERNAL, WINGX) {
//TODO change constructors
		@Override
		public void setUnlocked(IWingsCapability capabilityIn) {
//			isUnlocked = capabilityIn.getDataManager().getOrAddDefault(Serializers.FIREBALL_SERIALIZER).isUnlocked;
			this.isUnlocked = true;
		}
	};
}
