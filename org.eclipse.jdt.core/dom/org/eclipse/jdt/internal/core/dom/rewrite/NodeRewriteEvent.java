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
package org.eclipse.jdt.internal.core.dom.rewrite;



public class NodeRewriteEvent extends RewriteEvent {

	private final Object originalValue;
	private Object newValue;

	public NodeRewriteEvent(Object originalValue, Object newValue) {
		this.originalValue= originalValue;
		this.newValue= newValue;
	}

	/**
	 * @return Returns the new value.
	 */
	@Override
	public Object getNewValue() {
		return this.newValue;
	}

	/**
	 * @return Returns the original value.
	 */
	@Override
	public Object getOriginalValue() {
		return this.originalValue;
	}

	@Override
	public int getChangeKind() {
		if (this.originalValue == this.newValue) {
			return UNCHANGED;
		}
		if (this.originalValue == null) {
			return INSERTED;
		}
		if (this.newValue == null) {
			return REMOVED;
		}
		if (this.originalValue.equals(this.newValue)) {
			return UNCHANGED;
		}
		return REPLACED;
	}

	@Override
	public boolean isListRewrite() {
		return false;
	}

	/*
	 * Sets a new value for the new node. Internal access only.
	 * @param newValue The new value to set.
	 */
	public void setNewValue(Object newValue) {
		this.newValue= newValue;
	}

	@Override
	public RewriteEvent[] getChildren() {
		return null;
	}

	@Override //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
	public String toString() {
		StringBuilder buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		switch (getChangeKind()) {
		case INSERTED:
			buf.append(" [inserted: "); //$NON-NLS-1$ //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			buf.append(getNewValue()); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			buf.append(']'); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			break;
		case REPLACED:
			buf.append(" [replaced: "); //$NON-NLS-1$ //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			buf.append(getOriginalValue()); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			buf.append(" -> "); //$NON-NLS-1$ //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			buf.append(getNewValue()); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			buf.append(']'); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			break;
		case REMOVED:
			buf.append(" [removed: "); //$NON-NLS-1$ //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			buf.append(getOriginalValue()); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			buf.append(']'); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			break;
		default:
			buf.append(" [unchanged]"); //$NON-NLS-1$ //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		}
		return buf.toString();
	}


}
