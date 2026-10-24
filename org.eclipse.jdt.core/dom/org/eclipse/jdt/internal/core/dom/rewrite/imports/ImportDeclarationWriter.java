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

final class ImportDeclarationWriter {
	private final boolean insertSpaceBeforeSemicolon;

	ImportDeclarationWriter(boolean insertSpaceBeforeSemicolon) {
		this.insertSpaceBeforeSemicolon = insertSpaceBeforeSemicolon;
	}

	/**
	 * Writes the Java source for an import declaration of the given name.
	 */
	String writeImportDeclaration(ImportName importName) { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		StringBuilder sb = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW

		sb.append("import "); //$NON-NLS-1$ //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL

		if (importName.isStatic) {
			sb.append("static "); //$NON-NLS-1$ //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		}

		if (importName.isModule) {
			sb.append("module "); //$NON-NLS-1$ //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		}

		sb.append(importName.qualifiedName); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL

		if (this.insertSpaceBeforeSemicolon) {
			sb.append(' '); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		}

		sb.append(';'); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL

		return sb.toString();
	}
}
