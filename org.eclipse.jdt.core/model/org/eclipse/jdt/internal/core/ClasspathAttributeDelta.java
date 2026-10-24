/*******************************************************************************
 * Copyright (c) 2022 Simeon Andreev and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.jdt.internal.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathAttributeDelta;
import org.eclipse.jdt.core.IClasspathEntry;

/**
 * @see IClasspathAttributeDelta
 */
public class ClasspathAttributeDelta implements IClasspathAttributeDelta {

	private static final String NO_VALUE = ""; //$NON-NLS-1$

	private final int kind;
	private final String attributeName;
	private final String attributeValue;

	private ClasspathAttributeDelta(int kind, String attributeName, String attributeValue) {
		Assert.isTrue(kind == ADDED || kind == REMOVED || kind == CHANGED, "Unexpected delta kind: " + kind); //$NON-NLS-1$ //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		this.kind = kind;
		this.attributeName = attributeName;
		this.attributeValue = attributeValue;
	}

	@Override
	public int getKind() {
		return this.kind;
	}

	@Override
	public String getAttributeName() {
		return this.attributeName;
	}

	@Override
	public String getAttributeValue() {
		return this.attributeValue;
	}

	@Override //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
	public String toString() {
		StringBuilder buffer = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buffer.append("attribute name="); //$NON-NLS-1$ //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buffer.append(getAttributeName()); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		int deltaKind = getKind();
		if (deltaKind != REMOVED) {
			buffer.append(", value="); //$NON-NLS-1$ //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			buffer.append(getAttributeValue()); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		}
		buffer.append("["); //$NON-NLS-1$ //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		switch (deltaKind) {
			case ADDED :
				buffer.append('+'); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				break;
			case REMOVED :
				buffer.append('-'); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				break;
			case CHANGED :
				buffer.append('*'); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				break;
			default :
				buffer.append('?'); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				break;
		}
		buffer.append("]"); //$NON-NLS-1$ //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		return buffer.toString();
	}

	static List<IClasspathAttributeDelta> getAttributeDeltas(IClasspathEntry oldClasspathEntry, IClasspathEntry newClasspathEntry) { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		Map<String, String> oldAttributes = getAttributes(oldClasspathEntry);
		Map<String, String> newAttributes = getAttributes(newClasspathEntry);

		if (oldAttributes.isEmpty() && newAttributes.isEmpty()) {
			return Collections.emptyList();
		}

		Set<String> addedAttributes = new LinkedHashSet<>();
		Set<String> removedAttributes = new LinkedHashSet<>();
		Set<String> changedAttributes = new LinkedHashSet<>();

		for (Entry<String, String> newAttribute : newAttributes.entrySet()) { //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
			String name = newAttribute.getKey();
			if (!oldAttributes.containsKey(name)) {
				addedAttributes.add(name);
			}
		}
		for (Entry<String, String> oldAttribute : oldAttributes.entrySet()) { //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
			String name = oldAttribute.getKey();
			String oldValue = oldAttribute.getValue();
			String newValue = newAttributes.get(name);
			if (newValue != null) {
				if (!Objects.equals(oldValue, newValue)) {
					changedAttributes.add(name);
				}
			} else {
				removedAttributes.add(name);
			}
		}

		List<IClasspathAttributeDelta> attributeDeltas = new ArrayList<>(); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW
		for (String name : addedAttributes) { //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
			String value = newAttributes.get(name);
			IClasspathAttributeDelta attributeDelta = new ClasspathAttributeDelta(IClasspathAttributeDelta.ADDED, name, value);
			attributeDeltas.add(attributeDelta);
		}
		for (String name : removedAttributes) { //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
			String noValue = NO_VALUE;
			IClasspathAttributeDelta attributeDelta = new ClasspathAttributeDelta(IClasspathAttributeDelta.REMOVED, name, noValue);
			attributeDeltas.add(attributeDelta);
		}
		for (String name : changedAttributes) { //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
			String value = newAttributes.get(name);
			IClasspathAttributeDelta attributeDelta = new ClasspathAttributeDelta(IClasspathAttributeDelta.CHANGED, name, value);
			attributeDeltas.add(attributeDelta);
		}
		return attributeDeltas;
	}

	private static Map<String, String> getAttributes(IClasspathEntry classpathEntry) {
		IClasspathAttribute[] extraAttributes = classpathEntry.getExtraAttributes();
		Map<String, String> attributes = new LinkedHashMap<>(); //FLLDCIM: MARK — M04 M3_ORDERED_LANES NEW
		for (IClasspathAttribute attribute : extraAttributes) { //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
			String name = attribute.getName();
			String value = attribute.getValue();
			if (value == null) {
				value = NO_VALUE;
			}
			attributes.put(name, value);
		}
		return attributes;
	}
}
