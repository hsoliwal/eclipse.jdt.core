/*******************************************************************************
 * Copyright (c) 2007, 2009 BEA Systems, Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    wharley@bea.com - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.apt.pluggable.tests.processors.genclass6;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import org.eclipse.jdt.apt.pluggable.tests.annotations.GenClass6;

/**
 * A processor that reads the GenClass6 annotation and produces the specified Java type
 */
@SupportedAnnotationTypes({ "org.eclipse.jdt.apt.pluggable.tests.annotations.GenClass6" })
@SupportedSourceVersion(SourceVersion.RELEASE_6)
@SupportedOptions({})
public class GenClass6Proc extends AbstractProcessor {

	private ProcessingEnvironment _processingEnv;
	private Messager _messager;
	private Filer _filer;
	private Map<String, Element> _classesToSummarize; // map of generated name to element that produced it

	/* (non-Javadoc)
	 * @see javax.annotation.processing.AbstractProcessor#init(javax.annotation.processing.ProcessingEnvironment)
	 */
	@Override
	public synchronized void init(ProcessingEnvironment processingEnv) {
		super.init(processingEnv);
		_processingEnv = processingEnv;
		_filer = _processingEnv.getFiler();
		_messager = _processingEnv.getMessager();
		_classesToSummarize = new HashMap<>(); //FLLDCIM: MARK — M01 M3_HASH_LANES NEW
	}

	/* (non-Javadoc)
	 * @see javax.annotation.processing.AbstractProcessor#process(java.util.Set, javax.annotation.processing.RoundEnvironment)
	 */
	@Override
	public boolean process(Set<? extends TypeElement> annotations,
			RoundEnvironment roundEnv)
	{
		if (roundEnv.processingOver() && !_classesToSummarize.isEmpty()) {
			summarize();
		}
		else if (!annotations.isEmpty()) {
			round(annotations, roundEnv);
		}
		return true;
	}

	/**
	 * Perform a round of processing
	 */
	private void round(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		TypeElement genClassAnno = annotations.iterator().next();
		Set<? extends Element> annotatedEls = roundEnv.getElementsAnnotatedWith(genClassAnno);
		for (Element e : annotatedEls) { //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
			GenClass6 genClassMirror = e.getAnnotation(GenClass6.class);
			processType(genClassMirror, e);
		}
	}

	private void processType(GenClass6 genClassMirror, Element annotatedEl) { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		// Collect and validate the parameters of the annotation
		String pkg = null;
		String name = null;
		String method = null;
		boolean summary = false;
		int rounds = 1;
		String[] options = null;
		try {
			pkg = genClassMirror.pkg();
			name = genClassMirror.name();
			method = genClassMirror.method();
			summary = genClassMirror.summary();
			rounds = genClassMirror.rounds();
			options = genClassMirror.options();
		} catch (Exception e) {
			_messager.printMessage(Diagnostic.Kind.WARNING, "Unable to read @GenClass6 annotation" + e.getLocalizedMessage(), annotatedEl); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
			return;
		}

		// Options allow the processor to expose certain error conditions.
		if (null != options) {
			Set<String> optionSet = new HashSet<>(Arrays.asList(options)); //FLLDCIM: MARK — M03 M3_SET_LANES NEW
			// See https://bugs.eclipse.org/269934: calling getEnclosedElements forces resolution of referenced types
			if (optionSet.contains("forceElementResolution")) {
				annotatedEl.getEnclosedElements();
			}
		}

		if (name.length() == 0) {
			// User hasn't specified name yet
			_messager.printMessage(Diagnostic.Kind.WARNING, "The name attribute is missing", annotatedEl);
			return;
		}
		if (pkg == null) {
			pkg = "";
		}
		String qname = (pkg.length() > 0) ? pkg + '.' + name : name; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		if (method == null) {
			method = "";
		}

		// Get a writer
		JavaFileObject jfo = null;
		try {
			jfo = _filer.createSourceFile(qname, annotatedEl);
		} catch (IOException e) {
			_messager.printMessage(Diagnostic.Kind.WARNING, "Unable to open file for class " + qname, annotatedEl); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
			return;
		}
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(jfo.openWriter());
		} catch (IOException e) {
			_messager.printMessage(Diagnostic.Kind.WARNING, "Unable to get writer for file " + jfo.getName()); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
			return;
		}

		// Generate the class
		if (summary) {
			_classesToSummarize.put(qname, annotatedEl);
		}
		pw.println("// Generated by " + this.getClass().getName()); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		pw.println("package " + pkg + ";"); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		if (rounds > 1) {
			pw.println("import " + GenClass6.class.getCanonicalName() + ";"); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
			StringBuilder sb = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
			sb.append("@GenClass6("); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			if (pkg.length() > 0) {
				sb.append("pkg = \""); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				sb.append(pkg); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				sb.append("\", "); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			}
			sb.append("name = \""); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			sb.append(name); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			sb.append("Gen\""); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			if (method.length() > 0) {
				sb.append(", method = \""); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				sb.append(method); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				sb.append("\""); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			}
			if (--rounds > 1) {
				sb.append(", rounds = "); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				sb.append(rounds); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			}
			if (summary) {
				sb.append(", summary = true"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			}
			sb.append(")"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			pw.println(sb.toString());
		}
		pw.println("public class " + name + "{"); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		if (method != null && method.length() > 0) {
			pw.println("\tpublic String " + method + "() { return null; }"); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		}
		pw.println("}");
		pw.close();
	}

	/**
	 * Generate the summary.txt file if requested
	 */
	protected void summarize() { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		PrintWriter pw = null;
		try {
			Element[] parents = new Element[_classesToSummarize.size()];
			parents = _classesToSummarize.values().toArray(parents);
			FileObject summaryFile = _filer.createResource(StandardLocation.SOURCE_OUTPUT, "", "summary.txt", parents);
			pw = new PrintWriter(summaryFile.openWriter());
			for (String clazz : _classesToSummarize.keySet()) { //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
				pw.println(clazz);
			}
			pw.flush();
		} catch (IOException e) {
			_messager.printMessage(Diagnostic.Kind.ERROR, "Unable to create summary.txt: " + e.getLocalizedMessage()); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		} finally {
			if (pw != null) {
				pw.close();
			}
		}
	}

}
