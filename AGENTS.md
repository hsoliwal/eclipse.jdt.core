# Eclipse JDT Core agent instructions

## Clean output rule (owner directive, 2026-11-14) - read first

This rule overrides any older text in this file.

- This fork is clean output: one coherent Eclipse JDT Core repository. M3 work lives
  inside the project's own classes and packages, written the way the project
  writes its code. It is not stamped across folders.
- Do not add `m3/`, `.m3/`, `docs/m3/` or `tools/m3*` folders.
- Do not add any of the following. They belong in `hsoliwal/com.synexia`, the
  convergence workspace:
  - qualification bundles;
  - evidence, receipts and benchmark tables;
  - work orders;
  - donor copies;
  - recipe sources or recipe resources.
- Do not add parallel `M3`-prefixed types or `*.m3` packages where an existing
  class owns the behaviour. Change that class in place, keep it internal unless
  the owner admits new API, and follow the project's naming.
- A pull request here contains only the product change, plus its tests in the
  project's own test layout. Put the proof material in the paired `com.synexia`
  pull request and link to it.
- This fork's merged pull-request history shows the intended direction.



## Priority 0 — repository-wide full-history additive superset merge rule (2026-11-18)

For every merge, preserve the historically accepted **backward-compatible optimized superset**: public API/ABI, observable behavior, cross-platform capability, native/ownership contracts, tests, bug fixes, proven algorithms and performance. Evaluate all incumbent, donor, branch and PR candidates, select the best *validated* design, allow additive API extensions, and prohibit silent regression or lost code. Inspect ancestry to the **earliest reachable commits**, relevant heads/tags, merged/closed/reverted PRs, branch/merge bases, both merge parents, renames, deletions and abandoned optimizations; never rebase `develop`. Before accepting a merge, checkpoint per ref the last checked date, exact SHA, history/PR/branch coverage, earliest ancestry reached, last verified anchor and detected losses; never label a HEAD-only sample as full-history PASS. Use existing Synexia history-loss and Maven/OpenRewrite recipe gates, with clean postimage-only public receiving repos where applicable; do not add recipe files/evidence crates to product forks. Prefer first-party Apache-2.0 and compatible Apache/permissive donors, but preserve upstream original licenses, notices and attribution: algorithm ideas can be reimplemented independently; LLM rewriting does not erase copyright or derivative-license obligations. Canonical policy and explicitly **PENDING** 236-repo registry: [com.synexia PR #18764](https://github.com/hsoliwal/com.synexia/pull/18764).



## Superset merge and history-audit rule (2026-11-18)

Read [.github/MERGE_RULE.md](.github/MERGE_RULE.md) before changing an existing implementation or integrating any branch or PR.
Preserve the supported additive API/behavior superset and select implementations by correctness, predictability, measured performance, then elegance.
Audit all reachable history back to every root, all branches/tags and all PR states for lost code or regressions; preserve append-only history.
Record actual UTC coverage and remaining work in [.github/HISTORY_AUDIT.tsv](.github/HISTORY_AUDIT.tsv); policy installation is not a completed source-history audit.
Prefer verified Apache-2.0 sources and independent implementations while preserving all existing third-party rights; LLM rewriting does not remove license obligations.
Detailed private recipe/proof records remain in canonical Synexia; the compact local policy and checkpoint make the rule discoverable here.
<!-- MERGE-SUPERSET-HISTORY-20261118-2 -->

## Additive historical-convergence owner directive (2026-11-18)

Before a merge, loss repair or API enhancement, survey all accessible commits to history roots, all branches/tags, and paginated accepted/rejected/closed/merged PRs and head/merge refs. Compare actual pre/post class/member/atom source and tests: a merged PR or ancestry alone is not delivered code. Identify and restore lost working APIs, behavior and measured optimizations; never erase history to make a bad commit disappear.

Choose the strongest *compatible* correct algorithms and atoms from every qualifying revision; the outcome is an additive, backward-compatible superset. Preserve source/binary/reflective behavior, callbacks, concurrency, native lifetime, resource budgets and regression tests for this toolkit. All reusable atomization/patternization and Java/Maven/OpenRewrite recipes remain canonically in `hsoliwal/com.synexia`; this product fork receives only legitimate code/test postimages.

Prefer Apache-2.0 for separately original eligible contributions; preserve original upstream and per-file licenses (EPL/GPL/Apache/other), NOTICE, provenance and license exceptions. Algorithmic ideas may be independently reimplemented from lawful specifications, but LLM rewriting copyrighted code does not waive its rights or relevant patents.

Central last-checked checkpoint: `hsoliwal/com.synexia/docs/M3-SCALE/invariants/SYNEXIA-HISTORY-REVIEW-LAST-CHECKED.tsv`. Record exact checked SHA/date and root/ref/PR coverage; label unverified/lost-source reviews PARTIAL. Do not store history audit bundles in this clean product repository.

## MOP SDK — documented semantic atoms and machine-classified patterns (2026-11-18)

Before any source-changing task, follow the [canonical Synexia atom/pattern law](https://github.com/hsoliwal/com.synexia/blob/develop/docs/M3-SCALE/invariants/SYNEXIA-UNIVERSAL-API-ATOM-PATTERN-EVOLUTION-1.md) and [MOP SDK lattice proposal](https://github.com/hsoliwal/com.synexia/pull/18784). Descend repository -> project -> module -> package -> file -> declarations -> meaningful behavioral atoms; preserve original API/ABI, identity, effects, ordering, resource, native, serialization and concurrency contracts. Recognize existing algorithms/patterns by versioned catalogue ID, typed roles, applicability and proof. Where no qualified match exists, retain the code and register `UNKNOWN_ALGORITHM`, `UNKNOWN_PATTERN` or `UNPROVEN_MATCH` with source pin, owner/atom, effects, dependencies, rationale and evidence status. Link this typed residue from source-bound documentation or an **already supported** annotation; never create an uncompilable marker or invent an admitted pattern. Recompose upward through the actual affected dependency closure, with parallel independent file analysis and serial proof-gated canonical mutation. Preserve best-qualified additive history and source/provenance; pattern labels, hashes and LLM output are never equivalence proof. Canonical recipe, catalogue, evidence and correction memory stay in Synexia; clean product targets receive only qualified postimages. **Policy binding only**: full history, compiler, recipe, test, runtime and pattern coverage are NOT_RUN unless proven separately.
