package io.tapdata.entity.conversion.impl;

import io.tapdata.entity.codec.TapCodecsRegistry;
import io.tapdata.entity.codec.filter.TapCodecsFilterManager;
import io.tapdata.entity.mapping.DefaultExpressionMatchingMap;
import io.tapdata.entity.mapping.TypeExprResult;
import io.tapdata.entity.mapping.type.TapMapping;
import io.tapdata.entity.mapping.type.TapFloatMapping;
import io.tapdata.entity.result.TapResult;
import io.tapdata.entity.schema.TapField;
import io.tapdata.entity.schema.type.TapDouble;
import io.tapdata.entity.schema.type.TapFloat;
import io.tapdata.entity.schema.type.TapNumber;
import io.tapdata.entity.schema.type.TapType;
import io.tapdata.entity.utils.DataMap;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;

class TargetTypesGeneratorFloatingPointPriorityTest {
    @Test
    void tapFloatMustPreferTapFloatTargetEvenWhenCustomCodecPointsToNumeric() {
        DefaultExpressionMatchingMap targetMap = targetMap(true, true);
        TapField target = convert(new TapFloat(), targetMap, codecMap(TapFloat.class, "numeric"));

        assertEquals("real", target.getDataType());
        assertInstanceOf(TapFloat.class, resolveTargetType(targetMap, target.getDataType()));
    }

    @Test
    void tapDoubleMustPreferTapDoubleTargetEvenWhenCustomCodecPointsToNumeric() {
        DefaultExpressionMatchingMap targetMap = targetMap(true, true);
        TapField target = convert(new TapDouble(), targetMap, codecMap(TapDouble.class, "numeric"));

        assertEquals("double precision", target.getDataType());
        assertInstanceOf(TapDouble.class, resolveTargetType(targetMap, target.getDataType()));
    }

    @Test
    void tapFloatMayUseTapDoubleOnlyWhenNoTapFloatTargetExists() {
        DefaultExpressionMatchingMap targetMap = targetMap(false, true);
        TapField target = convert(new TapFloat(), targetMap, codecMap(TapFloat.class, "numeric"));

        assertEquals("double precision", target.getDataType());
        assertInstanceOf(TapDouble.class, resolveTargetType(targetMap, target.getDataType()));
    }

    @Test
    void tapDoubleMustSkipTapFloatAndFallBackToTapNumberWhenNoTapDoubleExists() {
        DefaultExpressionMatchingMap targetMap = targetMap(true, false);
        TapField target = convert(new TapDouble(), targetMap, codecMap(TapDouble.class, "numeric"));

        assertEquals("numeric", target.getDataType());
        assertInstanceOf(TapNumber.class, resolveTargetType(targetMap, target.getDataType()));
    }

    @Test
    void floatingPointMayUseTapNumberOnlyWhenNoFloatingPointTargetExists() {
        DefaultExpressionMatchingMap targetMap = targetMap(false, false);
        TapField target = convert(new TapFloat(), targetMap, codecMap(TapFloat.class, "numeric"));

        assertEquals("numeric", target.getDataType());
        assertInstanceOf(TapNumber.class, resolveTargetType(targetMap, target.getDataType()));
    }

    @Test
    void ordinaryTapNumberKeepsTheExistingCustomCodecPath() {
        DefaultExpressionMatchingMap targetMap = targetMap(true, true);
        TapField target = convert(new TapNumber(), targetMap, codecMap(TapNumber.class, "numeric"));

        assertEquals("numeric", target.getDataType());
        assertInstanceOf(TapNumber.class, resolveTargetType(targetMap, target.getDataType()));
    }

    @Test
    void postgresStyleCoefficientMappingStillResolvesTapFloatBeforeTapNumber() {
        DefaultExpressionMatchingMap targetMap = postgresStyleTargetMap();
        TapField target = convert(new TapFloat(), targetMap, codecMap(TapFloat.class, "numeric"));

        assertEquals(false, "numeric".equals(target.getDataType()));
        assertInstanceOf(TapFloat.class, resolveTargetType(targetMap, target.getDataType()));
    }

    @Test
    void sameScoreAndPriorityUsesDatatypeDefinitionOrder() {
        Map<String, DataMap> mappings = new LinkedHashMap<>();
        mappings.put("first_real", mapping("to", "TapFloat", "priority", 1));
        mappings.put("second_real", mapping("to", "TapFloat", "priority", 1));
        mappings.put("numeric", mapping("to", "TapNumber", "precision", Arrays.asList(1, 1000),
                "scale", Arrays.asList(0, 1000), "fixed", true));

        TapField target = convert(new TapFloat(), DefaultExpressionMatchingMap.map(mappings), Map.of());

        assertEquals("first_real", target.getDataType());
    }

    @Test
    void unusableHighScoreCandidateDoesNotHideUsableCandidateInTheSameTier() {
        Map<String, DataMap> mappings = new LinkedHashMap<>();
        mappings.put("invalid_real", mapped("TapFloat", new FailingFloatMapping()));
        mappings.put("valid_real", mapped("TapFloat", new LowerScoreFloatMapping()));
        mappings.put("numeric", mapping("to", "TapNumber", "precision", Arrays.asList(1, 1000),
                "scale", Arrays.asList(0, 1000), "fixed", true));

        TapField target = convert(new TapFloat(), DefaultExpressionMatchingMap.map(mappings), Map.of());

        assertEquals("valid_real", target.getDataType());
    }

    @Test
    void declaredFloatingPointTierDoesNotDowngradeWhenAllCandidatesAreUnusable() {
        Map<String, DataMap> mappings = new LinkedHashMap<>();
        mappings.put("float($precision)", mapping("to", "TapFloat"));
        mappings.put("double precision", mapping("to", "TapDouble"));
        mappings.put("numeric", mapping("to", "TapNumber", "precision", Arrays.asList(1, 1000),
                "scale", Arrays.asList(0, 1000), "fixed", true));

        TapResult<LinkedHashMap<String, TapField>> result = convertResult(new TapFloat(),
                DefaultExpressionMatchingMap.map(mappings), Map.of());

        assertEquals(TapResult.RESULT_FAILED, result.getResult());
        assertNull(result.getData().get("f").getDataType());
    }

    @Test
    void coefficientMappingDeclaresItsTargetTierFromCoefficientRanges() {
        Map<String, Object> coefficient = new LinkedHashMap<>();
        coefficient.put("TapFloat", Arrays.asList(1, 24));

        Map<String, DataMap> mappings = new LinkedHashMap<>();
        mappings.put("float($precision)", mapping("to", "TapCoefficientFloat", "name", "float",
                "coefficient", coefficient, "precision", Arrays.asList(1, 53), "fixed", false));
        mappings.put("numeric", mapping("to", "TapNumber", "precision", Arrays.asList(1, 1000),
                "scale", Arrays.asList(0, 1000), "fixed", true));

        TapField target = convert(new TapDouble(), DefaultExpressionMatchingMap.map(mappings), Map.of());

        assertEquals("numeric", target.getDataType());
    }

    @Test
    void floatingPointMappingUsesDeclaredTargetCapabilities() {
        Map<String, Object> singlePrecision = new LinkedHashMap<>();
        singlePrecision.put("range", Arrays.asList(1, 24));

        Map<String, DataMap> mappings = new LinkedHashMap<>();
        mappings.put("float($precision)", mapping("to", "TapFloat", "mapping", "TapFloatingPoint",
                "binaryPrecision", Arrays.asList(1, 53), "singlePrecision", singlePrecision));
        mappings.put("numeric", mapping("to", "TapNumber", "precision", Arrays.asList(1, 1000),
                "scale", Arrays.asList(0, 1000), "fixed", true));

        TapDouble sourceType = new TapDouble().binaryPrecision(53);
        TapField target = convert(sourceType, DefaultExpressionMatchingMap.map(mappings), Map.of());

        assertEquals("numeric", target.getDataType());
    }

    @Test
    void numberTierRejectsCandidatesThatReparseToFloatingPointTypes() {
        Map<String, DataMap> mappings = new LinkedHashMap<>();
        mappings.put("numeric_float", mapped("TapNumber", new TapFloatMapping()));

        TapResult<LinkedHashMap<String, TapField>> result = convertResult(new TapFloat(),
                DefaultExpressionMatchingMap.map(mappings), Map.of());

        assertEquals(TapResult.RESULT_FAILED, result.getResult());
        assertNull(result.getData().get("f").getDataType());
    }

    private TapField convert(TapType sourceType, DefaultExpressionMatchingMap targetMap,
                             Map<Class<?>, String> codecMap) {
        return convertResult(sourceType, targetMap, codecMap).getData().get("f");
    }

    private TapResult<LinkedHashMap<String, TapField>> convertResult(TapType sourceType,
                                                                       DefaultExpressionMatchingMap targetMap,
                                                                       Map<Class<?>, String> codecMap) {
        LinkedHashMap<String, TapField> sourceFields = new LinkedHashMap<>();
        sourceFields.put("f", new TapField("f", "source").tapType(sourceType));

        return new TargetTypesGeneratorImpl()
                .convert(sourceFields, targetMap,
                        TapCodecsFilterManager.create(TapCodecsRegistry.create().withTapTypeDataTypeMap(codecMap)));
    }

    private DataMap mapped(String to, TapMapping tapMapping) {
        DataMap mapping = mapping("to", to);
        tapMapping.setTo(to);
        mapping.put(TapMapping.FIELD_TYPE_MAPPING, tapMapping);
        return mapping;
    }

    private static class FailingFloatMapping extends TapFloatMapping {
        @Override
        public TapResult<String> fromTapType(String typeExpression, TapType tapType) {
            return TapResult.failed();
        }
    }

    private static class LowerScoreFloatMapping extends TapFloatMapping {
        @Override
        public BigDecimal matchingScore(TapField field) {
            return TapMapping.MAX_SCORE.subtract(BigDecimal.ONE);
        }
    }

    private Map<Class<?>, String> codecMap(Class<?> sourceType, String dataType) {
        Map<Class<?>, String> codecMap = new LinkedHashMap<>();
        codecMap.put(sourceType, dataType);
        return codecMap;
    }

    private TapType resolveTargetType(DefaultExpressionMatchingMap targetMap, String dataType) {
        TypeExprResult<DataMap> result = targetMap.get(dataType);
        TapMapping mapping = (TapMapping) result.getValue().get(TapMapping.FIELD_TYPE_MAPPING);
        return mapping.toTapType(dataType, result.getParams());
    }

    private DefaultExpressionMatchingMap targetMap(boolean includeFloat, boolean includeDouble) {
        Map<String, DataMap> mappings = new LinkedHashMap<>();
        if (includeFloat) {
            mappings.put("real", mapping("to", "TapFloat", "bit", 32, "defaultPrecision", 24, "defaultScale", 8,
                    "fixed", false));
        }
        if (includeDouble) {
            mappings.put("double precision", mapping("to", "TapDouble", "bit", 64, "defaultPrecision", 17,
                    "defaultScale", 15, "priority", 2, "fixed", false));
        }
        mappings.put("numeric", mapping("to", "TapNumber", "precision", Arrays.asList(1, 1000),
                "scale", Arrays.asList(0, 1000), "preferPrecision", 20, "preferScale", 8, "fixed", true));
        return DefaultExpressionMatchingMap.map(mappings);
    }

    private DefaultExpressionMatchingMap postgresStyleTargetMap() {
        Map<String, DataMap> mappings = new LinkedHashMap<>();
        Map<String, Object> coefficient = new LinkedHashMap<>();
        coefficient.put("TapFloat", Arrays.asList(1, 24));
        coefficient.put("TapDouble", Arrays.asList(25, 53));
        mappings.put("numeric[($precision,$scale)]", mapping("to", "TapNumber", "precision", Arrays.asList(1, 1000),
                "scale", Arrays.asList(0, 1000), "preferPrecision", 20, "preferScale", 8, "fixed", true,
                "priority", 1));
        mappings.put("float($precision)", mapping("to", "TapCoefficientFloat", "name", "float",
                "coefficient", coefficient, "precision", Arrays.asList(1, 53), "fixed", false));
        mappings.put("real", mapping("to", "TapFloat", "bit", 32, "defaultPrecision", 24,
                "defaultScale", 8, "fixed", false));
        mappings.put("double precision", mapping("to", "TapDouble", "priority", 2,
                "precision", Arrays.asList(1, 17), "preferPrecision", 11, "preferScale", 4,
                "scale", Arrays.asList(0, 17), "fixed", false));
        return DefaultExpressionMatchingMap.map(mappings);
    }

    private DataMap mapping(Object... values) {
        DataMap mapping = new DataMap();
        for (int i = 0; i < values.length; i += 2) {
            mapping.put((String) values[i], values[i + 1]);
        }
        return mapping;
    }
}
