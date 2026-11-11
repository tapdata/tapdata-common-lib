package io.tapdata.contracts;

import io.tapdata.ErrorCodeEntity;
import io.tapdata.Scanner;
import io.tapdata.entity.error.CoreException;
import io.tapdata.exception.*;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class ExistingErrorContractTest {
    @TapExClass(code = 43, module = "exercise", prefix = "EXERCISE")
    public static class Codes {
        @TapExCode(describe = "Authentication rejected", dynamicDescription = "Rejected {}", recoverable = true, skippable = true)
        public static final String AUTH = "430001";
        @Deprecated
        @TapExCode(describe = "Retained legacy declaration")
        public static final String LEGACY = "20001";
    }
    @TapExClass(code = 37, module = "other", prefix = "EXERCISE")
    public static class Duplicate {
        @TapExCode(describe = "Conflicting definition")
        public static final String AUTH = "430001";
    }
    @TapExClass(code = 43, module = "other-connector", prefix = "other")
    public static class OtherConnector {
        @TapExCode(describe = "Different connector definition")
        public static final String AUTH = "430001";
    }
    static class PdkFailure extends TapPdkBaseException {
        PdkFailure(Throwable cause) { super("EXERCISE430001", "driver", "registered-id", cause); }
    }
    @Test void positionalParametersAndCauseRetainTheirExistingSemantics() {
        Throwable cause = new IllegalStateException("root");
        Object[] parameters = {"host", null, 7};
        TapCodeException failure = new TapCodeException("EXERCISE430001", "message", cause)
                .dynamicDescriptionParameters(parameters);
        assertEquals("EXERCISE430001", failure.getCode());
        assertSame(cause, failure.getCause());
        assertArrayEquals(new String[]{"host", "null", "7"}, failure.getDynamicDescriptionParameters());
        String[] read = failure.getDynamicDescriptionParameters();
        read[0] = "changed";
        assertEquals("host", failure.getDynamicDescriptionParameters()[0]);
        parameters[0] = "mutable-input";
        assertEquals("mutable-input", failure.getDynamicDescriptionParameters()[0]);
        assertEquals(0, new TapCodeException("X").getDynamicDescriptionParameters().length);
    }
    @Test void parameterStringificationFailureIsNotHidden() {
        IllegalArgumentException original = new IllegalArgumentException("formatter");
        Object parameter = new Object() { public String toString() { throw original; } };
        TapCodeException failure = new TapCodeException("X").dynamicDescriptionParameters(parameter);
        assertSame(original, assertThrows(IllegalArgumentException.class, failure::getDynamicDescriptionParameters));
    }
    @Test void pdkContextAndTypedCatchRemainAvailable() {
        Throwable cause = new RuntimeException("driver");
        PdkFailure failure = new PdkFailure(cause);
        failure.withServerErrorCode("28000");
        failure.setTableName("orders");
        try { throw failure; } catch (TapPdkBaseException caught) {
            assertSame(failure, caught);
            assertSame(cause, caught.getCause());
            assertEquals("registered-id", caught.getPdkId());
            assertEquals("28000", caught.getServerErrorCode());
            assertEquals("orders", caught.getTableName());
        }
    }
    @Test void aggregateGetterExposesMutableNullableAndCyclicInputs() {
        CoreException root = new CoreException(12);
        assertNull(root.getMoreExceptions());
        CoreException child = new CoreException(13);
        root.coreException(child).coreException(child).coreException(null).coreException(root);
        assertEquals(Arrays.asList(child, null, root), root.getMoreExceptions());
        assertEquals(12, root.getCode());
        assertNull(root.getCause());
        root.getMoreExceptions().remove(child);
        assertEquals(Arrays.asList(null, root), root.getMoreExceptions());
    }
    @Test void fullCodeUsesPrefixAndEntireFieldWithoutRewritingModuleOrWidth() {
        Map<String, ErrorCodeEntity> codes = collect(Codes.class, OtherConnector.class);
        assertTrue(codes.containsKey("EXERCISE430001"));
        assertTrue(codes.containsKey("EXERCISE20001"));
        assertTrue(codes.containsKey("OTHER430001"));
        assertEquals("430001", codes.get("EXERCISE430001").getCode());
        assertEquals(3, codes.size());
        ErrorCodeEntity auth = codes.get("EXERCISE430001");
        assertEquals("Authentication rejected", auth.getDescribe());
        assertEquals("Rejected {}", auth.getDynamicDescription());
        assertTrue(auth.isRecoverable());
        assertTrue(auth.isSkippable());
        assertFalse(codes.get("EXERCISE20001").isRecoverable());
    }
    @Test void explicitConsumerCanRejectCollisionsBeforeLegacyMapOverwrite() {
        IllegalStateException conflict = assertThrows(IllegalStateException.class,
                () -> collect(Codes.class, Duplicate.class));
        assertTrue(conflict.getMessage().contains("EXERCISE430001"));
        assertEquals(2, collect(Codes.class, Codes.class).size());
    }
    @Test void legacyGlobalLookupStillUsesRawKeysAndPermitsOverwrite() {
        Map<String, ErrorCodeEntity> legacy = Scanner.getErrorCodeMap();
        assertTrue(legacy.containsKey("430001"));
        assertFalse(legacy.containsKey("EXERCISE430001"));
        assertTrue(Arrays.asList(Codes.class, Duplicate.class, OtherConnector.class)
                .contains(legacy.get("430001").getSourceExClass()));
    }
    @Test void explicitCatalogOrderingIsIndependentOfClassEnumeration() {
        assertEquals(collect(Codes.class, OtherConnector.class).keySet().toString(),
                collect(OtherConnector.class, Codes.class).keySet().toString());
    }
    // Test-only feasibility witness, not an exported library API or product adapter.
    static Map<String, ErrorCodeEntity> collect(Class<?>... declarations) {
        Map<String, ErrorCodeEntity> result = new TreeMap<>();
        Set<Class<?>> seen = new HashSet<>();
        for (Class<?> declaration : declarations) {
            if (!seen.add(declaration)) continue;
            Scanner.scanClassExCode(declaration, entity -> {
                String code = entity.fullErrorCode();
                if (result.containsKey(code)) throw new IllegalStateException("Duplicate full code: " + code);
                result.put(code, entity);
            });
        }
        return result;
    }
}
