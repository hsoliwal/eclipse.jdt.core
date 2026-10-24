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
package org.eclipse.jdt.core.tests.compiler.parser;

import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.env.ISourceMethod;

public class SourceMethod implements ISourceMethod {
	private final int modifiers;
	private final int declarationStart;
	private int declarationEnd;
	private final char[] returnTypeName;
	private final char[] selector;
	private final int nameSourceStart;
	private final int nameSourceEnd;
	private final char[][] argumentTypeNames;
	private final char[][] argumentNames;
	private final char[][] exceptionTypeNames;
	private final char[] source;
	private String explicitConstructorCall;
	char[][] typeParameterNames;
	char[][][] typeParameterBounds;

public SourceMethod(
	int declarationStart,
	int modifiers,
	char[] returnTypeName,
	char[] selector,
	int nameSourceStart,
	int nameSourceEnd,
	char[][] argumentTypeNames,
	char[][] argumentNames,
	char[][] exceptionTypeNames,
	char[] source) {

	this.declarationStart = declarationStart;
	this.modifiers = modifiers;
	this.returnTypeName = returnTypeName;
	this.selector = selector;
	this.nameSourceStart = nameSourceStart;
	this.nameSourceEnd = nameSourceEnd;
	this.argumentTypeNames = argumentTypeNames;
	this.argumentNames = argumentNames;
	this.exceptionTypeNames = exceptionTypeNames;
	this.source = source;
}
public String displayModifiers() { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
	StringBuilder buffer = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW

	if (this.modifiers == 0)
		return null;
	if ((this.modifiers & ClassFileConstants.AccPublic) != 0)
		buffer.append("public "); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
	if ((this.modifiers & ClassFileConstants.AccProtected) != 0)
		buffer.append("protected "); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
	if ((this.modifiers & ClassFileConstants.AccPrivate) != 0)
		buffer.append("private "); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
	if ((this.modifiers & ClassFileConstants.AccFinal) != 0)
		buffer.append("final "); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
	if ((this.modifiers & ClassFileConstants.AccStatic) != 0)
		buffer.append("static "); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
	if ((this.modifiers & ClassFileConstants.AccAbstract) != 0)
		buffer.append("abstract "); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
	if ((this.modifiers & ClassFileConstants.AccNative) != 0)
		buffer.append("native "); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
	if ((this.modifiers & ClassFileConstants.AccSynchronized) != 0)
		buffer.append("synchronized "); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
	if (buffer.toString().trim().equals(""))
		return null;
	return buffer.toString().trim();
}
public String getActualName() {
	StringBuilder buffer = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
	buffer.append(this.source, this.nameSourceStart, this.nameSourceEnd - this.nameSourceStart + 1); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
	return buffer.toString();
}
public char[][] getArgumentNames() {
	return this.argumentNames;
}
public char[][] getArgumentTypeNames() {
	return this.argumentTypeNames;
}
public int getDeclarationSourceEnd() {
	return this.declarationEnd;
}
public int getDeclarationSourceStart() {
	return this.declarationStart;
}
public char[][] getExceptionTypeNames() {
	return this.exceptionTypeNames;
}
public int getModifiers() {
	return this.modifiers;
}
public int getNameSourceEnd() {
	return this.nameSourceEnd;
}
public int getNameSourceStart() {
	return this.nameSourceStart;
}
public char[] getReturnTypeName() {
	return this.returnTypeName;
}
public char[] getSelector() {
	return this.selector;
}
public char[][][] getTypeParameterBounds() {
	return this.typeParameterBounds;
}
public char[][] getTypeParameterNames() {
	return this.typeParameterNames;
}
public boolean isConstructor() {
	return this.returnTypeName == null;
}
protected void setDeclarationSourceEnd(int position) {
	this.declarationEnd = position;
}
protected void setExplicitConstructorCall(String s) {
	this.explicitConstructorCall = s;
}
public String tabString(int tab) {
	/*slow code*/

	String s = "";
	for (int i = tab; i > 0; i--) //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
		s = s + "\t"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
	return s;
}
@Override
public String toString() {
	return toString(0);
}
public String toString(int tab) { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
	StringBuilder buffer = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
	buffer.append(tabString(tab)); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
	String displayModifiers = displayModifiers();
	if (displayModifiers != null) {
		buffer.append(displayModifiers).append(" "); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
	}
	if (this.returnTypeName != null) {
		buffer.append(this.returnTypeName).append(" "); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
	}
	buffer.append(this.selector).append("("); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
	if (this.argumentTypeNames != null) {
		for (int i = 0, max = this.argumentTypeNames.length; i < max; i++) { //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
			buffer.append(this.argumentTypeNames[i]).append(" ").append( //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				this.argumentNames[i]).append(
				", ");
		}
	}
	buffer.append(") "); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
	if (this.exceptionTypeNames != null) {
		buffer.append("throws "); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		for (int i = 0, max = this.exceptionTypeNames.length; i < max; i++) { //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
			buffer.append(this.exceptionTypeNames[i]).append(", "); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		}
	}
	if (this.explicitConstructorCall != null) {
		buffer.append("{\n").append(tabString(tab+1)).append(this.explicitConstructorCall).append(tabString(tab)).append("}"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
	} else {
		buffer.append("{}"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
	}
	return buffer.toString();
}
}
