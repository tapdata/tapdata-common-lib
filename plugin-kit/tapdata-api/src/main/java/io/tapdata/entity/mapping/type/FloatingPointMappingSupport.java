package io.tapdata.entity.mapping.type;

import io.tapdata.entity.schema.type.TapDouble;
import io.tapdata.entity.schema.type.TapFloat;
import io.tapdata.entity.schema.type.TapNumber;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/** Package-private parsing helpers shared by floating-point mappings. */
final class FloatingPointMappingSupport {
    private FloatingPointMappingSupport() {
    }

    static void apply(Map<String, Object> info, String dataType, Map<String, String> params, TapFloat type) {
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
        boolean legacyNumberMapping = applyLegacyNumberMapping(info, dataType, params, type);
        if (!legacyNumberMapping) {
            applyNumberProperties(info, dataType, params, type);
            type.minValue(valueAt(info.get("value"), 0, type.getMinValue()));
            type.maxValue(valueAt(info.get("value"), 1, type.getMaxValue()));
        }
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

    static void apply(Map<String, Object> info, String dataType, Map<String, String> params, TapDouble type) {
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
        boolean legacyNumberMapping = applyLegacyNumberMapping(info, dataType, params, type);
        if (!legacyNumberMapping) {
            applyNumberProperties(info, dataType, params, type);
            type.minValue(valueAt(info.get("value"), 0, type.getMinValue()));
            type.maxValue(valueAt(info.get("value"), 1, type.getMaxValue()));
        }
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

    private static void applyNumberProperties(Map<String, Object> info, String dataType,
                                              Map<String, String> params, TapNumber type) {
        Integer precision = integer(info.get("precision"));
        Integer scale = integer(info.get("scale"));
        if (params != null) {
            Integer parameterPrecision = integer(params.get("precision"));
            if (parameterPrecision != null) {
                precision = parameterPrecision;
            }
            Integer parameterScale = integer(params.get("scale"));
            if (parameterScale != null) {
                scale = parameterScale;
            }
        }
        if (precision != null) {
            type.precision(precision);
        }
        if (scale != null) {
            type.scale(scale);
        }
        if (matchesOption(info.get("unsigned"), dataType)) {
            type.unsigned(true);
        }
        if (matchesOption(info.get("zerofill"), dataType)) {
            type.zerofill(true);
        }
        Boolean cannotWrite = booleanValue(info.get("cannotWrite"));
        if (cannotWrite != null) {
            type.setCannotWrite(cannotWrite);
        }
    }

    /**
     * Floating-point mappings can still use the legacy TapNumber specification.
     * Resolve that specification through TapNumberMapping so ranges, preferred
     * values, defaults, parameters and value boundaries remain identical to the
     * pre-TapFloat/TapDouble behavior.
     */
    private static boolean applyLegacyNumberMapping(Map<String, Object> info, String dataType,
                                                    Map<String, String> params, TapNumber type) {
        if (!hasLegacyNumberProperties(info)) {
            return false;
        }

        TapNumberMapping legacyMapping = new TapNumberMapping();
        legacyMapping.from(info);
        TapNumber legacyType = (TapNumber) legacyMapping.toTapType(dataType, params);

        if (info.containsKey(TapNumberMapping.KEY_BIT) && legacyType.getBit() != null) {
            type.bit(legacyType.getBit());
        }
        type.precision(legacyType.getPrecision());
        type.scale(legacyType.getScale());
        type.minValue(legacyType.getMinValue());
        type.maxValue(legacyType.getMaxValue());
        if (legacyType.getUnsigned() != null) {
            type.unsigned(legacyType.getUnsigned());
        }
        if (legacyType.getZerofill() != null) {
            type.zerofill(legacyType.getZerofill());
        }
        if (info.containsKey(TapNumberMapping.KEY_FIXED) && legacyType.getFixed() != null) {
            type.fixed(legacyType.getFixed());
        }
        return true;
    }

    private static boolean hasLegacyNumberProperties(Map<String, Object> info) {
        return info.containsKey(TapNumberMapping.KEY_PRECISION)
                || info.containsKey(TapNumberMapping.KEY_PRECISION_DEFAULT)
                || info.containsKey(TapNumberMapping.KEY_PRECISION_PREFER)
                || info.containsKey(TapNumberMapping.KEY_SCALE)
                || info.containsKey(TapNumberMapping.KEY_SCALE_DEFAULT)
                || info.containsKey(TapNumberMapping.KEY_SCALE_PREFER);
    }

    private static boolean matchesOption(Object option, String dataType) {
        return option instanceof String && dataType != null && dataType.contains((String) option);
    }

    static Integer integer(Object value) {
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        if (value instanceof String) {
            try {
                return Integer.valueOf((String) value);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
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
