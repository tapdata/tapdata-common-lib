package io.tapdata.pdk.apis.entity.triggercdc;

import io.tapdata.pdk.apis.functions.ConnectorFunctions;
import io.tapdata.pdk.apis.functions.connector.source.StreamReadFunction;
import io.tapdata.pdk.apis.functions.connector.triggercdc.TriggerCdcAcknowledgeFunction;
import io.tapdata.pdk.apis.functions.connector.triggercdc.TriggerCdcCleanupFunction;
import io.tapdata.pdk.apis.functions.connector.triggercdc.TriggerCdcEnsureFunction;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class TriggerCdcContractTest {
    private TriggerCdcTable table(String name) {
        return TriggerCdcTable.create().sourceTableId(name).tableName(name)
                .captureId("capture-" + name).generation(1L);
    }

    private TriggerCdcInstallResult result(TriggerCdcTable... tables) {
        return TriggerCdcInstallResult.create().capabilities(TriggerCdcCapabilities.create())
                .requestedTables(Arrays.asList(tables));
    }

    @Test
    void successFactoryRejectsMissingGeneration() {
        TriggerCdcTable table = table("a").generation(null);
        assertThrows(IllegalArgumentException.class, () -> TriggerCdcTableResult.success(table));
    }

    @Test
    void deserializedSuccessWithoutGenerationIsRejected() {
        TriggerCdcTable table = table("a");
        TriggerCdcTableResult outcome = TriggerCdcTableResult.create().sourceTableId("a")
                .captureId("capture-a").success(true);
        TriggerCdcInstallResult result = result(table).tableResult(outcome);
        assertFalse(outcome.isSuccess());
        assertTrue(result.validate().stream().anyMatch(v -> v.contains("generation")));
        assertThrows(IllegalStateException.class, result::getCaptureIds);
    }

    @Test
    void completeInstallReturnsAllCaptures() {
        TriggerCdcTable a = table("a"), b = table("b");
        TriggerCdcInstallResult result = result(a, b).tableResult(TriggerCdcTableResult.success(a))
                .tableResult(TriggerCdcTableResult.success(b));
        assertTrue(result.isSuccess());
        assertEquals(Arrays.asList("capture-a", "capture-b"), result.getCaptureIds());
    }

    @Test
    void missingRequestedTableCannotShrinkReadSet() {
        TriggerCdcTable a = table("a"), b = table("b");
        TriggerCdcInstallResult result = result(a, b).tableResult(TriggerCdcTableResult.success(a));
        assertFalse(result.isValid());
        assertEquals(Collections.singletonList(b), result.getMissingTables());
        assertThrows(IllegalStateException.class, result::getCaptureIds);
    }

    @Test
    void callerRequestOverridesConnectorsSelfReportedRequest() {
        TriggerCdcTable a = table("a"), b = table("b");
        TriggerCdcInstallResult result = result(a).tableResult(TriggerCdcTableResult.success(a));
        assertTrue(result.isValid());
        assertFalse(result.validate(Arrays.asList(a, b)).isEmpty());
        assertThrows(IllegalStateException.class, () -> result.getCaptureIds(Arrays.asList(a, b)));
    }

    @Test
    void duplicateCaptureAndDuplicateOutcomeAreRejected() {
        TriggerCdcTable a = table("a"), b = table("b").captureId("capture-a");
        assertFalse(result(a, b).tableResult(TriggerCdcTableResult.success(a))
                .tableResult(TriggerCdcTableResult.success(b)).isValid());
        assertFalse(result(a).tableResult(TriggerCdcTableResult.success(a))
                .tableResult(TriggerCdcTableResult.success(a)).isValid());
    }

    @Test
    void unrequestedTableAndUnknownRequestAreRejected() {
        TriggerCdcTable a = table("a"), b = table("b");
        assertFalse(result(a).tableResult(TriggerCdcTableResult.success(a))
                .tableResult(TriggerCdcTableResult.success(b)).isValid());
        assertFalse(result().tableResult(TriggerCdcTableResult.success(a)).isValid());
    }

    @Test
    void partialFailureIsVisibleAndDoesNotReportTaskSuccess() {
        TriggerCdcTable a = table("a"), b = table("b");
        TriggerCdcTableResult failure = TriggerCdcTableResult.failure(b, "unsupported", "type");
        TriggerCdcInstallResult result = result(a, b).tableResult(TriggerCdcTableResult.success(a))
                .tableResult(failure);
        assertTrue(result.isValid());
        assertFalse(result.isSuccess());
        assertEquals(Collections.singletonList(failure), result.getFailedTables());
    }

    @Test
    void capabilitiesMustBeReturnedByEnsure() {
        TriggerCdcTable a = table("a");
        TriggerCdcInstallResult result = result(a).capabilities(null)
                .tableResult(TriggerCdcTableResult.success(a));
        assertFalse(result.isValid());
    }

    @Test
    void forceCleanupRejectsNullEmptyAndBlankConsumerIdsEvenAmongValidIds() {
        TriggerCdcConfig config = TriggerCdcConfig.create().forceCleanUnacknowledged(true);
        for (String invalid : new String[]{null, "", "  "}) {
            TriggerCdcCleanupResult result = TriggerCdcCleanupResult.create().deletedCount(1)
                    .deletedUnacknowledgedCount(1).integrityBrokenConsumer("valid")
                    .integrityBrokenConsumer(invalid);
            assertFalse(result.validate(config).isEmpty());
        }
    }

    @Test
    void forceCleanupRequiresOptInAndNamedConsumers() {
        TriggerCdcCleanupResult result = TriggerCdcCleanupResult.create().deletedCount(1)
                .deletedUnacknowledgedCount(1);
        assertFalse(result.validate(TriggerCdcConfig.create().withDefaults()).isEmpty());
        TriggerCdcConfig enabled = TriggerCdcConfig.create().forceCleanUnacknowledged(true);
        assertFalse(result.validate(enabled).isEmpty());
        result.integrityBrokenConsumer("consumer");
        assertTrue(result.validate(enabled).isEmpty());
    }

    @Test
    void cleanupRejectsImpossibleCounts() {
        assertFalse(TriggerCdcCleanupResult.create().deletedCount(-1).validate(null).isEmpty());
        assertFalse(TriggerCdcCleanupResult.create().remainingCount(-1).validate(null).isEmpty());
        assertFalse(TriggerCdcCleanupResult.create().deletedUnacknowledgedCount(-1)
                .validate(null).isEmpty());
        assertFalse(TriggerCdcCleanupResult.create().deletedCount(0).deletedUnacknowledgedCount(1)
                .integrityBrokenConsumer("a").validate(null).isEmpty());
    }

    @Test
    void acknowledgedOnlyCleanupDoesNotNeedForceOptIn() {
        assertTrue(TriggerCdcCleanupResult.create().deletedCount(3).remainingCount(2)
                .validate(TriggerCdcConfig.create().withDefaults()).isEmpty());
    }

    private boolean advertised(ConnectorFunctions functions) {
        return functions.getCapabilities().stream()
                .anyMatch(c -> TriggerCdcConstants.CAPABILITY_TRIGGER_CDC.equals(c.getId()));
    }

    @Test
    void capabilityRequiresAllThreeFunctionsAndExistingStreamReader() {
        TriggerCdcEnsureFunction ensure = (ctx, config, consumer, tables) -> null;
        TriggerCdcAcknowledgeFunction ack = (ctx, config, consumer, offset) -> {};
        TriggerCdcCleanupFunction cleanup = (ctx, config) -> null;
        StreamReadFunction read = (ctx, tables, offset, size, consumer) -> {};
        ConnectorFunctions functions = new ConnectorFunctions();
        assertFalse(advertised(functions));
        functions.supportTriggerCdcEnsureFunction(ensure).supportTriggerCdcAcknowledgeFunction(ack)
                .supportTriggerCdcCleanupFunction(cleanup);
        assertFalse(advertised(functions));
        functions.supportStreamRead(read);
        assertTrue(advertised(functions));
        functions.supportTriggerCdcCleanupFunction(null);
        assertFalse(advertised(functions));
        functions.supportTriggerCdcCleanupFunction(cleanup).supportTriggerCdcAcknowledgeFunction(null);
        assertFalse(advertised(functions));
        functions.supportTriggerCdcAcknowledgeFunction(ack).supportTriggerCdcEnsureFunction(null);
        assertFalse(advertised(functions));
    }
}
