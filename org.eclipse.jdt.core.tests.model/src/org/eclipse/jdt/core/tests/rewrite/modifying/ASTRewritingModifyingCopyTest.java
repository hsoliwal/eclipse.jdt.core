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
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.tests.util.Util;

@SuppressWarnings({"rawtypes", "unchecked"})
public class ASTRewritingModifyingCopyTest extends ASTRewritingModifyingTest {
	private static final Class THIS = ASTRewritingModifyingCopyTest.class;

	public ASTRewritingModifyingCopyTest(String name) {
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
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("public class X {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("class Y /**/ {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
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
		TypeDeclaration typeDeclaration = (TypeDeclaration)types.get(1);
		TypeDeclaration typeDeclaration2 = (TypeDeclaration)ASTNode.copySubtree(a, typeDeclaration);
		types.add(typeDeclaration2);

		String preview = evaluateRewrite(cu, astRoot);

		buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("package test0001;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("public class X {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("class Y /**/ {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("class Z {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("class Y /**/ {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		assertEqualString(preview, buf.toString());
	}

	/*
	 * https://bugs.eclipse.org/405699 : modify a copied node.
	 */
	public void test0002() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test0002", false, null);
		StringBuilder buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("package test0002;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("public class X {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("class Y /**/ {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("    //pre\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("    int i; //post\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("class Z {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		ICompilationUnit cu= pack1.createCompilationUnit("X.java", buf.toString(), false, null);

		CompilationUnit astRoot= createCU(cu, false);

		astRoot.recordModifications();

		AST a = astRoot.getAST();

		List types = astRoot.types();
		TypeDeclaration typeDeclaration = (TypeDeclaration)types.get(1);
		TypeDeclaration typeDeclaration2 = (TypeDeclaration)ASTNode.copySubtree(a, typeDeclaration);
		typeDeclaration2.setName(a.newSimpleName("A"));
		types.add(typeDeclaration2);

		String preview = evaluateRewrite(cu, astRoot);

		buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("package test0002;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("public class X {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("class Y /**/ {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("    //pre\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("    int i; //post\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("class Z {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("class A {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
//		buf.append("    //pre\n"); // we can't preserve everything, but we do our best...
//		buf.append("\n");
		buf.append("    int i; //post\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		assertEqualString(preview, buf.toString());
	}

	public void test0003() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test0003", false, null);
		StringBuilder buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("package test0003;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("public class X extends Z1\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("                        .Z2\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("                            .Z3 {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("class Y {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		ICompilationUnit cu= pack1.createCompilationUnit("X.java", buf.toString(), false, null);

		CompilationUnit astRoot= createCU(cu, false);

		astRoot.recordModifications();

		AST a = astRoot.getAST();

		List types = astRoot.types();
		TypeDeclaration typeDeclaration1 = (TypeDeclaration)types.get(0);
		TypeDeclaration typeDeclaration2 = (TypeDeclaration)types.get(1);
		Type name = typeDeclaration1.getSuperclassType();
		Type name2 = (Type)ASTNode.copySubtree(a, name);
		typeDeclaration2.setSuperclassType(name2);

		String preview = evaluateRewrite(cu, astRoot);

		buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("package test0003;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("public class X extends Z1\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("                        .Z2\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("                            .Z3 {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("class Y extends Z1\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("                        .Z2\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("                            .Z3 {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		assertEqualString(preview, buf.toString());
	}

	public void test0004() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test0004", false, null);
		StringBuilder buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("package test0004;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("public class X extends Z1\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("                        .Z2\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("                            .Z3 {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("class Y {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		ICompilationUnit cu= pack1.createCompilationUnit("X.java", buf.toString(), false, null);

		CompilationUnit astRoot= createCU(cu, false);

		astRoot.recordModifications();

		AST a = astRoot.getAST();

		List types = astRoot.types();
		TypeDeclaration typeDeclaration1 = (TypeDeclaration)types.get(0);
		TypeDeclaration typeDeclaration2 = (TypeDeclaration)types.get(1);
		Type type = typeDeclaration1.getSuperclassType();
		SimpleType type2 = (SimpleType)ASTNode.copySubtree(a, type);
		QualifiedName name3 = (QualifiedName)((QualifiedName)type2.getName()).getQualifier();
		((QualifiedName)type2.getName()).setQualifier(a.newSimpleName("A"));
		typeDeclaration2.setSuperclassType(a.newSimpleType(name3));

		String preview = evaluateRewrite(cu, astRoot);

		buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("package test0004;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("public class X extends Z1\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("                        .Z2\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("                            .Z3 {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("class Y extends Z1\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("                        .Z2 {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		assertEqualString(Util.convertToIndependantLineDelimiter(preview), Util.convertToIndependantLineDelimiter(buf.toString()));
	}

	public void test0005() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test0005", false, null);
		StringBuilder buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("package test0005;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("public class X extends Z1\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("                        .Z2\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("                            .Z3 {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("class Y {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		ICompilationUnit cu= pack1.createCompilationUnit("X.java", buf.toString(), false, null);

		CompilationUnit astRoot= createCU(cu, false);

		astRoot.recordModifications();

		AST a = astRoot.getAST();

		List types = astRoot.types();
		TypeDeclaration typeDeclaration1 = (TypeDeclaration)types.get(0);
		TypeDeclaration typeDeclaration2 = (TypeDeclaration)types.get(1);
		Type name = typeDeclaration1.getSuperclassType();
		SimpleType name2 = (SimpleType)ASTNode.copySubtree(a, name);
		QualifiedName name3 = (QualifiedName) ((QualifiedName)name2.getName()).getQualifier();
		((QualifiedName)name2.getName()).setQualifier(a.newSimpleName("A"));
		name3.getName().setIdentifier("B");
		typeDeclaration2.setSuperclassType(a.newSimpleType(name3));

		String preview = evaluateRewrite(cu, astRoot);

		buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("package test0005;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("public class X extends Z1\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("                        .Z2\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("                            .Z3 {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("class Y extends Z1\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("                        .Z2 {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		assertEqualString(Util.convertToIndependantLineDelimiter(preview), Util.convertToIndependantLineDelimiter(buf.toString()));
	}

	public void test0006() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test0006", false, null);
		StringBuilder buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("package test0006;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("public class X {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("    void foo() {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("        bar1();\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("        \n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("        //comment1\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("        bar2();//comment2\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("        //comment3\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("        bar3();\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("    }\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		ICompilationUnit cu= pack1.createCompilationUnit("X.java", buf.toString(), false, null);

		CompilationUnit astRoot= createCU(cu, false);

		astRoot.recordModifications();

		AST a = astRoot.getAST();

		List types = astRoot.types();
		TypeDeclaration typeDeclaration = (TypeDeclaration)types.get(0);
		MethodDeclaration methodDeclaration = typeDeclaration.getMethods()[0];
		Block body = methodDeclaration.getBody();
		List statements = body.statements();
		Statement statement1 = (Statement)statements.get(1);
		Statement statement2 = (Statement)ASTNode.copySubtree(a, statement1);
		statements.add(statement2);

		String preview = evaluateRewrite(cu, astRoot);

		buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("package test0006;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("public class X {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("    void foo() {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("        bar1();\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("        \n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("        //comment1\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("        bar2();//comment2\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("        //comment3\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("        bar3();\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("        //comment1\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("        bar2();//comment2\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("    }\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		assertEqualString(Util.convertToIndependantLineDelimiter(preview), Util.convertToIndependantLineDelimiter(buf.toString()));
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=93208
	public void test0007() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test", false, null);
		StringBuilder buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("package test; public class Test { }"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		ICompilationUnit cu= pack1.createCompilationUnit("Test.java", buf.toString(), false, null);

		CompilationUnit astRoot= createCU(cu, false);

		astRoot.recordModifications();

		TypeDeclaration type = (TypeDeclaration) astRoot.types().get(0);
		AST ast = type.getAST();

		MethodDeclaration m = ast.newMethodDeclaration();
		type.bodyDeclarations().add(m);

		Block block = ast.newBlock();
		m.setName(ast.newSimpleName("foo"));
		m.setReturnType2(ast.newPrimitiveType(PrimitiveType.VOID));
		m.setBody(block);

		FieldAccess fa = ast.newFieldAccess();
		fa.setExpression(ast.newThisExpression());
		fa.setName(ast.newSimpleName("x"));
		MethodInvocation mi = ast.newMethodInvocation();
		mi.setExpression(fa);
		mi.setName(ast.newSimpleName("llall"));

		ExpressionStatement exp = ast.newExpressionStatement(mi);
		block.statements().add(exp);

		StructuralPropertyDescriptor loc = mi.getLocationInParent();
		//This will cause the bug
		ASTNode node = ASTNode.copySubtree(ast, fa);
		exp.setStructuralProperty(loc, node);

		String preview = evaluateRewrite(cu, astRoot);

		buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("package test; public class Test {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("    void foo(){this.x;} }"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		assertEqualString(Util.convertToIndependantLineDelimiter(preview), Util.convertToIndependantLineDelimiter(buf.toString()));
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=304656
	public void test0008() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test0008", false, null);
		StringBuilder buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("package test0008;\n" + //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				"\n" +
				"public class TestClass {\n" +
				"    Thread t = new Thread(new Runnable(){\n" +
				"\n" +
				"        @Override\n" +
				"        public void run() {\n" +
				"            try {\n" +
				"                Thread.currentThread().sleep(1000);\n" +
				"            } catch (InterruptedException e) {\n" +
				"                e.printStackTrace();\n" +
				"            }\n" +
				"        }\n" +
				"        \n" +
				"    });\n" +
				"\n" +
				"    public void testMethod(){\n" +
				"        t.start();\n" +
				"    }\n" +
				"\n" +
				"    public static Thread staticTestMethod(Thread thread){\n" +
				"        return thread;\n" +
				"    }\n" +
				"}");
		ICompilationUnit cu = pack1.createCompilationUnit("Test.java", buf.toString(), false, null);

		@SuppressWarnings("deprecation")
        ASTParser astParser = ASTParser.newParser(AST.JLS8);
		astParser.setSource(cu);
		ASTNode root = astParser.createAST(new NullProgressMonitor());
		AST ast = root.getAST();

		CompilationUnit compilationUnit = (CompilationUnit) root;
		compilationUnit.recordModifications();
		List types = compilationUnit.types();
		for (int i = 0, max = types.size(); i < max; i++) { //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
			TypeDeclaration td = (TypeDeclaration) types.get(i);
			MethodDeclaration[] methods = td.getMethods();
			for (int j = 0, max2 = methods.length; j < max2; j++) { //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
				MethodDeclaration md = methods[j];
				if (md.getName().getFullyQualifiedName().equals("testMethod")) {
					List statements = md.getBody().statements();
					ExpressionStatement es = (ExpressionStatement) statements.get(0);
					MethodInvocation mi = (MethodInvocation) es.getExpression();
					Expression existingExpression = mi.getExpression();
					MethodInvocation threadNameInvocation = ast.newMethodInvocation();
					threadNameInvocation.setExpression(null);
					threadNameInvocation.setName(ast.newSimpleName("staticTestMethod"));
					existingExpression.delete();
					threadNameInvocation.arguments().add(existingExpression);
					mi.setExpression(threadNameInvocation);


					Block code = md.getBody();
					TryStatement tryStatement = ast.newTryStatement();
					List catches = tryStatement.catchClauses();
					// create catch clause
					CatchClause catchClause = ast.newCatchClause();
					// exception to catch
					SingleVariableDeclaration svd = ast.newSingleVariableDeclaration();
					svd.setType(ast.newSimpleType(ast.newName("Throwable")));
					svd.setName(ast.newSimpleName("e"));
					catchClause.setException(svd);
					// code to run on catch
					Block catchBody = ast.newBlock();
					List catchStatements = catchBody.statements();
					// throw statement
					ThrowStatement throwStatement = ast.newThrowStatement();
					Expression throwExpression = ast.newName("e");
					throwStatement.setExpression(throwExpression);
					catchStatements.add(throwStatement);
					catches.add(catchClause);
					// create finally statement
					Block finallyBlock = ast.newBlock();
					statements = code.statements();
					tryStatement.setFinally(finallyBlock);
					code.delete();
					tryStatement.setBody(code);
					Block tryBlock = ast.newBlock();
					tryBlock.statements().add(tryStatement);
					md.setBody(tryBlock);
				}
			}

		}
		String preview = evaluateRewrite(cu, compilationUnit);

		buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("package test0008;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("public class TestClass {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("    Thread t = new Thread(new Runnable(){\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("        @Override\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("        public void run() {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("            try {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("                Thread.currentThread().sleep(1000);\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("            } catch (InterruptedException e) {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("                e.printStackTrace();\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("            }\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("        }\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("        \n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("    });\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("    public void testMethod(){\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("        try {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("            staticTestMethod(t).start();\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("        } catch (Throwable e) {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("        } finally {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("        }\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("    }\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("    public static Thread staticTestMethod(Thread thread){\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("        return thread;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("    }\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("}"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		assertEqualString(Util.convertToIndependantLineDelimiter(preview), Util.convertToIndependantLineDelimiter(buf.toString()));
	}

	public void test0009() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test0007", false, null);
		StringBuilder buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("package test0007;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("public class X {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("    /**\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("     * NOTHING\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("     */\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("    void foo() {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("    \n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("    }\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("    void bar() {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("    \n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("    }\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		ICompilationUnit cu= pack1.createCompilationUnit("X.java", buf.toString(), false, null);

		@SuppressWarnings("deprecation")
        ASTParser astParser = ASTParser.newParser(AST.JLS8);
		astParser.setSource(cu);
		CompilationUnit compilationUnit = (CompilationUnit) astParser.createAST(new NullProgressMonitor());
		AST ast = compilationUnit.getAST();

		compilationUnit.recordModifications();

		List types = compilationUnit.types();
		TypeDeclaration typeDeclaration = (TypeDeclaration)types.get(0);
		MethodDeclaration methodDeclaration1 = typeDeclaration.getMethods()[0];
		MethodDeclaration methodDeclaration2 = typeDeclaration.getMethods()[1];
		Javadoc javadoc1 = methodDeclaration1.getJavadoc();
		Javadoc javadoc2 = (Javadoc)ASTNode.copySubtree(ast, javadoc1);
		methodDeclaration2.setJavadoc(javadoc2);

		String preview = evaluateRewrite(cu, compilationUnit);

		buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("package test0007;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("public class X {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("    /**\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("     * NOTHING\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("     */\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("    void foo() {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("    \n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("    }\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("    /**\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("     * NOTHING\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("     */\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("    void bar() {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("    \n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("    }\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		assertEqualString(Util.convertToIndependantLineDelimiter(preview), Util.convertToIndependantLineDelimiter(buf.toString()));
	}
}
