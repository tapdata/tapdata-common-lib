package io.tapdata.entity.conversion;

import io.tapdata.entity.codec.TapCodecsRegistry;
import io.tapdata.entity.codec.filter.TapCodecsFilterManager;
import io.tapdata.entity.mapping.DefaultExpressionMatchingMap;
import io.tapdata.entity.mapping.type.TapMapping;
import io.tapdata.entity.result.TapResult;
import io.tapdata.entity.schema.TapField;
import io.tapdata.entity.schema.type.TapDouble;
import io.tapdata.entity.schema.type.TapFloat;
import io.tapdata.entity.schema.type.TapNumber;
import io.tapdata.entity.schema.type.TapString;
import io.tapdata.entity.schema.type.TapType;
import io.tapdata.entity.utils.DataMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import static io.tapdata.entity.simplify.TapSimplify.field;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class FloatingPointFallbackTest {
    private TargetTypesGenerator targetTypesGenerator;
    private TableFieldTypesGenerator tableFieldTypesGenerator;
    private TapCodecsFilterManager targetCodecFilterManager;

    @BeforeEach
    void setUp() {
        targetTypesGenerator = new io.tapdata.entity.conversion.impl.TargetTypesGeneratorImpl();
        tableFieldTypesGenerator = new io.tapdata.entity.conversion.impl.TableFieldTypesGeneratorImpl();
        targetCodecFilterManager = TapCodecsFilterManager.create(TapCodecsRegistry.create());
    }

    @Test
    void exactFloatingPointMappingsKeepTheirSemanticTypes() {
        LinkedHashMap<String, TapField> sourceFields = new LinkedHashMap<>();
        sourceFields.put("float_value", field("float_value", "source").tapType(new TapFloat()));
        sourceFields.put("double_value", field("double_value", "source").tapType(new TapDouble()));

        LinkedHashMap<String, TapField> result = convert(sourceFields, targetMap(
                "real", "TapFloat", "double precision", "TapDouble", "numeric", "TapNumber"));

        assertEquals("real", result.get("float_value").getDataType());
        assertInstanceOf(TapFloat.class, result.get("float_value").getTapType());
        assertEquals("double precision", result.get("double_value").getDataType());
        assertInstanceOf(TapDouble.class, result.get("double_value").getTapType());
    }

    @Test
    void floatFallsBackToDoubleBeforeNumber() {
        LinkedHashMap<String, TapField> result = convert(fieldsWithType(new TapFloat()), targetMap(
                "double precision", "TapDouble", "numeric", "TapNumber"));

        assertEquals("double precision", result.get("value").getDataType());
        assertInstanceOf(TapDouble.class, result.get("value").getTapType());
    }

    @Test
    void floatFallsBackToNumberBeforeString() {
        LinkedHashMap<String, TapField> result = convert(fieldsWithType(new TapFloat()), targetMap(
                "numeric", "TapNumber"));

        assertEquals("numeric", result.get("value").getDataType());
        assertInstanceOf(TapNumber.class, result.get("value").getTapType());
    }

    @Test
    void doubleFallsBackToNumberBeforeString() {
        LinkedHashMap<String, TapField> result = convert(fieldsWithType(new TapDouble()), targetMap(
                "numeric", "TapNumber"));

        assertEquals("numeric", result.get("value").getDataType());
        assertInstanceOf(TapNumber.class, result.get("value").getTapType());
    }

    @Test
    void numberFallbackKeepsLegacyTapNumberType() {
        LinkedHashMap<String, TapField> result = convert(fieldsWithType(new TapFloat()), targetMap(
                "numeric", "TapNumber"));

        assertEquals(TapNumber.class, result.get("value").getTapType().getClass());
    }

    @Test
    void floatingPointFallsBackToStringOnlyWhenNoNumericMappingExists() {
        LinkedHashMap<String, TapField> result = convert(fieldsWithType(new TapDouble()), targetMap(
                "text", "TapString"));

        assertEquals("text", result.get("value").getDataType());
        assertInstanceOf(TapString.class, result.get("value").getTapType());
    }

    @Test
    void legacyNumberMappingsDoNotDowngradeFloatingPointToString() {
        LinkedHashMap<String, TapField> sourceFields = new LinkedHashMap<>();
        sourceFields.put("float_value", field("float_value", "source").tapType(new TapFloat()));
        sourceFields.put("double_value", field("double_value", "source").tapType(new TapDouble()));

        LinkedHashMap<String, TapField> result = convert(sourceFields, targetMap(
                "real", "TapNumber", "double precision", "TapNumber", "text", "TapString"));

        assertInstanceOf(TapNumber.class, result.get("float_value").getTapType());
        assertInstanceOf(TapNumber.class, result.get("double_value").getTapType());
        assertEquals(false, "text".equals(result.get("float_value").getDataType()));
        assertEquals(false, "text".equals(result.get("double_value").getDataType()));
    }

    @Test
    void floatFallbackDoesNotSelectIntegerWhenLegacyFloatMappingIsAvailable() {
        LinkedHashMap<String, TapField> result = convert(fieldsWithType(new TapFloat()), legacyFloatTargetMap());

        assertEquals("float", result.get("value").getDataType());
        assertInstanceOf(TapNumber.class, result.get("value").getTapType());
    }

    @Test
    void doubleFallbackDoesNotSelectIntegerWhenLegacyDoubleMappingIsAvailable() {
        LinkedHashMap<String, TapField> result = convert(fieldsWithType(new TapDouble()), legacyDoubleTargetMap());

        assertEquals("double", result.get("value").getDataType());
        assertInstanceOf(TapNumber.class, result.get("value").getTapType());
    }

    @Test
    void upgradedFloatingPointSourceKeepsLegacyRdsInference() {
        Map<String, Object> floatInfo = new DataMap()
                .kv("to", "TapFloat")
                .kv("precision", Arrays.asList(1, 6))
                .kv("scale", Arrays.asList(0, 6))
                .kv("fixed", false);
        Map<String, Object> doubleInfo = new DataMap()
                .kv("to", "TapDouble")
                .kv("precision", Arrays.asList(1, 17))
                .kv("preferPrecision", 11)
                .kv("preferScale", 4)
                .kv("scale", Arrays.asList(0, 17))
                .kv("fixed", false);

        TapField floatField = field("float_value", "source")
                .tapType(TapMapping.build(floatInfo).toTapType("float", java.util.Collections.emptyMap()));
        TapField doubleField = field("double_value", "source")
                .tapType(TapMapping.build(doubleInfo).toTapType("double", java.util.Collections.emptyMap()));
        LinkedHashMap<String, TapField> sourceFields = new LinkedHashMap<>();
        sourceFields.put(floatField.getName(), floatField);
        sourceFields.put(doubleField.getName(), doubleField);

        LinkedHashMap<String, TapField> result = convert(sourceFields, legacyRdsTargetMap());

        assertEquals("float", result.get("float_value").getDataType());
        assertEquals("double", result.get("double_value").getDataType());
    }

    @Test
    void floatingPointSourceWithoutScaleStillPrefersFloatingNumericTarget() {
        LinkedHashMap<String, TapField> result = convert(fieldsWithType(new TapFloat()), legacyRdsTargetMap());
        LinkedHashMap<String, TapField> doubleResult = convert(fieldsWithType(new TapDouble()), legacyRdsTargetMap());

        assertEquals("double", result.get("value").getDataType());
        assertEquals("double", doubleResult.get("value").getDataType());
    }

    private LinkedHashMap<String, TapField> fieldsWithType(TapType tapType) {
        LinkedHashMap<String, TapField> sourceFields = new LinkedHashMap<>();
        sourceFields.put("value", field("value", "source").tapType(tapType));
        return sourceFields;
    }

    private LinkedHashMap<String, TapField> convert(LinkedHashMap<String, TapField> sourceFields, DefaultExpressionMatchingMap targetMap) {
        TapResult<LinkedHashMap<String, TapField>> result = targetTypesGenerator.convert(
                sourceFields,
                targetMap,
                targetCodecFilterManager);
        tableFieldTypesGenerator.autoFill(result.getData(), targetMap);
        return result.getData();
    }

    private DefaultExpressionMatchingMap targetMap(String... types) {
        LinkedHashMap<String, DataMap> expression = new LinkedHashMap<>();
        for (int i = 0; i < types.length; i += 2) {
            DataMap mapping = new DataMap();
            mapping.put("to", types[i + 1]);
            expression.put(types[i], mapping);
        }
        return new DefaultExpressionMatchingMap(expression);
    }

    private DefaultExpressionMatchingMap legacyFloatTargetMap() {
        LinkedHashMap<String, DataMap> expression = new LinkedHashMap<>();
        expression.put("bigint unsigned", numberMapping()
                .kv("bit", 64)
                .kv("precision", 20)
                .kv("value", Arrays.asList("0", "18446744073709551615"))
                .kv("unsigned", "unsigned"));
        expression.put("float", numberMapping()
                .kv("bit", 32)
                .kv("precision", 7)
                .kv("fixed", false));
        return DefaultExpressionMatchingMap.map(expression);
    }

    private DefaultExpressionMatchingMap legacyDoubleTargetMap() {
        LinkedHashMap<String, DataMap> expression = new LinkedHashMap<>();
        expression.put("bigint unsigned", numberMapping()
                .kv("bit", 64)
                .kv("precision", 20)
                .kv("value", Arrays.asList("0", "18446744073709551615"))
                .kv("unsigned", "unsigned"));
        expression.put("double", numberMapping()
                .kv("bit", 64)
                .kv("precision", 15)
                .kv("fixed", false));
        return DefaultExpressionMatchingMap.map(expression);
    }

    private DefaultExpressionMatchingMap legacyRdsTargetMap() {
        LinkedHashMap<String, DataMap> expression = new LinkedHashMap<>();
        expression.put("bigint unsigned", numberMapping()
                .kv("bit", 64)
                .kv("precision", 20)
                .kv("value", Arrays.asList("0", "18446744073709551615"))
                .kv("unsigned", "unsigned"));
        expression.put("float", numberMapping()
                .kv("precision", Arrays.asList(1, 6))
                .kv("scale", Arrays.asList(0, 6))
                .kv("fixed", false));
        expression.put("double", numberMapping()
                .kv("precision", Arrays.asList(1, 17))
                .kv("preferPrecision", 11)
                .kv("preferScale", 4)
                .kv("scale", Arrays.asList(0, 17))
                .kv("fixed", false));
        expression.put("decimal", numberMapping()
                .kv("precision", Arrays.asList(1, 65))
                .kv("scale", Arrays.asList(0, 30))
                .kv("defaultPrecision", 10)
                .kv("defaultScale", 0)
                .kv("fixed", true));
        return DefaultExpressionMatchingMap.map(expression);
    }

    private DataMap numberMapping() {
        return new DataMap().kv("to", "TapNumber");
    }

}
