#!/usr/bin/env bash
# Copyright (c) 2026 Synexia, Hitesh Soliwal (hsoliwal@gmail.com) and others.
# SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
#
# One heartbeat of the detached build chain (builds_chain.sh): waits until out/logs/builds_chain.log gets a new line or
# WAIT seconds pass (default 235), then prints one line: last chain line, module counter and [ERROR] rows of the fork
# being built, free MB on F:. Cheap: tail/grep/df only. Usage: chain_wait.sh [wait-seconds]
set -uo pipefail
J=/f/m3-scratch/jdtfix; C=$J/out/logs/builds_chain.log; W=${1:-235}; n0=$(wc -l < "$C" 2>/dev/null); n0=${n0:-0}; end=$((SECONDS + W))
until [ "$(wc -l < "$C" 2>/dev/null)" != "$n0" ] || [ "$SECONDS" -ge "$end" ]; do sleep 15; done
c=$(tail -1 "$C" 2>/dev/null | cut -c1-100)
case "$c" in *"start eclipse.jdt.core"*|*"end eclipse.jdt.core"*|*"chain done"*) R=eclipse.jdt.core ;; *) R=eclipse.jdt.ui ;; esac
L=$J/out/build/$R.log
b=$(grep -a -E '^\[INFO\] Building .*\[[0-9]+/[0-9]+\]' "$L" 2>/dev/null | tail -1 | grep -o '\[[0-9]*/[0-9]*\]')
e=$(grep -a -c '^\[ERROR\]' "$L" 2>/dev/null); r=$(grep -a -o -E 'BUILD (SUCCESS|FAILURE)' "$L" 2>/dev/null | tail -1)
echo "$(date +%T) chain: ${c:-none}; $R module ${b:-?}, [ERROR] rows ${e:-?} ${r:-running}; F: $(df -BM /f | awk 'NR==2 { print $4 }')"
