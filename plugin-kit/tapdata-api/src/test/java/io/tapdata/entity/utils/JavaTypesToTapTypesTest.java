package io.tapdata.entity.utils;

import io.tapdata.entity.schema.type.TapDouble;
import io.tapdata.entity.schema.type.TapFloat;
import io.tapdata.entity.schema.type.TapNumber;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JavaTypesToTapTypesTest {
    @Test
    void shouldMapJavaFloatingPointValuesToDedicatedTapTypes() {
        assertEquals(TapFloat.class, JavaTypesToTapTypes.toTapType(Float.valueOf(1.0f)).getClass());
        assertEquals(TapDouble.class, JavaTypesToTapTypes.toTapType(Double.valueOf(1.0d)).getClass());
        assertEquals(TapNumber.class, JavaTypesToTapTypes.toTapType(new BigDecimal("1.0")).getClass());
        assertEquals(TapNumber.class, JavaTypesToTapTypes.toTapType(Long.valueOf(1L)).getClass());
    }

    @Test
    void shouldMapJavaTypeNamesWithoutLegacyFloatScaleInference() {
        assertEquals(TapFloat.class, JavaTypesToTapTypes.toTapType(JavaTypesToTapTypes.JAVA_Float).getClass());
        assertEquals(TapDouble.class, JavaTypesToTapTypes.toTapType(JavaTypesToTapTypes.JAVA_Double).getClass());
    }

    @Test
    void shouldMapTapFloatingPointClasses() {
        assertEquals(TapFloat.class, JavaTypesToTapTypes.toTapType(TapFloat.class).getClass());
        assertEquals(TapDouble.class, JavaTypesToTapTypes.toTapType(TapDouble.class).getClass());
    }
}
