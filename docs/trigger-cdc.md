# Trigger CDC PDK contract

Trigger CDC adds three connector functions: `ensure`, `acknowledge`, and `cleanup`.
Reading uses the existing `StreamReadFunction` and ordinary `TapEvent` types.
`ConnectorFunctions` advertises `trigger_cdc` only when all four are registered.
Database/version-specific guarantees come back in `TriggerCdcInstallResult.capabilities`.
Registration advertises implementation support; it does not establish database privileges,
DDL support or a safe snapshot boundary. Those must be verified by ensure.

## Lifecycle and checkpoint integration

1. Call ensure with the effective storage config, a stable consumer id and the original
   requested table list. It installs/verifies owned objects and registers the durable
   subscription before a snapshot or read can start. Repeat on resume. Keep subscriptions
   across stop/crash and do not expire them by TTL.
2. Check `validate(originalTables)` and require every table to succeed before starting the
   task. Persist every capture id and generation. A partial result describes retryable
   failures, not permission to silently omit those tables. Reject full-to-incremental
   handover if the connector cannot prove a capture boundary.
3. Invoke streamRead on the same node context. The connector uses the consumer/config/capture
   mapping retained by ensure and polls committed journal records. The offset is a complete
   `Map<String, TriggerCdcOffset>` keyed by capture id, even for a single table. Validate keys,
   identities, generations and token ownership before resume. Emit ordinary TapEvents and a
   fresh map/value snapshot on each callback. Stop on schema drift, disabled/missing triggers
   or a subscription marked integrity broken.
4. Each token describes the cumulative delivered set for its capture, including earlier
   unacknowledged batches. An out-of-order committed event must still be delivered: sequence
   allocation order is not commit order. Track row membership rather than an event-id high
   watermark. In-memory delivery tracking can avoid continuous duplicates while a checkpoint
   is pending; loss of that tracking after restart safely replays unacknowledged events.
5. Only after every destination's writes and the corresponding recoverable checkpoint are
   durable, pass each checkpoint entry to acknowledge. Tokens are bound to consumer, capture
   and generation. Duplicate or older acknowledgements are idempotent and cannot regress
   progress. A crash before acknowledgement may cause replay; targets need idempotent writes.
6. Run bounded cleanup. Delete only rows acknowledged by every registered consumer and old
   enough under retention. A paused or deleted task still holds its subscription in this
   first version. Forced deletion requires explicit opt-in, usable affected consumer ids,
   and integrity markings persisted before or atomically with the delete. Validate each result.

The current engine callback in `HazelcastSourcePdkDataNode.generateStreamReadConsumer`
enqueues events and updates an in-memory offset. Returning from it is not a durable checkpoint
notification. The engine integration must connect step 5 to durable progress covering every
sink; until then, it must not acknowledge on callback return or enable journal reclamation
based on delivery. This PR provides the PDK contracts, not that engine implementation.

## Scope

There are no separate capability-query, SQL-plan, read, periodic-inspect or release APIs.
Ensure returns verified capabilities and performs startup checks; the stream reader performs
ongoing integrity checks. SQL preview/manual installation, automatic subscription retirement
and automatic trigger/table removal are deferred. Operators must explicitly retire abandoned
subscriptions outside this API; automatic expiry would lose paused consumers' data.

Tapdata API artifacts use `2.0.12-SNAPSHOT`; the parent, runner, modules and error-code
artifacts use `2.12-SNAPSHOT`. The companion changes in the engine/manager and connectors
repositories must be applied with this release. Third-party libraries and Scala's binary
version are independent of this release.
