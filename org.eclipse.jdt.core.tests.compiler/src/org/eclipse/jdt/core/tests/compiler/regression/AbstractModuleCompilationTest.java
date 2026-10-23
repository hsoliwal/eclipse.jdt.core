/*******************************************************************************
 * Copyright (c) 2025 GK Software SE, and others.
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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import junit.framework.AssertionFailedError;
import org.eclipse.jdt.core.util.ClassFormatException;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;

public abstract class AbstractModuleCompilationTest extends AbstractBatchCompilerTest {

	// use -source rather than --release but suppress: warning: [options] bootstrap class path not set in conjunction with -source 9:
	protected static final String JAVAC_SOURCE_9_OPTIONS = "-source 9 -Xlint:-options";
	protected static Pattern ECJ_VERSION_OPTION_PATTERN = Pattern.compile("-([0-9]+)"); //FLLDCIM: MARK — M12 REGEX_PRECOMPUTE CALL

	public AbstractModuleCompilationTest(String name) {
		super(name);
	}

	class Runner extends AbstractRegressionTest.Runner {
		StringBuilder commandLine = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		String outputDir = OUTPUT_DIR;
		List<String> fileNames = new ArrayList<>(); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW
		/** will replace any -8, -9 ... option for javac */
		String javacVersionOptions;

		Runner() {
			this.javacTestOptions = JavacTestOptions.DEFAULT;
			this.expectedOutputString = "";
			this.expectedErrorString = "";
		}
		/** Create a source file and add the filename to the compiler command line. */
		void createFile(String directoryName, String fileName, String source) {
			writeFileCollecting(this.fileNames, directoryName, fileName, source);
		}
		Set<String> runConformModuleTest() {
			if (!this.fileNames.isEmpty()) {
				this.shouldFlushOutputDirectory = false;
				if (this.testFiles == null)
					this.testFiles = new String[0];
				for (String fileName : this.fileNames) { //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
					this.commandLine.append(" \"").append(fileName).append("\""); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				}
			}
			String commandLineString = this.commandLine.toString();
			String javacCommandLine = adjustForJavac(commandLineString, this.javacVersionOptions);
			return AbstractModuleCompilationTest.this.runConformModuleTest(this.testFiles, commandLineString,
					this.expectedOutputString, this.expectedErrorString,
					this.shouldFlushOutputDirectory, this.javacTestOptions,
					javacCommandLine);
		}
	}

	protected String getSourceDir() {
		return OUTPUT_DIR + File.separatorChar + "src"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
	}

	protected String getEcjOutputDir() {
		return OUTPUT_DIR + File.separatorChar + "bin"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
	}
	protected String getJavacOutputDir() {
		return OUTPUT_DIR + File.separatorChar + "javac"; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
	}

	protected void writeFileCollecting(List<String> collectedFiles, String directoryName, String fileName, String source) {
		writeFile(directoryName, fileName, source);
		collectedFiles.add(directoryName+File.separator+fileName); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
	}

	protected void writeFile(String directoryName, String fileName, String source) { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		File directory = new File(directoryName);
		if (!directory.exists()) { //FLLDCIM: MARK — M31 M3FILE_IO CALL
			if (!directory.mkdirs()) { //FLLDCIM: MARK — M31 M3FILE_IO CALL
				System.out.println("Could not create " + directoryName); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
				return;
			}
		}
		String filePath = directory.getAbsolutePath() + File.separator + fileName; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(filePath)); //FLLDCIM: MARK — M31 M3FILE_IO NEW
			writer.write(source);
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
	}

	protected void runConformModuleTest(List<String> testFileNames, StringBuilder commandLine, String expectedFailureOutOutputString, String expectedFailureErrOutputString) {
		runConformModuleTest(testFileNames, commandLine,
				expectedFailureOutOutputString, expectedFailureErrOutputString, JavacTestOptions.DEFAULT);
	}
	protected void runConformModuleTest(List<String> testFileNames, StringBuilder commandLine,
			String expectedFailureOutOutputString, String expectedFailureErrOutputString,
			JavacTestOptions javacTestOptions) {
		for (String file : testFileNames) //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
			commandLine.append(" \"").append(file).append("\""); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		runConformModuleTest(new String[0], commandLine.toString(),
				expectedFailureOutOutputString, expectedFailureErrOutputString, false,
				javacTestOptions, null);
	}

	protected Set<String> runConformModuleTest(String[] testFiles, String commandLine, String expectedFailureOutOutputString, String expectedFailureErrOutputString, boolean shouldFlushOutputDirectory) {
		return runConformModuleTest(testFiles, commandLine, expectedFailureErrOutputString, expectedFailureErrOutputString,
				shouldFlushOutputDirectory, JavacTestOptions.DEFAULT, null);
	}

	protected Set<String> runConformModuleTest(String[] testFiles, String commandLine, String expectedFailureOutOutputString, String expectedFailureErrOutputString, boolean shouldFlushOutputDirectory, JavacTestOptions options, //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
			String javacCommandLine) {
				String ecjOutput = getEcjOutputDir();
				File ecjOutFile = new File(ecjOutput);
				if (!ecjOutFile.exists()) { //FLLDCIM: MARK — M31 M3FILE_IO CALL
					ecjOutFile.mkdir(); //FLLDCIM: MARK — M31 M3FILE_IO CALL
				}
				String ecjCommandLine = commandLine.contains("-d ")
						? commandLine
						: " -d " + ecjOutput + ' ' + commandLine; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
				runConformTest(testFiles, ecjCommandLine, expectedFailureOutOutputString, expectedFailureErrOutputString, shouldFlushOutputDirectory);
				if (shouldRunJavac()) {
					final Set<String> outFiles = new HashSet<>(); //FLLDCIM: MARK — M03 M3_SET_LANES NEW
					walkOutFiles(ecjOutput, outFiles, true);

					String javacOutput = getJavacOutputDir();
					File javacOutputDir = new File(javacOutput);

					String[] testFileNames = new String[testFiles.length/2];
					for (int i = 0; i < testFileNames.length; i++) { //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
						testFileNames[i] = testFiles[i*2];
					}
					if (javacCommandLine == null) {
						javacCommandLine = " -d " + javacOutput + ' ' + adjustForJavac(commandLine, null); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
					}
					for (JavacCompiler javacCompiler : javacCompilers) { //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
						if (javacCompiler.compliance < ClassFileConstants.JDK9)
							continue;
						if (options.skip(javacCompiler)) {
							System.err.println("Skip testing javac in "+testName()); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
							continue;
						}
						numRunJavacTests++;
						StringBuilder log = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
						try {
							long compileResult = javacCompiler.compile(
													javacOutputDir, /* directory */
													javacCommandLine /* options */,
													testFileNames /* source file names */,
													log,
													false); // don't repeat filenames on the command line
							log = trimJavacLog(log);
							if (compileResult != 0 && !log.isEmpty()) {
								JavacTestOptions.Excuse excuse = options.excuseFor(javacCompiler);
								boolean hasError = log.toString().lines().anyMatch(l -> l.contains("error:"));
								int mismatch = hasError ? JavacTestOptions.MismatchType.JavacErrorsEclipseNone : JavacTestOptions.MismatchType.JavacWarningsEclipseNone;
								handleMismatch(javacCompiler, testName(), testFiles,
										"", expectedFailureOutOutputString, expectedFailureErrOutputString,
										log, "", "",
										excuse, mismatch);
							}
						} catch (IOException | InterruptedException e) {
							e.printStackTrace();
							throw new AssertionFailedError(e.getMessage());
						}
						final Set<String> expectedFiles = outFiles.stream().map(s -> s.replace(ecjOutput, javacOutput)).collect(Collectors.toSet());
						walkOutFiles(javacOutput, expectedFiles, false);
						for (String missingFile : expectedFiles) //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
							System.err.println("Missing output file from javac:    "+missingFile); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
					}
					return outFiles;
				}
				return null;
			}

	/** hook to filter uninteresting errors/warnings */
	protected StringBuilder trimJavacLog(StringBuilder log) {
		return log;
	}

	protected void runNegativeModuleTest(List<String> testFileNames, StringBuilder commandLine, String expectedFailureOutOutputString, String expectedFailureErrOutputString, String javacErrorMatch) {
		runNegativeModuleTest(testFileNames, commandLine, expectedFailureOutOutputString, expectedFailureErrOutputString,
				javacErrorMatch, JavacTestOptions.DEFAULT);
	}

	protected void runNegativeModuleTest(List<String> testFileNames, StringBuilder commandLine, String expectedFailureOutOutputString, String expectedFailureErrOutputString, String javacErrorMatch,
			JavacTestOptions options) {
		for (String file : testFileNames) //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
			commandLine.append(" \"").append(file).append("\""); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		runNegativeModuleTest(new String[0], commandLine.toString(),
				expectedFailureOutOutputString, expectedFailureErrOutputString, false, javacErrorMatch, options);
	}

	protected void runNegativeModuleTest(String[] testFiles, String commandLine, String expectedFailureOutOutputString, String expectedFailureErrOutputString, boolean shouldFlushOutputDirectory,
			String javacErrorMatch) {
		String prefix = "src" + File.separatorChar; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		for (int i = 0; i + 1 < testFiles.length; i+=2) { //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
			testFiles[i] = prefix + testFiles[i]; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		}
		runNegativeModuleTest(testFiles, commandLine, expectedFailureOutOutputString, expectedFailureErrOutputString,
				shouldFlushOutputDirectory, javacErrorMatch, JavacTestOptions.DEFAULT);
	}

	void runNegativeModuleTest(String[] testFiles, String commandLine, String expectedFailureOutOutputString, String expectedFailureErrOutputString, boolean shouldFlushOutputDirectory, String javacErrorMatch, //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
			JavacTestOptions options) {
		String ecjOutput = getEcjOutputDir();
		File ecjOutFile = new File(ecjOutput);
		if (!ecjOutFile.exists()) { //FLLDCIM: MARK — M31 M3FILE_IO CALL
			ecjOutFile.mkdir(); //FLLDCIM: MARK — M31 M3FILE_IO CALL
		}
		String ecjCommandLine = commandLine.contains("-d ")
				? commandLine
				: " -d " + ecjOutput + ' ' + commandLine; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		runNegativeTest(testFiles, ecjCommandLine, expectedFailureOutOutputString, expectedFailureErrOutputString, shouldFlushOutputDirectory);
		if (shouldRunJavac()) {
			final Set<String> outFiles = new HashSet<>(); //FLLDCIM: MARK — M03 M3_SET_LANES NEW
			walkOutFiles(ecjOutput, outFiles, true);

			String javacOutput = getJavacOutputDir();
			File outputDir = new File(javacOutput);

			String[] testFileNames = new String[testFiles.length/2];
			for (int i = 0; i < testFileNames.length; i++) { //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
				testFileNames[i] = testFiles[i*2];
			}

			for (JavacCompiler javacCompiler : javacCompilers) { //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
				if (javacCompiler.compliance < ClassFileConstants.JDK9)
					continue;
				JavacTestOptions.Excuse excuse = options.excuseFor(javacCompiler);
				numRunJavacTests++;

				String javacCommandLine = " -d " + javacOutput + ' ' + adjustForJavac(commandLine, null); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
				StringBuilder log = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
				int mismatch = 0;
				try {
					long compileResult = javacCompiler.compile(
											outputDir, /* directory */
											javacCommandLine /* options */,
											testFileNames /* source file names */,
											log,
											false);
					if (compileResult == 0) {
						mismatch = JavacTestOptions.MismatchType.EclipseErrorsJavacNone;
						javacErrorMatch = expectedFailureErrOutputString;
						System.err.println("Previous error was from "+testName()); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
					} else if (!log.toString().contains(javacErrorMatch)) {
						mismatch = JavacTestOptions.MismatchType.CompileErrorMismatch;
						System.err.println(testName()+": Error match " + javacErrorMatch + " not found in \n"+log.toString()); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
					}
				} catch (IOException | InterruptedException e) {
					e.printStackTrace();
					throw new AssertionFailedError(e.getMessage());
				}
				handleMismatch(javacCompiler, testName(), testFiles, javacErrorMatch,
						"", "", log, "", "",
						excuse, mismatch);
				final Set<String> expectedFiles = outFiles.stream().map(s -> s.replace(ecjOutput, javacOutput)).collect(Collectors.toSet());
				walkOutFiles(javacOutput, expectedFiles, false);
				for (String missingFile : expectedFiles) //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
					System.out.println("Missing output file from javac in negative test:    "+missingFile); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
			}
		}
	}

	/**
	 * @param commandLine command line arguments as used for ecj
	 * @param versionOptions if non-null use this to replace any ecj-specific -8, -9 etc. arg.
	 * 		If ecj-specific arg is not found, append anyway
	 * @return commandLine adjusted for javac
	 */
	protected String adjustForJavac(String commandLine, String versionOptions) { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		String[] tokens = commandLine.split(" "); //FLLDCIM: MARK — M12 REGEX_PRECOMPUTE CALL
		StringBuilder buf = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		boolean skipNext = false;
		for (int i = 0; i < tokens.length; i++) { //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
			if (skipNext) {
				skipNext = false;
				continue;
			}
			java.util.regex.Matcher matcher = ECJ_VERSION_OPTION_PATTERN.matcher(tokens[i].trim()); //FLLDCIM: MARK — M12 REGEX_PRECOMPUTE CALL
			if (matcher.matches()) { //FLLDCIM: MARK — M12 REGEX_PRECOMPUTE CALL
				if (versionOptions == null)
					buf.append(' ').append(" --release ").append(matcher.group(1)).append(' '); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				continue;
			}
			if (tokens[i].startsWith("-warn") || tokens[i].startsWith("-err") || tokens[i].startsWith("-info")) {
				if (tokens[i].contains("exports") && !tokens[i].contains("-exports"))
					buf.append(" -Xlint:exports "); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				continue;
			}
			if (tokens[i].trim().equals("-classNames")) {
				skipNext = true;
				continue;
			}
			buf.append(tokens[i]).append(' '); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		}
		if (versionOptions != null) {
			buf.append(versionOptions); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		}
		return buf.toString();
	}

	private void walkOutFiles(final String outputLocation, final Set<String> fileNames, boolean add) { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		if (!(new File(outputLocation)).exists()) //FLLDCIM: MARK — M31 M3FILE_IO CALL
			return;
		try {
			Files.walkFileTree(Path.of(outputLocation), new SimpleFileVisitor<Path>() { //FLLDCIM: MARK — M24 M3FILE_STREAMS CALL
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					if (file.toString().endsWith(".class")) {
						if (add) {
							fileNames.add(file.toString());
						} else {
							if (!fileNames.remove(file.toString()))
								System.err.println("Unexpected output file from javac: "+file.toString()); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
						}
					}
					return FileVisitResult.CONTINUE;
				}
			});
		} catch (IOException e) {
			e.printStackTrace();
			throw new AssertionFailedError(e.getMessage());
		}
	}

	protected void assertClassFile(String msg, String fileName, Set<String> classFiles) {
		if (classFiles != null) {
			assertTrue(msg, classFiles.contains(fileName));
		} else {
			assertTrue(msg, (new File(fileName).exists())); //FLLDCIM: MARK — M31 M3FILE_IO CALL
		}
	}
	@Override
	protected void verifyClassFile(String expectedOutput, String classFileName, int mode) throws IOException, ClassFormatException {
		verifyClassFile(expectedOutput, classFileName, mode, false);
	}
	protected void verifyClassFile(String expectedOutput, String classFileName, int mode, boolean skipJavac)
			throws IOException, ClassFormatException {
		verifyClassFile(expectedOutput, null, getEcjOutputDir()+File.separatorChar+classFileName, mode); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		if (!skipJavac && shouldRunJavac()) {
			verifyClassFile(expectedOutput, null, getJavacOutputDir()+File.separatorChar+classFileName, mode); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		}
	}
}
