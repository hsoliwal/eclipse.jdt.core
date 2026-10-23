/*******************************************************************************
 * Copyright (c) 2021, 2024 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.rewrite.describing;

import java.util.List;
import junit.framework.Test;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

@SuppressWarnings({"rawtypes", "deprecation"})
public class ASTRewritingSwitchPatternTest extends ASTRewritingTest {


	public ASTRewritingSwitchPatternTest(String name, int apiLevel) {
		super(name, apiLevel);
	}

	public static Test suite() {
		return createSuite(ASTRewritingSwitchPatternTest.class, 21);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		if (this.apiLevel == AST.JLS21 ) { // Remove this after it is a standard feature
			this.project1.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_21);
			this.project1.setOption(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_21);
			this.project1.setOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_21);
		}
	}

	private boolean checkAPILevel() {
		if (this.apiLevel < 21) {
			System.err.println("Test "+getName()+" requires a JRE 21"); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
			return true;
		}
		return false;
	}


	@SuppressWarnings("removal") //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
	public void testAddTypePattern() throws Exception {
		if (checkAPILevel()) {
			return;
		}
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("public class X {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(		"void foo(Object o) {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(		"	int i = switch (o) {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(	    "		case Integer i -> 1;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(	    "		default       	-> 0;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(	    "	};\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(	    "}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(		"\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(		"}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL

		ICompilationUnit cu= pack1.createCompilationUnit("X.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(this.apiLevel, cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		AST ast= astRoot.getAST();

		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		TypeDeclaration type= findTypeDeclaration(astRoot, "X");
		MethodDeclaration methodDecl= findMethodDeclaration(type, "foo");
		Block block= methodDecl.getBody();
		List blockStatements= block.statements();
		assertTrue("Number of statements not 1", blockStatements.size() == 1);
		{ // insert type pattern
			VariableDeclarationStatement variableDeclarationStatement = (VariableDeclarationStatement)blockStatements.get(0);

			VariableDeclarationFragment variableDeclarationFragment = (VariableDeclarationFragment)variableDeclarationStatement.fragments().get(0);
			SwitchExpression switchStatement= (SwitchExpression) variableDeclarationFragment.getInitializer();
			List statements= switchStatement.statements();
			assertTrue("Number of statements not 4", statements.size() == 4);

			SwitchCase caseStatement= ast.newSwitchCase();
			caseStatement.setSwitchLabeledRule(true);
			TypePattern typePattern = ast.newTypePattern();
			SingleVariableDeclaration patternVariable = ast.newSingleVariableDeclaration();
			patternVariable.setType(ast.newSimpleType(ast.newSimpleName("String")));
			patternVariable.setName(ast.newSimpleName("s"));

			if (this.apiLevel < AST.JLS22) {
				typePattern.setPatternVariable(patternVariable);
			} else {
				typePattern.setPatternVariable((VariableDeclaration) patternVariable);
			}

			caseStatement.expressions().add(typePattern);

			ListRewrite listRewrite= rewrite.getListRewrite(switchStatement, SwitchExpression.STATEMENTS_PROPERTY);
			listRewrite.insertAt(caseStatement, 2, null);

			YieldStatement yieldStatement = ast.newYieldStatement();
			yieldStatement.setExpression(ast.newNumberLiteral("2"));
			listRewrite.insertAt(yieldStatement, 3, null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("public class X {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(		"void foo(Object o) {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(		"	int i = switch (o) {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(	    "		case Integer i -> 1;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(	    "        case String s -> yield 2;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(	    "		default       	-> 0;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(	    "	};\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(	    "}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(		"\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(		"}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		assertEqualString(preview, buf.toString());
	}

	@SuppressWarnings("removal") //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
	public void tesModifyTypePattern() throws Exception {
		if (checkAPILevel()) {
			return;
		}
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("public class X {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(		"void foo(Object o) {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(		"	int i = switch (o) {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(	    "		case Integer i -> 1;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(	    "		default       	-> 0;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(	    "	};\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(	    "}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(		"\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(		"}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL

		ICompilationUnit cu= pack1.createCompilationUnit("X.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(this.apiLevel, cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		AST ast= astRoot.getAST();

		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		TypeDeclaration type= findTypeDeclaration(astRoot, "X");
		MethodDeclaration methodDecl= findMethodDeclaration(type, "foo");
		Block block= methodDecl.getBody();
		List blockStatements= block.statements();
		assertTrue("Number of statements not 1", blockStatements.size() == 1);
		{ // replace type pattern
			VariableDeclarationStatement variableDeclarationStatement = (VariableDeclarationStatement)blockStatements.get(0);

			VariableDeclarationFragment variableDeclarationFragment = (VariableDeclarationFragment)variableDeclarationStatement.fragments().get(0);
			SwitchExpression switchStatement= (SwitchExpression) variableDeclarationFragment.getInitializer();
			List statements= switchStatement.statements();
			assertTrue("Number of statements not 4", statements.size() == 4);

			SwitchCase caseStatement= (SwitchCase)statements.get(0);
			TypePattern typePattern = ast.newTypePattern();
			SingleVariableDeclaration patternVariable = ast.newSingleVariableDeclaration();
			patternVariable.setType(ast.newSimpleType(ast.newSimpleName("String")));
			patternVariable.setName(ast.newSimpleName("s"));
			typePattern.setPatternVariable(patternVariable);
			rewrite.replace((ASTNode) caseStatement.expressions().get(0), typePattern, null);

		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("public class X {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(		"void foo(Object o) {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(		"	int i = switch (o) {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(	    "		case String s -> 1;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(	    "		default       	-> 0;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(	    "	};\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(	    "}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(		"\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(		"}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		assertEqualString(preview, buf.toString());
	}

	public void testRemoveTypePattern() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		if (checkAPILevel()) {
			return;
		}
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("public class X {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(		"void foo(Object o) {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(		"	int i = switch (o) {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(	    "		case Integer i -> 1;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(	    "		case String s  -> 2;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(	    "		default       	-> 0;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(	    "	};\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(	    "}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(		"\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(		"}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL

		ICompilationUnit cu= pack1.createCompilationUnit("X.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(this.apiLevel, cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		TypeDeclaration type= findTypeDeclaration(astRoot, "X");
		MethodDeclaration methodDecl= findMethodDeclaration(type, "foo");
		Block block= methodDecl.getBody();
		List blockStatements= block.statements();
		assertTrue("Number of statements not 1", blockStatements.size() == 1);
		{ // remove type pattern statement
			VariableDeclarationStatement variableDeclarationStatement = (VariableDeclarationStatement)blockStatements.get(0);



			VariableDeclarationFragment variableDeclarationFragment = (VariableDeclarationFragment)variableDeclarationStatement.fragments().get(0);
			SwitchExpression switchStatement= (SwitchExpression) variableDeclarationFragment.getInitializer();
			List statements= switchStatement.statements();
			assertTrue("Number of statements not 6", statements.size() == 6);

			// remove statements

			rewrite.remove((ASTNode) statements.get(2), null);
			rewrite.remove((ASTNode) statements.get(3), null);
		}


		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("public class X {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(		"void foo(Object o) {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(		"	int i = switch (o) {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(	    "		case Integer i -> 1;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(	    "		default       	-> 0;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(	    "	};\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(	    "}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(		"\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(		"}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		assertEqualString(preview, buf.toString());
	}


	@SuppressWarnings("removal") //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
	public void testAddGuardedPattern() throws Exception {
		if (checkAPILevel()) {
			return;
		}
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("public class X {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(		"void foo(Object o) {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(		"	switch (o) {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(	    "		default       	: System.out.println(\"0\");\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(	    "	}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(	    "}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(		"\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(		"}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL

		ICompilationUnit cu= pack1.createCompilationUnit("X.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(this.apiLevel, cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		AST ast= astRoot.getAST();

		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		TypeDeclaration type= findTypeDeclaration(astRoot, "X");
		MethodDeclaration methodDecl= findMethodDeclaration(type, "foo");
		Block block= methodDecl.getBody();
		List blockStatements= block.statements();
		assertTrue("Number of statements not 1", blockStatements.size() == 1);
		{ // insert Guarded pattern
			SwitchStatement switchStatement = (SwitchStatement) blockStatements.get(0);

			List statements= switchStatement.statements();
			assertTrue("Number of statements not 2", statements.size() == 2);

			SwitchCase caseStatement= ast.newSwitchCase();
			caseStatement.setSwitchLabeledRule(false);
			GuardedPattern guardedPattern = ast.newGuardedPattern();
			TypePattern typePattern = ast.newTypePattern();
			SingleVariableDeclaration patternVariable = ast.newSingleVariableDeclaration();
			patternVariable.setType(ast.newSimpleType(ast.newSimpleName("Integer")));
			patternVariable.setName(ast.newSimpleName("i"));

			if (this.apiLevel < AST.JLS22) {
				typePattern.setPatternVariable(patternVariable);
			} else {
				typePattern.setPatternVariable((VariableDeclaration) patternVariable);
			}

			guardedPattern.setPattern(typePattern);
			InfixExpression infixExpression = ast.newInfixExpression();
			infixExpression.setOperator(InfixExpression.Operator.GREATER);
			infixExpression.setLeftOperand(ast.newSimpleName("i"));
			infixExpression.setRightOperand(ast.newNumberLiteral("10"));//$NON-NLS
			guardedPattern.setExpression(infixExpression);
			caseStatement.expressions().add(guardedPattern);

			ListRewrite listRewrite= rewrite.getListRewrite(switchStatement, SwitchStatement.STATEMENTS_PROPERTY);
			listRewrite.insertAt(caseStatement, 0, null);

			MethodInvocation methodInvocation = ast.newMethodInvocation();
			QualifiedName name =
				ast.newQualifiedName(
				ast.newSimpleName("System"),//$NON-NLS-1$
				ast.newSimpleName("out"));//$NON-NLS-1$
			methodInvocation.setExpression(name);
			methodInvocation.setName(ast.newSimpleName("println")); //$NON-NLS-1$
			StringLiteral stringLiteral = ast.newStringLiteral();
			stringLiteral.setLiteralValue("Greater than 10");//$NON-NLS-1$
			methodInvocation.arguments().add(stringLiteral);//$NON-NLS-1$
			ExpressionStatement expressionStatement = ast.newExpressionStatement(methodInvocation);
			listRewrite.insertAt(expressionStatement, 1, null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("public class X {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(		"void foo(Object o) {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(		"	switch (o) {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(	    "		case Integer i when i > 10:\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(	    "            System.out.println(\"Greater than 10\");\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(	    "        default       	: System.out.println(\"0\");\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(	    "	}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(	    "}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(		"\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(		"}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		assertEqualString(preview, buf.toString());
	}

	@SuppressWarnings("removal") //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
	public void testModifyGuardedPattern() throws Exception {
		if (checkAPILevel()) {
			return;
		}
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("public class X {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(		"void foo(Object o) {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(		"	switch (o) {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(	    "       case String s when s.equals(\"hi\") : System.out.println(\"hi\");\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(	    "		default       	: System.out.println(\"0\");\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(	    "	}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(	    "}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(		"\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(		"}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL

		ICompilationUnit cu= pack1.createCompilationUnit("X.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(this.apiLevel, cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		AST ast= astRoot.getAST();

		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		TypeDeclaration type= findTypeDeclaration(astRoot, "X");
		MethodDeclaration methodDecl= findMethodDeclaration(type, "foo");
		Block block= methodDecl.getBody();
		List blockStatements= block.statements();
		assertTrue("Number of statements not 1", blockStatements.size() == 1);
		{ // replace Guarded pattern
			SwitchStatement switchStatement = (SwitchStatement) blockStatements.get(0);

			List statements= switchStatement.statements();
			assertTrue("Number of statements not 4", statements.size() == 4);

			SwitchCase caseStatement= (SwitchCase)statements.get(0);
			GuardedPattern guardedPattern = ast.newGuardedPattern();
			TypePattern typePattern = ast.newTypePattern();
			SingleVariableDeclaration patternVariable = ast.newSingleVariableDeclaration();
			patternVariable.setType(ast.newSimpleType(ast.newSimpleName("Integer")));
			patternVariable.setName(ast.newSimpleName("i"));

			if (this.apiLevel < AST.JLS22) {
				typePattern.setPatternVariable(patternVariable);
			} else {
				typePattern.setPatternVariable((VariableDeclaration) patternVariable);
			}

			guardedPattern.setPattern(typePattern);
			InfixExpression infixExpression = ast.newInfixExpression();
			infixExpression.setOperator(InfixExpression.Operator.GREATER);
			infixExpression.setLeftOperand(ast.newSimpleName("i"));
			infixExpression.setRightOperand(ast.newNumberLiteral("10"));//$NON-NLS
			guardedPattern.setExpression(infixExpression);
			rewrite.replace((ASTNode) caseStatement.expressions().get(0),guardedPattern, null);

		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("public class X {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(		"void foo(Object o) {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(		"	switch (o) {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(	    "       case Integer i when i > 10 : System.out.println(\"hi\");\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(	    "		default       	: System.out.println(\"0\");\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(	    "	}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(	    "}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(		"\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(		"}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		assertEqualString(preview, buf.toString());
	}

	public void testRemoveGuardedPattern() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		if (checkAPILevel()) {
			return;
		}
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("public class X {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(		"void foo(Object o) {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(		"	int i = switch (o) {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(	    "		case Integer i when i > 10 : System.out.println(\"hi\");\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(	    "       case String s when s.equals(\"hi\") : System.out.println(\"hi\");\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(	    "		default       	-> 0;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(	    "	};\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(	    "}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(		"\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(		"}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL

		ICompilationUnit cu= pack1.createCompilationUnit("X.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(this.apiLevel, cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		TypeDeclaration type= findTypeDeclaration(astRoot, "X");
		MethodDeclaration methodDecl= findMethodDeclaration(type, "foo");
		Block block= methodDecl.getBody();
		List blockStatements= block.statements();
		assertTrue("Number of statements not 1", blockStatements.size() == 1);
		{ // remove guarded pattern statement
			VariableDeclarationStatement variableDeclarationStatement = (VariableDeclarationStatement)blockStatements.get(0);

			VariableDeclarationFragment variableDeclarationFragment = (VariableDeclarationFragment)variableDeclarationStatement.fragments().get(0);
			SwitchExpression switchStatement= (SwitchExpression) variableDeclarationFragment.getInitializer();
			List statements= switchStatement.statements();
			assertTrue("Number of statements not 6", statements.size() == 6);

			// remove statements

			rewrite.remove((ASTNode) statements.get(0), null);
			rewrite.remove((ASTNode) statements.get(1), null);
		}


		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("public class X {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(		"void foo(Object o) {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(		"	int i = switch (o) {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(	    "		case String s when s.equals(\"hi\") : System.out.println(\"hi\");\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(	    "		default       	-> 0;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(	    "	};\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(	    "}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(		"\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(		"}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		assertEqualString(preview, buf.toString());
	}

	public void testAddNullPattern() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		if (checkAPILevel()) {
			return;
		}
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("public class X {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(		"void foo(Object o) {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(		"	switch (o) {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(	    "		case Integer i -> System.out.println(\"Integer\");\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(	    "		default       	-> 0;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(	    "	}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(	    "}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(		"\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(		"}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL

		ICompilationUnit cu= pack1.createCompilationUnit("X.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(this.apiLevel, cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		AST ast= astRoot.getAST();

		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		TypeDeclaration type= findTypeDeclaration(astRoot, "X");
		MethodDeclaration methodDecl= findMethodDeclaration(type, "foo");
		Block block= methodDecl.getBody();
		List blockStatements= block.statements();
		assertTrue("Number of statements not 1", blockStatements.size() == 1);
		{ // insert null pattern
			SwitchStatement switchStatement = (SwitchStatement) blockStatements.get(0);

			List statements= switchStatement.statements();
			assertTrue("Number of statements not 4", statements.size() == 4);

			SwitchCase caseStatement= ast.newSwitchCase();
			caseStatement.setSwitchLabeledRule(true);
			NullPattern nullPattern = ast.newNullPattern();
			caseStatement.expressions().add(nullPattern);

			ListRewrite listRewrite= rewrite.getListRewrite(switchStatement, SwitchStatement.STATEMENTS_PROPERTY);
			listRewrite.insertAt(caseStatement, 2, null);

			MethodInvocation methodInvocation = ast.newMethodInvocation();
			QualifiedName name =
				ast.newQualifiedName(
				ast.newSimpleName("System"),//$NON-NLS-1$
				ast.newSimpleName("out"));//$NON-NLS-1$
			methodInvocation.setExpression(name);
			methodInvocation.setName(ast.newSimpleName("println")); //$NON-NLS-1$
			StringLiteral stringLiteral = ast.newStringLiteral();
			stringLiteral.setLiteralValue("Null");//$NON-NLS-1$
			methodInvocation.arguments().add(stringLiteral);//$NON-NLS-1$
			ExpressionStatement expressionStatement = ast.newExpressionStatement(methodInvocation);
			listRewrite.insertAt(expressionStatement, 3, null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("public class X {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(		"void foo(Object o) {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(		"	switch (o) {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(	    "		case Integer i -> System.out.println(\"Integer\");\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(	    "        case null -> System.out.println(\"Null\");\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(	    "		default       	-> 0;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(	    "	}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(	    "}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(		"\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(		"}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		assertEqualString(preview, buf.toString());
	}

	public void testRemoveNullPattern() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		if (checkAPILevel()) {
			return;
		}
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("public class X {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(		"void foo(Object o) {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(		"	switch (o) {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(	    "		case Integer i -> System.out.println(\"Integer\");\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(	    "		case null -> System.out.println(\"Null\");\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(	    "		default       	-> 0;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(	    "	}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(	    "}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(		"\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(		"}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL

		ICompilationUnit cu= pack1.createCompilationUnit("X.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(this.apiLevel, cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		TypeDeclaration type= findTypeDeclaration(astRoot, "X");
		MethodDeclaration methodDecl= findMethodDeclaration(type, "foo");
		Block block= methodDecl.getBody();
		List blockStatements= block.statements();
		assertTrue("Number of statements not 1", blockStatements.size() == 1);
		{ // remove null pattern
			SwitchStatement switchStatement = (SwitchStatement) blockStatements.get(0);

			List statements= switchStatement.statements();
			assertTrue("Number of statements not 6", statements.size() == 6);

			// remove statements

			rewrite.remove((ASTNode) statements.get(2), null);
			rewrite.remove((ASTNode) statements.get(3), null);


		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("public class X {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(		"void foo(Object o) {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(		"	switch (o) {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(	    "		case Integer i -> System.out.println(\"Integer\");\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(	    "		default       	-> 0;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(	    "	}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(	    "}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(		"\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(		"}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		assertEqualString(preview, buf.toString());
	}

	public void testAddCaseDefaultPattern() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		if (checkAPILevel()) {
			return;
		}
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("public class X {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(		"void foo(Object o) {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(		"	switch (o) {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(	    "		case Integer i -> System.out.println(\"Integer\");\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(	    "	}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(	    "}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(		"\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(		"}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL

		ICompilationUnit cu= pack1.createCompilationUnit("X.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(this.apiLevel, cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		AST ast= astRoot.getAST();

		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		TypeDeclaration type= findTypeDeclaration(astRoot, "X");
		MethodDeclaration methodDecl= findMethodDeclaration(type, "foo");
		Block block= methodDecl.getBody();
		List blockStatements= block.statements();
		assertTrue("Number of statements not 1", blockStatements.size() == 1);
		{ // insert case default pattern
			SwitchStatement switchStatement = (SwitchStatement) blockStatements.get(0);

			List statements= switchStatement.statements();
			assertTrue("Number of statements not 2", statements.size() == 2);

			SwitchCase caseStatement= ast.newSwitchCase();
			caseStatement.setSwitchLabeledRule(true);
			CaseDefaultExpression caseDefaultExpr = ast.newCaseDefaultExpression();
			caseStatement.expressions().add(caseDefaultExpr);

			ListRewrite listRewrite= rewrite.getListRewrite(switchStatement, SwitchStatement.STATEMENTS_PROPERTY);
			listRewrite.insertAt(caseStatement, 2, null);

			MethodInvocation methodInvocation = ast.newMethodInvocation();
			QualifiedName name =
				ast.newQualifiedName(
				ast.newSimpleName("System"),//$NON-NLS-1$
				ast.newSimpleName("out"));//$NON-NLS-1$
			methodInvocation.setExpression(name);
			methodInvocation.setName(ast.newSimpleName("println")); //$NON-NLS-1$
			StringLiteral stringLiteral = ast.newStringLiteral();
			stringLiteral.setLiteralValue("Default");//$NON-NLS-1$
			methodInvocation.arguments().add(stringLiteral);//$NON-NLS-1$
			ExpressionStatement expressionStatement = ast.newExpressionStatement(methodInvocation);
			listRewrite.insertAt(expressionStatement, 3, null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("public class X {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(		"void foo(Object o) {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(		"	switch (o) {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(	    "		case Integer i -> System.out.println(\"Integer\");\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(	    "        case default -> System.out.println(\"Default\");\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(	    "	}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(	    "}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(		"\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(		"}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		assertEqualString(preview, buf.toString());
	}

	public void testRemoveCaseDefaultPattern() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		if (checkAPILevel()) {
			return;
		}
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("public class X {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(		"void foo(Object o) {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(		"	switch (o) {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(	    "		case Integer i -> System.out.println(\"Integer\");\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(	    "		case default -> System.out.println(\"Default\");\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(	    "	}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(	    "}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(		"\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(		"}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL

		ICompilationUnit cu= pack1.createCompilationUnit("X.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(this.apiLevel, cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		TypeDeclaration type= findTypeDeclaration(astRoot, "X");
		MethodDeclaration methodDecl= findMethodDeclaration(type, "foo");
		Block block= methodDecl.getBody();
		List blockStatements= block.statements();
		assertTrue("Number of statements not 1", blockStatements.size() == 1);
		{ // remove case default pattern
			SwitchStatement switchStatement = (SwitchStatement) blockStatements.get(0);

			List statements= switchStatement.statements();
			assertTrue("Number of statements not 4", statements.size() == 4);

			// remove statements

			rewrite.remove((ASTNode) statements.get(2), null);
			rewrite.remove((ASTNode) statements.get(3), null);

		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("public class X {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(		"void foo(Object o) {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(		"	switch (o) {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(	    "		case Integer i -> System.out.println(\"Integer\");\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(	    "	}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(	    "}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(		"\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(		"}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		assertEqualString(preview, buf.toString());
	}

	public void testAddCaseDefaulAndNulltPattern() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		if (checkAPILevel()) {
			return;
		}
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("public class X {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(		"void foo(Object o) {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(		"	switch (o) {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(	    "		case Integer i -> System.out.println(\"Integer\");\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(	    "	}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(	    "}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(		"\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(		"}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL

		ICompilationUnit cu= pack1.createCompilationUnit("X.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(this.apiLevel, cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		AST ast= astRoot.getAST();

		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		TypeDeclaration type= findTypeDeclaration(astRoot, "X");
		MethodDeclaration methodDecl= findMethodDeclaration(type, "foo");
		Block block= methodDecl.getBody();
		List blockStatements= block.statements();
		assertTrue("Number of statements not 1", blockStatements.size() == 1);
		{ // insert case default pattern
			SwitchStatement switchStatement = (SwitchStatement) blockStatements.get(0);

			List statements= switchStatement.statements();
			assertTrue("Number of statements not 2", statements.size() == 2);

			SwitchCase caseStatement= ast.newSwitchCase();
			caseStatement.setSwitchLabeledRule(true);
			CaseDefaultExpression caseDefaultExpr = ast.newCaseDefaultExpression();
			caseStatement.expressions().add(caseDefaultExpr);
			NullPattern nullPattern = ast.newNullPattern();
			caseStatement.expressions().add(nullPattern);

			ListRewrite listRewrite= rewrite.getListRewrite(switchStatement, SwitchStatement.STATEMENTS_PROPERTY);
			listRewrite.insertAt(caseStatement, 2, null);

			MethodInvocation methodInvocation = ast.newMethodInvocation();
			QualifiedName name =
				ast.newQualifiedName(
				ast.newSimpleName("System"),//$NON-NLS-1$
				ast.newSimpleName("out"));//$NON-NLS-1$
			methodInvocation.setExpression(name);
			methodInvocation.setName(ast.newSimpleName("println")); //$NON-NLS-1$
			StringLiteral stringLiteral = ast.newStringLiteral();
			stringLiteral.setLiteralValue("Default");//$NON-NLS-1$
			methodInvocation.arguments().add(stringLiteral);//$NON-NLS-1$
			ExpressionStatement expressionStatement = ast.newExpressionStatement(methodInvocation);
			listRewrite.insertAt(expressionStatement, 3, null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("public class X {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(		"void foo(Object o) {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(		"	switch (o) {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(	    "		case Integer i -> System.out.println(\"Integer\");\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(	    "        case default, null -> System.out.println(\"Default\");\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(	    "	}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(	    "}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(		"\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(		"}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		assertEqualString(preview, buf.toString());
	}

	public void testRemoveCaseDefaultAndNullPattern() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		if (checkAPILevel()) {
			return;
		}
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("public class X {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(		"void foo(Object o) {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(		"	switch (o) {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(	    "		case Integer i -> System.out.println(\"Integer\");\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(	    "		case default, null -> System.out.println(\"Default\");\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(	    "	}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(	    "}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(		"\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(		"}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL

		ICompilationUnit cu= pack1.createCompilationUnit("X.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(this.apiLevel, cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		TypeDeclaration type= findTypeDeclaration(astRoot, "X");
		MethodDeclaration methodDecl= findMethodDeclaration(type, "foo");
		Block block= methodDecl.getBody();
		List blockStatements= block.statements();
		assertTrue("Number of statements not 1", blockStatements.size() == 1);
		{ // remove case default pattern
			SwitchStatement switchStatement = (SwitchStatement) blockStatements.get(0);

			List statements= switchStatement.statements();
			assertTrue("Number of statements not 4", statements.size() == 4);

			// remove statements

			rewrite.remove((ASTNode) statements.get(2), null);
			rewrite.remove((ASTNode) statements.get(3), null);

		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("public class X {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(		"void foo(Object o) {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(		"	switch (o) {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(	    "		case Integer i -> System.out.println(\"Integer\");\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(	    "	}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(	    "}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(		"\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(		"}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		assertEqualString(preview, buf.toString());
	}

	public void testAddCaseDefaultToNullPattern() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		if (checkAPILevel()) {
			return;
		}
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("public class X {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(		"void foo(Object o) {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(		"	switch (o) {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(	    "		case Integer i -> System.out.println(\"Integer\");\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(	    "		case null -> System.out.println(\"Default\");\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(	    "	}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(	    "}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(		"\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(		"}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL

		ICompilationUnit cu= pack1.createCompilationUnit("X.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(this.apiLevel, cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		TypeDeclaration type= findTypeDeclaration(astRoot, "X");
		MethodDeclaration methodDecl= findMethodDeclaration(type, "foo");
		Block block= methodDecl.getBody();
		List blockStatements= block.statements();
		AST ast= astRoot.getAST();
		assertTrue("Number of statements not 1", blockStatements.size() == 1);
		{ // Add case default pattern to existing null pattern
			SwitchStatement switchStatement = (SwitchStatement) blockStatements.get(0);

			List statements= switchStatement.statements();
			assertTrue("Number of statements not 4", statements.size() == 4);

			SwitchCase caseStatement= (SwitchCase)statements.get(2);
			ListRewrite listRewrite= rewrite.getListRewrite(caseStatement, SwitchCase.EXPRESSIONS2_PROPERTY);
			CaseDefaultExpression caseDefaultExpression = ast.newCaseDefaultExpression();
			listRewrite.insertAt(caseDefaultExpression, 1, null);

		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("public class X {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(		"void foo(Object o) {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(		"	switch (o) {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(	    "		case Integer i -> System.out.println(\"Integer\");\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(	    "		case null, default -> System.out.println(\"Default\");\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(	    "	}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(	    "}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(		"\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(		"}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		assertEqualString(preview, buf.toString());
	}

	public void testAddNullPatternToCaseDefault() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		if (checkAPILevel()) {
			return;
		}
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("public class X {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(		"void foo(Object o) {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(		"	switch (o) {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(	    "		case Integer i -> System.out.println(\"Integer\");\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(	    "		case default -> System.out.println(\"Default\");\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(	    "	}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(	    "}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(		"\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(		"}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL

		ICompilationUnit cu= pack1.createCompilationUnit("X.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(this.apiLevel, cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		TypeDeclaration type= findTypeDeclaration(astRoot, "X");
		MethodDeclaration methodDecl= findMethodDeclaration(type, "foo");
		Block block= methodDecl.getBody();
		List blockStatements= block.statements();
		AST ast= astRoot.getAST();
		assertTrue("Number of statements not 1", blockStatements.size() == 1);
		{ // Add case default pattern to existing null pattern
			SwitchStatement switchStatement = (SwitchStatement) blockStatements.get(0);

			List statements= switchStatement.statements();
			assertTrue("Number of statements not 4", statements.size() == 4);

			SwitchCase caseStatement= (SwitchCase)statements.get(2);
			ListRewrite listRewrite= rewrite.getListRewrite(caseStatement, SwitchCase.EXPRESSIONS2_PROPERTY);
			NullPattern nullPattern = ast.newNullPattern();
			listRewrite.insertAt(nullPattern, 0, null);

		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("public class X {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(		"void foo(Object o) {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(		"	switch (o) {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(	    "		case Integer i -> System.out.println(\"Integer\");\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(	    "		case null, default -> System.out.println(\"Default\");\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(	    "	}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(	    "}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(		"\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(		"}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		assertEqualString(preview, buf.toString());
	}

	public void testRemoveNullPatternFromNullAndCaseDefault() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		if (checkAPILevel()) {
			return;
		}
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("public class X {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(		"void foo(Object o) {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(		"	switch (o) {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(	    "		case Integer i -> System.out.println(\"Integer\");\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(	    "		case null, default -> System.out.println(\"Default\");\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(	    "	}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(	    "}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(		"\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(		"}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL

		ICompilationUnit cu= pack1.createCompilationUnit("X.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(this.apiLevel, cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		TypeDeclaration type= findTypeDeclaration(astRoot, "X");
		MethodDeclaration methodDecl= findMethodDeclaration(type, "foo");
		Block block= methodDecl.getBody();
		List blockStatements= block.statements();
		assertTrue("Number of statements not 1", blockStatements.size() == 1);
		{ // Add case default pattern to existing null pattern
			SwitchStatement switchStatement = (SwitchStatement) blockStatements.get(0);

			List statements= switchStatement.statements();
			assertTrue("Number of statements not 4", statements.size() == 4);

			SwitchCase caseStatement= (SwitchCase)statements.get(2);
			rewrite.remove((ASTNode) caseStatement.expressions().get(0), null);

		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("public class X {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(		"void foo(Object o) {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(		"	switch (o) {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(	    "		case Integer i -> System.out.println(\"Integer\");\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(	    "		case default -> System.out.println(\"Default\");\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(	    "	}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(	    "}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(		"\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(		"}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		assertEqualString(preview, buf.toString());
	}

	public void testRemoveDefaultPatternFromNullAndCaseDefault() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		if (checkAPILevel()) {
			return;
		}
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("public class X {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(		"void foo(Object o) {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(		"	switch (o) {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(	    "		case Integer i -> System.out.println(\"Integer\");\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(	    "		case null, default -> System.out.println(\"Default\");\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(	    "	}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(	    "}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(		"\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(		"}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL

		ICompilationUnit cu= pack1.createCompilationUnit("X.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(this.apiLevel, cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		TypeDeclaration type= findTypeDeclaration(astRoot, "X");
		MethodDeclaration methodDecl= findMethodDeclaration(type, "foo");
		Block block= methodDecl.getBody();
		List blockStatements= block.statements();
		assertTrue("Number of statements not 1", blockStatements.size() == 1);
		{ // Add case default pattern to existing null pattern
			SwitchStatement switchStatement = (SwitchStatement) blockStatements.get(0);

			List statements= switchStatement.statements();
			assertTrue("Number of statements not 4", statements.size() == 4);

			SwitchCase caseStatement= (SwitchCase)statements.get(2);
			rewrite.remove((ASTNode) caseStatement.expressions().get(1), null);

		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("public class X {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(		"void foo(Object o) {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(		"	switch (o) {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(	    "		case Integer i -> System.out.println(\"Integer\");\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(	    "		case null -> System.out.println(\"Default\");\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(	    "	}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(	    "}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(		"\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append(		"}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		assertEqualString(preview, buf.toString());
	}

	public void testNPEinASTRewriteFlattener() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
        if (checkAPILevel()) {
            return;
        }
        IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
        StringBuilder buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
        buf.append("public class X {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
        buf.append(     "void foo(Object o) {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
        buf.append(     "   switch (o) {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
        buf.append(     "       case Integer i when i > 10:\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
        buf.append(     "            System.out.println(\"Greater than 10\");\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
        buf.append(     "       default         : System.out.println(\"0\");\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
        buf.append(     "   }\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
        buf.append(     "}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
        buf.append(     "\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
        buf.append(     "}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL

        ICompilationUnit cu= pack1.createCompilationUnit("X.java", buf.toString(), false, null);

        CompilationUnit astRoot= createAST(this.apiLevel, cu);
        ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

        AST ast= astRoot.getAST();

        assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
        TypeDeclaration type= findTypeDeclaration(astRoot, "X");
        MethodDeclaration methodDecl= findMethodDeclaration(type, "foo");
        Block block= methodDecl.getBody();
        List blockStatements= block.statements();
        assertTrue("Number of statements not 1", blockStatements.size() == 1);

        { // Modify Pattern from the Guarded pattern

            SwitchStatement switchStatement = (SwitchStatement) blockStatements.get(0);
            List statements= switchStatement.statements();
            assertTrue("Number of statements not 4", statements.size() == 4);
            SwitchCase caseStatement= (SwitchCase)statements.get(0);
            GuardedPattern guardedPattern = ast.newGuardedPattern();

            rewrite.replace((ASTNode) caseStatement.expressions().get(0),guardedPattern, null);
        }

        String preview= evaluateRewrite(cu, rewrite);

        StringBuilder buf1= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
        buf1= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
        buf1.append("public class X {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
        buf1.append(        "void foo(Object o) {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
        buf1.append(        "   switch (o) {\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
        buf1.append(        "       case null when null:\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
        buf1.append(        "            System.out.println(\"Greater than 10\");\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
        buf1.append(        "       default         : System.out.println(\"0\");\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
        buf1.append(        "   }\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
        buf1.append(        "}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
        buf1.append(        "\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
        buf1.append(        "}\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
        assertEqualString(preview, buf1.toString());
    }

}
