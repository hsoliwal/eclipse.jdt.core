#!/usr/bin/env bash
# Copyright (c) 2026 Synexia, Hitesh Soliwal (hsoliwal@gmail.com) and others.
# SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
#
# CNPCDA placement scan (CPU, tracked Java files of the worktree): `//FLLDCIM:` anchors where they break a build or
# change output. Kinds:
#   javadoc    anchor on a Javadoc/block-comment line (` * ... //FLLDCIM:`): javadoc renders it, doclint may reject it
#              (repair: javadoc_anchor.py)
#   textblock  anchor after an opening text-block delimiter: illegal in every Java release (fix_textblock_anchors.sh)
#   string     anchor inside a string literal on the line (code view masks strings): changes runtime text (manual)
# Usage: anchor_scan.sh <worktree> > rows.tsv   (kind TAB path TAB line); empty output = PASS
set -uo pipefail
W="$1"; H=$(cd "$(dirname "$0")" && pwd); export LC_ALL=C
{ git -C "$W" grep -n -P '^\s*\*(?!/).*//FLLDCIM:' -- '*.java' || true; } | awk -F: 'BEGIN{OFS="\t"}{print "javadoc",$1,$2}'
{ git -C "$W" grep -n -P '"""[ \t]*//FLLDCIM:' -- '*.java' || true; } | awk -F: 'BEGIN{OFS="\t"}{print "textblock",$1,$2}'
# string: the line holds an anchor and a double quote before it; confirm on the code view (strings/comments masked).
# One code view and one awk per file (not per line); no `grep -P` here: MSYS grep 3.0 refuses -P under LC_ALL=C
# ("supports only unibyte and UTF-8 locales"), which made every row fall through and the scan run for minutes.
{ git -C "$W" grep -n -P '"[^"\n]*//FLLDCIM:' -- '*.java' || true; } | cut -d: -f1,2 \
	| awk -F: '{ l[$1] = l[$1] " " $2 } END { for (f in l) print f "\t" l[f] }' | sort | while IFS=$'\t' read -r f lines; do
	# the code view blanks strings and comments with columns kept (bytes, LC_ALL=C): an anchor that opens a line
	# comment leaves only blanks after its column; code after it (`");`, ` + x`) means the anchor sat inside a literal.
	# (A quote count is wrong here: a char literal '"' before a real anchor is one quote.)
	awk -v BINMODE=3 -f "$H/code_view.awk" "$W/$f" | awk -v BINMODE=3 -v F="$f" -v L="$lines" '
		BEGIN { n = split(L, a, " "); for (i = 1; i <= n; i++) want[a[i]] = 1 }
		NR == FNR { if (FNR in want) cv[FNR] = $0; next }
		(FNR in want) && $0 !~ /^[ \t]*(\*|\/\/)/ {
			i = index($0, "//FLLDCIM:"); if (substr(cv[FNR], i + 10) ~ /[^ \t\r]/) print "string\t" F "\t" FNR }' \
		- "$W/$f"
done
