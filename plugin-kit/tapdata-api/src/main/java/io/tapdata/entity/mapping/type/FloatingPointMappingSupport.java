package io.tapdata.entity.mapping.type;

import io.tapdata.entity.schema.type.TapDouble;
import io.tapdata.entity.schema.type.TapFloat;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/** Package-private parsing helpers shared by floating-point mappings. */
final class FloatingPointMappingSupport {
    private FloatingPointMappingSupport() {
    }

    static void apply(Map<String, Object> info, TapFloat type) {
        Integer bit = integer(info.get("bit"));
        if (bit != null) {
            type.bit(bit);
        }
        Integer storageBytes = integer(info.get("storageBytes"));
        if (storageBytes != null) {
            type.storageBytes(storageBytes);
        }
        Integer effectivePrecision = integer(info.get("effectivePrecision"));
        if (effectivePrecision != null) {
            type.effectivePrecision(effectivePrecision);
        }
        Integer binaryPrecision = integer(info.get("binaryPrecision"));
        if (binaryPrecision != null) {
            type.binaryPrecision(binaryPrecision);
        }
        type.minValue(valueAt(info.get("value"), 0, type.getMinValue()));
        type.maxValue(valueAt(info.get("value"), 1, type.getMaxValue()));
        Boolean fixed = booleanValue(info.get("fixed"));
        if (fixed != null) {
            type.fixed(fixed);
        }
        Boolean supportsNaN = booleanValue(info.get("supportsNaN"));
        if (supportsNaN != null) {
            type.supportsNaN(supportsNaN);
        }
        Boolean supportsInfinity = booleanValue(info.get("supportsInfinity"));
        if (supportsInfinity != null) {
            type.supportsInfinity(supportsInfinity);
        }
    }

    static void apply(Map<String, Object> info, TapDouble type) {
        Integer bit = integer(info.get("bit"));
        if (bit != null) {
            type.bit(bit);
        }
        Integer storageBytes = integer(info.get("storageBytes"));
        if (storageBytes != null) {
            type.storageBytes(storageBytes);
        }
        Integer effectivePrecision = integer(info.get("effectivePrecision"));
        if (effectivePrecision != null) {
            type.effectivePrecision(effectivePrecision);
        }
        Integer binaryPrecision = integer(info.get("binaryPrecision"));
        if (binaryPrecision != null) {
            type.binaryPrecision(binaryPrecision);
        }
        type.minValue(valueAt(info.get("value"), 0, type.getMinValue()));
        type.maxValue(valueAt(info.get("value"), 1, type.getMaxValue()));
        Boolean fixed = booleanValue(info.get("fixed"));
        if (fixed != null) {
            type.fixed(fixed);
        }
        Boolean supportsNaN = booleanValue(info.get("supportsNaN"));
        if (supportsNaN != null) {
            type.supportsNaN(supportsNaN);
        }
        Boolean supportsInfinity = booleanValue(info.get("supportsInfinity"));
        if (supportsInfinity != null) {
            type.supportsInfinity(supportsInfinity);
        }
    }

    static Integer integer(Object value) {
        return value instanceof Number ? ((Number) value).intValue() : null;
    }

    static Boolean booleanValue(Object value) {
        return value instanceof Boolean ? (Boolean) value : null;
    }

    private static BigDecimal valueAt(Object value, int index, BigDecimal defaultValue) {
        if (!(value instanceof List)) {
            return defaultValue;
        }
        List<?> values = (List<?>) value;
        if (values.size() <= index || values.get(index) == null) {
            return defaultValue;
        }
        try {
            return new BigDecimal(String.valueOf(values.get(index)));
        } catch (NumberFormatException ignored) {
            return defaultValue;
        }
    }
}
