/*******************************************************************************
 * Copyright (c) 2000, 2013 IBM Corporation and others.
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
package org.eclipse.jdt.core.tests.rewrite.describing;


import junit.framework.Test;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

public class ASTRewritingGroupNodeTest extends ASTRewritingTest {

	public ASTRewritingGroupNodeTest(String name) {
		super(name);
	}

	public ASTRewritingGroupNodeTest(String name, int apiLevel) {
		super(name, apiLevel);
	}

	public static Test suite() {
		return createSuite(ASTRewritingGroupNodeTest.class);
	}

	public void testCollapsedTargetNodes() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);

		StringBuilder buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("package test1;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("public class E {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("    public void foo(Object o) {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("        return;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("    }\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		AST ast= astRoot.getAST();

		TypeDeclaration type= findTypeDeclaration(astRoot, "E");
		MethodDeclaration methodDecl= findMethodDeclaration(type, "foo");

		ReturnStatement returnStatement= (ReturnStatement) methodDecl.getBody().statements().get(0);

		MethodInvocation newMethodInv1= ast.newMethodInvocation();
		newMethodInv1.setName(ast.newSimpleName("foo1"));
		ExpressionStatement st1= ast.newExpressionStatement(newMethodInv1);

		MethodInvocation newMethodInv2= ast.newMethodInvocation();
		newMethodInv2.setName(ast.newSimpleName("foo2"));
		ExpressionStatement st2= ast.newExpressionStatement(newMethodInv2);

		ASTNode placeholder= rewrite.createGroupNode(new Statement[] { st1, st2 });
		rewrite.replace(returnStatement, placeholder, null);

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("package test1;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("public class E {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("    public void foo(Object o) {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("        foo1();\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("        foo2();\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("    }\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		String expected= buf.toString();

		assertEqualString(preview, expected);
	}

	public void testCollapsedTargetNodes2() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);

		StringBuilder buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("package test1;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("public class E {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("    public void foo(Object o) {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("        {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("            return;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("        }\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("    }\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		AST ast= astRoot.getAST();

		TypeDeclaration type= findTypeDeclaration(astRoot, "E");
		MethodDeclaration methodDecl= findMethodDeclaration(type, "foo");

		Statement statement= (Statement) methodDecl.getBody().statements().get(0);

		MethodInvocation newMethodInv1= ast.newMethodInvocation();
		newMethodInv1.setName(ast.newSimpleName("foo1"));
		ExpressionStatement st1= ast.newExpressionStatement(newMethodInv1);

		MethodInvocation newMethodInv2= ast.newMethodInvocation();
		newMethodInv2.setName(ast.newSimpleName("foo2"));
		ExpressionStatement st2= ast.newExpressionStatement(newMethodInv2);

		ReturnStatement st3= (ReturnStatement) rewrite.createCopyTarget((ASTNode) ((Block) statement).statements().get(0));

		ASTNode placeholder= rewrite.createGroupNode(new Statement[] { st1, st2, st3 });
		rewrite.replace(statement, placeholder, null);

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("package test1;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("public class E {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("    public void foo(Object o) {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("        foo1();\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("        foo2();\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("        return;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("    }\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		String expected= buf.toString();

		assertEqualString(preview, expected);
	}

}
