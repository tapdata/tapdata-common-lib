# Compatible exact numbers in TapNumberValue

`TapNumberValue` still extends `AbsBasicTapValue<Double, TapNumber>`. The existing
no-argument and Double constructors, `getValue()`, `setValue(Double)`, and
`value(Double)` retain their Double contract, including calls through `TapValue`.

New producers use `TapNumberValue.ofNumber(number)` or `setNumberValue(number)`.
New consumers use `getNumberValue()`. BigDecimal (including scale), BigInteger,
Long, other integral wrappers, Float and Double retain their original representation.
Only one payload is active. Every setter clears the previous payload, including
null assignments. The legacy getter throws for a non-Double payload; it never
silently rounds an exact number to Double.

The default input codec retains Number objects and parses finite decimal strings
as BigDecimal. Boolean conversion still uses Double 0/1. Default output conversion
returns the original Number without a concrete target constraint. A null scale
does not imply integer conversion. Explicit integer/decimal targets check bounds,
fractional digits and precision; rounding is not implicit. Explicit floating
point targets allow normal IEEE rounding but reject finite overflow. NaN and
infinity are retained for unconstrained/floating point paths and rejected for
integer/decimal conversion. Decimal128 NaN/Infinity must still be handled by the
MongoDB connector before converting to BigDecimal.

## Serialization and rollout

V2 object serialization retains the existing TapValue `value` entry and numeric
tags. Double values keep the old wire format. New readers accept old Double
payloads. BigDecimal, BigInteger and numeric wrappers retain their values and
classes through nested records and repeated serialization/recovery.

The runner's JsonParser implementation registers a numeric serializer: legacy
finite Double payloads retain the numeric `value` field; other representations use
an explicit `numberType` and a textual number inside a `value` object.
The object form makes old Double JSON readers fail instead of rounding the value. This preserves scale, large integers,
non-finite floats and signed zero. The decoder accepts legacy Double JSON.
Callers using a different JSON library/configuration must supply an equivalent
adapter; the old `getValue()` is intentionally unsuitable for exact payloads.

Java object serialization explicitly includes the inherited TapValue metadata and
payload, since TapValue itself is not Serializable. Its stream format requires
matching library versions; use V2 for existing persistent Double data and rolling
upgrades. Historical Java-serialized TapNumberValue streams already omitted the
inherited payload and cannot recover that missing value.

Upgrade the common API and runner together before enabling exact-number producers.
Old plugin bytecode can continue using Double without recompilation. Old custom
codecs calling `getValue()` cannot consume newly introduced exact payloads and must
migrate to `getNumberValue()`. Do not route exact payloads to older engines or
runners: these paths are outside the compatibility guarantee. This change does
not enable the previously disabled global Number-to-TapValue registration.

## Validation

The tests compile a client against the original TapNumberValue source and run its
unchanged bytecode against the new API. Numeric tests cover decimal extremes,
unsigned 64-bit magnitudes, values beyond Double's integer precision, target
bounds, prohibited rounding, NaN/infinity, signed zero, and setter replacement.
API and runner tests cover cross-registry decoding and Java/V2/JSON round trips.

Run the API, PDK API and runner suites with their reactor dependencies:

```sh
TZ=Asia/Shanghai mvn -B -pl plugin-kit/tapdata-api,plugin-kit/tapdata-pdk-runner -am test -Dproject.build.sourceEncoding=UTF-8
```

The existing CommonUtils date-string test assumes Asia/Shanghai.
