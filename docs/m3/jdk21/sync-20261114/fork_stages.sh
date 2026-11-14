#!/usr/bin/env bash
# Copyright (c) 2026 Synexia, Hitesh Soliwal (hsoliwal@gmail.com) and others.
# SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
#
# One fork, current master, in order: Java-21 gate (gate_master.sh) -> parse gate (parse_gate.sh) -> metadata check
# (meta_check.sh) -> ECJ pre-check (ecj_precheck.sh) -> Tycho build (build_fork.sh). The build starts only when the
# stages before it are clean (residue 0, parse rows 0, ECJ rows 0) and F: has MIN_FREE_GB free; otherwise the script
# stops with the first stage that has rows, so the fix goes to that stage's rows first.
# Usage: fork_stages.sh <repo> [stage...]   stages: gate parse meta ecj build (default: all)
#   -> out/logs/<repo>.stages.log (one line per stage: time, stage, rows, verdict)
set -uo pipefail
R="$1"; shift; ST="${*:-gate parse meta ecj build}"; J=F:/m3-scratch/jdtfix; T=$J/tools; G=$J/out/gate/$R; W=$J/$R
export TEMP=F:/m3-scratch/tmp TMP=F:/m3-scratch/tmp; mkdir -p "$J/out/logs" "$J/out/gate"; L=$J/out/logs/$R.stages.log
say() { echo "$(date +%T) $R $*" | tee -a "$L"; }
for s in $ST; do case "$s" in
	gate) bash "$T/gate_master.sh" "$R" >> "$J/out/logs/$R.gate.run.log" 2>&1; rc=$?
		n=$(wc -l < "$G.residue.tsv" 2>/dev/null); n=${n:-NA}; c=$(wc -l < "$G.recipe.run.tsv" 2>/dev/null); c=${c:-NA}
		say "gate rc=$rc residue=$n recipe_changed=$c"; [ "$rc" = 0 ] && [ "$n" = 0 ] || { say "STOP at gate"; exit 3; } ;;
	parse) bash "$T/parse_gate.sh" "$R" >> "$J/out/logs/$R.gate.run.log" 2>&1; rc=$?
		n=$(wc -l < "$G.parse.tsv" 2>/dev/null); n=${n:-NA}
		say "parse rc=$rc rows=$n"; [ "$n" = 0 ] || { say "STOP at parse"; exit 4; } ;;
	meta) X=""; [ -f "$T/exempt-$R.tsv" ] && X="$T/exempt-$R.tsv"  # intentional Java levels (test input data), as in gate_master.sh
		bash "$T/meta_check.sh" "$W" $X > "$G.meta.tsv" 2>> "$J/out/logs/$R.gate.run.log"; n=$(wc -l < "$G.meta.tsv")
		say "meta rows=$n"; [ "$n" = 0 ] || { say "STOP at meta"; exit 5; } ;;
	ecj) bash "$T/ecj_precheck.sh" "$W" "$G.ecj" >> "$J/out/logs/$R.gate.run.log" 2>&1; n=$(wc -l < "$G.ecj.mvnlog" 2>/dev/null); n=${n:-NA}
		say "ecj rows=$n"; [ "$n" = 0 ] || { say "STOP at ecj"; exit 6; } ;;
	build) bash "$T/build_fork.sh" "$R" >> "$J/out/logs/$R.build.run.log" 2>&1; rc=$?
		say "build rc=$rc $(tail -1 "$J/out/build/$R.result.tsv" | cut -f2-8 | tr '\t' ' ')"; [ "$rc" = 0 ] || { say "STOP at build"; exit 7; } ;;
	*) say "unknown stage $s"; exit 2 ;;
esac; done
say "stages [$ST] done"
