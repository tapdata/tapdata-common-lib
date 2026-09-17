package io.tapdata.entity.schema.compat;

import io.tapdata.entity.mapping.type.TapMapping;
import io.tapdata.entity.schema.TapField;
import io.tapdata.entity.schema.type.TapDouble;
import io.tapdata.entity.schema.type.TapFloat;
import io.tapdata.entity.schema.type.TapNumber;
import io.tapdata.entity.schema.type.TapType;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Resolves legacy numeric fields without reloading a model or a connector.
 * Only an explicit, already-loaded source specification can upgrade a legacy
 * TapNumber to TapFloat or TapDouble.
 */
public final class LegacyTapTypeResolver {
    public static final String LEGACY_FLOAT_TYPE_UNRESOLVED = "LEGACY_FLOAT_TYPE_UNRESOLVED";
    public static final String LEGACY_NUMBER_KEPT = "LEGACY_NUMBER_KEPT";
    public static final String FLOAT_TYPE_RESOLVED = "FLOAT_TYPE_RESOLVED";
    private static final Pattern FIRST_INTEGER = Pattern.compile("\\(([-+]?\\d+)");

    private LegacyTapTypeResolver() {
    }

    public static LegacyTapTypeResolution resolve(String connectorId, TapField field, Map<String, Object> sourceSpec) {
        TapType currentType = field == null ? null : field.getTapType();
        String sourceType = sourceType(field);
        if (!(currentType instanceof TapNumber)) {
            return new LegacyTapTypeResolution(connectorId, sourceType, currentType, true, false,
                    LEGACY_NUMBER_KEPT, "Field is already represented by a dedicated or non-legacy TapType");
        }

        Map<String, Object> dataTypes = dataTypes(sourceSpec);
        Map<String, Object> mappingInfo = findMapping(dataTypes, sourceType);
        if (mappingInfo != null) {
            try {
                TapMapping mapping = TapMapping.build(mappingInfo);
                if (mapping != null) {
                    TapType resolvedType = mapping.toTapType(sourceType, parameters(sourceType));
                    if (resolvedType instanceof TapFloat || resolvedType instanceof TapDouble) {
                        return new LegacyTapTypeResolution(connectorId, sourceType, resolvedType, true, true,
                                FLOAT_TYPE_RESOLVED, "Legacy TapNumber normalized by the loaded source specification");
                    }
                }
            } catch (RuntimeException ignored) {
                // An invalid or incomplete spec must not turn a legacy number into a guessed float.
            }
        }

        if (looksLikeFloatingPoint(sourceType)) {
            return new LegacyTapTypeResolution(connectorId, sourceType, currentType, false, true,
                    LEGACY_FLOAT_TYPE_UNRESOLVED,
                    "Legacy floating-point field has no reliable in-memory source mapping");
        }
        return new LegacyTapTypeResolution(connectorId, sourceType, currentType, true, false,
                LEGACY_NUMBER_KEPT, "Legacy exact numeric field remains TapNumber");
    }

    private static String sourceType(TapField field) {
        if (field == null) {
            return null;
        }
        String sourceType = field.getPureDataType();
        if (sourceType == null || sourceType.trim().isEmpty()) {
            sourceType = field.getDataType();
        }
        return sourceType == null ? null : sourceType.trim();
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> dataTypes(Map<String, Object> sourceSpec) {
        if (sourceSpec == null) {
            return Collections.emptyMap();
        }
        Object dataTypes = sourceSpec.get("dataTypes");
        if (dataTypes instanceof Map) {
            return (Map<String, Object>) dataTypes;
        }
        return sourceSpec;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> findMapping(Map<String, Object> dataTypes, String sourceType) {
        if (sourceType == null || dataTypes == null) {
            return null;
        }
        String normalizedSource = normalize(sourceType);
        String sourceBase = baseType(normalizedSource);
        Object exact = dataTypes.get(sourceType);
        if (exact instanceof Map) {
            return (Map<String, Object>) exact;
        }
        for (Map.Entry<String, Object> entry : dataTypes.entrySet()) {
            if (!(entry.getValue() instanceof Map)) {
                continue;
            }
            String key = normalize(entry.getKey());
            if (key.equals(normalizedSource) || baseType(key).equals(sourceBase)) {
                return (Map<String, Object>) entry.getValue();
            }
        }
        return null;
    }

    private static Map<String, String> parameters(String sourceType) {
        if (sourceType == null) {
            return Collections.emptyMap();
        }
        Matcher matcher = FIRST_INTEGER.matcher(sourceType);
        if (!matcher.find()) {
            return Collections.emptyMap();
        }
        Map<String, String> params = new HashMap<>();
        params.put("precision", matcher.group(1));
        params.put("binaryPrecision", matcher.group(1));
        return params;
    }

    private static boolean looksLikeFloatingPoint(String sourceType) {
        if (sourceType == null) {
            return false;
        }
        String normalized = baseType(normalize(sourceType));
        return normalized.equals("float") || normalized.equals("float4") || normalized.equals("float8")
                || normalized.equals("real") || normalized.equals("double") || normalized.equals("double precision")
                || normalized.equals("binary_float") || normalized.equals("binary_double");
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private static String baseType(String value) {
        int bracket = value.indexOf('(');
        if (bracket >= 0) {
            return value.substring(0, bracket).trim();
        }
        return value.replace("$precision", "").trim();
    }
}
