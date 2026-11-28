package io.tapdata.entity.codec.impl;

import io.tapdata.entity.schema.type.TapNumber;
import io.tapdata.entity.schema.value.TapNumberValue;
import io.tapdata.entity.schema.value.TapValue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.io.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class TapNumberCompatibilityTest {
    private final ToTapNumberCodec to = new ToTapNumberCodec();
    private final FromTapNumberCodec from = new FromTapNumberCodec();

    static Stream<Number> numbers() {
        return Stream.of(new BigDecimal("123456789012345678901234567890.123400"),
                new BigDecimal("1E+6144"), new BigDecimal("1E-6176"),
                new BigInteger("18446744073709551615"), Long.MAX_VALUE, Long.MIN_VALUE,
                9007199254740993L, 12, (short) 3, (byte) 4, 1.25f, 1.25d,
                Float.NaN, Float.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, -0.0d);
    }

    @TestFactory Stream<DynamicTest> preservesRepresentationWithoutTarget() {
        return numbers().map(number -> DynamicTest.dynamicTest(number.getClass().getSimpleName() + ": " + number,
                () -> preservesRepresentationWithoutTarget(number)));
    }

    void preservesRepresentationWithoutTarget(Number number) {
        TapNumberValue value = TapNumberValue.ofNumber(number);
        assertSame(number, value.getNumberValue());
        assertSame(number, from.fromTapValue(value));
        value.setTapType(new TapNumber());
        assertSame(number, from.fromTapValue(value));
    }

    @Test void legacyWritesClearExactPayloadThroughBaseApi() {
        TapNumberValue value = TapNumberValue.ofNumber(BigDecimal.TEN);
        assertEquals(10d, value.getValue());
        TapValue<Double, TapNumber> base = value;
        base.setValue(2d);
        assertEquals(2d, value.getValue());
        assertEquals(2d, value.getNumberValue());
        value.setNumberValue(BigInteger.TEN);
        assertSame(value, base.value(3d));
        assertEquals(3d, value.getValue());
        value.setNumberValue(BigDecimal.ONE);
        base.value(null);
        assertNull(value.getNumberValue());
        value.setNumberValue(BigDecimal.ONE);
        value.setNumberValue(null);
        assertNull(value.getValue());
        assertEquals(4d, new TapNumberValue(4d).getValue());
    }

    @Test void parsesDecimalTextWithoutDoubleAndKeepsSpecialValues() {
        assertEquals(new BigDecimal("9007199254740993.00"), to.toTapValue("9007199254740993.00", null).getNumberValue());
        assertEquals(Double.POSITIVE_INFINITY, to.toTapValue("Infinity", null).getValue());
        assertNull(to.toTapValue("invalid", null));
        assertEquals(1d, to.toTapValue(true, null).getValue());
        assertNull(from.fromTapValue(null));
        assertNull(from.fromTapValue(new TapNumberValue()));
    }

    private TapNumberValue constrained(Number value, TapNumber type) {
        TapNumberValue result = TapNumberValue.ofNumber(value);
        result.setTapType(type);
        return result;
    }

    @Test void checksIntegerFractionAndBoundsInsteadOfTruncating() {
        TapNumber byteType = new TapNumber().scale(0).minValue(BigDecimal.valueOf(-128)).maxValue(BigDecimal.valueOf(127));
        assertEquals((byte) 127, from.fromTapValue(constrained(new BigDecimal("127.0"), byteType)));
        assertThrows(ArithmeticException.class, () -> from.fromTapValue(constrained(128L, byteType)));
        assertThrows(ArithmeticException.class, () -> from.fromTapValue(constrained(new BigDecimal("1.1"), byteType)));
        TapNumber longType = new TapNumber().scale(0).minValue(BigDecimal.valueOf(Long.MIN_VALUE)).maxValue(BigDecimal.valueOf(Long.MAX_VALUE));
        assertEquals(Long.MAX_VALUE, from.fromTapValue(constrained(Long.MAX_VALUE, longType)));
        assertThrows(ArithmeticException.class, () -> from.fromTapValue(constrained(BigInteger.valueOf(Long.MAX_VALUE).add(BigInteger.ONE), longType)));
        TapNumber unsigned = new TapNumber().scale(0).minValue(BigDecimal.ZERO).maxValue(new BigDecimal("18446744073709551615"));
        assertEquals(new BigDecimal("18446744073709551615"), from.fromTapValue(constrained(new BigInteger("18446744073709551615"), unsigned)));
    }

    @Test void decimalUsesExplicitScaleAndPrecisionWithoutRounding() {
        TapNumber type = new TapNumber().fixed(true).scale(2).precision(5);
        assertEquals(new BigDecimal("123.40"), from.fromTapValue(constrained(new BigDecimal("123.4"), type)));
        assertThrows(ArithmeticException.class, () -> from.fromTapValue(constrained(new BigDecimal("1.234"), type)));
        assertThrows(ArithmeticException.class, () -> from.fromTapValue(constrained(new BigDecimal("1234.56"), type)));
        assertEquals(new BigDecimal("12.3"), from.fromTapValue(constrained(new BigDecimal("12.3"), new TapNumber().minValue(BigDecimal.ZERO))));
    }

    @Test void floatingConversionRequiresExplicitTargetAndChecksOverflow() {
        TapNumber type = new TapNumber().fixed(false).bit(32);
        assertEquals(1.5f, from.fromTapValue(constrained(new BigDecimal("1.5"), type)));
        assertEquals(Double.POSITIVE_INFINITY, from.fromTapValue(constrained(Double.POSITIVE_INFINITY, type)));
        assertThrows(ArithmeticException.class, () -> from.fromTapValue(constrained(new BigDecimal("1E100"), type)));
        assertEquals(Double.NaN, from.fromTapValue(constrained(Double.NaN, new TapNumber().scale(0))));
        assertThrows(ArithmeticException.class, () -> from.fromTapValue(constrained(new BigDecimal("1E6144"), new TapNumber().fixed(false).bit(64))));
    }

    @TestFactory Stream<DynamicTest> javaSerializationRetainsHistoricalLimitations() {
        return numbers().map(number -> DynamicTest.dynamicTest(number.getClass().getSimpleName() + ": " + number,
                () -> javaSerializationRetainsHistoricalLimitations(number)));
    }

    void javaSerializationRetainsHistoricalLimitations(Number number) throws Exception {
        TapNumberValue value = TapNumberValue.ofNumber(number);
        value.setOriginValue("original");
        value.setOriginType("number");
        value.setTapType(new TapNumber().precision(34));
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(bytes)) { out.writeObject(value); }
        TapNumberValue restored;
        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) {
            restored = (TapNumberValue) in.readObject();
        }
        // Native Java streams historically omit all TapValue superclass fields.
        if (number instanceof Double) assertNull(restored.getNumberValue());
        else assertEquals(number, restored.getNumberValue());
        assertNull(restored.getOriginValue());
        assertNull(restored.getOriginType());
        assertNull(restored.getTapType());
    }
    @Test void legacyDoubleTargetConversionRemainsCompatible() {
        assertEquals(1.23456d, from.fromTapValue(constrained(1.23456d, new TapNumber().scale(2).precision(10))));
        assertEquals(12345.6d, from.fromTapValue(constrained(12345.6d, new TapNumber().scale(2).precision(5))));
        assertEquals(Double.NaN, from.fromTapValue(constrained(Double.NaN, new TapNumber().scale(2))));
        assertEquals(-1d, from.fromTapValue(constrained(-1d, new TapNumber().unsigned(true).scale(2))));
        assertEquals(new BigDecimal("2.5"), from.fromTapValue(constrained(2.5d, (TapNumber) new TapNumberValue().createDefaultTapType())));
        assertEquals((byte) 2, from.fromTapValue(constrained(2.9d, new TapNumber().scale(0).minValue(new BigDecimal(-128)).maxValue(new BigDecimal(127)))));
    }

    @Test void decimalTrailingZerosNeedNoRounding() {
        assertEquals(new BigDecimal("1.23"), from.fromTapValue(constrained(new BigDecimal("1.230"), new TapNumber().fixed(true).scale(2))));
        assertThrows(ArithmeticException.class, () -> from.fromTapValue(constrained(new BigDecimal("1.231"), new TapNumber().fixed(true).scale(2))));
    }

    @Test void inputCodecKeepsLegacyTypesExceptNumbersRequiringPrecision() {
        for (Number n : new Number[]{12, (byte) 1, (short) 3, 5L, 1.25f, 1.25d, new java.util.concurrent.atomic.AtomicLong(42)})
            assertEquals(n.doubleValue(), to.toTapValue(n, null).getNumberValue());
        for (Number n : new Number[]{9007199254740993L, Long.MAX_VALUE, Long.MIN_VALUE, BigDecimal.TEN, BigInteger.TEN})
            assertSame(n, to.toTapValue(n, null).getNumberValue());
        assertThrows(IllegalArgumentException.class, () -> TapNumberValue.ofNumber(new java.util.concurrent.atomic.AtomicLong(42)));
    }

}
