/*******************************************************************************
 * Copyright (c) 2004, 2022 IBM Corporation and others.
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

import java.util.Arrays;
import java.util.Comparator;
import java.util.function.Predicate;
import org.eclipse.jdt.core.CompletionContext;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.CompletionRequestor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.internal.codeassist.InternalCompletionContext;
import org.eclipse.jdt.internal.codeassist.impl.RestrictedIdentifiers;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.util.ObjectVector;
import org.eclipse.jdt.internal.core.JavaElement;

@SuppressWarnings({"rawtypes", "unchecked"})
public class CompletionTestsRequestor2 extends CompletionRequestor {
	private final char[] NULL_LITERAL = "null".toCharArray();//$NON-NLS-1$

	private CompletionContext context;
	public int proposalsPtr = -1;
	private final static int PROPOSALS_INCREMENT = 10;
	private CompletionProposal[] proposals = new CompletionProposal[PROPOSALS_INCREMENT];
	private IProblem problem;

	private final boolean showParameterNames;
	private final boolean showUniqueKeys;
	private final boolean showPositions;
	private final boolean showTokenPositions;
	private final boolean shortContext;
	private final boolean showMissingTypes;
	private final boolean showModifiers;

	private boolean computeVisibleElements;
	private boolean computeEnclosingElement;
	private String assignableType;

	public boolean debug = false;

	@SuppressWarnings("unused")
	private Predicate<CompletionProposal> proposalFilter = x -> true;
	@SuppressWarnings("unused")
	private Predicate<String> typeProposalFilter = x -> false;

	public CompletionTestsRequestor2() {
		this(false, false);
	}
	public CompletionTestsRequestor2(boolean showParamNames) {
		this(showParamNames, false, false);
	}
	public CompletionTestsRequestor2(boolean showParamNames, boolean showUniqueKeys) {
		this(showParamNames, showUniqueKeys, false);
	}
	public CompletionTestsRequestor2(boolean showParamNames, boolean showUniqueKeys, boolean showPositions) {
		this(showParamNames, showUniqueKeys, showPositions, true, false);
	}

	public CompletionTestsRequestor2(boolean showParamNames, boolean showUniqueKeys, boolean showPositions, boolean shortContext) {
		this(showParamNames, showUniqueKeys, showPositions, shortContext, false);
	}
	public CompletionTestsRequestor2(boolean showParamNames, boolean showUniqueKeys, boolean showPositions, boolean shortContext, boolean showMissingTypes) {
		this(showParamNames, showUniqueKeys, showPositions, shortContext, showMissingTypes, false);
	}
	public CompletionTestsRequestor2(boolean showParamNames, boolean showUniqueKeys, boolean showPositions, boolean shortContext, boolean showMissingTypes, boolean showTokenPositions) {
		this(false, showParamNames, showUniqueKeys, showPositions, shortContext, showMissingTypes, showTokenPositions, false);
	}
	public CompletionTestsRequestor2(
			boolean ignoreAll,
			boolean showParamNames,
			boolean showUniqueKeys,
			boolean showPositions,
			boolean shortContext,
			boolean showMissingTypes,
			boolean showTokenPositions,
			boolean showModifiers) {
		super(ignoreAll);
		this.showParameterNames = showParamNames;
		this.showUniqueKeys = showUniqueKeys;
		this.showPositions = showPositions;
		this.showTokenPositions =  showTokenPositions;
		this.shortContext = shortContext;
		this.showMissingTypes = showMissingTypes;
		this.showModifiers = showModifiers;
	}
	public void acceptContext(CompletionContext cc) {
		this.context = cc;
	}
	public void accept(CompletionProposal proposal) {
		if (!this.proposalFilter.test(proposal))
			return;

		int length = this.proposals.length;
		if (++this.proposalsPtr== length) {
			System.arraycopy(this.proposals, 0, this.proposals = new CompletionProposal[length+PROPOSALS_INCREMENT], 0, length);
		}
		this.proposals[this.proposalsPtr] = proposal;
	}

	@Override
	public boolean isIgnored(char[] typeName) {
		if (this.typeProposalFilter != null) {
			return this.typeProposalFilter.test(new String(typeName));
		}

		return super.isIgnored(typeName);
	}

	public void allowAllRequiredProposals() {
		for (int i = CompletionProposal.ANONYMOUS_CLASS_DECLARATION; i <= CompletionProposal.ANONYMOUS_CLASS_CONSTRUCTOR_INVOCATION; i++) { //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
			for (int j = CompletionProposal.ANONYMOUS_CLASS_DECLARATION; j <= CompletionProposal.ANONYMOUS_CLASS_CONSTRUCTOR_INVOCATION; j++) { //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
				setAllowsRequiredProposals(i, j, true);
			}
		}
	}

	public void completionFailure(IProblem p) {
		this.problem = p;
	}

	public String getContext() { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		if(this.context == null) return "";

		StringBuilder buffer = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW

		if(!this.shortContext) {
			buffer.append("completion offset="); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			buffer.append(this.context.getOffset()); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			buffer.append('\n'); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL

			buffer.append("completion range=["); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			buffer.append(this.context.getTokenStart()); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			buffer.append(", "); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			buffer.append(this.context.getTokenEnd()); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			buffer.append("]\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL

			char[] token = this.context.getToken();
			buffer.append("completion token="); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			if(token == null) {
				buffer.append("null"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			} else {
				buffer.append('\"'); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				buffer.append(token); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				buffer.append('\"'); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			}
			buffer.append('\n'); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL

			buffer.append("completion token kind="); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			int tokenKind = this.context.getTokenKind();
			if(tokenKind == CompletionContext.TOKEN_KIND_STRING_LITERAL) {
				buffer.append("TOKEN_KIND_STRING_LITERAL"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			} else if(tokenKind == CompletionContext.TOKEN_KIND_NAME) {
				buffer.append("TOKEN_KIND_NAME"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			} else {
				buffer.append("TOKEN_KIND_UNKNOWN"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			}
			buffer.append('\n'); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		}
		char[][] expectedTypesSignatures = this.context.getExpectedTypesSignatures();
		buffer.append("expectedTypesSignatures="); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		if(expectedTypesSignatures == null) {
			buffer.append(this.NULL_LITERAL); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		} else {
			buffer.append('{'); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			for (int i = 0; i < expectedTypesSignatures.length; i++) { //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
				if(i > 0) buffer.append(','); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				buffer.append(expectedTypesSignatures[i]); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL

			}
			buffer.append('}'); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		}
		buffer.append('\n'); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL

		char[][] expectedTypesKeys = this.context.getExpectedTypesKeys();
		buffer.append("expectedTypesKeys="); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		if(expectedTypesSignatures == null) {
			buffer.append(this.NULL_LITERAL); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		} else {
			buffer.append('{'); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			for (int i = 0; i < expectedTypesKeys.length; i++) { //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
				if(i > 0) buffer.append(','); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				buffer.append(expectedTypesKeys[i]); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL

			}
			buffer.append('}'); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		}

		if(!this.shortContext) {
			buffer.append('\n'); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL

			int locationType = this.context.getTokenLocation();
			if (locationType == 0) {
				buffer.append("completion token location=UNKNOWN"); //$NON-NLS-1$ //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			} else {
				buffer.append("completion token location={"); //$NON-NLS-1$ //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				boolean first = true;
				if ((locationType & CompletionContext.TL_MEMBER_START) != 0) {
					if (!first) buffer.append(','); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
					buffer.append("MEMBER_START"); //$NON-NLS-1$ //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
					first = false;
				}
				if ((locationType & CompletionContext.TL_STATEMENT_START) != 0) {
					if (!first) buffer.append(','); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
					buffer.append("STATEMENT_START"); //$NON-NLS-1$ //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
					first = false;
				}
				if ((locationType & CompletionContext.TL_CONSTRUCTOR_START) != 0) {
					if (!first) buffer.append(','); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
					buffer.append("CONSTRUCTOR_START"); //$NON-NLS-1$ //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
					first = false;
				}
				if ((locationType & CompletionContext.TL_IN_IMPORT) != 0) {
					if (!first) buffer.append(','); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
					buffer.append("IN_IMPORT"); //$NON-NLS-1$ //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
					first = false;
				}
				buffer.append('}'); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			}
		}

		if (this.computeEnclosingElement) {
			buffer.append('\n'); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			buffer.append("enclosingElement="); //$NON-NLS-1$ //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			JavaElement enclosingElement = (JavaElement)this.context.getEnclosingElement();
			if (enclosingElement == null) {
				buffer.append("null"); //$NON-NLS-1$ //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			} else {
				buffer.append(enclosingElement.toStringWithAncestors(true /*show resolved info*/)); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			}
		}

		if (this.computeVisibleElements) {
			buffer.append('\n'); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL

			IJavaElement[] visibleElements = this.context.getVisibleElements(this.assignableType);
			buffer.append("visibleElements="); //$NON-NLS-1$ //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			if (visibleElements == null) {
				buffer.append("null"); //$NON-NLS-1$ //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			} else if (visibleElements.length == 0) {
				buffer.append("{}"); //$NON-NLS-1$ //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			} else {
				buffer.append('{'); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				buffer.append('\n'); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				for (int i = 0; i < visibleElements.length; i++) { //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
					JavaElement element = (JavaElement) visibleElements[i];
					buffer.append('\t'); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
					buffer.append(element.toStringWithAncestors(true /*show resolved info*/)); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
					buffer.append(",\n"); //$NON-NLS-1$ //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				}
				buffer.append('}'); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			}
		}

		//buffer.append('\n');


		return buffer.toString();
	}
	public String getProblem() {
		return this.problem == null ? "" : this.problem.getMessage();
	}

	public CompletionProposal[] getProposals() {
		return this.proposals;
	}

	interface ProposalTextSupplier {
		public char[] getProposalText(CompletionProposal p);
	}
	private static ProposalTextSupplier DEFAULT_PROPOSAL_TEXT_SUPPLIER =  (p) -> p.getCompletion();

	/*
	 * Get sorted results in ascending order
	 */
	public String getResults() {
		if(this.proposalsPtr < 0) return "";
		quickSort(this.proposals, 0, this.proposalsPtr);
		return getResultsWithoutSorting();
	}
	public String getDisplayResults() {
		if(this.proposalsPtr < 0) return "";
		quickSort(this.proposals, 0, this.proposalsPtr);
		return getResultsWithoutSorting((p) -> p.getDisplayString());
	}
	/*
	 * Get sorted results in ascending order
	 */
	public String getReversedResults() {
		return getReversedResults(null);
	}
	public String getReversedResults(ProposalTextSupplier supplier) { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		if(this.proposalsPtr < 0) return "";
		Arrays.sort(this.proposals, new Comparator() {
			@Override
			public int compare(Object o1, Object o2) {
				if (o1 == o2)
					return 0;
				if (o1 == null)
					return 1;
				if (o2 == null)
					return -1;
				CompletionProposal p1 = (CompletionProposal) o1;
				CompletionProposal p2 = (CompletionProposal) o2;
				int relDif = p2.getRelevance() - p1.getRelevance();
				if(relDif != 0) return relDif;
				String name1 = getElementName(p1);
				String name2 = getElementName(p2);
				return name1.compareTo(name2);
			}
		});
		return getResultsWithoutSorting(supplier);
	}

	/*
	 * Get unsorted results (ie. same order as they were accepted by requestor)
	 */
	public String getResultsWithoutSorting() {
		return getResultsWithoutSorting(null);
	}
	public String getResultsWithoutSorting(ProposalTextSupplier supplier) {
		if(this.proposalsPtr < 0) return "";
		StringBuilder buffer = printProposal(this.proposals[0], supplier);
		for(int i = 1; i <=this.proposalsPtr; i++) { //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
			if(i > 0) buffer.append('\n'); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			buffer.append(printProposal(this.proposals[i], supplier)); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		}
		return buffer.toString();
	}
	public String[] getStringsResult() {
		return getStringsResult(null);
	}
	public String[] getStringsResult(ProposalTextSupplier supplier) {
		if(this.proposalsPtr < 0) {
			return new String[0];
		}
		String[] strings = new String[this.proposalsPtr+1];
		for (int i=0; i<=this.proposalsPtr; i++) { //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
			strings[i] =  printProposal(this.proposals[i]).toString();
		}
		return strings;
	}
	protected StringBuilder printProposal(CompletionProposal proposal) {
		StringBuilder buffer = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		return printProposal(proposal, null, 0, buffer);
	}
	protected StringBuilder printProposal(CompletionProposal proposal, ProposalTextSupplier supplier) {
		StringBuilder buffer = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		return printProposal(proposal, supplier, 0, buffer);
	}

	protected StringBuilder printProposal(CompletionProposal proposal, int tab, StringBuilder buffer) {
		return printProposal(proposal, null, tab, buffer);
	}
	protected StringBuilder printProposal(CompletionProposal proposal, ProposalTextSupplier textSupplier, int tab, StringBuilder buffer) { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		if (textSupplier == null) {
			textSupplier = DEFAULT_PROPOSAL_TEXT_SUPPLIER;
		}
		for (int i = 0; i < tab; i++) { //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
			buffer.append("   "); //$NON-NLS-1$ //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		}
		buffer.append(getElementName(proposal)); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buffer.append('['); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		switch(proposal.getKind()) {
			case CompletionProposal.ANONYMOUS_CLASS_DECLARATION :
				buffer.append("ANONYMOUS_CLASS_DECLARATION"); //$NON-NLS-1$ //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				break;
			case CompletionProposal.FIELD_REF :
				buffer.append("FIELD_REF"); //$NON-NLS-1$ //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				break;
			case CompletionProposal.FIELD_REF_WITH_CASTED_RECEIVER :
				buffer.append("FIELD_REF_WITH_CASTED_RECEIVER"); //$NON-NLS-1$ //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				break;
			case CompletionProposal.KEYWORD :{
				if(isRestrictedIdentifier(textSupplier.getProposalText(proposal)))
						buffer.append("RESTRICTED_IDENTIFIER"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
					else
						buffer.append("KEYWORD"); //$NON-NLS-1$ //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			}
				break;
			case CompletionProposal.LABEL_REF :
				buffer.append("LABEL_REF"); //$NON-NLS-1$ //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				break;
			case CompletionProposal.LOCAL_VARIABLE_REF :
				buffer.append("LOCAL_VARIABLE_REF"); //$NON-NLS-1$ //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				break;
			case CompletionProposal.METHOD_DECLARATION :
				buffer.append("METHOD_DECLARATION"); //$NON-NLS-1$ //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				if(proposal.isConstructor()) {
					buffer.append("<CONSTRUCTOR>"); //$NON-NLS-1$ //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				}
				break;
			case CompletionProposal.METHOD_REF :
				buffer.append("METHOD_REF"); //$NON-NLS-1$ //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				if(proposal.isConstructor()) {
					buffer.append("<CONSTRUCTOR>"); //$NON-NLS-1$ //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				}
				break;
			case CompletionProposal.METHOD_REF_WITH_CASTED_RECEIVER :
				buffer.append("METHOD_REF_WITH_CASTED_RECEIVER"); //$NON-NLS-1$ //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				if(proposal.isConstructor()) {
					buffer.append("<CONSTRUCTOR>"); //$NON-NLS-1$ //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				}
				break;
			case CompletionProposal.MODULE_DECLARATION :
				buffer.append("MODULE_DECLARATION"); //$NON-NLS-1$ //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				break;
			case CompletionProposal.MODULE_REF :
				buffer.append("MODULE_REF"); //$NON-NLS-1$ //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				break;
			case CompletionProposal.PACKAGE_REF :
				buffer.append("PACKAGE_REF"); //$NON-NLS-1$ //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				break;
			case CompletionProposal.TYPE_REF :
				buffer.append("TYPE_REF"); //$NON-NLS-1$ //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				if (proposal.getArrayDimensions() > 0) {
					buffer.append("<ARRAY>"); //$NON-NLS-1$ //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				}
				break;
			case CompletionProposal.VARIABLE_DECLARATION :
				buffer.append("VARIABLE_DECLARATION"); //$NON-NLS-1$ //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				break;
			case CompletionProposal.POTENTIAL_METHOD_DECLARATION :
				buffer.append("POTENTIAL_METHOD_DECLARATION"); //$NON-NLS-1$ //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				break;
			case CompletionProposal.METHOD_NAME_REFERENCE :
				buffer.append("METHOD_NAME_REFERENCE"); //$NON-NLS-1$ //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				break;
			case CompletionProposal.ANNOTATION_ATTRIBUTE_REF :
				buffer.append("ANNOTATION_ATTRIBUTE_REF"); //$NON-NLS-1$ //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				break;
			case CompletionProposal.JAVADOC_BLOCK_TAG :
				buffer.append("JAVADOC_BLOCK_TAG"); //$NON-NLS-1$ //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				break;
			case CompletionProposal.JAVADOC_INLINE_TAG :
				buffer.append("JAVADOC_INLINE_TAG"); //$NON-NLS-1$ //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				break;
			case CompletionProposal.JAVADOC_FIELD_REF:
				buffer.append("JAVADOC_FIELD_REF"); //$NON-NLS-1$ //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				break;
			case CompletionProposal.JAVADOC_METHOD_REF :
				buffer.append("JAVADOC_METHOD_REF"); //$NON-NLS-1$ //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				break;
			case CompletionProposal.JAVADOC_TYPE_REF :
				buffer.append("JAVADOC_TYPE_REF"); //$NON-NLS-1$ //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				break;
			case CompletionProposal.JAVADOC_PARAM_REF :
				buffer.append("JAVADOC_PARAM_REF"); //$NON-NLS-1$ //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				break;
			case CompletionProposal.JAVADOC_VALUE_REF :
				buffer.append("JAVADOC_VALUE_REF"); //$NON-NLS-1$ //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				break;
			case CompletionProposal.FIELD_IMPORT :
				buffer.append("FIELD_IMPORT"); //$NON-NLS-1$ //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				break;
			case CompletionProposal.METHOD_IMPORT :
				buffer.append("METHOD_IMPORT"); //$NON-NLS-1$ //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				break;
			case CompletionProposal.TYPE_IMPORT :
				buffer.append("TYPE_IMPORT"); //$NON-NLS-1$ //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				break;
			case CompletionProposal.CONSTRUCTOR_INVOCATION :
				buffer.append("CONSTRUCTOR_INVOCATION"); //$NON-NLS-1$ //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				break;
			case CompletionProposal.ANONYMOUS_CLASS_CONSTRUCTOR_INVOCATION :
				buffer.append("ANONYMOUS_CLASS_CONSTRUCTOR_INVOCATION"); //$NON-NLS-1$ //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				break;
			case CompletionProposal.LAMBDA_EXPRESSION :
				buffer.append("LAMBDA_EXPRESSION"); //$NON-NLS-1$ //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				break;
			default :
				buffer.append("PROPOSAL"); //$NON-NLS-1$ //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				break;

		}
		buffer.append("]{"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buffer.append(textSupplier.getProposalText(proposal) == null ? this.NULL_LITERAL : textSupplier.getProposalText(proposal)); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buffer.append(", "); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buffer.append(proposal.getDeclarationSignature() == null ? this.NULL_LITERAL : proposal.getDeclarationSignature()); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buffer.append(", "); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buffer.append(proposal.getSignature() == null ? this.NULL_LITERAL : proposal.getSignature()); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL

		char[] receiverSignature = proposal.getReceiverSignature();
		if (receiverSignature != null) {
			buffer.append(", "); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			buffer.append(receiverSignature); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		}

		if(this.showUniqueKeys) {
			buffer.append(", "); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			buffer.append(proposal.getDeclarationKey() == null ? this.NULL_LITERAL : proposal.getDeclarationKey()); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			buffer.append(", "); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			buffer.append(proposal.getKey() == null ? this.NULL_LITERAL : proposal.getKey()); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		}
		buffer.append(", "); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buffer.append(proposal.getName() == null ? this.NULL_LITERAL : proposal.getName()); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		if(this.showParameterNames) {
			char[][] parameterNames = proposal.findParameterNames(null);
			buffer.append(", "); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			if(parameterNames == null || parameterNames.length <= 0) {
				buffer.append(this.NULL_LITERAL); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			} else {
				buffer.append("("); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				for (int i = 0; i < parameterNames.length; i++) { //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
					if(i > 0) buffer.append(", "); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
					buffer.append(parameterNames[i]); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				}
				buffer.append(")"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			}
		}

		if(this.showPositions) {
			buffer.append(", "); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			if(this.showTokenPositions || receiverSignature != null) buffer.append("replace"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			buffer.append("["); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			buffer.append(proposal.getReplaceStart()); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			buffer.append(", "); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			buffer.append(proposal.getReplaceEnd()); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			buffer.append("]"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		}
		if(this.showTokenPositions) {
			buffer.append(", token["); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			buffer.append(proposal.getTokenStart()); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			buffer.append(", "); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			buffer.append(proposal.getTokenEnd()); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			buffer.append("]"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		}
		if(this.showPositions && receiverSignature != null) {
			buffer.append(", receiver["); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			buffer.append(proposal.getReceiverStart()); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			buffer.append(", "); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			buffer.append(proposal.getReceiverEnd()); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			buffer.append("]"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		}
		if(this.showModifiers) {
			int flags = proposal.getFlags();
			buffer.append(", "); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			buffer.append(Flags.toString(flags)); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			if (Flags.isDeprecated(flags)) {
				buffer.append(" deprecated"); //$NON-NLS-1$ //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			}
		}
		buffer.append(", "); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buffer.append(proposal.getRelevance()); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buffer.append('}'); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		if(this.showMissingTypes) {
			CompletionProposal[] requiredProposals = proposal.getRequiredProposals();
			if (requiredProposals != null) {
				int length = requiredProposals.length;
				System.arraycopy(requiredProposals, 0, requiredProposals = new CompletionProposal[length], 0, length);
				quickSort(requiredProposals, 0, length - 1);
				for (int i = 0; i < length; i++) { //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
					buffer.append('\n'); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
					printProposal(requiredProposals[i], tab + 1, buffer);
				}
			}
		}
		return buffer;
	}

	private boolean isRestrictedIdentifier(char[] completion) {
		if(CharOperation.equals(completion, RestrictedIdentifiers.RECORD))
			return true;
		if(CharOperation.equals(completion, RestrictedIdentifiers.SEALED))
			return true;
		if(CharOperation.equals(completion, RestrictedIdentifiers.NON_SEALED))
			return true;
		if(CharOperation.equals(completion, RestrictedIdentifiers.PERMITS))
			return true;

		return false;
	}
	protected CompletionProposal[] quickSort(CompletionProposal[] collection, int left, int right) { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		int original_left = left;
		int original_right = right;
		CompletionProposal mid = collection[left + ((right - left)/2)];
		do { //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
			while (compare(mid, collection[left]) > 0) //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
				// s[left] >= mid
				left++;
			while (compare(mid, collection[right]) < 0) //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
				// s[right] <= mid
				right--;
			if (left <= right) {
				CompletionProposal tmp = collection[left];
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

	protected int compare(CompletionProposal proposal1, CompletionProposal proposal2) { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		int relDif = proposal1.getRelevance() - proposal2.getRelevance();
		if(relDif != 0) return relDif;
		String name1 = getElementName(proposal1);
		String name2 = getElementName(proposal2);
		int nameDif = name1.compareTo(name2);
		if(nameDif != 0) return nameDif;
		int kindDif = proposal1.getKind() - proposal2.getKind();
		if(kindDif != 0) return kindDif;
		String completion1 = new String(proposal1.getCompletion());
		String completion2 = new String(proposal2.getCompletion());
		int completionDif = completion1.compareTo(completion2);
		if(completionDif != 0) return completionDif;
		char[] temp = proposal1.getSignature();
		String signature1 = temp == null ? null: new String(temp);
		temp = proposal2.getSignature();
		String signature2 = temp == null ? null: new String(temp);
		int signatureDif = 0;
		if(signature1 != null && signature2 != null) {
			signatureDif = signature1.compareTo(signature2);
		}
		if(signatureDif != 0) return signatureDif;
		temp = proposal1.getDeclarationSignature();
		String declarationSignature1 = temp == null ? null: new String(temp);
		temp = proposal2.getDeclarationSignature();
		String declarationSignature2 = temp == null ? null: new String(temp);
		int declarationSignatureDif = 0;
		if(declarationSignature1 != null && declarationSignature2 != null) {
			declarationSignatureDif = declarationSignature1.compareTo(declarationSignature2);
		}
		if(declarationSignatureDif != 0) return declarationSignatureDif;
		return 0;
	}

	protected String getElementName(CompletionProposal proposal) { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		switch(proposal.getKind()) {
			case CompletionProposal.ANONYMOUS_CLASS_DECLARATION :
				return new String(Signature.getSignatureSimpleName(proposal.getDeclarationSignature()));
			case CompletionProposal.TYPE_REF :
			case CompletionProposal.TYPE_IMPORT :
			case CompletionProposal.JAVADOC_TYPE_REF :
				return new String(Signature.getSignatureSimpleName(proposal.getSignature()));
			case CompletionProposal.FIELD_REF :
			case CompletionProposal.FIELD_REF_WITH_CASTED_RECEIVER :
			case CompletionProposal.KEYWORD:
			case CompletionProposal.LABEL_REF:
			case CompletionProposal.LOCAL_VARIABLE_REF:
			case CompletionProposal.METHOD_REF:
			case CompletionProposal.METHOD_REF_WITH_CASTED_RECEIVER:
			case CompletionProposal.METHOD_DECLARATION:
			case CompletionProposal.VARIABLE_DECLARATION:
			case CompletionProposal.POTENTIAL_METHOD_DECLARATION:
			case CompletionProposal.METHOD_NAME_REFERENCE:
			case CompletionProposal.ANNOTATION_ATTRIBUTE_REF:
			case CompletionProposal.JAVADOC_BLOCK_TAG :
			case CompletionProposal.JAVADOC_INLINE_TAG :
			case CompletionProposal.JAVADOC_FIELD_REF:
			case CompletionProposal.JAVADOC_METHOD_REF :
			case CompletionProposal.JAVADOC_PARAM_REF :
			case CompletionProposal.JAVADOC_VALUE_REF :
			case CompletionProposal.FIELD_IMPORT :
			case CompletionProposal.METHOD_IMPORT :
			case CompletionProposal.CONSTRUCTOR_INVOCATION :
			case CompletionProposal.ANONYMOUS_CLASS_CONSTRUCTOR_INVOCATION :
				return new String(proposal.getName());
			case CompletionProposal.PACKAGE_REF:
			case CompletionProposal.MODULE_DECLARATION:
				return new String(proposal.getDeclarationSignature());
		}
		return "";
	}
	public String toString() {
		return getResults();
	}
	public void setComputeVisibleElements(boolean computeVisibleElements) {
		this.computeVisibleElements = computeVisibleElements;
	}
	public void setAssignableType(String typeSignature) {
		this.assignableType = typeSignature;
	}
	public void setComputeEnclosingElement(boolean computeEnclosingElement) {
		this.computeEnclosingElement = computeEnclosingElement;
	}
	public void setProposalFilter(Predicate<CompletionProposal> proposalFilter) {
		this.proposalFilter = proposalFilter;
	}

	public void setTypeProposalFilter(Predicate<String> typeProposalFilter) {
		this.typeProposalFilter = typeProposalFilter;
	}
	public boolean canUseDiamond(int proposalNo) {
		if (proposalNo < this.proposals.length && this.proposals[proposalNo] != null) {
			return this.proposals[proposalNo].canUseDiamond(this.context);
		}
		return false;
	}

	public String getCompletionNode() {
		if (this.context instanceof InternalCompletionContext) {
			InternalCompletionContext internalCompletionContext = (InternalCompletionContext) this.context;
			ASTNode astNode = internalCompletionContext.getCompletionNode();
			if (astNode != null) return astNode.toString();

		}
		return null;
	}

	public String getCompletionNodeParent() {
		if (this.context instanceof InternalCompletionContext) {
			InternalCompletionContext internalCompletionContext = (InternalCompletionContext) this.context;
			ASTNode astNode = internalCompletionContext.getCompletionNodeParent();
			if (astNode != null) return astNode.toString();

		}
		return null;
	}

	public String getVisibleLocalVariables() {
		if (this.context instanceof InternalCompletionContext) {
			InternalCompletionContext internalCompletionContext = (InternalCompletionContext) this.context;
			ObjectVector locals = internalCompletionContext.getVisibleLocalVariables();
			if (locals != null) return locals.toString();

		}
		return null;
	}

	public String getVisibleFields() {
		if (this.context instanceof InternalCompletionContext) {
			InternalCompletionContext internalCompletionContext = (InternalCompletionContext) this.context;
			ObjectVector fields = internalCompletionContext.getVisibleFields();
			if (fields != null) return fields.toString();

		}
		return null;
	}

	public String getVisibleMethods() {
		if (this.context instanceof InternalCompletionContext) {
			InternalCompletionContext internalCompletionContext = (InternalCompletionContext) this.context;
			ObjectVector methods = internalCompletionContext.getVisibleMethods();
			if (methods != null) return methods.toString();

		}
		return null;
	}

	public char[][] getExpectedTypesSignatures() {
		return this.context.getExpectedTypesSignatures();
	}
}
