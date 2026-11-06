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
package org.eclipse.jdt.apt.tests.annotations.mirrortest;

import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.apt.Messager;
import com.sun.mirror.declaration.AnnotationTypeDeclaration;
import com.sun.mirror.declaration.AnnotationTypeElementDeclaration;
import com.sun.mirror.declaration.Declaration;
import com.sun.mirror.declaration.FieldDeclaration;
import com.sun.mirror.declaration.MethodDeclaration;
import com.sun.mirror.declaration.TypeDeclaration;
import com.sun.mirror.type.DeclaredType;
import java.util.Collection;
import org.eclipse.jdt.apt.tests.annotations.BaseProcessor;

public class DefaultConstantProcessor extends BaseProcessor {
	public DefaultConstantProcessor(AnnotationProcessorEnvironment env){
		super(env);
	}
	public void process() { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		final AnnotationTypeDeclaration trigger = (AnnotationTypeDeclaration)_env.getTypeDeclaration("test.Trigger");

		final Messager msger = _env.getMessager();
		if( trigger == null)
			msger.printError("cannot find test.Trigger");

		final Collection<Declaration> decls = _env.getDeclarationsAnnotatedWith(trigger);
		for(Declaration decl : decls ){ //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
			if( decl instanceof TypeDeclaration ){
				final TypeDeclaration typeDecl = (TypeDeclaration)decl;
				if( "test.EntryPoint".equals(typeDecl.getQualifiedName()) ){
					final Collection<FieldDeclaration> fields = typeDecl.getFields();
					for( FieldDeclaration field : fields ){ //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
						final String name = field.getSimpleName();
						if( "nestedAnno".equals(name)){
							final DeclaredType fieldType = (DeclaredType)field.getType();
							final Collection<TypeDeclaration> nestedTypes = fieldType.getDeclaration().getNestedTypes();
							for(TypeDeclaration nestedType : nestedTypes ){ //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
								if( "NestedAnnotation".equals(nestedType.getSimpleName()) ){
									final Collection<? extends MethodDeclaration> annotationMethods = nestedType.getMethods();
									for( MethodDeclaration annotationMethod : annotationMethods ){ //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
										if( "value".equals(annotationMethod.getSimpleName()) ){
											final AnnotationTypeElementDeclaration value =
												(AnnotationTypeElementDeclaration)annotationMethod;
											final String defaultString = value.getDefaultValue() == null ? "" :
												value.getDefaultValue().toString();
											final String expected = "Eore";
											if(!defaultString.equals(expected) )
												msger.printError("expecting default=" + expected + " but got " +defaultString); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
										}
									}
								}
							}

							final Collection<FieldDeclaration> nestedAnnoFields = fieldType.getDeclaration().getFields();
							for(FieldDeclaration nestedAnnoField : nestedAnnoFields ){ //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
								if(nestedAnnoField.getSimpleName().equals("FOUR")){
									final Object constant = nestedAnnoField.getConstantValue();
									final String expected = "4";
									final String constantStr = constant == null ? "" : constant.toString();
									if(!constantStr.equals(expected) )
										msger.printError("expecting constant=" + expected + " but got " + constantStr); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
								}
							}
							continue;
						}
						else{
							msger.printError("found unexpected field " + field ); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
						}

					}

					continue;
				}
			}
			msger.printError("found unexpected declaration " + decl ); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		}
	}
}
