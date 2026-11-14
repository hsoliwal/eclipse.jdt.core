#!/usr/bin/env bash
# Copyright (c) 2026 Synexia, Hitesh Soliwal (hsoliwal@gmail.com) and others.
# SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
#
# Tycho builds of the listed forks, one after another (fork_stages.sh <repo> build; each build has its own free-space
# HOLD in build_fork.sh). Made to be started outside the agent session, so that a session restart does not kill a
# 30-60 minute build half way (it did, twice, on 2026-11-13):
#   powershell: Invoke-CimMethod -ClassName Win32_Process -MethodName Create -Arguments @{ CommandLine =
#     '"C:\Program Files\Git\usr\bin\bash.exe" -lc "bash /f/m3-scratch/jdtfix/tools/builds_chain.sh <repo>..."' }
# Stop it: kill the PIDs in out/logs/builds_chain.pid (bash) and the java.exe whose command line holds jdtfix.
# Usage: builds_chain.sh <repo>...   -> out/logs/builds_chain.log (one line per start/end), out/build/<repo>.*
set -uo pipefail
J=/f/m3-scratch/jdtfix; L=$J/out/logs/builds_chain.log; echo $$ > "$J/out/logs/builds_chain.pid"
for R in "$@"; do
	echo "$(date +%T) chain start $R (bash pid $$)" >> "$L"
	bash "$J/tools/fork_stages.sh" "$R" build >> "$L" 2>&1
	echo "$(date +%T) chain end $R rc=$?" >> "$L"
done
echo "$(date +%T) chain done [$*]" >> "$L"
