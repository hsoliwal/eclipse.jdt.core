/*******************************************************************************
 * Copyright (c) 2022 Andrey Loskutov (loskutov@gmx.de) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrey Loskutov (loskutov@gmx.de) - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import junit.framework.Test;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.tests.compiler.regression.BatchCompilerTest.SubstringMatcher;
import org.eclipse.jdt.core.tests.util.Util;

public class BatchCompilerTest_17 extends AbstractBatchCompilerTest {

	/**
	 * This test suite only needs to be run on one compliance.
	 *
	 * @see TestAll
	 */
	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_17);
	}

	public static Class<BatchCompilerTest_17> testClass() {
		return BatchCompilerTest_17.class;
	}

	public BatchCompilerTest_17(String name) {
		super(name);
	}

	static {
		// TESTS_NAMES = new String [] { "testGHI1774_Expression" };
	}

	/**
	 * Test tries to compile same sources <b>in parallel</b> to different output directories.
	 * There should be no interdependencies between compiler threads, but the test failed
	 * due some error in the initialization of shared static compiler data.
	 * @see <a href="https://github.com/eclipse-jdt/eclipse.jdt.core/issues/183">bug 183</a>
	 */
	public void testParallelCompilation() throws Throwable { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		Path root = Files.createDirectories(Paths.get(OUTPUT_DIR));
		String[] sources = new String[] {
				"X.java",
				"public class X {\n"
				+ "	public static void main(String[] args) {\n"
				+ "		new X().printHello();\n"
				+ "		new Thread(() -> {\n"
				+ "			new X().printHello();\n"
				+ "		}).start();\n"
				+ "	}\n"
				+ "	private void printHello() {\n"
				+ "		System.out.println(\"Hello \" + Thread.currentThread());\n"
				+ "	}\n"
				+ "}",
				"ExampleEnum.java",
				"public enum ExampleEnum {\n"
				+ "	A, B, C;\n"
				+ "}\n"
				+ ""
			};

		for (int i = 0; i < sources.length; i += 2) { //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
			Files.writeString(root.resolve(sources[i]), sources[i + 1]);
		}

		Map<String, Object> map = new TreeMap<>(compileParallel(sources)); //FLLDCIM: MARK — M05 M3_SORTED_LANES NEW
		Optional<Object> err = map.values().stream().filter(x -> (x instanceof Throwable)).findFirst();
		if (err.isPresent()) {
			throw (Throwable) err.get();
		}
	}

	public Map<String, Object> compileParallel(String[] sources) throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		int threads = Math.max(16, 2 * Runtime.getRuntime().availableProcessors());
		CountDownLatch startLatch = new CountDownLatch(1);
		CountDownLatch finishLatch = new CountDownLatch(threads);
		ExecutorService executor = Executors.newFixedThreadPool(threads);
		Map<String, Object> results = new ConcurrentHashMap<>();
		for (int i = 0; i < threads; i++) { //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
			String name = String.format("Compile-Thread-%02d", i); //FLLDCIM: MARK — M11 FORMAT_PRECOMPUTE CALL
			executor.execute(() -> {
				Thread.currentThread().setName(name);
				try {
					results.put(name, compile(sources, startLatch));
				} catch (Throwable e) {
					results.put(name, e);
				} finally {
					finishLatch.countDown();
				}
			});
		}
		startLatch.countDown();
		finishLatch.await();
		executor.shutdownNow();
		return results;
	}

	private String compile(String[] sources, CountDownLatch startLatch) throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		String folder = "BatchCompilerTest_17_" + Thread.currentThread().getName(); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		File outputFolder = new File(OUTPUT_DIR, folder);
		Files.createDirectories(outputFolder.toPath());
		StringBuilder output = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		StringBuilder error = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		startLatch.await();
		runTest(
				true,
				sources,
		        "\"" + OUTPUT_DIR +  File.separator + sources[0] + "\"" //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		        + " -g -nowarn -target 11 -source 11 --release 11"
		        + " -encoding UTF-8"
		        + " -d \"" + outputFolder + "\"",
		        new SubstringMatcher("") {
		    		@Override
		    		boolean match(String out) {
		    			output.append(out); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		    			return super.match(out);
		    		}
		    	},
		        new Matcher() {
		    		@Override
		    		String expected() {
		    			return org.eclipse.jdt.internal.compiler.util.Util.EMPTY_STRING;
		    		}
		    		@Override
		    		boolean match(String err) {
		    			error.append(err); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		    			return err != null && err.length() == 0;
		    		}
		    	},
		        false);

		return "StdErr: " + error + " StdOut: " + output; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
	}

	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1774
	// Check behavior of expression switch in JDK18-
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/3096#issuecomment-2417954288
	public void testGHI1774_Expression() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		String javaVersion = System.getProperty("java.version");
		if (javaVersion != null && JavaCore.compareJavaVersions(javaVersion, "18") < 0)
			return;
		String path = LIB_DIR;
		String libPath = null;
		if (path.endsWith(File.separator)) {
			libPath = path + "lib.jar"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		} else {
			libPath = path + File.separator + "lib.jar"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		}
		Util.createJar(new String[] {
			"p/Color.java",
			"package p;\n" +
			"public enum Color {\n" +
			"	R, Y;\n" +
			"	public static Color getColor() {\n" +
			"		return R;\n" +
			"	}\n" +
			"}",
		},
		libPath,
		JavaCore.VERSION_18);
		this.runConformTest(
			new String[] {
				"src/p/X.java",
				"package p;\n"
				+ "import p.Color;\n"
				+ "public class X {\n"
				+ "	public static void main(String argv[]) {\n"
				+ "		Color c = Color.getColor();\n"
				+ "		try {\n"
				+ "			int a = switch (c) {\n"
				+ "				case R -> 1;\n"
				+ "				case Y -> 2;\n"
				+ "			};\n"
				+ "		} catch (IncompatibleClassChangeError e) {\n"
				+ "			System.out.print(\"OK\");\n"
				+ "		} catch (Exception e) {\n"
				+ "			System.out.print(\"NOT OK: \" + e);\n"
				+ "		}\n"
				+ "			System.out.print(\"END\");\n"
				+ "	}\n"
				+ "}",
			},
			"\"" + OUTPUT_DIR +  File.separator + "src/p/X.java\"" //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
			+ " -cp \"" + LIB_DIR + File.separator + "lib.jar\""
			+ " -sourcepath \"" + OUTPUT_DIR +  File.separator + "src\""
			+ " -source 18 -warn:none"
			+ " -d \"" + OUTPUT_DIR + File.separator + "bin\" ",
			"",
			"",
			true);
		this.verifier.execute("p.X", new String[] {OUTPUT_DIR + File.separator + "bin", libPath}, new String[0], new String[] {"--enable-preview"}); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		assertEquals("Incorrect output", "END", this.verifier.getExecutionOutput());
		Util.createJar(new String[] {
				"p/Color.java",
				"package p;\n" +
				"public enum Color {\n" +
				"	R, Y, B;\n" +
				"	public static Color getColor() {\n" +
				"		return B;\n" +
				"	}\n" +
				"}",
			},
			libPath,
			JavaCore.VERSION_18);
		this.verifier.execute("p.X", new String[] {OUTPUT_DIR + File.separator + "bin", libPath}, new String[0], new String[] {"--enable-preview"}); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		assertEquals("Incorrect output", "OKEND", this.verifier.getExecutionOutput());
	}
}
