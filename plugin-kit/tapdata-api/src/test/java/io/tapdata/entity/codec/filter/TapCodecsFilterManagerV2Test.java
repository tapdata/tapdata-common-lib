package io.tapdata.entity.codec.filter;

import io.tapdata.entity.codec.TapCodecsRegistry;
import io.tapdata.entity.schema.TapField;
import io.tapdata.entity.schema.value.DateTime;
import io.tapdata.entity.schema.value.TapDateValue;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class TapCodecsFilterManagerV2Test {
    @Test
    void targetAwareCodecReceivesTheActualTargetField() {
        TapCodecsRegistry registry = TapCodecsRegistry.create();
        registry.registerFromTapValueV2(TapDateValue.class,
                (value, targetField) -> targetField.getDataType() + ":" + targetField.getScale());
        TapCodecsFilterManager manager = TapCodecsFilterManager.create(registry);

        Map<String, Object> values = dateValue();
        Map<String, TapField> targetFields = fields(new TapField("birth_date", "DATE").scale(0));

        manager.transformFromTapValueMap(values, null, targetFields);

        assertEquals("DATE:0", values.get("birth_date"));
    }

    @Test
    void legacyCodecRemainsTheFallbackWhenNoTargetAwareCodecIsRegistered() {
        TapCodecsRegistry registry = TapCodecsRegistry.create();
        registry.registerFromTapValue(TapDateValue.class, value -> "legacy");
        TapCodecsFilterManager manager = TapCodecsFilterManager.create(registry);

        Map<String, Object> values = dateValue();
        Map<String, TapField> targetFields = fields(new TapField("birth_date", "DATE"));

        manager.transformFromTapValueMap(values, null, targetFields);

        assertEquals("legacy", values.get("birth_date"));
    }

    @Test
    void unregisterFromTapValueRemovesBothCodecVersions() {
        TapCodecsRegistry registry = TapCodecsRegistry.create();
        registry.registerFromTapValueV2(TapDateValue.class, (value, targetField) -> "v2");

        registry.unregisterFromTapValue(TapDateValue.class);

        assertNull(registry.getCustomFromTapValueCodecV2(TapDateValue.class));
    }

    @Test
    void schemaEnforcedPathPassesTheTargetField() {
        TapCodecsRegistry registry = TapCodecsRegistry.create();
        registry.registerFromTapValueV2(TapDateValue.class,
                (value, targetField) -> targetField.getDataType());
        TapCodecsFilterManagerSchemaEnforced manager = new TapCodecsFilterManagerSchemaEnforced(registry);

        Map<String, Object> values = dateValue();
        Map<String, TapField> targetFields = fields(new TapField("birth_date", "DATE"));

        manager.transformFromTapValueMap(values, null, targetFields,
                Collections.singleton("birth_date"));

        assertEquals("DATE", values.get("birth_date"));
    }

    private static Map<String, Object> dateValue() {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("birth_date", new TapDateValue(new DateTime(LocalDateTime.of(2026, 10, 21, 0, 0))));
        return values;
    }

    private static Map<String, TapField> fields(TapField field) {
        return Collections.singletonMap(field.getName(), field);
    }
}
