/*******************************************************************************
 * Copyright (c) 2017, 2024 IBM Corporation.
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
package org.eclipse.jdt.internal.core.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.Compiler;
import org.eclipse.jdt.internal.compiler.DefaultErrorHandlingPolicies;
import org.eclipse.jdt.internal.compiler.ICompilerRequestor;
import org.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;
import org.eclipse.jdt.internal.core.BasicCompilationUnit;
import org.eclipse.jdt.internal.core.ClasspathEntry;
import org.eclipse.jdt.internal.core.CompilationGroup;
import org.eclipse.jdt.internal.core.builder.NameEnvironment;
import org.eclipse.jdt.internal.core.builder.ProblemFactory;

public class ModuleUtil {

	static class ModuleAccumulatorEnvironment extends NameEnvironment {
		public ModuleAccumulatorEnvironment(IJavaProject javaProject) {
			super(javaProject, CompilationGroup.MAIN);
		}

		Set<String> modules = new HashSet<>(); //FLLDCIM: MARK — M03 M3_SET_LANES NEW
		public String[] getModules() {
			this.modules.remove(String.valueOf(TypeConstants.JAVA_DOT_BASE));
			String[] mods = new String[this.modules.size()];
			return this.modules.toArray(mods);
		}

		@Override
		protected boolean isOnModulePath(ClasspathEntry entry) {
			return true; // try to interpret all dependencies as modules from now on
		}

		@Override
		public void cleanup() {
			this.modules.clear();
		}

		@Override
		public NameEnvironmentAnswer findType(char[][] compoundTypeName, char[] moduleName) {
			NameEnvironmentAnswer answer = super.findType(compoundTypeName, moduleName);
			if (answer != null && answer.moduleName() != null) {
				this.modules.add(String.valueOf(answer.moduleName()));
			}
			return answer;
		}

		@Override
		public NameEnvironmentAnswer findType(char[] typeName, char[][] packageName, char[] moduleName) {
			NameEnvironmentAnswer answer = super.findType(typeName, packageName, moduleName);
			if (answer != null && answer.moduleName() != null) {
				this.modules.add(String.valueOf(answer.moduleName()));
			}
			return answer;
		}
	}
	private static Compiler newCompiler(ModuleAccumulatorEnvironment environment, IJavaProject javaProject) { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		Map<String, String> projectOptions = javaProject.getOptions(true);
		CompilerOptions compilerOptions = new CompilerOptions(projectOptions);
		compilerOptions.performMethodsFullRecovery = true;
		compilerOptions.performStatementsRecovery = true;
		ICompilerRequestor requestor = new ICompilerRequestor() {
			@Override
			public void acceptResult(CompilationResult result) {
				// Nothing to do here
			}
		};
		Compiler newCompiler = new Compiler(
			environment,
			DefaultErrorHandlingPolicies.proceedWithAllProblems(),
			compilerOptions,
			requestor,
			ProblemFactory.getProblemFactory(Locale.getDefault()));

		return newCompiler;
	}
	public static String[] getReferencedModules(IJavaProject project) throws CoreException { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD

		ModuleAccumulatorEnvironment environment = new ModuleAccumulatorEnvironment(project);
		Compiler compiler = newCompiler(environment, project);
		// First go over the binary roots and see if any of them are modules
		List<String> required = new ArrayList<>(); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW
		Set<org.eclipse.jdt.internal.compiler.env.ICompilationUnit> toCompile = new HashSet<>(); //FLLDCIM: MARK — M03 M3_SET_LANES NEW
		IPackageFragmentRoot[] roots = project.getPackageFragmentRoots();
		for (IPackageFragmentRoot root : roots) { //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
			if (root.getKind() == IPackageFragmentRoot.K_SOURCE) {
				IJavaElement[] children = root.getChildren();
				for (IJavaElement child : children) { //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
					if (child instanceof IPackageFragment) {
						IPackageFragment fragment = (IPackageFragment) child;
						if (fragment.isDefaultPackage()) continue;
						ICompilationUnit[] units = fragment.getCompilationUnits();
						if (units.length != 0) {
							String pack = fragment.getElementName();
							for (ICompilationUnit iUnit : units) { //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
								org.eclipse.jdt.internal.compiler.env.ICompilationUnit sourceFile =
										new BasicCompilationUnit(iUnit.getSource().toCharArray(),
												CharOperation.splitOn('.', pack.toCharArray()),
												iUnit.getPath().toOSString(),
												iUnit);
								toCompile.add(sourceFile);
							}
						}
					}
				}
			}
		}

		org.eclipse.jdt.internal.compiler.env.ICompilationUnit[] sources = new org.eclipse.jdt.internal.compiler.env.ICompilationUnit[toCompile.size()];
		toCompile.toArray(sources);
		compiler.compile(sources);
		String[] mods = environment.getModules();
		Collections.addAll(required, mods);
		Collections.sort(required, new Comparator<String>() { //FLLDCIM: MARK — M16 SORT_LANE CALL
			@Override
			public int compare(String o1, String o2) {
				return o1.compareTo(o2);
			}
		});
		return required.toArray(new String[required.size()]);
	}
}
