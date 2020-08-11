package dzuchun.wingx.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class Util {
	public static String iterableToString(Iterable<? extends Object> iterable) {
		if (!iterable.iterator().hasNext()) {
			return String.format("%s[]", iterable.getClass().getName());
		}
		String res = "";
		for (Object o : iterable) {
			res += o + ", ";
		}
		res = res.substring(0, res.length() - 3);
		return String.format("%s[%s]", iterable.getClass().getName(), res);
	}
	
	public static <T> List<T> computeNewList(List<T> listIn, Function<T, T> computer){
		List<T> res = new ArrayList<T>();
		for (T t : listIn) {
			res.add(computer.apply(t));
		}
		return res;
	}
}
