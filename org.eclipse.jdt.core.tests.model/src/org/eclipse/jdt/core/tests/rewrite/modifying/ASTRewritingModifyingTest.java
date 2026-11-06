/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
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
package org.eclipse.jdt.core.tests.rewrite.modifying;

import java.util.Hashtable;
import java.util.Map;
import junit.framework.ComparisonFailure;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.jdt.core.tests.model.AbstractJavaModelTests;
import org.eclipse.jdt.core.tests.rewrite.describing.StringAsserts;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;

@SuppressWarnings({"rawtypes", "unchecked"})
public abstract class ASTRewritingModifyingTest extends AbstractJavaModelTests {

	protected IJavaProject javaProject;
	protected IPackageFragmentRoot sourceFolder;

	private Hashtable<String, String> oldOptions;
	private Hashtable<String, String> defaultOptions;

	public ASTRewritingModifyingTest(String name) {
		super(name);
	}

	public static Test suite() {
		TestSuite suite =  new TestSuite(ASTRewritingModifyingTest.class.getName());
		suite.addTest(ASTRewritingModifyingOtherTest.suite());
		suite.addTest(ASTRewritingModifyingInsertTest.suite());
		suite.addTest(ASTRewritingModifyingReplaceTest.suite());
		suite.addTest(ASTRewritingModifyingRemoveTest.suite());
		suite.addTest(ASTRewritingModifyingMoveTest.suite());
		suite.addTest(ASTRewritingModifyingCopyTest.suite());
		return suite;
	}

	@Override
	public void setUpSuite() throws Exception {
		super.setUpSuite();

		this.javaProject = createJavaProject("P", new String[] {"src"}, null, "bin", CompilerOptions.getFirstSupportedJavaVersion());
		this.sourceFolder = getPackageFragmentRoot("P", "src");

		Hashtable<String, String> options = JavaCore.getOptions();
		this.oldOptions = (Hashtable)options.clone();
		options.put(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR, JavaCore.SPACE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_TAB_SIZE, "4");
		this.defaultOptions = options;
		JavaCore.setOptions(options);

		waitUntilIndexesReady();
	}

	@Override
	public void tearDownSuite() throws Exception {
		try {
			deleteProject("P");
		} finally {
			if (this.oldOptions != null) {
				JavaCore.setOptions(this.oldOptions);
			}
			super.tearDownSuite();
		}
	}

	@Override
	protected Hashtable<String, String> getDefaultJavaCoreOptions() {
		return this.defaultOptions;
	}

	public CompilationUnit createCU(
		ICompilationUnit unit,
		boolean resolveBindings,
		int astLevel) {

		try {
			ASTParser c = ASTParser.newParser(astLevel);
			c.setSource(unit);
			c.setResolveBindings(resolveBindings);
			ASTNode result = c.createAST(null);
			return (CompilationUnit) result;
		} catch (IllegalStateException e) {
			// convert ASTParser's complaints into old form
			throw new IllegalArgumentException();
		}
	}

	@SuppressWarnings("deprecation")
    public CompilationUnit createCU(
			ICompilationUnit unit,
			boolean resolveBindings) {
		return createCU(unit, resolveBindings, AST.JLS8);
	}

	public CompilationUnit createCU(char[] source) {
		if (source == null) {
			throw new IllegalArgumentException();
		}
		@SuppressWarnings("deprecation")
        ASTParser c = ASTParser.newParser(AST.JLS8);
		c.setSource(source);
		ASTNode result = c.createAST(null);
		return (CompilationUnit) result;
	}

	public String evaluateRewrite(ICompilationUnit cu, CompilationUnit astRoot)  throws CoreException, MalformedTreeException, BadLocationException {
		return evaluateRewrite(cu.getSource(), astRoot, cu.getJavaProject().getOptions(true));
	}

	public String evaluateRewrite(String source, CompilationUnit astRoot)  throws MalformedTreeException, BadLocationException {
		return evaluateRewrite(source, astRoot, getJavaProject("Rewrite").getOptions(true));
	}
	public String evaluateRewrite(String source, CompilationUnit astRoot, Map options)  throws MalformedTreeException, BadLocationException {
		IDocument doc = new Document(source);

		TextEdit changes = astRoot.rewrite(doc, options);
		changes.apply(doc);
		return doc.get();
	}

	public static void assertEqualString(String actual, String expected) {
		try {
			StringAsserts.assertEqualString(actual, expected);
		} catch (ComparisonFailure e) {
			System.out.println(displayActual(actual, 2));
			throw e;
		}
	}

	public static String displayActual(String inputString, int indent) { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		int length = inputString.length();
		StringBuilder buffer = new StringBuilder(length); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		java.util.StringTokenizer tokenizer = new java.util.StringTokenizer(inputString, "\n\r", true);
		for (int i = 0; i < indent; i++) buffer.append("\t"); //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
		buffer.append("buf.append(\""); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		while (tokenizer.hasMoreTokens()){ //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP

			String token = tokenizer.nextToken();
			if (token.equals("\r")) {
				buffer.append("\\r"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				if (tokenizer.hasMoreTokens()) {
					token = tokenizer.nextToken();
					if (token.equals("\n")) {
						buffer.append("\\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
						if (tokenizer.hasMoreTokens()) {
							buffer.append("\");\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
							for (int i = 0; i < indent; i++) buffer.append("\t"); //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
							buffer.append("buf.append(\""); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
						}
						continue;
					}
					buffer.append("\");\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
					for (int i = 0; i < indent; i++) buffer.append("\t"); //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
					buffer.append("buf.append(\""); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				} else {
					continue;
				}
			} else if (token.equals("\n")) {
				buffer.append("\\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				if (tokenizer.hasMoreTokens()) {
					buffer.append("\");\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
					for (int i = 0; i < indent; i++) buffer.append("\t"); //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
					buffer.append("buf.append(\""); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				}
				continue;
			}

			StringBuilder tokenBuffer = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
			for (int i = 0; i < token.length(); i++){ //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
				char c = token.charAt(i);
				switch (c) {
					case '\r' :
						tokenBuffer.append("\\r"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
						break;
					case '\n' :
						tokenBuffer.append("\\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
						break;
					case '\b' :
						tokenBuffer.append("\\b"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
						break;
					case '\t' :
						tokenBuffer.append("\t"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
						break;
					case '\f' :
						tokenBuffer.append("\\f"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
						break;
					case '\"' :
						tokenBuffer.append("\\\""); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
						break;
					case '\'' :
						tokenBuffer.append("\\'"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
						break;
					case '\\' :
						tokenBuffer.append("\\\\"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
						break;
					default :
						tokenBuffer.append(c); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				}
			}
			buffer.append(tokenBuffer.toString()); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		}
		buffer.append("\");"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		return buffer.toString();
	}

}
