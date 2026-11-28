package io.tapdata.entity.codec.filter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.tapdata.entity.mapping.type.TapMapping;
import io.tapdata.entity.schema.type.TapFloat;
import io.tapdata.entity.schema.type.TapDouble;
import io.tapdata.entity.schema.type.TapNumber;
import io.tapdata.entity.schema.value.TapNumberValue;
import org.junit.jupiter.api.*;
import java.io.InputStream;
import java.math.*;
import java.util.*;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.*;

/** All numeric entries from community/enterprise specs, rather than selected small samples. */
class NumberSpecMatrixTest {
    private static final String[] VALUES = {"0", "2.9", "-2.9", "1e7", "-1e10", "1e12",
            "12345678901234567890.123456789", "1234567890123456789012345678901234", "1e39", "1e309"};

    @TestFactory Stream<DynamicTest> allNumericSpecsThroughBothFilters() throws Exception {
        List<JSONObject> specs;
        try (InputStream input = getClass().getResourceAsStream("/compat/number-specs.json")) {
            specs = JSON.parseArray(new String(com.google.common.io.ByteStreams.toByteArray(input), java.nio.charset.StandardCharsets.UTF_8), JSONObject.class);
        }
        assertEquals(771, specs.size(), "Regenerate the inventory when connector specs change");
        return specs.stream().map(spec -> DynamicTest.dynamicTest(spec.getString("repo") + "/"
                + spec.getString("path") + ":" + spec.getString("expression"), () -> checkSpec(spec)));
    }

    private void checkSpec(JSONObject spec) {
        checkSpec(spec, Collections.emptyMap());
        if ("TapCoefficientFloat".equals(spec.getJSONObject("entry").getString("to"))) {
            JSONObject ranges = spec.getJSONObject("entry").getJSONObject("coefficient");
            for (String name : ranges.keySet()) {
                for (Object endpoint : ranges.getJSONArray(name))
                    checkSpec(spec, Collections.singletonMap("precision", endpoint.toString()));
            }
        }
    }

    private void checkSpec(JSONObject spec, Map<String, String> params) {
        TapMapping mapping = TapMapping.build(spec.getJSONObject("entry"));
        assertNotNull(mapping);
        TapNumber target = (TapNumber) mapping.toTapType(spec.getString("expression"), params);
        boolean binary = spec.getBooleanValue("binary");
        assertEquals(spec.getJSONObject("entry").getBoolean("binaryFloat"), target.getBinaryFloat(), spec.toJSONString());
        TapNumber clone = (TapNumber) target.cloneTapType();
        assertEquals(target.getBinaryFloat(), clone.getBinaryFloat());
        assertEquals(target.getFixed(), clone.getFixed());
        for (boolean enforced : new boolean[]{false, true}) {
            for (String text : VALUES) {
                BigDecimal input = new BigDecimal(text);
                if (binary) {
                    boolean single = target instanceof TapFloat || !(target instanceof TapDouble) && Integer.valueOf(32).equals(target.getBit());
                    boolean finite = single ? Float.isFinite(input.floatValue()) : Double.isFinite(input.doubleValue());
                    if (!finite || (Boolean.TRUE.equals(target.getUnsigned()) && input.signum() < 0)) {
                        assertThrows(ArithmeticException.class,
                                () -> TapNumberFilterCompatibilityTest.throughFilters(enforced, input, target), text);
                    } else {
                        Object expected = single ? (Object) input.floatValue() : input.doubleValue();
                        assertEquals(expected, TapNumberFilterCompatibilityTest.throughFilters(enforced, input, target), text);
                    }
                } else {
                    // A decimal target must never silently narrow an exact payload to binary floating point.
                    // Out-of-capacity values may fail; legal values must keep their decimal value after rounding.
                    BigDecimal rounded = input;
                    Integer scale = target.getScale();
                    boolean integer = target.getBit() != null && !Boolean.FALSE.equals(target.getFixed());
                    if (scale == null && integer) scale = 0;
                    if (scale != null) rounded = input.setScale(scale,
                            integer && scale == 0 ? RoundingMode.DOWN : RoundingMode.HALF_UP);
                    boolean exceeds = Boolean.TRUE.equals(target.getUnsigned()) && rounded.signum() < 0
                            || target.getMinValue() != null && rounded.compareTo(target.getMinValue()) < 0
                            || target.getMaxValue() != null && rounded.compareTo(target.getMaxValue()) > 0
                            || target.getPrecision() != null && rounded.precision() > target.getPrecision();
                    if (exceeds) assertThrows(ArithmeticException.class,
                            () -> TapNumberFilterCompatibilityTest.throughFilters(enforced, input, target), text);
                    else {
                        Object result = TapNumberFilterCompatibilityTest.throughFilters(enforced, input, target);
                        assertFalse(result instanceof Double || result instanceof Float, spec.getString("path") + ":" + spec.getString("expression") + " input=" + text);
                        assertEquals(0, rounded.compareTo(new BigDecimal(result.toString())), text);
                    }
                }
                Double legacy = input.doubleValue();
                TapNumberValue wrapped = new TapNumberValue(legacy);
                wrapped.setTapType(target);
                Object expected;
                try {
                    expected = new LegacyNumberCodec().fromTapValue(wrapped);
                } catch (RuntimeException historicalFailure) {
                    assertThrows(historicalFailure.getClass(),
                            () -> TapNumberFilterCompatibilityTest.throughFilters(enforced, legacy, target),
                            "legacy failure " + text);
                    continue;
                }
                Object actual = TapNumberFilterCompatibilityTest.throughFilters(enforced, legacy, target);
                assertEquals(expected.getClass(), actual.getClass(), "legacy type " + text);
                assertEquals(expected, actual, "legacy value " + text);
            }
        }
    }

    @Test void realDecimalSpecsRetainAllSignificantDigits() throws Exception {
        List<JSONObject> specs;
        try (InputStream input = getClass().getResourceAsStream("/compat/number-specs.json")) {
            specs = JSON.parseArray(new String(com.google.common.io.ByteStreams.toByteArray(input),
                    java.nio.charset.StandardCharsets.UTF_8), JSONObject.class);
        }
        int checked = 0;
        for (JSONObject spec : specs) {
            String path = spec.getString("path");
            String expression = spec.getString("expression").toUpperCase(Locale.ROOT);
            Map<String, String> params = new HashMap<>();
            String text;
            if (path.contains("bigquery") && (expression.startsWith("NUMERIC") || expression.startsWith("BIGNUMERIC"))) {
                text = expression.startsWith("BIGNUMERIC")
                        ? "123456789012345678.12345678901234567890" : "12345678901234567890.123456789";
                params.put("precision", "38");
                params.put("scale", expression.startsWith("BIGNUMERIC") ? "20" : "9");
            } else if (expression.equals("DECFLOAT")) {
                // Snowflake's existing query-only spec declares only precision=11, scale=4;
                // preserve its constraints rather than changing the connector's schema here.
                text = path.contains("snowflake") ? "1234567.8912"
                        : "1234567890123456789012345678901234";
            } else if (path.contains("oracle") && expression.startsWith("FLOAT")) {
                text = "12345678901234567890.123456";
            } else continue;
            TapNumber target = (TapNumber) TapMapping.build(spec.getJSONObject("entry"))
                    .toTapType(spec.getString("expression"), params);
            assertFalse(Boolean.TRUE.equals(target.getBinaryFloat()));
            BigDecimal exact = new BigDecimal(text);
            for (boolean enforced : new boolean[]{false, true}) {
                Object result = TapNumberFilterCompatibilityTest.throughFilters(enforced, exact, target);
                assertInstanceOf(BigDecimal.class, result, path + ":" + expression);
                assertEquals(0, exact.compareTo((BigDecimal) result), path + ":" + expression);
            }
            checked++;
        }
        assertEquals(12, checked);
    }

    @Test void missingDeclarationKeepsDecimalsAndBigQueryRejectsOverflow() {
        for (boolean enforced : new boolean[]{false, true}) {
            BigDecimal exact = new BigDecimal("12345678901234567890.123456789");
            assertEquals(exact, TapNumberFilterCompatibilityTest.throughFilters(enforced, exact,
                    new TapNumber().fixed(false)));
            TapNumber numeric = new TapNumber().fixed(false).precision(38).scale(9);
            assertEquals(exact, TapNumberFilterCompatibilityTest.throughFilters(enforced, exact, numeric));
            assertThrows(ArithmeticException.class, () -> TapNumberFilterCompatibilityTest.throughFilters(
                    enforced, new BigDecimal("1e40"), numeric));
            assertThrows(ArithmeticException.class, () -> TapNumberFilterCompatibilityTest.throughFilters(
                    enforced, new BigDecimal("-1e-1000"), new TapNumber().binaryFloat(true).unsigned(true)));
        }
    }
}
