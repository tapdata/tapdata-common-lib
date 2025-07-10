package io.tapdata.common.utils;

/**
 * @author samuel
 * @Description
 * @create 2025-07-10 18:10
 **/
public class StringUtils {

	public final static String SUB_COLUMN_NAME = "__tapd8";

	public static String mongodbKeySpecialCharHandler(String key, String replacement) {

		if (org.apache.commons.lang3.StringUtils.isNotBlank(key)) {
			if (key.startsWith("$")) {
				key = key.replaceFirst("\\$", replacement);
			}

			if (key.contains(".") && !key.startsWith(SUB_COLUMN_NAME + ".")) {
				key = key.replaceAll("\\.", replacement);
			}

			if (key.contains(" ")) {
				key = key.replaceAll(" ", replacement);
			}
		}

		return key;
	}
}
