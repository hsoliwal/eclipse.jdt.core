/*******************************************************************************
 * Copyright (c) 2008, 2017 IBM Corporation and others.
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
package org.eclipse.jdt.core.tests.dom;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.internal.core.dom.NaiveASTFlattener;

/**
 * Serialize the ast and decorate this ast with markers.
 * Compute also extra information about marked nodes.
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class MarkedASTFlattener extends NaiveASTFlattener {
	public static class DefaultMarkedNodeLabelProvider extends MarkedNodeLabelProvider implements DefaultMarkedNodeLabelProviderOptions {

		private final int options;

		public DefaultMarkedNodeLabelProvider(int options) {
			this.options = options;
		}

		private void appendBinding(ASTNode node, StringBuilder buffer) { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
			buffer.append('['); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			try {
				IBinding binding = resolveBinding(node);
				if (binding != null) {
					boolean first = true;
					if ((this.options & BINDING_KIND) != 0) {
						if (!first) buffer.append(','); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
						first = false;

						appendBindingKind(binding, buffer);
					}
					if ((this.options & BINDING_KEY) != 0) {
						if (!first) buffer.append(','); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
						first = false;

						appendBindingKey(binding, buffer);
					}
					if ((this.options & BINDING_FLAGS) != 0) {
						if (!first) buffer.append(','); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
						first = false;

						appendBindingFlags(binding, buffer);
					}
				} else {
					buffer.append("null"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				}
			} catch (IllegalArgumentException e) {
				buffer.append("N/A"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			}

			buffer.append(']'); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		}

		private void appendBindingFlags(IBinding binding, StringBuilder buffer) { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
			boolean firstFlag = true;
			if (binding.isDeprecated()) {
				if (!firstFlag) buffer.append('|'); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				firstFlag = false;

				buffer.append("DEPRECATED"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			}
			if (binding.isSynthetic()) {
				if (!firstFlag) buffer.append('|'); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				firstFlag = false;

				buffer.append("SYNTHETIC"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			}
			if (binding.isRecovered()) {
				if (!firstFlag) buffer.append('|'); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				firstFlag = false;

				buffer.append("RECOVERED"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			}
		}

		private void appendBindingKey(IBinding binding, StringBuilder buffer) {
			buffer.append(binding.getKey()); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		}

		private void appendBindingKind(IBinding binding, StringBuilder buffer) { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
			switch (binding.getKind()) {
				case IBinding.ANNOTATION:
					buffer.append("ANNOTATION");break; //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				case IBinding.MEMBER_VALUE_PAIR:
					buffer.append("MEMBER_VALUE_PAIR");break; //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				case IBinding.METHOD:
					buffer.append("METHOD");break; //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				case IBinding.PACKAGE:
					buffer.append("PACKAGE");break; //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				case IBinding.TYPE:
					buffer.append("TYPE");break; //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				case IBinding.VARIABLE:
					buffer.append("VARIABLE");break; //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				default:
					buffer.append("UNKNOWN");break; //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			}
		}

		private void appendFlags(ASTNode node, StringBuilder buffer) { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
			boolean firstFlag = true;
			int flags = node.getFlags();
			if ((flags & ASTNode.MALFORMED) != 0) {
				if (!firstFlag) buffer.append('|'); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				firstFlag = false;

				buffer.append("MALFORMED"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			}
			if ((flags & ASTNode.PROTECT) != 0) {
				if (!firstFlag) buffer.append('|'); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				firstFlag = false;

				buffer.append("PROTECT"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			}
			if ((flags & ASTNode.RECOVERED) != 0) {
				if (!firstFlag) buffer.append('|'); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				firstFlag = false;

				buffer.append("RECOVERED"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			}

			// All nodes are ORIGINAL by default. So an information is printed only when the node isn't ORIGINAL
			if ((flags & ASTNode.ORIGINAL) == 0) {
				if (!firstFlag) buffer.append('|'); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				firstFlag = false;

				buffer.append("!ORIGINAL"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			}
		}

		private void appendNodeExtendedPosition(ASTNode node, StringBuilder buffer) { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
			ASTNode root = node.getRoot();

			if (root.getNodeType() == ASTNode.COMPILATION_UNIT) {
				CompilationUnit cu = (CompilationUnit) root;

				int extendedStartPosition = cu.getExtendedStartPosition(node);
				int extendedLength = cu.getExtendedLength(node);
				if (extendedStartPosition != node.getStartPosition() ||
						extendedLength != node.getLength()) {
					buffer.append('['); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
					buffer.append(cu.getExtendedStartPosition(node)); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
					buffer.append(','); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
					buffer.append(cu.getExtendedLength(node)); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
					buffer.append(']'); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				}

			} else {
				buffer.append("[N/A]"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			}

		}

		private void appendNodePosition(ASTNode node, StringBuilder buffer) {
			buffer.append('['); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			buffer.append(node.getStartPosition()); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			buffer.append(','); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			buffer.append(node.getLength()); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			buffer.append(']'); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		}

		private void appendNodeType(ASTNode node, StringBuilder buffer) { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
			switch (node.getNodeType()) {
				case ASTNode.ANNOTATION_TYPE_DECLARATION :
					buffer.append("ANNOTATION_TYPE_DECLARATION");break; //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				case ASTNode.ANNOTATION_TYPE_MEMBER_DECLARATION :
					buffer.append("ANNOTATION_TYPE_MEMBER_DECLARATION");break; //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				case ASTNode.ANONYMOUS_CLASS_DECLARATION :
					buffer.append("ANONYMOUS_CLASS_DECLARATION");break; //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				case ASTNode.ARRAY_ACCESS :
					buffer.append("ARRAY_ACCESS");break; //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				case ASTNode.ARRAY_CREATION :
					buffer.append("ARRAY_CREATION");break; //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				case ASTNode.ARRAY_INITIALIZER :
					buffer.append("ARRAY_INITIALIZER");break; //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				case ASTNode.ARRAY_TYPE :
					buffer.append("ARRAY_TYPE");break; //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				case ASTNode.ASSERT_STATEMENT :
					buffer.append("ASSERT_STATEMENT");break; //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				case ASTNode.ASSIGNMENT :
					buffer.append("ASSIGNMENT");break; //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				case ASTNode.BLOCK :
					buffer.append("BLOCK");break; //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				case ASTNode.BLOCK_COMMENT :
					buffer.append("BLOCK_COMMENT");break; //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				case ASTNode.BOOLEAN_LITERAL :
					buffer.append("BOOLEAN_LITERAL");break; //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				case ASTNode.BREAK_STATEMENT :
					buffer.append("BREAK_STATEMENT");break; //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				case ASTNode.CAST_EXPRESSION :
					buffer.append("CAST_EXPRESSION");break; //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				case ASTNode.CATCH_CLAUSE :
					buffer.append("CATCH_CLAUSE");break; //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				case ASTNode.CHARACTER_LITERAL :
					buffer.append("CHARACTER_LITERAL");break; //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				case ASTNode.CLASS_INSTANCE_CREATION :
					buffer.append("CLASS_INSTANCE_CREATION");break; //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				case ASTNode.COMPILATION_UNIT :
					buffer.append("COMPILATION_UNIT");break; //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				case ASTNode.CONDITIONAL_EXPRESSION :
					buffer.append("CONDITIONAL_EXPRESSION");break; //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				case ASTNode.CONSTRUCTOR_INVOCATION :
					buffer.append("CONSTRUCTOR_INVOCATION");break; //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				case ASTNode.CONTINUE_STATEMENT :
					buffer.append("CONTINUE_STATEMENT");break; //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				case ASTNode.DO_STATEMENT :
					buffer.append("DO_STATEMENT");break; //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				case ASTNode.EMPTY_STATEMENT :
					buffer.append("EMPTY_STATEMENT");break; //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				case ASTNode.ENHANCED_FOR_STATEMENT :
					buffer.append("ENHANCED_FOR_STATEMENT");break; //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				case ASTNode.ENUM_CONSTANT_DECLARATION :
					buffer.append("ENUM_CONSTANT_DECLARATION");break; //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				case ASTNode.ENUM_DECLARATION :
					buffer.append("ENUM_DECLARATION");break; //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				case ASTNode.EXPRESSION_STATEMENT :
					buffer.append("EXPRESSION_STATEMENT");break; //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				case ASTNode.EXPORTS_DIRECTIVE :
					buffer.append("EXPORTS_STATEMENT");break; //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				case ASTNode.FIELD_ACCESS :
					buffer.append("FIELD_ACCESS");break; //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				case ASTNode.FIELD_DECLARATION :
					buffer.append("FIELD_DECLARATION");break; //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				case ASTNode.FOR_STATEMENT :
					buffer.append("FOR_STATEMENT");break; //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				case ASTNode.IF_STATEMENT :
					buffer.append("IF_STATEMENT");break; //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				case ASTNode.IMPORT_DECLARATION :
					buffer.append("IMPORT_DECLARATION");break; //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				case ASTNode.INFIX_EXPRESSION :
					buffer.append("INFIX_EXPRESSION");break; //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				case ASTNode.INITIALIZER :
					buffer.append("INITIALIZER");break; //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				case ASTNode.INSTANCEOF_EXPRESSION :
					buffer.append("INSTANCEOF_EXPRESSION");break; //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				case ASTNode.JAVADOC :
					buffer.append("JAVADOC");break; //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				case ASTNode.LABELED_STATEMENT :
					buffer.append("LABELED_STATEMENT");break; //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				case ASTNode.LINE_COMMENT :
					buffer.append("LINE_COMMENT");break; //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				case ASTNode.MARKER_ANNOTATION :
					buffer.append("MARKER_ANNOTATION");break; //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				case ASTNode.MEMBER_REF :
					buffer.append("MEMBER_REF");break; //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				case ASTNode.MEMBER_VALUE_PAIR :
					buffer.append("MEMBER_VALUE_PAIR");break; //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				case ASTNode.METHOD_DECLARATION :
					buffer.append("METHOD_DECLARATION");break; //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				case ASTNode.METHOD_INVOCATION :
					buffer.append("METHOD_INVOCATION");break; //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				case ASTNode.METHOD_REF :
					buffer.append("METHOD_REF");break; //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				case ASTNode.METHOD_REF_PARAMETER :
					buffer.append("METHOD_REF_PARAMETER");break; //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				case ASTNode.MODIFIER :
					buffer.append("MODIFIER");break; //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				case ASTNode.MODULE_DECLARATION :
					buffer.append("MODULE_DECLARATION");break; //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				case ASTNode.NORMAL_ANNOTATION :
					buffer.append("NORMAL_ANNOTATION");break; //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				case ASTNode.NULL_LITERAL :
					buffer.append("NULL_LITERAL");break; //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				case ASTNode.NUMBER_LITERAL :
					buffer.append("NUMBER_LITERAL");break; //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				case ASTNode.OPENS_DIRECTIVE :
					buffer.append("OPENS_STATEMENT");break; //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				case ASTNode.PACKAGE_DECLARATION :
					buffer.append("PACKAGE_DECLARATION");break; //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				case ASTNode.PARAMETERIZED_TYPE :
					buffer.append("PARAMETERIZED_TYPE");break; //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				case ASTNode.PARENTHESIZED_EXPRESSION :
					buffer.append("PARENTHESIZED_EXPRESSION");break; //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				case ASTNode.POSTFIX_EXPRESSION :
					buffer.append("POSTFIX_EXPRESSION");break; //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				case ASTNode.PREFIX_EXPRESSION :
					buffer.append("PREFIX_EXPRESSION");break; //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				case ASTNode.PRIMITIVE_TYPE :
					buffer.append("PRIMITIVE_TYPE");break; //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				case ASTNode.PROVIDES_DIRECTIVE :
					buffer.append("PROVIDES_STATEMENT");break; //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				case ASTNode.QUALIFIED_NAME :
					buffer.append("QUALIFIED_NAME");break; //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				case ASTNode.QUALIFIED_TYPE :
					buffer.append("QUALIFIED_TYPE");break; //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				case ASTNode.REQUIRES_DIRECTIVE :
					buffer.append("REQUIRES_STATEMENT");break; //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				case ASTNode.RETURN_STATEMENT :
					buffer.append("RETURN_STATEMENT");break; //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				case ASTNode.SIMPLE_NAME :
					buffer.append("SIMPLE_NAME");break; //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				case ASTNode.SIMPLE_TYPE :
					buffer.append("SIMPLE_TYPE");break; //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				case ASTNode.SINGLE_MEMBER_ANNOTATION :
					buffer.append("SINGLE_MEMBER_ANNOTATION");break; //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				case ASTNode.SINGLE_VARIABLE_DECLARATION :
					buffer.append("SINGLE_VARIABLE_DECLARATION");break; //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				case ASTNode.STRING_LITERAL :
					buffer.append("STRING_LITERAL");break; //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				case ASTNode.SUPER_CONSTRUCTOR_INVOCATION :
					buffer.append("SUPER_CONSTRUCTOR_INVOCATION");break; //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				case ASTNode.SUPER_FIELD_ACCESS :
					buffer.append("SUPER_FIELD_ACCESS");break; //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				case ASTNode.SUPER_METHOD_INVOCATION :
					buffer.append("SUPER_METHOD_INVOCATION");break; //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				case ASTNode.SWITCH_CASE:
					buffer.append("SWITCH_CASE");break; //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				case ASTNode.SWITCH_STATEMENT :
					buffer.append("SWITCH_STATEMENT");break; //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				case ASTNode.SYNCHRONIZED_STATEMENT :
					buffer.append("SYNCHRONIZED_STATEMENT");break; //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				case ASTNode.TAG_ELEMENT :
					buffer.append("TAG_ELEMENT");break; //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				case ASTNode.TEXT_ELEMENT :
					buffer.append("TEXT_ELEMENT");break; //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				case ASTNode.THIS_EXPRESSION :
					buffer.append("THIS_EXPRESSION");break; //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				case ASTNode.THROW_STATEMENT :
					buffer.append("THROW_STATEMENT");break; //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				case ASTNode.TRY_STATEMENT :
					buffer.append("TRY_STATEMENT");break; //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				case ASTNode.TYPE_DECLARATION :
					buffer.append("TYPE_DECLARATION");break; //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				case ASTNode.TYPE_DECLARATION_STATEMENT :
					buffer.append("TYPE_DECLARATION_STATEMENT");break; //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				case ASTNode.TYPE_LITERAL :
					buffer.append("TYPE_LITERAL");break; //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				case ASTNode.TYPE_PARAMETER :
					buffer.append("TYPE_PARAMETER");break; //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				case ASTNode.USES_DIRECTIVE :
					buffer.append("USES_STATEMENT");break; //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				case ASTNode.VARIABLE_DECLARATION_EXPRESSION :
					buffer.append("VARIABLE_DECLARATION_EXPRESSION");break; //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				case ASTNode.VARIABLE_DECLARATION_FRAGMENT :
					buffer.append("VARIABLE_DECLARATION_FRAGMENT");break; //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				case ASTNode.VARIABLE_DECLARATION_STATEMENT :
					buffer.append("VARIABLE_DECLARATION_STATEMENT");break; //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				case ASTNode.WHILE_STATEMENT :
					buffer.append("WHILE_STATEMENT");break; //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				case ASTNode.WILDCARD_TYPE :
					buffer.append("WILDCARD_TYPE");break; //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				default:
					buffer.append("UNKNOWN"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			}
		}

		@Override //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		public String getText(ASTNode node) {
			StringBuilder buffer = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW

			boolean first = true;

			if ((this.options & NODE_TYPE) != 0) {
				if (!first) buffer.append(','); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				first = false;

				appendNodeType(node, buffer);
			}
			if ((this.options & NODE_POSITION) != 0) {
				if (!first) buffer.append(','); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				first = false;

				appendNodePosition(node, buffer);
			}

			if ((this.options & NODE_EXTENDED_POSITION) != 0) {
				if (!first) buffer.append(','); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				first = false;

				appendNodeExtendedPosition(node, buffer);
			}

			if ((this.options & NODE_FLAGS) != 0) {
				if (!first) buffer.append(','); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				first = false;

				appendFlags(node, buffer);
			}

			if ((this.options & BINDING_OPTIONS) != 0) {
				if (!first) buffer.append(','); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				first = false;

				appendBinding(node, buffer);
			}

			return buffer.toString();
		}

		protected IBinding resolveBinding(ASTNode node) { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
			switch (node.getNodeType()) {
				case ASTNode.PACKAGE_DECLARATION:
					return ((PackageDeclaration) node).resolveBinding();
				case ASTNode.TYPE_DECLARATION:
					return ((TypeDeclaration) node).resolveBinding();
				case ASTNode.ANONYMOUS_CLASS_DECLARATION:
					return ((AnonymousClassDeclaration) node).resolveBinding();
				case ASTNode.TYPE_DECLARATION_STATEMENT:
					return ((TypeDeclarationStatement) node).resolveBinding();
				case ASTNode.METHOD_DECLARATION:
					return ((MethodDeclaration) node).resolveBinding();
				case ASTNode.METHOD_INVOCATION:
					return ((MethodInvocation) node).resolveMethodBinding();
				case ASTNode.TYPE_PARAMETER:
					return ((TypeParameter) node).resolveBinding();
				case ASTNode.PARAMETERIZED_TYPE:
					return ((ParameterizedType) node).resolveBinding();
				case ASTNode.WILDCARD_TYPE:
					return ((WildcardType) node).resolveBinding();
				case ASTNode.SIMPLE_NAME:
					return ((SimpleName) node).resolveBinding();
				case ASTNode.ARRAY_TYPE:
					return ((ArrayType) node).resolveBinding();
				case ASTNode.ASSIGNMENT:
					return ((Assignment) node).getRightHandSide().resolveTypeBinding();
				case ASTNode.SIMPLE_TYPE:
					return ((SimpleType) node).resolveBinding();
				case ASTNode.QUALIFIED_NAME:
					return ((QualifiedName) node).resolveBinding();
				case ASTNode.MARKER_ANNOTATION:
					return ((MarkerAnnotation) node).resolveAnnotationBinding();
				case ASTNode.NORMAL_ANNOTATION:
					return ((NormalAnnotation) node).resolveAnnotationBinding();
				case ASTNode.SINGLE_MEMBER_ANNOTATION:
					return ((SingleMemberAnnotation) node).resolveAnnotationBinding();
				case ASTNode.VARIABLE_DECLARATION_FRAGMENT:
					return ((VariableDeclarationFragment) node).resolveBinding();
				default:
					throw new IllegalArgumentException();
			}
		}
	}

	/**
	 * Compute extra information about a marked node
	 */
	public static abstract class MarkedNodeLabelProvider {
		public abstract String getText(ASTNode node);
	}

	private final static String AST_DELIMITER = "===== AST =====";
	private final static String DETAILS_DELIMITER = "===== Details =====";
	private final static String PROBLEMS_DELIMITER = "===== Problems =====";

	private final static String NO_PROBLEM = "No problem";
	private static final String NO_CORRESPONDING_NODE = "No corresponding node";

	// options
	private final boolean reportAST;
	private final boolean reportProblems;

	private String source;
	private CompilationUnit unit;
	private AbstractASTTests.MarkerInfo markerInfo;

	private Map markerFromNode;
	private Map nodeFromMarker;
	private Map markerPositonInBuffer;

	private boolean[] foundNodeFromMarker;
	private final StringBuilder markedNodesBuffer;

	private final MarkedNodeLabelProvider labelProvider;

	public MarkedASTFlattener(
			boolean reportAST,
			boolean reportProblems,
			MarkedNodeLabelProvider labelProvider) {

		this.reportAST = reportAST;
		this.reportProblems = reportProblems;

		this.markedNodesBuffer = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		this.labelProvider = labelProvider;
	}

	public String getResult() { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		StringBuilder resultBuffer = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW

		if (this.reportAST) {
			resultBuffer.append(AST_DELIMITER); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			resultBuffer.append('\n'); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			resultBuffer.append(super.getResult()); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			resultBuffer.append('\n'); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		}

		resultBuffer.append(DETAILS_DELIMITER); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		resultBuffer.append(this.markedNodesBuffer); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL

		if (this.reportProblems) {
			resultBuffer.append('\n'); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			resultBuffer.append(PROBLEMS_DELIMITER); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			resultBuffer.append('\n'); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL

			StringBuilder problemBuffer = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
			IProblem[] problems = this.unit.getProblems();
			int problemCount = problems.length;
			if (problemCount != 0) {
				for (int i = 0; i < problemCount; i++) { //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
					org.eclipse.jdt.core.tests.util.Util.appendProblem(problemBuffer, problems[i], this.source == null ? null : this.source.toCharArray() , i + 1);
				}
			} else {
				problemBuffer.append(NO_PROBLEM); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			}
			resultBuffer.append(Util.convertToIndependantLineDelimiter(problemBuffer.toString())); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		}

		return resultBuffer.toString();
	}

	public void postVisit(ASTNode node) {
		String markerName;
		if (this.reportAST && (markerName = (String)this.markerFromNode.get(node)) != null) {
			// add start marker
			int pos = ((Integer)this.markerPositonInBuffer.get(markerName)).intValue();
			while (Character.isWhitespace(this.buffer.charAt(pos))) {pos++;} //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
			this.buffer.insert(pos, this.markerInfo.markerStartStart + markerName + this.markerInfo.markerStartEnd); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT

			// add end marker
			pos = this.buffer.length() - 1;
			while (Character.isWhitespace(this.buffer.charAt(pos))) {pos--;} //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
			this.buffer.insert(pos + 1, this.markerInfo.markerEndStart + markerName + this.markerInfo.markerEndEnd); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		}
	}
	public void preVisit(ASTNode node) { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		String markerName = null;
		int index = -1;
		found : while ((index = this.markerInfo.indexOfASTStart(node.getStartPosition(), index + 1)) != -1) { //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
			if (node.getStartPosition() + node.getLength() == this.markerInfo.astEnds[index]) {
				markerName = String.valueOf(index + 1);

				if (this.nodeFromMarker.get(markerName) == null) {
					this.markerFromNode.put(node, markerName);
					this.nodeFromMarker.put(markerName, node);
					this.markerPositonInBuffer.put(markerName, Integer.valueOf(this.buffer.length()));
					this.foundNodeFromMarker[index] = true;
				}

				break found;
			}
		}

		if (markerName != null) {
			this.markedNodesBuffer.append('\n'); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			this.markedNodesBuffer.append(markerName); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			this.markedNodesBuffer.append(':'); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			this.markedNodesBuffer.append(this.labelProvider.getText(node)); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		}
	}

	public void process(CompilationUnit cu, AbstractASTTests.MarkerInfo mf) { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		this.source = mf.source;
		this.unit = cu;
		this.markerInfo = mf;

		this.markerFromNode = new HashMap(); //FLLDCIM: MARK — M01 M3_HASH_LANES NEW
		this.nodeFromMarker = new HashMap(); //FLLDCIM: MARK — M01 M3_HASH_LANES NEW
		this.markerPositonInBuffer = new HashMap(); //FLLDCIM: MARK — M01 M3_HASH_LANES NEW
		int length = mf.astStarts.length;
		this.foundNodeFromMarker = new boolean[length];
		this.unit.accept(this);

		for (int i = 0; i < length; i++) { //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
			if (!this.foundNodeFromMarker[i]) {
				this.markedNodesBuffer.append('\n'); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				this.markedNodesBuffer.append(String.valueOf(i + 1)); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				this.markedNodesBuffer.append(':'); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				this.markedNodesBuffer.append(NO_CORRESPONDING_NODE); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			}
		}
	}
}
