/*******************************************************************************
 * Copyright (c) 2021 jkubitz and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     jkubitz - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.jdt.core.tests.junit.extension.TestCase;
import org.eclipse.jdt.internal.compiler.util.CharDeduplication;

public class CharDeduplicationTest extends TestCase {

	public CharDeduplicationTest(String testName) {
		super(testName);
	}

	public static Test suite() {

		TestSuite suite = new TestSuite(CharDeduplicationTest.class.getPackageName());
		suite.addTest(new TestSuite(CharDeduplicationTest.class));
		return suite;
	}

	public void testDeduplication() { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		for (int i = 0; i < 3; i++) { //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
			assertDeduplication("a");
			// ..
			assertDeduplication("z");

			assertDeduplication("12");
			assertDeduplication("123");
			assertDeduplication("1234");
			assertDeduplication("12345");
			assertDeduplication("123456");
			assertDeduplication("1234567");
			assertDeduplication("eclipse");

			// new:
			assertDeduplication("0"); // illegal identifier - but who cares.
			assertDeduplication("A"); // why not?
			assertDeduplication("$"); // legal
			assertDeduplication("_"); // note that "_" may become more common after JEP 302 as keyword for unused
										// lambda parameter!
			assertDeduplication("" + (char) 0); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
			// ..
			assertDeduplication("" + (char) 127); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
			assertDeduplication("" + (char) 128); // non-Ascii //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		}
	}

	public void testDeduplicationMid() {
		String text = "abcdefghijklmn";
		for (int start = 0; start < text.length(); start++) { //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
			for (int end = start; end < text.length(); end++) { //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
				assertDedup(text, true, start, end);
			}
		}
	}

	@SuppressWarnings("deprecation") //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
	public void testDeduplicationTableSize() {
		CharDeduplication deduplication = CharDeduplication.getThreadLocalInstance();
		deduplication.reset();// to test the overflow we need to start empty
		for (int overload = 0; overload < 3; overload++) { //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
			HashMap<Integer, char[]> expecteds = new HashMap<>(); //FLLDCIM: MARK — M01 M3_HASH_LANES NEW
			for (int i = 0; i < CharDeduplication.SEARCH_SIZE + overload; i++) { //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
				int numberWithFixedLength = 10000 + i;
				String string = "" + numberWithFixedLength; //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
				char[] a = string.toCharArray();
				char[] expected = deduplication.sharedCopyOfRange(a, 0, a.length);
				expecteds.put(i, expected);
			}
			for (int t = 0; t < 2; t++) { //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
				for (int i = 0; i < expecteds.size(); i++) { //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
					char[] expected = expecteds.get(i);
					char[] other = String.valueOf(expected).toCharArray();
					if (overload == 0 || i > 0) {
						assertDedup(true, 0, expected.length, expected, other);
					} else {
						// situation after table overflow:
						char[] actual = deduplication.sharedCopyOfRange(other, 0, expected.length);
						// both actual == expected or actual != expected may happen
						// but we can assert that the next deduplication works again:
						char[] other2 = String.valueOf(expected).toCharArray();
						assertDedup(true, 0, expected.length, actual, other2);
					}
				}
			}
		}
	}

	@SuppressWarnings("unused")
	public static void main(String[] args) {
		CharDeduplicationTest test=new CharDeduplicationTest("");
		System.out.println("min= ~"+ LongStream.range(0, 20).map(t->test.runPerformanceTest()).min().getAsLong()/1_000_000_000d); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		//.21 s
	}

	public long runPerformanceTest() { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		CharDeduplication deduplication = CharDeduplication.getThreadLocalInstance();
		int MAX_LENGTH = 6;
		int RUNS = 1000;
		int TOKENS = 1080;
		Random rnd = new Random(0);

		StringBuilder content = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
		for (int i = 0; i < TOKENS + MAX_LENGTH; i++) { //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
			content.append(((char) (65 + (Math.round(15.0/rnd.nextDouble()) & 15)))); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
		}
		char[] contents = content.toString().toCharArray();
		ArrayList<char[]> results = new ArrayList<>(RUNS * TOKENS); //FLLDCIM: MARK — M02 M3_ARRAY_LANES NEW
		long nanoTime = System.nanoTime();
		for (int l = 1; l < MAX_LENGTH; l++) { //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
			for (int j = 0; j < RUNS; j++) { //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
					for (int i = 0; i < TOKENS; i++) { //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
						char[] result = deduplication.sharedCopyOfRange(contents, i, i + l);
						results.add(result);
					}
			}
		}
		long nanoTime2 = System.nanoTime();
		long durationNanos = nanoTime2 - nanoTime;
		System.out.println(durationNanos);
		IdentityHashMap<char[], char[]> identityHashMap = new IdentityHashMap<>(); //FLLDCIM: MARK — M06 M3_IDENTITY_WEAK_LANES NEW
		for (char[] r : results) { //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
			identityHashMap.put(r, r);
		}
		System.out.println("deduplicated " + (results.size() - identityHashMap.size()) * 100f / results.size() + "%"); // 99.9% //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		return durationNanos;
	}

	public void testAll() {
		testDeduplication();
		testDeduplicationMid();
		testDeduplicationTableSize();
	}

	@SuppressWarnings("unused")
	public void testMultithreaded() throws Exception {
		int nThreads = 8;
		List<FutureTask<Object>> tasks = IntStream.range(0, nThreads * 2).mapToObj(i -> new FutureTask<>(() -> {
			testAll();
			return null;
		})).collect(Collectors.toList());
		ExecutorService executor = Executors.newFixedThreadPool(nThreads);
		tasks.forEach(executor::submit);
		for (FutureTask<Object> task : tasks) { //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
			try {
				task.get();
			} catch (Exception e) {
				throw new AssertionError(e);
			}
		}
		executor.shutdownNow();
	}

	private void assertDeduplication(String string) {
		assertDedup(string, true, 0, string.length());
	}

	@SuppressWarnings("unused")
	private void assertNoDeduplication(String string) {
		assertDedup(string, false, 0, string.length());
	}

	private void assertDedup(String string, boolean same, int startPosition, int end) {
		char[] a = string.toCharArray();
		char[] b = string.toCharArray();
		assertNotSame(a, b);
		CharDeduplication deduplication = CharDeduplication.getThreadLocalInstance();
		char[] expected = deduplication.sharedCopyOfRange(a, startPosition, end);
		assertDedup(same, startPosition, end, expected, b);
	}

	private char[] assertDedup(boolean same, int startPosition, int end, char[] expected, char[] other) {
		assertNotSame(expected, other);
		CharDeduplication deduplication = CharDeduplication.getThreadLocalInstance();
		char[] actual = deduplication.sharedCopyOfRange(other, startPosition, end);
		String state = "expected=" + String.valueOf(expected) + ", actual=" + String.valueOf(actual); //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
		if (same) {
			assertSame(state, expected, actual);
		} else {
			assertNotSame("Expected different instances. But thats not a requirement but an implementation detail test:" //FLLDCIM: MARK — M33 M3STRING_COMPOSITE CONCAT
					+ state, expected, actual);
		}
		return actual;
	}
}
