package io.tapdata.entity.schema.type;

import io.tapdata.entity.schema.value.TapDoubleValue;
import io.tapdata.entity.schema.value.TapFloatValue;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class TapFloatingPointTypeTest {
    @Test
    void shouldExposeIndependentSingleAndDoublePrecisionTypes() {
        TapFloat tapFloat = new TapFloat();
        TapDouble tapDouble = new TapDouble();

        assertEquals(TapType.TYPE_NUMBER, tapFloat.getType());
        assertEquals(TapType.TYPE_NUMBER, tapDouble.getType());
        assertEquals("TapFloat", tapFloat.getTypeName());
        assertEquals("TapDouble", tapDouble.getTypeName());
        assertEquals(32, tapFloat.getBit());
        assertEquals(64, tapDouble.getBit());
        assertEquals(4, tapFloat.getStorageBytes());
        assertEquals(8, tapDouble.getStorageBytes());
        assertFalse(tapFloat.getFixed());
        assertFalse(tapDouble.getFixed());
        assertEquals(TapFloatValue.class, tapFloat.tapValueClass());
        assertEquals(TapDoubleValue.class, tapDouble.tapValueClass());
        assertEquals(TapFloat.class, tapFloat.cloneTapType().getClass());
        assertEquals(TapDouble.class, tapDouble.cloneTapType().getClass());
    }

    @Test
    void shouldQuantizeFloatValueAtEveryValueEntryPoint() {
        double source = 1.234567890123d;
        double expected = (double) ((float) source);

        TapFloatValue constructorValue = new TapFloatValue(source);
        TapFloatValue fluentValue = new TapFloatValue().value(source);
        TapFloatValue setterValue = new TapFloatValue();
        setterValue.setValue(source);

        assertEquals(expected, constructorValue.getValue());
        assertEquals(expected, fluentValue.getValue());
        assertEquals(expected, setterValue.getValue());
        assertEquals(source, new TapDoubleValue(source).getValue());
    }
}
