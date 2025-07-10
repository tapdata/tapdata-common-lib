package io.tapdata.common.utils;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

/**
 * @author samuel
 * @Description
 * @create 2025-07-10 17:36
 **/
public class MergeUtils {
	public static String dynamicKey(String key, Map<String, Object> data) {
		if (StringUtils.isBlank(key)) {
			return key;
		}
		if (MapUtils.isEmpty(data)) {
			return key;
		}

		// Quick check to avoid unnecessary processing
		if (!key.contains("${")) {
			return key;
		}

		return replaceTemplateVariables(key, data);
	}

	/**
	 * High-performance template variable replacement using char array traversal
	 * with batch character appending to minimize StringBuilder operations
	 */
	private static String replaceTemplateVariables(String template, Map<String, Object> data) {
		char[] chars = template.toCharArray();
		StringBuilder result = new StringBuilder(template.length() + 64); // Pre-allocate with extra space
		int len = chars.length;
		int i = 0;
		int lastAppend = 0; // Track last position we appended from

		while (i < len) {
			// Look for start of placeholder: ${
			if (i < len - 1 && chars[i] == '$' && chars[i + 1] == '{') {
				// Find the end of placeholder: }
				int keyStart = i + 2;
				int keyEnd = findClosingBrace(chars, keyStart, len);

				if (keyEnd == -1) {
					// Malformed placeholder, treat $ as regular character and continue
					i++;
					continue;
				}

				// Append any pending regular characters before this placeholder
				if (i > lastAppend) {
					result.append(chars, lastAppend, i - lastAppend);
				}

				// Extract key from char array (avoid substring creation when possible)
				String key = extractKey(chars, keyStart, keyEnd);

				// Get value from data
				Object value = io.tapdata.common.utils.MapUtils.getValueByKey(data, key);
				if (value != null) {
					result.append(value);
				} else {
					// Keep original placeholder if value not found
					result.append(chars, i, keyEnd + 1 - i);
				}

				i = keyEnd + 1;
				lastAppend = i;
			} else {
				i++;
			}
		}

		// Append any remaining characters
		if (lastAppend < len) {
			result.append(chars, lastAppend, len - lastAppend);
		}

		return result.toString();
	}

	/**
	 * Extract key from char array, optimized for common cases
	 */
	private static String extractKey(char[] chars, int start, int end) {
		// For short keys, direct construction might be faster than new String(char[], int, int)
		int length = end - start;
		if (length <= 16) {
			// Fast path for short keys - avoid array copy overhead
			StringBuilder keyBuilder = new StringBuilder(length);
			for (int i = start; i < end; i++) {
				keyBuilder.append(chars[i]);
			}
			return keyBuilder.toString();
		} else {
			// For longer keys, use the more efficient array constructor
			return new String(chars, start, length);
		}
	}

	/**
	 * Find the closing brace '}' starting from the given position
	 * @param chars the character array
	 * @param start starting position to search from
	 * @param len length of the array
	 * @return index of closing brace, or -1 if not found
	 */
	private static int findClosingBrace(char[] chars, int start, int len) {
		for (int i = start; i < len; i++) {
			if (chars[i] == '}') {
				return i;
			}
		}
		return -1;
	}
}
