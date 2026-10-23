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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.TypeNameRequestor;

final class TypeConflictingSimpleNameFinder implements ConflictingSimpleNameFinder {
	private static class ConflictAccumulatingTypeRequestor extends TypeNameRequestor {
		private static String buildContainerName(char[] packageName, char[][] enclosingTypeNames) {
			StringBuilder buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
			buf.append(packageName); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			for (char[] enclosingTypeName : enclosingTypeNames) { //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
				if (buf.length() > 0)
					buf.append('.'); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				buf.append(enclosingTypeName); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			}
			return buf.toString();
		}

		private final Set<String> namesFoundInMultipleContainers;
		private final Map<String, String> lastContainerFoundForType;

		ConflictAccumulatingTypeRequestor() {
			this.namesFoundInMultipleContainers = new HashSet<>(); //FLLDCIM: MARK — M03 M3_SET_LANES NEW
			this.lastContainerFoundForType = new HashMap<>(); //FLLDCIM: MARK — M01 M3_HASH_LANES NEW
		}

		@Override
		public void acceptType(
				int modifiers,
				char[] packageName,
				char[] simpleTypeName,
				char[][] enclosingTypeNames,
				String path) {
			String simpleName = new String(simpleTypeName);
			String newContainerName = buildContainerName(packageName, enclosingTypeNames);
			String oldContainerName = this.lastContainerFoundForType.put(simpleName, newContainerName);
			if (oldContainerName != null && !oldContainerName.equals(newContainerName)) {
				this.namesFoundInMultipleContainers.add(simpleName);
			}
		}

		Set<String> getNamesFoundInMultipleContainers() {
			return Collections.unmodifiableSet(this.namesFoundInMultipleContainers);
		}
	}

	private static char[][] stringsToCharArrays(Collection<String> strings) {
		char[][] arrayOfArrays = new char[strings.size()][];
		int i = 0;
		for (String string : strings) { //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
			arrayOfArrays[i] = string.toCharArray();
			i++;
		}
		return arrayOfArrays;
	}

	private final IJavaProject javaProject;
	private final SearchEngine searchEngine;

	TypeConflictingSimpleNameFinder(IJavaProject javaProject, SearchEngine searchEngine) {
		this.javaProject = javaProject;
		this.searchEngine = searchEngine;
	}

	@Override
	public Set<String> findConflictingSimpleNames(
			Set<String> simpleNames,
			Set<String> onDemandAndImplicitContainerNames,
			IProgressMonitor monitor) throws JavaModelException {
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] { this.javaProject });

		ConflictAccumulatingTypeRequestor requestor = new ConflictAccumulatingTypeRequestor();

		this.searchEngine.searchAllTypeNames(
				stringsToCharArrays(onDemandAndImplicitContainerNames),
				stringsToCharArrays(simpleNames),
				scope,
				requestor,
				IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
				monitor);

		return requestor.getNamesFoundInMultipleContainers();
	}
}