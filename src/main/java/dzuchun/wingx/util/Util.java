package dzuchun.wingx.util;

import java.util.ArrayList;
import java.util.function.Function;

public class Util {
	public static <T> String iterableToString(Iterable<T> iterable) {
		return iterableToString(iterable, t -> t.toString());
	}

	public static <T> String iterableToString(Iterable<T> iterable, Function<T, String> customToString) {
		if (!iterable.iterator().hasNext()) {
			return String.format("%s[]", iterable.getClass().getName());
		}
		String res = "";
		for (T o : iterable) {
			res += customToString.apply(o) + ", ";
		}
		res = res.substring(0, res.length() - 3);
		return String.format("%s[%s]", iterable.getClass().getName(), res);
	}

	public static <T> ArrayList<T> computeNewArrayList(Iterable<T> listIn, Function<T, T> computer) {
		ArrayList<T> res = new ArrayList<T>();
		for (T t : listIn) {
			res.add(computer.apply(t));
		}
		return res;
	}
}
