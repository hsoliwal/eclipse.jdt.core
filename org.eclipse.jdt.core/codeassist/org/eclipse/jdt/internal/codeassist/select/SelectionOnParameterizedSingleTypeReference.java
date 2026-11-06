/*******************************************************************************
 * Copyright (c) 2000, 2014 IBM Corporation and others.
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
 *     Stephan Herrmann - Contribution for
 *								Bug 429958 - [1.8][null] evaluate new DefaultLocation attribute of @NonNullByDefault
 *******************************************************************************/
package org.eclipse.jdt.internal.codeassist.select;

import org.eclipse.jdt.internal.compiler.ast.ParameterizedSingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

public class SelectionOnParameterizedSingleTypeReference extends ParameterizedSingleTypeReference {
	public SelectionOnParameterizedSingleTypeReference(char[] name, TypeReference[] typeArguments, long pos){
		super(name, typeArguments, 0, pos);
	}

	@Override
	public TypeBinding resolveType(BlockScope scope, boolean checkBounds, int location) {
		super.resolveType(scope, checkBounds, location);
		throw new SelectionNodeFound(this.resolvedType);
	}

	@Override
	public TypeBinding resolveType(ClassScope scope, int location) {
		super.resolveType(scope, location);
		throw new SelectionNodeFound(this.resolvedType);
	}

	@Override
	public StringBuilder printExpression(int indent, StringBuilder output){
		output.append("<SelectOnType:");//$NON-NLS-1$ //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		output.append(this.token); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		output.append('<'); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		int max = this.typeArguments.length - 1;
		for (int i= 0; i < max; i++) { //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
			this.typeArguments[i].print(0, output);
			output.append(", ");//$NON-NLS-1$ //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		}
		this.typeArguments[max].print(0, output);
		output.append('>'); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		output.append('>'); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		return output;
	}
}
