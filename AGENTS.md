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


## MOP SDK cross-repository agent read gate (2026-11-18)
<!-- MOP-AGENT-2026-11-18-1 -->
Before any source analysis, refactor, PR, merge, licensing decision, or history checkpoint, also read [AGENT.md](AGENT.md). It governs bidirectional atomization/patternization, documented contract-sealed atoms, additive best-of-best API supersets, complete available-history loss checks, dated last-verified proof, Apache-2.0 eligibility and upstream rights. Existing stronger project-specific and locked-canon constraints remain in force. Installation of this pointer does not establish that a source or historical audit has run.
