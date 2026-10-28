package io.tapdata.entity.codec.filter;

import io.tapdata.entity.codec.TapCodecsRegistry;
import io.tapdata.entity.codec.detector.TapSkipper;
import io.tapdata.entity.schema.TapField;
import io.tapdata.entity.schema.value.DateTime;
import io.tapdata.entity.schema.value.TapArrayValue;
import io.tapdata.entity.schema.value.TapDateValue;
import io.tapdata.entity.schema.value.TapMapValue;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
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

    @Test
    void legacyNestedMapConversionDoesNotApplyTopLevelSkipperToInnerFields() {
        TapCodecsRegistry registry = TapCodecsRegistry.create();
        registry.registerFromTapValue(TapMapValue.class, TapMapValue::getValue);
        registry.registerFromTapValue(TapDateValue.class, value -> "legacy");
        TapCodecsFilterManager manager = TapCodecsFilterManager.create(registry);
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("payload", new TapMapValue(dateValue()));
        TapSkipper skipUnknownFields = new TapSkipper() {
            @Override
            public boolean skip(TapField field) {
                return field == null;
            }
        };

        manager.transformFromTapValueMap(values,
                fields(new TapField("payload", "MAP")), skipUnknownFields);

        assertEquals("legacy", ((Map<?, ?>) values.get("payload")).get("birth_date"));
    }

    @Test
    void customMapCodecUsesNestedFieldInsteadOfSameNamedTopLevelField() {
        TapCodecsRegistry registry = TapCodecsRegistry.create();
        registry.registerFromTapValue(TapMapValue.class, TapMapValue::getValue);
        registry.registerFromTapValueV2(TapDateValue.class,
                (value, targetField) -> targetField.getName() + ":" + targetField.getDataType());
        TapCodecsFilterManager manager = TapCodecsFilterManager.create(registry);
        Map<String, TapField> targetFields = new LinkedHashMap<>();
        targetFields.put("profile", new TapField("profile", "MAP"));
        targetFields.put("profile.created", new TapField("profile.created", "TIMESTAMP"));
        targetFields.put("created", new TapField("created", "DATE"));
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("profile", new TapMapValue(createdValue()));

        manager.transformFromTapValueMap(values, null, targetFields);

        assertEquals("profile.created:TIMESTAMP", ((Map<?, ?>) values.get("profile")).get("created"));
    }

    @Test
    void customMapCodecInsideArrayUsesNestedField() {
        TapCodecsRegistry registry = TapCodecsRegistry.create();
        registry.registerFromTapValue(TapArrayValue.class, TapArrayValue::getValue);
        registry.registerFromTapValue(TapMapValue.class, TapMapValue::getValue);
        registry.registerFromTapValueV2(TapDateValue.class,
                (value, targetField) -> targetField.getName() + ":" + targetField.getDataType());
        TapCodecsFilterManager manager = TapCodecsFilterManager.create(registry);
        Map<String, TapField> targetFields = new LinkedHashMap<>();
        targetFields.put("items", new TapField("items", "ARRAY"));
        targetFields.put("items.created", new TapField("items.created", "DATE"));
        targetFields.put("created", new TapField("created", "CHAR"));
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("items", new TapArrayValue(Arrays.<Object>asList(new TapMapValue(createdValue()))));

        manager.transformFromTapValueMap(values, null, targetFields);

        List<?> items = (List<?>) values.get("items");
        assertEquals("items.created:DATE", ((Map<?, ?>) items.get(0)).get("created"));
    }

    @Test
    void nestedArrayIndexesSelectLeafTargetField() {
        TapCodecsRegistry registry = TapCodecsRegistry.create();
        registry.registerFromTapValue(TapArrayValue.class, TapArrayValue::getValue);
        registry.registerFromTapValue(TapMapValue.class, TapMapValue::getValue);
        registry.registerFromTapValueV2(TapDateValue.class,
                (value, targetField) -> targetField.getName() + ":" + targetField.getDataType());
        TapCodecsFilterManager manager = TapCodecsFilterManager.create(registry);
        Map<String, TapField> targetFields = new LinkedHashMap<>();
        targetFields.put("items", new TapField("items", "ARRAY"));
        targetFields.put("items.created", new TapField("items.created", "DATE"));
        assertEquals(targetFields.get("items.created"),
                manager.targetField(targetFields, "items.#0.created"));
        assertEquals(targetFields.get("items.created"),
                manager.targetField(targetFields, "items.#0.#0.created"));
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("items", new TapArrayValue(Arrays.<Object>asList(
                new TapArrayValue(Arrays.<Object>asList(new TapMapValue(createdValue()))))));

        manager.transformFromTapValueMap(values, null, targetFields);

        List<?> outerItems = (List<?>) values.get("items");
        List<?> innerItems = (List<?>) outerItems.get(0);
        assertEquals("items.created:DATE", ((Map<?, ?>) innerItems.get(0)).get("created"));
    }

    private static Map<String, Object> dateValue() {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("birth_date", new TapDateValue(new DateTime(LocalDateTime.of(2026, 10, 21, 0, 0))));
        return values;
    }

    private static Map<String, Object> createdValue() {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("created", new TapDateValue(new DateTime(LocalDateTime.of(2026, 10, 21, 0, 0))));
        return values;
    }

    private static Map<String, TapField> fields(TapField field) {
        return Collections.singletonMap(field.getName(), field);
    }
}
