# Release readiness for M3JDK21 (Java 21): eclipse.jdt.core

Measured on 2026-11-09 against fork master `f7fdb967a6bd440f818f7eb5c1b614cf48b75fc9` (upstream master `8e0ea72723b5`).
Tools and raw evidence: [`docs/m3/jdk21/release-20261109`](../jdk21/release-20261109/). Every gate ran locally on CPU; fork CI runs are not counted
(startup failures and queue starvation on this account, 2026-11).

| # | Gate | Verdict | Evidence |
| --- | --- | --- | --- |
| G1 | No Java 22+ requirement on master: BREE above JavaSE-21 (0), build/IDE pins above 21 (0), unnamed `_` (0), `import module` (0) | PASS; 23 exempt rows (EXEMPT.tsv, CENSUS_EXEMPT.tsv) | CENSUS.tsv (code view; hits inside strings, text blocks and comments: CENSUS_MASKED.tsv) |
| G2 | Java-21 compile gate: JDK 25 javac `--release 21` minus `--release 25` over 13 bundles (2251 files, 21 roots); 40305 baseline rows cancel | FAIL on master (residue 2, GATE_RESIDUE_MASTER.tsv); PASS with this PR (residue 0) | GATE_RESIDUE.tsv, GATE_SUMMARY.tsv |
| G2b | Parse gate: every tracked Java source (2899 files, test bundles included, test-input folders excluded), JDK 25 javac `--release 21` stopped after parsing | FAIL on master (238 rows, PARSE_GATE_MASTER.tsv); PASS with this PR | PARSE_GATE.tsv |
| G3 | Recipe fixed point (`apply_jdk21.sh` + `apply_jdk21_pins.sh` change nothing on master) | PASS | RECIPE_CHANGES.tsv |
| G4 | Metadata consistent: Tycho bundle/feature versions match their pom, no EE above 21 in a Java-21 bundle | PASS | META.tsv |
| G5 | Full Tycho build of this repository on JDK 21 (`mvn -B clean verify -DskipTests`, fork parent + SDK target from the fork aggregator, other components from the upstream 4.42 I-build repository) | PASS | BUILD SUCCESS in 236 s, modules 17/17 ok, 0 failed, 0 [ERROR] rows (BUILD_G5.tsv, BUILD_G5_ERRORS.tsv, BUILD_G5.log.gz) |
| G6 | Eclipse SDK product build and launch on JDK 21 | PENDING | aggregator verdict, eclipse.platform.releng.aggregator docs/m3/release/RELEASE_READINESS.md |

## What this PR changes

Fork master `f7fdb967a6bd` carries the 2026-10-20 delta (13 bundles at JavaSE-21, no unnamed `_` or `import module` in
code). One break remains: an M3 marks pass wrote 32 `//FLLDCIM:` anchors after opening text-block delimiters
(`""" //FLLDCIM: ...`) in five org.eclipse.jdt.core.tests.compiler sources (ComplianceDiagnoseTest, CompilerInvocationTests,
PatternMatching16Test, PrimitiveInPatternsTestSH, XLargeTest). javac rejects them in every Java release ("illegal text block
open delimiter sequence, missing line terminator"): PARSE_GATE_MASTER.tsv (238 rows); the 2-row master residue
(GATE_RESIDUE_MASTER.tsv) is parser recovery after that error.

Fill (`fix_textblock_anchors.sh` + `textblock_anchor.awk`, CNPCDA-safe): each anchor moves unchanged to its own line above
the statement; nothing else changes. Because the master baseline could not parse those five files, the `--release 25`
baseline of this PR is taken on the repaired tree (the repair is language-neutral); on master the same baseline had
6232 rows because those five files stopped at the parser.

Exempt (EXEMPT.tsv, CENSUS_EXEMPT.tsv): `JCL/*` (compiler-test class libraries built at Java 22..26 on purpose) and
`*/workspace/*` (test workspaces). SwitchTest.java starts with a UTF-8 BOM (as upstream): javac rejects it, ECJ (Tycho)
accepts it; the parse gate sets BOM files aside.

## G5 (Tycho build on JDK 21.0.9)

ECJ pre-check first (`ecj_precheck.sh`: 9 bundles that compile NLS or unused imports as errors): 0 rows.

| Attempt | Result |
| --- | --- |
| 1 | BUILD SUCCESS: 17/17 modules in 236 s, target/ 107 MB |

The 27 surplus `$NON-NLS$` tags the regex/strings lanes left in jdt.core (warnings here, no bundle compiles NLS as an
error) are not changed in this PR; they belong to the recipe fix (M3HoistConstantStringRegexCallsRecipe,
M3LinearStringBuilderRecipe).

## Re-run

```bash
git clone --depth 1 https://github.com/hsoliwal/eclipse.jdt.core.git && git -C eclipse.jdt.core remote add upstream <upstream>
git -C eclipse.jdt.core fetch --depth 1 --filter=blob:none upstream master:refs/remotes/upstream/master
bash docs/m3/jdk21/release-20261109/gate_master.sh eclipse.jdt.core    # paths at the top of the script; JAVAC = JDK 25 javac
bash docs/m3/jdk21/release-20261109/meta_check.sh eclipse.jdt.core > META.tsv
```

The gate compiles every bundle that upstream master declares JavaSE-22 or newer, plus the 2026-10-20 list, against the
newest jar of every bundle in a p2 pool. Errors in both runs are classpath noise (APIs newer than the pool); what remains
after `--release 21` is the Java 22+ residue and must be empty.
