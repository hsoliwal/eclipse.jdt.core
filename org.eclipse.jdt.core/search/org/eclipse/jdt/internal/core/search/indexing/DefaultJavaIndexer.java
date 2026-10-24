/*******************************************************************************
 * Copyright (c) 2011, 2013 IBM Corporation and others.
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
package org.eclipse.jdt.internal.core.search.indexing;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.internal.compiler.util.Util;
import org.eclipse.jdt.internal.core.index.FileIndexLocation;
import org.eclipse.jdt.internal.core.index.Index;
import org.eclipse.jdt.internal.core.index.IndexLocation;
import org.eclipse.jdt.internal.core.search.JavaSearchDocument;

public class DefaultJavaIndexer {
	private static final char JAR_SEPARATOR = IJavaSearchScope.JAR_FILE_ENTRY_SEPARATOR.charAt(0);

	public void generateIndexForJar(String pathToJar, String pathToIndexFile) throws IOException { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		File f = new File(pathToJar);
		if (!f.exists()) { //FLLDCIM: MARK — M31 M3FILE_IO CALL
			throw new FileNotFoundException(pathToJar + " not found"); //$NON-NLS-1$ //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		}
		IndexLocation indexLocation = new FileIndexLocation(new File(pathToIndexFile));
		Index index = new Index(indexLocation, pathToJar, false /*reuse index file*/);
		SearchParticipant participant = SearchEngine.getDefaultSearchParticipant();
		index.separator = JAR_SEPARATOR;
		try (ZipFile zip = new ZipFile(pathToJar)) { //FLLDCIM: MARK — M25 M3FILE_ARCHIVE_STREAM NEW
			for (Enumeration<? extends ZipEntry> e = zip.entries(); e.hasMoreElements();) { //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP //FLLDCIM: MARK — M25 M3FILE_ARCHIVE_STREAM CALL
				// iterate each entry to index it
				ZipEntry ze = e.nextElement();
				String zipEntryName = ze.getName();
				if (Util.isClassFileName(zipEntryName)) {
					final byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getZipEntryByteContent(ze, zip);
					JavaSearchDocument entryDocument = new JavaSearchDocument(ze, new Path(pathToJar), classFileBytes, participant);
					entryDocument.setIndex(index);
					new BinaryIndexer(entryDocument).indexDocument();
				}
			}
			index.save();
		}
		return;
	}
}
