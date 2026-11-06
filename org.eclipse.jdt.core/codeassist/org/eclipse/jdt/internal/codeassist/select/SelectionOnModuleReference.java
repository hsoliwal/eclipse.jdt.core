/*******************************************************************************
 * Copyright (c) 2016, 2017 IBM Corporation and others.
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
 *
 *******************************************************************************/
package org.eclipse.jdt.internal.codeassist.select;

import org.eclipse.jdt.internal.compiler.ast.ModuleReference;
import org.eclipse.jdt.internal.compiler.lookup.ModuleBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;

public class SelectionOnModuleReference extends ModuleReference {

	public SelectionOnModuleReference(char[][] tokens, long[] sourcePositions) {
		super(tokens, sourcePositions);
	}

	@Override
	public ModuleBinding resolve(Scope scope) {
		ModuleBinding resolvedBinding = super.resolve(scope);
		if (resolvedBinding != null) {
			throw new SelectionNodeFound(resolvedBinding);
		} else {
			throw new SelectionNodeFound();
		}
	}

	@Override
	public StringBuilder print(int tab, StringBuilder output) {
		printIndent(tab, output).append("<SelectOnModuleReference:"); //$NON-NLS-1$
		for (int i = 0; i < this.tokens.length; i++) { //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
			if (i > 0) output.append('.'); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			output.append(this.tokens[i]); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		}
		return output.append('>'); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
	}

}
