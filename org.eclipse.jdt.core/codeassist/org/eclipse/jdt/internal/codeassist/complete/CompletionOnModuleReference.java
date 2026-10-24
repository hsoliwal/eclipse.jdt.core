/*******************************************************************************
 * Copyright (c) 2016 IBM Corporation and others.
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
package org.eclipse.jdt.internal.codeassist.complete;

import org.eclipse.jdt.internal.compiler.ast.ModuleReference;
import org.eclipse.jdt.internal.compiler.lookup.ModuleBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;

public class CompletionOnModuleReference extends ModuleReference implements CompletionNode {

	public CompletionOnModuleReference(char[] ident, long pos) {
		this(new char[][]{ident}, new long[]{pos});
	}
	public CompletionOnModuleReference(char[][] tokens, long[] sourcePositions) {
		super(tokens, sourcePositions);
	}

	@Override
	public ModuleBinding resolve(Scope scope) {
		super.resolve(scope);
//		if (this.binding != null) {
//			throw new CompletionNodeFound(this, this.binding, scope);
//		} else {
			throw new CompletionNodeFound();
		//}
	}
	@Override
	public StringBuilder print(int indent, StringBuilder output) {

		printIndent(indent, output).append("<CompleteOnModuleReference:"); //$NON-NLS-1$
		for (int i = 0; i < this.tokens.length; i++) { //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
			if (i > 0) output.append('.'); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			output.append(this.tokens[i]); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		}
		return output.append('>'); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
	}

}
