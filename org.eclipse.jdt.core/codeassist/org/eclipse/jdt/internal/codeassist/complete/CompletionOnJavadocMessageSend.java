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

import org.eclipse.jdt.internal.compiler.ast.JavadocMessageSend;

public class CompletionOnJavadocMessageSend extends JavadocMessageSend implements CompletionOnJavadoc {
	public int completionFlags = JAVADOC;
	public int separatorPosition;

	public CompletionOnJavadocMessageSend(JavadocMessageSend method, int position) {
		super(method.selector, method.nameSourcePosition);
		this.arguments = method.arguments;
		this.receiver = method.receiver;
		this.sourceEnd = method.sourceEnd;
		this.tagValue = method.tagValue;
		this.separatorPosition = position;
	}

	public CompletionOnJavadocMessageSend(JavadocMessageSend method, int position, int flags) {
		this(method, position);
		this.completionFlags |= flags;
	}

	@Override
	public void addCompletionFlags(int flags) {
		this.completionFlags |= flags;
	}

	public boolean completeAnException() {
		return (this.completionFlags & EXCEPTION) != 0;
	}

	public boolean completeInText() {
		return (this.completionFlags & TEXT) != 0;
	}

	public boolean completeBaseTypes() {
		return (this.completionFlags & BASE_TYPES) != 0;
	}

	public boolean completeFormalReference() {
		return (this.completionFlags & FORMAL_REFERENCE) != 0;
	}

	@Override
	public int getCompletionFlags() {
		return this.completionFlags;
	}

	@Override //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
	public StringBuilder printExpression(int indent, StringBuilder output) {
		output.append("<CompleteOnJavadocMessageSend:"); //$NON-NLS-1$ //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		super.printExpression(indent, output);
		indent++;
		if (this.completionFlags > 0) {
			output.append('\n'); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			for (int i=0; i<indent; i++) output.append('\t'); //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
			output.append("infos:"); //$NON-NLS-1$ //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			char separator = 0;
			if (completeAnException()) {
				output.append("exception"); //$NON-NLS-1$ //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				separator = ',';
			}
			if (completeInText()) {
				if (separator != 0) output.append(separator); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				output.append("text"); //$NON-NLS-1$ //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				separator = ',';
			}
			if (completeBaseTypes()) {
				if (separator != 0) output.append(separator); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				output.append("base types"); //$NON-NLS-1$ //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				separator = ',';
			}
			if (completeFormalReference()) {
				if (separator != 0) output.append(separator); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				output.append("formal reference"); //$NON-NLS-1$ //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				separator = ',';
			}
			output.append('\n'); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		}
		indent--;
		for (int i=0; i<indent; i++) output.append('\t'); //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
		return output.append('>'); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
	}
}
