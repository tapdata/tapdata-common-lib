package io.tapdata.pdk.core.utils;

import com.alibaba.fastjson.JSON;
import io.tapdata.entity.schema.type.TapDouble;
import io.tapdata.entity.schema.type.TapFloat;
import io.tapdata.entity.schema.type.TapCoefficientFloat;
import io.tapdata.entity.schema.type.TapNumber;
import io.tapdata.entity.schema.type.TapType;
import io.tapdata.entity.utils.JsonParser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TapConstantsFloatingPointTest {
    @Test
    void shouldPrioritizeTypeNameDetectorsBeforeLegacyNumericDetector() {
        int floatDetector = detectorIndex("TapFloat");
        int doubleDetector = detectorIndex("TapDouble");
        int numberDetector = detectorIndex(TapType.TYPE_NUMBER);

        assertTrue(floatDetector >= 0);
        assertTrue(doubleDetector >= 0);
        assertTrue(floatDetector < numberDetector);
        assertTrue(doubleDetector < numberDetector);
    }

    @Test
    void shouldDeserializeFloatingPointTypesWithFastjson() {
        TapType floatType = JSON.parseObject("{\"type\":8,\"typeName\":\"TapFloat\"}", TapType.class, TapConstants.tapdataParserConfig);
        TapType doubleType = JSON.parseObject("{\"type\":8,\"typeName\":\"TapDouble\"}", TapType.class, TapConstants.tapdataParserConfig);
        TapType legacyType = JSON.parseObject("{\"type\":8}", TapType.class, TapConstants.tapdataParserConfig);

        assertInstanceOf(TapFloat.class, floatType);
        assertInstanceOf(TapDouble.class, doubleType);
        assertEquals(TapNumber.class, legacyType.getClass());
    }

    @Test
    void shouldDeserializeCoefficientFloatBeforeLegacyNumericDetector() {
        int coefficientDetector = detectorIndex("TapCoefficientFloat");
        int numberDetector = detectorIndex(TapType.TYPE_NUMBER);
        TapType type = JSON.parseObject("{\"type\":8,\"typeName\":\"TapCoefficientFloat\"}",
                TapType.class, TapConstants.tapdataParserConfig);

        assertTrue(coefficientDetector >= 0);
        assertTrue(coefficientDetector < numberDetector);
        assertInstanceOf(TapCoefficientFloat.class, type);
    }

    @Test
    void shouldRoundTripInheritedFloatingPointTypesWithFastjson() {
        TapFloat floatSource = new TapFloat().binaryPrecision(24).precision(7);
        TapDouble doubleSource = new TapDouble().binaryPrecision(53).precision(15);

        String floatJson = JSON.toJSONString(floatSource);
        String doubleJson = JSON.toJSONString(doubleSource);

        TapType floatType = JSON.parseObject(floatJson, TapType.class, TapConstants.tapdataParserConfig);
        TapType doubleType = JSON.parseObject(doubleJson, TapType.class, TapConstants.tapdataParserConfig);

        TapFloat parsedFloat = assertInstanceOf(TapFloat.class, floatType);
        TapDouble parsedDouble = assertInstanceOf(TapDouble.class, doubleType);
        assertEquals(24, parsedFloat.getBinaryPrecision());
        assertEquals(53, parsedDouble.getBinaryPrecision());
        assertEquals(7, parsedFloat.getPrecision());
        assertEquals(15, parsedDouble.getPrecision());
    }

    private int detectorIndex(Object value) {
        for (int i = 0; i < TapConstants.abstractClassDetectors.size(); i++) {
            JsonParser.AbstractClassDetector detector = TapConstants.abstractClassDetectors.get(i);
            if (value.equals(detector.getValue())) {
                return i;
            }
        }
        return -1;
    }
}
