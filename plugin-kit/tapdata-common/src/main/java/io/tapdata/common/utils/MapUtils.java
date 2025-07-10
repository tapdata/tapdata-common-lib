package io.tapdata.common.utils;

import org.apache.commons.lang3.StringUtils;

import java.util.*;

public class MapUtils {
	/**
	 * get value from map, before target
	 *
	 * @param dataMap
	 * @param key
	 * @return
	 * @throws NullPointerException
	 */
	public static Object getValueByKey(Map<String, Object> dataMap, String key) throws NullPointerException {
		return getValueByKey(dataMap, key, "");
	}

	/**
	 * get value from map in target
	 *
	 * @param dataMap
	 * @param key
	 * @param replacement
	 * @return
	 */
	public static Object getValueByKey(Map<String, Object> dataMap, String key, String replacement) {
		Object value = null;

		if (org.apache.commons.collections4.MapUtils.isEmpty(dataMap) || StringUtils.isBlank(key)) {
			return null;
		}

		if (needSplit(key)) {
			String[] split = key.split("\\.");

			if (split.length > 0) {

				List<String> keys = Arrays.stream(split).filter(StringUtils::isNotBlank).toList();

				value = dataMap;
				for (int i = 0; i < keys.size(); i++) {
					String subKey = keys.get(i);

					subKey = StringUtils.isNotBlank(replacement) ? io.tapdata.common.utils.StringUtils.mongodbKeySpecialCharHandler(subKey, replacement) : subKey;

					if (value instanceof Map && ((Map) value).containsKey(subKey)) {
						value = ((Map) value).get(subKey);
					} else {
						value = null;
						break;
					}
				}
			}
		}

		key = trimKey(key);

		if (value == null) {
			key = StringUtils.isNotBlank(replacement) ? io.tapdata.common.utils.StringUtils.mongodbKeySpecialCharHandler(key, replacement) : key;
			if (dataMap.containsKey(key)) {
				value = dataMap.get(key);
			}
		}

		return value;
	}

	/**
	 * 通过特殊符号"."判断是否是多层级的字段
	 *
	 * @param key
	 * @return
	 */
	public static boolean needSplit(String key) {
		return key.contains(".") && !key.startsWith(".") && !key.endsWith(".");
	}

	/**
	 * 去除key的双引号,oracle列名可能携带双引号
	 *
	 * @param key
	 * @return
	 */
	private static String trimKey(String key) {
		if (key.contains("\"") && key.startsWith("\"") && key.endsWith("\"")) {
			key = key.replace("\"", "");
		}
		return key;
	}

	private static void recursiveMapGetKeys(Map<String, Object> subMap, Set<String> keys, String parentKey) {
		Iterator<String> keyIter = subMap.keySet().iterator();

		while (keyIter.hasNext()) {
			String key = keyIter.next();
			Object value = subMap.get(key);

			keys.add(parentKey + "." + key);

			Optional.ofNullable(value).ifPresent(v -> {
				if (v instanceof Map) {
					recursiveMapGetKeys((Map<String, Object>) v, keys, parentKey + "." + key);
				} else if (v instanceof List) {
					recursiveListGetKeys((List) v, keys, parentKey + "." + key);
				}
			});
		}
	}

	private static void recursiveListGetKeys(List list, Set<String> keys, String parentKey) {
		for (int i = 0; i < list.size(); i++) {
			Object value = list.get(i);

			Optional.ofNullable(value).ifPresent(v -> {
				if (v instanceof Map) {
					recursiveMapGetKeys((Map<String, Object>) v, keys, parentKey);
				} else if (v instanceof List) {
					recursiveListGetKeys((List) v, keys, parentKey);
				}
			});
		}
	}
}
