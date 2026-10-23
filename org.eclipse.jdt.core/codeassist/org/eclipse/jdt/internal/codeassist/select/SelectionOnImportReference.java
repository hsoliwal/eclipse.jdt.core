/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.codeassist.select;

/*
 * Selection node build by the parser in any case it was intending to
 * reduce an import reference containing the assist identifier.
 * e.g.
 *
 *  import java.[start]io[end].*;
 *	class X {
 *    void foo() {
 *    }
 *  }
 *
 *	---> <SelectOnImport:java.io>
 *		 class X {
 *         void foo() {
 *         }
 *       }
 */

import org.eclipse.jdt.internal.compiler.ast.ImportReference;

public class SelectionOnImportReference extends ImportReference {

public SelectionOnImportReference(char[][] tokens , long[] positions, int modifiers) {
	super(tokens, positions, false, modifiers);
}
@Override
public StringBuilder print(int indent, StringBuilder output, boolean withOnDemand) {

	printIndent(indent, output).append("<SelectOnImport:"); //$NON-NLS-1$
	for (int i = 0; i < this.tokens.length; i++) { //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
		if (i > 0) output.append('.'); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		output.append(this.tokens[i]); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
	}
	return output.append('>'); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
}
}
