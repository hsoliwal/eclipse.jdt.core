/*******************************************************************************
 * Copyright (c) 2016 Google, Inc and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Stefan Xenos (Google) - Initial implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ICoreRunnable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.jobs.Job;

public class FreezeMonitor {

	private static /* @Nullable */ Job monitorJob;

	public static void expectCompletionIn(final long millis) { //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
		done();
		monitorJob = Job.create("", new ICoreRunnable() { //FLLDCIM: MARK — T09 BACKGROUND_MODEL_THREAD CALL
			@Override //FLLDCIM: MARK — ML METHOD_SPLIT METHOD
			public void run(IProgressMonitor monitor) throws CoreException {
				if (monitor.isCanceled()) {
					throw new OperationCanceledException();
				}
				StringBuilder result = new StringBuilder(); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE NEW
				result.append("Possible frozen test case\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				ThreadMXBean threadStuff = ManagementFactory.getThreadMXBean();
				ThreadInfo[] allThreads = threadStuff.getThreadInfo(threadStuff.getAllThreadIds(), 200);
				for (ThreadInfo threadInfo : allThreads) { //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
					result.append("\""); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
					result.append(threadInfo.getThreadName()); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
					result.append("\": "); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
					result.append(threadInfo.getThreadState()); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
					result.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
					final StackTraceElement[] elements = threadInfo.getStackTrace();
					for (StackTraceElement element : elements) { //FLLDCIM: MARK — M32 LOOP_TO_LANE LOOP
						result.append("    "); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
						result.append(element); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
						result.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
					}
					result.append("\n"); //FLLDCIM: MARK — M14 M3STRING_COMPOSITE CALL
				}
				System.out.println(result.toString());
			}
		});
		monitorJob.schedule(millis);
	}

	public static void done() {
		if (monitorJob != null) {
			monitorJob.cancel();
			monitorJob = null;
		}
	}
}
