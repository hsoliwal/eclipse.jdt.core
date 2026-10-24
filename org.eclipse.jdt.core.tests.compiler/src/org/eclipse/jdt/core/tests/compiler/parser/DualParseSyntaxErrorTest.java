/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
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
package org.eclipse.jdt.core.tests.compiler.parser;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Locale;
import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.core.tests.util.AbstractCompilerTest;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.DefaultErrorHandlingPolicies;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.batch.CompilationUnit;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.parser.Parser;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblem;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;

public class DualParseSyntaxErrorTest extends AbstractCompilerTest {
	public static boolean optimizeStringLiterals = false;
	public static long sourceLevel = CompilerOptions.getFirstSupportedJdkLevel(); //$NON-NLS-1$

public DualParseSyntaxErrorTest(String testName){
	super(testName);
}
public void checkParse( //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
	char[] source,
	String expectedSyntaxErrorDiagnosis,
	String testName) {

	Parser parser =
		new Parser(
			new ProblemReporter(
				DefaultErrorHandlingPolicies.proceedWithAllProblems(),
				new CompilerOptions(getCompilerOptions()),
				new DefaultProblemFactory(Locale.getDefault())),
			optimizeStringLiterals);

	ICompilationUnit sourceUnit = new CompilationUnit(source, testName, null);
	CompilationResult compilationResult = new CompilationResult(sourceUnit, 0, 0, 0);

	CompilationUnitDeclaration computedUnit = parser.dietParse(sourceUnit, compilationResult);
	if (computedUnit.types != null) {
		for (int i = 0, length = computedUnit.types.length; i < length; i++){ //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
			computedUnit.types[i].parseMethods(parser, computedUnit);
		}
	}

	StringBuilder buffer = new StringBuilder(100); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
	if (compilationResult.hasProblems() || compilationResult.hasTasks()) {
		CategorizedProblem[] problems = compilationResult.getAllProblems();
		int count = problems.length;
		int problemCount = 0;
		char[] unitSource = compilationResult.compilationUnit.getContents();
		for (int i = 0; i < count; i++) { //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
			if (problems[i] != null) {
				if (problemCount == 0)
					buffer.append("----------\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				problemCount++;
				buffer.append(problemCount + (problems[i].isError() ? ". ERROR" : ". WARNING")); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				buffer.append(" in " + new String(problems[i].getOriginatingFileName()).replace('/', '\\')); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				try {
					buffer.append(((DefaultProblem)problems[i]).errorReportSource(unitSource)); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
					buffer.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
					buffer.append(problems[i].getMessage()); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
					buffer.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				} catch (Exception e) {
					StringWriter stringWriter = new StringWriter();
					e.printStackTrace(new PrintWriter(stringWriter));
					buffer.append(stringWriter.getBuffer()); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				}
				buffer.append("----------\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			}
		}
	}
	String computedSyntaxErrorDiagnosis = buffer.toString();
	if(!Util.convertToIndependantLineDelimiter(expectedSyntaxErrorDiagnosis)
			.equals(Util.convertToIndependantLineDelimiter(computedSyntaxErrorDiagnosis))) {
 		System.out.println(Util.displayString(computedSyntaxErrorDiagnosis));
	}
	assertEquals(
		"Invalid syntax error diagnosis" + testName, //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		Util.convertToIndependantLineDelimiter(expectedSyntaxErrorDiagnosis),
		Util.convertToIndependantLineDelimiter(computedSyntaxErrorDiagnosis));
}

public void test01() { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD

	String s =
		"public class X {							\n"+
		"	int fX;         						\n"+
		"	void foo() {   							\n"+
		"		fX = 0;  							\n"+
		"	}			  							\n"+
		"	public void bar() {						\n"+
		"	}										\n"+
		"}											\n";

	String expectedSyntaxErrorDiagnosis =
		"";

	String testName = "<test1>";
	checkParse(
		s.toCharArray(),
		expectedSyntaxErrorDiagnosis,
		testName);
}
public void test02() { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD

	String s =
		"public class X {							\n"+
		"	int fX;         						\n"+
		"	void foo() {   							\n"+
		"		fX = 0;  							\n"+
		"	public void bar() {						\n"+
		"	}										\n"+
		"}											\n";

	String expectedSyntaxErrorDiagnosis =
		"----------\n" +
		"1. ERROR in <test2> (at line 4)\n" +
		"	fX = 0;  							\n" +
		"	      ^\n" +
		"Syntax error, insert \"}\" to complete MethodBody\n" +
		"----------\n";

	String testName = "<test2>";
	checkParse(
		s.toCharArray(),
		expectedSyntaxErrorDiagnosis,
		testName);
}
public void test03() { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD

	String s =
		"public class X {							\n"+
		"	int fX;         						\n"+
		"	void foo()   							\n"+
		"		fX = 0;  							\n"+
		"	public void bar() {						\n"+
		"	}										\n"+
		"}											\n";

	String expectedSyntaxErrorDiagnosis =
		"----------\n" +
		"1. ERROR in <test3> (at line 3)\n" +
		"	void foo()   							\n" +
		"	         ^\n" +
		"Syntax error on token \")\", { expected after this token\n" +
		"----------\n" +
		"2. ERROR in <test3> (at line 4)\n" +
		"	fX = 0;  							\n" +
		"	      ^\n" +
		"Syntax error, insert \"}\" to complete MethodBody\n" +
		"----------\n";

	String testName = "<test3>";
	checkParse(
		s.toCharArray(),
		expectedSyntaxErrorDiagnosis,
		testName);
}
public void test04() { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD

	String s =
		"public class X {							\n"+
		"	int fX;         						\n"+
		"	void foo()   							\n"+
		"		fX = 0;  							\n"+
		"	} 			  							\n"+
		"	public void bar() {						\n"+
		"	}										\n"+
		"}											\n";

	String expectedSyntaxErrorDiagnosis =
		"----------\n" +
		"1. ERROR in <test4> (at line 3)\n" +
		"	void foo()   							\n" +
		"	         ^\n" +
		"Syntax error on token \")\", { expected after this token\n" +
		"----------\n";

	String testName = "<test4>";
	checkParse(
		s.toCharArray(),
		expectedSyntaxErrorDiagnosis,
		testName);
}
public void test05() { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD

	String s =
		"public class X {							\n"+
		"	int fX;         						\n"+
		"	void foo() {  							\n"+
		"		if(true){  							\n"+
		"	} 			  							\n"+
		"	public void bar() {						\n"+
		"	}										\n"+
		"}											\n";

	String expectedSyntaxErrorDiagnosis =
		"----------\n" +
		"1. ERROR in <test5> (at line 4)\n" +
		"	if(true){  							\n" +
		"	        ^\n" +
		"Syntax error, insert \"}\" to complete Statement\n" +
		"----------\n";

	String testName = "<test5>";
	checkParse(
		s.toCharArray(),
		expectedSyntaxErrorDiagnosis,
		testName);
}
public void test06() { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD

	String s =
		"public class X {							\n"+
		"	int fX;         						\n"+
		"	void foo() {  							\n"+
		"		if(true){  							\n"+
		"	} 			  							\n"+
		"	//comment								\n"+
		"	public void bar() {						\n"+
		"	}										\n"+
		"}											\n";

	String expectedSyntaxErrorDiagnosis =
		"----------\n" +
		"1. ERROR in <test6> (at line 4)\n" +
		"	if(true){  							\n" +
		"	        ^\n" +
		"Syntax error, insert \"}\" to complete Statement\n" +
		"----------\n";

	String testName = "<test6>";
	checkParse(
		s.toCharArray(),
		expectedSyntaxErrorDiagnosis,
		testName);
}
public void test07() { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD

	String s =
		"public class X {							\n"+
		"	int fX;         						\n"+
		"	void foo() {  							\n"+
		"		if(true){  							\n"+
		"	} 			  							\n"+
		"	System.out.println();					\n"+
		"	public void bar() {						\n"+
		"	}										\n"+
		"}											\n";

	String expectedSyntaxErrorDiagnosis =
		"----------\n" +
		"1. ERROR in <test7> (at line 6)\n" +
		"	System.out.println();					\n" +
		"	                    ^\n" +
		"Syntax error, insert \"}\" to complete MethodBody\n" +
		"----------\n";

	String testName = "<test7>";
	checkParse(
		s.toCharArray(),
		expectedSyntaxErrorDiagnosis,
		testName);
}
public void test08() { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD

	String s =
		"public class X {							\n"+
		"	int fX;         						\n"+
		"	void foo() {  							\n"+
		"		if(true){  							\n"+
		"	} 			  							\n"+
		"	public int bar;							\n"+
		"}											\n";

	String expectedSyntaxErrorDiagnosis =
		"----------\n" +
		"1. ERROR in <test8> (at line 4)\n" +
		"	if(true){  							\n" +
		"	        ^\n" +
		"Syntax error, insert \"}\" to complete Statement\n" +
		"----------\n";

	String testName = "<test8>";
	checkParse(
		s.toCharArray(),
		expectedSyntaxErrorDiagnosis,
		testName);
}
public void test09() { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD

	String s =
		"public class X {							\n"+
		"	int fX;         						\n"+
		"	void foo() {  							\n"+
		"		if(true){  							\n"+
		"	} 			  							\n"+
		"	//comment	  							\n"+
		"	public int bar;							\n"+
		"}											\n";

	String expectedSyntaxErrorDiagnosis =
		"----------\n" +
		"1. ERROR in <test9> (at line 4)\n" +
		"	if(true){  							\n" +
		"	        ^\n" +
		"Syntax error, insert \"}\" to complete Statement\n" +
		"----------\n";

	String testName = "<test9>";
	checkParse(
		s.toCharArray(),
		expectedSyntaxErrorDiagnosis,
		testName);
}
public void test10() { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD

	String s =
		"public class X {							\n"+
		"	int fX;         						\n"+
		"	void foo() {  							\n"+
		"		if(true){  							\n"+
		"	} 			  							\n"+
		"	System.out.println();					\n"+
		"	public int bar;							\n"+
		"}											\n";

	String expectedSyntaxErrorDiagnosis =
		"----------\n" +
		"1. ERROR in <test10> (at line 6)\n" +
		"	System.out.println();					\n" +
		"	                    ^\n" +
		"Syntax error, insert \"}\" to complete MethodBody\n" +
		"----------\n";

	String testName = "<test10>";
	checkParse(
		s.toCharArray(),
		expectedSyntaxErrorDiagnosis,
		testName);
}
}
