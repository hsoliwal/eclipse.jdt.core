/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
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
package org.eclipse.jdt.core.tests.formatter;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

@SuppressWarnings({"rawtypes", "unchecked"})
public class DecodeCodeFormatterPreferences extends DefaultHandler {

	private boolean record;
	private Map entries;
	private final String profileName;

	public static Map decodeCodeFormatterOptions(String fileName) { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		try {
			LineNumberReader reader = new LineNumberReader(new FileReader(fileName)); //FLLDCIM: MARK — M31 M3FILE_IO NEW
			Map options = new HashMap(); //FLLDCIM: MARK — M01 M3_HASH_LANES NEW
			String line;
			while ((line = reader.readLine()) != null) { //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
				if (line.startsWith("#")) {
					continue;
				}
				StringTokenizer stringTokenizer = new StringTokenizer(line, "=");
				options.put(stringTokenizer.nextElement(), stringTokenizer.nextElement());
			}
			reader.close();
			return options;
		} catch (IOException e) {
			// ignore
		}
		return null;
	}

	public static Map decodeCodeFormatterOptions(String fileName, String profileName) { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		try {
			@SuppressWarnings("restriction")
			SAXParser saxParser = org.eclipse.core.internal.runtime.XmlProcessorFactory.createSAXParserWithErrorOnDOCTYPE();
			final DecodeCodeFormatterPreferences preferences = new DecodeCodeFormatterPreferences(profileName);
			saxParser.parse(new File(fileName), preferences);
			return preferences.getEntries();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (FactoryConfigurationError e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Map decodeCodeFormatterOptions(String zipFileName, String zipEntryName, String profileName) { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		ZipFile zipFile = null;
		BufferedInputStream inputStream = null;
		try {
			zipFile = new ZipFile(zipFileName); //FLLDCIM: MARK — M25 M3FILE_ARCHIVE_STREAM NEW
			ZipEntry zipEntry = zipFile.getEntry(zipEntryName);
			if (zipEntry == null) {
				return null;
			}
			inputStream = new BufferedInputStream(zipFile.getInputStream(zipEntry)); //FLLDCIM: MARK — M25 M3FILE_ARCHIVE_STREAM CALL
			@SuppressWarnings("restriction")
			SAXParser saxParser = org.eclipse.core.internal.runtime.XmlProcessorFactory.createSAXParserWithErrorOnDOCTYPE();
			final DecodeCodeFormatterPreferences preferences = new DecodeCodeFormatterPreferences(profileName);
			saxParser.parse(inputStream, preferences);
			return preferences.getEntries();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (FactoryConfigurationError e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (inputStream != null) {
					inputStream.close();
				}
				if (zipFile != null) {
					zipFile.close();
				}
			} catch (IOException e1) {
				// Do nothing
			}
		}
		return null;
	}

	DecodeCodeFormatterPreferences(String profileName) {
		this.profileName = profileName;
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.ContentHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {

		int attributesLength = attributes.getLength();
		if ("profile".equals(qName)) {
			for (int i = 0; i < attributesLength; i++) { //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
				if ("name".equals(attributes.getQName(i)) && this.profileName.equals(attributes.getValue(i))) {
					this.record = true;
					this.entries = new HashMap(); //FLLDCIM: MARK — M01 M3_HASH_LANES NEW
					break;
				}
			}
		} else if ("setting".equals(qName) && this.record) {
			if (attributesLength == 2) {
				this.entries.put(attributes.getValue(0), attributes.getValue(1));
			}
		}
	}
	/* (non-Javadoc)
	 * @see org.xml.sax.ContentHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		if ("profile".equals(qName) && this.record) {
			this.record = false;
		}
	}
	/**
	 * @return Returns the entries.
	 */
	public Map getEntries() {
		return this.entries;
	}
}
