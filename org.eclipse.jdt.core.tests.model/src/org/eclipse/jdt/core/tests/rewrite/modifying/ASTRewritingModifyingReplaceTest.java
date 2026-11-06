/*******************************************************************************
 * Copyright (c) 2000, 2025 IBM Corporation and others.
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

import java.util.List;
import junit.framework.Test;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.dom.*;

@SuppressWarnings({"rawtypes", "unchecked"})
public class ASTRewritingModifyingReplaceTest extends ASTRewritingModifyingTest {
	private static final Class THIS = ASTRewritingModifyingReplaceTest.class;

	public ASTRewritingModifyingReplaceTest(String name) {
		super(name);
	}

	public static Test allTests() {
		return new Suite(THIS);
	}

	public static Test suite() {
		return allTests();
	}

	public void test0001() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test0001", false, null);
		StringBuilder buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("package test0001;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("public class X {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		ICompilationUnit cu= pack1.createCompilationUnit("X.java", buf.toString(), false, null);

		CompilationUnit astRoot= createCU(cu, false);

		astRoot.recordModifications();

		Name name = astRoot.getAST().newSimpleName("aaa");
		astRoot.getPackage().setName(name);

		String preview = evaluateRewrite(cu, astRoot);

		buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("package aaa;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("public class X {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		assertEqualString(preview, buf.toString());
	}

	public void test0002() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test0002", false, null);
		StringBuilder buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("package test0002;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("public class X {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		ICompilationUnit cu= pack1.createCompilationUnit("X.java", buf.toString(), false, null);

		CompilationUnit astRoot= createCU(cu, false);

		astRoot.recordModifications();

		AST a = astRoot.getAST();

		Name name = a.newSimpleName("aaa");
		PackageDeclaration pack = a.newPackageDeclaration();
		pack.setName(name);
		astRoot.setPackage(pack);

		String preview = evaluateRewrite(cu, astRoot);

		buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("package aaa;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("public class X {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		assertEqualString(preview, buf.toString());
	}

	public void test0003() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test0003", false, null);
		StringBuilder buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("package test0003;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import java.util.*;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import java.lang.*;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import java.awt.*;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("public class X {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		ICompilationUnit cu= pack1.createCompilationUnit("X.java", buf.toString(), false, null);

		CompilationUnit astRoot= createCU(cu, false);

		astRoot.recordModifications();

		Name name = astRoot.getAST().newSimpleName("aaa");
		List imports = astRoot.imports();
		ImportDeclaration imp = (ImportDeclaration)imports.get(0);
		imp.setName(name);

		String preview = evaluateRewrite(cu, astRoot);

		buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("package test0003;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import aaa.*;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import java.lang.*;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import java.awt.*;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("public class X {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		assertEqualString(preview, buf.toString());
	}

	public void test0004() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test0004", false, null);
		StringBuilder buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("package test0004;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import java.util.*;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import java.lang.*;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import java.awt.*;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("public class X {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		ICompilationUnit cu= pack1.createCompilationUnit("X.java", buf.toString(), false, null);

		CompilationUnit astRoot= createCU(cu, false);

		astRoot.recordModifications();

		List types = astRoot.types();
		SimpleName name = astRoot.getAST().newSimpleName("AAA");
		TypeDeclaration t = (TypeDeclaration)types.get(0);
		t.setName(name);

		String preview = evaluateRewrite(cu, astRoot);

		buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("package test0004;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import java.util.*;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import java.lang.*;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("import java.awt.*;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("public class AAA {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		assertEqualString(preview, buf.toString());
	}

	public void test0005() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test0005", false, null);
		StringBuilder buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("package test0005;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("public class X {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("class Y {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("class Z {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		ICompilationUnit cu= pack1.createCompilationUnit("X.java", buf.toString(), false, null);

		CompilationUnit astRoot= createCU(cu, false);

		astRoot.recordModifications();

		AST a = astRoot.getAST();

		List types = astRoot.types();
		TypeDeclaration typeDeclaration = a.newTypeDeclaration();
		SimpleName name = a.newSimpleName("AAA");
		typeDeclaration.setName(name);
		types.set(1, typeDeclaration);

		String preview = evaluateRewrite(cu, astRoot);

		buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("package test0005;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("public class X {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("class AAA {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("class Z {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		assertEqualString(preview, buf.toString());
	}

	public void test0006() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test0006", false, null);
		StringBuilder buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("package test0006;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("public class X {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("class Y {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("class Z {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		ICompilationUnit cu= pack1.createCompilationUnit("X.java", buf.toString(), false, null);

		CompilationUnit astRoot= createCU(cu, false);

		astRoot.recordModifications();

		AST a = astRoot.getAST();

		List types = astRoot.types();
		TypeDeclaration typeDeclaration1 = a.newTypeDeclaration();
		typeDeclaration1.setName(a.newSimpleName("A"));
		types.set(1, typeDeclaration1);

		String preview = evaluateRewrite(cu, astRoot);

		buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("package test0006;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("public class X {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("class A {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("class Z {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		assertEqualString(preview, buf.toString());
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=192233
	public void test0007() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test0007", false, null);
		StringBuilder buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("package test0007;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("public class X {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("    List/**/getUsers() {}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL

		ICompilationUnit cu= pack1.createCompilationUnit("X.java", buf.toString(), false, null);

		CompilationUnit astRoot= createCU(cu, false);

		astRoot.recordModifications();

		AST ast = astRoot.getAST();

		List types = astRoot.types();
		List list= ((TypeDeclaration) types.get(0)).bodyDeclarations();
		MethodDeclaration methodDecl= (MethodDeclaration) list.get(0);

		methodDecl.setReturnType2(ast.newPrimitiveType(PrimitiveType.VOID));

		String preview = evaluateRewrite(cu, astRoot);

		buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("package test0007;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("public class X {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("    void getUsers() {}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		assertEqualString(preview, buf.toString());
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=192233
	public void test0008() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test0008", false, null);
		StringBuilder buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("package test0008;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("public class X {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("    List /**/getUsers() {}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL

		ICompilationUnit cu= pack1.createCompilationUnit("X.java", buf.toString(), false, null);

		CompilationUnit astRoot= createCU(cu, false);

		astRoot.recordModifications();

		AST ast = astRoot.getAST();

		List types = astRoot.types();
		List list= ((TypeDeclaration) types.get(0)).bodyDeclarations();
		MethodDeclaration methodDecl= (MethodDeclaration) list.get(0);

		methodDecl.setReturnType2(ast.newPrimitiveType(PrimitiveType.VOID));

		String preview = evaluateRewrite(cu, astRoot);

		buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("package test0008;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("public class X {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("    void getUsers() {}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		assertEqualString(preview, buf.toString());
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=192233
	public void test0009() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test0009", false, null);
		StringBuilder buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("package test0009;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("public class X {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("    List/**/ getUsers() {}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL

		ICompilationUnit cu= pack1.createCompilationUnit("X.java", buf.toString(), false, null);

		CompilationUnit astRoot= createCU(cu, false);

		astRoot.recordModifications();

		AST ast = astRoot.getAST();

		List types = astRoot.types();
		List list= ((TypeDeclaration) types.get(0)).bodyDeclarations();
		MethodDeclaration methodDecl= (MethodDeclaration) list.get(0);

		methodDecl.setReturnType2(ast.newPrimitiveType(PrimitiveType.VOID));

		String preview = evaluateRewrite(cu, astRoot);

		buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("package test0009;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("public class X {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("    void getUsers() {}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		assertEqualString(preview, buf.toString());
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=192233
	public void test0010() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test0010", false, null);
		StringBuilder buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("package test0010;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("public class X {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("    void getUsers(List/**/list) {}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL

		ICompilationUnit cu= pack1.createCompilationUnit("X.java", buf.toString(), false, null);

		CompilationUnit astRoot= createCU(cu, false);

		astRoot.recordModifications();

		AST ast = astRoot.getAST();

		List types = astRoot.types();
		List list= ((TypeDeclaration) types.get(0)).bodyDeclarations();
		MethodDeclaration methodDecl= (MethodDeclaration) list.get(0);
		List parameters = methodDecl.parameters();
		SingleVariableDeclaration variableDeclaration = (SingleVariableDeclaration) parameters.get(0);
		variableDeclaration.setType(ast.newPrimitiveType(PrimitiveType.INT));

		String preview = evaluateRewrite(cu, astRoot);

		buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("package test0010;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("public class X {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("    void getUsers(int list) {}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		assertEqualString(preview, buf.toString());
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=192233
	public void test0011() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test0011", false, null);
		StringBuilder buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("package test0011;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("public class X {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("    void getUsers(int i, List/**/list) {}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL

		ICompilationUnit cu= pack1.createCompilationUnit("X.java", buf.toString(), false, null);

		CompilationUnit astRoot= createCU(cu, false);

		astRoot.recordModifications();

		AST ast = astRoot.getAST();

		List types = astRoot.types();
		List list= ((TypeDeclaration) types.get(0)).bodyDeclarations();
		MethodDeclaration methodDecl= (MethodDeclaration) list.get(0);
		List parameters = methodDecl.parameters();
		SingleVariableDeclaration variableDeclaration = (SingleVariableDeclaration) parameters.get(1);
		variableDeclaration.setType(ast.newPrimitiveType(PrimitiveType.INT));

		String preview = evaluateRewrite(cu, astRoot);

		buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("package test0011;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("public class X {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("    void getUsers(int i, int list) {}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		assertEqualString(preview, buf.toString());
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=192233
	public void test0012() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test0012", false, null);
		StringBuilder buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("package test0012;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("public class X {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("    void getUsers(int i, List.../**/list) {}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL

		ICompilationUnit cu= pack1.createCompilationUnit("X.java", buf.toString(), false, null);

		CompilationUnit astRoot= createCU(cu, false);

		astRoot.recordModifications();

		AST ast = astRoot.getAST();

		List types = astRoot.types();
		List list= ((TypeDeclaration) types.get(0)).bodyDeclarations();
		MethodDeclaration methodDecl= (MethodDeclaration) list.get(0);
		List parameters = methodDecl.parameters();
		SingleVariableDeclaration variableDeclaration = (SingleVariableDeclaration) parameters.get(1);
		variableDeclaration.setType(ast.newPrimitiveType(PrimitiveType.INT));

		String preview = evaluateRewrite(cu, astRoot);

		buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("package test0012;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("public class X {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("    void getUsers(int i, int.../**/list) {}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		assertEqualString(preview, buf.toString());
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=192233
	public void test0013() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test0013", false, null);
		StringBuilder buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("package test0013;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("public class X {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("    List/**/list;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL

		ICompilationUnit cu= pack1.createCompilationUnit("X.java", buf.toString(), false, null);

		CompilationUnit astRoot= createCU(cu, false);

		astRoot.recordModifications();

		AST ast = astRoot.getAST();

		List types = astRoot.types();
		List list= ((TypeDeclaration) types.get(0)).bodyDeclarations();
		FieldDeclaration fieldDeclaration= (FieldDeclaration) list.get(0);
		fieldDeclaration.setType(ast.newPrimitiveType(PrimitiveType.INT));

		String preview = evaluateRewrite(cu, astRoot);

		buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("package test0013;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("public class X {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("    int list;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		assertEqualString(preview, buf.toString());
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=331138
	// Make sure comments between removed and replaced node are not removed.
	public void test0014a() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test0014a", false, null);
		StringBuilder buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("package test0014;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("public class X {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("	public void m() {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("    	String abc;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("		// do not delete this\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("		/* do not delete this\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("		 */\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("     	abc = \"\";\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("	}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		ICompilationUnit cu= pack1.createCompilationUnit("X.java", buf.toString(), false, null);

		CompilationUnit astRoot= createCU(cu, false);

		astRoot.recordModifications();

		List types = astRoot.types();
		TypeDeclaration typeDeclaration = (TypeDeclaration)types.get(0);
		MethodDeclaration methodDeclaration = typeDeclaration.getMethods()[0];
		Block body = methodDeclaration.getBody();
		List statements = body.statements();
		VariableDeclarationStatement varDeclaration = (VariableDeclarationStatement) statements.get(0);
		statements.remove(0);

		statements.set(0, varDeclaration);
		String preview = evaluateRewrite(cu, astRoot);

		buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("package test0014;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("public class X {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("	public void m() {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("    	\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("		// do not delete this\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("		/* do not delete this\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("		 */\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("     	String abc;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("	}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		assertEqualString(preview, buf.toString());
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=331138
	// Make sure comments between removed and replaced node are not removed.
	public void test0014b() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test0014b", false, null);
		StringBuilder buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("package test0014;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("public class X {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("	public void m() {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("    	String abc;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("		// do not delete this\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("		/* do not delete this\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("		 */\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("		// do not delete this\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("     	abc = \"\";\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("	}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		ICompilationUnit cu= pack1.createCompilationUnit("X.java", buf.toString(), false, null);

		CompilationUnit astRoot= createCU(cu, false);

		astRoot.recordModifications();

		List types = astRoot.types();
		TypeDeclaration typeDeclaration = (TypeDeclaration)types.get(0);
		MethodDeclaration methodDeclaration = typeDeclaration.getMethods()[0];
		Block body = methodDeclaration.getBody();
		List statements = body.statements();
		VariableDeclarationStatement varDeclaration = (VariableDeclarationStatement) statements.get(0);
		statements.remove(0);

		statements.set(0, varDeclaration);
		String preview = evaluateRewrite(cu, astRoot);

		buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("package test0014;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("public class X {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("	public void m() {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("    	\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("		// do not delete this\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("		/* do not delete this\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("		 */\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("		String abc;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("	}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		assertEqualString(preview, buf.toString());
	}
}
