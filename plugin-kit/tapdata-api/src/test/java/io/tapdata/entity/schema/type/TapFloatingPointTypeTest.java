package io.tapdata.entity.schema.type;

import io.tapdata.entity.codec.impl.FromTapDoubleCodec;
import io.tapdata.entity.codec.impl.FromTapFloatCodec;
import io.tapdata.entity.codec.impl.ToTapDoubleCodec;
import io.tapdata.entity.codec.impl.ToTapFloatCodec;
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

    @Test
    void shouldUseDedicatedCodecsForFloatAndDouble() {
        double source = 1.234567890123d;

        TapFloatValue floatValue = new ToTapFloatCodec().toTapValue(source, new TapFloat());
        TapDoubleValue doubleValue = new ToTapDoubleCodec().toTapValue(source, new TapDouble());

        assertEquals((double) ((float) source), floatValue.getValue());
        assertEquals(source, doubleValue.getValue());
        assertEquals(Float.valueOf(floatValue.getValue().floatValue()), new FromTapFloatCodec().fromTapValue(floatValue));
        assertEquals(Double.valueOf(source), new FromTapDoubleCodec().fromTapValue(doubleValue));
    }

    @Test
    void shouldRejectUnsupportedFloatSpecialValues() {
        TapFloat type = new TapFloat().supportsNaN(false).supportsInfinity(false);

        assertEquals(null, new ToTapFloatCodec().toTapValue(Double.NaN, type));
        assertEquals(null, new ToTapFloatCodec().toTapValue(Double.POSITIVE_INFINITY, type));
    }
}
