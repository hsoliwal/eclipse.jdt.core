/*******************************************************************************
 * Copyright (c) 2017, 2021 GK Software AG and others.
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.ArrayList;
import org.eclipse.jdt.core.compiler.CompilationProgress;
import org.eclipse.jdt.core.compiler.batch.BatchCompiler;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.internal.compiler.batch.ClasspathLocation;
import org.eclipse.jdt.internal.compiler.batch.FileSystem;
import org.eclipse.jdt.internal.compiler.batch.Main;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

public abstract class AbstractBatchCompilerTest extends AbstractRegressionTest {

	protected static abstract class Matcher {
		abstract boolean match(String effective);
		abstract String expected(); // for use in JUnit comparison framework
	}

	/**
	 * Used for preview features especially.
	 * @param compilerVersion - CompilerOptions.version string
	 * @return true if spec version is same as compiler version
	 */
	public static boolean isJREVersionEqualTo(String compilerVersion) {
		String specVersion = System.getProperty("java.specification.version");
		return specVersion != null && Integer.valueOf(specVersion) == Integer.valueOf(compilerVersion);
	}
	/**
	 * Abstract normalizer for output comparison. This class merely embodies a
	 * chain of responsibility, plus the signature of the method of interest
	 * here, that is {@link #normalized(String) normalized}.
	 */
	protected static abstract class Normalizer {
		private final Normalizer nextInChain;
		Normalizer(Normalizer nextInChain) {
			this.nextInChain = nextInChain;
		}
		String normalized(String originalValue) {
			String result;
			if (this.nextInChain == null)
				result = Util.convertToIndependantLineDelimiter(originalValue);
			else
				result = this.nextInChain.normalized(originalValue);
			return result;
		}
	}

	/**
	 * This normalizer replaces occurrences of a given string with a given
	 * placeholder.
	 */
	protected static class StringNormalizer extends Normalizer {
		private final String match;
		private final int matchLength;
		private final String placeholder;
		StringNormalizer(Normalizer nextInChain, String match, String placeholder) {
			super(nextInChain);
			this.match = match;
			this.matchLength = match.length();
			this.placeholder = placeholder;
		}
		@Override
		String normalized(String originalValue) {
			String result;
			StringBuilder normalizedValueBuffer = new StringBuilder(originalValue); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
			int nextOccurrenceIndex;
			while ((nextOccurrenceIndex = normalizedValueBuffer.indexOf(this.match)) != -1) //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
				normalizedValueBuffer.replace(nextOccurrenceIndex,
						nextOccurrenceIndex + this.matchLength, this.placeholder);
			result = super.normalized(normalizedValueBuffer.toString());
			return result;
		}
	}

	protected static class TestCompilationProgress extends CompilationProgress {
		boolean isCanceled = false;
		int workedSoFar = 0;
		StringBuilder buffer = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		public void begin(int remainingWork) {
			this.buffer.append("----------\n[worked: 0 - remaining: ").append(remainingWork).append("]\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		}
		public void done() {
			this.buffer.append("----------\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		}
		public boolean isCanceled() {
			return this.isCanceled;
		}
		public void setTaskName(String name) {
			this.buffer.append(name).append('\n'); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		}
		public String toString() {
			return this.buffer.toString();
		}
		public void worked(int workIncrement, int remainingWork) {
			this.workedSoFar += workIncrement;
			this.buffer.append("[worked: ").append(this.workedSoFar).append(" - remaining: ").append(remainingWork).append("]\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		}
	}

	public static final String OUTPUT_DIR_PLACEHOLDER = "---OUTPUT_DIR_PLACEHOLDER---";
	public static final String LIB_DIR_PLACEHOLDER = "---LIB_DIR_PLACEHOLDER---";

	/**
	 * Normalizer instance that replaces occurrences of OUTPUT_DIR with
	 * OUTPUT_DIR_PLACEHOLDER and changes file separator to / if the
	 * platform file separator is different from /.
	 */
	protected static Normalizer outputDirNormalizer;
	static {
		if (File.separatorChar == '/') {
			outputDirNormalizer =
				new StringNormalizer(
					new StringNormalizer(
						null, OUTPUT_DIR, OUTPUT_DIR_PLACEHOLDER),
					LIB_DIR, LIB_DIR_PLACEHOLDER);
		}
		else {
			outputDirNormalizer =
				new StringNormalizer(
					new StringNormalizer(
						new StringNormalizer(
							null, File.separator, "/"),
						OUTPUT_DIR, OUTPUT_DIR_PLACEHOLDER),
					LIB_DIR, LIB_DIR_PLACEHOLDER);
		}
	}

	public AbstractBatchCompilerTest(String name) {
		super(name);
	}

	protected static final String JRE_HOME_DIR = Util.getJREDirectory();
	protected static final Main MAIN = new Main(null/*outWriter*/, null/*errWriter*/, false/*systemExit*/, null/*options*/, null/*progress*/);

	private static boolean CASCADED_JARS_CREATED;
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		CASCADED_JARS_CREATED = false; // initialization needed for each subclass individually
	}

	protected void createCascadedJars() throws IOException { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		if (CASCADED_JARS_CREATED) {
			return;
		}
		File libDir = new File(LIB_DIR);
		Util.delete(libDir); // make sure we recycle the libs
 		libDir.mkdirs(); //FLLDCIM: MARK — M31 M3FILE_IO CALL
		Util.createJar(
			new String[] {
				"p/A.java",
				"package p;\n" +
				"public class A {\n" +
				"}",
			},
			new String[] {
				"META-INF/MANIFEST.MF",
				"Manifest-Version: 1.0\n" +
				"Created-By: Eclipse JDT Test Harness\n" +
				"Class-Path: lib2.jar\n",
				"p/S1.java",
				"package p;\n" +
				"public class S1 {\n" +
				"}",
			},
			LIB_DIR + "/lib1.jar", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
			CompilerOptions.getFirstSupportedJavaVersion());
		Util.createJar(
			new String[] {
				"p/B.java",
				"package p;\n" +
				"public class B {\n" +
				"}",
				"p/R.java",
				"package p;\n" +
				"public class R {\n" +
				"  public static final int R2 = 2;\n" +
				"}",
			},
			new String[] {
				"p/S2.java",
				"package p;\n" +
				"public class S2 {\n" +
				"}",
			},
			LIB_DIR + "/lib2.jar", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
			CompilerOptions.getFirstSupportedJavaVersion());
		Util.createJar(
			new String[] {
				"p/C.java",
				"package p;\n" +
				"public class C {\n" +
				"}",
				"p/R.java",
				"package p;\n" +
				"public class R {\n" +
				"  public static final int R3 = 3;\n" +
				"}",
			},
			new String[] {
				"META-INF/MANIFEST.MF",
				"Manifest-Version: 1.0\n" +
				"Created-By: Eclipse JDT Test Harness\n" +
				"Class-Path: lib4.jar\n",
			},
			LIB_DIR + "/lib3.jar", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
			CompilerOptions.getFirstSupportedJavaVersion());
		Util.createJar(
			new String[] {
				"p/D.java",
				"package p;\n" +
				"public class D {\n" +
				"}",
			},
			new String[] {
				"META-INF/MANIFEST.MF",
				"Manifest-Version: 1.0\n" +
				"Created-By: Eclipse JDT Test Harness\n" +
				"Class-Path: lib1.jar lib3.jar\n",
			},
			LIB_DIR + "/lib4.jar", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
			CompilerOptions.getFirstSupportedJavaVersion());
		Util.createJar(
			new String[] {
				"p/C.java",
				"package p;\n" +
				"public class C {\n" +
				"}",
				"p/R.java",
				"package p;\n" +
				"public class R {\n" +
				"  public static final int R3 = 3;\n" +
				"}",
			},
			new String[] {
				"META-INF/MANIFEST.MF",
				"Manifest-Version: 1.0\n" +
				"Created-By: Eclipse JDT Test Harness\n" +
				"Class-Path: s/lib6.jar\n",
			},
			LIB_DIR + "/lib5.jar", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
			CompilerOptions.getFirstSupportedJavaVersion());
		new File(LIB_DIR + "/s").mkdir(); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT //FLLDCIM: MARK — M31 M3FILE_IO CALL
		Util.createJar(
			new String[] {
				"p/D.java",
				"package p;\n" +
				"public class D {\n" +
				"}",
			},
			new String[] {
				"META-INF/MANIFEST.MF",
				"Manifest-Version: 1.0\n" +
				"Created-By: Eclipse JDT Test Harness\n" +
				"Class-Path: ../lib7.jar\n",
			},
			LIB_DIR + "/s/lib6.jar", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
			CompilerOptions.getFirstSupportedJavaVersion());
		Util.createJar(
			new String[] {
				"p/A.java",
				"package p;\n" +
				"public class A {\n" +
				"}",
			},
			new String[] {
				"META-INF/MANIFEST.MF",
				"Manifest-Version: 1.0\n" +
				"Created-By: Eclipse JDT Test Harness\n" +
				"Class-Path: lib2.jar\n",
			},
			LIB_DIR + "/lib7.jar", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
			CompilerOptions.getFirstSupportedJavaVersion());
		Util.createJar(
			new String[] {
				"p/F.java",
				"package p;\n" +
				"public class F {\n" +
				"}",
			},
			new String[] {
				"META-INF/MANIFEST.MF",
				"Manifest-Version: 1.0\n" + //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
				"Created-By: Eclipse JDT Test Harness\n" +
				"Class-Path: " + LIB_DIR + "/lib3.jar lib1.jar\n",
			},
			LIB_DIR + "/lib8.jar", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
			CompilerOptions.getFirstSupportedJavaVersion());
		Util.createJar(
			new String[] {
				"p/G.java",
				"package p;\n" +
				"public class G {\n" +
				"}",
			},
			new String[] {
				"META-INF/MANIFEST.MF",
				"Manifest-Version: 1.0\n" +
				"Created-By: Eclipse JDT Test Harness\n" +
				"Class-Path: lib1.jar\n" +
				"Class-Path: lib3.jar\n",
			},
			LIB_DIR + "/lib9.jar", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
			CompilerOptions.getFirstSupportedJavaVersion());
		Util.createJar(
			new String[] {
				"p/A.java",
				"package p;\n" +
				"public class A {\n" +
				"}",
			},
			// spoiled jar: MANIFEST.MF is a directory
			new String[] {
				"META-INF/MANIFEST.MF/MANIFEST.MF",
				"Manifest-Version: 1.0\n" +
				"Created-By: Eclipse JDT Test Harness\n" +
				"Class-Path: lib2.jar\n",
			},
			LIB_DIR + "/lib10.jar", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
			CompilerOptions.getFirstSupportedJavaVersion());
		Util.createJar(
			new String[] {
				"p/A.java",
				"package p;\n" +
				"public class A {\n" +
				"}",
			},
			new String[] {
				"META-INF/MANIFEST.MF",
				"Manifest-Version: 1.0\n" +
				"Created-By: Eclipse JDT Test Harness\n" +
				"Class-Path:\n",
			},
			LIB_DIR + "/lib11.jar", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
			CompilerOptions.getFirstSupportedJavaVersion());
		Util.createJar(
			null,
			new String[] {
				"META-INF/MANIFEST.MF",
				"Manifest-Version: 1.0\n" +
				"Created-By: Eclipse JDT Test Harness\n" +
				"Class-Path:lib1.jar\n", // missing space
			},
			LIB_DIR + "/lib12.jar", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
			CompilerOptions.getFirstSupportedJavaVersion());
		Util.createJar(
			null,
			new String[] {
				"META-INF/MANIFEST.MF",
				"Manifest-Version: 1.0\n" +
				"Created-By: Eclipse JDT Test Harness\n" +
				"Class-Path:lib1.jar lib1.jar\n", // missing space
			},
			LIB_DIR + "/lib13.jar", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
			CompilerOptions.getFirstSupportedJavaVersion());
		Util.createJar(
			null,
			new String[] {
				"META-INF/MANIFEST.MF",
				"Manifest-Version: 1.0\n" +
				"Created-By: Eclipse JDT Test Harness\n" +
				" Class-Path: lib1.jar\n", // extra space at line start
			},
			LIB_DIR + "/lib14.jar", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
			CompilerOptions.getFirstSupportedJavaVersion());
		Util.createJar(
			null,
			new String[] {
				"META-INF/MANIFEST.MF",
				"Manifest-Version: 1.0\n" +
				"Created-By: Eclipse JDT Test Harness\n" +
				"Class-Path: lib1.jar", // missing newline at end
			},
			LIB_DIR + "/lib15.jar", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
			CompilerOptions.getFirstSupportedJavaVersion());
		Util.createJar(
			new String[] {
				"p/A.java",
				"package p;\n" +
				"public class A {\n" +
				"}",
			},
			new String[] {
				"META-INF/MANIFEST.MF",
				"Manifest-Version: 1.0\n" +
				"Created-By: Eclipse JDT Test Harness\n" +
				"Class-Path: \n" +
				" lib2.jar\n",
				"p/S1.java",
				"package p;\n" +
				"public class S1 {\n" +
				"}",
			},
			LIB_DIR + "/lib16.jar", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
			CompilerOptions.getFirstSupportedJavaVersion());
		new File(LIB_DIR + "/dir").mkdir(); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT //FLLDCIM: MARK — M31 M3FILE_IO CALL
		Util.createJar(
			new String[] {
				"p/A.java",
				"package p;\n" +
				"public class A {\n" +
				"}",
			},
			new String[] {
				"META-INF/MANIFEST.MF",
				"Manifest-Version: 1.0\n" +
				"Created-By: Eclipse JDT Test Harness\n" +
				"Class-Path: ../lib2.jar\n",
			},
			LIB_DIR + "/dir/lib17.jar", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
			CompilerOptions.getFirstSupportedJavaVersion());
		CASCADED_JARS_CREATED = true;
	}

	protected String getLibraryClassesAsQuotedString() {
		String[] paths = Util.getJavaClassLibs();
		StringBuilder buffer = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buffer.append('"'); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		for (int i = 0, max = paths.length; i < max; i++) { //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
			if (i != 0) {
				buffer.append(File.pathSeparatorChar); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			}
			buffer.append(paths[i]); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		}
		buffer.append('"'); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		return String.valueOf(buffer);
	}

	protected String getExtDirectory() {
		return JRE_HOME_DIR + "/lib/ext"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
	}

	/**
	 * Run a compilation test that is expected to complete successfully and
	 * compare the outputs to expected ones.
	 *
	 * @param testFiles
	 *            the source files, given as a suite of file name, file content;
	 *            file names are relative to the output directory
	 * @param commandLine
	 *            the command line to pass to
	 *            {@link BatchCompiler#compile(String, PrintWriter, PrintWriter, org.eclipse.jdt.core.compiler.CompilationProgress) BatchCompiler#compile}
	 * @param expectedSuccessOutOutputString
	 *            the expected contents of the standard output stream; pass null
	 *            to bypass the comparison
	 * @param expectedSuccessErrOutputString
	 *            the expected contents of the standard error output stream;
	 *            pass null to bypass the comparison
	 * @param shouldFlushOutputDirectory
	 *            pass true to get the output directory flushed before the test
	 *            runs
	 */
	protected void runConformTest(String[] testFiles, String commandLine, String expectedSuccessOutOutputString, String expectedSuccessErrOutputString, boolean shouldFlushOutputDirectory) {
		runTest(true, testFiles, commandLine, expectedSuccessOutOutputString,
				expectedSuccessErrOutputString, shouldFlushOutputDirectory, null/*progress*/);
	}

	/**
	 * Run a compilation test that is expected to fail and compare the outputs
	 * to expected ones.
	 *
	 * @param testFiles
	 *            the source files, given as a suite of file name, file content;
	 *            file names are relative to the output directory
	 * @param commandLine
	 *            the command line to pass to
	 *            {@link BatchCompiler#compile(String, PrintWriter, PrintWriter, org.eclipse.jdt.core.compiler.CompilationProgress) BatchCompiler#compile}
	 * @param expectedFailureOutOutputString
	 *            the expected contents of the standard output stream; pass null
	 *            to bypass the comparison
	 * @param expectedFailureErrOutputString
	 *            the expected contents of the standard error output stream;
	 *            pass null to bypass the comparison
	 * @param shouldFlushOutputDirectory
	 *            pass true to get the output directory flushed before the test
	 *            runs
	 */
	protected void runNegativeTest(String[] testFiles, String commandLine, String expectedFailureOutOutputString, String expectedFailureErrOutputString, boolean shouldFlushOutputDirectory) {
		runTest(false, testFiles, commandLine, expectedFailureOutOutputString,
				expectedFailureErrOutputString, shouldFlushOutputDirectory, null/*progress*/);
	}

	protected void runProgressTest(String[] testFiles, String commandLine, String expectedOutOutputString, String expectedErrOutputString, String expectedProgress) {
		runTest(true/*shouldCompileOK*/, testFiles, commandLine, expectedOutOutputString, expectedErrOutputString, true/*shouldFlushOutputDirectory*/, new TestCompilationProgress());
	}

	protected void runProgressTest(boolean shouldCompileOK, String[] testFiles, String commandLine, String expectedOutOutputString, String expectedErrOutputString, TestCompilationProgress progress, String expectedProgress) {
		runTest(shouldCompileOK, testFiles, commandLine, expectedOutOutputString, expectedErrOutputString, true/*shouldFlushOutputDirectory*/, progress);
		String actualProgress = progress.toString();
		if (!semiNormalizedComparison(expectedProgress, actualProgress, outputDirNormalizer)) {
			System.out.println(Util.displayString(outputDirNormalizer.normalized(actualProgress), 2));
			assertEquals(
				"Unexpected progress",
				expectedProgress,
				actualProgress);
		}
	}

	/**
	 * Worker method for runConformTest and runNegativeTest.
	 *
	 * @param shouldCompileOK
	 *            set to true if the compiler should compile the given sources
	 *            without errors
	 * @param testFiles
	 *            the source files, given as a suite of file name, file content;
	 *            file names are relative to the output directory
	 * @param extraArguments
	 *            the command line to pass to {@link Main#compile(String[])
	 *            Main#compile} or other arguments to pass to {@link
	 *            #invokeCompiler(PrintWriter, PrintWriter, Object,
	 *            BatchCompilerTest.TestCompilationProgress)} (for use
	 *            by extending test classes)
	 * @param expectedOutOutputString
	 *            the expected contents of the standard output stream; pass null
	 *            to bypass the comparison
	 * @param expectedErrOutputString
	 *            the expected contents of the standard error output stream;
	 *            pass null to bypass the comparison
	 * @param shouldFlushOutputDirectory
	 *            pass true to get the output directory flushed before the test
	 *            runs
	 */
	protected void runTest(boolean shouldCompileOK, String[] testFiles, Object extraArguments, String expectedOutOutputString, String expectedErrOutputString, boolean shouldFlushOutputDirectory, TestCompilationProgress progress) { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		File outputDirectory = new File(OUTPUT_DIR);
		if (shouldFlushOutputDirectory)
			Util.flushDirectoryContent(outputDirectory);
		try {
			if (!outputDirectory.isDirectory()) { //FLLDCIM: MARK — M31 M3FILE_IO CALL
				outputDirectory.mkdirs(); //FLLDCIM: MARK — M31 M3FILE_IO CALL
			}
			if (testFiles != null) {
				PrintWriter sourceFileWriter;
				for (int i = 0; i < testFiles.length; i += 2) { //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
					String fileName = OUTPUT_DIR + File.separator + testFiles[i]; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
					File file = new File(fileName), innerOutputDirectory = file
							.getParentFile();
					if (!innerOutputDirectory.isDirectory()) { //FLLDCIM: MARK — M31 M3FILE_IO CALL
						innerOutputDirectory.mkdirs(); //FLLDCIM: MARK — M31 M3FILE_IO CALL
					}
					sourceFileWriter = new PrintWriter(new FileOutputStream(file)); //FLLDCIM: MARK — M31 M3FILE_IO NEW
					try {
						sourceFileWriter.write(testFiles[i + 1]);
					} finally {
						sourceFileWriter.close();
					}
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		String printerWritersNameRoot = OUTPUT_DIR + File.separator + testName(); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		String outFileName = printerWritersNameRoot + "out.txt", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
			   errFileName = printerWritersNameRoot + "err.txt"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		PrintWriter out = null;
		PrintWriter err = null;
		boolean compileOK;
		try {
			try {
				out = new PrintWriter(new FileOutputStream(outFileName)); //FLLDCIM: MARK — M31 M3FILE_IO NEW
				err = new PrintWriter(new FileOutputStream(errFileName)); //FLLDCIM: MARK — M31 M3FILE_IO NEW
			} catch (FileNotFoundException e) {
				System.out.println(getClass().getName() + '#' + getName());
				e.printStackTrace();
				throw new RuntimeException(e);
			}
			compileOK = invokeCompiler(out, err, extraArguments, progress);
		} finally {
			if (out != null)
				out.close();
			if (err != null)
				err.close();
		}
		String outOutputString = Util.fileContent(outFileName),
		       errOutputString = Util.fileContent(errFileName);
		boolean compareOK = false, outCompareOK = false, errCompareOK = false;
		if (compileOK == shouldCompileOK) {
			compareOK =
				(outCompareOK = semiNormalizedComparison(expectedOutOutputString,
					outOutputString, outputDirNormalizer))
				&& (errCompareOK = semiNormalizedComparison(expectedErrOutputString,
						errOutputString, outputDirNormalizer));
		}
		// test sanity of the test definition: did we forget to use "---OUTPUT_DIR_PLACEHOLDER---" instead of OUTPUT_DIR?
		if (!outCompareOK) {
			assertEquals("outputDirNormalizer should not affect expectedOutOutput", expectedOutOutputString, outputDirNormalizer.normalized(expectedOutOutputString));
		}
		if (!errCompareOK) {
			assertEquals("outputDirNormalizer should not affect expectedErrOutput", expectedErrOutputString, outputDirNormalizer.normalized(expectedErrOutputString));
		}
		if (compileOK != shouldCompileOK || !compareOK) {
			System.out.println(getClass().getName() + '#' + getName());
			if (testFiles != null) {
				for (int i = 0; i < testFiles.length; i += 2) { //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
					System.out.print(testFiles[i]);
					System.out.println(" [");
					System.out.println(testFiles[i + 1]);
					System.out.println("]");
				}
			}
		}
		if (compileOK != shouldCompileOK)
			System.out.println(errOutputString);
		if (compileOK == shouldCompileOK && !compareOK) {
			System.out.println(
					    "------------ [START OUT] ------------\n" //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
					+   "------------- Expected: -------------\n"
					+ expectedOutOutputString
					+ "\n------------- but was:  -------------\n"
					+ outOutputString
					+ "\n--------- (cut and paste:) ----------\n"
					+ outputDirNormalizer
							.normalized(outOutputString)
					+ "\n------------- [END OUT] -------------\n"
					+   "------------ [START ERR] ------------\n"
					+   "------------- Expected: -------------\n"
					+ expectedErrOutputString
					+ "\n------------- but was:  -------------\n"
					+ errOutputString
					+ "\n--------- (cut and paste:) ----------\n"
					+ outputDirNormalizer
							.normalized(errOutputString)
					+ "\n------------- [END ERR] -------------\n");
		}
		if (shouldCompileOK)
			assertTrue("Unexpected problems [out: " + outOutputString + "][err: " + errOutputString + "]", compileOK); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		else
			assertFalse("Unexpected success: [out: " + outOutputString + "][err: " + errOutputString + "]", compileOK); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		if (!outCompareOK) {
			// calling assertEquals to benefit from the comparison UI
			// (need appropriate exception)
			assertEquals(
					"Unexpected standard output for invocation with arguments [" //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
						+ extraArguments + "]",
					expectedOutOutputString,
					outOutputString);
		}
		if (!errCompareOK) {
			assertEquals(
					"Unexpected error output for invocation with arguments [" //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
						+ extraArguments + "]",
					expectedErrOutputString,
					errOutputString);
		}
	}

	protected boolean invokeCompiler(PrintWriter out, PrintWriter err, Object extraArguments, TestCompilationProgress compilationProgress) {
		try {
			final String[] tokenizedCommandLine = Main.tokenize((String) extraArguments);
			return new Main(out, err, false, null /* customDefaultOptions */, compilationProgress /* compilationProgress*/).compile(tokenizedCommandLine);
		} catch (RuntimeException e) {
			System.out.println(getClass().getName() + '#' + getName());
			e.printStackTrace();
			throw e;
		}
	}

	protected void runTest(boolean shouldCompileOK, String[] testFiles, String commandLine, Matcher outOutputStringMatcher, Matcher errOutputStringMatcher, boolean shouldFlushOutputDirectory) { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		File outputDirectory = new File(OUTPUT_DIR);
		if (shouldFlushOutputDirectory)
			Util.flushDirectoryContent(outputDirectory);
		try {
			if (!outputDirectory.isDirectory()) { //FLLDCIM: MARK — M31 M3FILE_IO CALL
				outputDirectory.mkdirs(); //FLLDCIM: MARK — M31 M3FILE_IO CALL
			}
			PrintWriter sourceFileWriter;
			for (int i = 0; i < testFiles.length; i += 2) { //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
				String fileName = OUTPUT_DIR + File.separator + testFiles[i]; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
				File file = new File(fileName), innerOutputDirectory = file
						.getParentFile();
				if (!innerOutputDirectory.isDirectory()) { //FLLDCIM: MARK — M31 M3FILE_IO CALL
					innerOutputDirectory.mkdirs(); //FLLDCIM: MARK — M31 M3FILE_IO CALL
				}
				sourceFileWriter = new PrintWriter(new FileOutputStream(file)); //FLLDCIM: MARK — M31 M3FILE_IO NEW
				try {
					sourceFileWriter.write(testFiles[i + 1]);
				} finally {
					sourceFileWriter.close();
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		String printerWritersNameRoot = OUTPUT_DIR + File.separator + testName(); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		String outFileName = printerWritersNameRoot + "out.txt", //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
			   errFileName = printerWritersNameRoot + "err.txt"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		Main batchCompiler;
		PrintWriter out = null;
		PrintWriter err = null;
		boolean compileOK;
		try {
			try {
				out = new PrintWriter(new FileOutputStream(outFileName)); //FLLDCIM: MARK — M31 M3FILE_IO NEW
				err = new PrintWriter(new FileOutputStream(errFileName)); //FLLDCIM: MARK — M31 M3FILE_IO NEW
				batchCompiler = new Main(out, err, false/*systemExit*/, null/*options*/, null/*progress*/);
			} catch (FileNotFoundException e) {
				System.out.println(getClass().getName() + '#' + getName());
				e.printStackTrace();
				throw new RuntimeException(e);
			}
			try {
				final String[] tokenizeCommandLine = Main.tokenize(commandLine);
				compileOK = batchCompiler.compile(tokenizeCommandLine);
			} catch (RuntimeException e) {
				compileOK = false;
				System.out.println(getClass().getName() + '#' + getName());
				e.printStackTrace();
				throw e;
			}
		} finally {
			if (out != null)
				out.close();
			if (err != null)
				err.close();
		}
		String outOutputString = Util.fileContent(outFileName),
		       errOutputString = Util.fileContent(errFileName);
		boolean compareOK = false, outCompareOK = false, errCompareOK = false;
		String expectedErrOutputString = null, expectedOutOutputString = null;
		if (compileOK == shouldCompileOK) {
			if (outOutputStringMatcher == null) {
				outCompareOK = true;
			} else {
				outCompareOK = outOutputStringMatcher.match(outOutputString);
				expectedOutOutputString = outOutputStringMatcher.expected();
			}
			if (errOutputStringMatcher == null) {
				errCompareOK = true;
			} else {
				errCompareOK = errOutputStringMatcher.match(errOutputString);
				expectedErrOutputString = errOutputStringMatcher.expected();
			}
			compareOK = outCompareOK && errCompareOK;
		}
		if (compileOK != shouldCompileOK || !compareOK) {
			System.out.println(getClass().getName() + '#' + getName());
			for (int i = 0; i < testFiles.length; i += 2) { //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
				System.out.print(testFiles[i]);
				System.out.println(" [");
				System.out.println(testFiles[i + 1]);
				System.out.println("]");
			}
		}
		if (compileOK != shouldCompileOK)
			System.out.println(errOutputString);
		if (compileOK == shouldCompileOK && !compareOK) {
			System.out.println(
					    "------------ [START OUT] ------------\n" //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
					+   "------------- Expected: -------------\n"
					+ expectedOutOutputString
					+ "\n------------- but was:  -------------\n"
					+ outOutputString
					+ "\n--------- (cut and paste:) ----------\n"
					+ Util.displayString(outputDirNormalizer
							.normalized(outOutputString))
					+ "\n------------- [END OUT] -------------\n"
					+   "------------ [START ERR] ------------\n"
					+   "------------- Expected: -------------\n"
					+ expectedErrOutputString
					+ "\n------------- but was:  -------------\n"
					+ errOutputString
					+ "\n--------- (cut and paste:) ----------\n"
					+ Util.displayString(outputDirNormalizer
							.normalized(errOutputString))
					+ "\n------------- [END ERR] -------------\n");
		}
		if (shouldCompileOK)
			assertTrue("Unexpected problems: " + errOutputString, compileOK); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		else
			assertTrue("Unexpected success: " + errOutputString, !compileOK); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		if (!outCompareOK) {
			// calling assertEquals to benefit from the comparison UI
			// (need appropriate exception)
			assertEquals(
					"Unexpected standard output for invocation with arguments [" //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
						+ commandLine + "]",
					expectedOutOutputString,
					outOutputString);
		}
		if (!errCompareOK) {
			assertEquals(
					"Unexpected error output for invocation with arguments [" //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
						+ commandLine + "]",
					expectedErrOutputString,
					errOutputString);
		}
	}

	protected void runClasspathTest(String classpathInput, String[] expectedClasspathEntries, String expectedError) { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		File outputDirectory = new File(OUTPUT_DIR);
		if (!outputDirectory.isDirectory()) { //FLLDCIM: MARK — M31 M3FILE_IO CALL
			outputDirectory.mkdirs(); //FLLDCIM: MARK — M31 M3FILE_IO CALL
		}
		ArrayList<FileSystem.Classpath> paths = new ArrayList<>(Main.DEFAULT_SIZE_CLASSPATH); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW
		try {
			(new Main(new PrintWriter(System.out), new PrintWriter(System.err), true/*systemExit*/, null/*options*/, null/*progress*/)).
				processPathEntries(Main.DEFAULT_SIZE_CLASSPATH, paths, classpathInput, null /* customEncoding */, true /* isSourceOnly */, false /* rejectDestinationPathOnJars*/);
		} catch (IllegalArgumentException e) {
			// e.printStackTrace();
			if (expectedError == null) {
				fail("unexpected invalid input exception: " + e.getMessage()); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
			} else if (! expectedError.equals(e.getMessage())) {
				System.out.println("\"" + e.getMessage() + "\""); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
				assertEquals(expectedError, e.getMessage());
			}
			return;
		}
		if (expectedError == null) {
			int l = paths.size();
			assertEquals("unexpected classpaths entries number: ",
					expectedClasspathEntries == null ? 0 : expectedClasspathEntries.length / 3, l);
			for (int i = 0, j = 0; i < l ; i++) { //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
				ClasspathLocation result = (ClasspathLocation) paths.get(i);
				String expected = expectedClasspathEntries[j++];
				String actual = result.toString();
				if (! actual.equals("ClasspathDirectory " + expected + File.separator) && //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
						! actual.equals("Classpath for jar file " + expected)) { //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
					assertEquals("dir/jar " + expected, actual); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
				}
				expected = expectedClasspathEntries[j++];
				if (result.accessRuleSet == null) {
					assertNull("actual access rule is null instead of <" + expected +">", expected); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
				} else if (! result.accessRuleSet.toString(false).
						startsWith("AccessRuleSet " + expected)) { //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
					System.out.println("\"" + result.accessRuleSet.toString(false) + "\""); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
					fail("inappropriate rules (expected " + expected + //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
						", got " + result.accessRuleSet.toString(false));
				}
				expected = expectedClasspathEntries[j++];
				if (expected == null) {
					assertNull(result.destinationPath);
				} else if (expected == Main.NONE &&
						result.destinationPath != Main.NONE) {
					fail("expected 'none' output directory");
				} else if (! expected.equals(result.destinationPath)) {
					System.out.println("\"" + result.destinationPath + "\""); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
					assertEquals(expected, result.destinationPath);
				}
			}
		} else {
			fail("missing error: " + expectedError); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		}
	}

	/**
	 * Check that no line of message extends beyond width columns. Tabs count for
	 * 4 characters.
	 * @param message the message to check
	 * @param width the maximum number of columns for the message
	 */
	protected void checkWidth(String message, int width) {
		BufferedReader reader = new BufferedReader(
				new StringReader(message.replaceAll("\t", "    "))); //FLLDCIM: MARK — M12 REGEX_PRECOMPUTE CALL
		String line;
		try {
			while ((line = reader.readLine()) != null) { //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
				assertTrue("line exceeds " + width + "characters: " + line, //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
					line.length() <= width);
			}
		} catch (IOException e) {
			// should never happen on a StringReader
		}
	}

	private static boolean equals(String a, String b) { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		StringBuilder aBuffer = new StringBuilder(a), bBuffer = new StringBuilder(b); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		int length = aBuffer.length(), bLength;
		boolean result = true;
		if (length != (bLength = bBuffer.length())) {
			System.err.println("a and b lengths differ");
			if (length > bLength) {
				length = bLength;
			}
			result = false;
		}
		for (int i = 0; i < length; i++) //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
			if (aBuffer.charAt(i) != bBuffer.charAt(i)) {
				int beforeStart = i - 5, beforeEnd = i - 1, afterStart = i + 1, afterEnd = i + 5;
				if (beforeStart < 0) {
					beforeStart = 0;
					if (beforeEnd < 0)
						beforeEnd = 0;
				}
				if (afterEnd >= length) {
					afterEnd = length - 1;
					if (afterStart >= length)
						afterStart = length - 1;
				}
				System.err.println("a and b differ at rank: " + i //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
						+ "\na: ..." + aBuffer.substring(beforeStart, beforeEnd)
							+ "<" + aBuffer.charAt(i) + ">"
							+ aBuffer.substring(afterStart, afterEnd) + "..."
						+ "\nb: ..." + bBuffer.substring(beforeStart, beforeEnd)
							+ "<" + bBuffer.charAt(i) + ">"
							+ bBuffer.substring(afterStart, afterEnd) + "...");
				return false;
			}
		return result; // may be false if one of the strings equals the beginning
		               // of the other one, which is longer anyway
	}

	/**
	 * Return true if and only if the two strings passed as parameters compare
	 * equal, modulo the transformation of the second string by a normalizer
	 * passed in parameter. This is meant to erase the variations of subparts of
	 * the compared strings in function of the test machine, the user account,
	 * etc.
	 *
	 * @param keep
	 *            the first string to compare, gets compared as it is
	 * @param normalize
	 *            the second string to compare, passed through the normalizer
	 *            before comparison
	 * @param normalizer
	 *            the transformation applied to normalize
	 * @return true if keep and normalize compare equal after normalize has been
	 *         normalized
	 */
	protected boolean semiNormalizedComparison(String keep, String normalize, Normalizer normalizer) {
		if (keep == null)
			return normalize == null;
		if (normalize == null)
			return false;
		// return keep.equals(normalizer.normalized(normalize));
		return equals(keep, normalizer.normalized(normalize));
	}

}
