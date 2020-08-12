package dzuchun.wingx.util.animation;

import net.minecraft.util.SortedArraySet;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
/**
 * 
 * @author Dzuchun
 *
 */
@OnlyIn(Dist.CLIENT)
public class AnimationFlow {
	public SortedArraySet<AnimationValue> upcomingValues = SortedArraySet.newSet(0);
	public AnimationValue lastValue;
}
