# Upstream sync 2026-11-14 (resolves #20)

Merge of upstream `eclipse-jdt/eclipse.jdt.core` master `dd2c87485ce2f01b4fc88167f1db701dd6540b35` (the head of
PR #20, 22 commits) into fork master `dd4fbdf1126d51fb407119d607c65a46596cad63`; merge base
`71be896c27d29385509cb2a59ce452a05a57fc97`; merge commit `7f213b571f06ea56da7f3a13e3fa490d6ba402f7`.

## Conflicts

4 files, 5 hunks; rows in [CONFLICTS.tsv](CONFLICTS.tsv). Every hunk has the same shape: the fork side differs
from the merge base only by a CNPCDA anchor (`//FLLDCIM: ...`) at the end of a line that upstream changed or
inserted code in front of. The resolution takes the upstream text and keeps the fork anchor on its line.

The resolution is a recipe, [merge_anchor_resolve.py](merge_anchor_resolve.py), not a hand edit. Per hunk it
requires `fork line minus anchor == base line` for every line, then emits the upstream lines and moves each
anchor along the base-to-upstream line diff: unchanged line, same line; replaced line, the line at the same
distance from the end of the replacing block; deleted line, the anchor stays as its own comment line. A hunk
where the fork changed more than anchors is reported `MANUAL` and the file is left untouched.

Checks on the four files after the recipe:

| check | result |
|---|---|
| conflict markers | 0 |
| anchors per file, compared with fork master (sorted multiset) | identical: 99, 361, 645, 155 |
| file minus anchors, compared with upstream | byte-identical for InterfaceMethodsTest, CompletionTests18, CompletionEngine |
| CommentsPreparator minus anchors, compared with upstream | differs by exactly the fork's 10 regex-hoist lines (`af49a6aac9`), which merged without conflict |

## Gates

Rows in [GATES.tsv](GATES.tsv); all on the merge commit, JDK 21 target.

- Java-21 recipe (`apply_jdk21.sh`, `apply_jdk21_pins.sh`): 0 files changed. The 22 upstream commits do not move
  a bundle or a pin above Java 21 in this fork; the fork's Java 21 values survive the merge.
- Java-21 gate: residue 0. `--release 25` and `--release 21` each report 40,383 rows over 2,252 files (27,950
  `cannot find symbol`, 32,737 of all rows in `org.eclipse.jdt.core.tests.model`). The rows are the same at both
  levels except 13 follow-up rows in the BOM file `SwitchTest.java` (same lines, different javac wording), which
  the gate sets aside. Fork master `dd4fbdf1126d` has 40,305 such rows at both levels and its Tycho build passes,
  so the rows are this gate's baseline, not a Java-level break.
- Parse gate: 2,900 files, 0 syntax errors.
- Metadata check: 0 rows.
- ECJ pre-check: 0 missing NLS tags, 0 surplus NLS tags, 0 unused imports.
- Tycho build on JDK 21 (`build_fork.sh`, tests skipped): running when this commit was made; BUILD.tsv is added with the result.

## Tools in this directory

| file | purpose |
|---|---|
| `merge_anchor_resolve.py` | the conflict recipe above |
| `fork_stages.sh` | gate, parse, metadata, ECJ pre-check and build of one fork in order; stops at the first stage with rows |
| `builds_chain.sh`, `chain_wait.sh` | run the Tycho builds outside the agent session and read one heartbeat |
| `build_fork.sh` | the 2026-11-09 build script with the free-space check rounded down (`df -BG` rounds up) |
| `anchor_scan.sh` | CNPCDA placement scan (Javadoc, text block, string literal) without `grep -P` in the string check |
