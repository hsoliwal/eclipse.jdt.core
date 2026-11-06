/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others.
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
package org.eclipse.jdt.core.tests.rewrite.describing;

import junit.framework.Test;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

public class ASTRewritingPackageDeclTest extends ASTRewritingTest {

	public ASTRewritingPackageDeclTest(String name) {
		super(name);
	}
	public ASTRewritingPackageDeclTest(String name, int apiLevel) {
		super(name, apiLevel);
	}

	public static Test suite() {
		return createSuite(ASTRewritingPackageDeclTest.class);
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=328400
	 */
	public void testAnnotations_since_3() throws Exception { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("package test1;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		ICompilationUnit cu= pack1.createCompilationUnit("package-info.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		AST ast= astRoot.getAST();
		{ // insert annotation first
			PackageDeclaration packageDeclaration = astRoot.getPackage();
			ListRewrite listRewrite= rewrite.getListRewrite(packageDeclaration, PackageDeclaration.ANNOTATIONS_PROPERTY);
			MarkerAnnotation annot= ast.newMarkerAnnotation();
			annot.setTypeName(ast.newSimpleName("Deprecated"));
			listRewrite.insertFirst(annot, null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buf.append("@Deprecated\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buf.append("package test1;\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		assertEqualString(preview, buf.toString());
	}
}
