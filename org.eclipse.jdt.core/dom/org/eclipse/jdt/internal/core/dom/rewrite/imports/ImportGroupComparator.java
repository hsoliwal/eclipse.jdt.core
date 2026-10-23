/*******************************************************************************
 * Copyright (c) 2015 Google Inc and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     John Glassmyer <jogl@google.com> - import group sorting is broken - https://bugs.eclipse.org/430303
 *     Lars Vogel <Lars.Vogel@vogella.com> - Contributions for
 *     						Bug 473178
 *******************************************************************************/
package org.eclipse.jdt.internal.core.dom.rewrite.imports;

import java.util.*;

/**
 * Sorts imports according to the order of import groups defined on the Organize Imports preference
 * page. Considers equal any two imports matching the same import group.
 */
final class ImportGroupComparator implements Comparator<ImportName>{
	private static final class ImportGroup {
		private final String name;
		private final int index;
		private final ImportGroup prefix;

		public ImportGroup(String name, int index, ImportGroup prefix) {
			this.name = name;
			this.index = index;
			this.prefix = prefix;
		}

		@Override
		public String toString() {
			return String.format("ImportGroup(%d:%s)", getIndex(), getName()); //$NON-NLS-1$ //FLLDCIM: MARK — M11 FORMAT_PRECOMPUTE CALL
		}

		String getName() {
			return this.name;
		}

		int getIndex() {
			return this.index;
		}

		ImportGroup getPrefix() {
			return this.prefix;
		}
	}

	private static final class IndexedImportGroups {
		final NavigableMap<String, ImportGroup> typeImportGroupsByName;
		final NavigableMap<String, ImportGroup> staticImportGroupByName;

		IndexedImportGroups(
				NavigableMap<String, ImportGroup> typeImportGroupsByName,
				NavigableMap<String, ImportGroup> staticImportGroupsByName) {
			this.typeImportGroupsByName = typeImportGroupsByName;
			this.staticImportGroupByName = staticImportGroupsByName;
		}
	}

	private static final String MATCH_ALL = ""; //$NON-NLS-1$
	private static final String STATIC_PREFIX = "#"; //$NON-NLS-1$
	private static final String STATIC_MATCH_ALL = STATIC_PREFIX + MATCH_ALL; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT

	private static List<String> memoizedImportOrder = null;
	private static IndexedImportGroups memoizedIndexedImportGroups = null;

	private static List<String> includeMatchAllImportGroups(List<String> importOrder) { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		boolean needsTypeMatchAll = !importOrder.contains(MATCH_ALL);
		boolean needsStaticMatchAll = !importOrder.contains(STATIC_MATCH_ALL);

		if (!needsTypeMatchAll && !needsStaticMatchAll) {
			return importOrder;
		}

		List<String> augmentedOrder = new ArrayList<>(importOrder.size() + 2); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW

		if (needsStaticMatchAll) {
			augmentedOrder.add(STATIC_MATCH_ALL);
		}

		augmentedOrder.addAll(importOrder);

		if (needsTypeMatchAll) {
			augmentedOrder.add(MATCH_ALL);
		}

		return augmentedOrder;
	}

	private static synchronized IndexedImportGroups indexImportOrder(List<String> importOrder) { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		if (importOrder.equals(memoizedImportOrder)) {
			return memoizedIndexedImportGroups;
		}

		Map<String, Integer> typeGroupsAndIndices = new HashMap<>(); //FLLDCIM: MARK — M01 M3_HASH_LANES NEW //FLLDCIM: MARK — M10 M3_PRIMITIVE_LANES LOCAL_VARIABLE
		Map<String, Integer> staticGroupsAndIndices = new HashMap<>(); //FLLDCIM: MARK — M01 M3_HASH_LANES NEW //FLLDCIM: MARK — M10 M3_PRIMITIVE_LANES LOCAL_VARIABLE
		for (int i = 0; i < importOrder.size(); i++) { //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
			String importGroupString = importOrder.get(i);

			final Map<String, Integer> groupsAndIndices; //FLLDCIM: MARK — M10 M3_PRIMITIVE_LANES LOCAL_VARIABLE
			if (importGroupString.startsWith(STATIC_PREFIX)) {
				groupsAndIndices = staticGroupsAndIndices;
				importGroupString = importGroupString.substring(1);
			} else {
				groupsAndIndices = typeGroupsAndIndices;
			}

			groupsAndIndices.put(importGroupString, i);
		}

		memoizedImportOrder = importOrder;

		memoizedIndexedImportGroups = new IndexedImportGroups(
				mapImportGroups(typeGroupsAndIndices),
				mapImportGroups(staticGroupsAndIndices));

		return memoizedIndexedImportGroups;
	}

	private static NavigableMap<String, ImportGroup> mapImportGroups(Map<String, Integer> importGroupNamesAndIndices) { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD //FLLDCIM: MARK — M10 M3_PRIMITIVE_LANES PARAMETER
		if (importGroupNamesAndIndices.isEmpty()) {
			importGroupNamesAndIndices = Collections.singletonMap(MATCH_ALL, 0);
		}

		List<String> sortedNames = new ArrayList<>(importGroupNamesAndIndices.keySet()); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW
		Collections.sort(sortedNames); //FLLDCIM: MARK — M16 SORT_LANE CALL

		ArrayList<ImportGroup> importGroups = new ArrayList<>(sortedNames.size()); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW

		Deque<ImportGroup> prefixingGroups = new ArrayDeque<>(); //FLLDCIM: MARK — M08 M3_DEQUE_LANES NEW
		for (String name : sortedNames) { //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
			while (!prefixingGroups.isEmpty() //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
					&& !isWholeSegmentPrefix(prefixingGroups.getLast().getName(), name)) {
				prefixingGroups.removeLast();
			}
			ImportGroup prefix = prefixingGroups.peekLast();

			ImportGroup group = new ImportGroup(name, importGroupNamesAndIndices.get(name), prefix);

			importGroups.add(group);

			prefixingGroups.addLast(group);
		}

		NavigableMap<String, ImportGroup> groupsByName = new TreeMap<>(); //FLLDCIM: MARK — M05 M3_SORTED_LANES NEW
		for (ImportGroup group : importGroups) { //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
			groupsByName.put(group.getName(), group);
		}

		return groupsByName;
	}

	private static boolean isWholeSegmentPrefix(String prefix, String name) {
		if (!name.startsWith(prefix)) {
			return false;
		}

		return prefix.isEmpty() || name.length() == prefix.length() || name.charAt(prefix.length()) == '.';
	}

	private final IndexedImportGroups indexedImportGroups;

	ImportGroupComparator(List<String> importOrder) {
		List<String> importOrderWithMatchAllGroups = includeMatchAllImportGroups(importOrder);
		this.indexedImportGroups = indexImportOrder(importOrderWithMatchAllGroups);
	}

	@Override
	public int compare(ImportName o1, ImportName o2) {
		return determineSortPosition(o1) - determineSortPosition(o2);
	}

	private int determineSortPosition(ImportName importName) {
		String name = (importName.isOnDemand() ? importName.containerName : importName.qualifiedName);

		NavigableMap<String, ImportGroup> groupsByName = importName.isStatic
				? this.indexedImportGroups.staticImportGroupByName
						: this.indexedImportGroups.typeImportGroupsByName;

		ImportGroup prefixingGroup = groupsByName.floorEntry(name).getValue();
		while (!isWholeSegmentPrefix(prefixingGroup.getName(), name)) { //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
			prefixingGroup = prefixingGroup.getPrefix();
		}

		return prefixingGroup.getIndex();
	}
}
