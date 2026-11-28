package io.tapdata.pdk.core.api.impl.serialize;

import io.tapdata.entity.codec.impl.FromTapNumberCodec;
import io.tapdata.entity.codec.impl.ToTapNumberCodec;
import io.tapdata.entity.codec.TapCodecsRegistry;
import io.tapdata.entity.schema.type.TapNumber;
import io.tapdata.entity.schema.value.TapNumberValue;
import io.tapdata.entity.utils.JsonParser;
import io.tapdata.entity.utils.ObjectSerializable;
import io.tapdata.pdk.core.api.impl.JsonParserImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class TapNumberSerializationTest {
    static Stream<Number> numbers() {
        return Stream.of(new BigDecimal("123456789012345678901234567890.123400"),
                new BigDecimal("1E+6144"), new BigDecimal("1E-6176"),
                new BigInteger("18446744073709551615"), Long.MAX_VALUE, Long.MIN_VALUE,
                9007199254740993L, 12, (short) 3, (byte) 4, 1.25f, 1.25d,
                Float.NaN, Float.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, -0.0d);
    }

    private TapNumberValue value(Number number) {
        TapNumberValue value = TapNumberValue.ofNumber(number);
        value.setOriginValue("original");
        value.setOriginType("decimal128");
        value.setTapType(new TapNumber().precision(34));
        return value;
    }

    private void check(Number number, TapNumberValue restored) {
        assertEquals(number.getClass(), restored.getNumberValue().getClass());
        assertEquals(number, restored.getNumberValue());
        assertEquals("original", restored.getOriginValue());
        assertEquals("decimal128", restored.getOriginType());
        assertEquals(34, restored.getTapType().getPrecision());
        assertEquals(number, new FromTapNumberCodec().fromTapValue(restored));
    }

    @ParameterizedTest @MethodSource("numbers")
    void v2NestedRoundTripAndRepeatedRecovery(Number number) {
        ObjectSerializableImplV2 serializer = new ObjectSerializableImplV2();
        Map<String, Object> event = new LinkedHashMap<>();
        event.put("after", Collections.singletonList(value(number)));
        for (boolean writeNulls : new boolean[]{false, true}) {
            Object restored = event;
            for (int recovery = 0; recovery < 3; recovery++) {
                restored = serializer.toObject(serializer.fromObject(restored,
                        new ObjectSerializable.FromObjectOptions().writeNullValue(writeNulls)));
                check(number, (TapNumberValue) ((List<?>) ((Map<?, ?>) restored).get("after")).get(0));
            }
        }
    }

    @ParameterizedTest @MethodSource("numbers")
    void jsonRoundTrip(Number number) {
        JsonParser parser = new JsonParserImpl();
        String json = parser.toJson(value(number));
        check(number, parser.fromJson(json, TapNumberValue.class));
        String withClass = parser.toJsonWithClass(value(number));
        assertTrue(withClass.contains(TapNumberValue.class.getName()));
        check(number, parser.fromJson(withClass, TapNumberValue.class));
    }

    @Test void defaultTargetRegistryUnwrapsExactNumbers() {
        TapCodecsRegistry source = new TapCodecsRegistry();
        source.registerToTapValue(BigDecimal.class, new ToTapNumberCodec());
        TapCodecsRegistry target = new TapCodecsRegistry();
        BigDecimal number = new BigDecimal("9007199254740993.00100");
        TapNumberValue wrapped = (TapNumberValue) source.getCustomToTapValueCodec(BigDecimal.class).toTapValue(number, null);
        assertSame(number, target.getValueFromDefaultTapValueCodec(wrapped));
    }

    @Test void legacyDoubleWireFormatRemainsIdentical() throws Exception {
        // Captured with the unmodified V2 serializer at a4bfa1d (origin/main).
        byte[] legacy;
        try (java.io.InputStream input = getClass().getResourceAsStream("/compat/tap-number-double-v2.base64")) {
            assertNotNull(input);
            legacy = Base64.getDecoder().decode(new String(input.readAllBytes(), java.nio.charset.StandardCharsets.US_ASCII).trim());
        }
        ObjectSerializableImplV2 serializer = new ObjectSerializableImplV2();
        assertEquals(1.25d, ((TapNumberValue) serializer.toObject(legacy)).getValue());
        assertArrayEquals(legacy, serializer.fromObject(new TapNumberValue(1.25d)));
    }

    public static class LegacyDoubleValue {
        private Double value;
        public Double getValue() { return value; }
        public void setValue(Double value) { this.value = value; }
    }

    @Test void exactJsonFailsInLegacyDoubleReaderInsteadOfRounding() {
        JsonParser parser = new JsonParserImpl();
        String json = parser.toJson(TapNumberValue.ofNumber(new BigDecimal("9007199254740993.001")));
        assertThrows(com.alibaba.fastjson.JSONException.class, () -> parser.fromJson(json, LegacyDoubleValue.class));
        assertEquals(1.25d, parser.fromJson(parser.toJson(new TapNumberValue(1.25d)), LegacyDoubleValue.class).getValue());
    }

    @Test void readsLegacyJsonAndNulls() {
        JsonParser parser = new JsonParserImpl();
        assertEquals(1.5d, parser.fromJson("{\"value\":1.5}", TapNumberValue.class).getValue());
        assertNull(parser.fromJson(parser.toJson(new TapNumberValue()), TapNumberValue.class).getNumberValue());
        ObjectSerializableImplV2 serializer = new ObjectSerializableImplV2();
        assertNull(((TapNumberValue) serializer.toObject(serializer.fromObject(new TapNumberValue()))).getNumberValue());
    }
}
