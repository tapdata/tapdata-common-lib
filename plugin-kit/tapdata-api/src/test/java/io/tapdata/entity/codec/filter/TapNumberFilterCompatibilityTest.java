package io.tapdata.entity.codec.filter;

import io.tapdata.entity.codec.TapCodecsRegistry;
import io.tapdata.entity.codec.impl.ToTapNumberCodec;
import io.tapdata.entity.schema.TapField;
import io.tapdata.entity.schema.TapTable;
import io.tapdata.entity.schema.type.TapNumber;
import io.tapdata.entity.schema.value.TapNumberValue;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.jupiter.api.Assertions.*;

class TapNumberFilterCompatibilityTest {
    @Test void bothFiltersPreserveExactPayloadAndPassItToChecks() {
        for (boolean enforced : new boolean[]{false, true}) {
            for (Number number : new Number[]{5, 9007199254740993L, new BigDecimal("9007199254740993.00100")}) {
                TapCodecsRegistry registry = new TapCodecsRegistry();
                registry.registerToTapValue(number.getClass(), new ToTapNumberCodec());
                TapCodecsFilterManager manager = enforced ? new TapCodecsFilterManagerSchemaEnforced(registry)
                        : TapCodecsFilterManager.create(registry);
                Map<String, TapField> fields = Collections.singletonMap("n", new TapField("n", "NUMBER").tapType(new TapNumber()));
                TapTable table = new TapTable("numbers");
                table.setNameFieldMap(new LinkedHashMap<>(fields));
                Map<String, Object> record = new LinkedHashMap<>();
                record.put("n", number);
                if (enforced) ((TapCodecsFilterManagerSchemaEnforced) manager).transformToTapValueMap(record, table);
                else manager.transformToTapValueMap(record, fields);
                Number expected = number instanceof Integer ? Double.valueOf(number.doubleValue()) : number;
                assertEquals(expected, ((TapNumberValue) record.get("n")).getNumberValue());
                AtomicReference<Object> checked = new AtomicReference<>();
                ToTapValueCheck check = (key, value) -> { checked.set(value); return true; };
                record.put("n", number);
                if (enforced) ((TapCodecsFilterManagerSchemaEnforced) manager).transformToTapValueMap(record, table, check);
                else manager.transformToTapValueMap(record, fields, check);
                assertEquals(expected, checked.get());
            }
        }
    }
    @org.junit.jupiter.api.TestFactory
    java.util.stream.Stream<org.junit.jupiter.api.DynamicTest> bothFiltersRoundDecimalsAndTruncateIntegers() {
        return java.util.stream.Stream.of(false, true).map(enforced -> org.junit.jupiter.api.DynamicTest.dynamicTest(
                "schemaEnforced=" + enforced, () -> bothFiltersRoundDecimalsAndTruncateIntegers(enforced)));
    }

    private void bothFiltersRoundDecimalsAndTruncateIntegers(boolean enforced) {
        TapNumber decimal = new TapNumber().fixed(true).scale(2).precision(10);
        TapNumber integer = new TapNumber().bit(32).scale(0).precision(10)
                .minValue(BigDecimal.valueOf(Integer.MIN_VALUE)).maxValue(BigDecimal.valueOf(Integer.MAX_VALUE));
        for (String text : new String[]{"123.456", "123.455", "-123.455", "123.450"}) {
            for (Object input : new Object[]{text, new BigDecimal(text)}) {
                assertEquals(new BigDecimal(text).setScale(2, java.math.RoundingMode.HALF_UP),
                        throughFilters(enforced, input, decimal));
                assertEquals(new BigDecimal(text).intValue(), throughFilters(enforced, input, integer));
            }
        }
        assertEquals(Integer.MAX_VALUE, throughFilters(enforced, new BigDecimal("2147483647.9"), integer));
        assertEquals(Integer.MIN_VALUE, throughFilters(enforced, new BigDecimal("-2147483648.9"), integer));
        BigDecimal exact = new BigDecimal("9007199254740993.00100");
        assertEquals(exact, throughFilters(enforced, exact, new TapNumber()));
        assertEquals(5d, throughFilters(enforced, 5L, new TapNumber()));
        assertEquals(9007199254740993L, throughFilters(enforced, 9007199254740993L, new TapNumber()));
    }

    @org.junit.jupiter.api.TestFactory
    java.util.stream.Stream<org.junit.jupiter.api.DynamicTest> bothFiltersRejectOverflowAfterConversion() {
        return java.util.stream.Stream.of(false, true).map(enforced -> org.junit.jupiter.api.DynamicTest.dynamicTest(
                "schemaEnforced=" + enforced, () -> bothFiltersRejectOverflowAfterConversion(enforced)));
    }

    private void bothFiltersRejectOverflowAfterConversion(boolean enforced) {
        TapNumber integer = new TapNumber().bit(32).scale(0)
                .minValue(BigDecimal.valueOf(Integer.MIN_VALUE)).maxValue(BigDecimal.valueOf(Integer.MAX_VALUE));
        for (String text : new String[]{"2147483648.1", "-2147483649.1"})
            assertThrows(ArithmeticException.class, () -> throughFilters(enforced, new BigDecimal(text), integer));
        TapNumber precision = new TapNumber().fixed(true).scale(2).precision(5);
        assertEquals(new BigDecimal("999.99"), throughFilters(enforced, "999.994", precision));
        assertThrows(ArithmeticException.class, () -> throughFilters(enforced, "999.995", precision));
        TapNumber bounds = new TapNumber().fixed(true).scale(2).minValue(new BigDecimal("-999.99"))
                .maxValue(new BigDecimal("999.99"));
        for (String text : new String[]{"999.995", "-999.995"})
            assertThrows(ArithmeticException.class, () -> throughFilters(enforced, text, bounds));
        TapNumber unsigned = new TapNumber().unsigned(true).bit(8).scale(0).minValue(BigDecimal.ZERO)
                .maxValue(new BigDecimal("255"));
        assertEquals((short) 0, throughFilters(enforced, "-0.9", unsigned));
        assertThrows(ArithmeticException.class, () -> throughFilters(enforced, "-1.1", unsigned));
        assertThrows(ArithmeticException.class, () -> throughFilters(enforced, "256.1", unsigned));
    }

    @org.junit.jupiter.api.TestFactory
    java.util.stream.Stream<org.junit.jupiter.api.DynamicTest> precisionOnlyTargetsAreChecked() {
        return java.util.stream.Stream.of(false, true).map(enforced -> org.junit.jupiter.api.DynamicTest.dynamicTest(
                "precisionOnly schemaEnforced=" + enforced, () -> {
            for (TapNumber target : new TapNumber[]{new TapNumber().precision(3),
                    new TapNumber().precision(3).minValue(BigDecimal.ZERO)}) {
                for (Object input : new Object[]{"1234", new BigDecimal("1234")})
                    assertThrows(ArithmeticException.class, () -> throughFilters(enforced, input, target));
                assertEquals(new BigDecimal("1.23"), throughFilters(enforced, "1.23", target));
            }
        }));
    }

    @org.junit.jupiter.api.TestFactory
    java.util.stream.Stream<org.junit.jupiter.api.DynamicTest> integerWidthWithoutBoundsPreservesCapacity() {
        return java.util.stream.Stream.of(false, true).map(enforced -> org.junit.jupiter.api.DynamicTest.dynamicTest(
                "integerWidth schemaEnforced=" + enforced, () -> {
            TapNumber target = new TapNumber().fixed(true).bit(64).scale(0);
            assertEquals(123L, throughFilters(enforced, "123.9", target));
            assertEquals(-123L, throughFilters(enforced, new BigDecimal("-123.9"), target));
            for (String text : new String[]{"9223372036854775807", "-9223372036854775808"})
                assertEquals(Long.valueOf(text), throughFilters(enforced, text, target));
            for (String text : new String[]{"9223372036854775808", "-9223372036854775809"})
                assertEquals(new java.math.BigInteger(text), throughFilters(enforced, text, target));
            assertThrows(ArithmeticException.class,
                    () -> throughFilters(enforced, "1000", new TapNumber().scale(0).precision(3)));
            BigDecimal decimal = new BigDecimal("123.900");
            TapNumber unconstrained = (TapNumber) TapNumberValue.ofNumber(decimal).createDefaultTapType();
            assertEquals(decimal, throughFilters(enforced, decimal, unconstrained));
        }));
    }

    @org.junit.jupiter.api.TestFactory
    java.util.stream.Stream<org.junit.jupiter.api.DynamicTest> zeroScaleDecimalsRoundHalfUp() {
        return java.util.stream.Stream.of(false, true).map(enforced -> org.junit.jupiter.api.DynamicTest.dynamicTest(
                "zeroScaleDecimal schemaEnforced=" + enforced, () -> {
            for (TapNumber target : new TapNumber[]{
                    new TapNumber().fixed(true).precision(18).scale(0),
                    new TapNumber().scale(0),
                    new TapNumber().fixed(true).precision(18).scale(0).unsigned(true),
                    new TapNumber().scale(0).minValue(new BigDecimal("-100"))}) {
                for (String text : new String[]{"2.9", "-2.9", "2.5", "-2.5", "2.4", "-2.4"}) {
                    if (Boolean.TRUE.equals(target.getUnsigned()) && text.startsWith("-")) continue;
                    for (Object input : new Object[]{text, new BigDecimal(text)})
                        assertEquals(new BigDecimal(text).setScale(0, java.math.RoundingMode.HALF_UP),
                                throughFilters(enforced, input, target));
                }
            }
            TapNumber decimal = new TapNumber().fixed(true).precision(3).scale(0);
            assertEquals(new BigDecimal("999"), throughFilters(enforced, "999.4", decimal));
            assertThrows(ArithmeticException.class, () -> throughFilters(enforced, "999.5", decimal));
            TapNumber unsigned = new TapNumber().fixed(true).precision(18).scale(0).unsigned(true);
            assertThrows(ArithmeticException.class, () -> throughFilters(enforced, "-0.5", unsigned));
            assertEquals(BigDecimal.ZERO, throughFilters(enforced, "-0.4", unsigned));
            for (String text : new String[]{"9223372036854775808", "-9223372036854775809"})
                assertEquals(new BigDecimal(text), throughFilters(enforced, text, new TapNumber().scale(0)));
        }));
    }

    @org.junit.jupiter.api.TestFactory
    java.util.stream.Stream<org.junit.jupiter.api.DynamicTest> floatingTargetsRoundAndRejectOverflow() {
        return java.util.stream.Stream.of(false, true).map(enforced -> org.junit.jupiter.api.DynamicTest.dynamicTest(
                "floating schemaEnforced=" + enforced, () -> {
            TapNumber floatType = new io.tapdata.entity.schema.type.TapFloat();
            TapNumber doubleType = new io.tapdata.entity.schema.type.TapDouble();
            BigDecimal value = new BigDecimal("1.234567890123456789");
            assertEquals(value.floatValue(), throughFilters(enforced, value, floatType));
            assertEquals(value.doubleValue(), throughFilters(enforced, value, doubleType));
            assertThrows(ArithmeticException.class,
                    () -> throughFilters(enforced, new BigDecimal("1e39"), floatType));
            assertThrows(ArithmeticException.class,
                    () -> throughFilters(enforced, new BigDecimal("1e309"), doubleType));
            assertThrows(ArithmeticException.class,
                    () -> throughFilters(enforced, value, new TapNumber().fixed(false).bit(32).maxValue(BigDecimal.ONE)));
        }));
    }

    @org.junit.jupiter.api.TestFactory
    java.util.stream.Stream<org.junit.jupiter.api.DynamicTest> implicitFixedIntegersAndFloatingExtremes() {
        return java.util.stream.Stream.of(false, true).map(enforced -> org.junit.jupiter.api.DynamicTest.dynamicTest(
                "integerAndFloatBounds schemaEnforced=" + enforced, () -> {
            for (Integer scale : new Integer[]{null, 0}) {
                TapNumber integer = new TapNumber().bit(32).scale(scale)
                        .minValue(BigDecimal.valueOf(Integer.MIN_VALUE)).maxValue(BigDecimal.valueOf(Integer.MAX_VALUE));
                TapNumber bigint = new TapNumber().bit(64).scale(scale)
                        .minValue(BigDecimal.valueOf(Long.MIN_VALUE)).maxValue(BigDecimal.valueOf(Long.MAX_VALUE));
                assertEquals(Integer.MAX_VALUE, throughFilters(enforced, new BigDecimal("2147483647"), integer));
                assertEquals(-2, throughFilters(enforced, "-2.9", integer));
                assertEquals(9007199254740993L, throughFilters(enforced, 9007199254740993L, bigint));
                assertEquals(Long.MAX_VALUE, throughFilters(enforced, new BigDecimal(Long.MAX_VALUE), bigint));
                assertThrows(ArithmeticException.class, () -> throughFilters(enforced, "2147483648", integer));
            }
            TapNumber floatType = new io.tapdata.entity.schema.type.TapFloat();
            TapNumber doubleType = new io.tapdata.entity.schema.type.TapDouble();
            for (String text : new String[]{Float.toString(Float.MAX_VALUE), Float.toString(-Float.MAX_VALUE)})
                assertEquals(Float.valueOf(text), throughFilters(enforced, new BigDecimal(text), floatType));
            for (String text : new String[]{"1.7976931348623158E308", "-1.7976931348623158E308"})
                assertEquals(Double.valueOf(text), throughFilters(enforced, new BigDecimal(text), doubleType));
            assertThrows(ArithmeticException.class, () -> throughFilters(enforced, "3.402824E38", floatType));
            assertThrows(ArithmeticException.class, () -> throughFilters(enforced, "1.7976931348623159E308", doubleType));
            assertThrows(ArithmeticException.class, () -> throughFilters(enforced, "1.2345",
                    new TapNumber().fixed(false).bit(32).maxValue(BigDecimal.ONE)));
        }));
    }

    @org.junit.jupiter.api.TestFactory
    java.util.stream.Stream<org.junit.jupiter.api.DynamicTest> mysqlSpecTargetsThroughBothFilters() {
        return java.util.stream.Stream.of(false, true).map(enforced -> org.junit.jupiter.api.DynamicTest.dynamicTest(
                "mysqlSpec schemaEnforced=" + enforced, () -> {
            Map<String, String> params = new HashMap<>();
            params.put("precision", "18");
            params.put("scale", "0");
            TapNumber decimal = mysqlTarget("decimal[($precision,$scale)][unsigned]", "decimal(18,0)", params);
            assertNotNull(decimal.getMinValue()); // precision-derived bounds must not imply an integer
            assertNull(decimal.getBit());
            for (String text : new String[]{"2.9", "-2.9", "2.5", "-2.5", "123.456"})
                assertEquals(new BigDecimal(text).setScale(0, java.math.RoundingMode.HALF_UP),
                        throughFilters(enforced, new BigDecimal(text), decimal));
            assertThrows(ArithmeticException.class,
                    () -> throughFilters(enforced, "999999999999999999.5", decimal));
            for (String name : new String[]{"float", "double"}) {
                TapNumber floating = mysqlTarget(name, name, Collections.emptyMap());
                assertNull(floating.getBit());
                for (String text : new String[]{"1", "2.9", "-7", "123456"}) {
                    assertEquals(Double.valueOf(text), throughFilters(enforced, new BigDecimal(text), floating));
                    assertEquals(Double.valueOf(text), throughFilters(enforced, Double.valueOf(text), floating));
                }
                assertThrows(ArithmeticException.class, () -> throughFilters(enforced, "1e309", floating));
            }
            // Use the actual explicit-bound DSL entries, including the shortened IEEE limits.
            for (String name : new String[]{"float($precision,$scale)[unsigned]", "double[($precision,$scale)][unsigned]"}) {
                TapNumber floating = mysqlTarget(name, name.startsWith("float") ? "float" : "double", Collections.emptyMap());
                assertEquals(2.9d, throughFilters(enforced, new BigDecimal("2.9"), floating));
                Map<String, String> floatingParams = new HashMap<>();
                floatingParams.put("precision", "6");
                floatingParams.put("scale", "2");
                TapNumber parameterized = mysqlTarget(name, name.startsWith("float") ? "float(6,2)" : "double(6,2)", floatingParams);
                assertEquals(2.912345d, throughFilters(enforced, new BigDecimal("2.912345"), parameterized));
                assertEquals(floating.getMaxValue().doubleValue(),
                        throughFilters(enforced, floating.getMaxValue(), floating));
                assertThrows(ArithmeticException.class,
                        () -> throughFilters(enforced, floating.getMaxValue().multiply(BigDecimal.TEN), floating));
            }
            TapNumber integer = mysqlTarget("int[($zerofill)]", "int", Collections.emptyMap());
            assertEquals(2, throughFilters(enforced, new BigDecimal("2.9"), integer));
            assertEquals(-2, throughFilters(enforced, new BigDecimal("-2.9"), integer));
            TapNumber bigint = mysqlTarget("bigint[($zerofill)]", "bigint", Collections.emptyMap());
            assertEquals(9007199254740993L, throughFilters(enforced, 9007199254740993L, bigint));
        }));
    }

    private TapNumber mysqlTarget(String expression, String dataType, Map<String, String> params) {
        io.tapdata.entity.mapping.type.TapNumberMapping mapping = new io.tapdata.entity.mapping.type.TapNumberMapping();
        mapping.from(MysqlNumberSpecFixture.entry(expression));
        return (TapNumber) mapping.toTapType(dataType, params);
    }

    private Object throughFilters(boolean enforced, Object input, TapNumber targetType) {
        TapCodecsRegistry registry = new TapCodecsRegistry();
        registry.registerToTapValue(input.getClass(), new ToTapNumberCodec());
        // API module tests have no runner ClassFactory; install the same output codec explicitly.
        registry.registerFromTapValue(TapNumberValue.class, new io.tapdata.entity.codec.impl.FromTapNumberCodec());
        TapCodecsFilterManager manager = enforced ? new TapCodecsFilterManagerSchemaEnforced(registry)
                : TapCodecsFilterManager.create(registry);
        Map<String, TapField> source = Collections.singletonMap("n", new TapField("n", "NUMBER").tapType(new TapNumber()));
        Map<String, TapField> target = Collections.singletonMap("n", new TapField("n", "NUMBER").tapType(targetType));
        Map<String, Object> record = new LinkedHashMap<>();
        record.put("n", input);
        Set<String> transformed = null;
        if (enforced) {
            TapTable table = new TapTable("numbers");
            table.setNameFieldMap(new LinkedHashMap<>(source));
            transformed = ((TapCodecsFilterManagerSchemaEnforced) manager).transformToTapValueMap(record, table);
        } else {
            manager.transformToTapValueMap(record, source);
        }
        // Target schema attachment is performed by the engine between the two filters.
        ((TapNumberValue) record.get("n")).setTapType(targetType);
        if (enforced)
            ((TapCodecsFilterManagerSchemaEnforced) manager).transformFromTapValueMap(record, source, target, transformed);
        else
            manager.transformFromTapValueMap(record, source, target);
        return record.get("n");
    }
}
