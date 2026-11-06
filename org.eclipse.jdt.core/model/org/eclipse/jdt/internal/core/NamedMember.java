/*******************************************************************************
 * Copyright (c) 2004, 2017 IBM Corporation and others.
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
package org.eclipse.jdt.internal.core;

import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.codeassist.ISelectionRequestor;
import org.eclipse.jdt.internal.codeassist.SelectionEngine;

public abstract class NamedMember extends Member {

	/*
	 * This element's name, or an empty <code>String</code> if this
	 * element does not have a name.
	 */
	final protected String name;

	public NamedMember(JavaElement parent, String name) {
		super(parent);
		this.name = name;
	}
	public NamedMember(JavaElement parent, String name, int occurrenceCount) {
		super(parent, occurrenceCount);
		this.name = name;
	}

	private void appendTypeParameters(StringBuilder buffer) throws JavaModelException { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		ITypeParameter[] typeParameters = getTypeParameters();
		int length = typeParameters.length;
		if (length == 0) return;
		buffer.append('<'); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		for (int i = 0; i < length; i++) { //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
			ITypeParameter typeParameter = typeParameters[i];
			buffer.append(typeParameter.getElementName()); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			String[] bounds = typeParameter.getBounds();
			int boundsLength = bounds.length;
			if (boundsLength > 0) {
				buffer.append(" extends "); //$NON-NLS-1$ //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				for (int j = 0; j < boundsLength; j++) { //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
					buffer.append(bounds[j]); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
					if (j < boundsLength-1)
						buffer.append(" & "); //$NON-NLS-1$ //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				}
			}
			if (i < length-1)
				buffer.append(", "); //$NON-NLS-1$ //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		}
		buffer.append('>'); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
	}

	@Override
	public String getElementName() {
		return this.name;
	}

	protected String getKey(IField field, boolean forceOpen) throws JavaModelException {
		StringBuilder key = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW

		// declaring class
		String declaringKey = getKey((IType) field.getParent(), forceOpen);
		key.append(declaringKey); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL

		// field name
		key.append('.'); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		key.append(field.getElementName()); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL

		return key.toString();
	}

	protected String getKey(IMethod method, boolean forceOpen) throws JavaModelException { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		StringBuilder key = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW

		// declaring class
		String declaringKey = getKey((IType) method.getParent(), forceOpen);
		key.append(declaringKey); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL

		// selector
		key.append('.'); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		if (!method.isConstructor()) { // empty selector for ctors, cf. BindingKeyResolver.consumeMethod()
			String selector = method.getElementName();
			key.append(selector); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		}

		// type parameters
		if (forceOpen) {
			ITypeParameter[] typeParameters = method.getTypeParameters();
			int length = typeParameters.length;
			if (length > 0) {
				key.append('<'); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				for (int i = 0; i < length; i++) { //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
					ITypeParameter typeParameter = typeParameters[i];
					String[] bounds = typeParameter.getBounds();
					int boundsLength = bounds.length;
					char[][] boundSignatures = new char[boundsLength][];
					for (int j = 0; j < boundsLength; j++) { //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
						boundSignatures[j] = Signature.createCharArrayTypeSignature(bounds[j].toCharArray(), method.isBinary());
						CharOperation.replace(boundSignatures[j], '.', '/');
					}
					char[] sig = Signature.createTypeParameterSignature(typeParameter.getElementName().toCharArray(), boundSignatures);
					key.append(sig); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				}
				key.append('>'); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			}
		}

		// parameters
		key.append('('); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		String[] parameters = method.getParameterTypes();
		for (String parameter : parameters) //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
			key.append(parameter.replace('.', '/')); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		key.append(')'); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL

		// return type
		if (forceOpen)
			key.append(method.getReturnType().replace('.', '/')); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		else
			key.append('V'); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL

		return key.toString();
	}

	protected String getKey(IType type, boolean forceOpen) throws JavaModelException { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		StringBuilder key = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		key.append('L'); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		String packageName = type.getPackageFragment().getElementName();
		key.append(packageName.replace('.', '/')); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		if (packageName.length() > 0)
			key.append('/'); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		String typeQualifiedName = type.getTypeQualifiedName('$');
		ICompilationUnit cu = (ICompilationUnit) type.getAncestor(IJavaElement.COMPILATION_UNIT);
		if (cu != null) {
			String cuName = cu.getElementName();
			String mainTypeName = cuName.substring(0, cuName.lastIndexOf('.'));
			int end = typeQualifiedName.indexOf('$');
			if (end == -1)
				end = typeQualifiedName.length();
			String topLevelTypeName = typeQualifiedName.substring(0, end);
			if (!mainTypeName.equals(topLevelTypeName)) {
				key.append(mainTypeName); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				key.append('~'); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			}
		}
		key.append(typeQualifiedName); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		key.append(';'); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		return key.toString();
	}
	protected String getKey(IModuleDescription module, boolean forceOpen) throws JavaModelException {
		StringBuilder key = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		key.append('"'); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		String modName = module.getElementName();
		key.append(modName); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		return key.toString();
	}

	protected String getFullyQualifiedParameterizedName(String fullyQualifiedName, String uniqueKey) throws JavaModelException {
		String[] typeArguments = new BindingKey(uniqueKey).getTypeArguments();
		int length = typeArguments.length;
		if (length == 0) return fullyQualifiedName;
		StringBuilder buffer = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buffer.append(fullyQualifiedName); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		buffer.append('<'); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		for (int i = 0; i < length; i++) { //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
			String typeArgument = typeArguments[i];
			buffer.append(Signature.toString(typeArgument)); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			if (i < length-1)
				buffer.append(','); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		}
		buffer.append('>'); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		return buffer.toString();
	}

	protected IPackageFragment getPackageFragment() {
		return null;
	}

	public String getFullyQualifiedName(char enclosingTypeSeparator, boolean showParameters) throws JavaModelException {
		String packageName = getPackageFragment().getElementName();
		if (packageName.equals(IPackageFragment.DEFAULT_PACKAGE_NAME)) {
			return getTypeQualifiedName(enclosingTypeSeparator, showParameters);
		}
		return packageName + '.' + getTypeQualifiedName(enclosingTypeSeparator, showParameters); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
	}

	public String getTypeQualifiedName(char enclosingTypeSeparator, boolean showParameters) throws JavaModelException { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		NamedMember declaringType;
		switch (this.getParent().getElementType()) {
			case IJavaElement.COMPILATION_UNIT:
				if (showParameters) {
					StringBuilder buffer = new StringBuilder(this.name); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
					appendTypeParameters(buffer);
					return buffer.toString();
				}
				return this.name;
			case IJavaElement.CLASS_FILE:
				if (this.getParent() instanceof IModularClassFile)
					return null;
				String classFileName = this.getParent().getElementName();
				String typeName;
				if (classFileName.indexOf('$') == -1) {
					// top level class file: name of type is same as name of class file
					typeName = this.name;
				} else {
					// anonymous or local class file
					typeName = classFileName.substring(0, classFileName.lastIndexOf('.'))/*remove .class*/.replace('$', enclosingTypeSeparator);
				}
				if (showParameters) {
					StringBuilder buffer = new StringBuilder(typeName); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
					appendTypeParameters(buffer);
					return buffer.toString();
				}
				return typeName;
			case IJavaElement.TYPE:
				declaringType = (NamedMember) this.getParent();
				break;
			case IJavaElement.FIELD:
			case IJavaElement.INITIALIZER:
			case IJavaElement.METHOD:
				declaringType = (NamedMember) ((IMember) this.getParent()).getDeclaringType();
				break;
			default:
				return null;
		}
		StringBuilder buffer = new StringBuilder(declaringType.getTypeQualifiedName(enclosingTypeSeparator, showParameters)); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		buffer.append(enclosingTypeSeparator); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		String simpleName = this.name.length() == 0 ? getOccurrenceCountSignature() : this.name;
		buffer.append(simpleName); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		if (showParameters) {
			appendTypeParameters(buffer);
		}
		return buffer.toString();
	}
	/*
	 * Returns the String representation of the occurrence count for this element.
	 * The occurrence count is a unique number representation to identify the particular element.
	 *
	 * @return the occurrence count for this element in the form of String
	 */
	protected String getOccurrenceCountSignature() {
		return Integer.toString(this.getOccurrenceCount());
	}
	protected ITypeParameter[] getTypeParameters() throws JavaModelException {
		return null;
	}

	/**
	 * @see IType#resolveType(String)
	 */
	public String[][] resolveType(String typeName) throws JavaModelException {
		return resolveType(typeName, DefaultWorkingCopyOwner.PRIMARY);
	}

	/**
	 * @see IType#resolveType(String, WorkingCopyOwner)
	 */
	public String[][] resolveType(String typeName, WorkingCopyOwner owner) throws JavaModelException { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		JavaProject project = getJavaProject();
		int release = JavaProject.getRelease(this);
		SearchableEnvironment environment = project.newSearchableNameEnvironment(owner, false, release);

		class TypeResolveRequestor implements ISelectionRequestor {
			String[][] answers = null;
			@Override
			public void acceptType(char[] packageName, char[] tName, int modifiers, boolean isDeclaration, char[] uniqueKey, int start, int end) {
				String[] answer = new String[]  {new String(packageName), new String(tName) };
				if (this.answers == null) {
					this.answers = new String[][]{ answer };
				} else {
					// grow
					int length = this.answers.length;
					System.arraycopy(this.answers, 0, this.answers = new String[length+1][], 0, length);
					this.answers[length] = answer;
				}
			}
			@Override
			public void acceptError(CategorizedProblem error) {
				// ignore
			}
			@Override
			public void acceptField(char[] declaringTypePackageName, char[] declaringTypeName, char[] fieldName, boolean isDeclaration, char[] uniqueKey, int start, int end) {
				// ignore
			}
			@Override
			public void acceptMethod(char[] declaringTypePackageName, char[] declaringTypeName, String enclosingDeclaringTypeSignature, char[] selector, char[][] parameterPackageNames, char[][] parameterTypeNames, String[] parameterSignatures, char[][] typeParameterNames, char[][][] typeParameterBoundNames, boolean isConstructor, boolean isDeclaration, char[] uniqueKey, int start, int end) {
				// ignore
			}
			@Override
			public void acceptPackage(char[] packageName){
				// ignore
			}
			@Override
			public void acceptTypeParameter(char[] declaringTypePackageName, char[] declaringTypeName, char[] typeParameterName, boolean isDeclaration, int start, int end) {
				// ignore
			}
			@Override
			public void acceptMethodTypeParameter(char[] declaringTypePackageName, char[] declaringTypeName, char[] selector, int selectorStart, int selcetorEnd, char[] typeParameterName, boolean isDeclaration, int start, int end) {
				// ignore
			}
			@Override
			public void acceptModule(char[] moduleName, char[] uniqueKey, int start, int end) {
				// ignore
			}

		}
		TypeResolveRequestor requestor = new TypeResolveRequestor();
		SelectionEngine engine =
			new SelectionEngine(environment, requestor, project.getOptions(true), owner);

		engine.selectType(typeName.toCharArray(), (IType) this);
		if (NameLookup.VERBOSE) {
			JavaModelManager.trace(Thread.currentThread() + " TIME SPENT in NameLoopkup#seekTypesInSourcePackage: " + environment.nameLookup.timeSpentInSeekTypesInSourcePackage + "ms");  //$NON-NLS-1$ //$NON-NLS-2$ //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
			JavaModelManager.trace(Thread.currentThread() + " TIME SPENT in NameLoopkup#seekTypesInBinaryPackage: " + environment.nameLookup.timeSpentInSeekTypesInBinaryPackage + "ms");  //$NON-NLS-1$ //$NON-NLS-2$ //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		}
		return requestor.answers;
	}
}
