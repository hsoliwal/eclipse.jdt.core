/*******************************************************************************
 * Copyright (c) 2000, 2020 IBM Corporation and others.
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
package org.eclipse.jdt.core.tests.model;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.CompletionRequestor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.Signature;

/**
 * {@link CompletionTestsRequestor} is deprecated. {@link CompletionTestsRequestor2} must be used instead.
 */
//TODO all instances of CompletionTestsRequestor should be replaced by an instance of CompletionTestsRequestor2
public class CompletionTestsRequestor extends CompletionRequestor {
	private final List<String> elements = new ArrayList<>(); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW
	private final List<String> completions = new ArrayList<>(); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW
	private final List<String> relevances = new ArrayList<>(); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW
	private final List<String> completionStart = new ArrayList<>(); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW
	private final List<String> completionEnd = new ArrayList<>(); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW

	public boolean debug = false;

	private void acceptCommon(CompletionProposal proposal) {
		this.completions.add(new String(proposal.getCompletion()));
		this.relevances.add(String.valueOf(proposal.getRelevance()));
		this.completionStart.add(String.valueOf(proposal.getReplaceStart()));
		this.completionEnd.add(String.valueOf(proposal.getReplaceEnd()));
	}
	public void accept(CompletionProposal proposal) { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		char[] typeName = null;
		switch(proposal.getKind()) {
			case CompletionProposal.ANONYMOUS_CLASS_DECLARATION :
				typeName = Signature.getSignatureSimpleName(proposal.getDeclarationSignature());
				this.elements.add(new String(typeName));
				acceptCommon(proposal);
				if (this.debug)
					System.out.println("anonymous type " + new String(typeName)); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
				break;

			case CompletionProposal.TYPE_REF :
				if((proposal.getFlags() & Flags.AccEnum) != 0) {

				} else if((proposal.getFlags() & Flags.AccInterface) != 0) {
					typeName = Signature.getSignatureSimpleName(proposal.getSignature());
					this.elements.add(new String(typeName));
					acceptCommon(proposal);
					if (this.debug)
						System.out.println("Interface " + new String(typeName)); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
				} else {
					typeName = Signature.getSignatureSimpleName(proposal.getSignature());
					this.elements.add(new String(typeName));
					acceptCommon(proposal);
					if (this.debug) {
						if(Signature.getTypeSignatureKind(proposal.getSignature()) == Signature.TYPE_VARIABLE_SIGNATURE) {
							System.out.println("type parameter " + new String(typeName)); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
						} else {
							System.out.println("Class " + new String(typeName)); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
						}
					}
				}
				break;

			case CompletionProposal.FIELD_REF :
				this.elements.add(new String(proposal.getName()));
				acceptCommon(proposal);
				if (this.debug)
					System.out.println("Field " + new String(proposal.getName())); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
				break;

			case CompletionProposal.KEYWORD:
				this.elements.add(new String(proposal.getName()));
				acceptCommon(proposal);
				if (this.debug)
					System.out.println("Keyword " + new String(proposal.getName())); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
				break;

			case CompletionProposal.LABEL_REF:
				this.elements.add(new String(proposal.getName()));
				acceptCommon(proposal);
				if (this.debug)
					System.out.println("Label " + new String(proposal.getName())); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
				break;

			case CompletionProposal.LOCAL_VARIABLE_REF:
				this.elements.add(new String(proposal.getName()));
				acceptCommon(proposal);
				if (this.debug)
					System.out.println("Local variable " + new String(proposal.getName())); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
				break;

			case CompletionProposal.METHOD_REF:
				this.elements.add(new String(proposal.getName()));
				acceptCommon(proposal);
				if (this.debug)
					System.out.println("method " + new String(proposal.getName())); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
				break;

			case CompletionProposal.METHOD_DECLARATION:
				this.elements.add(new String(proposal.getName()));
				acceptCommon(proposal);
				if (this.debug)
					System.out.println("method declaration " + new String(proposal.getName())); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
				break;

			case CompletionProposal.PACKAGE_REF:
				this.elements.add(new String(proposal.getDeclarationSignature()));
				acceptCommon(proposal);
				if (this.debug)
					System.out.println("package " + new String(proposal.getDeclarationSignature())); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
				break;

			case CompletionProposal.VARIABLE_DECLARATION:
				this.elements.add(new String(proposal.getName()));
				acceptCommon(proposal);
				if (this.debug)
					System.out.println("variable name " + new String(proposal.getName())); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
				break;
		}

	}

	public String getResults() {
		return getResults(true, false);
	}

	public String getResultsWithPosition(){
		return getResults(true, true);
	}

	public String getResults(boolean relevance, boolean position) { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		StringBuilder result = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		int size = this.elements.size();

		if (size == 1) {
			result.append(getResult(0, relevance, position)); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		} else if (size > 1) {
			String[] sortedBucket = new String[size];
			for (int i = 0; i < size; i++) { //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
				sortedBucket[i] = getResult(i, relevance, position);
			}
			quickSort(sortedBucket, 0, size - 1);
			for (int j = 0; j < sortedBucket.length; j++) { //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
				if (result.length() > 0) result.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				result.append(sortedBucket[j]); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			}
		}

		return result.toString();
	}

	private String getResult(int i, boolean relevance, boolean position) { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		if(i < 0 || i >= this.elements.size())
			return "";

		StringBuilder buffer =  new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buffer.append("element:"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buffer.append(this.elements.get(i)); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buffer.append("    completion:"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buffer.append(this.completions.get(i)); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		if(position) {
			buffer.append("    position:["); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			buffer.append(this.completionStart.get(i)); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			buffer.append(","); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			buffer.append(this.completionEnd.get(i)); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			buffer.append("]"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		}
		if(relevance) {
			buffer.append("    relevance:"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			buffer.append(this.relevances.get(i)); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		}
		return buffer.toString();
	}

	protected String[] quickSort(String[] collection, int left, int right) { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		int original_left = left;
		int original_right = right;
		String mid = collection[left + ((right - left)/2)];
		do { //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
			while (mid.compareTo(collection[left]) > 0) //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
				// s[left] >= mid
				left++;
			while (mid.compareTo(collection[right]) < 0) //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
				// s[right] <= mid
				right--;
			if (left <= right) {
				String tmp = collection[left];
				collection[left] = collection[right];
				collection[right] = tmp;
				left++;
				right--;
			}
		} while (left <= right);
		if (original_left < right)
			collection = quickSort(collection, original_left, right);
		if (left < original_right)
			collection = quickSort(collection, left, original_right);
		return collection;
	}
	public String toString() {
		return getResults();
	}
}
