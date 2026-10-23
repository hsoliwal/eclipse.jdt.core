/*******************************************************************************
 * Copyright (c) 2000, 2021 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *		IBM Corporation - initial API and implementation
 *		Stephan Herrmann - Contribution for Bug 378024 - Ordering of comments between imports not preserved
 *		John Glassmyer <jogl@google.com> - import group sorting is broken - https://bugs.eclipse.org/430303
 *******************************************************************************/
package org.eclipse.jdt.core.tests.rewrite.describing;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import junit.framework.Test;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.BindingKey;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite.ImportRewriteContext;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.jdt.core.tests.model.AbstractJavaModelTests;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;
import org.osgi.service.prefs.BackingStoreException;

@SuppressWarnings({"rawtypes", "unchecked"})
public class ImportRewriteTest extends AbstractJavaModelTests {

	private static final Class THIS= ImportRewriteTest.class;

	protected IPackageFragmentRoot sourceFolder;

	public ImportRewriteTest(String name) {
		super(name);
	}

	public static Test allTests() {
		return new Suite(THIS);
	}

	public static Test suite() {
//		System.err.println("Warning, only part of the ImportRewriteTest are being executed!");
//		Suite suite = new Suite(ImportRewriteTest.class.getName());
//		suite.addTest(new ImportRewriteTest("testRemoveImports1"));
//		return suite;
		return allTests();
	}

	@Override //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
	protected void setUp() throws Exception {
		this.indexDisabledForTest = false;
		super.setUp();

		IJavaProject proj= createJavaProject("P", new String[] {"src"}, new String[] {"JCL18_LIB"}, "bin");
		proj.setOption(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR, JavaCore.SPACE);
		proj.setOption(DefaultCodeFormatterConstants.FORMATTER_TAB_SIZE, "4");
		proj.setOption(JavaCore.COMPILER_COMPLIANCE, CompilerOptions.getFirstSupportedJavaVersion());
		proj.setOption(JavaCore.COMPILER_PB_ASSERT_IDENTIFIER, JavaCore.ERROR);
		proj.setOption(JavaCore.COMPILER_SOURCE, CompilerOptions.getFirstSupportedJavaVersion());
		proj.setOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, CompilerOptions.getFirstSupportedJavaVersion());
		proj.setOption(DefaultCodeFormatterConstants.FORMATTER_NUMBER_OF_EMPTY_LINES_TO_PRESERVE, String.valueOf(99));

		proj.setOption(DefaultCodeFormatterConstants.FORMATTER_BLANK_LINES_BETWEEN_IMPORT_GROUPS, String.valueOf(1));

		// The tests in this class assume that the line separator is "\n".
		new ProjectScope(proj.getProject()).getNode(Platform.PI_RUNTIME).put(Platform.PREF_LINE_SEPARATOR, "\n");

		this.sourceFolder = getPackageFragmentRoot("P", "src");

		waitUntilIndexesReady();
	}

	@Override
	protected void tearDown() throws Exception {
		deleteProject("P");
		super.tearDown();
	}

	/**
	 * Addresses https://bugs.eclipse.org/465566 ("Organize imports does not remove duplicated
	 * imports").
	 */
	public void testDuplicateImportOmittedWhenRestoreExistingImportsIsFalse() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		StringBuilder contents = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		contents.append("package pack1;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("import java.io.Serializable;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("import java.io.Serializable;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("public class Clazz {}"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		ICompilationUnit cu = createCompilationUnit("pack1", "Clazz", contents.toString());

		String[] order = new String[] { "java" };

		ImportRewrite imports = newImportsRewrite(cu, order, 2, 2, false);
		imports.setUseContextToFilterImplicitImports(true);
		imports.addImport("java.io.Serializable");

		apply(imports);

		StringBuilder expected = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		expected.append("package pack1;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("import java.io.Serializable;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("public class Clazz {}"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		assertEqualString(cu.getSource(), expected.toString());
	}

	/**
	 * Expects that imports can be added for types from an unnamed (default) package, and that such
	 * imports are not reduced into an on-demand import. Imports of types from an unnamed package
	 * were legal in versions of Java prior to 1.4.
	 *
	 * Addresses https://bugs.eclipse.org/461863 ("addImport creates .ypename for unqualified type
	 * from default package").
	 */
	public void testAddImportsFromUnnamedPackage() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		ICompilationUnit cu = createCompilationUnit("pack1", "C");

		String[] order = new String[] { "java" };

		ImportRewrite imports = newImportsRewrite(cu, order, 2, 2, false);
		imports.setUseContextToFilterImplicitImports(true);
		imports.addImport("java.util.ArrayDeque");
		imports.addImport("java.util.ArrayList");
		imports.addImport("Bar");
		imports.addImport("Foo");

		apply(imports);

		StringBuilder expected = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		expected.append("package pack1;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("import java.util.*;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("import Bar;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("import Foo;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("public class C {}"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		assertEqualString(cu.getSource(), expected.toString());
	}

	/**
	 * Addresses https://bugs.eclipse.org/412929 ("Adding a type results in adding a package and
	 * later does not honor order").
	 */
	public void testImportGroupMatchingQualifiedName() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		ICompilationUnit cu = createCompilationUnit("pack1", "C");

		String[] order = new String[] { "#android.R.doFoo", "android.R", "java", "android" };

		ImportRewrite imports = newImportsRewrite(cu, order, 999, 999, false);
		imports.setUseContextToFilterImplicitImports(true);
		imports.addImport("android.R");
		imports.addImport("java.util.List");
		imports.addImport("android.Foo");
		imports.addStaticImport("android.R", "doFoo", false);

		apply(imports);

		StringBuilder expected = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		expected.append("package pack1;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("import static android.R.doFoo;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("import android.R;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("import java.util.List;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("import android.Foo;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("public class C {}"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		assertEqualString(cu.getSource(), expected.toString());
	}

	/**
	 * Expects that the comments from single imports are reassigned
	 * to a new on-demand import into which they are reduced.
	 */
	public void testReduceNewOnDemand() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		StringBuilder contents = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		contents.append("package pack1;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("import java.io.Serializable;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("// A floating leading\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("// A leading\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("/* A same-line leading */ import java.net.A; // A same-line trailing\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("// A trailing\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("// B floating leading\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("// B leading\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("/* B same-line leading */ import java.net.B; // B same-line trailing\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("// B trailing\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("// C floating leading\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("// C leading\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("/* C same-line leading */ import java.net.C; // C same-line trailing\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("// C trailing\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("import java.util.List;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("public class Clazz {}"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		ICompilationUnit cu = createCompilationUnit("pack1", "Clazz", contents.toString());

		String[] order = new String[] { "java" };

		ImportRewrite imports = newImportsRewrite(cu, order, 1, 1, false);
		imports.setUseContextToFilterImplicitImports(true);
		imports.addImport("java.io.Serializable");
		imports.addImport("java.net.A");
		imports.addImport("java.net.B");
		imports.addImport("java.net.C");
		imports.addImport("java.util.List");

		apply(imports);

		StringBuilder expected = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		expected.append("package pack1;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("import java.io.*;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("// A floating leading\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("// A leading\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("/* A same-line leading */\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("// A same-line trailing\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("// A trailing\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("// B floating leading\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("// B leading\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("/* B same-line leading */\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("// B same-line trailing\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("// B trailing\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("// C floating leading\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("// C leading\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("/* C same-line leading */\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("// C same-line trailing\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("// C trailing\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("import java.net.*;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("import java.util.*;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("public class Clazz {}"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		assertEqualString(cu.getSource(), expected.toString());
	}

	/**
	 * Expects that the comments from single imports are reassigned
	 * to an existing on-demand import into which they are reduced,
	 * and that the on-demand import's own comments are preserved.
	 */
	public void testReduceExistingOnDemand() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		StringBuilder contents = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		contents.append("package pack1;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("import java.io.*;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("// on-demand floating\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("// on-demand leading\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("/* on-demand same-line leading */ import java.net.*; // on-demand same-line trailing\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("// on-demand trailing\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("// A floating leading\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("// A leading\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("/* A same-line leading */ import java.net.A; // A same-line trailing\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("// A trailing\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("// B floating leading\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("// B leading\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("/* B same-line leading */ import java.net.B; // B same-line trailing\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("// B trailing\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("// C floating leading\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("// C leading\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("/* C same-line leading */ import java.net.C; // C same-line trailing\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("// C trailing\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("import java.util.*;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("public class Clazz {}"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		ICompilationUnit cu = createCompilationUnit("pack1", "Clazz", contents.toString());

		String[] order = new String[] { "java.io", "java", "java.util" };

		ImportRewrite imports = newImportsRewrite(cu, order, 1, 1, false);
		imports.setUseContextToFilterImplicitImports(true);
		imports.addImport("java.io.Serializable");
		imports.addImport("java.net.A");
		imports.addImport("java.net.B");
		imports.addImport("java.net.C");
		imports.addImport("java.util.List");

		apply(imports);

		StringBuilder expected = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		expected.append("package pack1;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("import java.io.*;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("// A floating leading\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("// A leading\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("/* A same-line leading */\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("// A same-line trailing\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("// A trailing\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("// B floating leading\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("// B leading\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("/* B same-line leading */\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("// B same-line trailing\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("// B trailing\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("// C floating leading\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("// C leading\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("/* C same-line leading */\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("// C same-line trailing\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("// C trailing\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("// on-demand floating\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("// on-demand leading\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("/* on-demand same-line leading */ import java.net.*; // on-demand same-line trailing\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("// on-demand trailing\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("import java.util.*;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("public class Clazz {}"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		assertEqualString(cu.getSource(), expected.toString());
	}

	/**
	 * Expects that comments from an expanded on-demand import are reassigned
	 * to a corresponding single import, and that comments of other single imports
	 * with the same container name are preserved.
	 */
	public void testExpandOnDemand() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		StringBuilder contents = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		contents.append("package pack1\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("import com.example;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("/* on-demand floating */\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("// on-demand leading\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("/* on-demand same-line leading */ import java.util.*; // on-demand same-line trailing\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("// on-demand trailing\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("/* ArrayList floating */\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("// ArrayList leading\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("/* ArrayList same-line leading */ import java.util.ArrayList; // ArrayList same-line trailing\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("// ArrayList trailing\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("/* List floating */\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("// List leading\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("/* List same-line leading */ import java.util.List; // List same-line trailing\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("// List trailing\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("/* Map floating */\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("// Map leading\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("/* Map same-line leading */ import java.util.Map; // Map same-line trailing\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("// Map trailing\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("import java.net.Socket;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("public class C {}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		ICompilationUnit cu = createCompilationUnit("pack1", "C", contents.toString());

		String[] order = new String[] { "com", "java.util", "java.net" };

		ImportRewrite importRewrite = newImportsRewrite(cu, order, 999, 999, false);
		importRewrite.setUseContextToFilterImplicitImports(true);
		importRewrite.addImport("java.util.ArrayList");
		importRewrite.addImport("java.util.Map");
		importRewrite.addImport("java.util.Set");
		importRewrite.addImport("java.net.Socket");

		apply(importRewrite);

		StringBuilder expected = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		expected.append("package pack1\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("/* on-demand floating */\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("// on-demand leading\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("/* on-demand same-line leading */\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("// on-demand same-line trailing\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("// on-demand trailing\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("/* ArrayList floating */\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("// ArrayList leading\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("/* ArrayList same-line leading */ import java.util.ArrayList; // ArrayList same-line trailing\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("// ArrayList trailing\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("/* Map floating */\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("// Map leading\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("/* Map same-line leading */ import java.util.Map; // Map same-line trailing\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("// Map trailing\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("import java.util.Set;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("import java.net.Socket;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("public class C {}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		assertEqualString(cu.getSource(), expected.toString());
	}

	/**
	 * Expects that the comments of a removed import (other than an expanded on-demand import with
	 * a corresponding single import, or a reduced single import with a correponding on-demand
	 * import) are removed.
	 */
	public void testRemovedImportCommentsAreRemoved() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		IPackageFragment pack1 = this.sourceFolder.createPackageFragment("pack1", false, null);
		StringBuilder contents = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		contents.append("package pack1;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("/* Socket is a very useful class */\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("import java.net.Socket; // Socket to 'em!\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("/* Thank goodness Java has built-in networking libraries! */\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("import java.util.ArrayList;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("public class C {}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		ICompilationUnit cu = pack1.createCompilationUnit("C.java", contents.toString(), false, null);

		String[] order = new String[] { "java" };

		ImportRewrite imports = newImportsRewrite(cu, order, 99, 99, false);
		imports.addImport("java.util.ArrayList");

		apply(imports);

		StringBuilder expected = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		expected.append("package pack1;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("import java.util.ArrayList;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("public class C {}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		assertEqualString(cu.getSource(), expected.toString());
	}

	/**
	 * Addresses https://bugs.eclipse.org/318437 ("Organize Imports ignores Number of Imports needed
	 * for .*") and https://bugs.eclipse.org/359724 ("nested type imports not collapsed to wildcards
	 * ('*')").
	 */
	public void testOnDemandWithinType() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		ICompilationUnit cu = createCompilationUnit("pack1", "C");

		String[] order = new String[] { "java" };

		ImportRewrite imports = newImportsRewrite(cu, order, 1, 1, false);
		imports.setUseContextToFilterImplicitImports(true);
		imports.addImport("java.util.Map.Entry");

		apply(imports);

		StringBuilder expected = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		expected.append("package pack1;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("import java.util.Map.*;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("public class C {}"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		assertEqualString(cu.getSource(), expected.toString());
	}

	/**
	 * Expects that a comment embedded within an import declaration is preserved.
	 */
	public void testCommentWithinImportDeclaration() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		StringBuilder contents = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		contents.append("package pack1;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("import /* comment */ java.util.Map.*;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("public class C {}"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL

		ICompilationUnit cu = createCompilationUnit("pack1", "C", contents.toString());

		String[] order = new String[] { "java" };

		ImportRewrite imports = newImportsRewrite(cu, order, 1, 1, false);
		imports.setUseContextToFilterImplicitImports(true);
		imports.addImport("java.util.Map.*");

		apply(imports);

		StringBuilder expected = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		expected.append("package pack1;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("import /* comment */ java.util.Map.*;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("public class C {}"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		assertEqualString(cu.getSource(), expected.toString());
	}

	/**
	 * Addresses https://bugs.eclipse.org/457051 ("comment is discarded when reducing imports to an
	 * on-demand import").
	 */
	public void testFloatingCommentPreservedWhenReducingOnDemandAbove() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		StringBuilder contents = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		contents.append("package pack1;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("import java.util.Queue;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("/* floating comment */\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("import java.util.concurrent.BlockingDeque;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("public class C {}"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL

		ICompilationUnit cu = createCompilationUnit("pack1", "C", contents.toString());

		String[] order = new String[] { "java" };

		ImportRewrite importRewrite = newImportsRewrite(cu, order, 2, 2, true);
		importRewrite.setUseContextToFilterImplicitImports(true);
		importRewrite.addImport("java.util.Formatter");

		apply(importRewrite);

		StringBuilder expected = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		expected.append("package pack1;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("import java.util.*;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("/* floating comment */\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("import java.util.concurrent.BlockingDeque;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("public class C {}"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		assertEqualString(cu.getSource(), expected.toString());
	}

	/**
	 * Addresses https://bugs.eclipse.org/457089 ("imports are improperly reordered in the presence
	 * of a floating comment").
	 */
	public void testFloatingCommentDoesntCauseImportsToMove() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		StringBuilder contents = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		contents.append("package pack1;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("import java.io.Serializable;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("/* floating comment */\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("import java.util.List;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("import javax.sql.DataSource;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("public class C {}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL

		ICompilationUnit cu = createCompilationUnit("pack1", "C", contents.toString());

		String[] order = new String[] { "java", "javax" };

		ImportRewrite importRewrite = newImportsRewrite(cu, order, 999, 999, false);
		importRewrite.setUseContextToFilterImplicitImports(true);
		importRewrite.addImport("java.io.Serializable");
		importRewrite.addImport("java.util.List");
		importRewrite.addImport("javax.sql.DataSource");

		apply(importRewrite);

		StringBuilder expected = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		expected.append("package pack1;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("import java.io.Serializable;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("/* floating comment */\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("import java.util.List;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("import javax.sql.DataSource;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("public class C {}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		assertEqualString(cu.getSource(), expected.toString());
	}

	public void testAddImportIntoMatchAllImportGroup() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		StringBuilder contents = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		contents.append("package pack1;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("import java.util.ArrayList;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("public class C {}"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL

		ICompilationUnit cu = createCompilationUnit("pack1", "C", contents.toString());

		String[] order = new String[] { "", "java.net" };

		ImportRewrite importRewrite = newImportsRewrite(cu, order, 999, 999, true);
		importRewrite.setUseContextToFilterImplicitImports(true);
		importRewrite.addImport("java.net.Socket");

		apply(importRewrite);

		StringBuilder expected = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		expected.append("package pack1;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("import java.util.ArrayList;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("import java.net.Socket;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("public class C {}"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		assertEqualString(cu.getSource(), expected.toString());
	}

	public void testCuInDefaultPackageWithNoExistingImports() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		StringBuilder contents = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		contents.append("public class C {}"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL

		ICompilationUnit cu = createCompilationUnit("pack1", "C", contents.toString());

		String[] order = new String[] { "java", "java.net" };

		ImportRewrite importRewrite = newImportsRewrite(cu, order, 999, 999, false);
		importRewrite.setUseContextToFilterImplicitImports(true);
		importRewrite.addImport("java.net.Socket");
		importRewrite.addImport("java.util.ArrayList");

		apply(importRewrite);

		StringBuilder expected = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		expected.append("import java.util.ArrayList;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("import java.net.Socket;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("public class C {}"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		assertEqualString(cu.getSource(), expected.toString());
	}

	/**
	 * Addresses https://bugs.eclipse.org/71761 ("ImportRewrite should let me add explicit import to
	 * existing on demand import").
	 */
	public void testNeedsExplicitImport() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		ICompilationUnit cu = createCompilationUnit("pack1", "C");

		String[] order = new String[] { "java" };

		ImportRewriteContext needsExplicitImportContext = new ImportRewriteContext() {
			public int findInContext(String qualifier, String name, int kind) {
				return ImportRewriteContext.RES_NAME_UNKNOWN_NEEDS_EXPLICIT_IMPORT;
			}
		};

		ImportRewrite importRewrite = newImportsRewrite(cu, order, 1, 1, false);
		importRewrite.setUseContextToFilterImplicitImports(true);
		importRewrite.addStaticImport("java.util.Collections", "shuffle", false, needsExplicitImportContext);
		importRewrite.addStaticImport("java.util.Collections", "sort", false);
		importRewrite.addImport("java.util.List", needsExplicitImportContext);
		importRewrite.addImport("java.util.Map");

		apply(importRewrite);

		StringBuilder expected = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		expected.append("package pack1;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("import static java.util.Collections.*;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("import static java.util.Collections.shuffle;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("import java.util.*;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("import java.util.List;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("public class C {}"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		assertEqualString(cu.getSource(), expected.toString());
	}

	public void testOrganizeNoImportsWithOneLineDelim() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		StringBuilder contents = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		contents.append("package pack1;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("public class C {}"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL

		ICompilationUnit cu = createCompilationUnit("pack1", "C", contents.toString());

		String[] order = new String[] { "java" };

		ImportRewrite importRewrite = newImportsRewrite(cu, order, 1, 1, false);
		importRewrite.setUseContextToFilterImplicitImports(true);

		apply(importRewrite);

		StringBuilder expected = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		expected.append("package pack1;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("public class C {}"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		assertEqualString(cu.getSource(), expected.toString());
	}

	public void testOrganizeNoImportsWithTwoLineDelims() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		StringBuilder contents = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		contents.append("package pack1;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("public class C {}"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL

		ICompilationUnit cu = createCompilationUnit("pack1", "C", contents.toString());

		String[] order = new String[] { "java" };

		ImportRewrite importRewrite = newImportsRewrite(cu, order, 1, 1, false);
		importRewrite.setUseContextToFilterImplicitImports(true);

		apply(importRewrite);

		StringBuilder expected = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		expected.append("package pack1;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("public class C {}"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		assertEqualString(cu.getSource(), expected.toString());
	}

	public void testOrganizeNoImportsWithJavadoc() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		StringBuilder contents = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		contents.append("package pack1;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("/**\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append(" * Best class ever.\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append(" */\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("public class C {\n}"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL

		ICompilationUnit cu = createCompilationUnit("pack1", "C", contents.toString());

		String[] order = new String[] { "java" };

		ImportRewrite importRewrite = newImportsRewrite(cu, order, 1, 1, false);
		importRewrite.setUseContextToFilterImplicitImports(true);

		apply(importRewrite);

		StringBuilder expected = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		expected.append("package pack1;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("/**\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append(" * Best class ever.\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append(" */\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("public class C {\n}"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		assertEqualString(cu.getSource(), expected.toString());
	}

	/**
	 * Expects that imports are correctly placed after the end of a package declaration's multiline
	 * trailing comment.
	 */
	public void testPackageDeclarationTrailingComment() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		IPackageFragment pack1 = this.sourceFolder.createPackageFragment("pack1", false, null);
		StringBuilder contents = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		contents.append("package pack1; /* pack1 \n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("trailing \n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("comment */\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("public class C {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		ICompilationUnit cu = pack1.createCompilationUnit("C.java", contents.toString(), false, null);

		String[] order = new String[] { "java" };

		ImportRewrite imports = newImportsRewrite(cu, order, 99, 99, false);
		imports.addImport("java.util.ArrayList");

		apply(imports);

		StringBuilder expected = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		expected.append("package pack1; /* pack1 \n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("trailing \n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("comment */\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("import java.util.ArrayList;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("public class C {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		assertEqualString(cu.getSource(), expected.toString());
	}

	/**
	 * Expects correct placement of an import when package declaration, type declaration, and
	 * associated comments are all on one line.
	 */
	public void testAddImportWithPackageAndTypeOnSameLine() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		IPackageFragment pack1 = this.sourceFolder.createPackageFragment("pack1", false, null);
		StringBuilder contents = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		contents.append("package pack1; /* pack1 trailing */  /** C leading */ public class C {}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		ICompilationUnit cu = pack1.createCompilationUnit("C.java", contents.toString(), false, null);

		String[] order = new String[] { "java" };

		ImportRewrite imports = newImportsRewrite(cu, order, 99, 99, false);
		imports.addImport("java.util.ArrayList");

		apply(imports);

		StringBuilder expected = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		expected.append("package pack1; /* pack1 trailing */\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("import java.util.ArrayList;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("/** C leading */ public class C {}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		assertEqualString(cu.getSource(), expected.toString());
	}

	/**
	 * Expects that imports not matching defined import groups are placed together at the end.
	 *
	 * Addresses https://bugs.eclipse.org/430303 ("import group sorting is broken").
	 */
	public void testUnmatchedImports() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		ICompilationUnit cu = createCompilationUnit("pack1", "C");

		String[] order = new String[] { "java.net", "com.google" };

		ImportRewrite imports = newImportsRewrite(cu, order, 99, 99, false);
		imports.addImport("com.acme.BirdSeed");
		imports.addImport("com.acme.Dynamite");
		imports.addImport("com.google.Tgif");
		imports.addImport("java.net.Socket");
		imports.addImport("java.new.Bar");
		imports.addImport("org.linux.Kernel");

		apply(imports);

		StringBuilder expected = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		expected.append("package pack1;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("import java.net.Socket;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("import com.google.Tgif;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("import com.acme.BirdSeed;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("import com.acme.Dynamite;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("import java.new.Bar;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("import org.linux.Kernel;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("public class C {}"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		assertEqualString(cu.getSource(), expected.toString());
	}

	/**
	 * Expects that the order in which addImport is called does not affect the resulting order of
	 * import declarations.
	 *
	 * Addresses https://bugs.eclipse.org/430303 ("import group sorting is broken").
	 */
	public void testAddImportsInVaryingOrder() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		String[] order = new String[] { "h", "a" };

		List importsToAdd = new ArrayList(); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW
		importsToAdd.add("a.ClassInA");
		importsToAdd.add("b.ClassInB");
		importsToAdd.add("c.ClassInC");
		importsToAdd.add("d.ClassInD");
		importsToAdd.add("e.ClassInE");
		importsToAdd.add("f.ClassInF");
		importsToAdd.add("g.ClassInG");
		importsToAdd.add("h.ClassInH");

		ICompilationUnit cu1 = createCompilationUnit("pack1", "C");
		ImportRewrite imports1 = newImportsRewrite(cu1, order, 99, 99, false);
		for (Iterator importsToAddIter = importsToAdd.iterator(); importsToAddIter.hasNext(); ) { //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
			imports1.addImport((String) importsToAddIter.next());
		}
		apply(imports1);
		String source1 = cu1.getSource();

		Collections.reverse(importsToAdd);

		ICompilationUnit cu2 = createCompilationUnit("pack1", "C");
		ImportRewrite imports2 = newImportsRewrite(cu2, order, 99, 99, false);
		for (Iterator importsToAddIter = importsToAdd.iterator(); importsToAddIter.hasNext(); ) { //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
			imports2.addImport((String) importsToAddIter.next());
		}
		apply(imports2);
		String source2 = cu2.getSource();

		// Reversing the order in which imports are added via addImport() should not affect the rewritten order.
		assertEqualString(source2, source1);
	}

	/**
	 * Expects that static imports not matching any defined import group end up above defined import
	 * groups and that non-static imports not matching any defined import group end up below defined
	 * import groups.
	 *
	 * Addresses https://bugs.eclipse.org/430303 ("import group sorting is broken").
	 */
	public void testStaticAndNonStaticUnmatchedImports() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		ICompilationUnit cu = createCompilationUnit("pack1", "C");

		String[] order = new String[] { "#a", "h" };

		ImportRewrite imports = newImportsRewrite(cu, order, 99, 99, false);
		imports.addStaticImport("a.ClassInA", "staticMethodInA", false);
		imports.addStaticImport("b.ClassInB", "staticMethodInB", false);
		imports.addImport("g.ClassInG");
		imports.addImport("h.ClassInH");

		apply(imports);

		StringBuilder expected = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		expected.append("package pack1;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("import static b.ClassInB.staticMethodInB;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("import static a.ClassInA.staticMethodInA;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("import h.ClassInH;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("import g.ClassInG;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("public class C {}"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		assertEqualString(cu.getSource(), expected.toString());
	}

	/**
	 * Expect that two duplicate on-demand imports and their comments survive a rewrite.
	 */
	public void testAddWithDuplicateOnDemandImports() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		IPackageFragment pack1 = this.sourceFolder.createPackageFragment("pack1", false, null);
		StringBuilder contents = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		contents.append("package pack1;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("import java.lang.*;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("/* foo.bar.* 1 leading */\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("/* foo.bar.* 1 same-line leading */ import foo.bar.*; // foo.bar.* 1 same-line trailing\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("/* foo.bar.* 1 trailing */\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("import pack1.*;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("/* foo.bar.* 2 leading */\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("/* foo.bar.* 2 same-line leading */ import foo.bar.*; // foo.bar.* 2 same-line trailing\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("/* foo.bar.* 2 trailing */\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("public class C {}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		ICompilationUnit cu = pack1.createCompilationUnit("C.java", contents.toString(), false, null);

		String[] order = new String[] { "java.lang", "foo", "pack1", "com" };

		ImportRewrite imports = newImportsRewrite(cu, order, 99, 99, true);
		imports.addImport("com.example.MyClass");

		apply(imports);

		StringBuilder expected = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		expected.append("package pack1;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("import java.lang.*;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("/* foo.bar.* 1 leading */\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("/* foo.bar.* 1 same-line leading */ import foo.bar.*; // foo.bar.* 1 same-line trailing\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("/* foo.bar.* 1 trailing */\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("import pack1.*;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("import com.example.MyClass;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("/* foo.bar.* 2 leading */\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("/* foo.bar.* 2 same-line leading */ import foo.bar.*; // foo.bar.* 2 same-line trailing\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("/* foo.bar.* 2 trailing */\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("public class C {}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		assertEqualString(cu.getSource(), expected.toString());
	}

	/**
	 * Expect that two duplicate single imports and their comments survive a rewrite.
	 */
	public void testAddWithDuplicateSingleImports() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		IPackageFragment pack1 = this.sourceFolder.createPackageFragment("pack1", false, null);
		StringBuilder contents = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		contents.append("package pack1;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("import java.lang.*;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("/* foo.Bar 1 leading */\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("/* foo.Bar 1 same-line leading */ import foo.Bar; // foo.Bar 1 same-line trailing\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("/* foo.Bar 1 trailing */\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("import pack1.*;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("/* foo.Bar 2 leading */\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("/* foo.Bar 2 same-line leading */ import foo.Bar; // foo.Bar 2 same-line trailing\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("/* foo.Bar 2 trailing */\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("public class C {}"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		ICompilationUnit cu = pack1.createCompilationUnit("C.java", contents.toString(), false, null);

		String[] order = new String[] { "java.lang", "foo", "pack1", "com" };

		ImportRewrite imports = newImportsRewrite(cu, order, 99, 99, true);
		imports.addImport("com.example.MyClass");

		apply(imports);

		StringBuilder expected = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		expected.append("package pack1;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("import java.lang.*;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("/* foo.Bar 1 leading */\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("/* foo.Bar 1 same-line leading */ import foo.Bar; // foo.Bar 1 same-line trailing\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("/* foo.Bar 1 trailing */\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("import pack1.*;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("import com.example.MyClass;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("/* foo.Bar 2 leading */\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("/* foo.Bar 2 same-line leading */ import foo.Bar; // foo.Bar 2 same-line trailing\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("/* foo.Bar 2 trailing */\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("public class C {}"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		assertEqualString(cu.getSource(), expected.toString());
	}

	public void testOtherDuplicateImportsNotDisturbed() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		IPackageFragment pack1 = this.sourceFolder.createPackageFragment("pack1", false, null);
		StringBuilder contents = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		contents.append("package pack1;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("import pack1.SomeClass; // first import\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("import java.util.ArrayList;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("import pack1.SomeClass; // second import\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("import com.mycompany.Frobnigator;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("import pack1.SomeClass; // third import\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("import org.eclipse.GreatIde;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("public class C {}"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		ICompilationUnit cu = pack1.createCompilationUnit("C.java", contents.toString(), false, null);

		String[] order = new String[] { "java", "pack1", "com", "org" };

		ImportRewrite imports = newImportsRewrite(cu, order, 99, 99, true);
		imports.addImport("com.mycompany.Foo");

		apply(imports);

		StringBuilder expected = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		expected.append("package pack1;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("import pack1.SomeClass; // first import\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("import java.util.ArrayList;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("import pack1.SomeClass; // second import\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("import com.mycompany.Foo;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("import com.mycompany.Frobnigator;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("import pack1.SomeClass; // third import\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("import org.eclipse.GreatIde;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("public class C {}"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		assertEqualString(cu.getSource(), expected.toString());
	}

	public void testDuplicateImportsDoNotCountTowardOnDemandThreshold() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		IPackageFragment pack1 = this.sourceFolder.createPackageFragment("pack1", false, null);
		StringBuilder contents = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		contents.append("package pack1;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("import com.mycompany.Foo;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("import com.mycompany.Foo;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("public class C {}"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		ICompilationUnit cu = pack1.createCompilationUnit("C.java", contents.toString(), false, null);

		String[] order = new String[] {};

		ImportRewrite imports = newImportsRewrite(cu, order, 3, 3, true);
		imports.addImport("com.mycompany.Bar");

		apply(imports);

		// Expect that the 3-import on-demand threshold has not been reached.
		StringBuilder expected = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		expected.append("package pack1;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("import com.mycompany.Bar;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("import com.mycompany.Foo;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("import com.mycompany.Foo;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("public class C {}"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		assertEqualString(cu.getSource(), expected.toString());
	}

	/**
	 * Expects that a conflict between identically named fields from two static on-demand imports
	 * is resolved with an explicit import of one of the fields.
	 *
	 * Addresses https://bugs.eclipse.org/360789 ("Organize imports changes static imports to .*
	 * even when that introduces compile errors").
	 */
	public void testOnDemandConflictBetweenStaticFields() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		ICompilationUnit cu = createCompilationUnit("pack1", "C");

		// This test uses enum constants because the example in bug 360789 used enum constants,
		// but the behavior generalizes to static fields that are not enum constants.
		IPackageFragment pack2 = this.sourceFolder.createPackageFragment("pack2", false, null);
		StringBuilder horizontalEnum = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		horizontalEnum.append("package pack2;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		horizontalEnum.append("public enum Horizontal { LEFT, CENTER, RIGHT }\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		pack2.createCompilationUnit("Horizontal.java", horizontalEnum.toString(), false, null);
		StringBuilder verticalEnum = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		verticalEnum.append("package pack2;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		verticalEnum.append("public enum Vertical { TOP, CENTER, BOTTOM }\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		pack2.createCompilationUnit("Vertical.java", verticalEnum.toString(), false, null);

		String[] order = new String[] {};

		ImportRewrite imports = newImportsRewrite(cu, order, 1, 1, false);
		imports.addStaticImport("pack2.Horizontal", "CENTER", true);
		imports.addStaticImport("pack2.Vertical", "TOP", true);

		apply(imports);

		StringBuilder expected = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		expected.append("package pack1;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("import static pack2.Horizontal.CENTER;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("import static pack2.Vertical.*;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("public class C {}"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		assertEqualString(cu.getSource(), expected.toString());
	}

	/**
	 * Expects that a conflict between a static on-demand import and a type on-demand import
	 * is resolved with an explicit import of one of the conflicting member types.
	 *
	 * Inspired by https://bugs.eclipse.org/360789
	 */
	public void testOnDemandConflictBetweenTypeAndNestedStaticType() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		ICompilationUnit cu = createCompilationUnit("pack1", "C");

		IPackageFragment pack2 = this.sourceFolder.createPackageFragment("pack2", false, null);
		StringBuilder containingType = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		containingType.append("package pack2;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		containingType.append("public class ContainingType {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		containingType.append("    public static class TypeWithSameName {}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		containingType.append("    public static final int CONSTANT = 42;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		containingType.append("}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		pack2.createCompilationUnit("ContainingType.java", containingType.toString(), false, null);

		IPackageFragment pack3 = this.sourceFolder.createPackageFragment("pack3", false, null);
		StringBuilder typeWithSameName = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		typeWithSameName.append("package pack3;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		typeWithSameName.append("public class TypeWithSameName {}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		pack3.createCompilationUnit("TypeWithSameName.java", typeWithSameName.toString(), false, null);

		String[] order = new String[] {};

		ImportRewrite imports = newImportsRewrite(cu, order, 1, 1, false);
		imports.addStaticImport("pack2.ContainingType", "CONSTANT", true);
		imports.addImport("pack3.TypeWithSameName");

		apply(imports);

		StringBuilder expected = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		expected.append("package pack1;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("import static pack2.ContainingType.*;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("import pack3.TypeWithSameName;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("public class C {}"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		assertEqualString(cu.getSource(), expected.toString());
	}

	public void testFloatingCommentWithBlankLine() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		IPackageFragment pack1 = this.sourceFolder.createPackageFragment("pack1", false, null);
		StringBuilder contents = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		contents.append("package pack1;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("import com.mycompany.Bar;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("/*hello!\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("this is a comment!*/\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("import com.mycompany.Foo;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("public class C {}"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		ICompilationUnit cu = pack1.createCompilationUnit("C.java", contents.toString(), false, null);

		String[] order = new String[] {};

		ImportRewrite imports = newImportsRewrite(cu, order, 99, 99, false);
		imports.addImport("com.mycompany.Bar");
		imports.addImport("com.mycompany.Foo");

		apply(imports);

		StringBuilder expected = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		expected.append("package pack1;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("import com.mycompany.Bar;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("/*hello!\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("this is a comment!*/\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("import com.mycompany.Foo;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("public class C {}"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		assertEqualString(cu.getSource(), expected.toString());
	}

	/**
	 * Expects that an import rewrite with no effective changes produces an empty TextEdit.
	 */
	public void testNoEdits() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		IPackageFragment pack1 = this.sourceFolder.createPackageFragment("pack1", false, null);
		StringBuilder contents = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		contents.append("package pack1;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("// leading comment\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("import com.mycompany.Foo;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("// trailing comment\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("// leading comment\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("import java.util.ArrayList;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("// trailing comment\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("public class C {}"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		ICompilationUnit cu = pack1.createCompilationUnit("C.java", contents.toString(), false, null);

		String[] order = new String[] {"com", "java"};

		ImportRewrite imports = newImportsRewrite(cu, order, 99, 99, false);
		imports.addImport("com.mycompany.Foo");
		imports.addImport("java.util.ArrayList");

		TextEdit edit = imports.rewriteImports(null);

		assertEquals(0, edit.getChildrenSize());
	}

	public void testAddImportWithCommentBetweenImportsAndType() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		IPackageFragment pack1 = this.sourceFolder.createPackageFragment("pack1", false, null);
		StringBuilder contents = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		contents.append("package pack1;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("import com.mycompany.Bar;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("/* floating comment */\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("// type comment\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("public class C {}"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		ICompilationUnit cu = pack1.createCompilationUnit("C.java", contents.toString(), false, null);

		String[] order = new String[] {"com", "java"};

		ImportRewrite imports = newImportsRewrite(cu, order, 99, 99, false);
		imports.addImport("com.mycompany.Bar");
		imports.addImport("com.mycompany.Foo");

		apply(imports);

		StringBuilder expected = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		expected.append("package pack1;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("import com.mycompany.Bar;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("import com.mycompany.Foo;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("/* floating comment */\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("// type comment\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("public class C {}"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		assertEqualString(expected.toString(), cu.getSource());
	}

	public void testRenameImportedClassWithImportedNestedClass() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		StringBuilder contents = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		contents.append("package pack1;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("import com.example.A;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("import com.example.A.ANested;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("import com.example.C;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("import com.example.C.CNested;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("import com.example.E;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("import com.example.E.ENested;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("public class Clazz {}"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		ICompilationUnit cu = createCompilationUnit("pack1", "Clazz", contents.toString());

		String[] order = new String[] { "com" };

		ImportRewrite imports = newImportsRewrite(cu, order, 999, 999, true);
		imports.setUseContextToFilterImplicitImports(true);
		// Simulate renaming com.example.A to com.example.D.
		imports.removeImport("com.example.A");
		imports.removeImport("com.example.A.ANested");
		imports.addImport("com.example.D");
		imports.addImport("com.example.D.ANested");

		apply(imports);

		StringBuilder expected = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		expected.append("package pack1;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("import com.example.C;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("import com.example.C.CNested;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("import com.example.D;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("import com.example.D.ANested;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("import com.example.E;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("import com.example.E.ENested;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("public class Clazz {}"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		assertEqualString(cu.getSource(), expected.toString());
	}

	public void testConflictsBetweenOriginalOnDemands() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		// Create a type named "A" in each of two packages.
		createCompilationUnit("conflicting1", "A");
		createCompilationUnit("conflicting2", "A");

		// Create a static member named "doStuff" in each of two types.
		StringBuilder statics1 = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		statics1.append("package statics;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		statics1.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		statics1.append("public class Statics1 {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		statics1.append("    public static void doStuff() {}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		statics1.append("}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		createCompilationUnit("statics", "Statics1", statics1.toString());
		StringBuilder statics2 = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		statics2.append("package statics;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		statics2.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		statics2.append("public class Statics2 {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		statics2.append("    public static void doStuff() {}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		statics2.append("}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		createCompilationUnit("statics", "Statics2", statics2.toString());

		// Import the types and static members ambiguously via conflicting on-demand imports.
		StringBuilder contents = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		contents.append("package pack1;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("import static statics.Statics1.*;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("import static statics.Statics2.*;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("import conflicting1.*;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("import conflicting2.*;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("class Clazz {}"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		ICompilationUnit cu = createCompilationUnit("pack1", "Clazz", contents.toString());

		ImportRewrite imports = newImportsRewrite(cu, new String[0], 1, 1, true);
		imports.setUseContextToFilterImplicitImports(true);
		// Add imports that surface the ambiguity between the existing on-demand imports.
		imports.addImport("conflicting1.A");
		imports.addStaticImport("statics.Statics1", "doStuff", false);

		apply(imports);

		StringBuilder expected = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		// Expect that explicit single imports are added to resolve the conflicts.
		expected.append("package pack1;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("import static statics.Statics1.*;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("import static statics.Statics1.doStuff;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("import static statics.Statics2.*;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("import conflicting1.*;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("import conflicting1.A;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("import conflicting2.*;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("class Clazz {}"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		assertEqualString(cu.getSource(), expected.toString());
	}

	public void testRemoveImportsWithPackageDocComment() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		StringBuilder contents = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		contents.append("/** package doc comment */\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("package pack1;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("import com.example.Foo;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("public class Clazz {}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		ICompilationUnit cu = createCompilationUnit("pack1", "Clazz", contents.toString());

		ImportRewrite rewrite = newImportsRewrite(cu, new String[] {}, 999, 999, true);
		rewrite.setUseContextToFilterImplicitImports(true);
		rewrite.removeImport("com.example.Foo");
		apply(rewrite);

		StringBuilder expected = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		expected.append("/** package doc comment */\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("package pack1;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("public class Clazz {}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		assertEqualString(cu.getSource(), expected.toString());
	}

	public void testImplicitImportFiltering() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		String[] order = new String[] {};

		ICompilationUnit cuWithFiltering = createCompilationUnit("pack1", "CuWithFiltering");

		ImportRewrite rewriteWithFiltering = newImportsRewrite(cuWithFiltering, order, 999, 999, true);
		rewriteWithFiltering.setUseContextToFilterImplicitImports(true);
		rewriteWithFiltering.setFilterImplicitImports(true);
		rewriteWithFiltering.addImport("java.lang.Integer");
		apply(rewriteWithFiltering);

		StringBuilder expectedWithFiltering = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		// Expect that the implicit java.lang import has been filtered out.
		expectedWithFiltering.append("package pack1;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expectedWithFiltering.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expectedWithFiltering.append("public class CuWithFiltering {}"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		assertEqualString(cuWithFiltering.getSource(), expectedWithFiltering.toString());

		ICompilationUnit cuWithoutFiltering = createCompilationUnit("pack1", "CuWithoutFiltering");

		ImportRewrite rewriteWithoutFiltering = newImportsRewrite(cuWithoutFiltering, order, 999, 999, true);
		rewriteWithoutFiltering.setUseContextToFilterImplicitImports(true);
		rewriteWithoutFiltering.setFilterImplicitImports(false);
		rewriteWithoutFiltering.addImport("java.lang.Integer");
		apply(rewriteWithoutFiltering);

		StringBuilder expectedWithoutFiltering = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		// Expect that the java.lang import has been added to the compilation unit.
		expectedWithoutFiltering.append("package pack1;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expectedWithoutFiltering.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expectedWithoutFiltering.append("import java.lang.Integer;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expectedWithoutFiltering.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expectedWithoutFiltering.append("public class CuWithoutFiltering {}"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		assertEqualString(cuWithoutFiltering.getSource(), expectedWithoutFiltering.toString());
	}

	/**
	 * Addresses https://bugs.eclipse.org/460484 ("ImportRewrite throws SIOOBE when trying to add
	 * import").
	 */
	public void testAddAdjacentImportWithCommonPrefixButLongerInitialSegment() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		StringBuilder contents = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		contents.append("package pack1;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("import a.FromA;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("import b.FromB;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("public class Clazz {}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		ICompilationUnit cu = createCompilationUnit("pack1", "Clazz", contents.toString());

		ImportRewrite rewrite = newImportsRewrite(cu, new String[] {}, 999, 999, true);
		rewrite.setUseContextToFilterImplicitImports(true);
		// Expect that no exception is thrown when "ab" is compared with "a".
		rewrite.addImport("ab.FromAb");
		apply(rewrite);

		StringBuilder expected = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		expected.append("package pack1;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("import a.FromA;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("import ab.FromAb;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("import b.FromB;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		expected.append("public class Clazz {}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		assertEqualString(cu.getSource(), expected.toString());
	}

	// https://bugs.eclipse.org/459320
	public void testAddImportToCuNotOnClasspath() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		StringBuilder contents = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		contents.append("package pack1;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("public class Clazz {}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL

		createFolder("/P/alt-src/pack1/");
		IFile clazz = createFile("/P/alt-src/pack1/Clazz.java", contents.toString());
		ICompilationUnit cu = (ICompilationUnit) JavaCore.create(clazz);
		cu.becomeWorkingCopy(null);

		try {
			ImportRewrite rewrite = newImportsRewrite(cu, new String[] {}, 999, 999, true);
			rewrite.setUseContextToFilterImplicitImports(true);
			rewrite.addImport("pack1.AnotherClass");
			apply(rewrite);

			assertEqualString(cu.getSource(), contents.toString());
		} finally {
			cu.discardWorkingCopy();
		}
	}

	public void testAddImports1() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("pack1", false, null);
		StringBuilder buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("package pack1;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import java.util.Map;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import java.util.Set;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import java.util.Vector;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import pack.List;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import pack.List2;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("public class C {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);

		String[] order= new String[] { "java", "com", "pack" };

		ImportRewrite imports= newImportsRewrite(cu, order, 2, 2, true);
		imports.addImport("java.net.Socket");
		imports.addImport("p.A");
		imports.addImport("com.something.Foo");

		apply(imports);

		// java.net.Socket gets added to the "java" import group
		// p.A gets added to the default match-all group at the end
		// com.something.Foo gets added to the "com" import group
		buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("package pack1;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import java.net.Socket;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import java.util.Map;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import java.util.Set;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import java.util.Vector;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import com.something.Foo;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import pack.List;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import pack.List2;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import p.A;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("public class C {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		assertEqualString(cu.getSource(), buf.toString());
	}

	public void testAddImportsNoEmptyLines() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD

		this.sourceFolder.getJavaProject().setOption(DefaultCodeFormatterConstants.FORMATTER_BLANK_LINES_BETWEEN_IMPORT_GROUPS, String.valueOf(0));

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("pack1", false, null);
		StringBuilder buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("package pack1;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import java.util.Set;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("public class C {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);

		String[] order= new String[] { "java.util", "java.net", "p" };

		ImportRewrite imports= newImportsRewrite(cu, order, 2, 2, true);

		imports.addImport("java.net.Socket");
		imports.addImport("p.A");

		apply(imports);

		buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("package pack1;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import java.util.Set;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import java.net.Socket;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import p.A;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("public class C {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		assertEqualString(cu.getSource(), buf.toString());
	}

	public void testAddImportsMoreEmptyLines() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD

		this.sourceFolder.getJavaProject().setOption(DefaultCodeFormatterConstants.FORMATTER_BLANK_LINES_BETWEEN_IMPORT_GROUPS, String.valueOf(2));

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("pack1", false, null);
		StringBuilder buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("package pack1;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import java.util.Set;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("public class C {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);

		String[] order= new String[] { "java.util", "java.net", "p" };

		ImportRewrite imports= newImportsRewrite(cu, order, 2, 2, true);

		imports.addImport("java.net.Socket");
		imports.addImport("p.A");

		apply(imports);

		buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("package pack1;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import java.util.Set;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import java.net.Socket;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import p.A;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("public class C {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		assertEqualString(cu.getSource(), buf.toString());
	}

	public void testAddImports2() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("pack1", false, null);
		StringBuilder buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("package pack1;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import java.util.Set;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import java.util.Vector;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("public class C {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);

		String[] order= new String[] { "java", "java.util", "com", "pack" };

		ImportRewrite imports= newImportsRewrite(cu, order, 2, 2, true);
		imports.addImport("java.x.Socket");

		apply(imports);

		buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("package pack1;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import java.x.Socket;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import java.util.Set;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import java.util.Vector;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("public class C {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		assertEqualString(cu.getSource(), buf.toString());
	}


	public void testAddImports3() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("pack1", false, null);
		StringBuilder buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("package pack1;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import java.util.Set; // comment\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("public class C {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);

		String[] order= new String[] { "java", "java.util", "com", "pack" };

		ImportRewrite imports= newImportsRewrite(cu, order, 99, 99, true);
		imports.addImport("java.util.Vector");

		apply(imports);

		buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("package pack1;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import java.util.Set; // comment\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import java.util.Vector;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("public class C {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		assertEqualString(cu.getSource(), buf.toString());
	}

	public void testAddImports4() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		getJavaProject("P").setOption(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_SEMICOLON, JavaCore.INSERT);

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("pack1", false, null);
		StringBuilder buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("package pack1;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import java.util.Set; // comment\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("public class C {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);

		String[] order= new String[] { "java", "java.util", "com", "pack" };

		ImportRewrite imports= newImportsRewrite(cu, order, 99, 99, true);
		imports.addImport("java.util.Vector");

		apply(imports);

		buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("package pack1;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import java.util.Set; // comment\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import java.util.Vector ;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("public class C {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		assertEqualString(cu.getSource(), buf.toString());
	}

	public void testAddImports5() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		getJavaProject("P").setOption(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_SEMICOLON, JavaCore.INSERT);

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("pack1", false, null);
		StringBuilder buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("package pack1;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("public class C {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);

		String[] order= new String[] { "java", "java.util", "com", "pack" };

		ImportRewrite imports= newImportsRewrite(cu, order, 1, 1, true);
		imports.setUseContextToFilterImplicitImports(true);
		imports.addImport("java.util.Map");
		imports.addImport("java.util.Set");
		imports.addImport("java.util.Map.Entry");
		imports.addImport("java.util.Collections");

		apply(imports);

		// java.util.{Map,Set,Collections} are reduced to java.util.*
		// java.util.Map.Entry is reduced to java.util.Map.*
		buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("package pack1;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import java.util.* ;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import java.util.Map.* ;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("public class C {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		assertEqualString(cu.getSource(), buf.toString());
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=306568
	public void testAddImports6() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("pack1", false, null);
		StringBuilder buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append( //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				"package pack1;\n" +
				"\n" +
				"import java.util.*;\n" +
				"\n" +
				"public class C {\n" +
				"    public static void main(String[] args) {\n" +
				"        HashMap h;\n" +
				"\n" +
				"        Map.Entry e= null;\n" +
				"        Entry e2= null;\n" +
				"\n" +
				"        System.out.println(\"hello\");\n" +
				"    }\n" +
				"}");
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);

		String[] order= new String[] { "java", "java.util", "com", "pack" };

		ImportRewrite imports= newImportsRewrite(cu, order, 1, 1, true);
		imports.setUseContextToFilterImplicitImports(true);
		imports.addImport("java.util.Map.Entry");

		apply(imports);

		// With on-demand threshold set to 1, java.util.Map.Entry is reduced to java.util.Map.*.
		buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append( //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				"package pack1;\n" +
				"\n" +
				"import java.util.*;\n" +
				"import java.util.Map.*;\n" +
				"\n" +
				"public class C {\n" +
				"    public static void main(String[] args) {\n" +
				"        HashMap h;\n" +
				"\n" +
				"        Map.Entry e= null;\n" +
				"        Entry e2= null;\n" +
				"\n" +
				"        System.out.println(\"hello\");\n" +
				"    }\n" +
				"}");
		assertEqualString(cu.getSource(), buf.toString());
	}

	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=309022
	public void testAddImports7() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("pack1", false, null);
		StringBuilder buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append( //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				"package pack1;\n" +
				"\n" +
				"import java.util.*;\n" +
				"import java.util.Map.Entry;\n" +
				"\n" +
				"public class C {\n" +
				"    public static void main(String[] args) {\n" +
				"        HashMap h;\n" +
				"\n" +
				"        Map.Entry e= null;\n" +
				"        Entry e2= null;\n" +
				"\n" +
				"        PrintWriter pw;\n" +
				"        System.out.println(\"hello\");\n" +
				"    }\n" +
				"}");
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);

		String[] order= new String[] { "java", "java.util", "com", "pack" };

		ImportRewrite imports= newImportsRewrite(cu, order, 1, 1, true);
		imports.setUseContextToFilterImplicitImports(true);
		imports.addImport("java.io.PrintWriter");

		apply(imports);

		buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append( //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				"package pack1;\n" +
				"\n" +
				"import java.io.*;\n" +
				"\n" +
				"import java.util.*;\n" +
				"import java.util.Map.Entry;\n" +
				"\n" +
				"public class C {\n" +
				"    public static void main(String[] args) {\n" +
				"        HashMap h;\n" +
				"\n" +
				"        Map.Entry e= null;\n" +
				"        Entry e2= null;\n" +
				"\n" +
				"        PrintWriter pw;\n" +
				"        System.out.println(\"hello\");\n" +
				"    }\n" +
				"}");
		assertEqualString(cu.getSource(), buf.toString());
	}

	public void testAddImportsWithGroupsOfUnmatched1() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("pack1", false, null);
		StringBuilder buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("package pack1;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("public class C {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);

		String[] order= new String[] { "java", "", "org", "#", "pack" };

		ImportRewrite imports= newImportsRewrite(cu, order, 99, 99, true);
		imports.addImport("org.x.Y");
		imports.addImport("pack.P");
		imports.addImport("my.M");
		imports.addImport("java.util.Vector");
		imports.addStaticImport("stat.X", "CONST", true);

		apply(imports);

		buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("package pack1;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import java.util.Vector;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import my.M;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import org.x.Y;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import static stat.X.CONST;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import pack.P;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("public class C {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		assertEqualString(cu.getSource(), buf.toString());
	}

	public void testAddImportsWithGroupsOfUnmatched2() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("pack1", false, null);
		StringBuilder buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("package pack1;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("public class C {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);

		String[] order= new String[] { "org", "com", "pack", "#", "" };

		ImportRewrite imports= newImportsRewrite(cu, order, 99, 99, true);
		imports.addImport("com.x.Y");
		imports.addImport("pack.P");
		imports.addImport("my.M");
		imports.addImport("org.Vector");
		imports.addStaticImport("stat.X", "CONST", true);

		apply(imports);

		buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("package pack1;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import org.Vector;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import com.x.Y;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import pack.P;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import static stat.X.CONST;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import my.M;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("public class C {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		assertEqualString(cu.getSource(), buf.toString());
	}

	public void testRemoveImports1() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("pack1", false, null);
		StringBuilder buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("package pack1;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import java.util.Set;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import java.util.Vector;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import java.util.Map;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import pack.List;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import pack.List2;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("public class C {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);

		String[] order= new String[] { "java", "com", "pack" };

		ImportRewrite imports= newImportsRewrite(cu, order, 2, 2, true);
		imports.removeImport("java.util.Set");
		imports.removeImport("pack.List");

		apply(imports);

		buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("package pack1;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import java.util.*;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import pack.List2;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("public class C {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		assertEqualString(cu.getSource(), buf.toString());
	}

	public void testRemoveImports2() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("pack1", false, null);
		StringBuilder buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("package pack1;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import java.util.Set;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import java.util.Vector; // comment\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("public class C {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);

		String[] order= new String[] { "java", "com", "pack" };

		ImportRewrite imports= newImportsRewrite(cu, order, 2, 2, true);
		imports.removeImport("java.util.Vector");

		apply(imports);

		buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("package pack1;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import java.util.Set;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("public class C {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		assertEqualString(cu.getSource(), buf.toString());
	}

	public void testRemoveImports3() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		IPackageFragment pack= this.sourceFolder.createPackageFragment("pack", false, null);
		StringBuilder buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("package pack;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("public class A {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("    public class Inner {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("    }\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		pack.createCompilationUnit("A.java", buf.toString(), false, null);

		IPackageFragment test1= this.sourceFolder.createPackageFragment("test1", false, null);
		buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("package test1;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import pack.A;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import pack.A.Inner;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import pack.A.NotThere;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import pack.B;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import pack.B.Inner;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import pack.B.NotThere;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("public class T {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		ICompilationUnit cuT= test1.createCompilationUnit("T.java", buf.toString(), false, null);

		ASTParser parser= ASTParser.newParser(AST.getAllSupportedVersions().getFirst());
		parser.setSource(cuT);
		parser.setResolveBindings(true);
		CompilationUnit astRoot= (CompilationUnit) parser.createAST(null);

		ImportRewrite imports= newImportsRewrite(astRoot, new String[0], 99, 99, true);
		imports.setUseContextToFilterImplicitImports(true);

		imports.removeImport("pack.A.Inner");
		imports.removeImport("pack.A.NotThere");
		imports.removeImport("pack.B.Inner");
		imports.removeImport("pack.B.NotThere");

		apply(imports);

		buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("package test1;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import pack.A;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import pack.B;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("public class T {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		assertEqualString(cuT.getSource(), buf.toString());
	}

	public void testRemoveImportWithSyntaxError_bug494691() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("pack1", false, null);
		StringBuilder buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("package pack1;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import java.util.*;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("syntaxError\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("public class C {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);

		ImportRewrite imports= newImportsRewrite(cu, new String[0], 2, 2, true);
		imports.removeImport("java.util.*");

		apply(imports);

		buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("package pack1;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("syntaxError\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("public class C {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		assertEqualString(cu.getSource(), buf.toString());
	}

	public void testAddImports_bug23078() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("pack1", false, null);
		StringBuilder buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("package pack1;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import p.A.*;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("public class C {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);

		String[] order= new String[] { };

		ImportRewrite imports= newImportsRewrite(cu, order, 3, 3, true);
		imports.addImport("p.A");
		imports.addImport("p.Inner");
		imports.addImport("p.Inner.*");

		apply(imports);

		// Without having set useContextToFilterImplicitImports to true, we get pre-3.6 behavior,
		// which sorts imports by containing type and/or package before sorting by qualified name.
		buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("package pack1;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import p.A;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import p.Inner;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import p.A.*;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import p.Inner.*;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("public class C {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		assertEqualString(cu.getSource(), buf.toString());
	}

	public void testAddImports_bug23078_usingContext() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("pack1", false, null);
		StringBuilder buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("package pack1;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import p.A.*;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("public class C {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);

		String[] order= new String[] { };

		ImportRewrite imports= newImportsRewrite(cu, order, 3, 3, true);
		imports.setUseContextToFilterImplicitImports(true);
		imports.addImport("p.A");
		imports.addImport("p.Inner");
		imports.addImport("p.Inner.*");

		apply(imports);

		// Having set useContextToFilterImplicitImports to true, we get 3.6-and-later behavior,
		// which sorts imports by containing package and then by qualified name.
		buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("package pack1;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import p.A;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import p.A.*;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import p.Inner;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import p.Inner.*;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("public class C {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		assertEqualString(cu.getSource(), buf.toString());
	}

	public void testAddImports_bug25113() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("pack1", false, null);
		StringBuilder buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("package pack1;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import java.awt.Panel;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import java.math.BigInteger;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("public class C {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);

		String[] order= new String[] { "java.awt", "java" };

		ImportRewrite imports= newImportsRewrite(cu, order, 99, 99, true);
		imports.addImport("java.beans.Beans");

		apply(imports);

		buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("package pack1;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import java.awt.Panel;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import java.beans.Beans;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import java.math.BigInteger;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("public class C {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		assertEqualString(cu.getSource(), buf.toString());
	}

	public void testAddImports_bug42637() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("pack1", false, null);
		StringBuilder buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("package pack1;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import java.lang.System;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("public class C {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);

		String[] order= new String[] { "java" };

		ImportRewrite imports= newImportsRewrite(cu, order, 99, 99, true);
		imports.addImport("java.io.Exception");

		apply(imports);

		buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("package pack1;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import java.io.Exception;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import java.lang.System;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("public class C {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		assertEqualString(cu.getSource(), buf.toString());
	}

	/**
	 * Expects that, in the absence of a package declaration, comments preceding the first import
	 * declaration are treated as file header comments and left in place.
	 */
	public void testAddImports_bug121428() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("pack1", false, null);
		StringBuilder buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("/** comment */\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import java.lang.System;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("public class C {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);

		String[] order= new String[] { "java" };

		ImportRewrite imports= newImportsRewrite(cu, order, 99, 99, false);
		imports.addImport("java.io.Exception");

		apply(imports);

		buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("/** comment */\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import java.io.Exception;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("public class C {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		assertEqualString(cu.getSource(), buf.toString());
	}

	/**
	 * Test that the Inner class import comes in the right order (i.e. after the enclosing type's import) when re-organized
	 *
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=194358"
	 */
	public void testBug194358() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD

		StringBuilder buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("package pack1;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import pack2.A;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import pack2.A.Inner;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import pack2.B;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("public class C {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("pack1", false, null);
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);

		// We need to actually make some state in the AST for the classes, to test that we can
		// disambiguate between packages and inner classes (see the bug for details).
		IPackageFragment pack2= this.sourceFolder.createPackageFragment("pack2", false, null);
		ICompilationUnit aUnit= pack2.createCompilationUnit("A.java", "", false, null);
		ICompilationUnit bUnit= pack2.createCompilationUnit("B.java", "", false, null);
		bUnit.createType("class B {}", null, false, null);

		IType aType= aUnit.createType("class A {}", null, false, null);
		aType.createType("class Inner {}", null, false, null);
		String[] order= new String[] { "java" };

		ImportRewrite imports= newImportsRewrite(cu, order, 99, 99, false);
		imports.setUseContextToFilterImplicitImports(true);
		imports.addImport("pack2.A");
		imports.addImport("pack2.B");
		imports.addImport("pack2.A.Inner");

		apply(imports);

		buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("package pack1;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import pack2.A;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import pack2.A.Inner;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import pack2.B;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("public class C {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		assertEqualString(cu.getSource(), buf.toString());
	}

	/**
	 * Test that a valid inner class import is not removed even when the container
	 * class is implicitly available. This tests the case where the classes are in
	 * different compilation units.
	 *
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=194358"
	 */
	public void testBug194358a() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		StringBuilder buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("package com.pack1;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import com.pack1.A;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import com.pack1.A.Inner;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import com.pack2.B;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("public class C {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("com.pack1", false, null);
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);
		ICompilationUnit aUnit= pack1.createCompilationUnit("A.java", "", false, null);

		IPackageFragment pack2= this.sourceFolder.createPackageFragment("com.pack2", false, null);
		ICompilationUnit bUnit= pack2.createCompilationUnit("B.java", "", false, null);
		bUnit.createType("class B {}", null, false, null);
		IType aType= aUnit.createType("class A {}", null, false, null);
		aType.createType("class Inner {}", null, false, null);
		String[] order= new String[] { "java" };

		ImportRewrite imports= newImportsRewrite(cu, order, 99, 99, false);
		imports.setUseContextToFilterImplicitImports(false);
		imports.addImport("com.pack1.A");
		imports.addImport("com.pack1.A.Inner");
		imports.addImport("com.pack2.B");

		apply(imports);

		buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("package com.pack1;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import com.pack1.A.Inner;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import com.pack2.B;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("public class C {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		assertEqualString(cu.getSource(), buf.toString());
	}
	/**
	 * Test that the Inner type imports are not removed while organizing even though the
	 * containing class is implicitly available - for the case when both the classes are
	 * in the same compilation unit
	 *
	 * see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=235253"
	 */
	public void testBug235253() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		StringBuilder buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("package bug;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("class Bug {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("public void addFile(File file) {}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\tinterface Proto{};\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("class Foo implements Proto{}"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("bug", false, null);
		ICompilationUnit cu= pack1.createCompilationUnit("Bug.java", buf.toString(), false, null);
		String[] order= new String[] { "bug" , "java" };
		ImportRewrite imports= newImportsRewrite(cu, order, 99, 99, false);
		imports.setUseContextToFilterImplicitImports(true);
		imports.addImport("bug.Bug.Proto");
		imports.addImport("java.io.File");

		apply(imports);
		buf = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("package bug;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import bug.Bug.Proto;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import java.io.File;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("class Bug {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("public void addFile(File file) {}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\tinterface Proto{};\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("class Foo implements Proto{}"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		assertEqualString(cu.getSource(), buf.toString());
	}

	public void testAddStaticImports1() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("pack1", false, null);
		StringBuilder buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("package pack1;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import java.lang.System;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("public class C {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);

		String[] order= new String[] { "#", "java" };

		ImportRewrite imports= newImportsRewrite(cu, order, 99, 99, true);
		imports.addStaticImport("java.lang.Math", "min", true);
		imports.addImport("java.lang.Math");
		imports.addStaticImport("java.lang.Math", "max", true);

		apply(imports);

		buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("package pack1;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import static java.lang.Math.max;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import static java.lang.Math.min;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import java.lang.System;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("public class C {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		assertEqualString(cu.getSource(), buf.toString());
	}

	public void testAddStaticImports2() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("pack1", false, null);
		StringBuilder buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("package pack1;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import java.lang.System;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("public class C {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);

		String[] order= new String[] { "#", "java" };

		ImportRewrite imports= newImportsRewrite(cu, order, 99, 99, true);
		imports.addStaticImport("xx.MyConstants", "SIZE", true);
		imports.addStaticImport("xy.MyConstants", "*", true);
		imports.addImport("xy.MyConstants");

		apply(imports);

		buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("package pack1;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import static xx.MyConstants.SIZE;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import static xy.MyConstants.*;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import java.lang.System;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import xy.MyConstants;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("public class C {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		assertEqualString(cu.getSource(), buf.toString());
	}

	public void testAddStaticImports3() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("pack1", false, null);
		StringBuilder buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("package pack1;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import java.lang.System;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("public class C {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);

		String[] order= new String[] { "#", "java" };

		ImportRewrite imports= newImportsRewrite(cu, order, 99, 3, true);
		imports.addStaticImport("java.lang.Math", "min", true);
		imports.addStaticImport("java.lang.Math", "max", true);
		imports.addStaticImport("java.lang.Math", "abs", true);

		imports.addStaticImport("java.io.File", "pathSeparator", true);
		imports.addStaticImport("java.io.File", "separator", true);

		imports.addImport("java.util.List");
		imports.addImport("java.util.Vector");
		imports.addImport("java.util.ArrayList");

		apply(imports);

		buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("package pack1;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import static java.io.File.pathSeparator;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import static java.io.File.separator;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import static java.lang.Math.*;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import java.lang.System;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import java.util.ArrayList;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import java.util.List;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import java.util.Vector;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("public class C {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		assertEqualString(cu.getSource(), buf.toString());
	}


	private void createClassStub(String pack, String typeName, String typeKind) throws JavaModelException {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment(pack, false, null);
		StringBuilder buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("package ").append(pack).append(";\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("public ").append(typeKind).append(" ").append(typeName).append(" {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		String content= buf.toString();

		String name= typeName;
		int idx= typeName.indexOf('<');
		if (idx != -1) {
			name= typeName.substring(0, idx);
		}
		pack1.createCompilationUnit(name + ".java", content, false, null); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
	}


	public void testImportStructureWithSignatures() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		createClassStub("java.io", "IOException", "class");
		createClassStub("java.net", "URL", "class");
		createClassStub("java.util", "List<E>", "interface");
		createClassStub("java.net", "SocketAddress", "class");

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("package test1;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import java.util.*;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import java.net.*;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import java.io.*;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("public class A {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("    public void foo() {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("        IOException s;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("        URL[][] t;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("        List<SocketAddress> x;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("    }\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		String content= buf.toString();
		ICompilationUnit cu1= pack1.createCompilationUnit("A.java", content, false, null);

		buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("package test1;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("public class B {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		String content2= buf.toString();
		ICompilationUnit cu2= pack1.createCompilationUnit("B.java", content2, false, null);

		String[] order= new String[] { "java.util", "java.io", "java.net" };
		int threshold= 99;
		AST ast= AST.newAST(AST.getAllSupportedVersions().getFirst(), false);
		ImportRewrite importsRewrite= newImportsRewrite(cu2, order, threshold, threshold, true);
		{
			IJavaElement[] elements= cu1.codeSelect(content.indexOf("IOException"), "IOException".length());
			assertEquals(1, elements.length);
			String key= ((IType) elements[0]).getKey();
			String signature= new BindingKey(key).toSignature();

			importsRewrite.addImportFromSignature(signature, ast);
		}
		{
			IJavaElement[] elements= cu1.codeSelect(content.indexOf("URL"), "URL".length());
			assertEquals(1, elements.length);
			String key= ((IType) elements[0]).getKey();
			String signature= new BindingKey(key).toSignature();

			importsRewrite.addImportFromSignature(signature, ast);
		}
		{
			IJavaElement[] elements= cu1.codeSelect(content.indexOf("List"), "List".length());
			assertEquals(1, elements.length);
			String key= ((IType) elements[0]).getKey();
			String signature= new BindingKey(key).toSignature();

			importsRewrite.addImportFromSignature(signature, ast);
		}
		apply(importsRewrite);

		buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("package test1;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import java.util.List;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import java.io.IOException;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import java.net.SocketAddress;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import java.net.URL;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("public class B {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL

		assertEqualStringIgnoreDelim(cu2.getSource(), buf.toString());
	}

	public void testImportStructureWithSignatures2() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		createClassStub("java.util", "Map<S, T>", "interface");
		createClassStub("java.util", "Set<S>", "interface");
		createClassStub("java.net", "SocketAddress", "class");
		createClassStub("java.net", "ServerSocket", "class");

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("package test1;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import java.util.*;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import java.net.*;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import java.io.*;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("public class A {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("    public void foo() {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("        Map<?, ? extends Set<? super ServerSocket>> z;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("    }\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		String content= buf.toString();
		ICompilationUnit cu1= pack1.createCompilationUnit("A.java", content, false, null);

		buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("package test1;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("public class B {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		String content2= buf.toString();
		ICompilationUnit cu2= pack1.createCompilationUnit("B.java", content2, false, null);

		String[] order= new String[] { "java.util", "java.io", "java.net" };
		int threshold= 99;
		AST ast= AST.newAST(AST.getAllSupportedVersions().getFirst(), false);
		ImportRewrite importsRewrite= newImportsRewrite(cu2, order, threshold, threshold, true);
		{
			IJavaElement[] elements= cu1.codeSelect(content.indexOf("Map"), "Map".length());
			assertEquals(1, elements.length);
			String key= ((IType) elements[0]).getKey();
			String signature= new BindingKey(key).toSignature();

			importsRewrite.addImportFromSignature(signature, ast);
		}

		apply(importsRewrite);

		buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("package test1;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import java.util.Map;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import java.util.Set;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import java.net.ServerSocket;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("public class B {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL

		assertEqualStringIgnoreDelim(cu2.getSource(), buf.toString());
	}


	public void testAddedRemovedImportsAPI() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("pack1", false, null);
		StringBuilder buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("package pack1;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import java.util.Vector;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("public class C {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("    public final static int CONST= 9;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);

		String[] order= new String[] { "#", "java" };

		ImportRewrite imports= newImportsRewrite(cu, order, 99, 99, true);
		imports.addStaticImport("java.lang.Math", "min", true);
		imports.addImport("java.lang.Math");

		assertAddedAndRemoved(imports,
				new String[] { "java.lang.Math" }, new String[] {},
				new String[] { "java.lang.Math.min" }, new String[] {}
		);

		imports.addImport("java.lang.Math");
		imports.addStaticImport("java.lang.Math", "max", true);

		assertAddedAndRemoved(imports,
				new String[] { "java.lang.Math" }, new String[] {},
				new String[] { "java.lang.Math.min", "java.lang.Math.max" }, new String[] {}
		);

		imports.removeImport("java.lang.Math");
		imports.removeImport("java.util.Vector");
		imports.removeStaticImport("java.lang.Math.dup");

		assertAddedAndRemoved(imports,
				new String[] { }, new String[] { "java.util.Vector"},
				new String[] { "java.lang.Math.min", "java.lang.Math.max" }, new String[] {}
		);

		imports.addImport("java.util.Vector");
		imports.addStaticImport("pack1.C", "CONST", true);

		assertAddedAndRemoved(imports,
				new String[] { }, new String[] { },
				new String[] { "java.lang.Math.min", "java.lang.Math.max", "pack1.C.CONST" }, new String[] {}
		);

		apply(imports);

		buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("package pack1;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import static java.lang.Math.max;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import static java.lang.Math.min;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import static pack1.C.CONST;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import java.util.Vector;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("public class C {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("    public final static int CONST= 9;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		assertEqualString(cu.getSource(), buf.toString());
	}

	public void testPackageInfo() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("pack1", false, null);
		StringBuilder buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("package pack1;"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL

		ICompilationUnit cu= pack1.createCompilationUnit("package-info.java", buf.toString(), false, null);

		String[] order= new String[] { "#", "java" };

		ImportRewrite imports= newImportsRewrite(cu, order, 99, 99, true);
		imports.addImport("foo.Bar");

		apply(imports);

		buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("package pack1;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import foo.Bar;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		assertEqualString(cu.getSource(), buf.toString());
	}

	public void testBug252379() throws CoreException, BackingStoreException, //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
			MalformedTreeException, BadLocationException {


		ICompilationUnit[] units = new ICompilationUnit[3];

		IPackageFragment pack1 = this.sourceFolder.createPackageFragment(
				"bug", false, null);

		StringBuilder buf = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("package bug;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("enum CaseType {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\tone;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\tstatic CaseType[] all(){return null;}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL

		units[0] = pack1.createCompilationUnit("CaseType.java", buf.toString(), false, null);

		buf = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("package bug;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("enum ShareLevel{all})\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL

		units[1] = pack1.createCompilationUnit("ShareLevel.java", buf.toString(), false, null);

		buf = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("package bug;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("class Bug {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("public ShareLevel createControl() {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("for (CaseType cat : all())\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("cat.hashCode();\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("ShareLevel temp = all;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("return temp;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("};\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		units[2] = pack1.createCompilationUnit("Bug.java", buf.toString(), false, null);

		ImportRewrite imports = newImportsRewrite(units[2], new String[] {}, 99, 99, false);
		imports.addStaticImport("bug.CaseType", "all", false);
		imports.addStaticImport("bug.ShareLevel", "all", true);

		apply(imports);

		buf = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("package bug;\n\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import static bug.CaseType.all;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import static bug.ShareLevel.all;\n\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("class Bug {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("public ShareLevel createControl() {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("for (CaseType cat : all())\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("cat.hashCode();\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("ShareLevel temp = all;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("return temp;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("};\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		assertEqualString(units[2].getSource(), buf.toString());
	}

	/**
	 * Expects that comments in a variety of positions around and between import declarations
	 * are preserved when restoreExistingImports is set to false.
	 */
	public void testAddImports_bug24804() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("pack1", false, null);
		StringBuilder buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("package pack1;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("/** floating comment before first import */\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import java.util.ArrayList; // trailing same-line comment\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("/** floating comment between imports*/\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("/** preceding-line comment */\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import java.util.Collection;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("/** comment on line between imports */\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import java.util.Deque;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("public class C {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);

		String[] order= new String[] { "java" };

		ImportRewrite imports= newImportsRewrite(cu, order, 99, 99, false);
		imports.addImport("java.util.ArrayList");
		imports.addImport("java.util.Collection");
		imports.addImport("java.util.Deque");

		apply(imports);

		buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("package pack1;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("/** floating comment before first import */\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import java.util.ArrayList; // trailing same-line comment\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("/** floating comment between imports*/\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("/** preceding-line comment */\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import java.util.Collection;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("/** comment on line between imports */\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import java.util.Deque;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("public class C {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		assertEqualString(cu.getSource(), buf.toString());
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=376930
    public void testBug376930() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
        IPackageFragment pack1 = this.sourceFolder.createPackageFragment("pack1", false, null);
        StringBuilder buf = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
        // 2 imports are in 1 group but third is separated by a comment
        buf.append( //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
                "package pack1;\n" +
                "\n" +
                "import java.util.*; // test\n" +
                "import java.util.Map.Entry;\n" +
                "//comment 2\n" +
                "import java.util.Map.SomethingElse;\n" +
                "// commen 3\n" +
                "\n" +
                "public class C {\n" +
                "    public static void main(String[] args) {\n" +
                "        HashMap h;\n" +
                "\n" +
                "        Map.Entry e= null;\n" +
                "        Entry e2= null;\n" +
                "\n" +
                "        PrintWriter pw;\n" +
                "        System.out.println(\"hello\");\n" +
                "    }\n" +
                "}");
        ICompilationUnit cu = pack1.createCompilationUnit("C.java", buf.toString(), false, null);

        String[] order = new String[] { "java", "java.util", "com", "pack" };

        ImportRewrite imports= newImportsRewrite(cu, order, 1, 1, true);
        imports.setUseContextToFilterImplicitImports(true);
        imports.addImport("java.io.PrintWriter");

        apply(imports);

        buf = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
        buf.append( //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
                "package pack1;\n" +
                "\n" +
                "import java.io.*;\n" +
                "\n" +
                "import java.util.*; // test\n" +
                "import java.util.Map.Entry;\n" +
                "//comment 2\n" +
                "import java.util.Map.SomethingElse;\n" +
                "// commen 3\n" +
                "\n" +
                "public class C {\n" +
                "    public static void main(String[] args) {\n" +
                "        HashMap h;\n" +
                "\n" +
                "        Map.Entry e= null;\n" +
                "        Entry e2= null;\n" +
                "\n" +
                "        PrintWriter pw;\n" +
                "        System.out.println(\"hello\");\n" +
                "    }\n" +
                "}");
        assertEqualString(cu.getSource(), buf.toString());
    }

    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=376930
    public void testBug376930_2() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
        IPackageFragment pack1 = this.sourceFolder.createPackageFragment("pack1", false, null);
        StringBuilder buf = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
        // all imports are in same group
        buf.append( //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
                "package pack1;\n" +
                "\n" +
                "// comment 1\n" +
                "import java.util.*; // test\n" +
                "import java.util.Map.Entry; // test2\n" +
                "import java.util.Map.SomethingElse;\n" +
                "// commen 3\n" +
                "\n" +
                "public class C {\n" +
                "    public static void main(String[] args) {\n" +
                "        HashMap h;\n" +
                "\n" +
                "        Map.Entry e= null;\n" +
                "        Entry e2= null;\n" +
                "\n" +
                "        PrintWriter pw;\n" +
                "        System.out.println(\"hello\");\n" +
                "    }\n" +
                "}");
        ICompilationUnit cu = pack1.createCompilationUnit("C.java", buf.toString(), false, null);

        String[] order = new String[] { "java", "java.util", "com", "pack" };

        ImportRewrite imports= newImportsRewrite(cu, order, 1, 1, true);
        imports.setUseContextToFilterImplicitImports(true);
        imports.addImport("java.io.PrintWriter");

        apply(imports);

        buf = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
        buf.append( //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
                "package pack1;\n" +
                "\n" +
                "import java.io.*;\n" +
                "\n" +
                "// comment 1\n" +
                "import java.util.*; // test\n" +
                "import java.util.Map.Entry; // test2\n" +
                "import java.util.Map.SomethingElse;\n" +
                "// commen 3\n" +
                "\n" +
                "public class C {\n" +
                "    public static void main(String[] args) {\n" +
                "        HashMap h;\n" +
                "\n" +
                "        Map.Entry e= null;\n" +
                "        Entry e2= null;\n" +
                "\n" +
                "        PrintWriter pw;\n" +
                "        System.out.println(\"hello\");\n" +
                "    }\n" +
                "}");
        assertEqualString(cu.getSource(), buf.toString());
    }

    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=376930
    public void testBug376930_3() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
        IPackageFragment pack1 = this.sourceFolder.createPackageFragment("pack1", false, null);
        StringBuilder buf = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
        // all imports are in same group
        // leading and trailing comments
        buf.append( //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
                "package pack1;\n" +
                "\n" +
                "// comment 1\n" +
                "/* lead 1*/ import java.util.*; // test1\n" +
                "/* lead 2*/import java.util.Map.Entry; // test2\n" +
                "/* lead 3*/ import java.util.Map.SomethingElse; // test3\n" +
                "// commen 3\n" +
                "\n" +
                "public class C {\n" +
                "    public static void main(String[] args) {\n" +
                "        HashMap h;\n" +
                "\n" +
                "        Map.Entry e= null;\n" +
                "        Entry e2= null;\n" +
                "\n" +
                "        PrintWriter pw;\n" +
                "        System.out.println(\"hello\");\n" +
                "    }\n" +
                "}");
        ICompilationUnit cu = pack1.createCompilationUnit("C.java", buf.toString(), false, null);

        String[] order = new String[] { "java", "java.util", "com", "pack" };

        ImportRewrite imports= newImportsRewrite(cu, order, 1, 1, true);
        imports.setUseContextToFilterImplicitImports(true);
        imports.addImport("java.io.PrintWriter");

        apply(imports);

        buf = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
        buf.append( //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
                "package pack1;\n" +
                "\n" +
                "import java.io.*;\n" +
                "\n" +
                "// comment 1\n" +
                "/* lead 1*/ import java.util.*; // test1\n" +
                "/* lead 2*/import java.util.Map.Entry; // test2\n" +
                "/* lead 3*/ import java.util.Map.SomethingElse; // test3\n" +
                "// commen 3\n" +
                "\n" +
                "public class C {\n" +
                "    public static void main(String[] args) {\n" +
                "        HashMap h;\n" +
                "\n" +
                "        Map.Entry e= null;\n" +
                "        Entry e2= null;\n" +
                "\n" +
                "        PrintWriter pw;\n" +
                "        System.out.println(\"hello\");\n" +
                "    }\n" +
                "}");
        assertEqualString(cu.getSource(), buf.toString());
    }

    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=376930
    // remove imports, preserve all comments
    public void testBug376930_3a() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
        IPackageFragment pack1 = this.sourceFolder.createPackageFragment("pack1", false, null);
        StringBuilder buf = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
        buf.append( //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
                "package pack1;\n" +
                "\n" +
                "// comment 1\n" +
                "/* lead 1*/ import java.util.*; // test1\n" +
                "/* lead 2*/import java.util.Map.Entry; // test2\n" +
                "/* lead 3*/ import java.util.Map.SomethingElse; // test3\n" +
                "// commen 3\n" +
                "\n" +
                "public class C {\n" +
                "    public static void main(String[] args) {\n" +
                "        HashMap h;\n" +
                "\n" +
                "        Map.Entry e= null;\n" +
                "        Entry e2= null;\n" +
                "\n" +
                "        PrintWriter pw;\n" +
                "        System.out.println(\"hello\");\n" +
                "    }\n" +
                "}");
        ICompilationUnit cu = pack1.createCompilationUnit("C.java", buf.toString(), false, null);

        String[] order = new String[] { "java", "java.util", "com", "pack" };

        ImportRewrite imports= newImportsRewrite(cu, order, 1, 1, false);
        imports.setUseContextToFilterImplicitImports(true);
        imports.addImport("java.io.PrintWriter");

        apply(imports);

        buf = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
        buf.append( //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
                "package pack1;\n" +
                "\n" +
				"import java.io.*;\n" +
				"\n" +
                "public class C {\n" +
                "    public static void main(String[] args) {\n" +
                "        HashMap h;\n" +
                "\n" +
                "        Map.Entry e= null;\n" +
                "        Entry e2= null;\n" +
                "\n" +
                "        PrintWriter pw;\n" +
                "        System.out.println(\"hello\");\n" +
                "    }\n" +
                "}");
        assertEqualString(cu.getSource(), buf.toString());
    }

    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=376930
    public void testBug376930_4() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
        IPackageFragment pack1 = this.sourceFolder.createPackageFragment("pack1", false, null);
        StringBuilder buf = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
        // all imports are in same group
        // leading and trailing comments
        // two on demand imports in the group
        buf.append( //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
                "package pack1;\n" +
                "\n" +
                "// comment 1\n" +
                "/* lead 1*/ import java.util.*; // test1\n" +
                "/* lead 2*/import java.util.Map.*; // test2\n" +
                "/* lead 3*/ import java.util.Map.SomethingElse; // test3\n" +
                "// commen 3\n" +
                "\n" +
                "public class C {\n" +
                "    public static void main(String[] args) {\n" +
                "        HashMap h;\n" +
                "\n" +
                "        Map.Entry e= null;\n" +
                "        Entry e2= null;\n" +
                "\n" +
                "        PrintWriter pw;\n" +
                "        System.out.println(\"hello\");\n" +
                "    }\n" +
                "}");
        ICompilationUnit cu = pack1.createCompilationUnit("C.java", buf.toString(), false, null);

        String[] order = new String[] { "java", "java.util", "com", "pack" };

        ImportRewrite imports= newImportsRewrite(cu, order, 1, 1, true);
        imports.setUseContextToFilterImplicitImports(true);
        imports.addImport("java.io.PrintWriter");

        apply(imports);

        buf = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
        buf.append( //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
                "package pack1;\n" +
                "\n" +
                "import java.io.*;\n" +
                "\n" +
                "// comment 1\n" +
                "/* lead 1*/ import java.util.*; // test1\n" +
                "/* lead 2*/import java.util.Map.*; // test2\n" +
                "/* lead 3*/ import java.util.Map.SomethingElse; // test3\n" +
                "// commen 3\n" +
                "\n" +
                "public class C {\n" +
                "    public static void main(String[] args) {\n" +
                "        HashMap h;\n" +
                "\n" +
                "        Map.Entry e= null;\n" +
                "        Entry e2= null;\n" +
                "\n" +
                "        PrintWriter pw;\n" +
                "        System.out.println(\"hello\");\n" +
                "    }\n" +
                "}");
        assertEqualString(cu.getSource(), buf.toString());
    }

    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=376930
    // remove imports, preserve all comments
    public void testBug376930_4a() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
        IPackageFragment pack1 = this.sourceFolder.createPackageFragment("pack1", false, null);
        StringBuilder buf = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
        buf.append( //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
                "package pack1;\n" +
                "\n" +
                "// comment 1\n" +
                "/* lead 1*/ import java.util.HashMap; // test1\n" +
                "/* lead 2*/import java.util.Map.*; // test2\n" +
                "/* lead 3*/ import java.util.Map.SomethingElse; // test3\n" +
                "// commen 3\n" +
                "\n" +
                "public class C {\n" +
                "    public static void main(String[] args) {\n" +
                "        HashMap h;\n" +
                "\n" +
                "        Map.Entry e= null;\n" +
                "        Entry e2= null;\n" +
                "\n" +
                "        PrintWriter pw;\n" +
                "        System.out.println(\"hello\");\n" +
                "    }\n" +
                "}");
        ICompilationUnit cu = pack1.createCompilationUnit("C.java", buf.toString(), false, null);

        String[] order = new String[] { "java", "java.util", "com", "pack" };

        ImportRewrite imports= newImportsRewrite(cu, order, 1, 1, false);
        imports.setUseContextToFilterImplicitImports(true);
        imports.addImport("java.io.PrintWriter");

        apply(imports);

        buf = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
        buf.append( //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
                "package pack1;\n" +
                "\n" +
				"import java.io.*;\n" +
				"\n" +
                "public class C {\n" +
                "    public static void main(String[] args) {\n" +
                "        HashMap h;\n" +
                "\n" +
                "        Map.Entry e= null;\n" +
                "        Entry e2= null;\n" +
                "\n" +
                "        PrintWriter pw;\n" +
                "        System.out.println(\"hello\");\n" +
                "    }\n" +
                "}");
        assertEqualString(cu.getSource(), buf.toString());
    }

    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=376930
    public void testBug376930_5() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
        IPackageFragment pack1 = this.sourceFolder.createPackageFragment("pack1", false, null);
        StringBuilder buf = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
        // all imports of same group are scattered around
        // leading and trailing comments
        // adding an on-demand import belonging to a group
        buf.append( //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
                "package pack1;\n" +
                "\n" +
                "// comment 1\n" +
                "/* lead 1*/ import java.util.*; // test1\n" +
                "/* lead 2*/import java.io.PrintWriter.*; // test2\n" +
                "/* lead 3*/ import java.util.Map.SomethingElse; // test3\n" +
                "// commen 3\n" +
                "\n" +
                "public class C {\n" +
                "    public static void main(String[] args) {\n" +
                "        HashMap h;\n" +
                "\n" +
                "        Map.Entry e= null;\n" +
                "        Entry e2= null;\n" +
                "\n" +
                "        PrintWriter pw;\n" +
                "        System.out.println(\"hello\");\n" +
                "    }\n" +
                "}");
        ICompilationUnit cu = pack1.createCompilationUnit("C.java", buf.toString(), false, null);

        String[] order = new String[] { "java", "java.util", "com", "pack" };

        ImportRewrite imports= newImportsRewrite(cu, order, 1, 1, true);
        imports.setUseContextToFilterImplicitImports(true);
        imports.addImport("java.util.Map.*");

        apply(imports);

        buf = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
        // java.util.Map.* is placed after java.util.* and is assigned the comments
        // from java.util.Map.SomethingElse.
        buf.append( //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
                "package pack1;\n" +
                "\n" +
                "// comment 1\n" +
                "/* lead 1*/ import java.util.*; // test1\n" +
                "/* lead 3*/\n" +
                "// test3\n" +
                "// commen 3\n" +
                "import java.util.Map.*;\n" +
                "\n" +
                "/* lead 2*/import java.io.PrintWriter.*; // test2\n" +
                "\n" +
                "public class C {\n" +
                "    public static void main(String[] args) {\n" +
                "        HashMap h;\n" +
                "\n" +
                "        Map.Entry e= null;\n" +
                "        Entry e2= null;\n" +
                "\n" +
                "        PrintWriter pw;\n" +
                "        System.out.println(\"hello\");\n" +
                "    }\n" +
                "}");
        assertEqualString(cu.getSource(), buf.toString());
    }

    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=376930
    public void testBug376930_5a() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
        IPackageFragment pack1 = this.sourceFolder.createPackageFragment("pack1", false, null);
        StringBuilder buf = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
        // all imports are in same group
        // leading and trailing comments
        // adding an on-demand import belonging to a group
        buf.append( //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
                "package pack1;\n" +
                "\n" +
                "// comment 1\n" +
                "/* lead 2*/import java.io.PrintWriter.*; // test2\n" +
                "/* lead 1*/ import java.util.*; // test1\n" +
                "/* lead 3*/ import java.util.Map.SomethingElse; // test3\n" +
                "// commen 3\n" +
                "\n" +
                "public class C {\n" +
                "    public static void main(String[] args) {\n" +
                "        HashMap h;\n" +
                "\n" +
                "        Map.Entry e= null;\n" +
                "        Entry e2= null;\n" +
                "\n" +
                "        PrintWriter pw;\n" +
                "        System.out.println(\"hello\");\n" +
                "    }\n" +
                "}");
        ICompilationUnit cu = pack1.createCompilationUnit("C.java", buf.toString(), false, null);

        String[] order = new String[] { "java", "java.util", "com", "pack" };

        ImportRewrite imports= newImportsRewrite(cu, order, 1, 1, true);
        imports.setUseContextToFilterImplicitImports(true);
        imports.addImport("java.util.Map.*");

        apply(imports);

        // java.util.Map.* takes the place of java.util.Map.SomethingElse,
        // and the latter's comments are reassigned to it.
        buf = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
        buf.append( //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
                "package pack1;\n" +
                "\n" +
                "// comment 1\n" +
                "/* lead 2*/import java.io.PrintWriter.*; // test2\n" +
                "/* lead 1*/ import java.util.*; // test1\n" +
                "/* lead 3*/\n" +
                "// test3\n" +
                "// commen 3\n" +
                "import java.util.Map.*;\n" +
                "\n" +
                "public class C {\n" +
                "    public static void main(String[] args) {\n" +
                "        HashMap h;\n" +
                "\n" +
                "        Map.Entry e= null;\n" +
                "        Entry e2= null;\n" +
                "\n" +
                "        PrintWriter pw;\n" +
                "        System.out.println(\"hello\");\n" +
                "    }\n" +
                "}");
        assertEqualString(cu.getSource(), buf.toString());
    }

    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=376930
    // added import should get folded into existing *, without touching comments
    public void testBug376930_5b() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
        IPackageFragment pack1 = this.sourceFolder.createPackageFragment("pack1", false, null);
        StringBuilder buf = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
        buf.append( //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
                "package pack1;\n" +
                "\n" +
                "// comment 1\n" +
                "/* lead 2*/import java.io.PrintWriter.*; // test2\n" +
                "/* lead 1*/ import java.util.*; // test1\n" +
                "/* lead 3*/ import java.util.Map.SomethingElse; // test3\n" +
                "// commen 3\n" +
                "\n" +
                "public class C {\n" +
                "    public static void main(String[] args) {\n" +
                "        HashMap h;\n" +
                "\n" +
                "        Map.Entry e= null;\n" +
                "        Entry e2= null;\n" +
                "\n" +
                "        PrintWriter pw;\n" +
                "        System.out.println(\"hello\");\n" +
                "    }\n" +
                "}");
        ICompilationUnit cu = pack1.createCompilationUnit("C.java", buf.toString(), false, null);

        String[] order = new String[] { "java", "java.util", "com", "pack" };

        ImportRewrite imports= newImportsRewrite(cu, order, 1, 1, false);
        imports.setUseContextToFilterImplicitImports(true);
        imports.addImport("java.util.Map");

        apply(imports);

        buf = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
        buf.append( //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
                "package pack1;\n" +
                "\n" +
                "/* lead 1*/ import java.util.*; // test1\n" +
				"\n" +
                "public class C {\n" +
                "    public static void main(String[] args) {\n" +
                "        HashMap h;\n" +
                "\n" +
                "        Map.Entry e= null;\n" +
                "        Entry e2= null;\n" +
                "\n" +
                "        PrintWriter pw;\n" +
                "        System.out.println(\"hello\");\n" +
                "    }\n" +
                "}");
        assertEqualString(cu.getSource(), buf.toString());
    }

    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=376930
    // remove imports, preserve all comments
    public void testBug376930_5c() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
        IPackageFragment pack1 = this.sourceFolder.createPackageFragment("pack1", false, null);
        StringBuilder buf = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
        buf.append( //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
                "package pack1;\n" +
                "\n" +
                "// comment 1\n" +
                "/* lead 1*/ import java.util.*; // test1\n" +
                "/* lead 2*/import java.io.PrintWriter.*; // test2\n" +
                "/* lead 3*/ import java.util.Map.SomethingElse; // test3\n" +
                "// commen 3\n" +
                "\n" +
                "public class C {\n" +
                "    public static void main(String[] args) {\n" +
                "        HashMap h;\n" +
                "\n" +
                "        Map.Entry e= null;\n" +
                "        Entry e2= null;\n" +
                "\n" +
                "        PrintWriter pw;\n" +
                "        System.out.println(\"hello\");\n" +
                "    }\n" +
                "}");
        ICompilationUnit cu = pack1.createCompilationUnit("C.java", buf.toString(), false, null);

        String[] order = new String[] { "java", "java.util", "com", "pack" };

        ImportRewrite imports= newImportsRewrite(cu, order, 1, 1, false);
        imports.setUseContextToFilterImplicitImports(true);
        imports.addImport("java.util.Map.*");

        apply(imports);

        // java.util.Map.* takes the place of java.util.Map.SomethingElse,
        // and the latter's comments are reassigned to it.
        buf = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
        buf.append( //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
                "package pack1;\n" +
                "\n" +
				"/* lead 3*/\n" +
				"// test3\n" +
				"// commen 3\n" +
				"import java.util.Map.*;\n" +
				"\n" +
                "public class C {\n" +
                "    public static void main(String[] args) {\n" +
                "        HashMap h;\n" +
                "\n" +
                "        Map.Entry e= null;\n" +
                "        Entry e2= null;\n" +
                "\n" +
                "        PrintWriter pw;\n" +
                "        System.out.println(\"hello\");\n" +
                "    }\n" +
                "}");
        assertEqualString(cu.getSource(), buf.toString());
    }

    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=376930
    // added import should get folded along with existing import into *, without deleting comments
    public void testBug376930_5d() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
        IPackageFragment pack1 = this.sourceFolder.createPackageFragment("pack1", false, null);
        StringBuilder buf = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
        buf.append( //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
                "package pack1;\n" +
                "\n" +
                "// comment 1\n" +
                "/* lead 2*/import java.io.PrintWriter.*; // test2\n" +
                "/* lead 1*/ import java.util.Map; // test1\n" +
                "// commen 3\n" +
                "\n" +
                "public class C {\n" +
                "    public static void main(String[] args) {\n" +
                "        HashMap h;\n" +
                "\n" +
                "        Map.Entry e= null;\n" +
                "        Entry e2= null;\n" +
                "\n" +
                "        PrintWriter pw;\n" +
                "        System.out.println(\"hello\");\n" +
                "    }\n" +
                "}");
        ICompilationUnit cu = pack1.createCompilationUnit("C.java", buf.toString(), false, null);

        String[] order = new String[] { "java", "java.util", "com", "pack" };

        ImportRewrite imports= newImportsRewrite(cu, order, 2, 2, true);
        imports.setUseContextToFilterImplicitImports(true);
        imports.addImport("java.util.HashMap");

        apply(imports);

        buf = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
        buf.append( //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
                "package pack1;\n" +
                "\n" +
                "// comment 1\n" +
                "/* lead 2*/import java.io.PrintWriter.*; // test2\n" +
                "\n" +
                "/* lead 1*/\n" +
                "// test1\n" +
                "// commen 3\n" +
                "import java.util.*;\n" +
                "\n" +
                "public class C {\n" +
                "    public static void main(String[] args) {\n" +
                "        HashMap h;\n" +
                "\n" +
                "        Map.Entry e= null;\n" +
                "        Entry e2= null;\n" +
                "\n" +
                "        PrintWriter pw;\n" +
                "        System.out.println(\"hello\");\n" +
                "    }\n" +
                "}");
        assertEqualString(cu.getSource(), buf.toString());
    }

    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=376930
    // separating comment should not prevent folding into *-import
    public void testBug376930_5e() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
        IPackageFragment pack1 = this.sourceFolder.createPackageFragment("pack1", false, null);
        StringBuilder buf = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
        buf.append( //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
                "package pack1;\n" +
                "\n" +
                "import java.util.Map;\n" +
                "/* comment leading Map.Entry */\n" +
                "import java.util.Map.Entry;\n" +
                "\n" +
                "public class C {\n" +
                "    public static void main(String[] args) {\n" +
                "        HashMap h;\n" +
                "\n" +
                "        Map.Entry e= null;\n" +
                "        Entry e2= null;\n" +
                "\n" +
                "    }\n" +
                "}");
        ICompilationUnit cu = pack1.createCompilationUnit("C.java", buf.toString(), false, null);

        String[] order = new String[] { "java", "javax", "org", "com" };

        ImportRewrite imports= newImportsRewrite(cu, order, 2, 2, true);
        imports.setUseContextToFilterImplicitImports(true);
        imports.addImport("java.util.HashMap");

        apply(imports);

        buf = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
        buf.append( //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
                "package pack1;\n" +
                "\n" +
                "import java.util.*;\n" +
                "/* comment leading Map.Entry */\n" +
                "import java.util.Map.Entry;\n" +
                "\n" +
                "public class C {\n" +
                "    public static void main(String[] args) {\n" +
                "        HashMap h;\n" +
                "\n" +
                "        Map.Entry e= null;\n" +
                "        Entry e2= null;\n" +
                "\n" +
                "    }\n" +
                "}");
        assertEqualString(cu.getSource(), buf.toString());
    }

    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=378024
    public void testBug378024() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
        IPackageFragment pack1 = this.sourceFolder.createPackageFragment("pack1", false, null);
        StringBuilder buf = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
        buf.append( //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
                "package pack1;\n" +
                "\n" +
                "// comment 1\n" +
                "/*\n" +
                " * keep me with List\n" +
                " *\n" +
                " */\n" +
                "import java.awt.List;// test1\n" +
                "/*\n" +
                " * keep me with Serializable\n" +
                " */\n" +
                "import java.io.Serializable;// test2\n" +
                "/*\n" +
                " * keep me with HashMap\n" +
                " */\n" +
                "import java.util.HashMap;// test3\n" +
                "// commen 3\n" +
                "\n" +
                "public class C implements Serializable{\n" +
                "    public static void main(String[] args) {\n" +
                "        List l = new List();\n" +
                "        Map e= null;\n" +
                "    }\n" +
                "}");
        ICompilationUnit cu = pack1.createCompilationUnit("C.java", buf.toString(), false, null);

        String[] order = new String[] { "java", "java.awt", "java.io", "java.util" };

        ImportRewrite imports= newImportsRewrite(cu, order, 2, 2, false);
        imports.setUseContextToFilterImplicitImports(true);
        imports.addImport("java.awt.List");
        imports.addImport("java.io.Serializable");
        imports.addImport("java.util.HashMap");

        apply(imports);

        buf = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
        buf.append( //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
                "package pack1;\n" +
                "\n" +
                "// comment 1\n" +
                "/*\n" +
                " * keep me with List\n" +
                " *\n" +
                " */\n" +
                "import java.awt.List;// test1\n\n" +
                "/*\n" +
                " * keep me with Serializable\n" +
                " */\n" +
                "import java.io.Serializable;// test2\n\n" +
                "/*\n" +
                " * keep me with HashMap\n" +
                " */\n" +
                "import java.util.HashMap;// test3\n" +
                "// commen 3\n" +
                "\n" +
                "public class C implements Serializable{\n" +
                "    public static void main(String[] args) {\n" +
                "        List l = new List();\n" +
                "        Map e= null;\n" +
                "    }\n" +
                "}");
        assertEqualString(cu.getSource(), buf.toString());
    }

    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=378024
    public void testBug378024b() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
        IPackageFragment pack1 = this.sourceFolder.createPackageFragment("pack1", false, null);
        StringBuilder buf = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
        buf.append( //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
                "package pack1;\n" +
                "\n" +
                "// comment 1\n" +
                "/*\n" +
                " * don't move me 1\n" +
                " *\n" +
                " */\n" +
                "import java.awt.List;// test1\n" +
                "/*\n" +
                " * don't move me 2\n" +
                " */\n" +
                "import java.io.Serializable;// test2\n" +
                "/*\n" +
                " * don't move me 3\n" +
                " */\n" +
                "import java.util.HashMap;// test3\n" +
                "// commen 3\n" +
                "\n" +
                "public class C implements Serializable{\n" +
                "    public static void main(String[] args) {\n" +
                "        List l = new List();\n" +
                "        Map e= null;\n" +
                "    }\n" +
                "}");
        ICompilationUnit cu = pack1.createCompilationUnit("C.java", buf.toString(), false, null);

        String[] order = new String[] { "java", "java.util", "com", "pack" };

        ImportRewrite imports= newImportsRewrite(cu, order, 1, 1, false);
        imports.setUseContextToFilterImplicitImports(true);
        imports.addImport("java.awt.List");
        imports.addImport("java.io.Serializable");
        imports.addImport("java.util.HashMap");

        apply(imports);

        buf = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
        buf.append( //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
                "package pack1;\n" +
                "\n" +
                "// comment 1\n" +
                "/*\n" +
                " * don't move me 1\n" +
                " *\n" +
                " */\n" +
                "// test1\n" +
                "import java.awt.*;\n" +
                "/*\n" +
                " * don't move me 2\n" +
                " */\n" +
                "// test2\n" +
                "import java.io.*;\n" +
                "\n" +
                "/*\n" +
                " * don't move me 3\n" +
                " */\n" +
                "// test3\n" +
                "// commen 3\n" +
                "import java.util.*;\n" +
                "\n" +
                "public class C implements Serializable{\n" +
                "    public static void main(String[] args) {\n" +
                "        List l = new List();\n" +
                "        Map e= null;\n" +
                "    }\n" +
                "}");
        assertEqualString(cu.getSource(), buf.toString());
    }

    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=378024
    // leading and trailing comments always move with imports.
    // comments in between stay where they are
    public void testBug378024c() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
        IPackageFragment pack1 = this.sourceFolder.createPackageFragment("pack1", false, null);
        StringBuilder buf = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
        buf.append( //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
                "package pack1;\n" +
                "\n" +
                "// comment 1\n" +
                "/*\n" +
                " * don't move me 1\n" +
                " *\n" +
                " */\n" +
                "\n" +
                "// lead 1\n" +
                "import java.awt.List;// test1\n" +
                "\n" +
                "/*\n" +
                " * don't move me 2\n" +
                " */\n" +
                "\n" +
                "// lead 2\n" +
                "import java.io.Serializable;// test2\n" +
                "/*\n" +
                " * don't move me 3\n" +
                " */\n" +
                "\n" +
                "/*\n" +
                " * don't move me 4\n" +
                " */\n" +
                "\n" +
                "//lead 3\n" +
                "import java.util.HashMap;// test3\n" +
                "// commen 3\n" +
                "\n" +
                "public class C implements Serializable{\n" +
                "    public static void main(String[] args) {\n" +
                "        List l = new List();\n" +
                "        Map e= null;\n" +
                "    }\n" +
                "}");
        ICompilationUnit cu = pack1.createCompilationUnit("C.java", buf.toString(), false, null);

        String[] order = new String[] { "java", "java.util", "com", "pack" };

        ImportRewrite imports= newImportsRewrite(cu, order, 99, 99, false);
        imports.setUseContextToFilterImplicitImports(true);
        imports.addImport("java.awt.List");
        imports.addImport("java.io.Serializable");
        imports.addImport("java.util.HashMap");

        apply(imports);

        buf = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
        buf.append( //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
                "package pack1;\n" +
                "\n" +
                "// comment 1\n" +
                "/*\n" +
                " * don't move me 1\n" +
                " *\n" +
                " */\n" +
                "\n" +
                "// lead 1\n" +
                "import java.awt.List;// test1\n" +
                "\n" +
                "/*\n" +
                " * don't move me 2\n" +
                " */\n" +
                "\n" +
                "// lead 2\n" +
                "import java.io.Serializable;// test2\n" +
                "/*\n" +
                " * don't move me 3\n" +
                " */\n" +
                "\n" +
                "/*\n" +
                " * don't move me 4\n" +
                " */\n" +
                "\n" +
                "//lead 3\n" +
                "import java.util.HashMap;// test3\n" +
                "// commen 3\n" +
                "\n" +
                "public class C implements Serializable{\n" +
                "    public static void main(String[] args) {\n" +
                "        List l = new List();\n" +
                "        Map e= null;\n" +
                "    }\n" +
                "}");
        assertEqualString(cu.getSource(), buf.toString());
    }

    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=378024
    // leading and trailing comments always move with imports.
    // comments in between stay where they are
    public void testBug378024c_1() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
        IPackageFragment pack1 = this.sourceFolder.createPackageFragment("pack1", false, null);
        StringBuilder buf = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
        buf.append( //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
                "package pack1;\n" +
                "\n" +
                "// comment 1\n" +
                "/*\n" +
                " * don't move me 1\n" +
                " *\n" +
                " */\n" +
                "\n" +
                "// lead 1\n" +
                "import java.awt.List;// test1\n" +
                "\n" +
                "/*\n" +
                " * don't move me 2\n" +
                " */\n" +
                "\n" +
                "// lead 2\n" +
                "import java.io.Serializable;// test2\n" +
                "/*\n" +
                " * don't move me 3\n" +
                " */\n" +
                "\n" +
                "/*\n" +
                " * don't move me 4\n" +
                " */\n" +
                "\n" +
                "//lead 3\n" +
                "import java.util.HashMap;// test3\n" +
                "// commen 3\n" +
                "\n" +
                "public class C implements Serializable{\n" +
                "    public static void main(String[] args) {\n" +
                "        List l = new List();\n" +
                "        Map e= null;\n" +
                "    }\n" +
                "}");
        ICompilationUnit cu = pack1.createCompilationUnit("C.java", buf.toString(), false, null);

        String[] order = new String[] { "java", "com", "pack" };

        ImportRewrite imports= newImportsRewrite(cu, order, 99, 99, false);
        imports.setUseContextToFilterImplicitImports(true);
        imports.addImport("java.awt.List");
        imports.addImport("java.io.Serializable");
        imports.addImport("java.util.HashMap");

        apply(imports);

        buf = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
        buf.append( //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
                "package pack1;\n" +
                "\n" +
                "// comment 1\n" +
                "/*\n" +
                " * don't move me 1\n" +
                " *\n" +
                " */\n" +
                "\n" +
                "// lead 1\n" +
                "import java.awt.List;// test1\n" +
                "\n" +
                "/*\n" +
                " * don't move me 2\n" +
                " */\n" +
                "\n" +
                "// lead 2\n" +
                "import java.io.Serializable;// test2\n" +
                "/*\n" +
                " * don't move me 3\n" +
                " */\n" +
                "\n" +
                "/*\n" +
                " * don't move me 4\n" +
                " */\n" +
                "\n" +
                "//lead 3\n" +
                "import java.util.HashMap;// test3\n" +
                "// commen 3\n" +
                "\n" +
                "public class C implements Serializable{\n" +
                "    public static void main(String[] args) {\n" +
                "        List l = new List();\n" +
                "        Map e= null;\n" +
                "    }\n" +
                "}");
        assertEqualString(cu.getSource(), buf.toString());
    }

    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=378024
    // leading and trailing comments always move with imports, even if they get folded.
    // comments in between stay where they are
    public void testBug378024c_2() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
        IPackageFragment pack1 = this.sourceFolder.createPackageFragment("pack1", false, null);
        StringBuilder buf = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
        buf.append( //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
                "package pack1;\n" +
                "\n" +
                "// comment 1\n" +
                "/*\n" +
                " * don't move me 1\n" +
                " *\n" +
                " */\n" +
                "\n" +
                "// lead 1\n" +
                "import java.awt.List;// test1\n" +
                "\n" +
                "/*\n" +
                " * don't move me 2\n" +
                " */\n" +
                "\n" +
                "// lead 2\n" +
                "import java.io.Serializable;// test2\n" +
                "/*\n" +
                " * don't move me 3\n" +
                " */\n" +
                "\n" +
                "/*\n" +
                " * don't move me 4\n" +
                " */\n" +
                "\n" +
                "//lead 3\n" +
                "import java.util.HashMap;// test3\n" +
                "// commen 3\n" +
                "\n" +
                "public class C implements Serializable{\n" +
                "    public static void main(String[] args) {\n" +
                "        List l = new List();\n" +
                "        Map e= null;\n" +
                "    }\n" +
                "}");
        ICompilationUnit cu = pack1.createCompilationUnit("C.java", buf.toString(), false, null);

        String[] order = new String[] { "java", "com", "pack" };

        ImportRewrite imports= newImportsRewrite(cu, order, 1, 1, false);
        imports.setUseContextToFilterImplicitImports(true);
        imports.addImport("java.awt.List");
        imports.addImport("java.io.Serializable");
        imports.addImport("java.util.HashMap");

        apply(imports);

        buf = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
        buf.append( //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
                "package pack1;\n" +
                "\n" +
                "// comment 1\n" +
                "/*\n" +
                " * don't move me 1\n" +
                " *\n" +
                " */\n" +
                "\n" +
                "// lead 1\n" +
                "// test1\n" +
                "import java.awt.*;\n" +
                "\n" +
                "/*\n" +
                " * don't move me 2\n" +
                " */\n" +
                "\n" +
                "// lead 2\n" +
                "// test2\n" +
                "/*\n" +
                " * don't move me 3\n" +
                " */\n" +
                "import java.io.*;\n" +
                "\n" +
                "/*\n" +
                " * don't move me 4\n" +
                " */\n" +
                "\n" +
                "//lead 3\n" +
                "// test3\n" +
                "// commen 3\n" +
                "import java.util.*;\n" +
                "\n" +
                "public class C implements Serializable{\n" +
                "    public static void main(String[] args) {\n" +
                "        List l = new List();\n" +
                "        Map e= null;\n" +
                "    }\n" +
                "}");
        assertEqualString(cu.getSource(), buf.toString());
    }

    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=378024
    // not adding an import should preserve its comments and put them at the end.
    public void testBug378024d() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
        IPackageFragment pack1 = this.sourceFolder.createPackageFragment("pack1", false, null);
        StringBuilder buf = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
        buf.append( //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
                "package pack1;\n" +
                "\n" +
                "// comment 1\n" +
                "/*\n" +
                " * don't move me 1\n" +
                " *\n" +
                " */\n" +
                "\n" +
                "// lead 1\n" +
                "import java.awt.List;// test1\n" +
                "\n" +
                "/*\n" +
                " * don't move me 2\n" +
                " */\n" +
                "\n" +
                "// lead 2\n" +
                "import java.io.Serializable;// test2\n" +
                "/*\n" +
                " * don't move me 3\n" +
                " */\n" +
                "\n" +
                "/*\n" +
                " * don't move me 4\n" +
                " */\n" +
                "\n" +
                "//lead 3\n" +
                "import java.util.HashMap;// test3\n" +
                "// commen 3\n" +
                "\n" +
                "public class C implements Serializable{\n" +
                "    public static void main(String[] args) {\n" +
                "        List l = new List();\n" +
                "        Map e= null;\n" +
                "    }\n" +
                "}");
        ICompilationUnit cu = pack1.createCompilationUnit("C.java", buf.toString(), false, null);

        String[] order = new String[] { "java", "java.util", "com", "pack" };

        ImportRewrite imports= newImportsRewrite(cu, order, 1, 1, false);
        imports.setUseContextToFilterImplicitImports(true);
        imports.addImport("java.awt.List");
        imports.addImport("java.util.HashMap");

        apply(imports);

        buf = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
        buf.append( //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
                "package pack1;\n" +
                "\n" +
                "// comment 1\n" +
                "/*\n" +
                " * don't move me 1\n" +
                " *\n" +
                " */\n" +
                "\n" +
                "// lead 1\n" +
                "// test1\n" +
                "import java.awt.*;\n" +
                "\n" +
                "/*\n" +
                " * don't move me 4\n" +
                " */\n" +
                "\n" +
                "//lead 3\n" +
                "// test3\n" +
                "// commen 3\n" +
                "import java.util.*;\n" +
                "\n" +
                "public class C implements Serializable{\n" +
                "    public static void main(String[] args) {\n" +
                "        List l = new List();\n" +
                "        Map e= null;\n" +
                "    }\n" +
                "}");
        assertEqualString(cu.getSource(), buf.toString());
    }

    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=378024
    // adding a new import should not disturb comments and import should be added in its group
    public void testBug378024e() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
        IPackageFragment pack1 = this.sourceFolder.createPackageFragment("pack1", false, null);
        StringBuilder buf = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
        buf.append( //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
                "package pack1;\n" +
                "\n" +
                "// comment 1\n" +
                "/*\n" +
                " * don't move me 1\n" +
                " *\n" +
                " */\n" +
                "\n" +
                "// lead 1\n" +
                "import java.awt.List;// test1\n" +
                "\n" +
                "/*\n" +
                " * don't move me 2\n" +
                " */\n" +
                "\n" +
                "// lead 2\n" +
                "import java.io.Serializable;// test2\n" +
                "/*\n" +
                " * don't move me 3\n" +
                " */\n" +
                "\n" +
                "/*\n" +
                " * don't move me 4\n" +
                " */\n" +
                "\n" +
                "//lead 3\n" +
                "import java.util.HashMap;// test3\n" +
                "// commen 3\n" +
                "\n" +
                "public class C implements Serializable{\n" +
                "    public static void main(String[] args) {\n" +
                "        List l = new List();\n" +
                "        Map e= null;\n" +
                "    }\n" +
                "}");
        ICompilationUnit cu = pack1.createCompilationUnit("C.java", buf.toString(), false, null);

        String[] order = new String[] { "java", "com", "pack" };

        ImportRewrite imports= newImportsRewrite(cu, order, 2, 2, false);
        imports.setUseContextToFilterImplicitImports(true);
        imports.addImport("java.awt.List");
        imports.addImport("java.io.Serializable");
        imports.addImport("java.io.PrintWriter");
        imports.addImport("java.util.HashMap");

        apply(imports);

        buf = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
        buf.append( //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
                "package pack1;\n" +
                "\n" +
                "// comment 1\n" +
                "/*\n" +
                " * don't move me 1\n" +
                " *\n" +
                " */\n" +
                "\n" +
                "// lead 1\n" +
                "import java.awt.List;// test1\n" +
                "\n" +
                "/*\n" +
                " * don't move me 2\n" +
                " */\n" +
                "\n" +
                "// lead 2\n" +
                "// test2\n" +
                "/*\n" +
                " * don't move me 3\n" +
                " */\n" +
                "import java.io.*;\n" +
                "\n" +
                "/*\n" +
                " * don't move me 4\n" +
                " */\n" +
                "\n" +
                "//lead 3\n" +
                "import java.util.HashMap;// test3\n" +
                "// commen 3\n" +
                "\n" +
                "public class C implements Serializable{\n" +
                "    public static void main(String[] args) {\n" +
                "        List l = new List();\n" +
                "        Map e= null;\n" +
                "    }\n" +
                "}");
        assertEqualString(cu.getSource(), buf.toString());
    }

    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=378024
    // removing an import should preserve its comments at the end, and adding a new import should not disturb
    // existing comments
    public void testBug378024e_1() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
        IPackageFragment pack1 = this.sourceFolder.createPackageFragment("pack1", false, null);
        StringBuilder buf = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
        buf.append( //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
                "package pack1;\n" +
                "\n" +
                "// comment 1\n" +
                "/*\n" +
                " * don't move me 1\n" +
                " *\n" +
                " */\n" +
                "\n" +
                "// lead 1\n" +
                "import java.awt.List;// test1\n" +
                "\n" +
                "/*\n" +
                " * don't move me 2\n" +
                " */\n" +
                "\n" +
                "// lead 2\n" +
                "import java.io.Serializable;// test2\n" +
                "/*\n" +
                " * don't move me 3\n" +
                " */\n" +
                "\n" +
                "/*\n" +
                " * don't move me 4\n" +
                " */\n" +
                "\n" +
                "//lead 3\n" +
                "import java.util.HashMap;// test3\n" +
                "// commen 3\n" +
                "\n" +
                "public class C implements Serializable{\n" +
                "    public static void main(String[] args) {\n" +
                "        List l = new List();\n" +
                "        Map e= null;\n" +
                "    }\n" +
                "}");
        ICompilationUnit cu = pack1.createCompilationUnit("C.java", buf.toString(), false, null);

        String[] order = new String[] { "java", "java.util", "com", "pack" };

        ImportRewrite imports= newImportsRewrite(cu, order, 2, 2, false);
        imports.setUseContextToFilterImplicitImports(true);
        imports.addImport("java.awt.List");
        imports.addImport("java.io.PrintWriter");
        imports.addImport("java.util.HashMap");

        apply(imports);

        buf = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
        buf.append( //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
                "package pack1;\n" +
                "\n" +
                "// comment 1\n" +
                "/*\n" +
                " * don't move me 1\n" +
                " *\n" +
                " */\n" +
                "\n" +
                "// lead 1\n" +
                "import java.awt.List;// test1\n" +
                "import java.io.PrintWriter;\n" +
                "\n" +
                "/*\n" +
                " * don't move me 4\n" +
                " */\n" +
                "\n" +
                "//lead 3\n" +
                "import java.util.HashMap;// test3\n" +
                "// commen 3\n" +
                "\n" +
                "public class C implements Serializable{\n" +
                "    public static void main(String[] args) {\n" +
                "        List l = new List();\n" +
                "        Map e= null;\n" +
                "    }\n" +
                "}");
        assertEqualString(cu.getSource(), buf.toString());
    }

    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=378024
    // folding imports because of a newly added import should preserve comments
    public void testBug378024f() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
        IPackageFragment pack1 = this.sourceFolder.createPackageFragment("pack1", false, null);
        StringBuilder buf = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
        buf.append( //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
                "package pack1;\n" +
                "\n" +
                "// comment 1\n" +
                "/*\n" +
                " * don't move me 1\n" +
                " *\n" +
                " */\n" +
                "\n" +
                "// lead 1\n" +
                "import java.awt.List;// test1\n" +
                "\n" +
                "/*\n" +
                " * don't move me 2\n" +
                " */\n" +
                "\n" +
                "// lead 2\n" +
                "import java.io.Serializable;// test2\n" +
                "/*\n" +
                " * don't move me 3\n" +
                " */\n" +
                "\n" +
                "/*\n" +
                " * don't move me 4\n" +
                " */\n" +
                "\n" +
                "//lead 3\n" +
                "import java.util.HashMap;// test3\n" +
                "// commen 3\n" +
                "\n" +
                "public class C implements Serializable{\n" +
                "    public static void main(String[] args) {\n" +
                "        List l = new List();\n" +
                "        Map e= null;\n" +
                "    }\n" +
                "}");
        ICompilationUnit cu = pack1.createCompilationUnit("C.java", buf.toString(), false, null);

        String[] order = new String[] { "java", "com", "pack" };

        ImportRewrite imports= newImportsRewrite(cu, order, 2, 2, false);
        imports.setUseContextToFilterImplicitImports(true);
        imports.addImport("java.awt.List");
        imports.addImport("java.io.Serializable");
        imports.addImport("java.io.PrintWriter");
        imports.addImport("java.util.HashMap");

        apply(imports);

        buf = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
        buf.append( //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
                "package pack1;\n" +
                "\n" +
                "// comment 1\n" +
                "/*\n" +
                " * don't move me 1\n" +
                " *\n" +
                " */\n" +
                "\n" +
                "// lead 1\n" +
                "import java.awt.List;// test1\n" +
                "\n" +
                "/*\n" +
                " * don't move me 2\n" +
                " */\n" +
                "\n" +
                "// lead 2\n" +
                "// test2\n" +
                "/*\n" +
                " * don't move me 3\n" +
                " */\n" +
                "import java.io.*;\n" +
                "\n" +
                "/*\n" +
                " * don't move me 4\n" +
                " */\n" +
                "\n" +
                "//lead 3\n" +
                "import java.util.HashMap;// test3\n" +
                "// commen 3\n" +
                "\n" +
                "public class C implements Serializable{\n" +
                "    public static void main(String[] args) {\n" +
                "        List l = new List();\n" +
                "        Map e= null;\n" +
                "    }\n" +
                "}");
        assertEqualString(cu.getSource(), buf.toString());
    }

    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=378024
    // folding imports because of a newly added import should preserve comments
    public void testBug378024f_1() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
        IPackageFragment pack1 = this.sourceFolder.createPackageFragment("pack1", false, null);
        StringBuilder buf = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
        buf.append( //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
                "package pack1;\n" +
                "\n" +
                "// comment 1\n" +
                "/*\n" +
                " * keep me with List\n" +
                " *\n" +
                " */\n" +
                "\n" +
                "// lead 1\n" +
                "import java.awt.List;// test1\n" +
                "\n" +
                "/*\n" +
                " * keep me with Serializable\n" +
                " */\n" +
                "\n" +
                "// lead 2\n" +
                "import java.io.Serializable;// test2\n" +
                "/*\n" +
                " * keep me with Serializable 2\n" +
                " */\n" +
                "\n" +
                "// lead 3\n" +
                "import java.io.PrintWriter;// test3\n" +
                "/*\n" +
                " * keep me with PrintWriter\n" +
                " */\n" +
                "\n" +
                "/*\n" +
                " * don't move me\n" +
                " */\n" +
                "\n" +
                "//lead 4\n" +
                "import java.util.HashMap;// test4\n" +
                "// commen 3\n" +
                "\n" +
                "public class C implements Serializable{\n" +
                "    public static void main(String[] args) {\n" +
                "        List l = new List();\n" +
                "        Map e= null;\n" +
                "    }\n" +
                "}");
        ICompilationUnit cu = pack1.createCompilationUnit("C.java", buf.toString(), false, null);

        String[] order = new String[] { "java", "java.util", "com", "pack" };

        ImportRewrite imports= newImportsRewrite(cu, order, 2, 2, false);
        imports.setUseContextToFilterImplicitImports(true);
        imports.addImport("java.awt.List");
        imports.addImport("java.io.Serializable");
        imports.addImport("java.io.PrintWriter");
        imports.addImport("java.util.HashMap");

        apply(imports);

        buf = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
        buf.append( //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
                "package pack1;\n" +
                "\n" +
                "// comment 1\n" +
                "/*\n" +
                " * keep me with List\n" +
                " *\n" +
                " */\n" +
                "\n" +
                "// lead 1\n" +
                "import java.awt.List;// test1\n" +
                "\n" +
                "/*\n" +
                " * keep me with Serializable\n" +
                " */\n" +
                "\n" +
                "// lead 2\n" +
                "// test2\n" +
                "/*\n" +
                " * keep me with Serializable 2\n" +
                " */\n" +
                "// lead 3\n" +
                "// test3\n" +
                "/*\n" +
                " * keep me with PrintWriter\n" +
                " */\n" +
                "import java.io.*;\n" +
                "\n" +
                "/*\n" +
                " * don't move me\n" +
                " */\n" +
                "\n" +
                "//lead 4\n" +
                "import java.util.HashMap;// test4\n" +
                "// commen 3\n" +
                "\n" +
                "public class C implements Serializable{\n" +
                "    public static void main(String[] args) {\n" +
                "        List l = new List();\n" +
                "        Map e= null;\n" +
                "    }\n" +
                "}");
        assertEqualString(cu.getSource(), buf.toString());
    }

    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=378024
    // Re-ordering imports and converting them to *
    public void testBug378024g() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
        IPackageFragment pack1 = this.sourceFolder.createPackageFragment("pack1", false, null);
        StringBuilder buf = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
        buf.append( //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
                "package pack1;\n" +
                "\n" +
                "// comment 1\n" +
                "/*\n" +
                " * don't move me 1\n" +
                " *\n" +
                " */\n" +
                "\n" +
                "// lead 1\n" +
                "import java.awt.List;// test1\n" +
                "\n" +
                "/*\n" +
                " * don't move me 2\n" +
                " */\n" +
                "\n" +
                "// lead 2\n" +
                "import java.io.Serializable;// test2\n" +
                "/*\n" +
                " * don't move me 3\n" +
                " */\n" +
                "/*\n" +
                " * don't move me 4\n" +
                " */\n" +
                "\n" +
                "//lead 3\n" +
                "import java.util.HashMap;// test3\n" +
                "// commen 3\n" +
                "\n" +
                "public class C implements Serializable{\n" +
                "    public static void main(String[] args) {\n" +
                "        List l = new List();\n" +
                "        Map e= null;\n" +
                "    }\n" +
                "}");
        ICompilationUnit cu = pack1.createCompilationUnit("C.java", buf.toString(), false, null);

        String[] order = new String[] { "java", "java.awt", "java.util", "java.io", "com", "pack" };

        ImportRewrite imports= newImportsRewrite(cu, order, 1, 1, false);
        imports.setUseContextToFilterImplicitImports(true);
        imports.addImport("java.awt.List");
        imports.addImport("java.io.Serializable");
        imports.addImport("java.util.HashMap");

        apply(imports);

        StringBuilder buf2 = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
        buf2.append( //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
                "package pack1;\n" +
                "\n" +
                "// comment 1\n" +
                "/*\n" +
                " * don't move me 1\n" +
                " *\n" +
                " */\n" +
                "\n" +
                "// lead 1\n" +
                "// test1\n" +
                "import java.awt.*;\n" +
                "\n" +
                "//lead 3\n" +
                "// test3\n" +
                "// commen 3\n" +
                "import java.util.*;\n" +
                "\n" +
                "/*\n" +
                " * don't move me 2\n" +
                " */\n" +
                "\n" +
                "// lead 2\n" +
                "// test2\n" +
                "/*\n" +
                " * don't move me 3\n" +
                " */\n" +
                "/*\n" +
                " * don't move me 4\n" +
                " */\n" +
                "import java.io.*;\n" +
                "\n" +
                "public class C implements Serializable{\n" +
                "    public static void main(String[] args) {\n" +
                "        List l = new List();\n" +
                "        Map e= null;\n" +
                "    }\n" +
                "}");
        assertEqualString(cu.getSource(), buf2.toString());
    }

    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=378024
    // Preserve comments when imports are removed in case the restoring of imports is enabled
    // This will test changes in org.eclipse.jdt.internal.core.dom.rewrite.ImportRewriteAnalyzer.removeImport(String, boolean)
    public void testBug378024h() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
        IPackageFragment pack1 = this.sourceFolder.createPackageFragment("pack1", false, null);
        StringBuilder buf = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
        buf.append( //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
                "package pack1;\n" +
                "\n" +
                "// comment 1\n" +
                "/*\n" +
                " * don't move me 1\n" +
                " *\n" +
                " */\n" +
                "\n" +
                "// lead 1\n" +
                "import java.awt.List;// test1\n" +
                "\n" +
                "/*\n" +
                " * don't move me 2\n" +
                " */\n" +
                "\n" +
                "// lead 2\n" +
                "import java.io.Serializable;// test2\n" +
                "/*\n" +
                " * don't move me 3\n" +
                " */\n" +
                "\n" +
                "/*\n" +
                " * don't move me 4\n" +
                " */\n" +
                "\n" +
                "//lead 3\n" +
                "import java.util.HashMap;// test3\n" +
                "// commen 3\n" +
                "\n" +
                "public class C implements Serializable{\n" +
                "    public static void main(String[] args) {\n" +
                "        Map e= null;\n" +
                "    }\n" +
                "}");
        ICompilationUnit cu = pack1.createCompilationUnit("C.java", buf.toString(), false, null);

        String[] order = new String[] { "java", "java.util", "com", "pack" };

        ImportRewrite imports= newImportsRewrite(cu, order, 99, 99, true);
        imports.setUseContextToFilterImplicitImports(true);
        imports.removeImport("java.awt.List");

        apply(imports);

        buf = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
        buf.append( //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
                "package pack1;\n" +
                "\n" +
                "/*\n" +
                " * don't move me 2\n" +
                " */\n" +
                "\n" +
                "// lead 2\n" +
                "import java.io.Serializable;// test2\n" +
                "/*\n" +
                " * don't move me 3\n" +
                " */\n" +
                "\n" +
                "/*\n" +
                " * don't move me 4\n" +
                " */\n" +
                "\n" +
                "//lead 3\n" +
                "import java.util.HashMap;// test3\n" +
                "// commen 3\n" +
                "\n" +
                "public class C implements Serializable{\n" +
                "    public static void main(String[] args) {\n" +
                "        Map e= null;\n" +
                "    }\n" +
                "}");
        assertEqualString(cu.getSource(), buf.toString());
    }

    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=378024
    // Preserve comments when imports are removed in case the restoring of imports is enabled
    public void testBug378024h_1() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
        IPackageFragment pack1 = this.sourceFolder.createPackageFragment("pack1", false, null);
        StringBuilder buf = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
        buf.append( //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
                "package pack1;\n" +
                "\n" +
                "// comment 1\n" +
                "/*\n" +
                " * don't move me 1\n" +
                " *\n" +
                " */\n" +
                "// lead 1\n" +
                "import java.awt.List;// test1\n" +
                "/* i am with List */\n" +
                "\n" +
                "/*\n" +
                " * don't move me 2\n" +
                " */\n" +
                "\n" +
                "// lead 2\n" +
                "import java.io.Serializable;// test2\n" +
                "/*\n" +
                " * don't move me 3\n" +
                " */\n" +
                "\n" +
                "/*\n" +
                " * don't move me 4\n" +
                " */\n" +
                "\n" +
                "//lead 3\n" +
                "import java.util.HashMap;// test3\n" +
                "// commen 3\n" +
                "\n" +
                "public class C implements Serializable{\n" +
                "    public static void main(String[] args) {\n" +
                "        Map e= null;\n" +
                "    }\n" +
                "}");
        ICompilationUnit cu = pack1.createCompilationUnit("C.java", buf.toString(), false, null);

        String[] order = new String[] { "java", "java.util", "com", "pack" };

        ImportRewrite imports= newImportsRewrite(cu, order, 99, 99, true);
        imports.setUseContextToFilterImplicitImports(true);
        imports.removeImport("java.awt.List");
        imports.addImport("java.util.List");

        apply(imports);

        buf = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
        buf.append( //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
                "package pack1;\n" +
                "\n" +
                "/*\n" +
                " * don't move me 2\n" +
                " */\n" +
                "\n" +
                "// lead 2\n" +
                "import java.io.Serializable;// test2\n" +
                "/*\n" +
                " * don't move me 3\n" +
                " */\n" +
                "\n" +
                "/*\n" +
                " * don't move me 4\n" +
                " */\n" +
                "\n" +
                "//lead 3\n" +
                "import java.util.HashMap;// test3\n" +
                "// commen 3\n" +
                "import java.util.List;\n" +
                "\n" +
                "public class C implements Serializable{\n" +
                "    public static void main(String[] args) {\n" +
                "        Map e= null;\n" +
                "    }\n" +
                "}");
        assertEqualString(cu.getSource(), buf.toString());
    }

    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=378024
    // Preserve comments when imports are unfolded.
    public void testBug378024i() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
        IPackageFragment pack1 = this.sourceFolder.createPackageFragment("pack1", false, null);
        StringBuilder buf = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
        buf.append( //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
                "package pack1;\n" +
                "\n" +
                "// comment 1\n" +
                "/*\n" +
                " * don't move me 1\n" +
                " *\n" +
                " */\n" +
                "// lead 1\n" +
                "import java.awt.*;// test1\n" +
                "/* i am with List */\n" +
                "\n" +
                "/*\n" +
                " * don't move me 2\n" +
                " */\n" +
                "\n" +
                "// lead 2\n" +
                "import java.io.*;// test2\n" +
                "/*\n" +
                " * don't move me 3\n" +
                " */\n" +
                "\n" +
                "/*\n" +
                " * don't move me 4\n" +
                " */\n" +
                "\n" +
                "//lead 3\n" +
                "import java.util.*;// test3\n" +
                "// commen 3\n" +
                "\n" +
                "public class C implements Serializable{\n" +
                "    public static void main(String[] args) {\n" +
                "        HashMap e= null;\n" +
                "        PrintWriter p= null;\n" +
                "        List l= null;\n" +
                "    }\n" +
                "}");
        ICompilationUnit cu = pack1.createCompilationUnit("C.java", buf.toString(), false, null);

        String[] order = new String[] { "java", "com", "pack" };

        ImportRewrite imports= newImportsRewrite(cu, order, 99, 99, false);
        imports.setUseContextToFilterImplicitImports(true);
        imports.addImport("java.awt.List");
        imports.addImport("java.io.PrintWriter");
        imports.addImport("java.io.Serializable");
        imports.addImport("java.util.HashMap");
        imports.addImport("java.util.Map");
        apply(imports);

        buf = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
        buf.append( //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
                "package pack1;\n" +
                "\n" +
                "// comment 1\n" +
                "/*\n" +
                " * don't move me 1\n" +
                " *\n" +
                " */\n" +
                "// lead 1\n" +
                "// test1\n" +
                "/* i am with List */\n" +
                "import java.awt.List;\n" +
                "\n" +
                "/*\n" +
                " * don't move me 2\n" +
                " */\n" +
                "\n" +
                "// lead 2\n" +
                "// test2\n" +
                "/*\n" +
                " * don't move me 3\n" +
                " */\n" +
                "import java.io.PrintWriter;\n" +
                "import java.io.Serializable;\n" +
                "\n" +
                "/*\n" +
                " * don't move me 4\n" +
                " */\n" +
                "\n" +
                "//lead 3\n" +
                "// test3\n" +
                "// commen 3\n" +
                "import java.util.HashMap;\n" +
                "import java.util.Map;\n" +
                "\n" +
                "public class C implements Serializable{\n" +
                "    public static void main(String[] args) {\n" +
                "        HashMap e= null;\n" +
                "        PrintWriter p= null;\n" +
                "        List l= null;\n" +
                "    }\n" +
                "}");
        assertEqualString(cu.getSource(), buf.toString());
    }

    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=378024
    // Preserve comments when imports are folded but a member type import is present
    public void testBug378024j() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
        IPackageFragment pack1 = this.sourceFolder.createPackageFragment("pack1", false, null);
        StringBuilder buf = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
        buf.append( //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
                "package pack1;\n" +
                "\n" +
                "// comment 1\n" +
                "/*\n" +
                " * don't move me 1\n" +
                " *\n" +
                " */\n" +
                "// lead 1\n" +
                "import java.awt.List;// test1\n" +
                "/* i am with List */\n" +
                "\n" +
                "/*\n" +
                " * don't move me 2\n" +
                " */\n" +
                "\n" +
                "//lead 3\n" +
                "import java.util.HashMap;// test3\n" +
                "/*\n" +
                " * don't move me 3\n" +
                " */\n" +
                "\n" +
                "/*keep me with Map.Entry*/\n" +
                "import java.util.Map.Entry;// member type import\n" +
                "/*keep me with Map.Entry 2*/\n" +
                "\n" +
                "/*\n" +
                " * don't move me 4\n" +
                " */\n" +
                "\n" +
                "// lead 2\n" +
                "import java.io.Serializable;// test2\n" +
                "// commen 3\n" +
                "\n" +
                "public class C implements Serializable{\n" +
                "    public static void main(String[] args) {\n" +
                "        Map e= null;\n" +
                "    }\n" +
                "}");
        ICompilationUnit cu = pack1.createCompilationUnit("C.java", buf.toString(), false, null);

        String[] order = new String[] { "java", "java.util", "com", "pack" };

        ImportRewrite imports= newImportsRewrite(cu, order, 1, 1, false);
        imports.setUseContextToFilterImplicitImports(true);
        imports.addImport("java.awt.List");
        imports.addImport("java.util.HashMap");
        imports.addImport("java.util.Map.Entry");
        imports.addImport("java.io.Serializable");

        apply(imports);

        buf = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
        buf.append( //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
                "package pack1;\n" +
                "\n" +
                "// comment 1\n" +
                "/*\n" +
                " * don't move me 1\n" +
                " *\n" +
                " */\n" +
                "// lead 1\n" +
                "// test1\n" +
                "/* i am with List */\n" +
                "import java.awt.*;\n" +
                "\n" +
                "/*\n" +
                " * don't move me 4\n" +
                " */\n" +
                "\n" +
                "// lead 2\n" +
                "// test2\n" +
                "// commen 3\n" +
                "import java.io.*;\n" +
                "\n" +
                "/*\n" +
                " * don't move me 2\n" +
                " */\n" +
                "\n" +
                "//lead 3\n" +
                "// test3\n" +
                "/*\n" +
                " * don't move me 3\n" +
                " */\n" +
                "import java.util.*;\n" +
                "/*keep me with Map.Entry*/\n" +
                "// member type import\n" +
                "/*keep me with Map.Entry 2*/\n" +
                "import java.util.Map.*;\n" +
                "\n" +
                "public class C implements Serializable{\n" +
                "    public static void main(String[] args) {\n" +
                "        Map e= null;\n" +
                "    }\n" +
                "}");
        assertEqualString(cu.getSource(), buf.toString());
    }

	public void testBug430108_001() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		IPackageFragment pack1 = this.sourceFolder.createPackageFragment("pack1", false, null);
		String contents = "package pack1;\n" +
				"public class X {\n" +
				"}\n";
		ICompilationUnit cu = pack1.createCompilationUnit("X.java", contents, false, null);

		ASTParser parser = ASTParser.newParser(AST_INTERNAL_LATEST);
		parser.setSource(cu);
		parser.setResolveBindings(true);
		parser.setStatementsRecovery(true);
		CompilationUnit astRoot = (CompilationUnit) parser.createAST(null);
		TypeDeclaration typeDeclaration = (TypeDeclaration) astRoot.types().get(0);
		ITypeBinding typeBinding = typeDeclaration.resolveBinding();
		contents = "package pack2;\n" +
				"public class X {\n" +
				"}\n";
		IPackageFragment pack2 = this.sourceFolder.createPackageFragment("pack2", false, null);
		cu = pack2.createCompilationUnit("X.java", contents, false, null);
		ImportRewrite rewrite = newImportsRewrite(cu, new String[0], 99, 99, true);
		rewrite.setUseContextToFilterImplicitImports(true);
		Type actualType = rewrite.addImport(typeBinding, astRoot.getAST());
		assertEquals("pack1.X", actualType.toString());
	}

	public void testBug430108_002() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		IPackageFragment pack1 = this.sourceFolder.createPackageFragment("pack1", false, null);
		String contents = "package pack1;\n" +
				"public class X {\n" +
				"}\n";
		ICompilationUnit cu = pack1.createCompilationUnit("X.java", contents, false, null);

		ASTParser parser = ASTParser.newParser(AST_INTERNAL_LATEST);
		parser.setSource(cu);
		parser.setResolveBindings(true);
		parser.setStatementsRecovery(true);
		CompilationUnit astRoot = (CompilationUnit) parser.createAST(null);
		TypeDeclaration typeDeclaration = (TypeDeclaration) astRoot.types().get(0);
		ITypeBinding typeBinding = typeDeclaration.resolveBinding();
		contents = "package pack2;\n" +
				"public class X {\n" +
				"}\n";
		IPackageFragment pack2 = this.sourceFolder.createPackageFragment("pack2", false, null);
		parser.setSource(pack2.createCompilationUnit("X.java", contents, false, null));
		CompilationUnit astRoot2 = (CompilationUnit) parser.createAST(null);
		ImportRewrite rewrite = ImportRewrite.create(astRoot2, true);
		rewrite.setUseContextToFilterImplicitImports(true);
		Type actualType = rewrite.addImport(typeBinding, astRoot2.getAST());
		assertEquals("pack1.X", actualType.toString());
	}

	private ICompilationUnit createCompilationUnit(String packageName, String className) throws JavaModelException {
		StringBuilder contents = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		contents.append("package " + packageName + ";\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		contents.append("public class " + className + " {}"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		return createCompilationUnit(packageName, className, contents.toString());
	}

	private ICompilationUnit createCompilationUnit(
			String packageName, String className, String contents) throws JavaModelException {
		IPackageFragment pack1 = this.sourceFolder.createPackageFragment(packageName, false, null);
		ICompilationUnit cu = pack1.createCompilationUnit(className + ".java", contents, /* force */ true, null); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		return cu;
	}

	private void assertAddedAndRemoved(ImportRewrite imports, String[] expectedAdded, String[] expectedRemoved, String[] expectedAddedStatic, String[] expectedRemovedStatic) {
		assertEqualStringsIgnoreOrder(imports.getAddedImports(), expectedAdded);
		assertEqualStringsIgnoreOrder(imports.getAddedStaticImports(), expectedAddedStatic);
		assertEqualStringsIgnoreOrder(imports.getRemovedImports(), expectedRemoved);
		assertEqualStringsIgnoreOrder(imports.getRemovedStaticImports(), expectedRemovedStatic);
	}

	private void assertEqualString(String actual, String expected) {
		StringAsserts.assertEqualString(actual, expected);
	}

	private void assertEqualStringsIgnoreOrder(String[] actual, String[] expecteds) {
		StringAsserts.assertEqualStringsIgnoreOrder(actual, expecteds);
	}

	private void assertEqualStringIgnoreDelim(String actual, String expected) throws IOException {
		StringAsserts.assertEqualStringIgnoreDelim(actual, expected);
	}

	private ImportRewrite newImportsRewrite(ICompilationUnit cu, String[] order, int normalThreshold, int staticThreshold, boolean restoreExistingImports) throws CoreException, BackingStoreException {
		ImportRewrite rewrite= ImportRewrite.create(cu, restoreExistingImports);
		rewrite.setImportOrder(order);
		rewrite.setOnDemandImportThreshold(normalThreshold);
		rewrite.setStaticOnDemandImportThreshold(staticThreshold);
		return rewrite;
	}

	protected ImportRewrite newImportsRewrite(CompilationUnit cu, String[] order, int normalThreshold, int staticThreshold, boolean restoreExistingImports) {
		ImportRewrite rewrite= ImportRewrite.create(cu, restoreExistingImports);
		rewrite.setImportOrder(order);
		rewrite.setOnDemandImportThreshold(normalThreshold);
		rewrite.setStaticOnDemandImportThreshold(staticThreshold);
		return rewrite;
	}

	private void apply(ImportRewrite rewrite) throws CoreException, MalformedTreeException, BadLocationException {
		TextEdit edit= rewrite.rewriteImports(null);

		// not the efficient way!
		ICompilationUnit compilationUnit= rewrite.getCompilationUnit();
		Document document= new Document(compilationUnit.getSource());
		edit.apply(document);
		compilationUnit.getBuffer().setContents(document.get());
	}

}
