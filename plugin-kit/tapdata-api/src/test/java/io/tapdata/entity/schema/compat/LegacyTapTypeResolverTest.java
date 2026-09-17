package io.tapdata.entity.schema.compat;

import io.tapdata.entity.schema.TapField;
import io.tapdata.entity.schema.type.TapDouble;
import io.tapdata.entity.schema.type.TapFloat;
import io.tapdata.entity.schema.type.TapNumber;
import io.tapdata.entity.schema.type.TapType;
import io.tapdata.entity.mapping.DefaultExpressionMatchingMap;
import io.tapdata.entity.utils.DataMap;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LegacyTapTypeResolverTest {
    @Test
    void shouldNormalizeLegacyNumberWhenCorrectedSpecIdentifiesFloat() {
        TapField field = legacyField("FLOAT");
        Map<String, Object> sourceSpec = spec("float", "TapFloat");

        LegacyTapTypeResolution resolution = LegacyTapTypeResolver.resolve("mysql", field, sourceSpec);

        assertTrue(resolution.isResolved());
        assertInstanceOf(TapFloat.class, resolution.getTapType());
    }

    @Test
    void shouldResolveParameterizedLegacyDoubleWithoutReloadingModel() {
        TapField field = legacyField("float(53)");
        Map<String, Object> entry = new HashMap<>();
        entry.put("to", "TapFloat");
        entry.put("mapping", "TapFloatingPoint");
        entry.put("binaryPrecision", Arrays.asList(1, 53));
        entry.put("defaultBinaryPrecision", 53);
        entry.put("singlePrecision", rule(1, 24));
        entry.put("doublePrecision", rule(25, 53));
        Map<String, Object> sourceSpec = new HashMap<>();
        sourceSpec.put("float", entry);

        LegacyTapTypeResolution resolution = LegacyTapTypeResolver.resolve("sqlserver", field, sourceSpec);

        assertTrue(resolution.isResolved());
        assertInstanceOf(TapDouble.class, resolution.getTapType());
        assertEquals(53, ((TapDouble) resolution.getTapType()).getBinaryPrecision());
    }

    @Test
    void shouldKeepLegacyNumberForExactNumericTypes() {
        TapField field = legacyField("DECIMAL(18,2)");
        LegacyTapTypeResolution resolution = LegacyTapTypeResolver.resolve("mysql", field, Collections.<String, Object>emptyMap());

        assertTrue(resolution.isResolved());
        assertInstanceOf(TapNumber.class, resolution.getTapType());
        assertFalse(resolution.isLegacyFloat());
    }

    @Test
    void shouldNotGuessWhenLegacyFloatHasNoReliableSpec() {
        TapField field = legacyField("FLOAT");
        LegacyTapTypeResolution resolution = LegacyTapTypeResolver.resolve("unknown", field, Collections.<String, Object>emptyMap());

        assertFalse(resolution.isResolved());
        assertEquals(LegacyTapTypeResolver.LEGACY_FLOAT_TYPE_UNRESOLVED, resolution.getCode());
        assertInstanceOf(TapNumber.class, resolution.getTapType());
    }

    @Test
    void shouldResolveAgainstAlreadyLoadedExpressionMapWithoutRebuildingSpec() {
        DataMap floatType = DataMap.create().kv("to", "TapFloat").kv("bit", 32)
                .kv("storageBytes", 4).kv("effectivePrecision", 7);
        Map<String, DataMap> entries = new HashMap<>();
        entries.put("real", floatType);

        LegacyTapTypeResolution resolution = LegacyTapTypeResolver.resolve(
                "postgres", legacyField("real"), DefaultExpressionMatchingMap.map(entries));

        assertTrue(resolution.isResolved());
        assertInstanceOf(TapFloat.class, resolution.getTapType());
    }

    private TapField legacyField(String dataType) {
        return new TapField("amount", dataType).tapType(new TapNumber());
    }

    private Map<String, Object> spec(String key, String to) {
        Map<String, Object> sourceSpec = new HashMap<>();
        Map<String, Object> entry = new HashMap<>();
        entry.put("to", to);
        sourceSpec.put(key, entry);
        return sourceSpec;
    }

    private Map<String, Object> rule(int min, int max) {
        Map<String, Object> range = new HashMap<>();
        range.put("range", Arrays.asList(min, max));
        return range;
    }
}
