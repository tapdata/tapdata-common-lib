package io.tapdata.entity.codec.filter;

import io.tapdata.entity.codec.TapCodecsRegistry;
import io.tapdata.entity.schema.TapField;
import io.tapdata.entity.schema.value.TapDecimalValue;
import io.tapdata.entity.schema.value.TapNumberValue;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TapDecimalValueCodecTest {
    private static final BigDecimal EXACT = new BigDecimal("9007199254740993.123456789012345678");

    @Test
    void freshRegistryUnwrapsWithoutPrecisionLoss() {
        Map<String, Object> event = new LinkedHashMap<>();
        event.put("amount", new TapDecimalValue(EXACT));
        TapCodecsFilterManager.create(TapCodecsRegistry.create()).transformFromTapValueMap(event);
        assertEquals(EXACT, event.get("amount"));
        assertInstanceOf(BigDecimal.class, event.get("amount"));
    }

    @Test
    void targetCodecStillReceivesFieldMetadata() {
        TapCodecsRegistry registry = TapCodecsRegistry.create();
        registry.registerFromTapValueV2(TapDecimalValue.class,
                (value, field) -> value.getValue().setScale(field.getScale()));
        Map<String, Object> event = new LinkedHashMap<>();
        event.put("amount", new TapDecimalValue(new BigDecimal("9007199254740993")));
        TapCodecsFilterManager.create(registry).transformFromTapValueMap(event, null,
                Collections.singletonMap("amount", new TapField("amount", "DECIMAL(30,2)").scale(2)));
        assertEquals(new BigDecimal("9007199254740993.00"), event.get("amount"));
    }

    @Test
    void decimalNullAndNegativeScaleArePreservedWithoutChangingLegacyNumbers() {
        TapCodecsRegistry registry = TapCodecsRegistry.create();
        assertNull(registry.getFromTapValueCodec(TapDecimalValue.class).fromTapValue(new TapDecimalValue()));
        BigDecimal exponent = new BigDecimal("1.234E+100");
        TapDecimalValue value = new TapDecimalValue(exponent);
        assertEquals(exponent, registry.getFromTapValueCodec(TapDecimalValue.class).fromTapValue(value));
        assertNotNull(value.createDefaultTapType());
        assertNotNull(new TapDecimalValue().createDefaultTapType());
        assertEquals(1.25d, registry.getFromTapValueCodec(TapNumberValue.class).fromTapValue(new TapNumberValue(1.25d)));
    }
}
