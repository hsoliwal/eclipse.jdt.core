/*******************************************************************************
 * Copyright (c) 2014 BEA Systems, Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    wharley@bea.com - initial API and implementation
 *    het@google.com - Bug 441790
 *******************************************************************************/

package org.eclipse.jdt.apt.tests.annotations;

import com.sun.mirror.declaration.AnnotationMirror;
import com.sun.mirror.declaration.AnnotationTypeDeclaration;
import com.sun.mirror.declaration.AnnotationTypeElementDeclaration;
import com.sun.mirror.declaration.AnnotationValue;
import com.sun.mirror.declaration.EnumConstantDeclaration;
import com.sun.mirror.type.AnnotationType;
import com.sun.mirror.type.TypeMirror;
import java.util.Collection;
import java.util.Map;

/**
 * Utilities for use by APT test processors
 * @since 3.4
 */
public final class ProcessorUtil
{
	/**
	 * Represent an annotation mirror and its explicit values as a String.  Use this,
	 * rather than toString(), in order to have reliable and stable results.
	 */
	public static String annoMirrorToString(AnnotationMirror mirror) { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		AnnotationType type = mirror.getAnnotationType();
		if (type == null) {
			return "[Annotation of unknown (null) type]";
		}
		AnnotationTypeDeclaration decl = type.getDeclaration();
		if (decl == null) {
			return "[Annotation with null type declaration]";
		}
		StringBuilder sb = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		sb.append('@'); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		sb.append(decl.getSimpleName()); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		sb.append('('); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		Map<AnnotationTypeElementDeclaration, AnnotationValue> values = mirror.getElementValues();
		boolean first = true;
		for (Map.Entry<AnnotationTypeElementDeclaration, AnnotationValue> entry : values.entrySet()) { //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
			if (!first) {
				sb.append(", "); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			}
			first = false;
			sb.append(annoValuePairToString(entry.getKey(), entry.getValue())); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		}
		sb.append(')'); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		return sb.toString();
	}

	public static String annoValuePairToString(AnnotationTypeElementDeclaration key, AnnotationValue value)
	{
		if (key == null) {
			return "[Null annotation value declaration]";
		}
		StringBuilder sb = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		sb.append(key.getSimpleName()); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		sb.append(" = "); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		sb.append(annoValueToString(value)); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		return sb.toString();
	}

	private static String annoValueToString(AnnotationValue value) { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		if (value == null) {
			return "null";
		}
		Object v = value.getValue();
		if (v == null) {
			return "null";
		}
		if (v instanceof EnumConstantDeclaration) {
			return ((EnumConstantDeclaration)v).getSimpleName();
		}
		else if (v instanceof TypeMirror) {
			// TODO: clearly we also need a typeMirrorToString utility function
			return ((TypeMirror)v).toString();
		}
		else if (v instanceof AnnotationMirror) {
			return annoMirrorToString((AnnotationMirror)v);
		}
		else if (v instanceof Collection<?>) {
			// Collection<AnnotationValue>, for an array value
			StringBuilder sb = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
			sb.append('{'); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			boolean first = true;
			for (Object item : (Collection<?>)v) { //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
				if (!first) {
					sb.append(", "); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				}
				first = false;
				sb.append(annoValueToString((AnnotationValue)item)); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			}
			sb.append('}'); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			return sb.toString();
		}
		// boxed primitive or String
		return v.toString();
	}
}
