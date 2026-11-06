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
 *******************************************************************************/
package org.eclipse.jdt.internal.core.dom.rewrite.imports;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Reassigns comments associated with removed imports (those present before but not present
 * after the rewrite) to resultant imports (those present after the rewrite).
 * <p>
 * Reassigns comments of removed single imports to the first (in iteration order) resultant
 * on-demand import having the same container name, if one exists.
 * <p>
 * Reassigns comments of removed on-demand imports to the first (in iteration order) resultant
 * single import having the same container name, if one exists.
 * <p>
 * Leaves unassigned any removed import comment not matching the above cases.
 */
final class RemovedImportCommentReassigner {
	private static Collection<OriginalImportEntry> retainImportsWithComments(Collection<OriginalImportEntry> imports) {
		Collection<OriginalImportEntry> importsWithComments = new ArrayList<>(imports.size()); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW
		for (OriginalImportEntry currentImport : imports) { //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
			if (!currentImport.comments.isEmpty()) {
				importsWithComments.add(currentImport);
			}
		}

		return importsWithComments;
	}

	private static boolean hasFloatingComment(OriginalImportEntry nextAssignedImport) {
		for (ImportComment importComment : nextAssignedImport.comments) { //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
			if (importComment.succeedingLineDelimiters > 1) {
				return true;
			}
		}

		return false;
	}

	private final Collection<OriginalImportEntry> originalImportsWithComments;

	RemovedImportCommentReassigner(List<OriginalImportEntry> originalImports) {
		this.originalImportsWithComments = retainImportsWithComments(originalImports);
	}

	/**
	 * Assigns comments of removed import entries (those in {@code originalImports} but not in
	 * {@code resultantImports}) to resultant import entries.
	 * <p>
	 * Returns a map containing the resulting assignments, where each key is an element of
	 * {@code resultantImports} and each value is a collection of comments reassigned to that
	 * resultant import.
	 */
	Map<ImportEntry, Collection<ImportComment>> reassignComments(Collection<ImportEntry> resultantImports) { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		Map<ImportEntry, Collection<OriginalImportEntry>> importAssignments = assignRemovedImports(resultantImports);

		Map<ImportEntry, Collection<ImportComment>> commentAssignments =
				new HashMap<>(); //FLLDCIM: MARK — M01 M3_HASH_LANES NEW

		for (Map.Entry<ImportEntry, Collection<OriginalImportEntry>> importAssignment : importAssignments.entrySet()) { //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
			ImportEntry targetImport = importAssignment.getKey();
			if (targetImport != null) {
				Deque<ImportComment> assignedComments = new ArrayDeque<>(); //FLLDCIM: MARK — M08 M3_DEQUE_LANES NEW

				Collection<OriginalImportEntry> assignedImports = importAssignment.getValue();

				Iterator<OriginalImportEntry> nextAssignedImportIterator = assignedImports.iterator();
				if (nextAssignedImportIterator.hasNext()) {
					nextAssignedImportIterator.next();
				}

				for (OriginalImportEntry currentAssignedImport : assignedImports) { //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
					OriginalImportEntry nextAssignedImport =
							nextAssignedImportIterator.hasNext() ? nextAssignedImportIterator.next() : null;

					assignedComments.addAll(currentAssignedImport.comments);

					if (nextAssignedImport != null && hasFloatingComment(nextAssignedImport)) {
						// Ensure that a blank line separates this removed import's comments
						// from the next removed import's floating comments.
						ImportComment lastComment = assignedComments.removeLast();
						ImportComment lastCommentWithTrailingBlankLine = new ImportComment(lastComment.region, 2);
						assignedComments.add(lastCommentWithTrailingBlankLine);
					}
				}

				commentAssignments.put(targetImport, assignedComments);
			}
		}

		return commentAssignments;
	}

	private Map<ImportEntry, Collection<OriginalImportEntry>> assignRemovedImports(Collection<ImportEntry> imports) { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		Collection<OriginalImportEntry> removedImportsWithComments = identifyRemovedImportsWithComments(imports);
		if (removedImportsWithComments.isEmpty()) {
			return Collections.emptyMap();
		}

		Map<ImportName, ImportEntry> firstSingleForOnDemand = identifyFirstSingleForEachOnDemand(imports);
		Map<ImportName, ImportEntry> firstOccurrences = identifyFirstOccurrenceOfEachImportName(imports);

		Map<ImportEntry, Collection<OriginalImportEntry>> removedImportsForRetainedImport =
				new HashMap<>(); //FLLDCIM: MARK — M01 M3_HASH_LANES NEW
		for (ImportEntry retainedImport : imports) { //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
			removedImportsForRetainedImport.put(retainedImport, new ArrayList<>()); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW
		}
		// The null key will map to the removed imports not assigned to any import.
		removedImportsForRetainedImport.put(null, new ArrayList<>()); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW

		for (OriginalImportEntry removedImport : removedImportsWithComments) { //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
			ImportName removedImportName = removedImport.importName;

			final ImportEntry retainedImport;
			if (removedImportName.isOnDemand()) {
				retainedImport = firstSingleForOnDemand.get(removedImportName);
			} else {
				retainedImport = firstOccurrences.get(removedImportName.getContainerOnDemand());
			}

			// retainedImport will be null if there's no corresponding import to which to assign the removed import.
			removedImportsForRetainedImport.get(retainedImport).add(removedImport);
		}

		return removedImportsForRetainedImport;
	}

	private Collection<OriginalImportEntry> identifyRemovedImportsWithComments(Collection<ImportEntry> imports) {
		Collection<OriginalImportEntry> removedImports =
				new ArrayList<>(this.originalImportsWithComments); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW
		removedImports.removeAll(imports);
		return removedImports;
	}

	/**
	 * Assigns each removed on-demand import to the first single import in {@code imports} having
	 * the same container name.
	 * <p>
	 * Returns a map where each key is a single import and each value is the corresponding
	 * removed on-demand import.
	 * <p>
	 * The returned map only contains mappings to removed on-demand imports for which there are
	 * corresponding single imports in {@code imports}.
	 */
	private Map<ImportName, ImportEntry> identifyFirstSingleForEachOnDemand(Iterable<ImportEntry> imports) {
		Map<ImportName, ImportEntry> firstSingleImportForContainer = new HashMap<>(); //FLLDCIM: MARK — M01 M3_HASH_LANES NEW
		for (ImportEntry currentImport : imports) { //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
			if (!currentImport.importName.isOnDemand()) {
				ImportName containerOnDemand = currentImport.importName.getContainerOnDemand();
				if (!firstSingleImportForContainer.containsKey(containerOnDemand)) {
					firstSingleImportForContainer.put(containerOnDemand, currentImport);
				}
			}
		}
		return firstSingleImportForContainer;
	}

	private Map<ImportName, ImportEntry> identifyFirstOccurrenceOfEachImportName(Iterable<ImportEntry> imports) {
		Map<ImportName, ImportEntry> firstOccurrenceOfImport = new HashMap<>(); //FLLDCIM: MARK — M01 M3_HASH_LANES NEW
		for (ImportEntry resultantImport : imports) { //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
			if (!firstOccurrenceOfImport.containsKey(resultantImport.importName)) {
				firstOccurrenceOfImport.put(resultantImport.importName, resultantImport);
			}
		}
		return firstOccurrenceOfImport;
	}
}
