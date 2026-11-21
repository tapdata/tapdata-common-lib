package io.tapdata.pdk.apis.entity.triggercdc;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Outcome of {@link io.tapdata.pdk.apis.functions.connector.triggercdc.TriggerCdcEnsureFunction}.
 *
 * <p>{@code ensure} must be idempotent: running it twice yields the same capture ids and the
 * same generation. Only a real reinstall increments the generation.</p>
 *
 * <p>Fail closed: {@link #getCaptureIds()} is the framework's authoritative read list, so the
 * result is checked as data rather than trusted as prose. {@link #validate()} returns one message
 * per inconsistency (a success without a capture id, two tables sharing a capture id, a requested
 * table with no outcome at all, an outcome for a table nobody asked for, no successful table at
 * all) and {@link #getCaptureIds()} throws instead of filtering such entries out. Call
 * {@link #isValid()} or {@link #isSuccess()} before using the result.</p>
 *
 * <p>Completeness needs both sides: a report can only be checked against the request it answers.
 * The result therefore carries the requested tables (see {@link #requestedTables(Collection)}), and
 * the framework may instead pass the request it actually made to
 * {@link #validate(Collection)} / {@link #getCaptureIds(Collection)}. Without a request the capture
 * list cannot be proven to cover every table, so an unknown request is itself a violation: the
 * silent failure mode this guards against is a connector that reports only the tables it managed to
 * install, leaving the rest neither in the capture list nor in the failure list.</p>
 */
public class TriggerCdcInstallResult implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Database/version-specific guarantees verified during ensure; required even on failure. */
    protected TriggerCdcCapabilities capabilities;
    protected TriggerCdcStatus status;
    protected String catalog;
    protected String schema;
    /** Per table outcome, in the order the tables were requested. */
    protected List<TriggerCdcTableResult> tableResults;
    /**
     * The tables {@code ensure} was asked for. Kept so {@link #validate()} can prove that every
     * requested table has exactly one outcome; without it a report is only self consistent, not
     * complete.
     */
    protected List<TriggerCdcTable> requestedTables;
    /** Non blocking remarks raised during installation. */
    protected List<String> warnings;

    public TriggerCdcCapabilities getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(TriggerCdcCapabilities capabilities) {
        this.capabilities = capabilities;
    }

    public TriggerCdcInstallResult capabilities(TriggerCdcCapabilities capabilities) {
        this.capabilities = capabilities;
        return this;
    }

    public static TriggerCdcInstallResult create() {
        return new TriggerCdcInstallResult();
    }

    public TriggerCdcInstallResult tableResult(TriggerCdcTableResult tableResult) {
        if (tableResults == null) {
            tableResults = new ArrayList<>();
        }
        tableResults.add(tableResult);
        return this;
    }

    public TriggerCdcInstallResult warning(String warning) {
        if (warnings == null) {
            warnings = new ArrayList<>();
        }
        warnings.add(warning);
        return this;
    }

    /**
     * Records one table that {@code ensure} was asked to capture. Pair it with the outcome reported
     * in {@link #tableResult(TriggerCdcTableResult)}: completeness is checked across the two lists,
     * so a requested table without an outcome is rejected instead of being read as "nothing to do".
     */
    public TriggerCdcInstallResult requestedTable(TriggerCdcTable table) {
        if (requestedTables == null) {
            requestedTables = new ArrayList<>();
        }
        requestedTables.add(table);
        return this;
    }

    /**
     * Records the full request. {@code ensure} receives this list as an argument, so calling this (or
     * letting the framework pass the same list to {@link #validate(Collection)}) is what makes the
     * result checkable: one outcome per requested table, no extras, no duplicates.
     */
    public TriggerCdcInstallResult requestedTables(Collection<TriggerCdcTable> tables) {
        if (tables == null) {
            this.requestedTables = null;
            return this;
        }
        this.requestedTables = new ArrayList<>(tables);
        return this;
    }

    /** True only when every requested table was installed or reused successfully and the result is
     * structurally valid (see {@link #validate()}). */
    public boolean isSuccess() {
        if (!isValid()) {
            return false;
        }
        for (TriggerCdcTableResult tableResult : tableResults) {
            if (!tableResult.isSuccessReported()) {
                return false;
            }
        }
        return true;
    }

    /**
     * True when this result is structurally usable: every table has an outcome, and every entry
     * that claims success carries a non blank, unique capture id.
     *
     * <p>Equivalent to {@code validate().isEmpty()}. A partial install (some tables reported as
     * {@code failure}) is still a valid result. Failed outcomes remain available through
     * {@link #getFailedTables()}, while capture accessors reject the partial install. A result
     * where <b>no</b> table succeeded is a violation, because its capture list would be
     * empty and a framework that iterates it would look healthy while reading nothing.</p>
     */
    public boolean isValid() {
        return validate().isEmpty();
    }

    /**
     * Checks this result against the rules in this class, using the request recorded through
     * {@link #requestedTables(Collection)}, and returns one message per violation; an empty list
     * means the report is structurally valid, not necessarily that every table succeeded.
     *
     * <p>Equivalent to {@code validate(getRequestedTables())}.</p>
     */
    public List<String> validate() {
        return validate(requestedTables);
    }

    /**
     * Checks this result against the tables the caller actually asked {@code ensure} for, and
     * returns one message per violation; an empty list means the report is structurally valid,
     * not necessarily ready to start.
     *
     * <p>Why the request is required: a report that only lists the tables the connector managed to
     * install is self consistent and still wrong. If {@code ensure} is called for A and B and only
     * A comes back, every entry looks fine, {@link #getCaptureIds()} would return A alone, and B
     * would be captured by nothing - no capture to read, and no failure entry to retry. Only the
     * request can tell the difference, so this method requires exactly one outcome per requested
     * table (a {@code failure} outcome counts, because it is retryable) and rejects the three ways
     * a set can fail to line up: a requested table with no outcome, more than one outcome for the
     * same table, and an outcome for a table that was never requested.</p>
     *
     * <p>The remaining rules are about readability of what is reported: a success must carry a non
     * blank capture id, no two tables may share a capture id, and at least one table must have been
     * installed, otherwise the capture list would be empty and a framework iterating it would look
     * like a healthy no-op install. Both used to be filtered away silently; now they are reported
     * here and rejected instead.</p>
     *
     * <p>The framework must call this (or {@link #isSuccess()}) before trusting the result; the
     * check verifies the report against the request, not what the database really did.</p>
     *
     * @param requestedTables the tables handed to {@code ensure}; never {@code null} and never empty
     */
    public List<String> validate(Collection<TriggerCdcTable> requestedTables) {
        List<String> violations = new ArrayList<>();
        if (capabilities == null) {
            violations.add("ensure must report the verified database capabilities");
        } else {
            if (capabilities.getDatabaseType() == null || capabilities.getDatabaseType().trim().isEmpty()) {
                violations.add("ensure must report a nonblank databaseType");
            }
            if (capabilities.getDatabaseVersion() == null || capabilities.getDatabaseVersion().trim().isEmpty()) {
                violations.add("ensure must report a nonblank databaseVersion");
            }
            if (!capabilities.supports(TriggerCdcErrorPolicy.STRICT)) {
                violations.add("ensure capabilities must declare STRICT error policy");
            }
            if (!capabilities.isDurableJournal()) {
                violations.add("ensure capabilities must declare a durable journal");
            }
            if (capabilities.isTruncateCaptureSupported()
                    != capabilities.supports(TriggerCdcOperation.TRUNCATE)) {
                violations.add("truncateCaptureSupported must agree with the TRUNCATE operation declaration");
            }
        }
        if (status != TriggerCdcStatus.CAPTURING && status != TriggerCdcStatus.SNAPSHOTTING
                && status != TriggerCdcStatus.STREAMING && status != TriggerCdcStatus.DEGRADED) {
            violations.add("ensure is not usable in status " + status);
        }
        Map<String, String> requested = indexRequested(requestedTables, violations);
        if (tableResults == null || tableResults.isEmpty()) {
            violations.add("ensure reported no per table result; at least one table outcome is "
                    + "required, otherwise the capture list is unknowable");
            return violations;
        }
        Map<String, String> captureOwners = new LinkedHashMap<>();
        Map<String, String> outcomeOwners = new LinkedHashMap<>();
        List<String> readable = new ArrayList<>();
        for (int i = 0; i < tableResults.size(); i++) {
            TriggerCdcTableResult tableResult = tableResults.get(i);
            String owner = describe(tableResult, i);
            if (tableResult == null) {
                violations.add("table result at index " + i + " is null; every requested table needs "
                        + "an explicit outcome");
                continue;
            }
            String key = tableKey(tableResult.getSourceTableId(), tableResult.getTableName());
            if (key == null) {
                violations.add(owner + " carries neither sourceTableId nor tableName, so its outcome "
                        + "can be matched to no requested table and no capture can be attributed");
            } else {
                String previousOutcome = outcomeOwners.put(key, owner);
                if (previousOutcome != null) {
                    violations.add(owner + " and " + previousOutcome + " are the same table reported "
                            + "twice; two outcomes for one table make its authoritative capture "
                            + "unknowable");
                }
                if (!requested.isEmpty() && !requested.containsKey(key)) {
                    violations.add(owner + " was not requested; an outcome for a table outside the "
                            + "request would add a capture nobody asked the framework to own");
                }
            }
            if (!tableResult.isSuccessReported()) {
                continue;
            }
            if (!tableResult.hasCaptureId()) {
                violations.add(owner + " is reported successful but has no captureId: its journal "
                        + "cannot be addressed, so it would be installed and never read");
                continue;
            }
            if (tableResult.getGeneration() == null) {
                violations.add(owner + " is reported successful but has no generation");
            }
            String captureId = tableResult.getCaptureId();
            String previousOwner = captureOwners.put(captureId, owner);
            if (previousOwner != null) {
                violations.add("captureId " + captureId + " is claimed by both " + previousOwner
                        + " and " + owner + "; one capture id must address exactly one table");
            }
            readable.add(owner);
        }
        for (Map.Entry<String, String> entry : requested.entrySet()) {
            if (!outcomeOwners.containsKey(entry.getKey())) {
                violations.add(entry.getValue() + " has no outcome in the result: a requested table "
                        + "that is neither reported successful nor reported failed is silently "
                        + "uninstalled, and nothing in the capture list or in getFailedTables() "
                        + "catches it");
            }
        }
        if (readable.isEmpty()) {
            violations.add("no table was installed successfully, so there is no capture to read; "
                    + "see getFailedTables() for the reasons");
        }
        return violations;
    }

    /**
     * Indexes the request by table identity and reports the ways the request itself is unusable.
     *
     * <p>An unknown request (null or empty) is a violation, not a free pass: with no request there
     * is nothing to prove the report complete against, and "cannot check" must not read as "fine".
     * The same applies to a requested table with no stable identity - no outcome could ever be
     * matched to it.</p>
     */
    private static Map<String, String> indexRequested(Collection<TriggerCdcTable> requestedTables,
            List<String> violations) {
        Map<String, String> requested = new LinkedHashMap<>();
        if (requestedTables == null || requestedTables.isEmpty()) {
            violations.add("the set of requested tables is unknown, so the report cannot be checked "
                    + "for completeness: a requested table that is simply absent from the result "
                    + "would look identical to one that was never asked for. Record the request with "
                    + "requestedTables(...), or pass it to validate(requestedTables)");
            return requested;
        }
        int index = 0;
        for (TriggerCdcTable table : requestedTables) {
            String owner = describeRequested(table, index);
            if (table == null) {
                violations.add(owner + " is null; a request must name the tables it asks for");
                index++;
                continue;
            }
            String key = tableKey(table.getSourceTableId(), table.getTableName());
            if (key == null) {
                violations.add(owner + " has neither sourceTableId nor tableName, so no outcome can "
                        + "be matched to it");
                index++;
                continue;
            }
            String previous = requested.put(key, owner);
            if (previous != null) {
                violations.add(owner + " and " + previous + " are the same table; the request lists "
                        + "it twice, so one capture would be reported for two requested tables");
            }
            index++;
        }
        return requested;
    }

    /**
     * Identity of a table for matching an outcome to the request: the stable id when the connector
     * echoes it, the physical name as a fallback. The two spaces are prefixed differently so an id
     * that happens to look like a table name cannot be mistaken for one.
     */
    private static String tableKey(String sourceTableId, String tableName) {
        if (sourceTableId != null && !sourceTableId.trim().isEmpty()) {
            return "id:" + sourceTableId;
        }
        if (tableName != null && !tableName.trim().isEmpty()) {
            return "name:" + tableName;
        }
        return null;
    }

    private static String describeRequested(TriggerCdcTable table, int index) {
        if (table == null) {
            return "requested table at index " + index;
        }
        StringBuilder text = new StringBuilder("requested table ")
                .append(emptyToIndex(table.getTableName(), index));
        if (table.getSourceTableId() != null && !table.getSourceTableId().isEmpty()) {
            text.append(" (sourceTableId ").append(table.getSourceTableId()).append(")");
        }
        return text.toString();
    }

    private static String emptyToIndex(String tableName, int index) {
        return tableName == null || tableName.isEmpty() ? "#" + index : tableName;
    }

    private static String describe(TriggerCdcTableResult tableResult, int index) {
        if (tableResult == null) {
            return "table result at index " + index;
        }
        StringBuilder text = new StringBuilder("table ")
                .append(emptyToIndex(tableResult.getTableName(), index));
        if (tableResult.getSourceTableId() != null && !tableResult.getSourceTableId().isEmpty()) {
            text.append(" (sourceTableId ").append(tableResult.getSourceTableId()).append(")");
        }
        return text.toString();
    }

    /**
     * Requested tables that have no outcome at all in this result.
     *
     * <p>Non empty means {@code ensure} skipped part of the request: those tables are not in the
     * capture list (so they will not be read) and not in {@link #getFailedTables()} (so nobody will
     * retry them). That is why {@link #validate()} treats it as a violation rather than a warning.</p>
     */
    public List<TriggerCdcTable> getMissingTables() {
        List<TriggerCdcTable> missing = new ArrayList<>();
        if (requestedTables == null || requestedTables.isEmpty()) {
            return missing;
        }
        Map<String, String> reported = reportedTables();
        for (TriggerCdcTable table : requestedTables) {
            String key = table == null ? null : tableKey(table.getSourceTableId(), table.getTableName());
            if (key != null && !reported.containsKey(key)) {
                missing.add(table);
            }
        }
        return missing;
    }

    private Map<String, String> reportedTables() {
        Map<String, String> reported = new LinkedHashMap<>();
        if (tableResults == null) {
            return reported;
        }
        for (int i = 0; i < tableResults.size(); i++) {
            TriggerCdcTableResult tableResult = tableResults.get(i);
            if (tableResult == null) {
                continue;
            }
            String key = tableKey(tableResult.getSourceTableId(), tableResult.getTableName());
            if (key != null) {
                reported.put(key, describe(tableResult, i));
            }
        }
        return reported;
    }

    /**
     * Tables that are not usable: outright failures, plus entries that claim success but carry no
     * capture id (those are installed but unreadable, so they are reported here to be fixed rather
     * than dropped from {@link #getCaptureIds()}).
     *
     * <p>Requested tables with no outcome at all cannot appear here - there is no entry to return -
     * so they are exposed by {@link #getMissingTables()} and reported by {@link #validate()}.</p>
     */
    public List<TriggerCdcTableResult> getFailedTables() {
        List<TriggerCdcTableResult> failed = new ArrayList<>();
        if (tableResults != null) {
            for (TriggerCdcTableResult tableResult : tableResults) {
                if (tableResult != null && !tableResult.isSuccess()) {
                    failed.add(tableResult);
                }
            }
        }
        return failed;
    }

    /**
     * Capture ids for a fully successful installation.
     *
     * <p>This is the authoritative capture list used to start a multi table task. Every requested
     * table must succeed; any failed outcome makes this accessor throw, even when the report is
     * structurally valid. Use {@link #getFailedTables()} for diagnosis and retry information.</p>
     *
     * <p>Fail closed: missing or duplicate outcomes, missing or duplicate capture ids, unusable
     * status and invalid capabilities also cause this accessor to throw. Nothing is filtered out
     * and an empty capture list is never returned.</p>
     *
     * <p>Completeness is checked against the request recorded on this result. The framework normally
     * also has the request it just made, so it should call
     * {@link #getCaptureIds(Collection)} with that list: it is the authoritative answer to "what did
     * we ask for", and it cannot be forged by an incomplete report.</p>
     *
     * @throws IllegalStateException when validation fails or any requested table failed
     */
    public List<String> getCaptureIds() {
        return getCaptureIds(requestedTables);
    }

    /**
     * Same as {@link #getCaptureIds()}, but checks completeness against the request the framework
     * actually made rather than the one recorded on the result.
     *
     * @param requestedTables the tables handed to {@code ensure}; never {@code null} and never empty
     * @throws IllegalStateException when validation fails or any requested table failed
     */
    public List<String> getCaptureIds(Collection<TriggerCdcTable> requestedTables) {
        List<String> violations = validate(requestedTables);
        if (!violations.isEmpty()) {
            throw new IllegalStateException("install result rejected, its capture list is not "
                    + "trustworthy: " + String.join("; ", violations));
        }
        List<String> captureIds = new ArrayList<>(tableResults.size());
        for (TriggerCdcTableResult tableResult : tableResults) {
            if (!tableResult.isSuccessReported()) {
                throw new IllegalStateException("install result rejected: every requested table must "
                        + "succeed before starting; see getFailedTables() for retry information");
            }
            captureIds.add(tableResult.getCaptureId());
        }
        return captureIds;
    }

    public TriggerCdcStatus getStatus() {
        return status;
    }

    public void setStatus(TriggerCdcStatus status) {
        this.status = status;
    }

    public TriggerCdcInstallResult status(TriggerCdcStatus status) {
        this.status = status;
        return this;
    }

    public String getCatalog() {
        return catalog;
    }

    public void setCatalog(String catalog) {
        this.catalog = catalog;
    }

    public TriggerCdcInstallResult catalog(String catalog) {
        this.catalog = catalog;
        return this;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public TriggerCdcInstallResult schema(String schema) {
        this.schema = schema;
        return this;
    }

    public List<TriggerCdcTableResult> getTableResults() {
        return tableResults;
    }

    public void setTableResults(List<TriggerCdcTableResult> tableResults) {
        this.tableResults = tableResults;
    }

    /** Tables {@code ensure} was asked for; see {@link #requestedTables(Collection)}. */
    public List<TriggerCdcTable> getRequestedTables() {
        return requestedTables;
    }

    public void setRequestedTables(List<TriggerCdcTable> requestedTables) {
        this.requestedTables = requestedTables;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public void setWarnings(List<String> warnings) {
        this.warnings = warnings;
    }
}
