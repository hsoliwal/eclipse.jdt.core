/*******************************************************************************
 * Copyright (c) 2024, 2025 GK Software SE, and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Stephan Herrmann - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import junit.framework.Test;
import org.eclipse.jdt.core.util.ClassFileBytesDisassembler;
import org.eclipse.jdt.core.util.ClassFormatException;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;

@RunJavac
public class ModuleImportTests extends AbstractModuleCompilationTest {

	static {
//		 TESTS_NAMES = new String[] { "test000_previewDisabled" };
		// TESTS_NUMBERS = new int[] { 1 };
		// TESTS_RANGE = new int[] { 298, -1 };
	}

	public ModuleImportTests(String name) {
		super(name);
	}

	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_23);
	}

	public static Class<?> testClass() {
		return ModuleImportTests.class;
	}

	public void test001_simpleOK() throws IOException, ClassFormatException { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		runConformModuleTest(
			new String[] {
				"p/X.java",
				"""
					package p;
					import /*ignoreme*/ module java.sql;
					public class X {
						public static void main(String[] args) {
							@SuppressWarnings("unused")
							Connection con = null;
						}
					}
					""",
				"module-info.java",
				"""
					module mod.one {
						requires java.sql;
					}
					"""
	        },
			" -25 \"" + OUTPUT_DIR +  File.separator + "module-info.java\" " //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
	        + "\"" + OUTPUT_DIR +  File.separator + "p/X.java\"",
	        "",
	        "",
	        true);
		verifyClassFile("version 25 : 69.0", "p/X.class", ClassFileBytesDisassembler.SYSTEM);
	}

	public void test001_simple_24() throws IOException, ClassFormatException { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		runNegativeModuleTest(
			new String[] {
				"p/X.java",
				"""
					package p;
					import module java.sql;
					public class X {
						public static void main(String[] args) {
							@SuppressWarnings("unused")
							Connection con = null;
						}
					}
					""",
				"module-info.java",
				"""
					module mod.one {
						requires java.sql;
					}
					"""
			},
			" -24 \"" + getSourceDir() +  File.separator + "module-info.java\" " //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
			+ "\"" + getSourceDir() +  File.separator + "p/X.java\"",
			"",
			"""
			----------
			1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/p/X.java (at line 2)
				import module java.sql;
				              ^^^^^^^^
			The Java feature 'Module Import Declarations' is only available with source level 25 and above
			----------
			2. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/p/X.java (at line 6)
				Connection con = null;
				^^^^^^^^^^
			Connection cannot be resolved to a type
			----------
			2 problems (2 errors)
			""",
			true,
			"not supported");
	}

	public void test002_moduleNotRead() { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		runNegativeModuleTest(
			new String[] {
				"p/X.java",
				"""
					package p;
					import module java.sql;
					public class X {
						public static void main(String[] args) {
							@SuppressWarnings("unused")
							Connection con = null;
						}
					}
					""",
				"module-info.java",
				"""
					module mod.one {
					}
					"""
	        },
			" -25 \"" + getSourceDir() +  File.separator + "module-info.java\" " //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
	        + "\"" + getSourceDir() +  File.separator + "p/X.java\"",
	        "",
	        """
				----------
				1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/p/X.java (at line 2)
					import module java.sql;
					              ^^^^^^^^
				Module mod.one does not read module java.sql
				----------
				2. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/p/X.java (at line 6)
					Connection con = null;
					^^^^^^^^^^
				Connection cannot be resolved to a type
				----------
				2 problems (2 errors)
				""",
	        true,
	        "read");
	}

	public void test003_unresolvableModule() { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		runNegativeModuleTest(
			new String[] {
				"p/X.java",
				"""
					package p;
					import module missing;
					@SuppressWarnings("preview")
					public class X {
						public static void main(String[] args) {
							@SuppressWarnings("unused")
							Connection con = null;
						}
					}
					""",
				"module-info.java",
				"""
					module mod.one {
					}
					"""
	        },
			" -25 \"" + getSourceDir() +  File.separator + "module-info.java\" " //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
	        + "\"" + getSourceDir() +  File.separator + "p/X.java\"",
	        "",
	        """
				----------
				1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/p/X.java (at line 2)
					import module missing;
					              ^^^^^^^
				The import missing cannot be resolved
				----------
				2. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/p/X.java (at line 7)
					Connection con = null;
					^^^^^^^^^^
				Connection cannot be resolved to a type
				----------
				2 problems (2 errors)
				""",
	        true,
	        "imported module not found");
	}

	public void test004_selfImport_OK() throws IOException, ClassFormatException { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		String modsDir = getSourceDir() +  File.separator + "mods"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		String modOneDir = modsDir + File.separator + "mod.one"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		List<String> files = new ArrayList<>(); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW
		writeFileCollecting(files, modOneDir + File.separator + "p1", "X1.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
				"""
					package p1;
					public class X1 {}
					""");
		writeFileCollecting(files, modOneDir + File.separator + "p2", "X2.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
				"""
					package p2;
					public class X2 {}
					""");
		writeFileCollecting(files, modOneDir + File.separator + "p", "X.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
				"""
					package p;
					import module mod.one;
					public class X {
						X1 x1;
						X2 x2;
					}
					""");
		writeFileCollecting(files, modOneDir, "module-info.java",
				"""
					module mod.one {
						exports p1;
						exports p2 to mod.one;
					}
					""");
		StringBuilder commandLine = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		commandLine.append(" -25 "); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		runConformModuleTest(
				files,
				commandLine,
				"",
				"");
		String classFile = String.join(File.separator, "p", "X.class");
		verifyClassFile("version 25 : 69.0", classFile, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test005_selfImport_NOK() { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		String modsDir = getSourceDir() +  File.separator + "mods"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		String modOneDir = modsDir + File.separator + "mod.one"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		List<String> files = new ArrayList<>(); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW
		writeFileCollecting(files, modOneDir + File.separator + "p1", "X1.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
				"""
					package p1;
					public class X1 {}
					""");
		writeFileCollecting(files, modOneDir + File.separator + "p", "X.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
				"""
					package p;
					import module mod.one;
					@SuppressWarnings("preview")
					public class X {
						X1 x1;
					}
					""");
		writeFileCollecting(files, modOneDir, "module-info.java",
				"""
					module mod.one {
						exports p1 to mod.other;
					}
					""");
		StringBuilder commandLine = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		commandLine.append(" -25 "); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL

		runNegativeModuleTest(
				files,
				commandLine,
				"",
				"""
				----------
				1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mods/mod.one/p/X.java (at line 5)
					X1 x1;
					^^
				X1 cannot be resolved to a type
				----------
				1 problem (1 error)
				""",
				"cannot find symbol"); // javac additionally reports warning: [module] module not found: mod.other
	}

	public void test006_selfImportInModule() throws IOException, ClassFormatException { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		String modsDir = getSourceDir()+  File.separator + "mods"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		String modOneDir = modsDir + File.separator + "mod.one"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		List<String> files = new ArrayList<>(); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW
		writeFileCollecting(files, modOneDir + File.separator + "api", "IService.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
				"""
					package api;
					public interface IService {}
					""");
		writeFileCollecting(files, modOneDir + File.separator + "impl", "ServiceImpl.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
				"""
					package impl;
					import api.IService;
					public class ServiceImpl implements IService {
						public static IService provider() { return new ServiceImpl(); }
					}
					""");
		writeFileCollecting(files, modOneDir, "module-info.java",
				"""
					import module mod.one;
					module mod.one {
						exports api;
						exports impl to mod.one;
						provides IService with ServiceImpl;
					}
					""");
		StringBuilder commandLine = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		commandLine.append(" -25 "); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		runConformModuleTest(
				files,
				commandLine,
				"",
				"");
		verifyClassFile("version 25 : 69.0", "module-info.class", ClassFileBytesDisassembler.SYSTEM);
	}

	public void test007_shadowing() throws IOException, ClassFormatException { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		String srcDir = getSourceDir();
		List<String> files = new ArrayList<>(); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW
		writeFileCollecting(files, srcDir + File.separator + "p1", "Connection.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
				"""
					package p1;
					public class Connection {
						public void foo() {}
					}
					""");
		writeFileCollecting(files, srcDir, "module-info.java",
				"""
					module mod.one {
						requires java.sql;
					}
					""");
		writeFileCollecting(files, srcDir + File.separator + "p2", "Client.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
				"""
					package p2;
					import module java.sql;
					import p1.Connection;
					class Client {
						void m(Connection c, ConnectionBuilder builder) { // ConnectionBuiler is from java.sql
							c.foo(); // ensure we select p1.Connection
						}
					}
					""");
		StringBuilder commandLine = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		commandLine.append(" -25 "); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL

		runConformModuleTest(
				files,
				commandLine,
				"",
				"");
		String classFile = String.join(File.separator, "p2", "Client.class");
		verifyClassFile("version 25 : 69.0", classFile, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test008_shadowing() throws IOException, ClassFormatException { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		String srcDir = getSourceDir();
		List<String> files = new ArrayList<>(); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW
		writeFileCollecting(files, srcDir + File.separator + "p1", "Connection.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
				"""
					package p1;
					public class Connection {
						public void foo() {}
					}
					""");
		writeFileCollecting(files, srcDir, "module-info.java",
				"""
					module mod.one {
						requires java.sql;
					}
					""");
		writeFileCollecting(files, srcDir + File.separator + "p2", "Client.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
				"""
					package p2;
					import module java.sql;
					import p1.*;
					@SuppressWarnings("unused") // module import is not actually used
					class Client {
						void m(Connection c) {
							c.foo(); // ensure we select p1.Connection
						}
					}
					""");
		StringBuilder commandLine = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		commandLine.append(" -25 "); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL

		runConformModuleTest(
				files,
				commandLine,
				"",
				"");
		String classFile = String.join(File.separator, "p2", "Client.class");
		verifyClassFile("version 25 : 69.0", classFile, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test008_shadowing_static_nested() throws IOException, ClassFormatException { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		String srcDir = getSourceDir() + File.separator + "src"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		List<String> files = new ArrayList<>(); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW
		writeFileCollecting(files, srcDir + File.separator + "p1", "Outer.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
				"""
					package p1;
					public class Outer {
						public static class Connection {
							public void foo() {}
						}
					}
					""");
		writeFileCollecting(files, srcDir, "module-info.java",
				"""
					module mod.one {
						requires java.sql;
					}
					""");
		writeFileCollecting(files, srcDir + File.separator + "p2", "Client.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
				"""
					package p2;
					import module java.sql;
					import p1.Outer.*;
					@SuppressWarnings( "unused" ) // module import is not actually used
					class Client {
						void m(Connection c) {
							c.foo(); // ensure we select p1.Outer.Connection
						}
					}
					""");
		StringBuilder commandLine = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		commandLine.append(" -25 "); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL

		runConformModuleTest(files, commandLine, "", "");
		String classFile = String.join(File.separator, "p2", "Client.class");
		verifyClassFile("version 25 : 69.0", classFile, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test009_ambiguous_modules() { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		String srcDir = getSourceDir();
		List<String> files = new ArrayList<>(); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW
		String modOneDir = srcDir + File.separator + "mod.one"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		writeFileCollecting(files, modOneDir, "module-info.java",
				"""
					module mod.one {
						exports p1;
						exports p2;
						requires transitive java.sql;
					}
					""");
		writeFileCollecting(files, modOneDir + File.separator + "p1", "Connection.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
				"""
					package p1;
					public class Connection {
					}
					""");
		writeFileCollecting(files, modOneDir + File.separator + "p1", "Other.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
				"""
					package p1;
					public class Other {
					}
					""");
		writeFileCollecting(files, modOneDir + File.separator + "p2", "Other.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
				"""
					package p2;
					public class Other{
					}
					""");

		String modTwoDir = srcDir + File.separator + "mod.two"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		writeFileCollecting(files, modTwoDir, "module-info.java",
				"""
					module mod.two {
						requires mod.one;
					}
					""");
		writeFileCollecting(files, modTwoDir + File.separator + "p3", "Client.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
				"""
					package p3;
					import module mod.one;
					class Client {
						Connection conn; // module conflict mod.one java.sql (via requires transitive)
						Other other; // package conflict mod.one/p1 mod.one/p2
					}
					""");
		StringBuilder commandLine = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		commandLine.append(" -25 "); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		commandLine.append(" --module-source-path \"").append(srcDir).append("\""); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		commandLine.append(" -d \"").append(OUTPUT_DIR).append(File.separatorChar).append("bin").append("\""); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL

		runNegativeModuleTest(
				files,
				commandLine,
				"",
				"""
					----------
					1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.two/p3/Client.java (at line 4)
						Connection conn; // module conflict mod.one java.sql (via requires transitive)
						^^^^^^^^^^
					The type Connection is ambiguous
					----------
					2. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.two/p3/Client.java (at line 5)
						Other other; // package conflict mod.one/p1 mod.one/p2
						^^^^^
					The type Other is ambiguous
					----------
					2 problems (2 errors)
					""",
				"reference to Connection is ambiguous");
	}

	public void test009_ambiguous_modules2() { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		// module conflict via separate module imports based on separate requires directly in mod.two
		String srcDir = getSourceDir();
		List<String> files = new ArrayList<>(); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW
		String modOneDir = srcDir + File.separator + "mod.one"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		writeFileCollecting(files, modOneDir, "module-info.java",
				"""
					module mod.one {
						exports p1;
					}
					""");
		writeFileCollecting(files, modOneDir + File.separator + "p1", "Connection.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
				"""
					package p1;
					public class Connection {
					}
					""");

		String modTwoDir = srcDir + File.separator + "mod.two"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		writeFileCollecting(files, modTwoDir, "module-info.java",
				"""
					module mod.two {
						requires mod.one;
						requires java.sql;
					}
					""");
		writeFileCollecting(files, modTwoDir + File.separator + "p3", "Client.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
				"""
					package p3;
					import module mod.one;
					import module java.sql;
					@SuppressWarnings("preview")
					class Client {
						Connection conn; // module conflict mod.one java.sql
					}
					""");
		StringBuilder commandLine = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		commandLine.append(" -25 "); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		commandLine.append(" --module-source-path \"").append(srcDir).append("\""); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		commandLine.append(" -d \"").append(OUTPUT_DIR).append(File.separatorChar).append("bin").append("\""); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL

		runNegativeModuleTest(
				files,
				commandLine,
				"",
				"""
					----------
					1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.two/p3/Client.java (at line 6)
						Connection conn; // module conflict mod.one java.sql
						^^^^^^^^^^
					The type Connection is ambiguous
					----------
					1 problem (1 error)
					""",
				"reference to Connection is ambiguous");
	}

	public void test010_notAccessible() { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		String srcDir = OUTPUT_DIR + File.separator + "src"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		String modOneDir = srcDir + File.separator + "mod.one"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		List<String> files = new ArrayList<>(); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW
		writeFileCollecting(files, modOneDir, "module-info.java",
				"""
					module mod.one {
						exports p1;
					}
					""");
		writeFileCollecting(files, modOneDir + File.separator + "p1", "NoAccess.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
				"""
					package p1;
					class NoAccess {
					}
					""");
		writeFileCollecting(files, modOneDir + File.separator + "p1", "Access.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
				"""
					package p1;
					public class Access {
					}
					""");
		String modTwoDir = srcDir + File.separator + "mod.two"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		writeFileCollecting(files, modTwoDir, "module-info.java",
				"""
					module mod.two {
						requires mod.one;
					}
					""");
		writeFileCollecting(files, modTwoDir + File.separator + "p2", "Client.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
				"""
					package p2;
					import module mod.one;
					@SuppressWarnings("preview")
					class Client {
						Access good;
						NoAccess bad;
					}
					""");
		StringBuilder commandLine = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		commandLine.append(" -25"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		commandLine.append(" --module-source-path ").append(srcDir); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		commandLine.append(" -d ").append(OUTPUT_DIR+File.separator+"bin"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL

		runNegativeModuleTest(
				files,
				commandLine,
				"",
				"""
					----------
					1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.two/p2/Client.java (at line 6)
						NoAccess bad;
						^^^^^^^^
					NoAccess cannot be resolved to a type
					----------
					1 problem (1 error)
					""",
				"cannot find symbol");
	}

	public void test011_transitive() throws IOException, ClassFormatException { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		String srcDir = OUTPUT_DIR + File.separator + "src"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		String modOneDir = srcDir + File.separator + "mod.one"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		List<String> files = new ArrayList<>(); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW
		writeFileCollecting(files, modOneDir, "module-info.java",
				"""
					module mod.one {
						exports p1;
					}
					""");
		writeFileCollecting(files, modOneDir + File.separator + "p1", "Access.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
				"""
					package p1;
					public class Access {
					}
					""");
		String modTwoDir = srcDir + File.separator + "mod.two"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		writeFileCollecting(files, modTwoDir, "module-info.java",
				"""
					module mod.two {
						requires transitive mod.one;
					}
					""");
		String modThreeDir = srcDir + File.separator + "mod.three"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		writeFileCollecting(files, modThreeDir, "module-info.java",
				"""
					module mod.three {
						requires mod.two;
					}
					""");
		writeFileCollecting(files, modThreeDir + File.separator + "p2", "Client.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
				"""
					package p2;
					import module mod.two;
					class Client {
						Access good;
					}
					""");
		StringBuilder commandLine = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		commandLine.append(" -25"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		commandLine.append(" --module-source-path ").append(srcDir); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL

		runConformModuleTest(
				files,
				commandLine,
				"",
				"");
		String classFile = String.join(File.separator, "mod.three", "p2", "Client.class");
		verifyClassFile("version 25 : 69.0", classFile, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test012_redundant() { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		List<String> files = new ArrayList<>(); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW
		writeFileCollecting(files, OUTPUT_DIR + File.separator + "p", "X.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
				"""
					package p;
					import module java.sql;
					import module java.sql; // redundant
					public class X {
						public static void main(String[] args) {
							@SuppressWarnings("unused")
							Connection con = null;
							Zork zork;
						}
					}
					""");
		writeFileCollecting(files, OUTPUT_DIR, "module-info.java",
				"""
					module mod.one {
						requires java.sql;
					}
					""");

		StringBuilder commandLine = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		commandLine.append(" -25"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL

		runNegativeModuleTest(
				files,
				commandLine,
				"",
				"""
					----------
					1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/p/X.java (at line 3)
						import module java.sql; // redundant
						              ^^^^^^^^
					The import java.sql is never used
					----------
					2. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/p/X.java (at line 8)
						Zork zork;
						^^^^
					Zork cannot be resolved to a type
					----------
					2 problems (1 error, 1 warning)
					""",
				"cannot find symbol");
	}

	public void test013_inUnnamedModule() throws IOException, ClassFormatException { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		runConformModuleTest(
			new String[] {
				"p/X.java",
				"""
					package p;
					import module java.sql;
					public class X {
						Connection con = null;
					}
					"""
	        },
			" -25 " //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
	        + "\"" + OUTPUT_DIR +  File.separator + "p/X.java\"",
	        "",
	        "",
	        true);
		verifyClassFile("version 25 : 69.0", "p/X.class", ClassFileBytesDisassembler.SYSTEM);
	}

	public void test014_moduleAsPackageName_regular() {
		List<String> files = new ArrayList<>(); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW
		writeFileCollecting(files, OUTPUT_DIR + File.separator + "module", "Z.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
				"""
					package module;
					public class Z {}
					""");
		writeFileCollecting(files, OUTPUT_DIR + File.separator + "test", "X.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
				"""
					package test;
					import module.Z;
					public class X extends Z {}
					""");
		StringBuilder commandLine = new StringBuilder(" -25"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		runConformModuleTest(files, commandLine, "", "");
	}


	public void test014_moduleAsPackageName_moduleInfo() {
		List<String> files = new ArrayList<>(); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW
		writeFileCollecting(files, OUTPUT_DIR + File.separator + "module", "Z.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
				"""
					package module;
					public class Z {}
					""");
		writeFileCollecting(files, OUTPUT_DIR, "module-info.java",
				"""
					import module.Z;
					module one {
						uses Z;
					}
					""");
		StringBuilder commandLine = new StringBuilder(" -25"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		runConformModuleTest(files, commandLine, "", "");
	}

	public void testIllegalModifierRequiresJavaBase_2() { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		List<String> files = new ArrayList<>(); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW
		writeFileCollecting(files, OUTPUT_DIR, "module-info.java",
				"""
					module one {
						requires static java.base;
					}
					""");
		runNegativeModuleTest(files,
				new StringBuilder(" --release 25"), //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
				"",
				"""
				----------
				1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/module-info.java (at line 2)
					requires static java.base;
					         ^^^^^^^^^^^^^^^^
				Modifiers are not allowed for dependence on module 'java.base'
				----------
				1 problem (1 error)
				""",
				"modifier static not allowed here");
	}

	public void testIllegalModifierRequiresJavaBase_3() throws IOException, ClassFormatException {
		List<String> files = new ArrayList<>(); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW
		writeFileCollecting(files, OUTPUT_DIR, "module-info.java",
				"""
					module one {
						requires transitive java.base;
					}
					""");
		runConformModuleTest(files,
				new StringBuilder(" --release 25"), //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
				"",
				"");
		verifyClassFile("version 25 : 69.0", "module-info.class", ClassFileBytesDisassembler.SYSTEM,
				this.complianceLevel < ClassFileConstants.JDK25); // Skipped for javac < 25 due to https://bugs.openjdk.org/browse/JDK-8347646 - fixed in 25
	}

	public void testIllegalModifierRequiresJavaBase_3_24() throws IOException, ClassFormatException { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		List<String> files = new ArrayList<>(); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW
		writeFileCollecting(files, OUTPUT_DIR, "module-info.java",
				"""
					module one {
						requires transitive java.base;
					}
					""");
		runNegativeModuleTest(files,
				new StringBuilder(" --release 24"), //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
				"",
				"""
				----------
				1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/module-info.java (at line 2)
					requires transitive java.base;
					         ^^^^^^^^^^^^^^^^^^^^
				Modifiers are not allowed for dependence on module 'java.base' for source level below 25
				----------
				1 problem (1 error)
				""",
				"not supported");
	}

	public void testIllegalModifierRequiresJavaBase_4() { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		List<String> files = new ArrayList<>(); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW
		writeFileCollecting(files, OUTPUT_DIR, "module-info.java",
				"""
					module one {
						requires static java.base;
					}
					""");
		runNegativeModuleTest(files,
				new StringBuilder(" --release 25"), //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
				"",
				"""
				----------
				1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/module-info.java (at line 2)
					requires static java.base;
					         ^^^^^^^^^^^^^^^^
				Modifiers are not allowed for dependence on module 'java.base'
				----------
				1 problem (1 error)
				""",
				"modifier static not allowed here");
	}


	public void testUseRequiresTransitiveJavaBase() throws IOException, ClassFormatException { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		List<String> files = new ArrayList<>(); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW
		writeFileCollecting(files, OUTPUT_DIR, "module-info.java",
				"""
					module one {
						requires transitive java.base;
					}
					""");
		writeFileCollecting(files, OUTPUT_DIR + File.separator + "p1", "Client.java", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
				"""
				package p1;
				import module one;
				public class Client {
					BigDecimal num;
				}
				""");
		runConformModuleTest(files,
				new StringBuilder(" --release 25"), //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
				"",
				"");
		verifyClassFile("version 25 : 69.0", "module-info.class", ClassFileBytesDisassembler.SYSTEM);
		verifyClassFile("version 25 : 69.0", "p1/Client.class", ClassFileBytesDisassembler.SYSTEM);
	}

}
