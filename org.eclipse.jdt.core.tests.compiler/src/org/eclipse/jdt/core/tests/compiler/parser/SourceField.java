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
import org.eclipse.jdt.internal.compiler.env.ISourceField;

public class SourceField implements ISourceField {
	protected int modifiers;
	protected char[] typeName;
	protected char[] name;
	protected int declarationStart;
	protected int declarationEnd;
	protected int nameSourceStart;
	protected int nameSourceEnd;
	protected char[] source;
public SourceField(
	int declarationStart,
	int modifiers,
	char[] typeName,
	char[] name,
	int nameSourceStart,
	int nameSourceEnd,
	char[] source) {

	this.declarationStart = declarationStart;
	this.modifiers = modifiers;
	this.typeName = typeName;
	this.name = name;
	this.nameSourceStart = nameSourceStart;
	this.nameSourceEnd = nameSourceEnd;
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
	return buffer.toString();
}
public String getActualName() {
	StringBuilder buffer = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
	buffer.append(this.source, this.nameSourceStart, this.nameSourceEnd - this.nameSourceStart + 1); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
	return buffer.toString();
}
public int getDeclarationSourceEnd() {
	return this.declarationEnd;
}
public int getDeclarationSourceStart() {
	return this.declarationStart;
}
public char[] getInitializationSource() {
	return null;
}
public int getModifiers() {
	return this.modifiers;
}
public char[] getName() {
	return this.name;
}
public int getNameSourceEnd() {
	return this.nameSourceEnd;
}
public int getNameSourceStart() {
	return this.nameSourceStart;
}
public char[] getTypeName() {
	return this.typeName;
}
protected void setDeclarationSourceEnd(int position) {
	this.declarationEnd = position;
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
public String toString(int tab) {
	StringBuilder buffer = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
	buffer.append(tabString(tab)); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
	String displayModifiers = displayModifiers();
	if (displayModifiers != null) {
		buffer.append(displayModifiers); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
	}
	buffer.append(this.typeName).append(" ").append(this.name); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
	buffer.append(";"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
	return buffer.toString();
}
}
