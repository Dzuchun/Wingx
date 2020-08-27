package dzuchun.wingx.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import com.ibm.icu.impl.Pair;

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

	public static <T, U> ArrayList<U> computeNewArrayList(Iterable<T> listIn, Function<T, U> computer) {
		ArrayList<U> res = new ArrayList<U>();
		for (T t : listIn) {
			res.add(computer.apply(t));
		}
		return res;
	}

	@SafeVarargs
	public static <T, U> Map<T, U> mapOf(Pair<T, U>... pairs) {
		Map<T, U> res = new HashMap<T, U>(0);
		for (Pair<T, U> entry : pairs) {
			res.put(entry.first, entry.second);
		}
		return res;
	}
}
