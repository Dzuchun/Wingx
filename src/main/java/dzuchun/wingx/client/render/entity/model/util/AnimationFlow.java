package dzuchun.wingx.client.render.entity.model.util;

import net.minecraft.util.SortedArraySet;

public class AnimationFlow {
	public SortedArraySet<AnimationValue> upcomingValues = SortedArraySet.newSet(0);
	public AnimationValue lastValue;
}
