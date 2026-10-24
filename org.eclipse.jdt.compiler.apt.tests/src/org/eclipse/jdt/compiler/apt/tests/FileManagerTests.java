/*******************************************************************************
 * Copyright (c) 2007, 2015 BEA Systems, Inc. and others
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
 *    IBM Corporation - fixed a resource leak warning
 *******************************************************************************/

package org.eclipse.jdt.compiler.apt.tests;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLClassLoader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import javax.lang.model.SourceVersion;
import javax.tools.FileObject;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import junit.framework.TestCase;
import org.eclipse.jdt.internal.compiler.apt.util.EclipseFileManager;

/**
 * Test the implementation of the Filer interface,
 * in more detail than BatchDispatchTests does.
 * @since 3.4
 */
public class FileManagerTests extends TestCase {

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		BatchTestUtils.init();
	}

	public void testFileManager() { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		String tmpFolder = System.getProperty("java.io.tmpdir");
		File dir = new File(tmpFolder, "src" + System.currentTimeMillis()); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		dir.mkdirs(); //FLLDCIM: MARK — M31 M3FILE_IO CALL
		File inputFile = new File(dir, "X.java");
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(inputFile)); //FLLDCIM: MARK — M31 M3FILE_IO NEW
			writer.write("public class X {}");
			writer.flush();
			writer.close();
		} catch (IOException e) {
			// ignore
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					// ignore
				}
			}
		}
		StandardJavaFileManager fileManager = null;
		try {
			fileManager = new EclipseFileManager(Locale.getDefault(), Charset.defaultCharset());

			List<File> fins = new ArrayList<>(); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW
			fins.add(dir);

			JavaFileManager.Location sourceLoc = javax.tools.StandardLocation.SOURCE_PATH;
			fileManager.setLocation(sourceLoc, fins);

			Set<JavaFileObject.Kind> fileTypes = new HashSet<>(); //FLLDCIM: MARK — M03 M3_SET_LANES NEW
			fileTypes.add(JavaFileObject.Kind.SOURCE);

			Iterable<? extends JavaFileObject> compilationUnits = fileManager.list(sourceLoc, "", fileTypes, true);

			Iterator<? extends JavaFileObject> it = compilationUnits.iterator();
			StringBuilder builder = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
			while (it.hasNext()) { //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
				JavaFileObject next = it.next();
				String name = next.getName();
				name = name.replace('\\', '/');
				int lastIndexOf = name.lastIndexOf('/');
				builder.append(name.substring(lastIndexOf + 1)); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
			}
			assertEquals("Wrong contents", "X.java", String.valueOf(builder));

			List<File> files = new ArrayList<>(); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW
			files.add(dir);
			try {
				fileManager.getJavaFileObjectsFromFiles(files);
				fail("IllegalArgumentException should be thrown but not");
			} catch(IllegalArgumentException iae) {
				// Do nothing
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				fileManager.close();
			} catch (IOException e) {
				//ignore the exception
			}
		}
		// check that the .class file exist for X
		assertTrue("delete failed", inputFile.delete()); //FLLDCIM: MARK — M31 M3FILE_IO CALL
		assertTrue("delete failed", dir.delete()); //FLLDCIM: MARK — M31 M3FILE_IO CALL
	}
	// Test that JavaFileManager#inferBinaryName returns null for invalid file
	public void testInferBinaryName() { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		String tmpFolder = System.getProperty("java.io.tmpdir");
		File dir = new File(tmpFolder, "src" + System.currentTimeMillis()); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		dir.mkdirs(); //FLLDCIM: MARK — M31 M3FILE_IO CALL
		File inputFile = new File(dir, "test.txt");
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(inputFile)); //FLLDCIM: MARK — M31 M3FILE_IO NEW
			writer.write("This is not a valid Java file");
			writer.flush();
			writer.close();
		} catch (IOException e) {
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
				}
			}
		}
		try {
			StandardJavaFileManager fileManager = new EclipseFileManager(Locale.getDefault(), Charset.defaultCharset());

			List<File> fins = new ArrayList<>(); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW
			fins.add(dir);
			JavaFileManager.Location sourceLoc = javax.tools.StandardLocation.SOURCE_PATH;
			fileManager.setLocation(sourceLoc, fins);

			Set<JavaFileObject.Kind> fileTypes = new HashSet<>(); //FLLDCIM: MARK — M03 M3_SET_LANES NEW
			fileTypes.add(JavaFileObject.Kind.OTHER);

			Iterable<? extends JavaFileObject> compilationUnits = fileManager.list(sourceLoc, "", fileTypes, true);
			JavaFileObject invalid = null;
			for (JavaFileObject javaFileObject : compilationUnits) { //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
				invalid = javaFileObject;
				break;
			}
			String inferredName = fileManager.inferBinaryName(sourceLoc, invalid);
			fileManager.close();
			assertNull("Should return null for invalid file", inferredName);
		} catch (IOException e) {
			e.printStackTrace();
		}
		assertTrue("delete failed", inputFile.delete()); //FLLDCIM: MARK — M31 M3FILE_IO CALL
		assertTrue("delete failed", dir.delete()); //FLLDCIM: MARK — M31 M3FILE_IO CALL
	}

	public void testBug460085() { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		if (isOnJRE9()) return;
		try {
			boolean found = false;
			EclipseFileManager fileManager = null;
			fileManager = new EclipseFileManager(Locale.getDefault(), Charset.defaultCharset());
			Iterable <? extends File> files = fileManager.getLocation(javax.tools.StandardLocation.PLATFORM_CLASS_PATH);
			Iterator<? extends File> iter = files.iterator();
			while (iter.hasNext()) { //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
				File f = iter.next();
				if ("rt.jar".equals(f.getName())) {
					found = true;
					break;
				}
			}
			fileManager.close();
			assertTrue("rt.jar not found", found);
		} catch (IOException e) {
			fail(e.getMessage());
		}
	}
	private boolean isOnJRE9() {
		try {
			SourceVersion.valueOf("RELEASE_9");
		} catch(IllegalArgumentException iae) {
			return false;
		}
		return true;
	}

	public void testBug466878_getResource_defaultPackage() throws Exception {
		EclipseFileManager fileManager = new EclipseFileManager(Locale.getDefault(), Charset.defaultCharset());
		List<File> classpath = new ArrayList<>(); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW
		classpath.add(new File(BatchTestUtils.getPluginDirectoryPath(), "resources/targets/filemanager/classes"));
		classpath.add(new File(BatchTestUtils.getPluginDirectoryPath(), "resources/targets/filemanager/dependency.zip"));
		fileManager.setLocation(javax.tools.StandardLocation.CLASS_PATH, classpath);
		assertNotNull(fileManager.getFileForInput(javax.tools.StandardLocation.CLASS_PATH, "", "dirresource.txt"));
		assertNotNull(fileManager.getFileForInput(javax.tools.StandardLocation.CLASS_PATH, "", "jarresource.txt"));
		fileManager.close();
	}

	public void testBug514121_getClassloader_close() throws Exception {
		EclipseFileManager fileManager = new EclipseFileManager(Locale.getDefault(), Charset.defaultCharset());
		List<File> classpath = new ArrayList<>(); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW
		classpath.add(new File(BatchTestUtils.getPluginDirectoryPath(), "resources/targets/filemanager/dependency.zip"));
		fileManager.setLocation(javax.tools.StandardLocation.ANNOTATION_PROCESSOR_PATH, classpath);
		URLClassLoader loader = (URLClassLoader) fileManager
				.getClassLoader(javax.tools.StandardLocation.ANNOTATION_PROCESSOR_PATH);
		assertNotNull(loader.findResource("jarresource.txt")); // sanity check
		fileManager.close();
		assertNull(loader.findResource("jarresource.txt")); // assert the classloader is closed
	}

	@SuppressWarnings("removal") //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
	public void testBug573287_ArchiveFileObject_openInputStream() throws Exception {
		Path dir = Files.createTempDirectory("repro573287-");
		Path target = dir.resolve("copy573287.zip");
		File src = new File(BatchTestUtils.getPluginDirectoryPath(), "resources/targets/filemanager/dependency.zip");
		Path copy = Files.copy(src.toPath(), target); //FLLDCIM: MARK — M31 M3FILE_IO CALL

		extracted_ArchiveFileObject_openInputStream(copy); //do not inline (!) - otherwise the reference to local variables are not lost
		try {
			// Try to clean up:
			// On Windows this resulted in java.nio.file.FileSystemException because copy
			// was still open until GC:
			Files.delete(copy); // (impossible to delete a open file under Windows) //FLLDCIM: MARK — M31 M3FILE_IO CALL
			Files.delete(dir); //FLLDCIM: MARK — M31 M3FILE_IO CALL
		} catch (Exception e) {
			// clean up temp files:
			try {
				// clean up memory:
				System.gc();
				// runs finalize() - but only if extracted_ArchiveFileObject_openInputStream is not inlined(!):
				System.runFinalization();
				// clean up files - now it works:
				Files.delete(copy); //FLLDCIM: MARK — M31 M3FILE_IO CALL
				Files.delete(dir); //FLLDCIM: MARK — M31 M3FILE_IO CALL
			} catch (Exception e2) {
				e.addSuppressed(e2);
			}
			throw e;
		}
	}

	private void extracted_ArchiveFileObject_openInputStream(Path copy) throws IOException {
		try (EclipseFileManager fileManager = new EclipseFileManager(Locale.getDefault(), Charset.defaultCharset())) {
			List<File> classpath = new ArrayList<>(); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW
			classpath.add(copy.toFile());
			fileManager.setLocation(javax.tools.StandardLocation.CLASS_PATH, classpath);
			FileObject fileForInput = fileManager.getFileForInput(javax.tools.StandardLocation.CLASS_PATH, "",
					"jarresource.txt");
			assertNotNull(fileForInput);
			byte[] content;
			try (InputStream inputStream = fileForInput.openInputStream()) {
				// inputStream may be java.util.zip.ZipFile$ZipFileInputStream which would hold a reference to ZipFile
				content = inputStream.readAllBytes();
			}
			assertEquals(new String(content, StandardCharsets.UTF_8), "jar resource");
		}
	}
}
