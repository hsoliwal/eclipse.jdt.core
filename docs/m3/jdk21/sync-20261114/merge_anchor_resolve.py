# Copyright (c) 2026 Synexia, Hitesh Soliwal (hsoliwal@gmail.com) and others.
# SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
#
# Merge-conflict recipe for upstream syncs of a fork that carries CNPCDA anchors (`//FLLDCIM: ...` at line ends):
# a conflict hunk whose fork side differs from the merge base ONLY by anchors is resolved as "upstream text, fork
# anchors kept". Per hunk (three stages of the index, `git merge-file --diff3`):
#   precondition  strip_anchor(ours line i) == strip_anchor(base line i) for every line, same line count
#   result        theirs lines; each fork anchor follows its line through the base->theirs line diff:
#                   line unchanged upstream  -> the same line
#                   line replaced upstream   -> the line at the same distance from the end of the replacing block
#                   line deleted upstream    -> the anchor stays as its own comment line at that place (never dropped)
# A hunk that fails the precondition is left for a manual decision: the file is not written and the row says MANUAL.
# Bytes in, bytes out (line endings kept). Usage (inside the worktree, during a merge):
#   python merge_anchor_resolve.py <conflicted-path>...   -> TSV rows: path, hunk, our_line, verdict, detail
import difflib, re, subprocess, sys, tempfile, os

ANCHOR = re.compile(rb'[ \t]*//FLLDCIM:[^\r\n]*')
MARK = (b'<<<<<<< ', b'||||||| ', b'=======', b'>>>>>>> ')


def split(line):
    """(text without anchor and line ending, anchor or b'', line ending)"""
    body = line.rstrip(b'\r\n'); eol = line[len(body):]
    m = ANCHOR.search(body)
    return (body[:m.start()], body[m.start():], eol) if m else (body, b'', eol)


def stage(n, path):
    return subprocess.run(['git', 'show', ':%d:%s' % (n, path)], check=True, capture_output=True).stdout


def resolve_hunk(ours, base, theirs):
    """list of result lines, or None when the fork side is more than base + anchors"""
    if len(ours) != len(base) or any(split(o)[0] != split(b)[0] for o, b in zip(ours, base)):
        return None
    out = list(theirs); extra = {}  # index in theirs -> anchors to append; extra: own-line anchors before index
    codes = difflib.SequenceMatcher(None, [split(b)[0] for b in base], [split(t)[0] for t in theirs], autojunk=False).get_opcodes()
    for i, o in enumerate(ours):
        anchor = split(o)[1]
        if not anchor:
            continue
        for tag, i1, i2, j1, j2 in codes:
            if not i1 <= i < i2:
                continue
            if tag == 'equal':
                j = j1 + (i - i1)
            elif tag == 'replace':
                j = max(j1, j2 - (i2 - i))
            else:  # delete: no upstream line carries it
                indent = re.match(rb'[ \t]*', o).group(0)
                extra.setdefault(j1, []).append(indent + anchor.lstrip() + split(o)[2]); j = None
            if j is not None:
                text, old, eol = split(out[j]); out[j] = text + old + anchor + eol
    res = []
    for j, line in enumerate(out):
        res.extend(extra.get(j, [])); res.append(line)
    res.extend(extra.get(len(out), []))
    return res


def run(path):
    tmp = []
    for n in (2, 1, 3):
        f = tempfile.NamedTemporaryFile(delete=False, suffix='.s%d' % n); f.write(stage(n, path)); f.close(); tmp.append(f.name)
    merged = subprocess.run(['git', 'merge-file', '-p', '--diff3', '-L', 'ours', '-L', 'base', '-L', 'theirs'] + tmp, capture_output=True).stdout
    for t in tmp:
        os.unlink(t)
    out, rows, ok, state, hunk, n_line = [], [], True, None, 0, 0
    ours, base, theirs = [], [], []
    for line in merged.splitlines(keepends=True):
        if state is None:
            if line.startswith(MARK[0]):
                state, ours, base, theirs, hunk = 'o', [], [], [], hunk + 1; start = n_line + 1
            else:
                out.append(line); n_line += 1
        elif state == 'o' and line.startswith(MARK[1]):
            state = 'b'
        elif state == 'b' and line.rstrip(b'\r\n') == MARK[2]:
            state = 't'
        elif state == 't' and line.startswith(MARK[3]):
            state = None; res = resolve_hunk(ours, base, theirs)
            if res is None:
                ok = False; rows.append((path, hunk, start, 'MANUAL', 'fork side is more than base + anchors'))
            else:
                out.extend(res); n_line += len(res)
                rows.append((path, hunk, start, 'RESOLVED', 'ours %d / base %d / theirs %d lines; anchors kept %d'
                             % (len(ours), len(base), len(theirs), sum(1 for o in ours if split(o)[1]))))
        else:
            {'o': ours, 'b': base, 't': theirs}[state].append(line)
    if ok and hunk:
        open(path, 'wb').write(b''.join(out))
    return rows


for p in sys.argv[1:]:
    for r in run(p):
        print('\t'.join(str(x) for x in r))
