package io.tapdata.entity.mapping.type;

import io.tapdata.entity.schema.type.TapDouble;
import io.tapdata.entity.schema.type.TapFloat;
import io.tapdata.entity.schema.type.TapType;
import io.tapdata.entity.schema.TapField;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TapFloatingPointMappingTest {
    @Test
    void shouldBuildStaticFloatMappingWithBinaryProperties() {
        Map<String, Object> info = new HashMap<>();
        info.put("to", "TapFloat");
        info.put("bit", 32);
        info.put("storageBytes", 4);
        info.put("effectivePrecision", 7);
        info.put("fixed", false);
        info.put("value", Arrays.asList("-3.402823466E+38", "3.402823466E+38"));

        TapMapping mapping = TapMapping.build(info);
        TapType type = mapping.toTapType("FLOAT", Collections.<String, String>emptyMap());

        assertInstanceOf(TapFloatMapping.class, mapping);
        assertInstanceOf(TapFloat.class, type);
        assertEquals(32, ((TapFloat) type).getBit());
        assertEquals(4, ((TapFloat) type).getStorageBytes());
        assertEquals(7, ((TapFloat) type).getEffectivePrecision());
    }

    @Test
    void shouldResolveParameterizedFloatingPointPrecision() {
        Map<String, Object> info = new HashMap<>();
        info.put("to", "TapFloat");
        info.put("mapping", "TapFloatingPoint");
        info.put("binaryPrecision", Arrays.asList(1, 53));
        info.put("defaultBinaryPrecision", 53);
        info.put("singlePrecision", precisionRule(1, 24, "TapFloat"));
        info.put("doublePrecision", precisionRule(25, 53, "TapDouble"));

        TapMapping mapping = TapMapping.build(info);
        assertInstanceOf(TapFloatingPointMapping.class, mapping);
        assertInstanceOf(TapFloat.class, mapping.toTapType("FLOAT", parameter("precision", "24")));
        assertInstanceOf(TapDouble.class, mapping.toTapType("FLOAT", parameter("precision", "25")));
        assertInstanceOf(TapDouble.class, mapping.toTapType("FLOAT", Collections.<String, String>emptyMap()));
    }

    @Test
    void shouldRankDedicatedFloatingPointMappingsAboveNumericFallback() {
        TapNumberMapping mapping = new TapNumberMapping();
        Map<String, Object> info = new HashMap<>();
        info.put("to", "TapNumber");
        mapping.from(info);

        TapField field = new TapField("f", "FLOAT").tapType(new TapFloat());
        TapFloatMapping floatMapping = new TapFloatMapping();
        TapDoubleMapping doubleMapping = new TapDoubleMapping();

        assertTrue(floatMapping.matchingScore(field).compareTo(doubleMapping.matchingScore(field)) > 0);
        assertTrue(doubleMapping.matchingScore(field).compareTo(mapping.matchingScore(field)) > 0);
        assertNotNull(mapping.fromTapType("numeric", field.getTapType()));
        assertEquals("numeric", mapping.fromTapType("numeric", field.getTapType()).getData());
    }

    @Test
    void shouldAcceptFloatAsACompatibleDoubleMapping() {
        TapDoubleMapping mapping = new TapDoubleMapping();

        assertNotNull(mapping.fromTapType("double precision", new TapFloat()));
        assertEquals("double precision", mapping.fromTapType("double precision", new TapFloat()).getData());
    }

    @Test
    void shouldNotTurnFloatingPointFallbackIntoFixedScaleNumber() {
        Map<String, Object> info = new HashMap<>();
        info.put("to", "TapNumber");
        info.put("precision", Arrays.asList(1, 1000));
        info.put("scale", Arrays.asList(0, 1000));
        info.put("fixed", true);

        TapMapping mapping = TapMapping.build(info);
        assertEquals("numeric", mapping.fromTapType("numeric[($precision,$scale)]", new TapFloat()).getData());
    }

    private Map<String, Object> precisionRule(int min, int max, String to) {
        Map<String, Object> rule = new HashMap<>();
        rule.put("range", Arrays.asList(min, max));
        rule.put("to", to);
        return rule;
    }

    private Map<String, String> parameter(String key, String value) {
        Map<String, String> params = new HashMap<>();
        params.put(key, value);
        return params;
    }
}
