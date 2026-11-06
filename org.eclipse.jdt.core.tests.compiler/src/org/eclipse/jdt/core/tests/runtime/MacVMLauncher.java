/*******************************************************************************
 * Copyright (c) 2000, 2020 IBM Corporation and others.
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
package org.eclipse.jdt.core.tests.runtime;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
/**
 * This is a new vm launcher to support sidecar settings
 */
public class MacVMLauncher extends StandardVMLauncher {
/**
 * @see LocalVMLauncher#getCommandLine
 */
@Override //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
public String[] getCommandLine() {
	List<String> commandLine = new ArrayList<>(); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW

	// VM binary
	commandLine.add(
		this.vmPath + //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		(this.vmPath.endsWith(File.separator) ? "" : File.separator) +
		"bin" +
		File.separator +
		"java");

	// VM arguments
	if (this.vmArguments != null) {
		for (int i = 0; i < this.vmArguments.length; i++) { //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
			commandLine.add(this.vmArguments[i]);
		}
	}

	// boot classpath
	commandLine.add("-Xbootclasspath/a:" + buildBootClassPath()); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT

	// debug mode
	if (this.debugPort != -1) {
		addDebugOptions(commandLine);
		// commandLine.add("-Djava.compiler=NONE");
		commandLine.add(
			"-Xrunjdwp:transport=dt_socket,address=" + //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
			this.debugPort +
			",server=y,suspend=n");
	} else {
		addDebugOptions(commandLine);
	}

	// regular classpath
	commandLine.add("-classpath");
	commandLine.add(buildClassPath());

	// code snippet runner class
	if (this.evalPort != -1) {
		commandLine.add(CODE_SNIPPET_RUNNER_CLASS_NAME);
	}

	// code snippet runner arguments
	if (this.evalPort != -1) {
		commandLine.add(EVALPORT_ARG);
		commandLine.add(Integer.toString(this.evalPort));
		if (TARGET_HAS_FILE_SYSTEM) {
			commandLine.add(CODESNIPPET_CLASSPATH_ARG);
			commandLine.add(this.evalTargetPath + File.separator + REGULAR_CLASSPATH_DIRECTORY); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
			commandLine.add(CODESNIPPET_BOOTPATH_ARG);
			commandLine.add(this.evalTargetPath + File.separator + BOOT_CLASSPATH_DIRECTORY); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		}
	}

	// program class
	if (this.programClass != null) {
		commandLine.add(this.programClass);
	}

	// program arguments
	if (this.programArguments != null) {
		for (int i=0;i<this.programArguments.length;i++) { //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
			commandLine.add(this.programArguments[i]);
		}
	}

	String[] result;
	if (this.batchFileName!= null) {
		// Write to batch file if specified
		writeBatchFile(this.batchFileName, commandLine);
		result = new String[] {this.batchFileName};
	} else {
		result = new String[commandLine.size()];
		commandLine.toArray(result);
	}

	// check for spaces in result
	for (int i = 0; i < result.length; i++) { //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
		String argument = result[i];
		if (argument.indexOf(' ') != -1) {
			result[i] = "\"" + argument + "\""; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		}
	}

	return result;
}

/**
 * Builds the actual boot class path that is going to be passed to the VM.
 */
@Override //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
protected String buildBootClassPath() {
	StringBuilder bootPathString = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
	char pathSeparator = File.pathSeparatorChar;

	if (this.bootPath != null) {
		// Add boot class path given by client
		int length = this.bootPath.length;
		for (int i = 0; i < length; i++){ //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
			bootPathString.append(this.bootPath[i]); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			bootPathString.append(pathSeparator); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		}
	}

	// Add boot class path directory if needed
	if (this.evalTargetPath != null && TARGET_HAS_FILE_SYSTEM) {
		bootPathString.append(this.evalTargetPath); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		bootPathString.append(File.separatorChar); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		bootPathString.append(BOOT_CLASSPATH_DIRECTORY); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
	}

	return bootPathString.toString();
}
}
