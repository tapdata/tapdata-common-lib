# Floating-Point Raw Path Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Remove per-record `TapFloatValue`/`TapDoubleValue` allocation from the engine hot path while preserving the existing public `TapValue` conversion contract.

**Architecture:** Keep `ToTapValueCodec` as the compatibility path. Add a separate raw-value codec contract for `TapFloat` and `TapDouble`, and expose an explicit raw floating-point mode from `TapCodecsFilterManager`. The engine uses raw mode only when schema is known and origin/custom-codec semantics do not require a `TapValue`; all other callers retain the existing behavior.

**Tech Stack:** Java, Maven, JUnit 5, tapdata-common-lib codec/filter APIs, new_tapdata engine.

**Spec:** The confirmed user requirement is to solve the main performance cost caused by per-record floating-point `TapValue` allocation and reverse conversion, without reintroducing the broad `TYPE_NUMBER` regression.

## Global Constraints

- Plain `TapNumber` fields must continue to pass through without `ToTapNumberCodec`.
- `TapFloat` must preserve binary32 quantization and NaN/Infinity/overflow validation.
- `TapDouble` must preserve binary64 conversion and NaN/Infinity/overflow validation.
- Existing public `transformToTapValueMap` behavior remains the compatibility default.
- Raw mode returns the existing common-model representation: a binary32-quantized `Double` for `TapFloat` and a `Double` for `TapDouble`.
- A null raw conversion result means invalid conversion only; raw mode must not overload the existing `TapValue` fallback semantics.
- Do not pool or reuse mutable `TapValue` instances.

## Review Focus

- A raw float must still be binary32-quantized and emitted as the common-model `Double` representation.
- Disallowed NaN/Infinity and finite overflow must not bypass validation.
- Custom codecs and origin-value maps must take precedence over raw mode.
- Nested maps/arrays must apply raw mode to scalar floating-point fields only.
- Schema cache invalidation must not reuse a field plan after table schema changes.

### Task 1: Add raw floating-point codec contract

**Files:**
- Create: `plugin-kit/tapdata-api/src/main/java/io/tapdata/entity/codec/ToTapRawValueCodec.java`
- Modify: `plugin-kit/tapdata-api/src/main/java/io/tapdata/entity/codec/impl/ToTapFloatCodec.java`
- Modify: `plugin-kit/tapdata-api/src/main/java/io/tapdata/entity/codec/impl/ToTapDoubleCodec.java`
- Test: `plugin-kit/tapdata-api/src/test/java/io/tapdata/entity/schema/type/TapFloatingPointTypeTest.java`

**Interfaces:**
- Produces `ToTapRawValueCodec.toRawValue(Object, TapType)`.
- Existing `ToTapValueCodec.toTapValue` remains unchanged.

- [ ] Add failing tests for raw float/double conversion, quantization, special-value rejection, and finite overflow.
- [ ] Verify the tests fail because the raw codec interface is absent.
- [ ] Implement the raw codec interface and share conversion/validation logic with the existing codecs.
- [ ] Run the focused common-lib tests and verify they pass.

### Task 2: Add explicit raw mode to codec filter managers

**Files:**
- Create: `plugin-kit/tapdata-api/src/main/java/io/tapdata/entity/codec/filter/FloatingPointTransformMode.java`
- Modify: `plugin-kit/tapdata-api/src/main/java/io/tapdata/entity/codec/filter/TapCodecsFilterManager.java`
- Modify: `plugin-kit/tapdata-api/src/main/java/io/tapdata/entity/codec/filter/TapCodecsFilterManagerSchemaEnforced.java`
- Test: `plugin-kit/tapdata-api/src/test/java/io/tapdata/entity/codec/filter/TapCodecsFilterManagerTest.java`

**Interfaces:**
- Existing overloads default to compatibility mode.
- New overloads accept `FloatingPointTransformMode`.
- Raw mode must keep custom codec precedence and bypass only concrete `TapFloat`/`TapDouble` schema fields.

- [ ] Add failing tests proving compatibility mode still returns `TapValue` for concrete floating-point schema types.
- [ ] Add failing tests proving raw mode returns the common-model `Double` representation for both floating-point types, does not return `TapValue`, and leaves plain numbers unchanged.
- [ ] Add failing tests for custom codec and origin-map fallback behavior.
- [ ] Implement the mode-aware conversion with a distinct raw path, never interpreting raw `null` as pass-through.
- [ ] Run focused manager tests and verify they pass.

### Task 3: Cache field-level codec plans

**Files:**
- Modify: `plugin-kit/tapdata-api/src/main/java/io/tapdata/entity/codec/filter/entity/TransformToTapValueFieldWrapper.java`
- Modify: `plugin-kit/tapdata-api/src/main/java/io/tapdata/entity/codec/filter/TapCodecsFilterManagerSchemaEnforced.java`
- Test: `plugin-kit/tapdata-api/src/test/java/io/tapdata/entity/codec/filter/TapCodecsFilterManagerTest.java`

**Interfaces:**
- The cached wrapper stores the field and its schema codec plan, but never stores per-record values.
- Custom runtime codecs remain checked before the cached schema codec.

- [ ] Add a regression test that repeated records reuse the field-level schema codec plan.
- [ ] Implement codec-plan caching with schema identity/version safety.
- [ ] Run manager tests and inspect allocation-sensitive behavior with the local performance test.

### Task 4: Integrate raw mode into engine hot paths

**Files:**
- Modify: `iengine/iengine-app/src/main/java/io/tapdata/flow/engine/V2/node/hazelcast/data/pdk/HazelcastSourcePdkBaseNode.java`
- Modify: `iengine/iengine-app/src/main/java/io/tapdata/flow/engine/V2/node/hazelcast/data/pdk/HazelcastPdkBaseNode.java`
- Modify: `iengine/iengine-app/src/main/java/io/tapdata/flow/engine/V2/node/hazelcast/HazelcastBaseNode.java`
- Modify: `iengine/iengine-app/src/main/java/io/tapdata/flow/engine/V2/node/hazelcast/data/pdk/HazelcastTargetPdkBaseNode.java`
- Test: relevant source/target and conversion tests

**Interfaces:**
- Source/processor paths request raw mode for schema-known transformations; the manager itself keeps compatibility as the public default.
- Schema-free values, origin-sensitive values, and custom codecs retain the compatibility path inside raw mode.
- Target conversion leaves raw floating-point values untouched and still unwraps existing `TapValue` instances.

- [ ] Add failing engine tests covering source-to-target float/double raw values and fallback to `TapValue` when origin/custom semantics apply.
- [ ] Change the hot paths to request raw floating-point mode.
- [ ] Run focused engine tests and verify no target receives `TapFloatValue`/`TapDoubleValue` for the raw path.

### Task 5: Full verification and benchmark comparison

**Files:**
- Modify or create the existing benchmark/test fixture only if needed.

- [ ] Run the complete common-lib test suite.
- [ ] Run the focused engine test suite available in the workspace.
- [ ] Compare baseline, compatibility mode, codec-plan cache, and raw mode for throughput, allocation rate, and GC.
- [ ] Review the final diff for public API compatibility and schema-cache invalidation.
