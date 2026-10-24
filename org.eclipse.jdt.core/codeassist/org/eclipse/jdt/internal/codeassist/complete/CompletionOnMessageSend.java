/*******************************************************************************
 * Copyright (c) 2000, 2013 IBM Corporation and others.
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

/*
 * Completion node build by the parser in any case it was intending to
 * reduce a message send containing the cursor.
 * e.g.
 *
 *	class X {
 *    void foo() {
 *      this.bar(1, 2, [cursor]
 *    }
 *  }
 *
 *	---> class X {
 *         void foo() {
 *           <CompleteOnMessageSend:this.bar(1, 2)>
 *         }
 *       }
 *
 * The source range is always of length 0.
 * The arguments of the message send are all the arguments defined
 * before the cursor.
 */
import org.eclipse.jdt.internal.compiler.ast.MessageSend;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

public class CompletionOnMessageSend extends MessageSend implements CompletionNode {

	@Override //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
	public TypeBinding resolveType(BlockScope scope) {
		this.constant = Constant.NotAConstant;
		if (this.arguments != null) {
			int argsLength = this.arguments.length;
			for (int a = argsLength; --a >= 0;) { //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
				try {
					this.arguments[a].resolveType(scope);
				} catch (CompletionNodeFound cnf) {
					// ignore nested completion node
				}
			}
		}

		if (this.receiver.isImplicitThis())
			throw new CompletionNodeFound(this, null, scope);

		this.actualReceiverType = this.receiver.resolveType(scope);
		if (this.actualReceiverType == null || this.actualReceiverType.isBaseType())
			throw new CompletionNodeFound();

		if (this.actualReceiverType.isArrayType())
			this.actualReceiverType = scope.getJavaLangObject();
		throw new CompletionNodeFound(this, this.actualReceiverType, scope);
	}

	@Override //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
	public StringBuilder printExpression(int indent, StringBuilder output) {

		output.append("<CompleteOnMessageSend:"); //$NON-NLS-1$ //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		if (!this.receiver.isImplicitThis()) this.receiver.printExpression(0, output).append('.');
		if (this.typeArguments != null) {
			output.append('<'); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			int max = this.typeArguments.length - 1;
			for (int j = 0; j < max; j++) { //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
				this.typeArguments[j].print(0, output);
				output.append(", ");//$NON-NLS-1$ //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			}
			this.typeArguments[max].print(0, output);
			output.append('>'); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		}
		output.append(this.selector).append('('); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		if (this.arguments != null) {
			for (int i = 0; i < this.arguments.length; i++) { //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
				if (i > 0) output.append(", "); //$NON-NLS-1$ //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				this.arguments[i].printExpression(0, output);
			}
		}
		return output.append(")>"); //$NON-NLS-1$ //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
	}
}
