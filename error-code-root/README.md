# Existing error contracts and maintenance

The production annotations, exceptions, scanner, versions and dependency graph are unchanged. The standalone `contract-tests` directory supplies regression tests and a maintenance exercise; it is not part of the production reactor, BOM or published API. Its collector and exporter are feasibility witnesses, not a replacement for the existing scanner or a delivered product adapter.

## Ownership and numbering

Maintain a business error in its owning repository and module. Search existing definitions and reuse an error only when its meaning, parameters and recovery behavior match. Adding a business definition does not itself require a new shared-library API or release.

`@TapExClass.code` is module metadata. Existing practice commonly uses two digits, but the current implementation does not concatenate this number into the field value. A field annotated with `@TapExCode` contains the entire numeric portion, not a module-local suffix. `ErrorCodeEntity.fullErrorCode()` prepends the uppercased prefix to that field value. For example, prefix `EXERCISE`, module metadata `43` and field `430001` produce `EXERCISE430001`, not `EXERCISE43430001`.

**Example values do not specify a numbering format.** `430001` and `430002` are literal test inputs, not an approved split into a two-digit module and a four-digit sequence. Module metadata `43` neither pads nor truncates the field and does not determine its width. With the same prefix `EXAMPLE` and module metadata `43`, the existing calculation gives:

| Literal field value | Full code |
|---|---|
| `430001` | `EXAMPLE430001` |
| `043001` | `EXAMPLE043001` |
| `43001` | `EXAMPLE43001` |

These are three distinct existing-interface inputs, not interchangeable spellings or newly allocated codes. Preserve leading zeroes and the entire original field. The examples do not authorize a new product numbering policy; concrete widths for product-owned new definitions require a separate explicit decision before use. This does not add restrictions to connector-defined inputs.

Connector module `43` is connector-defined, not a newly allocated universal reserved number. This verification adds no special module, numeric-width or prefix/module-combination restriction to connector inputs. Connectors may share module metadata and use their own definitions. Preserve existing complete codes, including historical inconsistencies and different widths; do not pad, reinterpret or renumber them. Module metadata alone is not globally unique identity.

A product consuming definitions must resolve them in the appropriate trusted source domain. Within one catalog domain, different declarations with the same full code are ambiguous and must be detected before insertion overwrites either declaration. Distinct prefixes may distinguish full codes. A prefix or self-reported PDK ID alone does not prove connector provenance. Existing `Scanner.getErrorCodeMap()` keys by the raw field and uses `Map.put`; it does not implement this stricter product contract. No existing global scanner behavior is changed here.

## Routine change checklist

1. Search for both the proposed code and its meaning, including equivalent errors in related operations. Read parameter order and recovery/skip behavior before reuse.
2. Add the declaration, its required descriptions, the throw/use site and focused tests in the owning change. Use a stable prefix and preserve the entire field value. The exercise prefix below is temporary and must not be shipped.
3. Review semantics: can an existing code express this same error? If not, explain the differing responsibility, proven failure, parameter contract or actual recovery branch. A different object value, call site, language or log level alone does not justify another code. Automated text similarity is only a review hint; it cannot prove semantic uniqueness.
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
| Human specification review (clarity, consistency and unresolved decisions, separate from execution success) | |

Retain existing baseline manifests and evidence. Refresh only entries affected by a source/artifact change after reviewing why they changed; never regenerate a golden to hide a new violation. Missing source identity, unavailable environments or unrun runtime cases remain explicitly unverified.

## Definition source, generated catalog and runtime diagnostics

The exercise TSV is deliberately small. It is neither an editable registry of all errors nor the final product catalog. Do not edit exported rows to change an error. The maintained source is the declaration in the owning module, together with its documented parameter contract and focused tests. Generated catalogs are reproducible read models; runtime diagnostics contain occurrence-specific values and must not be copied into definition resources.

| Surface | Maintainer action | Consumer action |
|---|---|---|
| Source declaration and use sites | Review and edit in the owning repository | Build-time extraction reads the declaration |
| Generated definition catalog | Regenerate from pinned inputs; never hand-edit rows | Lookup, review and runtime resource loading |
| Diagnostic occurrence | Capture through the owning product adapter | Render the original code and safe contextual values |

A catalog called "complete" must state its coverage: product revision, resolved library artifacts, explicit definition inputs, included connector artifacts and unresolved inputs. It is not a list of every private or historical connector. Missing declarations or a dynamically supplied unknown code stay explicitly unresolved; preserve the raw code rather than invent a definition.

### Concrete source change example

This is an illustrative declaration, not a new namespace allocation or code to ship. The numeric field is the entire value used by the existing library. Its six characters are a fixture choice, not a required width; see the three input/output examples above.

```java
@TapExClass(code = 43, module = "example-connector", prefix = "EXAMPLE")
public final class ExampleErrors {
    @TapExCode(
        describe = "Authentication was rejected",
        dynamicDescription = "Authentication was rejected by {}",
        solution = "Verify the configured credentials and authentication method",
        recoverable = false,
        skippable = false)
    public static final String AUTHENTICATION_REJECTED = "430001";
}
```

Before adding it, search the owning source tree for `430001`, authentication failures, and equivalent credential errors. For a repository that has `rg`, these are executable read-only searches:

```sh
rg -n --glob '*.java' '@TapExClass|@TapExCode' .
rg -n -i --glob '*.java' '430001|authentication|credential' .
```

These searches discover candidates; they do not establish cross-repository uniqueness. Review the existing use sites. If an existing declaration has the same trigger, meaning and parameter contract, reuse it. If the new failure is a timeout rather than rejection, explain that distinction rather than giving the same error a second code. Do not pass credentials as parameters. For the example, document parameter 0 as a sanitized endpoint label and test that order at the throw/capture boundary. Existing positional `{}` templates and positional values remain authoritative; a prose name does not introduce a new named-parameter API.

To change wording, retain meaning and parameter semantics, update available localized descriptions and test formatting. A material change of meaning requires a distinct reviewed definition; do not silently reinterpret an old code. To deprecate, mark the declaration and retain it for old diagnostics. A replacement relationship or first-version claim must come from reviewed metadata/history; it must not be guessed from `@Deprecated` or Git's current tip.

### Intended catalog reading view

The following fields describe the product-maintenance reading view. They are not a published JSON schema, a wire-format commitment or a command already delivered by this library.

| Information | Example | Evidence and missing-value rule |
|---|---|---|
| Full code and raw field | `EXAMPLE430001`, `430001` | Existing full-code calculation; preserve observed bytes |
| Prefix and module metadata | `EXAMPLE`, `43`, `example-connector` | Read the class annotation; do not infer ownership from these values |
| Declaration | `ExampleErrors.AUTHENTICATION_REJECTED` | Source or explicit artifact declaration; include its location when known |
| Descriptions and solutions | Authentication rejected; verify credentials | Extract available annotation text; absent locale is missing, not a translation |
| Parameter contract | Positional template plus documented position 0 | The template alone does not prove a semantic name or type; mark undocumented positions |
| Recovery and skip metadata | `false`, `false` | Preserve declared values; metadata is not permission to introduce automatic retry/skip |
| Severity, type and related exception | `NORMAL`, `RUNTIME`, `RuntimeException` defaults in this example | Extract explicitly; do not assume the old scanner copies every annotation member |
| Deprecation and replacement | Not marked deprecated; no replacement record in this example | Read the actual field annotation; retain deprecated codes; replacement requires separate evidence |
| Build provenance | Product revision, resolved GAV and content hash | Generate from the exact inputs; unknown hash is unverified, not trusted provenance |
| Resolution domain | A product catalog or a registered plugin domain | Bind to trusted registration/artifact context; a prefix or self-reported ID is insufficient |
| Coverage and unresolved inputs | Listed inputs and missing declarations | Report the gap; never label a partial catalog as globally complete |

The existing explicit scanner omits class-level module metadata, field deprecation and some annotation members from its output. A consuming build tool must read the required declarations explicitly instead of fabricating those values from an incomplete `ErrorCodeEntity`. Runtime reflection-based scanning is not required by this guide.

A useful query result shows one resolved definition with its source, parameter contract and compatibility status. If two independently isolated plugin domains contain the same full code, a query must include a verified domain. If that identity is absent, return ambiguity with the raw code; do not choose the first definition. Within one domain, conflicting full-code declarations are rejected before overwrite. These are consuming-product responsibilities, not new restrictions on connector numbering.

### Pull request review and activation

| Check | Mechanical evidence | Human decision |
|---|---|---|
| Exact identity conflicts | Pre-overwrite full-code checks within the actual resolution domain | Confirm any isolated-domain explanation is real |
| Equivalent meanings | Search results and nearby definitions | Explain reuse or why the new trigger/recovery semantics differ |
| Parameter compatibility | Formatting and use-site tests | Confirm positions, meaning and safe values |
| Existing-code preservation | Reviewed baseline diff tied to input revisions | Approve each change; never refresh the entire baseline to hide violations |
| Definition completeness | Required metadata and catalog/input matching | Resolve genuinely missing source facts without guessing |
| Deprecation | Deprecated declarations remain visible while included in the inputs; removed inputs still require trusted published-baseline non-reuse checks | Confirm replacement/migration guidance and backward readability |

Suggested PR evidence: changed declarations and use sites; reuse search and rationale; catalog diff with coverage/provenance where the product catalog is active; positive/negative tests; compatibility impact; and reviewer conclusion. A text-similarity check cannot replace the meaning review.

The current library contribution provides contract tests and this guide. It does not deliver a production-wide catalog command, a semantic duplicate detector, or repository-wide admission gates. Consuming products activate their own generated catalog and strict checks with the actual migrated paths. Unmigrated paths and unrelated feature/bug-fix work retain their existing contracts. Changes in business definitions do not automatically require a shared-library release; changing a definition owned by the library itself still follows that library's ordinary release process.


## Counterexamples to review before accepting the specification

The implementer runs commands and supplies real, revision-bound evidence. Human review judges the rules and counterexamples: clear terminology, complete responsibility boundaries, compatibility and consistent failure handling. A human need not operate the command line, and passing commands does not approve a specification. Earlier user-run evidence stays valid. The present contribution does not require approval of the usability of future product catalog tools.

| Counterexample | Required interpretation |
|---|---|
| Identical declaration and artifact, different JVM default locales | The old library uppercases prefixes using the default locale. Record the catalog generation environment and test matching against supported runtime environments. Preserve an observed raw code; do not rewrite it using a new normalization algorithm. An unmatched definition remains unresolved. |
| Two positional parameters are swapped but counts still match | For managed definitions document semantics, type and requiredness at each position. Use distinct sentinel values to test call sites and all language templates. Missing legacy documentation is unknown, not inferred from text. This requires no new typed-parameter API. |
| An old diagnostic is read after a connector upgrade with the same ID or GAV | Resolve against the verified source/artifact binding at occurrence time, not the latest registration. If that binding cannot be verified or the matching definition is unavailable, retain the raw code and available safe facts, marking provenance unverified or ambiguous. This does not require a new history store. |
| A deprecated code vanishes from the current build inputs | Current catalog absence does not free a published identity in the same resolution domain. Compare with trusted supported-release baselines; an unavailable baseline means unverified compatibility. A replacement hint must not rewrite old occurrences. |
| Two branches independently add the same full code | Revalidate against the current merge target under the existing review and integration procedure. An old green check is not evidence about the combined catalog; no allocation service is introduced. |
| An old external definition lacks metadata required for a new managed definition | Apply strict new-definition rules to the activated managed scope. Report missing external information without renumbering or inventing it. This does not permit conflicting definitions in one domain or guessing a source. |

The PR evidence is stage-specific: this contribution supplies specification review and existing-interface witnesses. The consuming product later supplies its actual generated catalog, checks and usability evidence. Future tools are not a prerequisite for accepting the present specification and existing-interface evidence. None of these clarifications changes the shared-library implementation or automatically expands supported environments.
