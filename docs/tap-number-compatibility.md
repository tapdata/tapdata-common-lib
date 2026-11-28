# Compatible exact numbers in TapNumberValue

`TapNumberValue` still extends `AbsBasicTapValue<Double, TapNumber>`. Existing
constructors, `getValue()`, `setValue(Double)`, and `value(Double)` retain their
Double contract, including calls through `TapValue` and unchanged plugin bytecode.

New producers use `ofNumber(number)` / `setNumberValue(number)`; new consumers use
`getNumberValue()`. Supported payloads are BigDecimal, BigInteger, Long, Integer,
Short, Byte, Float and Double. Unsupported Number implementations are rejected by
this new API before they reach serialization. Exactly one payload slot is active;
every setter clears the previous payload, including null assignments. Instances
are mutable and not thread safe.

The legacy `getValue()` returns a Double view and never throws solely because the
payload is exact. This view can round large integers/decimals or overflow to
infinity, just as the old input codec did. It does not modify the stored value.
Internal numeric consumers, both codec filters, and serializers use
`getNumberValue()` instead. Existing third-party consumers must migrate to that
method to gain precision preservation; binary compatibility alone cannot make a
Double-returning API lossless.

## Default codecs

The input codec retains BigDecimal, BigInteger and Long values outside the safe
53-bit integer range. Other Number inputs, including custom implementations, keep
the legacy doubleValue conversion. Float-to-Double widening preserves the Float's
binary value. Long-to-Double conversion is not generally lossless. Finite decimal
strings parse as BigDecimal; Boolean and non-finite strings retain the Double path.
The previously disabled global Number-to-TapValue registration remains disabled.

The output codec preserves historical conversion for Double payloads, including
truncation/narrowing and permissive decimal handling. Missing integer bounds now
allow Double passthrough rather than a null-pointer failure. New non-Double
payloads use compatible target conversion: reducing a nonzero scale uses HALF_UP (`123.455` to scale 2 is `123.46`, `-123.455` is `-123.46`);
scale 0 truncates toward zero only for targets declaring both min/max bounds or
an explicit bit width (`123.9` to INT is `123`, `-123.9` is `-123`).
Scale-zero decimal targets without that integer metadata use HALF_UP
(`2.9` to DECIMAL(18,0) is `3`, `-2.9` is `-3`) and return BigDecimal.
Unsigned, min/max bounds and precision checks apply after conversion, so rounding
carry that exceeds the target capacity is rejected. Unsigned validation uses the
converted value, so `-0.4` rounded to zero is accepted. Trailing zeros require no rounding.
These rules also apply to decimal strings parsed by the input codec. Explicit
floating targets (`fixed=false`, bit 32/64) permit IEEE rounding but reject finite overflow.
Their unsigned and min/max checks apply to the converted IEEE value, including finite
Float/Double extremes. A bit width with absent `fixed` identifies an integer, not a float;
when its scale is absent it uses integer truncation too. Other null-scale targets
do not imply integer conversion for exact values.
Precision-only targets validate precision even when scale and fixed are absent.
Explicit scale=0 integer targets with bit width but without either bound return
Long when it fits, otherwise BigInteger; no implicit 64-bit capacity is imposed. Bounded integer targets
continue to choose their representation from the declared range.
Unconstrained targets preserve the original Number representation. Float-to-decimal
conversion uses the shortest decimal string, not a binary expansion.

An exact payload's default TapNumber is intentionally unbounded: inferring a target
column's capacity from one record could under-size it for subsequent records.
Consumers must handle absent bounds. Double's default type keeps its historical
bounds and its historical output conversion.

## Serialization and rollout

V2 retains existing numeric tags and the TapValue `value` entry. Legacy Double
bytes are unchanged. New readers accept old Double bytes. Exact numbers preserve
value, representation and metadata through nested events and repeated recovery.

The runner's JsonParser uses instance-local numeric adapters, without replacing
Fastjson's global numeric adapters. Read paths consult live host/global deserializer
registrations, including registrations added or replaced after parser construction
and class-aware reads; the exact TapNumberValue adapter remains local. Fastjson
retains its existing bean/field adapter caching behavior after first use. Finite legacy Double payloads retain a numeric
`value`; exact representations use a `numberType` and textual number in a `value`
object. Non-finite floats and signed zero also use this explicit form. Numeric
schema subtypes are restored using a fixed allowlist and `typeName`. Malformed
explicit representations fail clearly. Numeric envelopes carry no Java collection
class metadata, including in toJsonWithClass/fromJsonWithClass round trips. The
codec removes redundant numeric parsing from the serialization path.

Direct Fastjson calls and other JSON libraries continue using their own adapters;
the legacy value getter exposes a Double view; bean introspection may also expose
the new numberValue property. Their JSON shape is not the runner transport format.
Use the runner's JsonParser for exact-number transport. JsonParser retains its existing convention of disabling
circular-reference detection; it is not an object-identity/cyclic-graph transport.

Native Java serialization retains the original computed serialVersionUID
(`7726157022888528681`) and default stream protocol. Old/new streams can be read
in either direction without InvalidClassException. This does **not** repair the
historical omission of inherited TapValue fields: Double payload, tapType and
origin metadata were never serialized. An old reader ignores the new numberValue
field. Do not use native Java serialization for event transport or exact-number
rollout; a framework-wide TapValue serialization design is separate work.

Upgrade API and runner together before enabling exact producers. Do not route new
exact wire representations to older runners/engines. Old plugins continue to work
through the Double view; exact consumption requires the new API. This change does
not update engine-side consumers in other repositories.

## Validation

Tests compile unchanged legacy client source against the original API, then run
its bytecode against the new API. They also load the original class in isolation
to verify native Java UID and stream compatibility in both directions, including
the historical omission of inherited fields.

Coverage includes both filter paths and callbacks, large Long values, decimal
extremes, target checks, legacy Double behavior, trailing zeros, unsupported Number
handling, unchanged V2 Double bytes, nested V2 recovery, local JSON registration,
JSON schema subtypes, malformed JSON, and exact values across registries.

```sh
TZ=Asia/Shanghai mvn -B -pl plugin-kit/tapdata-api,plugin-kit/tapdata-pdk-runner -am test -Dproject.build.sourceEncoding=UTF-8
```

The existing CommonUtils date-string test assumes Asia/Shanghai.
