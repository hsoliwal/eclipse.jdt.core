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
package org.eclipse.jdt.internal.codeassist.complete;

import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;

public class CompletionOnMethodName extends MethodDeclaration implements CompletionNode {
	public int selectorEnd;

	public CompletionOnMethodName(CompilationResult compilationResult){
		super(compilationResult);
	}

	@Override //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
	public StringBuilder print(int indent, StringBuilder output) {

		printIndent(indent, output);
		output.append("<CompletionOnMethodName:"); //$NON-NLS-1$ //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		printModifiers(this.modifiers, output);
		printReturnType(0, output);
		output.append(this.selector).append('('); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		if (this.arguments != null) {
			for (int i = 0; i < this.arguments.length; i++) { //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
				if (i > 0) output.append(", "); //$NON-NLS-1$ //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				this.arguments[i].print(0, output);
			}
		}
		output.append(')'); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		if (this.thrownExceptions != null) {
			output.append(" throws "); //$NON-NLS-1$ //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			for (int i = 0; i < this.thrownExceptions.length; i++) { //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
				if (i > 0) output.append(", "); //$NON-NLS-1$ //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				this.thrownExceptions[i].print(0, output);
			}
		}
		return output.append('>'); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
	}

	@Override
	public void resolve(ClassScope upperScope) {

		super.resolve(upperScope);
		throw new CompletionNodeFound(this, upperScope);
	}
}
