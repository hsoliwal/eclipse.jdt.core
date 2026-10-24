/*******************************************************************************
 * Copyright (c) 2018 IBM Corporation and others.
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
package org.eclipse.jdt.core.tests.model;

import junit.framework.Test;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.ReferenceMatch;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.TypeReferenceMatch;

public class JavaSearchBugs10Tests extends AbstractJavaSearchTests {

	static {
//	 org.eclipse.jdt.internal.core.search.BasicSearchEngine.VERBOSE = true;
//		TESTS_NUMBERS = new int[] { 19 };
//		TESTS_RANGE = new int[] { 1, -1 };
//	TESTS_NAMES = new String[] {"testBug529434_001"};
}

public JavaSearchBugs10Tests(String name) {
	super(name);
	this.endChar = "";
}
public static Test suite() {
	return buildModelTestSuite(JavaSearchBugs10Tests.class, BYTECODE_DECLARATION_ORDER);
}
class TestCollector extends JavaSearchResultCollector {
	@Override
	public void acceptSearchMatch(SearchMatch searchMatch) throws CoreException {
		super.acceptSearchMatch(searchMatch);
	}
}
class ReferenceCollector extends JavaSearchResultCollector {
	@Override
	protected void writeLine() throws CoreException {
		super.writeLine();
		ReferenceMatch refMatch = (ReferenceMatch) this.match;
		IJavaElement localElement = refMatch.getLocalElement();
		if (localElement != null) {
			this.line.append("+["); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			if (localElement.getElementType() == IJavaElement.ANNOTATION) {
				this.line.append('@'); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				this.line.append(localElement.getElementName()); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				this.line.append(" on "); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				this.line.append(localElement.getParent().getElementName()); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			} else {
				this.line.append(localElement.getElementName()); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			}
			this.line.append(']'); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		}
	}

}
class TypeReferenceCollector extends ReferenceCollector {
	@Override //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
	protected void writeLine() throws CoreException {
		super.writeLine();
		TypeReferenceMatch typeRefMatch = (TypeReferenceMatch) this.match;
		IJavaElement[] others = typeRefMatch.getOtherElements();
		int length = others==null ? 0 : others.length;
		if (length > 0) {
			this.line.append("+["); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			for (int i=0; i<length; i++) { //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
				IJavaElement other = others[i];
				if (i>0) this.line.append(','); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				if (other.getElementType() == IJavaElement.ANNOTATION) {
					this.line.append('@'); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
					this.line.append(other.getElementName()); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
					this.line.append(" on "); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
					this.line.append(other.getParent().getElementName()); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				} else {
					this.line.append(other.getElementName()); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				}
			}
			this.line.append(']'); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		}
	}
}

	@Override
IJavaSearchScope getJavaSearchScope() {
	return SearchEngine.createJavaSearchScope(new IJavaProject[] {getJavaProject("JavaSearchBugs")});
}
IJavaSearchScope getJavaSearchScopeBugs(String packageName, boolean addSubpackages) throws JavaModelException {
	if (packageName == null) return getJavaSearchScope();
	return getJavaSearchPackageScope("JavaSearchBugs", packageName, addSubpackages);
}
@Override
public ICompilationUnit getWorkingCopy(String path, String source) throws JavaModelException {
	if (this.wcOwner == null) {
		this.wcOwner = new WorkingCopyOwner() {};
	}
	return getWorkingCopy(path, source, this.wcOwner);
}
@Override
public void setUpSuite() throws Exception {
	super.setUpSuite();
	JAVA_PROJECT = setUpJavaProject("JavaSearchBugs", "10");
}
@Override
public void tearDownSuite() throws Exception {
	deleteProject("JavaSearchBugs");
	super.tearDownSuite();
}
@Override
protected void setUp () throws Exception {
	super.setUp();
	this.resultCollector = new TestCollector();
	this.resultCollector.showAccuracy(true);
}

public void testBug529434_001() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
			"class B {\n" +
			"}\n" +
			"public class X {\n" +
			"    public static void main(String [] args) throws Exception {\n" +
			"    	var B = new B();\n" +
			"    }\n" +
			"}\n"
			);
	search("B", CONSTRUCTOR, REFERENCES, EXACT_RULE);
	assertSearchResults("src/X.java void X.main(String[]) [new B()] EXACT_MATCH");
}
// Add more tests here
}