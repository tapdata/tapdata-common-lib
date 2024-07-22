package io.tapdata.entity.utils;

/**
 * @author samuel
 * @Description
 * @create 2024-07-11 15:31
 **/
public class PropertyUtils {
	public static String getenv(String key) {
		return System.getenv(key);
	}

	public static String getProperty(String key) {
		String value = System.getProperty(key);
		if(value == null)
			value = getenv(key);
		return value;
	}

	public static String getProperty(String key, String defaultValue) {
		String value = getProperty(key);
		if(value == null)
			value = defaultValue;
		return value;
	}

	public static boolean getPropertyBool(String key, boolean defaultValue) {
		String value = getProperty(key);
		if (value != null) {
			switch (value.trim().toLowerCase()) {
				case "y":
				case "yes":
				case "t":
				case "true":
				case "1":
					return Boolean.TRUE;
				case "n":
				case "no":
				case "f":
				case "false":
				case "0":
					return Boolean.FALSE;
				default:
					break;
			}
		}
		return defaultValue;
	}

	public static int getPropertyInt(String key, int defaultValue) {
		String value = getProperty(key);
		Integer valueInt = null;
		if(value != null) {
			try {
				valueInt = Integer.parseInt(value);
			} catch(Throwable ignored) {}
		}
		if(valueInt == null)
			valueInt = defaultValue;
		return valueInt;
	}

	public static long getPropertyLong(String key, long defaultValue) {
		String value = getProperty(key);
		Long valueLong = null;
		if(value != null) {
			try {
				valueLong = Long.parseLong(value);
			} catch(Throwable ignored) {}
		}
		if(valueLong == null)
			valueLong = defaultValue;
		return valueLong;
	}
}
