/*******************************************************************************
 * Copyright (c) 2016, 2024 IBM Corporation and others.
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
package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import junit.framework.Test;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.tests.compiler.regression.AbstractRegressionTest.JavacTestOptions.Excuse;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.core.util.IClassFileAttribute;
import org.eclipse.jdt.core.util.IClassFileReader;
import org.eclipse.jdt.core.util.IModuleAttribute;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.codegen.AttributeNamesConstants;
import org.eclipse.jdt.internal.compiler.lookup.SplitPackageBinding;

public class ModuleCompilationTests extends AbstractModuleCompilationTest {

	static {
//		 TESTS_NAMES = new String[] { "testRelease565930" };
		// TESTS_NUMBERS = new int[] { 1 };
		// TESTS_RANGE = new int[] { 298, -1 };
	}

	public ModuleCompilationTests(String name) {
		super(name);
	}

	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_9);
	}

	public static Class<?> testClass() {
		return ModuleCompilationTests.class;
	}

	public void test001() { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		runNegativeModuleTest(
			new String[] {
				"p/X.java",
				"package p;\n" +
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"     java.sql.Connection con = null;\n" +
				"	}\n" +
				"}",
				"module-info.java",
				"module mod.one { \n" +
				"	requires java.base;\n" +
				"}"
	        },
			" -9 \"" + getSourceDir() +  File.separator + "module-info.java\" " //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
	        + "\"" + getSourceDir() +  File.separator + "p/X.java\"",
	        "",
	        "----------\n" +
    		"1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/p/X.java (at line 4)\n" +
    		"	java.sql.Connection con = null;\n" +
    		"	^^^^^^^^^^^^^^^^^^^\n" +
    		"The type java.sql.Connection is not accessible\n" +
    		"----------\n" +
    		"1 problem (1 error)\n",
	        true,
	        "package java.sql" /* match for javac error */);
	}
	public void test002() { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		runConformModuleTest(
			new String[] {
				"p/X.java",
				"package p;\n" +
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"     java.sql.Connection con = null;\n" +
				"     System.out.println(con);\n" +
				"	}\n" +
				"}",
				"module-info.java",
				"module mod.one { \n" +
				"	requires java.base;\n" +
				"	requires java.sql;\n" +
				"}"
	        },
			" -9 \"" + OUTPUT_DIR +  File.separator + "module-info.java\" " //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
	        + "\"" + OUTPUT_DIR +  File.separator + "p/X.java\"",
	        "",
	        "",
	        true);
	}
	public void test003() {
		runConformModuleTest(
			new String[] {
				"p/X.java",
				"package p;\n" +
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"     java.sql.Connection con = null;\n" +
				"     System.out.println(con);\n" +
				"	}\n" +
				"}",
	        },
	        "\"" + OUTPUT_DIR +  File.separator + "p/X.java\"", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
	        "",
	        "",
	        true);
	}
	public void test004() {
		Set<String> classFiles = runConformModuleTest(
			new String[] {
				"module-info.java",
				"module mod.one { \n" +
				"	requires java.base;\n" +
				"	requires java.sql;\n" +
				"}"
	        },
			" -9 \"" + OUTPUT_DIR +  File.separator + "module-info.java\"", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
	        "",
	        "",
	        true);
		String fileName = getEcjOutputDir() + File.separator + "module-info.class"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		assertClassFile("Missing modul-info.class: " + fileName, fileName, classFiles); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
	}
	public void test005() { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		Set<String> classFiles = runConformModuleTest(
			new String[] {
				"p/X.java",
				"package p;\n" +
				"public class X {\n" +
				"	java.sql.Connection con;\n" +
				"}",
				"module-info.java",
				"module mod.one { \n" +
				"	requires java.base;\n" +
				"	requires java.sql;\n" +
				"	requires java.desktop;\n" +
				"}",
				"q/Y.java",
				"package q;\n" +
				"public class Y {\n" +
				"   java.awt.Image image;\n" +
				"}"
	        },
			" -9 \"" + OUTPUT_DIR + File.separator + "module-info.java\" " //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
			+ "\"" + OUTPUT_DIR + File.separator + "q/Y.java\" "
	        + "\"" + OUTPUT_DIR + File.separator + "p/X.java\"",
	        "",
	        "",
	        true);
		String fileName = getEcjOutputDir()  + File.separator + "module-info.class"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		assertClassFile("Missing modul-info.class: " + fileName, fileName, classFiles); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
	}
	public void test006() { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		String moduleLoc = directory + File.separator + "mod.one"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		List<String> files = new ArrayList<>(); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.one { \n" +
						"	requires java.base;\n" +
						"	requires java.sql;\n" +
						"	requires java.desktop;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "p", "X.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
						"package p;\n" +
						"public class X {\n" +
						"	java.sql.Connection con;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "q", "Y.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
						"package q;\n" +
						"public class Y {\n" +
						"   java.awt.Image image;\n" +
						"}");

		StringBuilder buffer = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buffer.append("-d " + OUTPUT_DIR + File.separator + out ) //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-source-path " + "\"" + directory + "\""); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		runConformModuleTest(files, buffer, "", "");
	}
	public void test007() { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		String moduleLoc = directory + File.separator + "mod.one"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		List<String> files = new ArrayList<>(); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.one { \n" +
						"	requires java.base;\n" +
						"	requires transitive java.sql;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "p", "X.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
						"package p;\n" +
						"public class X {\n" +
						"	public static java.sql.Connection getConnection() {\n" +
						"		return null;\n" +
						"	}\n" +
						"}");
		moduleLoc = directory + File.separator + "mod.two"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.two { \n" +
						"	requires java.base;\n" +
						"	requires mod.one;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "q", "Y.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
						"package q;\n" +
						"public class Y {\n" +
						"   java.sql.Connection con = p.X.getConnection();\n" +
						"}");

		StringBuilder buffer = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buffer.append("-d " + OUTPUT_DIR + File.separator + out ) //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-source-path " + "\"" + directory + "\""); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		runNegativeModuleTest(files,
				buffer,
				"",
				"----------\n" +
				"1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.two/q/Y.java (at line 3)\n" +
				"	java.sql.Connection con = p.X.getConnection();\n" +
				"	                          ^^^\n" +
				"The type p.X is not accessible\n" +
				"----------\n" +
				"1 problem (1 error)\n",
				"p.X");
	}
	public void test008() { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		String moduleLoc = directory + File.separator + "mod.one"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		List<String> files = new ArrayList<>(); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.one { \n" +
						"	exports p;\n" +
						"	requires mod.two;\n" +
						"	requires transitive java.sql;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "p", "X.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
						"package p;\n" +
						"import q.Y;\n" +
						"public class X {\n" +
						"	public static java.sql.Connection getConnection() {\n" +
						"		return Y.con;\n" +
						"	}\n" +
						"}");
		moduleLoc = directory + File.separator + "mod.two"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.two { \n" +
						"	exports q;\n" +
						"	requires java.sql;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "q", "Y.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
						"package q;\n" +
						"public class Y {\n" +
						"   public static java.sql.Connection con = null;\n" +
						"}");

		StringBuilder buffer = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buffer.append("-d " + OUTPUT_DIR + File.separator + out ) //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" -warn:-exports") // Y.con unreliably refers to Connection (missing requires transitive)
			.append(" --module-source-path " + "\"" + directory + "\""); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT

		runConformModuleTest(files,
				buffer,
				"",
				"");
	}
	public void test008a() { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		String moduleLoc = directory + File.separator + "mod.one"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		List<String> files = new ArrayList<>(); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.one { \n" +
						"	exports p.q;\n" +
						"	requires java.base;\n" +
						"	requires transitive java.sql;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "p" + File.separator + "q", "X.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
						"package p.q;\n" +
						"public class X {\n" +
						"	public static java.sql.Connection getConnection() {\n" +
						"		return null;\n" +
						"	}\n" +
						"}");
		moduleLoc = directory + File.separator + "mod.two"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.two { \n" +
						"	requires java.base;\n" +
						"	requires mod.one;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "q" + File.separator + "r", "Y.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
						"package q.r;\n" +
						"public class Y {\n" +
						"   java.sql.Connection con = p.q.X.getConnection();\n" +
						"}");

		String systemDirectory = OUTPUT_DIR+File.separator+"system"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		writeFile(systemDirectory, "readme.txt", "Not a valid system");

		StringBuilder buffer = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buffer.append("-d " + OUTPUT_DIR + File.separator + out ) //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			.append(" -9 ")
			.append("--system ").append(systemDirectory)
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-source-path " + "\"" + directory + "\""); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT

		runNegativeModuleTest(files,
				buffer,
				"",
				"invalid location for system libraries: ---OUTPUT_DIR_PLACEHOLDER---/system\n",
				"system");
	}
	public void test009() { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		String moduleLoc = directory + File.separator + "mod.one"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		List<String> files = new ArrayList<>(); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.one { \n" +
						"	exports p;\n" +
						"	requires java.base;\n" +
						"	requires transitive java.sql;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "p", "X.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
						"package p;\n" +
						"public class X {\n" +
						"	public static java.sql.Connection getConnection() {\n" +
						"		return null;\n" +
						"	}\n" +
						"}");
		moduleLoc = directory + File.separator + "mod.two"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.two { \n" +
						"	requires java.base;\n" +
						"	requires mod.one;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "q", "Y.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
						"package q;\n" +
						"public class Y {\n" +
						"   java.sql.Connection con = p.X.getConnection();\n" +
						"}");

		StringBuilder buffer = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buffer.append("-d " + OUTPUT_DIR + File.separator + out ) //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			.append(" -9 ")
			.append(" --module-source-path " + "\"" + directory + "\""); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT

		runConformModuleTest(files,
				buffer,
				"",
				"");
	}
	private void createUnnamedLibrary(String unnamedLoc, String unnamedBin) { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		writeFile(unnamedLoc + File.separator + "s" + File.separator + "t", "Tester.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
				"package s.t;\n" +
				"public class Tester {\n" +
				"}");

		StringBuilder buffer = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buffer.append("-d " + unnamedBin) //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\"")
			.append(" -sourcepath \"" + unnamedLoc + "\" ") //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
			.append(unnamedLoc + File.separator + "s" + File.separator + "t" + File.separator + "Tester.java"); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT

		runConformTest(new String[]{},
				buffer.toString(),
				"",
				"",
				false);
	}
	private void createReusableModules(String srcDir, String outDir, File modDir) { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		String moduleLoc = srcDir + File.separator + "mod.one"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		List<String> files = new ArrayList<>(); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.one { \n" +
						"	exports p;\n" +
						"	requires java.base;\n" +
						"	requires transitive java.sql;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "p", "X.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
						"package p;\n" +
						"public class X {\n" +
						"	public static java.sql.Connection getConnection() {\n" +
						"		return null;\n" +
						"	}\n" +
						"}");
		// This one is not exported (i.e. internal to this module)
		writeFileCollecting(files, moduleLoc + File.separator + "p1", "X1.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
				"package p1;\n" +
				"public class X1 {\n" +
				"	public static java.sql.Connection getConnection() {\n" +
				"		return null;\n" +
				"	}\n" +
				"}");

		moduleLoc = srcDir + File.separator + "mod.two"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.two { \n" +
						"	exports q;\n" +
						"	requires java.base;\n" +
						"	requires mod.one;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "q", "Y.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
						"package q;\n" +
						"public class Y {\n" +
						"   java.sql.Connection con = p.X.getConnection();\n" +
						"}");

		StringBuilder buffer = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
			buffer.append("-d " + outDir ) //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			.append(" -9 ")
			.append(" --module-path \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-source-path " + "\"" + srcDir + "\""); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		for (String fileName : files) //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
			buffer.append(" \"").append(fileName).append("\""); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL

		runConformTest(new String[]{},
				buffer.toString(),
				"",
				"",
				false);

		String fileName = modDir + File.separator + "mod.one.jar"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		try {
			Util.zip(new File(outDir + File.separator + "mod.one"), //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
								fileName);
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (!modDir.exists()) {
			if (!modDir.mkdirs()) {
				fail("Coult not create folder " + modDir); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
			}
		}
		File mod2 = new File(modDir, "mod.two");
		if (!mod2.mkdir()) {
			fail("Coult not create folder " + mod2); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		}
		Util.copy(outDir + File.separator + "mod.two", mod2.getAbsolutePath()); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT

		Util.flushDirectoryContent(new File(outDir));
		Util.flushDirectoryContent(new File(srcDir));
	}
	public void test010() { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		Util.flushDirectoryContent(new File(OUTPUT_DIR));
		String outDir = OUTPUT_DIR + File.separator + "bin"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		String srcDir = OUTPUT_DIR + File.separator + "src"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		File modDir = new File(OUTPUT_DIR + File.separator + "mod"); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		createReusableModules(srcDir, outDir, modDir);
		String moduleLoc = srcDir + File.separator + "mod.three"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		List<String> files = new ArrayList<>(); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.three { \n" +
						"	requires mod.one;\n" +
						"	requires mod.two;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "r", "Z.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
						"package r;\n" +
						"public class Z extends Object {\n" +
						"	p.X x = null;\n" +
						"	q.Y y = null;\n" +
						"}");

		StringBuilder buffer = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
			buffer.append("-d " + outDir ) //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			.append(" -9 ")
			.append(" -p \"")
			.append(Util.getJavaClassLibsAsString())
			.append(modDir.getAbsolutePath())
			.append("\" ")
			.append(" --module-source-path " + "\"" + srcDir + "\""); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT

		runConformModuleTest(files,
				buffer,
				"",
				"");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=487421
	public void test011() { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		runConformModuleTest(
			new String[] {
				"p/X.java",
				"package p;\n" +
				"public class X {\n" +
				"   java.lang.SecurityException ex = null;\n" +
				"}",
				"module-info.java",
				"module mod.one { \n" +
				"	requires java.base;\n" +
				"}"
	        },
			" -9 \"" + OUTPUT_DIR +  File.separator + "module-info.java\" " //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
	        + "\"" + OUTPUT_DIR +  File.separator + "p/X.java\"",
	        "",
	        "",
	        true);
	}
	// Modules used as regular -classpath (as opposed to --module-path) and module-info referencing
	// those modules are reported as missing.
	public void test012() { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		Util.flushDirectoryContent(new File(OUTPUT_DIR));
		String outDir = OUTPUT_DIR + File.separator + "bin"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		String srcDir = OUTPUT_DIR + File.separator + "src"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		File modDir = new File(OUTPUT_DIR + File.separator + "mod"); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		createReusableModules(srcDir, outDir, modDir);
		String moduleLoc = srcDir + File.separator + "mod.three"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		List<String> files = new ArrayList<>(); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.three { \n" +
						"	requires mod.one;\n" +
						"	requires mod.two;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "r", "Z.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
						"package r;\n" +
						"public class Z extends Object {\n" +
						"}");

		StringBuilder buffer = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
			buffer.append("-d " + outDir ) //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append(modDir + File.separator + "mod.one.jar").append(File.pathSeparator) //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
			.append(modDir + File.separator + "mod.two").append(File.pathSeparator) //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
			.append("\" ")
			.append(" --module-source-path " + "\"" + srcDir + "\""); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT

		runNegativeModuleTest(files,
				buffer,
				"",
				"----------\n" +
				"1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.three/module-info.java (at line 2)\n" +
				"	requires mod.one;\n" +
				"	         ^^^^^^^\n" +
				"mod.one cannot be resolved to a module\n" +
				"----------\n" +
				"2. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.three/module-info.java (at line 3)\n" +
				"	requires mod.two;\n" +
				"	         ^^^^^^^\n" +
				"mod.two cannot be resolved to a module\n" +
				"----------\n" +
				"2 problems (2 errors)\n",
				"module");
	}
	// Modules used as regular -classpath as opposed to --module-path. The files being compiled
	// aren't part of any modules (i.e. module-info is missing). The files should be able to
	// reference the types from referenced classpath.
	public void test013() { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		Util.flushDirectoryContent(new File(OUTPUT_DIR));
		String outDir = OUTPUT_DIR + File.separator + "bin"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		String srcDir = OUTPUT_DIR + File.separator + "src"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		File modDir = new File(OUTPUT_DIR + File.separator + "mod"); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		createReusableModules(srcDir, outDir, modDir);
		String moduleLoc = srcDir + File.separator + "mod.three"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		List<String> files = new ArrayList<>(); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW
		writeFileCollecting(files, moduleLoc + File.separator + "p", "Z.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
						"package r;\n" +
						"public class Z extends Object {\n" +
						"	p.X x = null;\n" +
						"	q.Y y = null;\n" +
						"}");

		StringBuilder buffer = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
			buffer.append("-d " + outDir ) //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			.append(" -9")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append(modDir + File.separator + "mod.one.jar").append(File.pathSeparator) //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
			.append(modDir + File.separator + "mod.two").append(File.pathSeparator) //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
			.append("\" ");
		runConformModuleTest(files,
				buffer,
				"",
				"");
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=495500
	//-source 9
	public void testBug495500a() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"/** */\n" +
				"public class X {\n" +
				"}",
			},
	  "\"" + OUTPUT_DIR +  File.separator + "X.java\"" //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
	  + " -9 -d \"" + OUTPUT_DIR + "\"",
	  "",
	  "",
	  true);
		String expectedOutput = "// Compiled from X.java (version 9 : 53.0, super bit)";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
	}
	//-source 8 -target 9
	public void testBug495500b() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"/** */\n" +
				"public class X {\n" +
				"}",
			},
			"\"" + OUTPUT_DIR +  File.separator + "X.java\"" //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
			+ " -9 -source 8 -target 9 -d \"" + OUTPUT_DIR + "\"",
			"",
			"",
			true);
		String expectedOutput = "// Compiled from X.java (version 9 : 53.0, super bit)";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
	}
	// compliance 9 -source 9 -target 9
	public void testBug495500c() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"/** */\n" +
				"public class X {\n" +
				"}",
			},
			"\"" + OUTPUT_DIR +  File.separator + "X.java\"" //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
			+ " -9 -source 9 -target 9 -d \"" + OUTPUT_DIR + "\"",
			"",
			"",
			true);
		String expectedOutput = "// Compiled from X.java (version 9 : 53.0, super bit)";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
	}
	/*
	 * Test add-exports grants visibility to another module
	 */
	public void test014() { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		String moduleLoc = directory + File.separator + "mod.one"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		List<String> files = new ArrayList<>(); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.one { \n" +
						"	requires java.base;\n" +
						"	requires transitive java.sql;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "p", "X.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
						"package p;\n" +
						"public class X {\n" +
						"	public static java.sql.Connection getConnection() {\n" +
						"		return null;\n" +
						"	}\n" +
						"}");
		moduleLoc = directory + File.separator + "mod.two"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.two { \n" +
						"	requires java.base;\n" +
						"	requires mod.one;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "q", "Y.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
						"package q;\n" +
						"public class Y {\n" +
						"   java.sql.Connection con = p.X.getConnection();\n" +
						"}");

		StringBuilder buffer = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buffer.append("-d " + OUTPUT_DIR + File.separator + out ) //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-source-path " + "\"" + directory + "\"") //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
			.append(" --add-exports mod.one/p=mod.two");

		runConformModuleTest(files,
				buffer,
				"",
				"");
	}
	public void test015() { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		String moduleLoc = directory + File.separator + "mod.one"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		List<String> files = new ArrayList<>(); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.one { \n" +
						"	requires java.base;\n" +
						"	requires transitive java.sql;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "p", "X.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
						"package p;\n" +
						"public class X {\n" +
						"	public static java.sql.Connection getConnection() {\n" +
						"		return null;\n" +
						"	}\n" +
						"}");
		moduleLoc = directory + File.separator + "mod.two"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.two { \n" +
						"	requires java.base;\n" +
						"	requires java.sql;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "q", "Y.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
						"package q;\n" +
						"public class Y {\n" +
						"   java.sql.Connection con = p.X.getConnection();\n" +
						"}");

		StringBuilder buffer = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buffer.append("-d " + OUTPUT_DIR + File.separator + out ) //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-source-path " + "\"" + directory + "\"") //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
			.append(" --add-exports mod.one/p=mod.two");
		runNegativeModuleTest(files,
				buffer,
				"",
				"----------\n" +
				"1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.two/q/Y.java (at line 3)\n" +
				"	java.sql.Connection con = p.X.getConnection();\n" +
				"	                          ^^^\n" +
				"The type p.X is not accessible\n" +
				"----------\n" +
				"1 problem (1 error)\n",
				"does not read it",
				JavacTestOptions.JavacHasABug.JavacBug8207032);
	}
	public void test016() { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		String moduleLoc = directory + File.separator + "mod.one"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		List<String> files = new ArrayList<>(); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.one { \n" +
						"	requires java.base;\n" +
						"	requires transitive java.sql;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "p", "X.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
						"package p;\n" +
						"public class X {\n" +
						"	public static java.sql.Connection getConnection() {\n" +
						"		return null;\n" +
						"	}\n" +
						"}");
		moduleLoc = directory + File.separator + "mod.two"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.two { \n" +
						"	requires java.base;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "q", "Y.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
						"package q;\n" +
						"public class Y {\n" +
						"   java.sql.Connection con = p.X.getConnection();\n" +
						"}");

		StringBuilder buffer = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buffer.append("-d " + OUTPUT_DIR + File.separator + out ) //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-source-path " + "\"" + directory + "\"") //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
			.append(" --add-exports mod.one/p=mod.two")
			.append(" --add-reads mod.two=mod.one");

		runConformModuleTest(files,
				buffer,
				"",
				"");
	}
	public void test017() { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		String moduleLoc = directory + File.separator + "mod.one"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		List<String> files = new ArrayList<>(); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.one { \n" +
						"	requires java.base;\n" +
						"	requires transitive java.sql;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "p", "X.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
						"package p;\n" +
						"public class X {\n" +
						"	public static java.sql.Connection getConnection() {\n" +
						"		return null;\n" +
						"	}\n" +
						"}");
		moduleLoc = directory + File.separator + "mod.two"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.two { \n" +
						"	requires java.base;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "q", "Y.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
						"package q;\n" +
						"public class Y {\n" +
						"   java.sql.Connection con = p.X.getConnection();\n" +
						"}");

		StringBuilder buffer = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buffer.append("-d " + OUTPUT_DIR + File.separator + out ) //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-source-path " + "\"" + directory + "\"") //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
			.append(" --add-exports mod.one/p=mod.three")
			.append(" --add-reads mod.two=mod.one");

		runNegativeModuleTest(files,
				buffer,
				"",
				"----------\n" +
				"1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.two/q/Y.java (at line 3)\n" +
				"	java.sql.Connection con = p.X.getConnection();\n" +
				"	                          ^^^\n" +
				"The type p.X is not accessible\n" +
				"----------\n" +
				"1 problem (1 error)\n",
				"visible");
	}
	public void test018() { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		String moduleLoc = directory + File.separator + "mod.one"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		List<String> files = new ArrayList<>(); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.one { \n" +
						"	requires java.base;\n" +
						"	requires transitive java.sql;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "p", "X.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
						"package p;\n" +
						"public class X {\n" +
						"	public static java.sql.Connection getConnection() {\n" +
						"		return null;\n" +
						"	}\n" +
						"}");
		moduleLoc = directory + File.separator + "mod.two"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.two { \n" +
						"	requires java.base;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "q", "Y.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
						"package q;\n" +
						"public class Y {\n" +
						"   java.sql.Connection con = p.X.getConnection();\n" +
						"}");
		moduleLoc = directory + File.separator + "mod.three"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.three { \n" +
						"	requires java.base;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "r", "Z.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
						"package r;\n" +
						"public class Z {\n" +
						"   java.sql.Connection con = p.X.getConnection();\n" +
						"}");


		StringBuilder buffer = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buffer.append("-d " + OUTPUT_DIR + File.separator + out ) //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-source-path " + "\"" + directory + "\"") //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
			.append(" --add-exports mod.one/p=mod.two,mod.three")
			.append(" --add-reads mod.two=mod.one")
			.append(" --add-reads mod.three=mod.one");

		runConformModuleTest(files,
				buffer,
				"",
				"");
	}
	/*
	 * Unnamed module tries to access a type from an unexported package successfully due to --add-exports
	 */
	public void test019() { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		Runner runner = new Runner();
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		String moduleLoc = directory + File.separator + "mod.one"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		runner.createFile(moduleLoc + File.separator + "p", "X.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
						"package p;\n" +
						"public abstract class X extends com.sun.security.ntlm.Server {\n" +
						"	//public X() {}\n" +
						"	public X(String arg0, String arg1) throws com.sun.security.ntlm.NTLMException {\n" +
						"		super(arg0, arg1);\n" +
						"	}\n" +
						"}");

		runner.commandLine.append("-d " + OUTPUT_DIR + File.separator + out ) //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" -sourcepath " + "\"" + moduleLoc + "\" ") //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
			.append(" --add-exports java.base/com.sun.security.ntlm=ALL-UNNAMED ");

		runner.javacVersionOptions = JAVAC_SOURCE_9_OPTIONS; // otherwise javac: error: exporting a package from system module java.base is not allowed with --release
		runner.runConformModuleTest();
	}
	/*
	 * Named module tries to access a type from an unnamed module successfully due to --add-reads
	 */
	public void test019b() { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		String unnamedLoc = directory + File.separator + "nomodule"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		String unnamedBin = OUTPUT_DIR + File.separator + "un_bin"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		String moduleLoc = directory + File.separator + "mod" + File.separator + "mod.one"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT

		createUnnamedLibrary(unnamedLoc, unnamedBin);

		List<String> files = new ArrayList<>(); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.one {\n" +
						"	exports p.q;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "p" + File.separator + "q", "X.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
						"package p.q;\n" +
						"public abstract class X {\n" +
						"	s.t.Tester t;\n" +
						"}");

		StringBuilder buffer = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buffer.append("-d " + OUTPUT_DIR + File.separator + out ) //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append(unnamedBin + File.pathSeparator) //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
			.append("\"")
			.append(" --module-source-path \"" + directory + File.separator + "mod" + "\" ") //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
			.append(" --add-reads mod.one=ALL-UNNAMED ");

		runConformModuleTest(files,
				buffer,
				"",
				"");
	}

	/*
	 * Can only import from a package that contains compilation units (from the unnamed module)
	 */
	public void test019c() { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		String unnamedLoc = directory + File.separator + "nomodule"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		String unnamedBin = OUTPUT_DIR + File.separator + "un_bin"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT

		createUnnamedLibrary(unnamedLoc, unnamedBin);

		List<String> files = new ArrayList<>(); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW
		String moduleLoc = directory + File.separator + "mod.one"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.one {\n" +
						"	exports p.q;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "p" + File.separator + "q", "X.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
						"package p.q;\n" +
						"import s.*;\n" +
						"import s.t.*;\n" +
						"public abstract class X {\n" +
						"}");

		StringBuilder buffer = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buffer.append("-d " + OUTPUT_DIR + File.separator + out ) //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append(unnamedBin + File.pathSeparator) //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
			.append("\"")
			.append(" --module-source-path \"" + directory + "\" ") //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
			.append(" --add-reads mod.one=ALL-UNNAMED ");

		runNegativeModuleTest(files,
				buffer,
				"",
				"----------\n" +
				"1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.one/p/q/X.java (at line 2)\n" +
				"	import s.*;\n" +
				"	       ^\n" +
				"The package s is not accessible\n" +
				"----------\n" +
				"1 problem (1 error)\n",
				"package s",
				 JavacTestOptions.JavacHasABug.JavacBug8204534);
	}
	/*
	 * Unnamed module tries to access a type from an unexported package, fail
	 */
	public void test019fail() { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		String moduleLoc = directory + File.separator + "mod.one"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		List<String> files = new ArrayList<>(); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW
		writeFileCollecting(files, moduleLoc + File.separator + "p", "X.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
						"package p;\n" +
						"public abstract class X extends com.sun.security.ntlm.Server {\n" +
						"	//public X() {}\n" +
						"	public X(String arg0, String arg1) throws com.sun.security.ntlm.NTLMException {\n" +
						"		super(arg0, arg1);\n" +
						"	}\n" +
						"}");

		StringBuilder buffer = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buffer.append("-d " + OUTPUT_DIR + File.separator + out ) //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" -sourcepath " + "\"" + moduleLoc + "\" "); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT

		runNegativeModuleTest(files,
				buffer,
				"",
				"----------\n" +
				"1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.one/p/X.java (at line 2)\n" +
				"	public abstract class X extends com.sun.security.ntlm.Server {\n" +
				"	                                ^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
				"The type com.sun.security.ntlm.Server is not accessible\n" +
				"----------\n" +
				"2. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.one/p/X.java (at line 4)\n" +
				"	public X(String arg0, String arg1) throws com.sun.security.ntlm.NTLMException {\n" +
				"	                                          ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
				"The type com.sun.security.ntlm.NTLMException is not accessible\n" +
				"----------\n" +
				"2 problems (2 errors)\n",
				"package com.sun.security.ntlm");
				/* javac9:
				 * src/mod.one/p/X.java:2: error: package com.sun.security.ntlm is not visible
				 * public abstract class X extends com.sun.security.ntlm.Server {
				 *                                                 ^
				 *   (package com.sun.security.ntlm is declared in module java.base, which does not export it to the unnamed module)
				 * src/mod.one/p/X.java:4: error: package com.sun.security.ntlm is not visible
				 * public X(String arg0, String arg1) throws com.sun.security.ntlm.NTLMException {
				 *                                                           ^
				 *   (package com.sun.security.ntlm is declared in module java.base, which does not export it to the unnamed module)
				 */
				/* javac10:
				 * src/mod.one/p/X.java:2: error: package com.sun.security.ntlm does not exist
				 * public abstract class X extends com.sun.security.ntlm.Server {
				 *                                                      ^
				 * src/mod.one/p/X.java:4: error: package com.sun.security.ntlm does not exist
				 * public X(String arg0, String arg1) throws com.sun.security.ntlm.NTLMException {
				 *                                                                ^
				 */
	}
	public void test020() { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		String moduleLoc = directory + File.separator + "mod.one"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		List<String> files = new ArrayList<>(); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.one { \n" +
						"	requires java.base;\n" +
						"	requires transitive java.sql;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "p", "X.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
						"package p;\n" +
						"public class X {\n" +
						"	public static java.sql.Connection getConnection() {\n" +
						"		return null;\n" +
						"	}\n" +
						"}");

		StringBuilder buffer = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buffer.append("-d " + OUTPUT_DIR + File.separator + out ) //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-source-path " + "\"" + directory + "\"") //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
			.append(" --add-exports mod.one=mod.two,mod.three");

		runNegativeModuleTest(files,
				buffer,
				"",
				"incorrectly formatted option: --add-exports mod.one=mod.two,mod.three\n",
				"option");
	}
	public void test021() { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		String moduleLoc = directory + File.separator + "mod.one"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		List<String> files = new ArrayList<>(); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.one { \n" +
						"	requires java.base;\n" +
						"	requires transitive java.sql;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "p", "X.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
						"package p;\n" +
						"public class X {\n" +
						"	public static java.sql.Connection getConnection() {\n" +
						"		return null;\n" +
						"	}\n" +
						"}");

		StringBuilder buffer = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buffer.append("-d " + OUTPUT_DIR + File.separator + out ) //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-source-path " + "\"" + directory + "\"") //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
			.append(" --add-reads mod.one/mod.two");

		runNegativeModuleTest(files,
				buffer,
				"",
				"incorrectly formatted option: --add-reads mod.one/mod.two\n",
				"option");
	}
	public void test022() { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		String moduleLoc = directory + File.separator + "mod.one"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		List<String> files = new ArrayList<>(); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.one { \n" +
						"	requires java.base;\n" +
						"	requires transitive java.sql;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "p", "X.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
						"package p;\n" +
						"public class X {\n" +
						"	public static java.sql.Connection getConnection() {\n" +
						"		return null;\n" +
						"	}\n" +
						"}");

		StringBuilder buffer = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buffer.append("-d " + OUTPUT_DIR + File.separator + out ) //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-source-path " + "\"" + directory + "\"") //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
			.append(" --add-exports mod.one/p=mod.three")
			.append(" --add-exports mod.one/p=mod.three");

		runNegativeModuleTest(files,
				buffer,
				"",
				"can specify a package in a module only once with --add-export\n",
				"export");
	}
	public void test023() { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		String moduleLoc = directory + File.separator + "mod.one"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		List<String> files = new ArrayList<>(); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.one { \n" +
						"	requires java.base;\n" +
						"	requires transitive java.sql;\n" +
						"}");

		StringBuilder buffer = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buffer.append("-d " + OUTPUT_DIR + File.separator + out ) //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append("\"" + moduleLoc +  File.separator + "module-info.java\" ") //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
			.append(" -extdirs " + OUTPUT_DIR + File.separator + "src"); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT

		runNegativeModuleTest(files,
				buffer,
				"",
				"option -extdirs not supported at compliance level 9 and above\n",
				"extdirs");
	}
	public void test024() { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		String moduleLoc = directory + File.separator + "mod.one"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		List<String> files = new ArrayList<>(); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.one { \n" +
						"	requires java.base;\n" +
						"	requires transitive java.sql;\n" +
						"}");

		StringBuilder buffer = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buffer.append("-d " + OUTPUT_DIR + File.separator + out ) //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" \"" + moduleLoc +  File.separator + "module-info.java\" ") //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
			.append(" -bootclasspath " + OUTPUT_DIR + File.separator + "src"); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT

		runNegativeModuleTest(files,
				buffer,
				"",
				"option -bootclasspath not supported at compliance level 9 and above\n",
				"option --boot-class-path cannot be used together with --release"); // error message has changed between versions, name of option plus reason for illegality can be questioned
	}
	public void test025() { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		String moduleLoc = directory + File.separator + "mod.one"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		List<String> files = new ArrayList<>(); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.one { \n" +
						"	requires java.base;\n" +
						"	requires transitive java.sql;\n" +
						"}");

		StringBuilder buffer = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buffer.append("-d " + OUTPUT_DIR + File.separator + out ) //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append("\"" + moduleLoc +  File.separator + "module-info.java\" ") //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
			.append(" -endorseddirs " + OUTPUT_DIR + File.separator + "src"); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT

		runNegativeModuleTest(files,
				buffer,
				"",
				"option -endorseddirs not supported at compliance level 9 and above\n",
				"endorseddirs");
	}
	public void test026() { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		Runner runner = new Runner();
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		String moduleLoc = directory + File.separator + "mod.one"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		runner.createFile(
						moduleLoc, "module-info.java",
						"module mod.one { \n" +
						"	requires java.base;\n" +
						"	requires transitive java.sql;\n" +
						"}");
		String javaHome = System.getProperty("java.home");
		runner.commandLine.append("-d " + OUTPUT_DIR + File.separator + out ) //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			.append(" -9 ")
			.append(" --system \"").append(javaHome).append("\"");

		runner.javacVersionOptions = JAVAC_SOURCE_9_OPTIONS;
		runner.runConformModuleTest();
	}
	/**
	 * Mixed case of exported and non exported packages being referred to in another module
	 */
	public void test028() { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		Util.flushDirectoryContent(new File(OUTPUT_DIR));
		String outDir = OUTPUT_DIR + File.separator + "bin"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		String srcDir = OUTPUT_DIR + File.separator + "src"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		File modDir = new File(OUTPUT_DIR + File.separator + "mod"); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		createReusableModules(srcDir, outDir, modDir);
		String moduleLoc = srcDir + File.separator + "mod.three"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		List<String> files = new ArrayList<>(); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.three { \n" +
						"	requires mod.one;\n" +
						"	requires mod.two;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "r", "Z.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
						"package r;\n" +
						"public class Z extends Object {\n" +
						"	p.X x = null;\n" +
						"	p1.X1 x1 = null;\n" +
						"}");

		StringBuilder buffer = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buffer.append("-d " + outDir ) //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		.append(" -9 ")
		.append(" -p \"")
		.append(Util.getJavaClassLibsAsString())
		.append(modDir.getAbsolutePath())
		.append("\" ")
		.append(" --module-source-path " + "\"" + srcDir + "\""); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT

		runNegativeModuleTest(files,
				buffer,
				"",
				"----------\n" +
				"1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.three/r/Z.java (at line 4)\n"+
				"	p1.X1 x1 = null;\n" +
				"	^^^^^\n" +
				"The type p1.X1 is not accessible\n" +
				"----------\n" +
				"1 problem (1 error)\n",
				"visible");
	}
	public void test029() { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		String moduleLoc = directory + File.separator + "mod.one"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		List<String> files = new ArrayList<>(); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.one { \n" +
						"	requires java.base;\n" +
						"	requires java.sql;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "p", "X.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
						"package p;\n" +
						"public class X {\n" +
						"	public static java.sql.Connection getConnection() {\n" +
						"		return null;\n" +
						"	}\n" +
						"}");
		moduleLoc = directory + File.separator + "mod.two"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.two { \n" +
						"	requires java.base;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "q", "Y.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
						"package q;\n" +
						"public class Y {\n" +
						"   java.sql.Connection con = p.X.getConnection();\n" +
						"}");

		StringBuilder buffer = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buffer.append("-d " + OUTPUT_DIR + File.separator + out ) //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-source-path " + "\"" + directory + "\"") //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
			.append(" --add-exports mod.one/p=mod.two,mod.three")
			.append(" --add-reads mod.two=mod.one");

		runNegativeModuleTest(files,
			buffer,
			"",
			"----------\n" +
			"1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.one/p/X.java (at line 3)\n" +
			"	public static java.sql.Connection getConnection() {\n" +
			"	              ^^^^^^^^^^^^^^^^^^^\n" +
			"The type Connection from module java.sql may not be accessible to clients due to missing \'requires transitive\'\n" +
			"----------\n" +
			"----------\n" +
			"2. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.two/q/Y.java (at line 3)\n" +
			"	java.sql.Connection con = p.X.getConnection();\n" +
			"	^^^^^^^^^^^^^^^^^^^\n" +
			"The type java.sql.Connection is not accessible\n" +
			"----------\n" +
			"2 problems (1 error, 1 warning)\n",
			"visible");
	}
	public void test030() { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		String moduleLoc = directory + File.separator + "mod.one"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		List<String> files = new ArrayList<>(); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.one { \n" +
						"	requires java.base;\n" +
						"	requires java.sql;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "p", "X.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
						"package p;\n" +
						"public class X {\n" +
						"	public static java.sql.Connection getConnection() {\n" +
						"		return null;\n" +
						"	}\n" +
						"}");
		moduleLoc = directory + File.separator + "mod.two"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.two { \n" +
						"	requires java.base;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "q", "Y.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
						"package q;\n" +
						"import java.sql.*;\n" +
						"public class Y {\n" +
						"   Connection con = null;\n" +
						"}");

		StringBuilder buffer = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buffer.append("-d " + OUTPUT_DIR + File.separator + out ) //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" -warn:-exports") // getConnection() leaks non-transitively required type
			.append(" --module-source-path " + "\"" + directory + "\"") //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
			.append(" --add-exports mod.one/p=mod.two,mod.three")
			.append(" --add-reads mod.two=mod.one");

		runNegativeModuleTest(files,
			buffer,
			"",
			"----------\n"+
			"1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.two/q/Y.java (at line 2)\n"+
			"	import java.sql.*;\n"+
			"	       ^^^^^^^^\n"+
			"The package java.sql is not accessible\n"+
			"----------\n"+
			"2. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.two/q/Y.java (at line 4)\n"+
			"	Connection con = null;\n"+
			"	^^^^^^^^^^\n"+
			"Connection cannot be resolved to a type\n"+
			"----------\n"+
			"2 problems (2 errors)\n",
			"visible");
	}
	public void test031() { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		String moduleLoc = directory + File.separator + "mod.one"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		List<String> files = new ArrayList<>(); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.one { \n" +
						"	requires java.base;\n" +
						"	requires java.sql;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "p", "X.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
						"package p;\n" +
						"public class X {\n" +
						"	static java.sql.Connection getConnection() {\n" +
						"		return null;\n" +
						"	}\n" +
						"}");
		moduleLoc = directory + File.separator + "mod.two"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.two { \n" +
						"	requires java.base;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "q", "Y.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
						"package q;\n" +
						"import java.sql.Connection;\n" +
						"public class Y {\n" +
						"   Connection con = null;\n" +
						"}");

		StringBuilder buffer = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buffer.append("-d " + OUTPUT_DIR + File.separator + out ) //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-source-path " + "\"" + directory + "\"") //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
			.append(" --add-exports mod.one/p=mod.two,mod.three")
			.append(" --add-reads mod.two=mod.one");

		runNegativeModuleTest(files,
			buffer,
			"",
			"----------\n"+
			"1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.two/q/Y.java (at line 2)\n"+
			"	import java.sql.Connection;\n"+
			"	       ^^^^^^^^^^^^^^^^^^^\n"+
			"The type java.sql.Connection is not accessible\n"+
			"----------\n"+
			"2. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.two/q/Y.java (at line 4)\n"+
			"	Connection con = null;\n"+
			"	^^^^^^^^^^\n"+
			"Connection cannot be resolved to a type\n"+
			"----------\n"+
			"2 problems (2 errors)\n",
			"visible");
	}
	public void test032() { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		String moduleLoc = directory + File.separator + "mod.one"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		List<String> files = new ArrayList<>(); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.one { \n" +
						"	requires java.base;\n" +
						"}");
		writeFileCollecting(files, moduleLoc+"/p", "X.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
						"package p;\n" +
						"public class X {\n" +
						"	public static class Inner {\n" +
						"	}\n" +
						"}");

		StringBuilder buffer = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buffer.append("-d " + OUTPUT_DIR + File.separator + out ) //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-source-path " + "\"" + directory + "\""); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT

		runConformModuleTest(files,
			buffer,
			"",
			"");
	}
	/**
	 * Test that a module can't access types/packages in a plain Jar put in classpath
	 */
	public void test033() { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		File libDir = new File(LIB_DIR);
		Util.delete(libDir); // make sure we recycle the libs
 		libDir.mkdirs();
		try {
			Util.createJar(
				new String[] {
					"a/A.java",
					"package a;\n" +
					"public class A {\n" +
					"}"
				},
				LIB_DIR + "/lib1.jar", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
				JavaCore.VERSION_9);
		} catch (IOException e) {
			// ignore
		}
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		String moduleLoc = directory + File.separator + "mod.one"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		List<String> files = new ArrayList<>(); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.one { \n" +
						"	requires java.base;\n" +
						"	requires java.sql;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "p", "X.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
						"package p;\n" +
						"public class X extends a.A {\n" +
						"}");

		StringBuilder buffer = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buffer.append("-d " + OUTPUT_DIR + File.separator + out ) //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append(LIB_DIR).append(File.separator).append("lib1.jar").append(File.pathSeparator).append("\" ")
			.append(" --module-source-path " + "\"" + directory + "\""); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		runNegativeModuleTest(files,
				buffer,
				"",
				"----------\n" +
				"1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.one/p/X.java (at line 2)\n" +
				"	public class X extends a.A {\n" +
				"	                       ^^^\n" +
				"The type a.A is not accessible\n" +
				"----------\n" +
				"1 problem (1 error)\n",
				"package a does not exist");
	}
	/**
	 * Test that a module can't access types/packages in a plain Jar put in modulepath
	 * but not explicitly added to the "requires" clause
	 */
	public void test034() { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		File libDir = new File(LIB_DIR);
		Util.delete(libDir); // make sure we recycle the libs
 		libDir.mkdirs();
		try {
			Util.createJar(
				new String[] {
					"a/A.java",
					"package a;\n" +
					"public class A {\n" +
					"}"
				},
				LIB_DIR + "/lib1.jar", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
				JavaCore.VERSION_9);
		} catch (IOException e) {
			// ignore
		}
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		String moduleLoc = directory + File.separator + "mod.one"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		List<String> files = new ArrayList<>(); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.one { \n" +
						"	requires java.base;\n" +
						"	requires java.sql;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "p", "X.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
						"package p;\n" +
						"public class X extends a.A {\n" +
						"}");

		StringBuilder buffer = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buffer.append("-d " + OUTPUT_DIR + File.separator + out ) //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString()).append("\" ")
			.append("-p \"")
			.append(LIB_DIR).append("\" ")
			.append(" --module-source-path " + "\"" + directory + "\""); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		runNegativeModuleTest(files,
				buffer,
				"",
				"----------\n" +
				"1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.one/p/X.java (at line 2)\n" +
				"	public class X extends a.A {\n" +
				"	                       ^^^\n" +
				"The type a.A is not accessible\n" +
				"----------\n" +
				"1 problem (1 error)\n",
				"does not read");
	}
	/**
	 * Test that a module can access types/packages in a plain Jar put in modulepath
	 * and explicitly added to the "requires" clause
	 */
	public void test035() { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		File libDir = new File(LIB_DIR);
		Util.delete(libDir); // make sure we recycle the libs
 		libDir.mkdirs();
		try {
			Util.createJar(
				new String[] {
					"a/A.java",
					"package a;\n" +
					"public class A {\n" +
					"}"
				},
				LIB_DIR + "/lib1.jar", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
				JavaCore.VERSION_9);
		} catch (IOException e) {
			// ignore
		}
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		String moduleLoc = directory + File.separator + "mod.one"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		List<String> files = new ArrayList<>(); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.one { \n" +
						"	requires java.base;\n" +
						"	requires java.sql;\n" +
						"	requires lib1;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "p", "X.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
						"package p;\n" +
						"public class X extends a.A {\n" +
						"}");

		StringBuilder buffer = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buffer.append("-d " + OUTPUT_DIR + File.separator + out ) //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString()).append("\" ")
			.append("-p \"")
			.append(LIB_DIR).append("\" ")
			.append(" -warn:-module ")
			.append(" --module-source-path " + "\"" + directory + "\""); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		runConformModuleTest(files,
				buffer,
				"",
				"");
	}
	public void testBug515985() { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT

		String moduleLoc = directory + File.separator + "mod.one"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		List<String> files = new ArrayList<>(); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.one { \n" +
						"	exports pm;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "impl", "Other.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
						"package impl;\n" +
						"public class Other {\n" +
						"    public void privateMethod() {}" +
						"}\n");
		writeFileCollecting(files, moduleLoc + File.separator + "pm", "C1.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
						"package pm;\n" +
						"import impl.Other;\n" +
						"public class C1 extends Other {\n" +
						"}\n");

		moduleLoc = directory + File.separator + "mod.two"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.two { \n" +
						"	requires mod.one;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "po", "Client.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
						"package po;\n" +
						"import pm.C1;\n" +
						"public class Client {\n" +
						"    void test1(C1 one) {\n" +
						"        one.privateMethod(); // ecj: The method privateMethod() is undefined for the type C1\n" +
						"    }\n" +
						"}\n");

		StringBuilder buffer = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buffer.append("-d " + OUTPUT_DIR + File.separator + out ) //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-source-path " + "\"" + directory + "\""); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT

		runConformModuleTest(files,
				buffer,
				"",
				"");
	}

	public void testApiLeak1() { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT

		String moduleLoc = directory + File.separator + "mod.one"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		List<String> files = new ArrayList<>(); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.one { \n" +
						"	exports pm;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "impl", "Other.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
						"package impl;\n" +
						"public class Other {\n" +
						"}\n");
		writeFileCollecting(files, moduleLoc + File.separator + "pm", "C1.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
						"package pm;\n" +
						"import impl.Other;\n" +
						"public class C1 extends Other {\n" +
						"	public void m1(Other o) {}\n" +
						"}\n");

		moduleLoc = directory + File.separator + "mod.two"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.two { \n" +
						"	requires mod.one;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "impl", "Other.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
						"package impl;\n" +
						"public class Other {\n" +
						"}\n");
		writeFileCollecting(files, moduleLoc + File.separator + "po", "Client.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
						"package po;\n" +
						"import pm.C1;\n" +
						"public class Client {\n" +
						"    void test1(C1 one) {\n" +
						"        one.m1(one);\n" +
						"    }\n" +
						"}\n");

		StringBuilder buffer = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buffer.append("-d " + OUTPUT_DIR + File.separator + out ) //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-source-path " + "\"" + directory + "\""); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT

		runConformModuleTest(files,
				buffer,
				"",
				"----------\n" +
				"1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.one/pm/C1.java (at line 4)\n" +
				"	public void m1(Other o) {}\n" +
				"	               ^^^^^\n" +
				"The type Other is not exported from this module\n" +
				"----------\n" +
				"1 problem (1 warning)\n");
	}

	/**
	 * Same-named classes should not conflict, since one is not accessible.
	 * Still a sub class of the inaccessible class can be accessed and used for a method argument.
	 */
	public void testApiLeak2() { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT

		String moduleLoc = directory + File.separator + "mod.one"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		List<String> files = new ArrayList<>(); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.one { \n" +
						"	exports pm;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "impl", "SomeImpl.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
						"package impl;\n" +
						"public class SomeImpl {\n" +
						"}\n");
		writeFileCollecting(files, moduleLoc + File.separator + "pm", "C1.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
						"package pm;\n" +
						"import impl.SomeImpl;\n" +
						"public class C1 {\n" +
						"	public void m1(SomeImpl o) {}\n" +
						"}\n");
		writeFileCollecting(files, moduleLoc + File.separator + "pm", "Other.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
						"package pm;\n" +
						"import impl.SomeImpl;\n" +
						"public class Other extends SomeImpl {\n" +
						"}\n");

		moduleLoc = directory + File.separator + "mod.two"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.two { \n" +
						"	requires mod.one;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "impl", "SomeImpl.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
						"package impl;\n" +
						"public class SomeImpl {\n" + // pseudo-conflict to same named, but inaccessible class from mod.one
						"}\n");
		writeFileCollecting(files, moduleLoc + File.separator + "po", "Client.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
						"package po;\n" +
						"import pm.C1;\n" +
						"import pm.Other;\n" +
						"import impl.SomeImpl;\n" +
						"public class Client {\n" +
						"    void test1(C1 one) {\n" +
						"		 SomeImpl impl = new SomeImpl();\n" + // our own version
						"        one.m1(impl);\n" + // incompatible to what's required
						"		 one.m1(new Other());\n" + // OK
						"    }\n" +
						"}\n");

		StringBuilder buffer = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buffer.append("-d " + OUTPUT_DIR + File.separator + out ) //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" -info:+exports")
			.append(" --module-source-path " + "\"" + directory + "\""); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT

		runNegativeModuleTest(files,
				buffer,
				"",
				"----------\n" +
				"1. INFO in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.one/pm/C1.java (at line 4)\n" +
				"	public void m1(SomeImpl o) {}\n" +
				"	               ^^^^^^^^\n" +
				"The type SomeImpl is not exported from this module\n" +
				"----------\n" +
				"----------\n" +
				"2. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.two/po/Client.java (at line 8)\n" +
				"	one.m1(impl);\n" +
				"	    ^^\n" +
				"The method m1(impl.SomeImpl) in the type C1 is not applicable for the arguments (impl.SomeImpl)\n" +
				"----------\n" +
				"2 problems (1 error, 0 warnings, 1 info)\n",
				"incompatible");
	}

	// conflict even without any reference to the conflicting package
	// - three-way conflict between two direct and one indirect dependency
	public void testPackageConflict0() { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT

		String moduleLoc = directory + File.separator + "mod.x"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		List<String> files = new ArrayList<>(); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.x { \n" +
						"	exports pm;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "pm", "C1x.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
						"package pm;\n" +
						"public class C1x {\n" +
						"}\n");

		moduleLoc = directory + File.separator + "mod.y"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.y { \n" +
						"	requires transitive mod.x;\n" +
						"}");

		moduleLoc = directory + File.separator + "mod.one"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.one { \n" +
						"	exports pm;\n" +
						"	exports p2;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "pm", "C1.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
						"package pm;\n" +
						"public class C1 {\n" +
						"}\n");
		writeFileCollecting(files, moduleLoc + File.separator + "p2", "C2.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
						"package p2;\n" +
						"public class C2 {\n" +
						"}\n");

		moduleLoc = directory + File.separator + "mod.two"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.two { \n" +
						"	exports pm;\n" +
						"	exports p2.sub;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "pm", "C3.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
						"package pm;\n" +
						"public class C3 {\n" +
						"}\n");
		writeFileCollecting(files, moduleLoc + File.separator + "p2" + File.separator + "sub", "C4.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
						"package p2.sub;\n" +
						"public class C4 {\n" +
						"}\n");

		moduleLoc = directory + File.separator + "mod.three"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.three { \n" +
						"	requires mod.one;\n" +
						"	requires mod.two;\n" +
						"	requires transitive mod.y;\n" +
						"}");

		StringBuilder buffer = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buffer.append("-d " + OUTPUT_DIR + File.separator + out ) //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-source-path " + "\"" + directory + "\""); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT

		runNegativeModuleTest(files,
				buffer,
				"",
				"----------\n" +
				"1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.three/module-info.java (at line 2)\n" +
				"	requires mod.one;\n" +
				"	^^^^^^^^^^^^^^^^\n" +
				"The package pm is accessible from more than one module: mod.one, mod.two, mod.x\n" +
				"----------\n" +
				"2. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.three/module-info.java (at line 3)\n" +
				"	requires mod.two;\n" +
				"	^^^^^^^^^^^^^^^^\n" +
				"The package pm is accessible from more than one module: mod.one, mod.two, mod.x\n" +
				"----------\n" +
				"3. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.three/module-info.java (at line 4)\n" +
				"	requires transitive mod.y;\n" +
				"	^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
				"The package pm is accessible from more than one module: mod.one, mod.two, mod.x\n" +
				"----------\n" +
				"3 problems (3 errors)\n",
				"reads package pm");
	}

	public void testPackageConflict1() { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT

		String moduleLoc = directory + File.separator + "mod.one"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		List<String> files = new ArrayList<>(); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.one { \n" +
						"	exports pm;\n" +
						"	exports p2;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "pm", "C1.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
						"package pm;\n" +
						"public class C1 {\n" +
						"}\n");
		writeFileCollecting(files, moduleLoc + File.separator + "p2", "C2.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
						"package p2;\n" +
						"public class C2 {\n" +
						"}\n");

		moduleLoc = directory + File.separator + "mod.two"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.two { \n" +
						"	exports pm;\n" +
						"	exports p2.sub;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "pm", "C3.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
						"package pm;\n" +
						"public class C3 {\n" +
						"}\n");
		writeFileCollecting(files, moduleLoc + File.separator + "p2" + File.separator + "sub", "C4.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
						"package p2.sub;\n" +
						"public class C4 {\n" +
						"}\n");

		moduleLoc = directory + File.separator + "mod.three"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.three { \n" +
						"	requires mod.one;\n" +
						"	requires mod.two;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "po", "Client.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
						"package po;\n" +
						"import pm.*;\n" +
						"import pm.C3;\n" +
						"import p2.C2;\n" +
						"public class Client {\n" +
						"    void test1(C1 one) {\n" +
						"    }\n" +
						"	 pm.C1 f1;\n" +
						"	 p2.sub.C4 f4;\n" + // no conflict mod.one/p2 <-> mod.two/p2.sub
						"}\n");

		StringBuilder buffer = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buffer.append("-d " + OUTPUT_DIR + File.separator + out ) //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-source-path " + "\"" + directory + "\""); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT

		runNegativeModuleTest(files,
				buffer,
				"",
				"----------\n" +
				"1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.three/module-info.java (at line 2)\n" +
				"	requires mod.one;\n" +
				"	^^^^^^^^^^^^^^^^\n" +
				"The package pm is accessible from more than one module: mod.one, mod.two\n" +
				"----------\n" +
				"2. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.three/module-info.java (at line 3)\n" +
				"	requires mod.two;\n" +
				"	^^^^^^^^^^^^^^^^\n" +
				"The package pm is accessible from more than one module: mod.one, mod.two\n" +
				"----------\n" +
				"----------\n" +
				"3. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.three/po/Client.java (at line 2)\n" +
				"	import pm.*;\n" +
				"	       ^^\n" +
				"The package pm is accessible from more than one module: mod.one, mod.two\n" +
				"----------\n" +
				"4. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.three/po/Client.java (at line 3)\n" +
				"	import pm.C3;\n" +
				"	       ^^\n" +
				"The package pm is accessible from more than one module: mod.one, mod.two\n" +
				"----------\n" +
				"5. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.three/po/Client.java (at line 8)\n" +
				"	pm.C1 f1;\n" +
				"	^^\n" +
				"The package pm is accessible from more than one module: mod.one, mod.two\n" +
				"----------\n" +
				"5 problems (5 errors)\n",
				"reads package pm");
	}
	// conflict foreign<->local package
	public void testPackageConflict3() { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT

		List<String> files = new ArrayList<>(); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW
		String moduleLoc = directory + File.separator + "mod.one"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.one { \n" +
						"	exports pm;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "pm", "C1.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
						"package pm;\n" +
						"public class C1 {\n" +
						"}\n");

		moduleLoc = directory + File.separator + "mod.two"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.two { \n" +
						"	requires mod.one;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "pm", "C3.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
						"package pm;\n" +
						"public class C3 {\n" +
						"}\n");

		StringBuilder buffer = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buffer.append("-d " + OUTPUT_DIR + File.separator + out ) //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-source-path " + "\"" + directory + "\""); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT

		runNegativeModuleTest(files,
				buffer,
				"",
				"----------\n" +
				"1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.two/pm/C3.java (at line 1)\n" +
				"	package pm;\n" +
				"	        ^^\n" +
				"The package pm conflicts with a package accessible from another module: mod.one\n" +
				"----------\n" +
				"1 problem (1 error)\n",
				"");
	}
	public void testPackageConflict4() { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		File srcDir = new File(directory);

		String moduleLoc = directory + File.separator + "mod.x"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		List<String> files = new ArrayList<>(); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.x { \n" +
						"	exports pm;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "pm", "C1.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
						"package pm;\n" +
						"public class C1 {\n" +
						"}\n");

		moduleLoc = directory + File.separator + "mod.y"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.y { \n" +
						"	exports pm;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "pm", "C1.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
						"package pm;\n" +
						"public class C1 {\n" +
						"}\n");

		StringBuilder buffer = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buffer.append("-d " + OUTPUT_DIR + File.separator + out ) //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-source-path " + "\"" + directory + "\""); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT

		runConformModuleTest(files,
				buffer,
				"",
				"");
		Util.flushDirectoryContent(srcDir);
		files.clear();
		writeFileCollecting(files, directory + File.separator + "p", "X.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
						"public class X extends pm.C1 { \n" +
						"}");
		buffer = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buffer.append("-d " + OUTPUT_DIR + File.separator + out ) //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-path " + "\"" + OUTPUT_DIR + File.separator + out + "\""); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		runNegativeModuleTest(files,
				buffer,
				"",
				"----------\n" +
				"1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/p/X.java (at line 1)\n" +
				"	public class X extends pm.C1 { \n" +
				"	                       ^^\n" +
				"The package pm is accessible from more than one module: mod.x, mod.y\n" +
				"----------\n" +
				"1 problem (1 error)\n",
				"package conflict");
	}
	/**
	 * currently disabled because ECJ allows unnamed modules to read from other modules from
	 * module-path even if they are not part of root modules.
	 */
	public void _testPackageConflict4a() { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		File srcDir = new File(directory);

		String moduleLoc = directory + File.separator + "mod.x"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		List<String> files = new ArrayList<>(); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.x { \n" +
						"	exports pm;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "pm", "C1.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
						"package pm;\n" +
						"public class C1 {\n" +
						"}\n");

		StringBuilder buffer = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buffer.append("-d " + OUTPUT_DIR + File.separator + out ) //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-source-path " + "\"" + directory + "\""); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT

		runConformModuleTest(files,
				buffer,
				"",
				"");
		Util.flushDirectoryContent(srcDir);
		files.clear();
		writeFileCollecting(files, directory + File.separator + "p", "X.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
						"public class X extends pm.C1 { \n" +
						"}");
		buffer = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buffer.append("-d " + OUTPUT_DIR + File.separator + out ) //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-path " + "\"" + OUTPUT_DIR + File.separator + out + "\""); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		runNegativeModuleTest(files,
				buffer,
				"",
				"----------\n" +
				"1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/p/X.java (at line 1)\n" +
				"	public class X extends pm.C1 { \n" +
				"	                       ^^\n" +
				"pm cannot be resolved to a type\n" +
				"----------\n" +
				"1 problem (1 error)\n",
				"package conflict");
	}
	public void testPackageConflict5() { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		File srcDir = new File(directory);

		String moduleLoc = directory + File.separator + "mod.x"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		List<String> files = new ArrayList<>(); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.x { \n" +
						"	exports pm;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "pm", "C1.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
						"package pm;\n" +
						"public class C1 {\n" +
						"}\n");

		moduleLoc = directory + File.separator + "mod.y"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.y { \n" +
						"	exports pm;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "pm", "C1.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
						"package pm;\n" +
						"public class C1 {\n" +
						"}\n");

		StringBuilder buffer = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buffer.append("-d " + OUTPUT_DIR + File.separator + out ) //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-source-path " + "\"" + directory + "\""); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT

		runConformModuleTest(files,
				buffer,
				"",
				"");
		Util.flushDirectoryContent(srcDir);
		files.clear();
		writeFileCollecting(files, directory + File.separator + "p", "X.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
						"public class X extends pm.C1 { \n" +
						"}");
		buffer = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buffer.append("-d " + OUTPUT_DIR + File.separator + out ) //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-path " + "\"" + OUTPUT_DIR + File.separator + out + "\"") //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
			.append(" --add-modules mod.x,mod.y");
		runNegativeModuleTest(files,
				buffer,
				"",
				"The package pm is accessible from more than one module: mod.y, mod.x\n",
				"reads package pm from both");
		buffer = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buffer.append("-d " + OUTPUT_DIR + File.separator + out ) //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-path " + "\"" + OUTPUT_DIR + File.separator + out + "\"") //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
			.append(" --add-modules mod.x,mod.z");
		runNegativeModuleTest(files,
				buffer,
				"",
				"invalid module name: mod.z\n",
				"module not found");
	}
	public void testPackageConflict6() { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		File srcDir = new File(directory);

		String moduleLoc = directory + File.separator + "mod.x"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		List<String> files = new ArrayList<>(); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.x { \n" +
						"	exports pm;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "pm", "C1.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
						"package pm;\n" +
						"public class C1 {\n" +
						"}\n");

		moduleLoc = directory + File.separator + "mod.y"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.y { \n" +
						"	exports pm;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "pm", "C1.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
						"package pm;\n" +
						"public class C1 {\n" +
						"}\n");

		StringBuilder buffer = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buffer.append("-d " + OUTPUT_DIR + File.separator + out ) //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-source-path " + "\"" + directory + "\""); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT

		runConformModuleTest(files,
				buffer,
				"",
				"");
		Util.flushDirectoryContent(srcDir);
		files.clear();
		writeFileCollecting(files, directory + File.separator + "p", "X.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
						"public class X extends pm.C1 { \n" +
						"}");
		buffer = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buffer.append("-d " + OUTPUT_DIR + File.separator + out ) //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-path " + "\"" + OUTPUT_DIR + File.separator + out + "\"") //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
			.append(" --add-modules mod.x,");
		runNegativeModuleTest(files,
				buffer,
				"",
				"----------\n" +
				"1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/p/X.java (at line 1)\n" +
				"	public class X extends pm.C1 { \n" +
				"	                       ^^\n" +
				"The package pm is accessible from more than one module: mod.x, mod.y\n" +
				"----------\n" +
				"1 problem (1 error)\n",
				"package conflict");
	}
	public void testPackageConflict7() { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		File srcDir = new File(directory);

		String moduleLoc = directory + File.separator + "mod.x"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		List<String> files = new ArrayList<>(); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.x { \n" +
						"	exports pm;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "pm", "C1.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
						"package pm;\n" +
						"public class C1 {\n" +
						"}\n");

		moduleLoc = directory + File.separator + "mod.y"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.y { \n" +
						"	exports pm;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "pm", "C1.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
						"package pm;\n" +
						"public class C1 {\n" +
						"}\n");

		StringBuilder buffer = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buffer.append("-d " + OUTPUT_DIR + File.separator + out ) //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-source-path " + "\"" + directory + "\""); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT

		runConformModuleTest(files,
				buffer,
				"",
				"");
		Util.flushDirectoryContent(srcDir);
		files.clear();
		writeFileCollecting(files, directory + File.separator + "p", "X.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
						"public class X { \n" +
						"}");
		buffer = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buffer.append("-d " + OUTPUT_DIR + File.separator + out ) //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-path " + "\"" + OUTPUT_DIR + File.separator + out + "\"") //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
			.append(" --add-modules mod.x,mod.y");
		runNegativeModuleTest(files,
				buffer,
				"",
				"The package pm is accessible from more than one module: mod.y, mod.x\n",
				"reads package pm from both");
	}
	public void testPackageTypeConflict1() { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT

		String moduleLoc = directory + File.separator + "mod.one"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		List<String> files = new ArrayList<>(); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.one { \n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "p1" + File.separator + "p2", "t3.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
						"package p1.p2;\n" +
						"public class t3 {\n" +
						"}\n");

		moduleLoc = directory + File.separator + "mod.two"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.two { \n" +
						"	exports p1.p2.t3;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "p1" + File.separator + "p2" + File.separator + "t3", "t4.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
						"package p1.p2.t3;\n" +
						"public class t4 {\n" +
						"}\n");

		moduleLoc = directory + File.separator + "mod.three"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.three { \n" +
						"	requires mod.one;\n" +
						"	requires mod.two;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "po", "Client.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
						"package po;\n" +
						"public class Client {\n" +
						"	 p1.p2.t3.t4 f;\n" + // no conflict mod.one/p1.p2.t3 <-> mod.two/p1.p2.t3
						"}\n");

		StringBuilder buffer = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buffer.append("-d " + OUTPUT_DIR + File.separator + out ) //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-source-path " + "\"" + directory + "\""); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT

		runConformModuleTest(files,
				buffer,
				"",
				"");
	}

	public void testBug519922() { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT

		List<String> files = new ArrayList<>(); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW
		writeFileCollecting(files, directory + File.separator + "test", "Test.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
						"package test;\n" +
						"\n" +
						"public class Test implements org.eclipse.SomeInterface {\n" +
						"}\n");

		StringBuilder buffer = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buffer.append("-d " + OUTPUT_DIR + File.separator + out ) //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ");

		runNegativeModuleTest(files,
				buffer,
				"",
				"----------\n" +
				"1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/test/Test.java (at line 3)\n" +
				"	public class Test implements org.eclipse.SomeInterface {\n" +
				"	                             ^^^^^^^^^^^\n" +
				"org.eclipse cannot be resolved to a type\n" +
				"----------\n" +
				"1 problem (1 error)\n",
				"does not exist");
	}
	public void testMixedSourcepath() { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		String moduleLoc = directory + File.separator + "mod" + File.separator + "mod.one"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT

		List<String> files = new ArrayList<>(); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.one {\n" +
						"	exports p.q;\n" +
						"}");

		StringBuilder buffer = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buffer.append("-d " + OUTPUT_DIR + File.separator + out ) //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\"")
			.append(" -sourcepath \"" + directory + "\" ") //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
			.append(" --module-source-path \"" + directory + File.separator + "mod" + "\" ") //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
			.append(" --add-reads mod.one=ALL-UNNAMED ");

		runNegativeModuleTest(files,
				buffer,
				"",
				"cannot specify both -source-path and --module-source-path\n",
				"cannot specify both");
	}

	// causes: non-public type (C0), non-exported package (p.priv)
	// locations: field, method parameter, method return
	public void testAPILeakDetection1() { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT

		String moduleLoc = directory + File.separator + "mod.one"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		List<String> files = new ArrayList<>(); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.one { \n" +
						"	exports p.exp;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "p" + File.separator + "exp", "C1.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
						"package p.exp;\n" +
						"import p.priv.*;\n" +
						"class C0 {\n" +
						"	public void test(C0 c) {}\n" +
						"}\n" +
						"public class C1 {\n" +
						"	public C2 f;\n" +
						"	public void test1(C0 c) {}\n" +
						"	public void test2(C2 c) {}\n" +
						"	protected void test3(C0 c) {}\n" +
						"	protected void test4(C2 c) {}\n" +
						"	public p.priv.C2 test5() { return null; }\n" +
						"}\n");

		writeFileCollecting(files, moduleLoc + File.separator + "p" + File.separator + "priv", "C2.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
						"package p.priv;\n" +
						"public class C2 {\n" +
						"}\n");

		StringBuilder buffer = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buffer.append("-d " + OUTPUT_DIR + File.separator + out ) //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" -err:exports")
			.append(" --module-source-path " + "\"" + directory + "\""); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT

		runNegativeModuleTest(files, buffer,
				"",
				"----------\n" +
				"1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.one/p/exp/C1.java (at line 7)\n" +
				"	public C2 f;\n" +
				"	       ^^\n" +
				"The type C2 is not exported from this module\n" +
				"----------\n" +
				"2. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.one/p/exp/C1.java (at line 8)\n" +
				"	public void test1(C0 c) {}\n" +
				"	                  ^^\n" +
				"The type C0 is not accessible to clients that require this module\n" +
				"----------\n" +
				"3. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.one/p/exp/C1.java (at line 9)\n" +
				"	public void test2(C2 c) {}\n" +
				"	                  ^^\n" +
				"The type C2 is not exported from this module\n" +
				"----------\n" +
				"4. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.one/p/exp/C1.java (at line 12)\n" +
				"	public p.priv.C2 test5() { return null; }\n" +
				"	       ^^^^^^^^^\n" +
				"The type C2 is not exported from this module\n" +
				"----------\n" +
				"4 problems (4 errors)\n",
				"is not exported");
	}

	// details: in array, parameterized type
	public void testAPILeakDetection2() { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT

		String moduleLoc = directory + File.separator + "mod.one"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		List<String> files = new ArrayList<>(); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.one { \n" +
						"	exports p.exp;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "p" + File.separator + "exp", "C1.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
						"package p.exp;\n" +
						"import java.util.*;\n" +
						"class C0 {\n" +
						"	public void test(C0 c) {}\n" +
						"}\n" +
						"public class C1 {\n" +
						"	public List<C0> f1;\n" +
						"	public C0[] f2;\n" +
						"}\n");

		StringBuilder buffer = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buffer.append("-d " + OUTPUT_DIR + File.separator + out ) //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" -err:+exports")
			.append(" --module-source-path " + "\"" + directory + "\""); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT

		runNegativeModuleTest(files, buffer,
				"",
				"----------\n" +
				"1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.one/p/exp/C1.java (at line 7)\n" +
				"	public List<C0> f1;\n" +
				"	            ^^\n" +
				"The type C0 is not accessible to clients that require this module\n" +
				"----------\n" +
				"2. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.one/p/exp/C1.java (at line 8)\n" +
				"	public C0[] f2;\n" +
				"	       ^^\n" +
				"The type C0 is not accessible to clients that require this module\n" +
				"----------\n" +
				"2 problems (2 errors)\n",
				"not accessible to clients");
	}

	// suppress
	public void testAPILeakDetection3() { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT

		String moduleLoc = directory + File.separator + "mod.one"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		List<String> files = new ArrayList<>(); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.one { \n" +
						"	exports p.exp;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "p" + File.separator + "exp", "C1.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
						"package p.exp;\n" +
						"import java.util.*;\n" +
						"class C0 {\n" +
						"	public void test(C0 c) {}\n" +
						"}\n" +
						"public class C1 {\n" +
						"	@SuppressWarnings(\"exports\")\n" +
						"	public List<C0> f1;\n" +
						"	@SuppressWarnings(\"exports\")\n" +
						"	public C0[] f2;\n" +
						"}\n");

		StringBuilder buffer = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buffer.append("-d " + OUTPUT_DIR + File.separator + out ) //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" -warn:+exports,+suppress")
			.append(" --module-source-path " + "\"" + directory + "\""); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT

		runConformModuleTest(files,
				buffer,
				"",
				"");
	}

	// details: nested types
	public void testAPILeakDetection4() { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT

		String moduleLoc = directory + File.separator + "mod.one"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		List<String> files = new ArrayList<>(); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.one { \n" +
						"	exports p.exp;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "p" + File.separator + "exp", "C1.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
						"package p.exp;\n" +
						"public class C1 {\n" +
						"	static class C3 {\n" +
						"		public static class C4 {}\n" + // public but nested in non-public
						"	}\n" +
						"	public C3 f1;\n" +
						"	public C3.C4 f2;\n" +
						"}\n");

		StringBuilder buffer = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buffer.append("-d " + OUTPUT_DIR + File.separator + out ) //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" -err:+exports")
			.append(" --module-source-path " + "\"" + directory + "\""); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT

		runNegativeModuleTest(files, buffer,
				"",
				"----------\n" +
				"1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.one/p/exp/C1.java (at line 6)\n" +
				"	public C3 f1;\n" +
				"	       ^^\n" +
				"The type C1.C3 is not accessible to clients that require this module\n" +
				"----------\n" +
				"2. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.one/p/exp/C1.java (at line 7)\n" +
				"	public C3.C4 f2;\n" +
				"	       ^^^^^\n" +
				"The type C1.C3.C4 is not accessible to clients that require this module\n" +
				"----------\n" +
				"2 problems (2 errors)\n",
				"one is not accessible to clients");
	}

	// type from non-transitive required module
	public void testAPILeakDetection5() { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT

		String moduleLoc = directory + File.separator + "mod.one"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		List<String> files = new ArrayList<>(); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.one { \n" +
						"	exports p.exp1;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "p" + File.separator + "exp1", "C1.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
						"package p.exp1;\n" +
						"public class C1 {\n" +
						"}\n");

		moduleLoc = directory + File.separator + "mod.two"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.two { \n" +
						"	exports p.exp2;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "p" + File.separator + "exp2", "C2.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
						"package p.exp2;\n" +
						"public class C2 {\n" +
						"}\n");

		moduleLoc = directory + File.separator + "mod.three"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.three { \n" +
						"	requires mod.one; // missing transitive\n" +
						"	requires transitive mod.two;\n" +
						"	exports p.exp3;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "p" + File.separator + "exp3", "C3.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
						"package p.exp3;\n" +
						"public class C3 {\n" +
						"	public void m1(p.exp1.C1 arg) {}\n" +
						"	public void m2(p.exp2.C2 arg) {}\n" +
						"}\n");

		StringBuilder buffer = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buffer.append("-d " + OUTPUT_DIR + File.separator + out ) //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" -err:+exports")
			.append(" --module-source-path " + "\"" + directory + "\""); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT

		runNegativeModuleTest(files, buffer,
				"",
				"----------\n" +
				"1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.three/p/exp3/C3.java (at line 3)\n" +
				"	public void m1(p.exp1.C1 arg) {}\n" +
				"	               ^^^^^^^^^\n" +
				"The type C1 from module mod.one may not be accessible to clients due to missing \'requires transitive\'\n" +
				"----------\n" +
				"1 problem (1 error)\n",
				"is not indirectly exported");
	}

	// annotated types in API
	public void testAPILeakDetection6() { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT

		String moduleLoc = directory + File.separator + "mod.one"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		List<String> files = new ArrayList<>(); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.one { \n" +
						"	exports p.exp;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "p" + File.separator + "exp", "C1.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
						"package p.exp;\n" +
						"import java.lang.annotation.*;\n" +
						"@Target(ElementType.TYPE_USE)\n" +
						"@interface ANN {}\n" +
						"class C0 {}\n" +
						"public class C1 {\n" +
						"	public @ANN String f1;\n" +
						"	public @ANN C0 f3;\n" +
						"	public @ANN String test(@ANN String arg, @ANN C0 c) { return \"\"; }\n" +
						"}\n");

		StringBuilder buffer = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buffer.append("-d " + OUTPUT_DIR + File.separator + out ) //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" -err:exports")
			.append(" --module-source-path " + "\"" + directory + "\""); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT

		runNegativeModuleTest(files, buffer,
				"",
				"----------\n" +
				"1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.one/p/exp/C1.java (at line 8)\n" +
				"	public @ANN C0 f3;\n" +
				"	            ^^\n" +
				"The type C0 is not accessible to clients that require this module\n" +
				"----------\n" +
				"2. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.one/p/exp/C1.java (at line 9)\n" +
				"	public @ANN String test(@ANN String arg, @ANN C0 c) { return \"\"; }\n" +
				"	                                              ^^\n" +
				"The type C0 is not accessible to clients that require this module\n" +
				"----------\n" +
				"2 problems (2 errors)\n",
				"is not accessible to clients");
	}

	// enum API
	public void testAPILeakDetection7() { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT

		String moduleLoc = directory + File.separator + "mod.one"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		List<String> files = new ArrayList<>(); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.one { \n" +
						"	exports p.exp;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "p" + File.separator + "exp", "C1.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
						"package p.exp;\n" +
						"public enum C1 {\n" +
						"	X, Y, Z;\n" +
						"}\n");

		StringBuilder buffer = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buffer.append("-d " + OUTPUT_DIR + File.separator + out ) //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" -err:exports")
			.append(" --module-source-path " + "\"" + directory + "\""); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT

		runConformModuleTest(files, buffer,
				"",
				"");
	}

	public void testBug486013_comment27() { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT

		String projLoc = directory + File.separator + "Proj"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		List<String> files = new ArrayList<>(); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW
		writeFileCollecting(files, projLoc + File.separator + "p" + File.separator + "exp", "C1.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
						"package p.exp;\n" +
						"import java.util.*;\n" +
						"public class C1 {\n" +
						"	List<?> l;\n" +
						"}\n");

		StringBuilder buffer = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buffer.append("-d " + OUTPUT_DIR + File.separator + out ) //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" -err:exports");

		runConformModuleTest(files, buffer,
				"",
				"");
	}
	public void testBug518295a() { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		Util.flushDirectoryContent(new File(OUTPUT_DIR));
		String outDir = OUTPUT_DIR + File.separator + "bin"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		String srcDir = OUTPUT_DIR + File.separator + "src"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		File modDir = new File(OUTPUT_DIR + File.separator + "mod"); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		createReusableModules(srcDir, outDir, modDir);
		String moduleLoc = srcDir + File.separator + "mod.three"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		List<String> files = new ArrayList<>(); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.three { \n" +
						"	requires mod.one;\n" +
						"	requires mod.two;\n" +
						"}");

		StringBuilder buffer = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buffer.append("-d " + outDir ) //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		.append(" -9 ")
		.append(" -p \"")
		.append(Util.getJavaClassLibsAsString())
		.append(modDir.getAbsolutePath())
		.append("\" ")
		.append("-classNames mod.one/p.XYZ")
		.append(" --module-source-path " + "\"" + srcDir + "\""); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT

		files.forEach(name -> buffer.append(" \"" + name + "\"")); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		runNegativeTest(new String[0],
				buffer.toString(),
				"",
				"invalid class name: mod.one/p.XYZ\n",
				false);
	}
	public void testBug518295b() { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		Util.flushDirectoryContent(new File(OUTPUT_DIR));
		String outDir = OUTPUT_DIR + File.separator + "bin"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		String srcDir = OUTPUT_DIR + File.separator + "src"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		File modDir = new File(OUTPUT_DIR + File.separator + "mod"); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		createReusableModules(srcDir, outDir, modDir);
		String moduleLoc = srcDir + File.separator + "mod.three"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		List<String> files = new ArrayList<>(); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.three { \n" +
						"	requires mod.one;\n" +
						"	requires mod.two;\n" +
						"}");

		StringBuilder buffer = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buffer.append("-d " + outDir ) //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		.append(" -9 ")
		.append(" -p \"")
		.append(Util.getJavaClassLibsAsString())
		.append(modDir.getAbsolutePath())
		.append("\" ")
		.append("-classNames mod.xyz/p.X")
		.append(" --module-source-path " + "\"" + srcDir + "\""); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT

		files.forEach(name -> buffer.append(" \"" + name + "\"")); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		runNegativeTest(new String[0],
				buffer.toString(),
				"",
				"invalid module name: mod.xyz\n",
				false);
	}
	public void testBug518295c() { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		Util.flushDirectoryContent(new File(OUTPUT_DIR));
		String outDir = OUTPUT_DIR + File.separator + "bin"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		String srcDir = OUTPUT_DIR + File.separator + "src"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		File modDir = new File(OUTPUT_DIR + File.separator + "mod"); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		createReusableModules(srcDir, outDir, modDir);
		String moduleLoc = srcDir + File.separator + "mod.three"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		List<String> files = new ArrayList<>(); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.three { \n" +
						"	requires mod.one;\n" +
						"	requires mod.two;\n" +
						"}");

		StringBuilder buffer = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buffer.append("-d " + outDir ) //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		.append(" -9 ")
		.append(" -p \"")
		.append(Util.getJavaClassLibsAsString())
		.append(modDir.getAbsolutePath())
		.append("\" ")
		.append("-classNames mod.one/p.X")
		.append(" --module-source-path " + "\"" + srcDir + "\""); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT

		runConformModuleTest(files,
				buffer,
				"",
				"");
	}
	public void testUnnamedPackage_Bug520839() { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		String moduleLoc = directory + File.separator + "mod.one"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		List<String> files = new ArrayList<>(); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.one { \n" +
						"	requires java.base;\n" +
						"}");
		writeFileCollecting(files, moduleLoc, "X.java",
						"public class X {\n" +
						"}");

		StringBuilder buffer = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buffer.append("-d " + OUTPUT_DIR + File.separator + out ) //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-source-path " + "\"" + directory + "\""); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT

		runNegativeModuleTest(files,
			buffer,
			"",
			"----------\n" +
			"1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.one/X.java (at line 1)\n" +
			"	public class X {\n" +
			"	^\n" +
			"Must declare a named package because this compilation unit is associated to the named module \'mod.one\'\n" +
			"----------\n" +
			"1 problem (1 error)\n",
			"unnamed package is not allowed in named modules");
	}
	public void testAutoModule1() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);

		String[] sources = {
			"p/a/X.java",
			"package p.a;\n" +
			"public class X {}\n;"
		};
		String jarPath = OUTPUT_DIR + File.separator + "lib-x.jar"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		Util.createJar(sources, jarPath, "1.8");

		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		String moduleLoc = directory + File.separator + "mod.one"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		List<String> files = new ArrayList<>(); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.one { \n" +
						"	requires lib.x;\n" + // lib.x is derived from lib-x.jar
						"}");
		writeFileCollecting(files, moduleLoc+File.separator+"q", "X.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
						"package q;\n" +
						"public class X {\n" +
						"	p.a.X f;\n" +
						"}");

		StringBuilder buffer = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buffer.append("-d " + OUTPUT_DIR + File.separator + out ) //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" -info:+module ")
			.append(" --module-path " + "\"" + jarPath + "\""); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT

		runConformModuleTest(files,
			buffer,
			"",
			"----------\n" +
			"1. INFO in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.one/module-info.java (at line 2)\n" +
			"	requires lib.x;\n" +
			"	         ^^^^^\n" +
			"Name of automatic module \'lib.x\' is unstable, it is derived from the module\'s file name.\n" +
			"----------\n" +
			"1 problem (1 info)\n");
	}
	public void testBug521458a() { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		Util.flushDirectoryContent(new File(OUTPUT_DIR));
		String outDir = OUTPUT_DIR + File.separator + "bin"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		String srcDir = OUTPUT_DIR + File.separator + "src"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		File modDir = new File(OUTPUT_DIR + File.separator + "mod"); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		createReusableModules(srcDir, outDir, modDir);
		String moduleLoc = srcDir + File.separator + "mod.three"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		List<String> files = new ArrayList<>(); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod._3_ { \n" +
						"	requires mod.one;\n" +
						"	requires mod.two;\n" +
						"}");

		StringBuilder buffer = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buffer.append("-d " + outDir ) //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		.append(" -9 ")
		.append(" -p \"")
		.append(Util.getJavaClassLibsAsString())
		.append(modDir.getAbsolutePath())
		.append("\" ")
		.append("-classNames mod.one/p.X")
		.append(" --module-source-path " + "\"" + srcDir + "\""); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT

		runNegativeModuleTest(files,
				buffer,
				"",
				"module name mod._3_ does not match expected name mod.three\n",
				"does not match expected name");
	}
	/*
	 * Disabled because the parser seem to take the module path as mod and not mod.e
	 */
	public void _testBug521458b() { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		Util.flushDirectoryContent(new File(OUTPUT_DIR));
		String outDir = OUTPUT_DIR + File.separator + "bin"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		String srcDir = OUTPUT_DIR + File.separator + "src"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		File modDir = new File(OUTPUT_DIR + File.separator + "mod"); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		createReusableModules(srcDir, outDir, modDir);
		String moduleLoc = srcDir + File.separator + "mod.three"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		List<String> files = new ArrayList<>(); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.3 { \n" +
						"	requires mod.one;\n" +
						"	requires mod.two;\n" +
						"}");

		StringBuilder buffer = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buffer.append("-d " + outDir ) //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		.append(" -9 ")
		.append(" -p \"")
		.append(Util.getJavaClassLibsAsString())
		.append(modDir.getAbsolutePath())
		.append("\" ")
		.append("-classNames mod.one/p.X")
		.append(" --module-source-path " + "\"" + srcDir + "\""); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT

		runNegativeModuleTest(files,
				buffer,
				"",
				"module name mod.3 does not match expected name mod.three\r\n",
				outDir);
	}
public void testBug521362_emptyFile() { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		String moduleLoc = directory + File.separator + "mod.one"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		List<String> files = new ArrayList<>(); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.one { \n" +
						"	exports p1;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "p1", "X.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
						"");

		StringBuilder buffer = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buffer.append("-d " + OUTPUT_DIR + File.separator + out ) //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-source-path " + "\"" + directory + "\""); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT

		runNegativeModuleTest(files,
			buffer,
			"",
			"----------\n" +
			"1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.one/module-info.java (at line 2)\n" +
			"	exports p1;\n" +
			"	        ^^\n" +
			"The package p1 does not exist or is empty\n" +
			"----------\n" +
			"1 problem (1 error)\n",
			"empty");
	}
	public void testBug521362_mismatchingdeclaration() { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		String moduleLoc = directory + File.separator + "mod.one"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		List<String> files = new ArrayList<>(); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.one { \n" +
						"	exports p1;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "p1", "X.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
						"package q;\n");

		StringBuilder buffer = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buffer.append("-d " + OUTPUT_DIR + File.separator + out ) //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-source-path " + "\"" + directory + "\""); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT

		runNegativeModuleTest(files,
			buffer,
			"",
			"----------\n" +
			"1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.one/module-info.java (at line 2)\n" +
			"	exports p1;\n" +
			"	        ^^\n" +
			"The package p1 does not exist or is empty\n" +
			"----------\n" +
			"1 problem (1 error)\n",
			"package is empty");
	}
	public void testBug521362_multiplePackages() { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		String moduleLoc = directory + File.separator + "mod.one"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		List<String> files = new ArrayList<>(); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.one { \n" +
						"	exports p1;\n" +
						"	exports p2;\n" +
						"	exports p3;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "p1", "X.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
						"package q;\n");
		writeFileCollecting(files, moduleLoc + File.separator + "p2", "X.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
				"package q2;\n");
		writeFileCollecting(files, moduleLoc + File.separator + "p3", "X.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
				"package p3;\n");
		StringBuilder buffer = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buffer.append("-d " + OUTPUT_DIR + File.separator + out ) //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-source-path " + "\"" + directory + "\""); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT

		runNegativeModuleTest(files,
			buffer,
			"",
			"----------\n" +
			"1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.one/module-info.java (at line 2)\n" +
			"	exports p1;\n" +
			"	        ^^\n" +
			"The package p1 does not exist or is empty\n" +
			"----------\n" +
			"2. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.one/module-info.java (at line 3)\n" +
			"	exports p2;\n" +
			"	        ^^\n" +
			"The package p2 does not exist or is empty\n" +
			"----------\n" +
			"2 problems (2 errors)\n",
			"package is empty");
	}
	public void testBug521362_multiplePackages2() { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		String moduleLoc = directory + File.separator + "mod.one"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		List<String> files = new ArrayList<>(); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.one { \n" +
						"	exports p1;\n" +
						"	exports p2;\n" +
						"	exports p3.p4.p5;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "p1", "X.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
						"package q;\n");
		writeFileCollecting(files, moduleLoc + File.separator + "p2", "X.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
				"package q2;\n");
		writeFileCollecting(files, moduleLoc + File.separator + "p3" + File.separator + "p4" + File.separator + "p5", "X.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
				"package p3.p4.p5;\n");
		StringBuilder buffer = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buffer.append("-d " + OUTPUT_DIR + File.separator + out ) //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-source-path " + "\"" + directory + "\""); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT

		runNegativeModuleTest(files,
			buffer,
			"",
			"----------\n" +
			"1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.one/module-info.java (at line 2)\n" +
			"	exports p1;\n" +
			"	        ^^\n" +
			"The package p1 does not exist or is empty\n" +
			"----------\n" +
			"2. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.one/module-info.java (at line 3)\n" +
			"	exports p2;\n" +
			"	        ^^\n" +
			"The package p2 does not exist or is empty\n" +
			"----------\n" +
			"2 problems (2 errors)\n",
			"package is empty");
	}
	/*
	 * Test that when module-info is the only file being compiled, the class is still
	 * generated inside the module's sub folder.
	 */
	public void testBug500170a() { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String directory = getSourceDir();
		String moduleLoc = directory + File.separator + "mod.one"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		List<String> files = new ArrayList<>(); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.one { \n" +
						"	requires java.sql;\n" +
						"}");
		StringBuilder buffer = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buffer.append("-d " + getEcjOutputDir() ) //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-source-path " + "\"" + directory + "\" ") //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
			.append(moduleLoc + File.separator + "module-info.java"); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT

		Set<String> classFiles = runConformModuleTest(
				new String[0],
				buffer.toString(),
				"",
				"",
				false);
		String fileName = getEcjOutputDir() + File.separator + "mod.one" + File.separator + "module-info.class"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		assertClassFile("Missing modul-info.class: " + fileName, fileName, classFiles); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
	}
	/*
	 * Test that no NPE is thrown when the module-info is compiled at a level below 9
	 */
	public void testBug500170b() { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		String moduleLoc = directory + File.separator + "mod.one"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		List<String> files = new ArrayList<>(); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.one { \n" +
						"	requires java.sql;\n" +
						"}");
		StringBuilder buffer = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buffer.append("-d " + OUTPUT_DIR + File.separator + out ) //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			.append(" -8")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ");

		runNegativeModuleTest(files,
				buffer,
				"",
				"""
				----------
				1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.one/module-info.java (at line 1)
					module mod.one {\s
					^^^^^^
				Syntax error on token(s), misplaced construct(s)
				----------
				2. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.one/module-info.java (at line 1)
					module mod.one {\s
					^^^^^^^^^^^^^^
				Syntax error on token(s), misplaced construct(s)
				----------
				3. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.one/module-info.java (at line 2)
					requires java.sql;
					             ^
				Syntax error on token ".", , expected
				----------
				4. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.one/module-info.java (at line 3)
					}
					^
				Syntax error on token "}", delete this token
				----------
				4 problems (4 errors)
				""",
				"modules are not supported");
	}
	public void testBug522472c() { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		File srcDir = new File(directory);
		String moduleLoc = directory + File.separator + "mod.one"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		List<String> files = new ArrayList<>(); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW
		writeFileCollecting(files, moduleLoc,
				"module-info.java",
				"module mod.one { \n" +
				"	exports x.y.z;\n" +
				"	exports a.b.c;\n" +
				"}");
		writeFileCollecting(files, moduleLoc + File.separator + "x" + File.separator + "y" + File.separator + "z", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
				"X.java",
				"package x.y.z;\n");
		writeFileCollecting(files, moduleLoc + File.separator + "a" + File.separator + "b" + File.separator + "c", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
				"A.java",
				"package a.b.c;\n" +
				"public class A {}");

		moduleLoc = directory + File.separator + "mod.one.a"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		writeFileCollecting(files, moduleLoc,
				"module-info.java",
				"module mod.one.a { \n" +
				"	exports x.y.z;\n" +
				"}");
		writeFileCollecting(files, moduleLoc + File.separator + "x" + File.separator + "y" + File.separator + "z", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
				"X.java",
				"package x.y.z;\n" +
				"public class X {}\n");
		StringBuilder buffer = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buffer.append("-d " + OUTPUT_DIR + File.separator + out ) //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-source-path " + "\"" + directory + "\" "); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT

		runConformModuleTest(files,
				buffer,
				"",
				"");

		Util.flushDirectoryContent(srcDir);
		files.clear();
		moduleLoc = directory + File.separator + "mod.two"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		writeFileCollecting(files, moduleLoc,
				"module-info.java",
				"module mod.two { \n" +
					"	requires mod.one;\n" +
					"	requires mod.one.a;\n" +
				"}");
		writeFileCollecting(files, moduleLoc + File.separator + "p" + File.separator + "q" + File.separator + "r", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
				"Main.java",
				"package p.q.r;\n" +
				"import a.b.c.*;\n" +
				"import x.y.z.*;\n" +
				"@SuppressWarnings(\"unused\")\n" +
				"public class Main {"
				+ "}");
		buffer = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buffer.append("-d " + OUTPUT_DIR + File.separator + out ) //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-path " + "\"" + OUTPUT_DIR + File.separator + out + "\" ") //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
			.append(" --module-source-path " + "\"" + directory + "\" "); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		runNegativeModuleTest(files,
				buffer,
				"",
				"----------\n" +
				"1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.two/module-info.java (at line 2)\n" +
				"	requires mod.one;\n" +
				"	^^^^^^^^^^^^^^^^\n" +
				"The package x.y.z is accessible from more than one module: mod.one, mod.one.a\n" +
				"----------\n" +
				"2. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.two/module-info.java (at line 3)\n" +
				"	requires mod.one.a;\n" +
				"	^^^^^^^^^^^^^^^^^^\n" +
				"The package x.y.z is accessible from more than one module: mod.one, mod.one.a\n" +
				"----------\n" +
				"2 problems (2 errors)\n",
				"module mod.two reads package x.y.z from both mod.one and mod.one.a");
	}
	public void testReleaseOption1() throws Exception {
		this.runConformTest(
				new String[] {
					"X.java",
					"/** */\n" +
					"public class X {\n" +
					"}",
				},
		     "\"" + OUTPUT_DIR +  File.separator + "X.java\"" //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		     + " --release 8 -d \"" + OUTPUT_DIR + "\"",
		     "",
		     "",
		     true);
		String expectedOutput = "// Compiled from X.java (version 1.8 : 52.0, super bit)";
			checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
	}
	public void testReleaseOption2() throws Exception {
		if (!isJRE17Plus) return;
		this.runConformTest(
				new String[] {
					"X.java",
					"/** */\n" +
					"public class X {\n" +
					"}",
				},
		     "\"" + OUTPUT_DIR +  File.separator + "X.java\"" //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		     + " --release 10 -d \"" + OUTPUT_DIR + "\"",
		     "",
		     "",
		     true);
		String expectedOutput = "// Compiled from X.java (version 10 : 54.0, super bit)";
			checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
	}
	public void testReleaseOption3() throws Exception {
		if (!isJRE17Plus) return;
		this.runConformTest(
				new String[] {
					"X.java",
					"/** */\n" +
					"public class X {\n" +
					"}",
				},
		     "\"" + OUTPUT_DIR +  File.separator + "X.java\"" //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		     + " --release 10 -d \"" + OUTPUT_DIR + "\"",
		     "",
		     "",
		     true);
		String expectedOutput = "// Compiled from X.java (version 10 : 54.0, super bit)";
			checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
	}
	public void testReleaseOption4() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"/** */\n" +
					"public class X {\n" +
					"}",
				},
		     "\"" + OUTPUT_DIR +  File.separator + "X.java\"" //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		     + " --release 8 -source 1.6 -d \"" + OUTPUT_DIR + "\"",
		     "",
		     "option -source is not supported when --release is used\n",
		     true);
	}
	public void testReleaseOption5() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"/** */\n" +
					"public class X {\n" +
					"}",
				},
		     "\"" + OUTPUT_DIR +  File.separator + "X.java\"" //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		     + " --release 8 -target 1.8 -d \"" + OUTPUT_DIR + "\"",
		     "",
		     "option -target is not supported when --release is used\n",
		     true);
	}
	public void testReleaseOption6() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"/** */\n" +
					"public class X {\n" +
					"}",
				},
		     "\"" + OUTPUT_DIR +  File.separator + "X.java\"" //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		     + " --release 5 -d \"" + OUTPUT_DIR + "\"",
		     "",
		     "release 5 is not found in the system\n",
		     true);
	}
	public void testReleaseOption7() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		this.runConformTest(
				new String[] {
					"X.java",
					"import java.util.stream.*;\n" +
					"/** */\n" +
					"public class X {\n" +
					"	public Stream<String> emptyStream() {\n" +
					"		Stream<String> st = Stream.empty();\n" +
					"		return st;\n" +
					"	}\n" +
					"}",
				},
		     "\"" + OUTPUT_DIR +  File.separator + "X.java\"" //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		     + " --release 8 -d \"" + OUTPUT_DIR + "\"",
		     "",
		     "",
		     true);
	}
	public void testReleaseOption8() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		if (isJRE20Plus) return;
		String output =
						"	public java.util.SequencedCollection<String> emptyCollection() {\n" +
						"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
						"java.util.SequencedCollection cannot be resolved to a type\n";

		this.runNegativeTest(
				new String[] {
					"X.java",
					"/** */\n" +
					"public class X {\n" +
					"	public java.util.SequencedCollection<String> emptyCollection() {\n" +
					"		return null;\n" +
					"	}\n" +
					"}",
				},
		     "\"" + OUTPUT_DIR +  File.separator + "X.java\"" //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		     + " --release 8 -d \"" + OUTPUT_DIR + "\"",
		     "",
		     "----------\n" + //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
    		 "1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 3)\n" +
    		 output +
    		 "----------\n" +
    		 "1 problem (1 error)\n",
		     true);
	}
	public void testReleaseOption9() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		if (isJRE20Plus) return;
		this.runNegativeTest(
				new String[] {
					"X.java",
					"interface I {\n" +
					"  int add(int x, int y);\n" +
					"}\n" +
					"public class X {\n" +
					"  public static void main(String[] args) {\n" +
					"    String i = \"\"\"\n" +
					"    		\"\"\";\n" +
					"  }\n" +
					"}\n",
				},
		     "\"" + OUTPUT_DIR +  File.separator + "X.java\"" //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		     + " --release 8 -d \"" + OUTPUT_DIR + "\"",
		     "",
		     "----------\n" +
			"""
			1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 6)
				String i = \"\"\"
			    		\"\"\";
				           ^^^^^^^^^^^^^
			The Java feature 'Text Blocks' is only available with source level 15 and above
			""" +
    		 "----------\n" +
    		 "1 problem (1 error)\n",
		     true);
	}
	public void testReleaseOption10() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		if (isJRE12Plus) return;
		this.runNegativeTest(
				new String[] {
					"X.java",
					"import java.io.*;\n" +
					"\n" +
					"public class X {\n" +
					"	public static void main(String[] args) {\n" +
					"		try {\n" +
					"			System.out.println();\n" +
					"			Reader r = new FileReader(args[0]);\n" +
					"			r.read();\n" +
					"		} catch(IOException | FileNotFoundException e) {\n" +
					"			e.printStackTrace();\n" +
					"		}\n" +
					"	}\n" +
					"}",
				},
		     "\"" + OUTPUT_DIR +  File.separator + "X.java\"" //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		     + " --release 8 -d \"" + OUTPUT_DIR + "\"",
		     "",
		     "----------\n" +
    		 "1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 9)\n" +
    		 "	} catch(IOException | FileNotFoundException e) {\n" +
    		 "	        ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
    		 "Multi-catch parameters are not allowed for source level below 1.7\n" +
    		 "----------\n" +
    		 "2. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 9)\n" +
    		 "	} catch(IOException | FileNotFoundException e) {\n" +
    		 "	                      ^^^^^^^^^^^^^^^^^^^^^\n" +
    		 "The exception FileNotFoundException is already caught by the alternative IOException\n" +
    		 "----------\n" +
    		 "2 problems (2 errors)\n",
		     true);
	}
	public void testReleaseOption11() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"public class X {\n" +
					"	public static void main(String[] args) {\n" +
					"	}\n" +
					"}",
				},
		     "\"" + OUTPUT_DIR +  File.separator + "X.java\"" + //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		     " -bootclasspath " + OUTPUT_DIR + File.separator + "src " +
		     " --release 9 -d \"" + OUTPUT_DIR + "\"",
		     "",
    		 "option -bootclasspath not supported at compliance level 9 and above\n",
		     true);
	}
	public void _testReleaseOption12() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		String javaHome = System.getProperty("java.home");
		this.runNegativeTest(
				new String[] {
					"X.java",
					"public class X {\n" +
					"	public static void main(String[] args) {\n" +
					"	}\n" +
					"}",
				},
		     "\"" + OUTPUT_DIR +  File.separator + "X.java\"" + //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		      " --system \"" + javaHome + "\"" +
		     " --release 8 -d \"" + OUTPUT_DIR + "\"",
		     "",
		     "----------\n" +
    		 "option --system not supported below compliance level 9",
		     true);
	}
	public void testReleaseOption13() { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		runConformModuleTest(
			new String[] {
				"p/X.java",
				"package p;\n" +
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"	}\n" +
				"}",
				"module-info.java",
				"module mod.one { \n" +
				"	requires java.base;\n" +
				"}"
	        },
			" --release 9 \"" + OUTPUT_DIR +  File.separator + "module-info.java\" " //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
	        + "\"" + OUTPUT_DIR +  File.separator + "p/X.java\"",
	        "",
	        "",
	        true);
	}
	public void testReleaseOption13a() { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		Runner runner = new Runner();
		runner.createFile(
				OUTPUT_DIR +File.separator+"p", "X.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
				"package p;\n" +
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"	}\n" +
				"}");
		runner.createFile(
				OUTPUT_DIR, "module-info.java",
				"module mod.one { \n" +
				"	requires java.base;\n" +
				"}");
		runner.commandLine.append(" --release 10"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		runner.javacTestOptions = new JavacTestOptions(ClassFileConstants.JDK10);
		runner.runConformModuleTest();
	}
	public void testReleaseOption14() { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		runNegativeModuleTest(
			new String[] {
				"module-info.java",
				"module mod.one { \n" +
				"}"
			},
			" --release 8 \"" + getSourceDir() +  File.separator + "module-info.java\" ", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
			"",
			"""
			----------
			1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/module-info.java (at line 1)
				module mod.one {\s
				^^^^^^
			Syntax error on token(s), misplaced construct(s)
			----------
			2. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/module-info.java (at line 1)
				module mod.one {\s
				           ^^^
			Syntax error, insert "Identifier (" to complete MethodHeaderName
			----------
			3. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/module-info.java (at line 1)
				module mod.one {\s
				           ^^^
			Syntax error, insert ")" to complete MethodDeclaration
			----------
			3 problems (3 errors)
			""",
			true,
			/*not tested with javac*/"");
	}
	// Test from https://bugs.eclipse.org/bugs/show_bug.cgi?id=526997
	public void testReleaseOption15() { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		Runner runner = new Runner();
		String fooDir = OUTPUT_DIR + File.separator + "foo"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		runner.createFile(
				fooDir, "Module.java",
				"package foo;\n" +
				"public class Module {}\n");
		runner.createFile(
				fooDir, "X.java",
				"package foo;\n" +
				"public class X { \n" +
				"	public Module getModule(String name) {\n" +
				"		return null;\n" +
				"	}\n" +
				"}");
		runner.commandLine.append(" --release 8 "); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		// javac:
		// warning: [options] source value 8 is obsolete and will be removed in a future release
		// warning: [options] target value 8 is obsolete and will be removed in a future release
		// warning: [options] To suppress warnings about obsolete options, use -Xlint:-options.
		runner.javacTestOptions = Excuse.JavacHasWarningsEclipseNotConfigured;
	    runner.runConformModuleTest();
	}
	// Test from https://bugs.eclipse.org/bugs/show_bug.cgi?id=526997
	public void testReleaseOption16() { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		runNegativeModuleTest(
			new String[] {
				"foo/Module.java",
				"package foo;\n" +
				"public class Module {}\n",
				"bar/X.java",
				"package bar;\n" +
				"import foo.*;\n" +
				"public class X { \n" +
				"	public Module getModule(String name) {\n" +
				"		return null;\n" +
				"	}\n" +
				"}"
	        },
			" -source 9 \"" + getSourceDir() +  File.separator + "foo" + File.separator + "Module.java\" " + //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
			"\"" +  getSourceDir() +  File.separator + "bar" + File.separator + "X.java\" ",
	        "",
	        "----------\n" +
    		"1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/bar/X.java (at line 4)\n" +
    		"	public Module getModule(String name) {\n" +
    		"	       ^^^^^^\n" +
    		"The type Module is ambiguous\n" +
    		"----------\n" +
    		"1 problem (1 error)\n",
	        true,
	        /*not tested with javac*/"");
	}
	public void testReleaseOption17() { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		runNegativeModuleTest(
			new String[] {
				"foo/Module.java",
				"package foo;\n" +
				"public class Module {}\n",
				"foo/X.java",
				"package foo;\n" +
				"public class X { \n" +
				"	public Module getModule(String name) {\n" +
				"		return null;\n" +
				"	}\n" +
				"}"
	        },
			" --release 60 \"" + OUTPUT_DIR +  File.separator + "foo" + File.separator + "Module.java\" " + //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
			"\"" +  OUTPUT_DIR +  File.separator + "foo" + File.separator + "X.java\" ",
	        "",
	        "release version 60 is not supported\n",
	        true,
	        /*not tested with javac*/"");
	}
	public void testReleaseOption18() {
		runNegativeModuleTest(
			new String[] {
				"X.java",
				"/** */\n" +
				"public class X {\n" +
				"}",
				},
			" --release 6 -1.8 \"" + OUTPUT_DIR +  File.separator + "foo" + File.separator + "Module.java\" " + //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
			"\"" +  OUTPUT_DIR +  File.separator + "foo" + File.separator + "X.java\" ",
	        "",
    		"option 1.8 is not supported when --release is used\n",
	        true,
	        /*not tested with javac*/"");
	}
	public void testReleaseOption19() {
		runNegativeModuleTest(
			new String[] {
			"X.java",
			"/** */\n" +
			"public class X {\n" +
			"}",
			},
			" -9 --release 9 \"" + OUTPUT_DIR +  File.separator + "foo" + File.separator + "Module.java\" " + //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
			"\"" +  OUTPUT_DIR +  File.separator + "foo" + File.separator + "X.java\" ",
	        "",
    		"option 9 is not supported when --release is used\n",
	        true,
	        /*not tested with javac*/"");
	}
	public void testReleaseOption20() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		if (!isJRE12Plus || isJRE20Plus) return;
		this.runNegativeTest(
				new String[] {
					"X.java",
					"import java.util.*;\n" +
					"\n" +
					"public class X {\n" +
					"	public static void main(String[] args) {\n" +
					"		String[] strings = List.of(\"\").toArray(String[]::new);\n" +
					"	}\n" +
					"}",
				},
		     "\"" + OUTPUT_DIR +  File.separator + "X.java\"" //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		     + " --release 8 -d \"" + OUTPUT_DIR + "\"",
		     "",
		     "----------\n" +
		     """
				1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 5)
					String[] strings = List.of("").toArray(String[]::new);
					                        ^^
				The method of(String) is undefined for the type List
		     """ +
		     "----------\n" +
    		 "1 problem (1 error)\n",
		     true);
	}
	public void testReleaseOption21() throws Exception {
		if (!isJRE12Plus) return;
		this.runConformTest(
				new String[] {
					"X.java",
					"public class X {\n" +
					"	public static void main(String[] args) {\n" +
					"		Integer.toUnsignedString(1, 1);\n" +
					"	}\n" +
					"}",
				},
		     "\"" + OUTPUT_DIR +  File.separator + "X.java\"" //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		     + " --release 8 -d \"" + OUTPUT_DIR + "\"",
		     "",
    		 "",
		     true);
	}
	public void testReleaseOption22() { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		if (isJRE11Plus || isJRE12Plus) return;
		runConformTest(
			new String[] {
				"p/X.java",
				"package p;\n" +
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"	}\n" +
				"}",
				"module-info.java",
				"module mod.one { \n" +
				"	requires java.base;\n" +
				"	requires java.xml.ws.annotation;\n" +
				"}"
	        },
			" --limit-modules java.base,java.xml.ws.annotation " + //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
			" --release 10 \"" + OUTPUT_DIR +  File.separator + "module-info.java\" "
	        + "\"" + OUTPUT_DIR +  File.separator + "p/X.java\"",
	        "",
	        "----------\n" +
    		"1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/module-info.java (at line 3)\n" +
    		"	requires java.xml.ws.annotation;\n" +
    		"	         ^^^^^^^^^^^^^^^^^^^^^^\n" +
    		"The module java.xml.ws.annotation has been deprecated since version 9 and marked for removal\n" +
    		"----------\n" +
    		"1 problem (1 warning)\n",
	        true);
	}
	public void testReleaseOption23() { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		if (!isJRE11Plus) return;
		runNegativeTest(
			new String[] {
				"p/X.java",
				"package p;\n" +
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"	}\n" +
				"}",
				"module-info.java",
				"module mod.one { \n" +
				"	requires java.xml.ws.annotation;\n" +
				"}"
	        },
			" --limit-modules java.base,java.xml.ws.annotation " + //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
			" --release 11 \"" + OUTPUT_DIR +  File.separator + "module-info.java\" "
	        + "\"" + OUTPUT_DIR +  File.separator + "p/X.java\"",
	        "",
	        "invalid module name: java.xml.ws.annotation\n",
	        true);
	}
	public void testReleaseOption24() { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		if (!isJRE11Plus) return;
		runNegativeTest(
			new String[] {
				"p/X.java",
				"package p;\n" +
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"	}\n" +
				"}",
				"module-info.java",
				"module mod.one { \n" +
				"	requires java.xml.ws.annotation;\n" +
				"}"
	        },
			" --limit-modules java.base,java.xml.ws.annotation " + //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
			" --release 12 \"" + OUTPUT_DIR +  File.separator + "module-info.java\" "
	        + "\"" + OUTPUT_DIR +  File.separator + "p/X.java\"",
	        "",
	        "invalid module name: java.xml.ws.annotation\n",
	        true);
	}
	public void testLimitModules1() { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT

		String moduleLoc = directory + File.separator + "mod.x"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		List<String> files = new ArrayList<>(); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.x { \n" +
						"	exports pm;\n" +
						"	requires java.sql;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "pm", "C1.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
						"package pm;\n" +
						"public class C1 {\n" +
						"}\n");


		StringBuilder buffer = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buffer.append("-d " + OUTPUT_DIR + File.separator + out ) //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --limit-modules java.base")
			.append(" --module-source-path " + "\"" + directory + "\""); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		runNegativeModuleTest(files, buffer,
				"",
				"----------\n" +
				"1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.x/module-info.java (at line 3)\n" +
				"	requires java.sql;\n" +
				"	         ^^^^^^^^\n" +
				"java.sql cannot be resolved to a module\n" +
				"----------\n" +
				"1 problem (1 error)\n",
				"module not found");
	}
	public void testLimitModules2() { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT

		String moduleLoc = directory + File.separator + "mod.x"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		List<String> files = new ArrayList<>(); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.x { \n" +
						"	exports pm;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "pm", "C1.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
						"package pm;\n" +
						"import java.sql.Connection;\n" +
						"public class C1 {\n" +
						"}\n");


		StringBuilder buffer = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buffer.append("-d " + OUTPUT_DIR + File.separator + out ) //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --limit-modules java.base")
			.append(" --module-source-path " + "\"" + directory + "\""); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		runNegativeModuleTest(files, buffer,
				"",
				"----------\n" +
				"1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.x/pm/C1.java (at line 2)\n" +
				"	import java.sql.Connection;\n" +
				"	       ^^^^^^^^\n" +
				"The import java.sql cannot be resolved\n" +
				"----------\n" +
				"1 problem (1 error)\n",
				"is not visible");
	}
	public void testLimitModules3() { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT

		String moduleLoc = directory + File.separator + "mod.x"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		List<String> files = new ArrayList<>(); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.x { \n" +
						"	exports pm;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "pm", "C1.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
						"package pm;\n" +
						"public class C1 {\n" +
						"}\n");


		StringBuilder buffer = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buffer.append("-d " + OUTPUT_DIR + File.separator + out ) //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --limit-modules java.sql")
			.append(" --module-source-path " + "\"" + directory + "\""); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		runConformModuleTest(files, buffer,
				"",
				"");
	}
	public void testLimitModules4() { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		Util.flushDirectoryContent(new File(OUTPUT_DIR));
		String outDir = OUTPUT_DIR + File.separator + "bin"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		String srcDir = OUTPUT_DIR + File.separator + "src"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		File modDir = new File(OUTPUT_DIR + File.separator + "mod"); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		createReusableModules(srcDir, outDir, modDir);
		String moduleLoc = srcDir + File.separator + "mod.three"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		List<String> files = new ArrayList<>(); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.three { \n" +
						"	requires mod.one;\n" +
						"	requires mod.two;\n" +
						"}");


		StringBuilder buffer = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buffer.append("-d " + outDir ) //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			.append(" -9 ")
			.append(" --module-path \"")
			.append(Util.getJavaClassLibsAsString())
			.append(modDir.getAbsolutePath())
			.append("\" ")
			.append(" --limit-modules mod.one,mod.two ")
			.append(" --module-source-path " + "\"" + srcDir + "\" "); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		runConformModuleTest(files, buffer,
				"",
				"");
	}
	public void testLimitModules5() { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		Util.flushDirectoryContent(new File(OUTPUT_DIR));
		String outDir = OUTPUT_DIR + File.separator + "bin"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		String srcDir = OUTPUT_DIR + File.separator + "src"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		File modDir = new File(OUTPUT_DIR + File.separator + "mod"); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		createReusableModules(srcDir, outDir, modDir);
		String moduleLoc = srcDir + File.separator + "mod.three"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		List<String> files = new ArrayList<>(); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.three { \n" +
						"	requires mod.one;\n" +
						"	requires mod.two;\n" +
						"}");


		StringBuilder buffer = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buffer.append("-d " + outDir ) //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			.append(" -9 ")
			.append(" --module-path \"")
			.append(Util.getJavaClassLibsAsString())
			.append(modDir.getAbsolutePath())
			.append("\" ")
			.append(" --limit-modules mod.one ")
			.append(" --module-source-path " + "\"" + srcDir + "\" "); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		runNegativeModuleTest(files, buffer,
				"",
				"----------\n" +
				"1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.three/module-info.java (at line 3)\n" +
				"	requires mod.two;\n" +
				"	         ^^^^^^^\n" +
				"mod.two cannot be resolved to a module\n" +
				"----------\n" +
				"1 problem (1 error)\n",
				"");
	}
	public void testBug519600() { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		Util.flushDirectoryContent(new File(OUTPUT_DIR));
		String outDir = OUTPUT_DIR + File.separator + "bin"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		String srcDir = OUTPUT_DIR + File.separator + "src"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		String moduleLoc = srcDir + File.separator + "test"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		Runner runner = new Runner();
		runner.createFile(moduleLoc, "module-info.java",
						"module test {}");
		runner.createFile(moduleLoc + File.separator + "test", "Thing.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
				"package test;\n" +
				"import java.util.Comparator;\n" +
				"import java.util.Iterator;\n" +
				"public abstract class Thing implements Iterator<Object> {\n" +
				"    void breaking() {\n" +
				"        remove(); // allowed (good)\n" +
				"        Iterator.super.remove(); // not 1.7-compliant (must be an error)\n" +
				"        Comparator.naturalOrder(); // not 1.7-compliant (bad error message)\n" +
				"    }\n" +
				"}\n");

		runner.commandLine.append("-d " + outDir ) //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			.append(" -source 9 ")
			.append(" --module-source-path " + "\"" + srcDir + "\" "); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		runner.javacVersionOptions = "-Xlint:-options"; // -source 9 already provided
		runner.runConformModuleTest();
	}
	public void testBug508889_001() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		this.runConformTest(
			new String[] {
				"module-info.java",
				"module mymodule {\n" +
				"}",
			},
			"\"" + OUTPUT_DIR +  File.separator + "module-info.java\"" //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
			+ " -9 -source 9 -target 9 -d \"" + OUTPUT_DIR + "\"",
			"",
			"",
			true);
		String expectedOutput = "// Compiled from module-info.java (version 9 : 53.0, no super bit)\n" +
				" module mymodule  {\n" +
				"  // Version: \n" +
				"\n" +
				"  requires java.base;\n" +
				"\n" +
				"}";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "module-info.class", "module-info", expectedOutput); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
	}
	public void testBug508889_002() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		String moduleLoc = directory + File.separator + "mod.one"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		List<String> files = new ArrayList<>(); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.one { \n" +
								"    exports pack1;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "pack1", "X11.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
						"package pack1;\n" +
						"public class X11 {\n" +
						"}");
		StringBuilder buffer = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buffer.append("-d " + OUTPUT_DIR + File.separator + out ) //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ");

		runConformModuleTest(files,
				buffer,
				"",
				"");
		String expectedOutput = "// Compiled from module-info.java (version 9 : 53.0, no super bit)\n" +
				" module mod.one  {\n" +
				"  // Version: \n" +
				"\n" +
				"  requires java.base;\n" +
				"\n" +
				"  exports pack1;\n" +
				"\n" +
				"}";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + out + File.separator + "module-info.class", "module-info", expectedOutput); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
	}
	public void testBug508889_003() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		String moduleLoc = directory + File.separator + "mod.one"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		List<String> files = new ArrayList<>(); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.one { \n" +
								"    exports pack1;\n" +
								"    exports pack2 to second;\n" +
								"    opens pack3;\n" +
								"    opens pack4 to third;\n" +
								"    uses pack5.X51;\n" +
								"    provides pack1.I11 with pack1.X11;\n" +
								"    requires transitive java.sql;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "pack1", "I11.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
				"package pack1;\n" +
						"public interface I11 {\n" +
				"}");
		writeFileCollecting(files, moduleLoc + File.separator + "pack1", "X11.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
						"package pack1;\n" +
						"public class X11 implements I11{\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "pack2", "X21.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
				"package pack2;\n" +
				"public class X21 {\n" +
				"}");
		writeFileCollecting(files, moduleLoc + File.separator + "pack3", "X31.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
				"package pack3;\n" +
				"public class X31 {\n" +
				"}");
		writeFileCollecting(files, moduleLoc + File.separator + "pack4", "X41.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
				"package pack4;\n" +
				"public class X41 {\n" +
				"}");
		writeFileCollecting(files, moduleLoc + File.separator + "pack5", "X51.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
				"package pack5;\n" +
				"public class X51 {\n" +
				"}");
		StringBuilder buffer = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buffer.append("-d " + OUTPUT_DIR + File.separator + out ) //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ");
		files.forEach(name -> buffer.append(" \"" + name + "\"")); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		runConformTest(new String[0],
				buffer.toString(),
				"",
				"",
				false);
		String expectedOutput = "// Compiled from module-info.java (version 9 : 53.0, no super bit)\n" +
				" module mod.one  {\n" +
				"  // Version: \n" +
				"\n" +
				"  requires transitive java.sql;\n" +
				"  requires java.base;\n" +
				"\n" +
				"  exports pack1;\n" +
				"  exports pack2 to second;\n" +
				"\n" +
				"  opens pack3;\n" +
				"  opens pack4 to third;\n" +
				"\n" +
				"  uses pack5.X51\n" +
				"\n" +
				"  provides pack1.I11 with pack1.X11;\n" +
				"\n" +
				"}";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + out + File.separator + "module-info.class", "module-info", expectedOutput); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
	}
	public void testBug520858() { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		Util.flushDirectoryContent(new File(OUTPUT_DIR));
		String outDir = OUTPUT_DIR + File.separator + "bin"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		String srcDir = OUTPUT_DIR + File.separator + "src"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		String moduleLoc = srcDir + File.separator + "test"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		Runner runner = new Runner();
		runner.createFile(moduleLoc, "module-info.java",
						"module test {\n" +
						"	requires org.astro;\n" +
						"}");
		runner.createFile(moduleLoc + File.separator + "p", "Test.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
			"package p;\n" +
			"import org.astro.World;\n" +
			"public class Test {\n" +
			"	World w = null;\n" +
			"}");
		moduleLoc = srcDir + File.separator + "org.astro"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		runner.createFile(moduleLoc, "module-info.java",
				"module org.astro {\n" +
				"	exports org.astro;\n" +
				"}");
		runner.createFile(moduleLoc + File.separator + "org" + File.separator + "astro", "World.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
			"package org.astro;\n" +
			"public interface World {\n" +
			"	public static String name() {\n" +
			"		return \"\";\n" +
			"	}\n" +
			"}");
		runner.commandLine.append("-d " + outDir ) //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			.append(" -source 9 ")
			.append(" --module-source-path " + "\"" + srcDir + "\" "); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		runner.javacVersionOptions = " -Xlint:-options";
		runner.runConformModuleTest();
	}
	public void testBug520858a() { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		Util.flushDirectoryContent(new File(OUTPUT_DIR));
		String outDir = OUTPUT_DIR + File.separator + "bin"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		String srcDir = OUTPUT_DIR + File.separator + "src"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		String moduleLoc = srcDir + File.separator + "test"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		Runner runner = new Runner();
		// not adding some files to the command line
		writeFile(moduleLoc, "module-info.java",
						"module test {\n" +
						"	requires org.astro;\n" +
						"}");
		// the only file added:
		runner.createFile(moduleLoc + File.separator + "p", "Test.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
			"package p;\n" +
			"import org.astro.World;\n" +
			"public class Test {\n" +
			"	World w = null;\n" +
			"}");
		moduleLoc = srcDir + File.separator + "org.astro"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		writeFile(moduleLoc, "module-info.java",
				"module org.astro {\n" +
				"	exports org.astro;\n" +
				"}");
		writeFile(moduleLoc + File.separator + "org" + File.separator + "astro", "World.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
			"package org.astro;\n" +
			"public interface World {\n" +
			"	public static String name() {\n" +
			"		return \"\";\n" +
			"	}\n" +
			"}");
		runner.commandLine.append("-d " + outDir ) //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			.append(" -source 9 ")
			.append(" --module-source-path " + "\"" + srcDir + "\" "); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		runner.javacVersionOptions = " -Xlint:-options";
		runner.runConformModuleTest();
	}
	public void testBug520858b() { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		Util.flushDirectoryContent(new File(OUTPUT_DIR));
		String outDir = OUTPUT_DIR + File.separator + "bin"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		String srcDir = OUTPUT_DIR + File.separator + "src"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		String moduleLoc = srcDir + File.separator + "test"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		Runner runner = new Runner();
		runner.createFile(moduleLoc, "module-info.java",
						"module test {\n" +
						"	requires org.astro;\n" +
						"}");
		runner.createFile(moduleLoc + File.separator + "p", "Test.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
			"package p;\n" +
			"import org.astro.World;\n" +
			"public class Test {\n" +
			"	World w = null;\n" +
			"}");
		moduleLoc = srcDir + File.separator + "org.astro"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		// not adding this file to the command line (intentional?):
		writeFile(moduleLoc, "module-info.java",
				"module org.astro {\n" +
				"	exports org.astro;\n" +
				"}");
		runner.createFile(moduleLoc + File.separator + "org" + File.separator + "astro", "World.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
			"package org.astro;\n" +
			"public interface World {\n" +
			"	public static String name() {\n" +
			"		return \"\";\n" +
			"	}\n" +
			"}");
		runner.commandLine.append("-d " + outDir ) //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			.append(" -source 9 ")
			.append(" --module-source-path " + "\"" + srcDir + "\" "); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		runner.javacVersionOptions = " -Xlint:-options";
		runner.runConformModuleTest();
	}
	public void testBug520858c() { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		Util.flushDirectoryContent(new File(OUTPUT_DIR));
		String outDir = OUTPUT_DIR + File.separator + "bin"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		String srcDir = OUTPUT_DIR + File.separator + "src"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		String moduleLoc = srcDir + File.separator + "test"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		List<String> files = new ArrayList<>(); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW

		writeFileCollecting(files, moduleLoc + File.separator + "p", "Test.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
			"package p;\n" +
			"import org.astro.World;\n" +
			"public class Test {\n" +
			"	World w = null;\n" +
			"}");
		moduleLoc = srcDir + File.separator + "org.astro"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		writeFileCollecting(files, moduleLoc, "module-info.java",
				"module org.astro {\n" +
				"	exports org.astro;\n" +
				"}");
		writeFileCollecting(files, moduleLoc + File.separator + "org" + File.separator + "astro", "World.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
			"package org.astro;\n" +
			"public interface World {\n" +
			"	public static String name() {\n" +
			"		return \"\";\n" +
			"	}\n" +
			"}");
		StringBuilder buffer = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buffer.append("-d " + outDir ) //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			.append(" -source 9 ")
			.append(" --module-source-path " + "\"" + srcDir + "\" "); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		runNegativeModuleTest(files, buffer,
				"",
				"\'---OUTPUT_DIR_PLACEHOLDER---/src/test/p/Test.java\' does not belong to a module on the module source path\n",
				"not in a module on the module source path");
	}
	public void testBug520858d() { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		Util.flushDirectoryContent(new File(OUTPUT_DIR));
		String outDir = OUTPUT_DIR + File.separator + "bin"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		String srcDir = OUTPUT_DIR + File.separator + "src"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		String moduleLoc = srcDir + File.separator + "test"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		List<String> files = new ArrayList<>(); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW

		writeFileCollecting(files, moduleLoc + File.separator + "p", "Test.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
			"package p;\n" +
			"import org.astro.World;\n" +
			"public class Test {\n" +
			"	World w = null;\n" +
			"}");
		moduleLoc = srcDir + File.separator + "org.astro"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		writeFileCollecting(files, moduleLoc, "module-info.java",
				"module org.astro {\n" +
				"	exports org.astro;\n" +
				"}");
		writeFileCollecting(files, moduleLoc + File.separator + "org" + File.separator + "astro", "World.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
			"package org.astro;\n" +
			"public interface World {\n" +
			"	public static String name() {\n" +
			"		return \"\";\n" +
			"	}\n" +
			"}");
		StringBuilder buffer = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buffer.append("-d " + outDir ) //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			.append(" -source 9 ")
			.append(" --module-source-path " + "\"" + srcDir + "\" ") //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
			.append(srcDir + File.separator + "org.astro" + File.separator + "org" + File.separator + "astro" + File.separator + "World.java ") //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
			.append(srcDir + File.separator + "test" + File.separator + "p" + File.separator + "Test.java"); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		runNegativeModuleTest(Collections.emptyList(), buffer,
			"",
			"\'---OUTPUT_DIR_PLACEHOLDER---/src/test/p/Test.java\' does not belong to a module on the module source path\n",
			"not in a module on the module source path");
	}
	public void testBug520858e() { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		Util.flushDirectoryContent(new File(OUTPUT_DIR));
		String outDir = OUTPUT_DIR + File.separator + "bin"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		String srcDir = OUTPUT_DIR + File.separator + "src"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		String moduleLoc = srcDir + File.separator + "test"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		List<String> files = new ArrayList<>(); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW

		writeFileCollecting(files, moduleLoc + File.separator + "p", "Test.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
			"package p;\n" +
			"import org.astro.World;\n" +
			"public class Test {\n" +
			"	World w = null;\n" +
			"}");
		moduleLoc = srcDir + File.separator + "org.astro"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		writeFileCollecting(files, moduleLoc, "module-info.java",
				"module org.astro {\n" +
				"	exports org.astro;\n" +
				"}");
		writeFileCollecting(files, moduleLoc + File.separator + "org" + File.separator + "astro", "World.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
			"package org.astro;\n" +
			"public interface World {\n" +
			"	public static String name() {\n" +
			"		return \"\";\n" +
			"	}\n" +
			"}");
		StringBuilder buffer = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buffer.append("-d " + outDir ) //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			.append(" -source 9 ")
			.append(" --module-source-path " + "\"" + srcDir + "\" ") //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
			.append(srcDir + File.separator + "org.astro" + File.separator + "org" + File.separator + "astro" + File.separator + "World.java ") //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
			.append(srcDir + File.separator + "org.astro" + File.separator + "module-info.java ") //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
			.append(srcDir + File.separator + "test" + File.separator + "p" + File.separator + "Test.java"); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		runNegativeModuleTest(Collections.emptyList(), buffer,
			"",
			"\'---OUTPUT_DIR_PLACEHOLDER---/src/test/p/Test.java\' does not belong to a module on the module source path\n",
			"not in a module on the module source path");
	}
	public void testBug530575() { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		File srcDir = new File(directory);

		String moduleLoc = directory + File.separator + "mod.x"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		List<String> files = new ArrayList<>(); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.x { \n" +
						"	exports px;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "px", "C1.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
						"package px;\n" +
						"public class C1 {\n" +
						"}\n");

		moduleLoc = directory + File.separator + "mod.y"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.y { \n" +
						"	exports py;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "py", "C1.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
						"package py;\n" +
						"public class C1 {\n" +
						"}\n");

		StringBuilder buffer = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buffer.append("-d " + OUTPUT_DIR + File.separator + out ) //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-source-path " + "\"" + directory + "\""); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		for (String fileName : files) //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
			buffer.append(" \"").append(fileName).append("\""); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		runConformTest(new String[0],
				buffer.toString(),
				"",
				"",
				false);
		Util.flushDirectoryContent(srcDir);
		files.clear();
		writeFileCollecting(files, directory, "module-info.java",
				"module test { \n" +
				"	requires mod.x;\n" +
				"	requires mod.y;\n" +
				"}");
		writeFileCollecting(files, directory + File.separator + "p", "X.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
						"package p;\n" +
						"public class X extends px.C1 { \n" +
						"	py.C1 c = null;\n" +
						"}");
		buffer = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buffer.append("-d " + OUTPUT_DIR + File.separator + out ) //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-path " + "\"" + OUTPUT_DIR + File.separator + out + File.separator + "mod.x" + File.pathSeparator + OUTPUT_DIR + File.separator + out + File.separator + "mod.y" + "\""); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		runConformModuleTest(files,
				buffer,
				"",
				"");
	}
	/*
	 * Test that when module-info is not included in the command line, the class is still
	 * generated inside the module's sub folder.
	 */
	public void testBug533411() { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		String moduleLoc = directory + File.separator + "mod.one"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		List<String> files = new ArrayList<>(); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.one { \n" +
						"	requires java.sql;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "p", "Test.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
				"package p;\n" +
				"public class Test {\n" +
				"	java.sql.Connection conn = null;\n" +
				"}");

		StringBuilder buffer = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buffer.append("-d " + OUTPUT_DIR + File.separator + out ) //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-source-path " + "\"" + directory + "\" ") //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
			.append(moduleLoc + File.separator + "p" + File.separator + "Test.java"); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT

		Set<String> classFiles = runConformModuleTest(
				new String[0],
				buffer.toString(),
				"",
				"",
				false);
		String fileName = OUTPUT_DIR + File.separator + out + File.separator + "mod.one" + File.separator + "module-info.class"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		assertClassFile("Missing modul-info.class: " + fileName, fileName, classFiles); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
	}
	public void test_npe_bug535107() { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		runConformModuleTest(
				new String[] {
					"p/X.java",
					"package p;\n" +
			  		"import java.lang.annotation.*;\n" +
					"@Target(ElementType.MODULE)\n" +
					"public @interface X {\n" +
					"	ElementType value();\n" +
					"}",
					"module-info.java",
			  		"import java.lang.annotation.*;\n" +
			  		"@p.X(ElementType.MODULE)\n" +
					"module mod.one { \n" +
					"}"
		        },
				" -9 \"" + OUTPUT_DIR +  File.separator + "module-info.java\" " //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		        + "\"" + OUTPUT_DIR +  File.separator + "p/X.java\"",
		        "",
		        "",
		        true);
	}
	public void testBug540067a() { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		String moduleLoc = directory + File.separator + "mod.one"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		List<String> files = new ArrayList<>(); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.one { \n" +
						" exports p;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "p", "X.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
				"package p;\n" +
				"public class X {\n" +
				"}");
		writeFileCollecting(files, moduleLoc + File.separator + "p" + File.separator + "q", "Test.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
				"/*nothing in it */");

		StringBuilder buffer = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buffer.append("-d " + OUTPUT_DIR + File.separator + out ) //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-source-path " + "\"" + directory + "\" ") //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
			.append(moduleLoc + File.separator + "module-info.java ") //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
			.append(moduleLoc + File.separator + "p" + File.separator + "X.java ") //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
			.append(moduleLoc + File.separator + "p" + File.separator + "q" + File.separator + "Test.java"); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT

		runConformModuleTest(
				new String[0],
				buffer.toString(),
				"",
				"",
				false);
	}
	public void testBug540067b() { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		String moduleLoc = directory + File.separator + "mod.one"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		List<String> files = new ArrayList<>(); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.one { \n" +
						" exports p;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "p", "X.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
				"package p;\n" +
				"public class X {\n" +
				"}");
		writeFileCollecting(files, moduleLoc + File.separator + "p" + File.separator + "q", "Test.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
				"package p.q;");

		StringBuilder buffer = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buffer.append("-d " + OUTPUT_DIR + File.separator + out ) //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-source-path " + "\"" + directory + "\" ") //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
			.append(moduleLoc + File.separator + "module-info.java ") //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
			.append(moduleLoc + File.separator + "p" + File.separator + "X.java ") //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
			.append(moduleLoc + File.separator + "p" + File.separator + "q" + File.separator + "Test.java"); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT

		runConformModuleTest(
				new String[0],
				buffer.toString(),
				"",
				"",
				false);
	}
	public void testBug540067c() { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		String moduleLoc = directory + File.separator + "mod.one"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		List<String> files = new ArrayList<>(); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.one { \n" +
						" exports p;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "p", "X.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
				"package p;\n" +
				"public class X {\n" +
				"}");
		writeFileCollecting(files, moduleLoc + File.separator + "p" + File.separator + "q", "Test.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
				"package p.q;\n"
				+ "class Test {}");

		StringBuilder buffer = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buffer.append("-d " + OUTPUT_DIR + File.separator + out ) //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-source-path " + "\"" + directory + "\" ") //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
			.append(moduleLoc + File.separator + "module-info.java ") //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
			.append(moduleLoc + File.separator + "p" + File.separator + "X.java ") //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
			.append(moduleLoc + File.separator + "p" + File.separator + "q" + File.separator + "Test.java"); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT

		runConformModuleTest(
				new String[0],
				buffer.toString(),
				"",
				"",
				false);
	}
	public void testBug540067d() { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		String moduleLoc = directory + File.separator + "mod.one"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		List<String> files = new ArrayList<>(); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.one { \n" +
						" exports p;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "p", "X.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
				"package p;\n" +
				"public class X {\n" +
				"}");
		writeFileCollecting(files, moduleLoc + File.separator + "p" + File.separator + "q", "Test.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
				"class Test {}");

		StringBuilder buffer = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buffer.append("-d " + OUTPUT_DIR + File.separator + out ) //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-source-path " + "\"" + directory + "\" ") //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
			.append(moduleLoc + File.separator + "module-info.java ") //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
			.append(moduleLoc + File.separator + "p" + File.separator + "X.java ") //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
			.append(moduleLoc + File.separator + "p" + File.separator + "q" + File.separator + "Test.java"); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT

		runNegativeModuleTest(
				new String[0],
				buffer.toString(),
				"",
				"----------\n" +
				"1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.one/p/q/Test.java (at line 1)\n" +
				"	class Test {}\n" +
				"	^\n" +
				"Must declare a named package because this compilation unit is associated to the named module \'mod.one\'\n" +
				"----------\n" +
				"1 problem (1 error)\n",
				false,
				"unnamed package is not allowed in named modules");
	}
	public void testBug540067e() { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		String moduleLoc = directory + File.separator + "mod.one"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		List<String> files = new ArrayList<>(); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.one { \n" +
						" exports p;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "p", "X.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
				"package p;\n" +
				"public class X {\n" +
				"}");
		writeFileCollecting(files, moduleLoc + File.separator + "p" + File.separator + "q", "Test.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
				"import java.lang.*;");

		StringBuilder buffer = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buffer.append("-d " + OUTPUT_DIR + File.separator + out ) //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" -warn:-unused")
			.append(" --module-source-path " + "\"" + directory + "\" ") //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
			.append(moduleLoc + File.separator + "module-info.java ") //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
			.append(moduleLoc + File.separator + "p" + File.separator + "X.java ") //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
			.append(moduleLoc + File.separator + "p" + File.separator + "q" + File.separator + "Test.java"); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT

		runNegativeModuleTest(
				new String[0],
				buffer.toString(),
				"",
				"----------\n" +
				"1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.one/p/q/Test.java (at line 1)\n" +
				"	import java.lang.*;\n" +
				"	^\n" +
				"Must declare a named package because this compilation unit is associated to the named module \'mod.one\'\n" +
				"----------\n" +
				"1 problem (1 error)\n",
				false,
				"unnamed package is not allowed in named modules");
	}
	public void testBug548195() { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		String moduleLoc = directory + File.separator + "mod.one"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		List<String> files = new ArrayList<>(); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.one { \n" +
						" exports p;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "p", "X.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
				"package p;\n" +
				"public class X {\n" +
				"}");

		StringBuilder buffer = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		String binDir = OUTPUT_DIR + File.separator + out; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		buffer.append("-d " + binDir ) //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" -warn:-unused")
			.append(" --module-source-path " + "\"" + directory + "\" ") //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
			.append(" --module-version 47.11 ");
		String outText = isJRE9Plus ? "" : "Could not invoke method java.lang.module.ModuleDescriptor.Version.parse(), cannot validate module version.\n";
		runConformModuleTest(files, buffer, outText, "");

		IClassFileReader cfr = ToolFactory.createDefaultClassFileReader(binDir + File.separator + "mod.one" + File.separator + "module-info.class", IClassFileReader.CLASSFILE_ATTRIBUTES); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		assertNotNull("Error reading module-info.class", cfr);
		IClassFileAttribute[] attrs = cfr.getAttributes();
		for (IClassFileAttribute attr : attrs) { //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
			char[] name = attr.getAttributeName();
			if (CharOperation.equals(name, AttributeNamesConstants.ModuleName)) {
				IModuleAttribute modAttr = (IModuleAttribute) attr;
				String expectedVersion = isJRE9Plus ? "47.11" : "";
				assertEquals("version in attribute", expectedVersion, new String(modAttr.getModuleVersionValue()));
				return;
			}
		}
		fail("module attribute not found");
	}
	public void testBug548195fail() { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		String moduleLoc = directory + File.separator + "mod.one"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		List<String> files = new ArrayList<>(); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.one { \n" +
						" exports p;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "p", "X.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
				"package p;\n" +
				"public class X {\n" +
				"}");

		StringBuilder buffer = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		String binDir = OUTPUT_DIR + File.separator + out; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		buffer.append("-d " + binDir ) //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" -warn:-unused")
			.append(" --module-source-path " + "\"" + directory + "\" ") //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
			.append(" --module-version fourtyseven.11 ");
		if (isJRE9Plus) {
			runNegativeModuleTest(files, buffer, "", "fourtyseven.11: Version string does not start with a number\n", "bad value");
		} else {
			runConformModuleTest(files, buffer, "Could not invoke method java.lang.module.ModuleDescriptor.Version.parse(), cannot validate module version.\n", "");
		}
	}
	public void testPackageTypeConflict2() { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT

		String moduleLoc = directory + File.separator + "mod.one"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		List<String> files = new ArrayList<>(); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.one { \n" +
								"	exports p1.p2;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "p1" + File.separator + "p2", "t3.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
						"package p1.p2;\n" +
						"public class t3 {\n" +
						"}\n");

		moduleLoc = directory + File.separator + "mod.two"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.two { \n" +
							"	exports p1.p2.t3;\n" +
							"	requires mod.one;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "p1" + File.separator + "p2" + File.separator + "t3", "t4.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
						"package p1.p2.t3;\n" +
						"public class t4 {\n" +
						"}\n");

		StringBuilder buffer = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buffer.append("-d " + OUTPUT_DIR + File.separator + out ) //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-source-path " + "\"" + directory + "\""); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT

		runNegativeModuleTest(
				files,
				buffer,
				"",
				"----------\n" +
				"1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.two/p1/p2/t3/t4.java (at line 1)\n" +
				"	package p1.p2.t3;\n" +
				"	        ^^^^^^^^\n" +
				"The package p1.p2.t3 collides with a type\n" +
				"----------\n" +
				"1 problem (1 error)\n",
				"package p1.p2.t3 clashes with class of same name");
	}
	public void testBug550178() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		Util.flushDirectoryContent(new File(OUTPUT_DIR));
		String outDir = OUTPUT_DIR + File.separator + "bin"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		String srcDir = OUTPUT_DIR + File.separator + "src"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		File modDir = new File(OUTPUT_DIR + File.separator + "mod"); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		createReusableModules(srcDir, outDir, modDir);
		String moduleLoc = srcDir + File.separator + "mod.three"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		List<String> files = new ArrayList<>(); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.three { \n" +
						"	exports pkg.invalid;\n" +
						"}");


		StringBuilder buffer = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buffer.append("-d " + outDir ) //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			.append(" -9 ")
			.append(" --module-path \"")
			.append(Util.getJavaClassLibsAsString())
			.append(modDir.getAbsolutePath())
			.append("\" ")
			.append(" --module-source-path " + "\"" + srcDir + "\" "); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		runNegativeModuleTest(files, buffer,
				"",
				"----------\n" +
				"1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.three/module-info.java (at line 2)\n" +
				"	exports pkg.invalid;\n" +
				"	        ^^^^^^^^^^^\n" +
				"The package pkg.invalid does not exist or is empty\n" +
				"----------\n" +
				"1 problem (1 error)\n",
				"");
	}
	public void testRelease565930_1() throws Exception {
		this.runConformTest(
				new String[] {
					"Dummy.java",
					"public class Dummy {\n"
					+ "	boolean b = new String(\"a\").contains(\"b\");\n"
					+ "}",
				},
		     "\"" + OUTPUT_DIR +  File.separator + "Dummy.java\"" //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		     + " --release 9 -d \"" + OUTPUT_DIR + "\"",
		     "",
		     "",
		     true);
	}
	public void testRelease565930_2() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		this.runConformTest(
				new String[] {
					"Main.java",
					"public final class Main<T extends Object> {\n"
					+ "    public void test() {\n"
					+ "    	final ClassLoader classLoader = this.getClass().getClassLoader();\n"
					+ "    } \n"
					+ "}",
				},
		     "\"" + OUTPUT_DIR +  File.separator + "Main.java\"" //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		     + " --release 9 -d \"" + OUTPUT_DIR + "\"",
		     "",
		     "----------\n" +
    		 "1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/Main.java (at line 3)\n" +
    		 "	final ClassLoader classLoader = this.getClass().getClassLoader();\n" +
    		 "	                  ^^^^^^^^^^^\n" +
    		 "The value of the local variable classLoader is not used\n" +
    		 "----------\n" +
    		 "1 problem (1 warning)\n",
		     true);
	}
	public void testBug571363() throws Exception {
		if (!isJRE12Plus) return;
		this.runConformTest(
			new String[] {
				"A.java",
				"public final class A {\n"
				+ "    org.w3c.dom.Element list;\n"
				+ "}",
			},
	     "\"" + OUTPUT_DIR +  File.separator + "A.java\"" //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
	     + " -classpath " + "\"" + this.getCompilerTestsPluginDirectoryPath() + File.separator + "workspace" + File.separator + "Test571363.jar\""
	     + " --release 11 -d \"" + OUTPUT_DIR + "\"",
	     "",
	     "",
	     true);
	}
	public void testBug574097() { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		Util.flushDirectoryContent(new File(OUTPUT_DIR));
		String outDir = OUTPUT_DIR + File.separator + "bin"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		String srcDir = OUTPUT_DIR + File.separator + "src"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		File modDir = new File(OUTPUT_DIR + File.separator + "mod"); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		String moduleLoc = srcDir + File.separator + "mod.one"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		List<String> files = new ArrayList<>(); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.one { \n" +
						"	exports p;\n" +
						"	requires transitive java.compiler;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "p", "TestProcessor.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
						"package p;\n"
						+ "import java.util.Set;\n"
						+ "import javax.annotation.processing.AbstractProcessor;\n"
						+ "import javax.annotation.processing.RoundEnvironment;\n"
						+ "import javax.lang.model.element.TypeElement;\n"
						+ "public class TestProcessor extends AbstractProcessor {\n"
						+ "	@Override\n"
						+ "	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {\n"
						+ "		return false;\n"
						+ "	}\n"
						+ "}");

		StringBuilder buffer = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buffer.append("-d " + outDir ) //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		.append(" -9 ")
		.append(" --module-path \"")
		.append(Util.getJavaClassLibsAsString())
		.append("\" ")
		.append(" --module-source-path " + "\"" + srcDir + "\""); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		for (String fileName : files) //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
			buffer.append(" \"").append(fileName).append("\""); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL

		runConformTest(new String[]{},
			buffer.toString(),
			"",
			"",
			false);
		String jarName = modDir + File.separator + "mod.one.jar"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		try {
			Util.zip(new File(outDir + File.separator + "mod.one"), //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
								jarName);
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (!modDir.exists()) {
			if (!modDir.mkdirs()) {
				fail("Coult not create folder " + modDir); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
			}
		}
		Util.flushDirectoryContent(new File(srcDir));
		files = new ArrayList<>(); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW
		moduleLoc = srcDir + File.separator + "mod.two"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.two { \n" +
						"	exports q;\n" +
						"	requires java.base;\n" +
						"	requires mod.one;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "q", "A.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
						"package q;\n" +
						"public class A {\n" +
						"   p.TestProcessor prc = null;\n" +
						"}");
		buffer = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buffer.append("-d " + outDir ) //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		.append(" -9 ")
		.append(" --module-path \"")
		.append(Util.getJavaClassLibsAsString())
		.append("\" ")
		.append(" --module-source-path " + "\"" + srcDir + "\"") //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		.append(" --processor-module-path " + "\"" + jarName + "\""); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		for (String name : files) //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
			buffer.append(" \"").append(name).append("\""); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL

		runNegativeTest(new String[]{},
			buffer.toString(),
			"",
			"----------\n" +
			"1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.two/module-info.java (at line 4)\n" +
			"	requires mod.one;\n" +
			"	         ^^^^^^^\n" +
			"mod.one cannot be resolved to a module\n" +
			"----------\n" +
			"----------\n" +
			"2. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.two/q/A.java (at line 3)\n" +
			"	p.TestProcessor prc = null;\n" +
			"	^\n" +
			"p cannot be resolved to a type\n" +
			"----------\n" +
			"2 problems (2 errors)\n",
			false);
	}
	/*
	 * Test that reference to a binary package that is exported in a module
	 * but doesn't have a corresponding resource or .class files is reported.
	 */
	public void testBug522472a() { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		File srcDir = new File(directory);
		String moduleLoc = directory + File.separator + "mod.one"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		List<String> files = new ArrayList<>(); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW
		writeFileCollecting(files, moduleLoc,
				"module-info.java",
				"module mod.one { \n" +
				"	exports x.y.z;\n" +
				"	exports a.b.c;\n" +
				"}");
		writeFileCollecting(files, moduleLoc + File.separator + "x" + File.separator + "y" + File.separator + "z", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
				"X.java",
				"package x.y.z;\n");
		writeFileCollecting(files, moduleLoc + File.separator + "a" + File.separator + "b" + File.separator + "c", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
				"A.java",
				"package a.b.c;\n" +
				"public class A {}");
		StringBuilder buffer = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buffer.append("-d " + OUTPUT_DIR + File.separator + out ) //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-source-path " + "\"" + directory + "\" "); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT

		runConformModuleTest(files,
				buffer,
				"",
				"");

		Util.flushDirectoryContent(srcDir);
		files.clear();
		moduleLoc = directory + File.separator + "mod.two"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		writeFileCollecting(files, moduleLoc,
				"module-info.java",
				"module mod.two { \n" +
					"	requires mod.one;\n" +
				"}");
		writeFileCollecting(files, moduleLoc + File.separator + "p" + File.separator + "q" + File.separator + "r", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
				"Main.java",
				"package p.q.r;\n" +
				"import a.b.c.*;\n" +
				"import x.y.z.*;\n" +
				"public class Main {"
				+ "}");
		buffer = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buffer.append("-d " + OUTPUT_DIR + File.separator + out ) //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-path " + "\"" + OUTPUT_DIR + File.separator + out + "\" ") //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
			.append(" --module-source-path " + "\"" + directory + "\" "); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		runConformModuleTest(files,
				buffer,
				"",
				"----------\n"
				+ "1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.two/p/q/r/Main.java (at line 2)\n"
				+ "	import a.b.c.*;\n"
				+ "	       ^^^^^\n"
				+ "The import a.b.c is never used\n"
				+ "----------\n"
				+ "2. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.two/p/q/r/Main.java (at line 3)\n"
				+ "	import x.y.z.*;\n"
				+ "	       ^^^^^\n"
				+ "The import x.y.z is never used\n"
				+ "----------\n"
				+ "2 problems (2 warnings)\n");
	}
	/*
	 * Same as above test case, but two binary modules export the package, without any .class files
	 */
	public void testBug522472b() { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		File srcDir = new File(directory);
		String moduleLoc = directory + File.separator + "mod.one"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		List<String> files = new ArrayList<>(); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW
		writeFileCollecting(files, moduleLoc,
				"module-info.java",
				"module mod.one { \n" +
				"	exports x.y.z;\n" +
				"	exports a.b.c;\n" +
				"}");
		writeFileCollecting(files, moduleLoc + File.separator + "x" + File.separator + "y" + File.separator + "z", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
				"X.java",
				"package x.y.z;\n");
		writeFileCollecting(files, moduleLoc + File.separator + "a" + File.separator + "b" + File.separator + "c", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
				"A.java",
				"package a.b.c;\n" +
				"public class A {}");

		moduleLoc = directory + File.separator + "mod.one.a"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		writeFileCollecting(files, moduleLoc,
				"module-info.java",
				"module mod.one.a { \n" +
				"	exports x.y.z;\n" +
				"}");
		writeFileCollecting(files, moduleLoc + File.separator + "x" + File.separator + "y" + File.separator + "z", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
				"X.java",
				"package x.y.z;\n");
		StringBuilder buffer = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buffer.append("-d " + OUTPUT_DIR + File.separator + out ) //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-source-path " + "\"" + directory + "\" "); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT

		runConformModuleTest(files,
				buffer,
				"",
				"");

		Util.flushDirectoryContent(srcDir);
		files.clear();
		moduleLoc = directory + File.separator + "mod.two"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		writeFileCollecting(files, moduleLoc,
				"module-info.java",
				"module mod.two { \n" +
					"	requires mod.one;\n" +
					"	requires mod.one.a;\n" +
				"}");
		writeFileCollecting(files, moduleLoc + File.separator + "p" + File.separator + "q" + File.separator + "r", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
				"Main.java",
				"package p.q.r;\n" +
				"import a.b.c.*;\n" +
				"import x.y.z.*;\n" +
				"public class Main {"
				+ "}");
		buffer = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buffer.append("-d " + OUTPUT_DIR + File.separator + out ) //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-path " + "\"" + OUTPUT_DIR + File.separator + out + "\" ") //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
			.append(" --module-source-path " + "\"" + directory + "\" "); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		runNegativeModuleTest(files,
				buffer,
				"",
				"----------\n" +
				"1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.two/module-info.java (at line 2)\n" +
				"	requires mod.one;\n" +
				"	^^^^^^^^^^^^^^^^\n" +
				"The package x.y.z is accessible from more than one module: mod.one, mod.one.a\n" +
				"----------\n" +
				"2. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.two/module-info.java (at line 3)\n" +
				"	requires mod.one.a;\n" +
				"	^^^^^^^^^^^^^^^^^^\n" +
				"The package x.y.z is accessible from more than one module: mod.one, mod.one.a\n" +
				"----------\n" +
				"----------\n" +
				"3. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.two/p/q/r/Main.java (at line 3)\n" +
				"	import x.y.z.*;\n" +
				"	       ^^^^^\n" +
				"The import x.y.z cannot be resolved\n" +
				"----------\n" +
				"3 problems (3 errors)\n",
				"reads package x.y.z from both");
	}
	public void testBug522472d() { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		String moduleLoc = directory + File.separator + "mod.one"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		List<String> files = new ArrayList<>(); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW
		writeFileCollecting(files, moduleLoc,
				"module-info.java",
				"module mod.one { \n" +
				"	exports x.y.z;\n" +
				"	exports a.b.c;\n" +
				"}");
		writeFileCollecting(files, moduleLoc + File.separator + "x" + File.separator + "y" + File.separator + "z", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
				"X.java",
				"package x.y.z;\n");
		writeFileCollecting(files, moduleLoc + File.separator + "a" + File.separator + "b" + File.separator + "c", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
				"A.java",
				"package a.b.c;\n" +
				"public class A {}");

		moduleLoc = directory + File.separator + "mod.one.a"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		writeFileCollecting(files, moduleLoc,
				"module-info.java",
				"module mod.one.a { \n" +
				"	exports x.y.z;\n" +
				"}");
		writeFileCollecting(files, moduleLoc + File.separator + "x" + File.separator + "y" + File.separator + "z", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
				"X.java",
				"package x.y.z;\n");
		StringBuilder buffer = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buffer.append("-d " + OUTPUT_DIR + File.separator + out ) //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-source-path " + "\"" + directory + "\" "); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT

		moduleLoc = directory + File.separator + "mod.two"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		writeFileCollecting(files, moduleLoc,
				"module-info.java",
				"module mod.two { \n" +
					"	requires mod.one;\n" +
					"	requires mod.one.a;\n" +
				"}");
		writeFileCollecting(files, moduleLoc + File.separator + "p" + File.separator + "q" + File.separator + "r", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
				"Main.java",
				"package p.q.r;\n" +
				"import a.b.c.*;\n" +
				"import x.y.z.*;\n" +
				"public class Main {"
				+ "}");

		runNegativeModuleTest(files,
				buffer,
				"",
				"----------\n" +
				"1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.two/module-info.java (at line 2)\n" +
				"	requires mod.one;\n" +
				"	^^^^^^^^^^^^^^^^\n" +
				"The package x.y.z is accessible from more than one module: mod.one, mod.one.a\n" +
				"----------\n" +
				"2. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.two/module-info.java (at line 3)\n" +
				"	requires mod.one.a;\n" +
				"	^^^^^^^^^^^^^^^^^^\n" +
				"The package x.y.z is accessible from more than one module: mod.one, mod.one.a\n" +
				"----------\n" +
				"----------\n" +
				"3. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.two/p/q/r/Main.java (at line 3)\n" +
				"	import x.y.z.*;\n" +
				"	       ^^^^^\n" +
				"The package x.y.z is accessible from more than one module: mod.one, mod.one.a\n" +
				"----------\n" +
				"3 problems (3 errors)\n",
				"reads package x.y.z from both");
	}
	public void testIssue2357_001() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		String moduleLoc = directory + File.separator + "mod.one"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		List<String> files = new ArrayList<>(); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.one { \n" +
								"    exports pack1;\n" +
								"    exports pack2 to second;\n" +
								"    opens pack3;\n" +
								"    opens pack4 to third;\n" +
								"    uses pack5.X51;\n" +
								"    provides pack1.I11 with pack1.X11;\n" +
								"    requires static java.sql;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "pack1", "I11.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
				"package pack1;\n" +
						"public interface I11 {\n" +
				"}");
		writeFileCollecting(files, moduleLoc + File.separator + "pack1", "X11.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
						"package pack1;\n" +
						"public class X11 implements I11{\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "pack2", "X21.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
				"package pack2;\n" +
				"public class X21 {\n" +
				"}");
		writeFileCollecting(files, moduleLoc + File.separator + "pack3", "X31.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
				"package pack3;\n" +
				"public class X31 {\n" +
				"}");
		writeFileCollecting(files, moduleLoc + File.separator + "pack4", "X41.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
				"package pack4;\n" +
				"public class X41 {\n" +
				"}");
		writeFileCollecting(files, moduleLoc + File.separator + "pack5", "X51.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
				"package pack5;\n" +
				"public class X51 {\n" +
				"}");
		StringBuilder buffer = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buffer.append("-d " + OUTPUT_DIR + File.separator + out ) //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ");
		files.forEach(name -> buffer.append(" \"" + name + "\"")); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		runConformTest(new String[0],
				buffer.toString(),
				"",
				"",
				false);
		String expectedOutput = "// Compiled from module-info.java (version 9 : 53.0, no super bit)\n" +
				" module mod.one  {\n" +
				"  // Version: \n" +
				"\n" +
				"  requires static java.sql;\n" +
				"  requires java.base;\n" +
				"\n" +
				"  exports pack1;\n" +
				"  exports pack2 to second;\n" +
				"\n" +
				"  opens pack3;\n" +
				"  opens pack4 to third;\n" +
				"\n" +
				"  uses pack5.X51\n" +
				"\n" +
				"  provides pack1.I11 with pack1.X11;\n" +
				"\n" +
				"}";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + out + File.separator + "module-info.class", "module-info", expectedOutput); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
	}

	public void testPatchModuleSingle() { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out1 = OUTPUT_DIR + File.separator + "bin1"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		String src1 = OUTPUT_DIR + File.separator + "src1"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		List<String> files = new ArrayList<>(); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW
		writeFileCollecting(files, src1,
				"module-info.java",
				"module mod.one { \n" + // no exports!
				"}");
		writeFileCollecting(files, src1 + File.separator + "test1", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
				"A.java",
				"package test1;\n" +
				"public class A {}");
		StringBuilder buffer = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buffer.append("-d " + out1 ) //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			.append(" -9 ")
			.append(" -proc:none ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ");
		runConformModuleTest(
				files,
				buffer,
				"",
				"");

		files.clear();
		String out2 = "bin2";
		String src2 = OUTPUT_DIR + File.separator + "src2"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		writeFileCollecting(files, src2 + File.separator + "test2", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
				"B.java",
				"""
				package test2;
				import test1.A;
				class B extends A {}
				""");
		buffer = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buffer.append("-d " + OUTPUT_DIR + File.separator + out2 ) //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			.append(" -9 ")
			.append(" -proc:none ")
			.append(" --patch-module mod.one=\"").append(src2).append("\" ")
			.append(" --module-path \"")
			.append(Util.getJavaClassLibsAsString())
			.append(File.pathSeparatorChar)
			.append(out1)
			.append("\" ");
		runConformModuleTest(
				files,
				buffer,
				"",
				"");
	}

	public void testPatchModuleSingle_duplicateModule() { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String src1 = OUTPUT_DIR + File.separator + "src1"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		String src2 = OUTPUT_DIR + File.separator + "src2"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		List<String> files = new ArrayList<>(); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW
		writeFileCollecting(files, src1 + File.separator + "test1", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
				"A.java",
				"package test1;\n" +
				"public class A {}");
		StringBuilder buffer = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buffer.append(" -9 ") //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			.append(" -proc:none ")
			.append(" --patch-module mod.one=\"").append(src1).append("\" ")
			.append(" --patch-module mod.one=\"").append(src2).append("\" ")
			.append(" --module-path \"").append(Util.getJavaClassLibsAsString()).append("\" ");
		runNegativeModuleTest(
				files,
				buffer,
				"",
				"duplicate module in --patch-module: mod.one\n",
				"--patch-module specified more than once");
	}

	public void testPatchModuleSingle_duplicateLocation1() { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String src1 = OUTPUT_DIR + File.separator + "src1"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		List<String> files = new ArrayList<>(); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW
		writeFileCollecting(files, src1 + File.separator + "test1", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
				"A.java",
				"package test1;\n" +
				"public class A {}");
		StringBuilder buffer = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buffer.append(" -9 ") //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			.append(" -proc:none ")
			.append(" --patch-module mod.one=\"").append(src1).append("\"")
			.append(File.pathSeparatorChar).append("\"").append(src1).append("\" ")
			.append(" --module-path \"").append(Util.getJavaClassLibsAsString()).append("\" ");
		runNegativeModuleTest(
				files,
				buffer,
				"",
				"location ---OUTPUT_DIR_PLACEHOLDER---/src1 is specified more than once in --patch-module\n",
				"",
				JavacTestOptions.SKIP);
	}

	public void testPatchModuleSingle_duplicateLocation2() { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String src1 = OUTPUT_DIR + File.separator + "src1"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		List<String> files = new ArrayList<>(); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW
		writeFileCollecting(files, src1 + File.separator + "test1", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
				"A.java",
				"package test1;\n" +
				"public class A {}");
		StringBuilder buffer = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buffer.append(" -9 ") //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			.append(" -proc:none ")
			.append(" --patch-module mod.one=\"").append(src1).append("\" ")
			.append(" --patch-module mod.two=\"").append(src1).append("\" ")
			.append(" --module-path \"").append(Util.getJavaClassLibsAsString()).append("\" ");
		runNegativeModuleTest(
				files,
				buffer,
				"",
				"location ---OUTPUT_DIR_PLACEHOLDER---/src1 is specified more than once in --patch-module\n",
				"",
				JavacTestOptions.SKIP);
	}

	public void testPatchModuleSingle_syntaxErr() { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String src1 = OUTPUT_DIR + File.separator + "src1"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		List<String> files = new ArrayList<>(); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW
		writeFileCollecting(files, src1 + File.separator + "test1", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
				"A.java",
				"package test1;\n" +
				"public class A {}");
		StringBuilder buffer = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buffer.append(" -9 ") //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			.append(" -proc:none ")
			.append(" --patch-module mod.one \"").append(src1).append("\" ")
			.append(" --module-path \"").append(Util.getJavaClassLibsAsString()).append("\" ");
		runNegativeModuleTest(
				files,
				buffer,
				"",
				"invalid syntax for --patch-module: mod.one\n",
				"bad value for --patch-module option");
	}

	public void testPatchModuleSingle_noSuchModule() { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String src1 = OUTPUT_DIR + File.separator + "src1"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		List<String> files = new ArrayList<>(); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW
		writeFileCollecting(files, src1 + File.separator + "test1", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
				"A.java",
				"package test1;\n" +
				"public class A {}");
		StringBuilder buffer = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buffer.append(" -9 ") //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			.append(" -proc:none ")
			.append(" --patch-module mod.one=\"").append(src1).append("\" ")
			.append(" --module-path \"").append(Util.getJavaClassLibsAsString()).append("\" ");
		runNegativeModuleTest(
				files,
				buffer,
				"",
				"""
				----------
				1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src1/test1/A.java (at line 1)
					package test1;
					        ^^^^^
				mod.one cannot be resolved to a module
				----------
				1 problem (1 error)
				""",
				"module not found");
	}

	public void testPatchModuleMulti() { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		// separately compile two modules, then patch them both in one go:
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);

		// mod.one
		String out1 = OUTPUT_DIR + File.separator + "bin1"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		String src1 = OUTPUT_DIR + File.separator + "src1"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		List<String> files = new ArrayList<>(); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW
		writeFileCollecting(files, src1,
				"module-info.java",
				"module mod.one { \n" + // no exports!
				"}");
		writeFileCollecting(files, src1 + File.separator + "test1", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
				"A.java",
				"package test1;\n" +
				"public class A {}");
		StringBuilder buffer = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buffer.append("-d " + out1 ) //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			.append(" -9 ")
			.append(" -proc:none ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ");
		runConformModuleTest(
				files,
				buffer,
				"",
				"");

		// mod.two
		String out2 = OUTPUT_DIR + File.separator + "bin2"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		String src2 = OUTPUT_DIR + File.separator + "src2"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		files.clear();
		writeFileCollecting(files, src2,
				"module-info.java",
				"module mod.two { \n" + // no exports!
				"}");
		writeFileCollecting(files, src2 + File.separator + "test2", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
				"B.java",
				"package test2;\n" +
				"public class B {}");
		buffer = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buffer.append("-d " + out2 ) //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			.append(" -9 ")
			.append(" -proc:none ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ");
		runConformModuleTest(
				files,
				buffer,
				"",
				"");

		// two files to patch each of the above modules:
		files.clear();
		String out3 = "bin3";
		String src3 = OUTPUT_DIR + File.separator + "src3"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		writeFileCollecting(files, src3 + File.separator + "test3", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
				"C.java",
				"""
				package test2;
				import test1.A;
				class C extends A {}
				""");
		String src4 = OUTPUT_DIR + File.separator + "src4"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		writeFileCollecting(files, src4 + File.separator + "test4", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
				"E.java",
				"""
				package test4;
				import test2.B;
				class E extends B {}
				""");
		buffer = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buffer.append("-d " + OUTPUT_DIR + File.separator + out3 ) //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			.append(" -9 ")
			.append(" -proc:none ")
			.append(" --patch-module mod.one=\"").append(src3).append("\" ")
			.append(" --patch-module mod.two=\"").append(src4).append("\" ")
			.append(" --module-path \"")
			.append(Util.getJavaClassLibsAsString())
			.append(File.pathSeparatorChar)
			.append(out1)
			.append(File.pathSeparatorChar)
			.append(out2)
			.append("\" ");
		runConformModuleTest(
				files,
				buffer,
				"",
				"");
	}

	public void testPatchModuleMulti2() { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);

		String out = OUTPUT_DIR + File.separator + "bin"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		// mod.one
		String src = OUTPUT_DIR + File.separator + "src"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		String m1src = src+ File.separator + "mod.one"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		List<String> files = new ArrayList<>(); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW
		writeFileCollecting(files, m1src,
				"module-info.java",
				"module mod.one {}\n"); // no exports!
		writeFileCollecting(files, m1src + File.separator + "test1", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
				"A.java",
				"package test1;\n" +
				"public class A {}");

		// mod.two
		String m2src = src + File.separator + "mod.two"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		writeFileCollecting(files, m2src,
				"module-info.java",
				"module mod.two {} \n"); // no exports!
		writeFileCollecting(files, m2src + File.separator + "test2", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
				"B.java",
				"package test2;\n" +
				"public class B {}\n");

		// two files to patch each of the above modules:
		String srcPatch1 = OUTPUT_DIR + File.separator + "src1"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		writeFileCollecting(files, srcPatch1 + File.separator + "test3", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
				"C.java",
				"""
				package test2;
				import test1.A;
				class C extends A {}
				""");
		String srcPatch2 = OUTPUT_DIR + File.separator + "src2"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		writeFileCollecting(files, srcPatch2 + File.separator + "test4", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
				"E.java",
				"""
				package test4;
				import test2.B;
				class E extends B {}
				""");
		StringBuilder buffer = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buffer.append("-d ").append(out) //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			.append(" -9 ")
			.append(" -proc:none ")
			.append(" --module-source-path \"").append(src).append("\"")
			.append(" --patch-module mod.one=\"").append(srcPatch1).append("\" ")
			.append(" --patch-module mod.two=\"").append(srcPatch2).append("\" ")
			.append(" --module-path \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ");
		runConformModuleTest(
				files,
				buffer,
				"",
				"");
	}
	public void testGH2646() { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		try {
			class Counter implements Consumer<SplitPackageBinding> {
				int count;
				@Override
				public void accept(SplitPackageBinding t) {
					this.count++;
				}
			}
			Counter counter = new Counter();
			SplitPackageBinding.instanceListener = counter;
			runConformModuleTest(
				new String[] {
					"p/X.java",
					"package p;\n" +
					"public class X {\n" +
					"	java.sql.Connection con;\n" +
					"}",
					"module-info.java",
					"module mod.one { \n" +
					"	requires java.se;\n" +
					"}",
					"q/Y.java",
					"package q;\n" +
					"public class Y {\n" +
					"   java.awt.Image image;\n" +
					"}"
				},
				" -9 \"" + OUTPUT_DIR + File.separator + "module-info.java\" " //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
				+ "\"" + OUTPUT_DIR + File.separator + "q/Y.java\" "
				+ "\"" + OUTPUT_DIR + File.separator + "p/X.java\" "
				+ "-d " + OUTPUT_DIR ,
				"",
				"",
				true);
			assertTrue("Number of SplitPackageBinding created: "+counter.count, counter.count <= 20); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		} finally {
			SplitPackageBinding.instanceListener = null;
		}
	}

	public void testGH2748() throws IOException { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);

		List<String> modules = new ArrayList<>(); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW
		// compile two explicit modules
		for (String m : new String[] { "A", "B" }) { //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
			String out = OUTPUT_DIR + File.separator + "module" + m + File.separator + "bin"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
			String src = OUTPUT_DIR + File.separator + "module" + m + File.separator + "src"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
			modules.add(out);

			List<String> files = new ArrayList<>(); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW
			writeFileCollecting(files, src,
					"module-info.java",
					"module split.module" + m + " {\n" + //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
					"	exports pkg.bug.split.sub" + m + ";\n" +
					"}");
			writeFileCollecting(files, Paths.get(src, "pkg", "bug", "split", "sub" + m).toString(), //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
					"SubModule" + m + ".java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
					"package pkg.bug.split.sub" + m + ";\n" + //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
					"\n" +
					"public class SubModule" + m + " {\n" +
					"	\n" +
					"}");

			StringBuilder buffer = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
			buffer.append("-d " + out ) //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				.append(" -9 ")
				.append(" -proc:none ")
				.append(" -classpath \"")
				.append(Util.getJavaClassLibsAsString())
				.append("\" ");
			runConformModuleTest(
					files,
					buffer,
					"",
					"");
		}

		// compile a jar which serves as auto-module
		String[] sources = {
			"pkg/bug/service/IService.java",
			"""
				package pkg.bug.service;

				public interface IService {

				}""",
		};
		String jarPath = OUTPUT_DIR + File.separator + "autoModule.jar"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		Util.createJar(sources, jarPath, "1.8");
		modules.add(jarPath);

		// compile the main code which requires the other modules
		String outFinal = OUTPUT_DIR + File.separator + "modularizedApp" + File.separator + "bin"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		String srcFinal = OUTPUT_DIR + File.separator + "modularizedApp" + File.separator + "src"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT

		List<String> files = new ArrayList<>(); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW
		writeFileCollecting(files, srcFinal,
				"module-info.java",
				"""
					module split.app {\t
						requires autoModule;
						requires split.moduleA;
						requires split.moduleB;
					\t
						exports pkg.bug.app;
					}""");
		writeFileCollecting(files, Paths.get(srcFinal, "pkg", "bug", "app").toString(),
				"App.java",
				"""
					package pkg.bug.app;

					import pkg.bug.split.subA.SubModuleA;

					public class App {
						public static void main(String[] args) {
							new SubModuleA();
						}
					}""");
		writeFileCollecting(files, Paths.get(srcFinal, "pkg", "bug", "service", "impl").toString(),
				"AppServiceImpl.java",
				"""
					package pkg.bug.service.impl;

					import pkg.bug.service.IService;

					public class AppServiceImpl implements IService {

					}""");

		String modulePath = String.join(File.pathSeparator, modules);
		StringBuilder buffer = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buffer.append("-d " + outFinal ) //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			.append(" -9 ")
			.append(" -proc:none ")
			.append(" --module-path \"")
			.append(Util.getJavaClassLibsAsString())
			.append(File.pathSeparatorChar)
			.append(modulePath)
			.append("\" ")
			.append(" --add-modules autoModule ");
		runConformModuleTest(
				files,
				buffer,
				"",
				"""
					----------
					1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/modularizedApp/src/module-info.java (at line 2)
						requires autoModule;
						         ^^^^^^^^^^
					Name of automatic module 'autoModule' is unstable, it is derived from the module's file name.
					----------
					1 problem (1 warning)
					""");
	}
}
