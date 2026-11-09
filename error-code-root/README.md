# Existing error contracts and maintenance

The production annotations, exceptions, scanner, versions and dependency graph are unchanged. The standalone `contract-tests` directory supplies regression tests and a maintenance exercise; it is not part of the production reactor, BOM or published API. Its collector and exporter are feasibility witnesses, not a replacement for the existing scanner or a delivered product adapter.

## Ownership and numbering

Maintain a business error in its owning repository and module. Search existing definitions and reuse an error only when its meaning, parameters and recovery behavior match. Adding a business definition does not itself require a new shared-library API or release.

`@TapExClass.code` is module metadata. Existing practice commonly uses two digits, but the current implementation does not concatenate this number into the field value. A field annotated with `@TapExCode` contains the entire numeric portion, not a module-local suffix. `ErrorCodeEntity.fullErrorCode()` prepends the uppercased prefix to that field value. For example, prefix `EXERCISE`, module metadata `43` and field `430001` produce `EXERCISE430001`, not `EXERCISE43430001`.

Connector module `43` is connector-defined, not a newly allocated universal reserved number. This verification adds no special module, numeric-width or prefix/module-combination restriction to connector inputs. Connectors may share module metadata and use their own definitions. Preserve existing complete codes, including historical inconsistencies and different widths; do not pad, reinterpret or renumber them. Module metadata alone is not globally unique identity.

A product consuming definitions must resolve them in the appropriate trusted source domain. Within one catalog domain, different declarations with the same full code are ambiguous and must be detected before insertion overwrites either declaration. Distinct prefixes may distinguish full codes. A prefix or self-reported PDK ID alone does not prove connector provenance. Existing `Scanner.getErrorCodeMap()` keys by the raw field and uses `Map.put`; it does not implement this stricter product contract. No existing global scanner behavior is changed here.

## Routine change checklist

1. Search for both the proposed code and its meaning, including equivalent errors in related operations. Read parameter order and recovery/skip behavior before reuse.
2. Add the declaration, its required descriptions, the throw/use site and focused tests in the owning change. Use a stable prefix and preserve the entire field value. The exercise prefix below is temporary and must not be shipped.
3. Review semantics: can an existing code express this same error? If not, explain the differing trigger, affected object or recovery behavior. Automated text similarity is only a review hint; it cannot prove semantic uniqueness.
4. Check exact full-code conflicts in the actual catalog domain before overwrite, required metadata, parameter compatibility and preservation of existing codes. Product-side strict checks apply when that consuming path is delivered; these tests do not activate gates in other repositories.
5. Deprecate a definition without removing it, changing its meaning or reusing its code. Keep old diagnostics readable. Migration or a breaking compatibility change requires a separate explicit decision.

The owning module's maintainer reviews meaning and parameter contracts. The shared-library maintainer owns these existing-interface regression tests. The consuming product maintainer owns its catalog generation, source resolution and diagnostic adapter. Ordinary feature and bug-fix work continues on current dependencies and does not wait for a future migration stage.

## Isolated verification

Use JDK 17 and Maven. Run from the repository root in your own worktree. These commands download dependencies and install only to a fresh temporary Maven repository; they do not deploy/upload artifacts or alter Maven settings. Do not use your shared `~/.m2/repository` for this exercise.

```sh
set -eu
exercise_dir=$(mktemp -d "${TMPDIR:-/tmp}/existing-errors.XXXXXX")
repo="$exercise_dir/repository"
mvn -B -ntp -Dmaven.repo.local="$repo" -Pjdk-17 \
  -pl error-code-root/error-code-bom,error-code-root/error-code-scanner,error-code-root/pdk-error-code,plugin-kit/tapdata-api \
  -am clean install
mvn -B -ntp -Dmaven.repo.local="$repo" \
  -f error-code-root/contract-tests/pom.xml clean verify
```

Expected: both commands succeed; `ExistingErrorContractTest` has no failures or skips. A dependency/network/setup failure is not a passed test. JDK 17 success is not proof of a complete JDK 8 producer build: the existing API sources have separate known Java 8 compilation limitations. Frozen old callers and real connector/Flow Engine runtime checks remain separate compatibility evidence.

## Manual exercise

Continue in the same isolated shell. Keep the directory for review; no cleanup is automatic.

```sh
mvn -B -ntp -Dmaven.repo.local="$repo" \
  -f error-code-root/contract-tests/pom.xml \
  dependency:build-classpath -Dmdep.outputFile=target/exercise-classpath.txt
cp_test="error-code-root/contract-tests/target/test-classes:$(cat error-code-root/contract-tests/target/exercise-classpath.txt)"
java -cp "$cp_test" io.tapdata.contracts.MaintenanceExercise export "$exercise_dir/catalog.tsv"
cat "$exercise_dir/catalog.tsv"
```

Expected: sorted `EXERCISE20001`, `EXERCISE430001`, `OTHER430001`. The deprecated `20001` declaration stays present without padding; two connectors can share module metadata 43. The field contains the full numeric portion.

To try an addition, copy this checkout to a disposable worktree/directory before editing. Add `@TapExCode(describe = "Connection closed") public static final String CLOSED = "430002";` to the test-only `Codes` class, recompile using `test-compile`, and export again. Expected: `EXERCISE430002` appears. The fixed regression suite intentionally protects the original fixture; run the unmodified suite for delivery verification. Do not commit the exercise mutation.

Check conflict handling against an existing export:

```sh
cp "$exercise_dir/catalog.tsv" "$exercise_dir/catalog.before.tsv"
if java -cp "$cp_test" io.tapdata.contracts.MaintenanceExercise conflict "$exercise_dir/catalog.tsv" > "$exercise_dir/conflict.log" 2>&1; then
  echo 'FAIL: conflicting definitions were accepted' >&2
  exit 1
fi
grep -F 'Duplicate full code: EXERCISE430001' "$exercise_dir/conflict.log"
cmp "$exercise_dir/catalog.before.tsv" "$exercise_dir/catalog.tsv"
```

Expected: nonzero exit naming `EXERCISE430001`, then `cmp` succeeds. The second definition has module metadata 37: changing module metadata does not resolve an identical full-code collision. This checks a test-only consumer before overwrite; it does not claim the old global scanner rejects duplicates.

## Record template

Record the following alongside the run logs, outside the product checkout:

| Item | Observation |
|---|---|
| Operator, date, source SHA | |
| JDK / Maven / OS | |
| Isolated repository and output directory | |
| Library GAVs and JAR/POM hashes | |
| Contract test count / failures / skips | |
| Lookup and original code values | |
| Added code and exported value | |
| Deprecated code retained unchanged | |
| Conflict exit and previous-file comparison | |
| Semantic reuse decision and reviewer | |
| Questions / unsupported cases | |
| Human acceptance (explicit, separate from test success) | |

Retain existing baseline manifests and evidence. Refresh only entries affected by a source/artifact change after reviewing why they changed; never regenerate a golden to hide a new violation. Missing source identity, unavailable environments or unrun runtime cases remain explicitly unverified.
