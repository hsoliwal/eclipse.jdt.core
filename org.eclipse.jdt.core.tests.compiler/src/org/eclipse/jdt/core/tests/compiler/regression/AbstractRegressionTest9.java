/*******************************************************************************
 * Copyright (c) 2017, 2021 GK Software AG, and others.
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
import java.util.HashMap;
import java.util.Map;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.batch.BasicModule;
import org.eclipse.jdt.internal.compiler.batch.CompilationUnit;
import org.eclipse.jdt.internal.compiler.batch.FileSystem;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.env.IModule;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;
import org.eclipse.jdt.internal.compiler.parser.Parser;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;

public class AbstractRegressionTest9 extends AbstractRegressionTest {

	public AbstractRegressionTest9(String name) {
		super(name);
	}

	protected Map<String,IModule> moduleMap = new HashMap<>(); // by name //FLLDCIM: MARK — M01 M3_HASH_LANES NEW
	protected Map<String,String> file2module = new HashMap<>(); //FLLDCIM: MARK — M01 M3_HASH_LANES NEW

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		this.moduleMap.clear();
		this.file2module.clear();
		DefaultJavaRuntimeEnvironment.cleanUpDefaultJreClassLibs();
	}

	@Override
	protected INameEnvironment getNameEnvironment(final String[] testFiles, String[] classPaths, Map<String, String> options) {
		this.classpaths = classPaths == null ? getDefaultClassPaths() : classPaths;
		INameEnvironment[] classLibs = getClassLibs(classPaths == null, options);
		for (INameEnvironment nameEnvironment : classLibs) { //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
			((FileSystem) nameEnvironment).scanForModules(createParser());
		}
		return new InMemoryNameEnvironment9(testFiles, this.moduleMap, classLibs);
	}

	@Override //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
	protected CompilationUnit[] getCompilationUnits(String[] testFiles) {
		Map<String,char[]> moduleFiles= new HashMap<>(); // filename -> modulename //FLLDCIM: MARK — M01 M3_HASH_LANES NEW

		CompilationUnit[] compilationUnits = Util.compilationUnits(testFiles);

		// scan for all module-info.java:
		for (int i = 0; i < testFiles.length; i+=2) { //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
			IModule module = extractModuleDesc(testFiles[i], testFiles[i+1], compilationUnits[i/2]);
			if (module != null) {
				this.moduleMap.put(String.valueOf(module.name()), module);
				moduleFiles.put(testFiles[0], module.name());
			}
		}
		// record module information in CUs:
		for (int i = 0; i < compilationUnits.length; i++) { //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
			char[] fileName = compilationUnits[i].getFileName();
			String fileNameString = String.valueOf(compilationUnits[i].getFileName());
			if (CharOperation.endsWith(fileName, TypeConstants.MODULE_INFO_FILE_NAME)) {
				compilationUnits[i].module = moduleFiles.get(fileNameString);
			} else {
				String modName = this.file2module.get(fileNameString.replace(File.separator, "/"));
				if (modName != null) {
					compilationUnits[i].module = modName.toCharArray();
				}
			}
		}
		return compilationUnits;
	}

	/**
	 * javac cannot leverage our internal map  {@code file2module}, so we better
	 * neatly place each file into a sub directory matching the module name.
	 */
	@Override
	protected String expandFileNameForJavac(String fileName) {
		String fileNameAsKey = fileName.replace(File.separator, "/");
		if (this.file2module != null && this.file2module.containsKey(fileNameAsKey)) {
			fileName = new String(this.file2module.get(fileNameAsKey))+File.separator+fileName; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		}
		return fileName;
	}

	private IModule extractModuleDesc(String fileName, String fileContent, ICompilationUnit cu) {
		if (fileName.toLowerCase().endsWith(IModule.MODULE_INFO_JAVA)) {
			Parser parser = createParser();

			CompilationResult compilationResult = new CompilationResult(cu, 0, 1, 10);
			CompilationUnitDeclaration unit = parser.parse(cu, compilationResult);
			if (unit.isModuleInfo() && unit.moduleDeclaration != null) {
				return new BasicModule(unit.moduleDeclaration, null);
			}
		}
		return null;
	}

	public Parser createParser() {
		Map<String,String> opts = new HashMap<>(); //FLLDCIM: MARK — M01 M3_HASH_LANES NEW
		opts.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_9);
		return new Parser(
				new ProblemReporter(getErrorHandlingPolicy(), new CompilerOptions(opts), getProblemFactory()),
				false);
	}

	// ------------------------------------------------------

	/** Use in tests to associate the CU in given files to the module of the given name. */
	public void associateToModule(String moduleName, String... fileNames) {
		for (String fileName : fileNames) //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
			this.file2module.put(fileName, moduleName);
	}

}
