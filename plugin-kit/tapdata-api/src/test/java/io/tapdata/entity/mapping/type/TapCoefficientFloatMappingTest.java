package io.tapdata.entity.mapping.type;

import io.tapdata.entity.schema.type.TapFloat;
import io.tapdata.entity.schema.type.TapType;
import io.tapdata.entity.conversion.impl.TableFieldTypesGeneratorImpl;
import io.tapdata.entity.mapping.DefaultExpressionMatchingMap;
import io.tapdata.entity.schema.TapField;
import io.tapdata.entity.utils.DataMap;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TapCoefficientFloatMappingTest {
    @Test
    void shouldResolveEffectivePrecisionToTapFloat() {
        TapMapping mapping = TapMapping.build(mappingInfo());

        assertNotNull(mapping);
        assertEquals("TapCoefficientFloatMapping", mapping.getClass().getSimpleName());

        TapType type = mapping.toTapType("float(23)", parameter("precision", "23"));

        TapFloat tapFloat = assertInstanceOf(TapFloat.class, type);
        assertEquals(23, tapFloat.getPrecision());
    }

    @Test
    void shouldResolveEffectivePrecisionToTapDouble() {
        TapMapping mapping = TapMapping.build(mappingInfo());

        TapType type = mapping.toTapType("float(24)", parameter("precision", "24"));

        io.tapdata.entity.schema.type.TapDouble tapDouble =
                assertInstanceOf(io.tapdata.entity.schema.type.TapDouble.class, type);
        assertEquals(24, tapDouble.getPrecision());
        assertEquals(java.math.BigDecimal.valueOf(-Double.MAX_VALUE), tapDouble.getMinValue());
    }

    @Test
    void shouldKeepResolvedTypeDefaultsForLegacyBitMetadata() {
        Map<String, Object> info = mappingInfo();
        info.put("bit", 4);

        TapFloat tapFloat = assertInstanceOf(TapFloat.class,
                TapMapping.build(info).toTapType("float(23)", parameter("precision", "23")));

        assertEquals(4, tapFloat.getBit());
        assertEquals(4, tapFloat.getStorageBytes());
        assertEquals(false, tapFloat.getFixed());
    }

    @Test
    void shouldReuseExistingFloatingPointPropertiesAfterResolvingTargetType() {
        Map<String, Object> info = mappingInfo();
        info.put("unsigned", "unsigned");
        info.put("scale", Arrays.asList(0, 2));

        TapMapping mapping = TapMapping.build(info);
        TapFloat type = assertInstanceOf(TapFloat.class,
                mapping.toTapType("float(23) unsigned", parameter("precision", "23")));

        assertEquals(23, type.getPrecision());
        assertEquals(2, type.getScale());
        assertEquals(Boolean.TRUE, type.getUnsigned());
    }

    @Test
    void shouldRejectPrecisionOutsideDeclaredCoefficientRanges() {
        TapMapping mapping = TapMapping.build(mappingInfo());

        assertThrows(IllegalArgumentException.class,
                () -> mapping.toTapType("float(54)", parameter("precision", "54")));
    }

    @Test
    void shouldRejectOverlappingCoefficientRanges() {
        Map<String, Object> info = mappingInfo();
        Map<String, Object> coefficient = coefficientRanges();
        coefficient.put("TapFloat", Arrays.asList(1, 24));
        info.put("coefficient", coefficient);

        assertThrows(IllegalArgumentException.class, () -> new TapCoefficientFloatMapping().from(info));
    }

    @Test
    void shouldResolveCoefficientThroughExpressionMatchingMap() {
        Map<String, DataMap> entries = new LinkedHashMap<>();
        entries.put("float($precision)", DataMap.create()
                .kv("to", "TapCoefficientFloat")
                .kv("coefficient", coefficientRanges())
                .kv("fixed", false));

        TapField field = new TapField("value", "float(53)");
        new TableFieldTypesGeneratorImpl().autoFill(field, DefaultExpressionMatchingMap.map(entries));

        assertInstanceOf(io.tapdata.entity.schema.type.TapDouble.class, field.getTapType());
    }

    private Map<String, Object> mappingInfo() {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("to", "TapCoefficientFloat");

        info.put("coefficient", coefficientRanges());
        info.put("fixed", false);
        return info;
    }

    private Map<String, Object> coefficientRanges() {
        Map<String, Object> coefficient = new LinkedHashMap<>();
        coefficient.put("TapFloat", Arrays.asList(1, 23));
        coefficient.put("TapDouble", Arrays.asList(24, 53));
        return coefficient;
    }

    private Map<String, String> parameter(String key, String value) {
        return Collections.singletonMap(key, value);
    }
}
