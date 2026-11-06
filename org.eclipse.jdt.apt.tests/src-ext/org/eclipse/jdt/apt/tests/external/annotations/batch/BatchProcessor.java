/*******************************************************************************
 * Copyright (c) 2005 BEA Systems, Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    tyeung@bea.com - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.apt.tests.external.annotations.batch;

import com.sun.mirror.apt.AnnotationProcessor;
import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.apt.Messager;
import com.sun.mirror.declaration.AnnotationTypeDeclaration;
import com.sun.mirror.declaration.Declaration;
import com.sun.mirror.declaration.TypeDeclaration;
import java.util.Collection;
import java.util.HashSet;

public class BatchProcessor implements AnnotationProcessor {

	// Store how often we've been called in order to
	// test classloader resetting
	private static int TIMES_CALLED = 0;

	private final AnnotationProcessorEnvironment _env;
	BatchProcessor(AnnotationProcessorEnvironment env){
		_env = env;
	}
	public void process() { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD

		final Messager msger = _env.getMessager();
		if( TIMES_CALLED == 0 ){

			final Collection<String> expectedList = new HashSet<>(); //FLLDCIM: MARK — M03 M3_SET_LANES NEW
			expectedList.add("p1.A");
			expectedList.add("p1.B");
			expectedList.add("p1.C");
			expectedList.add("p1.D");
			final Collection<TypeDeclaration> allTypes = _env.getSpecifiedTypeDeclarations();
			for( TypeDeclaration type : allTypes ){ //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
				expectedList.remove(type.getQualifiedName());
			}

			if( !expectedList.isEmpty() ){
				msger.printError("failed to find type " + expectedList); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
			}

			final Collection<String> expectedAnnotated = new HashSet<>(); //FLLDCIM: MARK — M03 M3_SET_LANES NEW
			expectedList.add("p1.A");
			expectedList.add("p1.C");
			final AnnotationTypeDeclaration batchAnnoDecl =
				(AnnotationTypeDeclaration)_env.getTypeDeclaration(Batch.class.getName());
			final Collection<Declaration> decls = _env.getDeclarationsAnnotatedWith(batchAnnoDecl);
			for( Declaration decl : decls  ){ //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
				if( decl instanceof TypeDeclaration )
					expectedAnnotated.remove( ((TypeDeclaration)decl).getQualifiedName() );
			}
			if( !expectedAnnotated.isEmpty() ){
				msger.printError("failed to find annotated type " + expectedAnnotated ); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
			}

			msger.printWarning("CompletedSuccessfully");
		}
		else{
			msger.printWarning("Called " + (TIMES_CALLED + 1) + " times."); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		}

		TIMES_CALLED++;
	}
}
