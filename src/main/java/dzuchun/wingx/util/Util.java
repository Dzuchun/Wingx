package dzuchun.wingx.util;

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
}
