package dzuchun.wingx.util;

import java.util.LinkedHashMap;

public class TimeMeasure {
	private static LinkedHashMap<String, Long> timePointers;

	public static void createTimePoint(String name) {
		if (!timePointers.containsKey(name)) {
			timePointers.put(name, System.currentTimeMillis());
		} else {
			timePointers.replace(name, System.currentTimeMillis());
		}
	}

	public static Long getIntervalLength(String name) {
		return System.currentTimeMillis() - timePointers.get(name);
	}

	public static void deleteTimePoint(String name) {
		if (timePointers.containsKey(name)) {
			timePointers.remove(name);
		}
	}
}
