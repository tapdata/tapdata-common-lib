package io.tapdata.entity.conversion;

import io.tapdata.entity.codec.TapCodecsRegistry;
import io.tapdata.entity.codec.filter.TapCodecsFilterManager;
import io.tapdata.entity.mapping.DefaultExpressionMatchingMap;
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

import java.util.LinkedHashMap;

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
}
