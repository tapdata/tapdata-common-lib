package io.tapdata.entity.schema.type;

import io.tapdata.entity.codec.impl.FromTapDoubleCodec;
import io.tapdata.entity.codec.impl.FromTapFloatCodec;
import io.tapdata.entity.codec.impl.ToTapDoubleCodec;
import io.tapdata.entity.codec.impl.ToTapFloatCodec;
import io.tapdata.entity.schema.value.TapDoubleValue;
import io.tapdata.entity.schema.value.TapFloatValue;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
    void shouldKeepLegacyTapNumberApiUnchanged() {
        assertThrows(NoSuchMethodException.class, () -> TapNumber.class.getMethod("getFloatingPoint"));
        assertThrows(NoSuchMethodException.class, () -> TapNumber.class.getMethod("getBinaryPrecision"));
        assertThrows(NoSuchMethodException.class, () -> TapNumber.class.getMethod("floatingPoint", Boolean.class));
        assertThrows(NoSuchMethodException.class, () -> TapNumber.class.getMethod("binaryPrecision", Integer.class));
    }

    @Test
    void floatingPointTypesShouldInheritTapNumberProperties() {
        TapType floatType = new TapFloat()
                .bit(32)
                .precision(7)
                .scale(null)
                .fixed(false)
                .minValue(java.math.BigDecimal.valueOf(-Float.MAX_VALUE))
                .maxValue(java.math.BigDecimal.valueOf(Float.MAX_VALUE));
        TapType doubleType = new TapDouble()
                .bit(64)
                .precision(15)
                .scale(null)
                .fixed(false);

        TapNumber floatNumber = assertInstanceOf(TapNumber.class, floatType);
        TapNumber doubleNumber = assertInstanceOf(TapNumber.class, doubleType);
        assertEquals(7, floatNumber.getPrecision());
        assertEquals(15, doubleNumber.getPrecision());
        assertEquals(32, floatNumber.getBit());
        assertEquals(64, doubleNumber.getBit());
        assertEquals(TapFloat.class, floatNumber.cloneTapType().getClass());
        assertEquals(TapDouble.class, doubleNumber.cloneTapType().getClass());
    }

    @Test
    void floatingPointTypesShouldRoundTripWithJavaSerialization() throws Exception {
        TapFloat source = new TapFloat()
                .binaryPrecision(24)
                .precision(7)
                .minValue(java.math.BigDecimal.valueOf(-10))
                .maxValue(java.math.BigDecimal.valueOf(10));

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream output = new ObjectOutputStream(bytes)) {
            output.writeObject(source);
        }

        TapFloat restored;
        try (ObjectInputStream input = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) {
            restored = assertInstanceOf(TapFloat.class, input.readObject());
        }

        assertEquals(32, restored.getBit());
        assertEquals(24, restored.getBinaryPrecision());
        assertEquals(7, restored.getPrecision());
        assertEquals(java.math.BigDecimal.valueOf(-10), restored.getMinValue());
        assertEquals(java.math.BigDecimal.valueOf(10), restored.getMaxValue());
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
