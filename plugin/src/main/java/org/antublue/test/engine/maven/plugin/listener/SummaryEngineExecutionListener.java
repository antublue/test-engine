/*
 * Copyright (C) 2023 The AntuBLUE test-engine project authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.antublue.test.engine.maven.plugin.listener;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import org.antublue.test.engine.TestEngine;
import org.antublue.test.engine.internal.descriptor.ArgumentTestDescriptor;
import org.antublue.test.engine.internal.descriptor.ClassTestDescriptor;
import org.antublue.test.engine.internal.descriptor.MethodTestDescriptor;
import org.antublue.test.engine.internal.util.AnsiColor;
import org.antublue.test.engine.internal.util.AnsiColorStringBuilder;
import org.antublue.test.engine.internal.util.HumanReadableTime;
import org.antublue.test.engine.internal.util.NanosecondsConverter;
import org.antublue.test.engine.internal.util.StopWatch;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;

public class SummaryEngineExecutionListener
        implements org.junit.platform.engine.EngineExecutionListener {

    private static final String BANNER =
            new AnsiColorStringBuilder()
                    .color(AnsiColor.WHITE_BRIGHT)
                    .append("Antu")
                    .color(AnsiColor.BLUE_BOLD_BRIGHT)
                    .append("BLUE")
                    .color(AnsiColor.WHITE_BRIGHT)
                    .append(" Test Engine ")
                    .append(TestEngine.VERSION)
                    .color(AnsiColor.TEXT_RESET)
                    .toString();

    private static final String SUMMARY_BANNER = BANNER + AnsiColor.WHITE_BRIGHT.wrap(" Summary");

    private static final String SEPARATOR =
            AnsiColor.WHITE_BRIGHT.wrap(
                    "------------------------------------------------------------------------");

    private static final String INFO =
            new AnsiColorStringBuilder()
                    .color(AnsiColor.WHITE)
                    .append("[")
                    .color(AnsiColor.BLUE_BOLD)
                    .append("INFO")
                    .color(AnsiColor.WHITE)
                    .append("]")
                    .color(AnsiColor.TEXT_RESET)
                    .append(" ")
                    .toString();

    private final Set<Class<?>> testClasses;
    private final AtomicLong testClassesFoundCount;
    private final AtomicLong testClassesSuccessCount;
    private final AtomicLong testClassesFailedCount;
    private final AtomicLong testClassesSkippedCount;

    private final AtomicLong testArgumentsFoundCount;
    private final AtomicLong testArgumentsSuccessCount;
    private final AtomicLong testArgumentsFailedCount;
    private final AtomicLong argumentsSkippedCount;

    private final AtomicLong testMethodFoundCount;
    private final AtomicLong methodsSuccess;
    private final AtomicLong testMethodFailedCount;
    private final AtomicLong testMethodsSkippedCount;

    private final StopWatch stopWatch;

    public SummaryEngineExecutionListener() {
        testClasses = Collections.synchronizedSet(new HashSet<>());

        testClassesFoundCount = new AtomicLong();
        testClassesSuccessCount = new AtomicLong();
        testClassesFailedCount = new AtomicLong();
        testClassesSkippedCount = new AtomicLong();

        testArgumentsFoundCount = new AtomicLong();
        testArgumentsSuccessCount = new AtomicLong();
        testArgumentsFailedCount = new AtomicLong();
        argumentsSkippedCount = new AtomicLong();

        testMethodFoundCount = new AtomicLong();
        methodsSuccess = new AtomicLong();
        testMethodFailedCount = new AtomicLong();
        testMethodsSkippedCount = new AtomicLong();

        stopWatch = new StopWatch();
    }

    public void begin() {
        stopWatch.start();

        println(INFO + SEPARATOR);
        println(INFO + BANNER);
        println(INFO + SEPARATOR);
    }

    public long getTestClassCount() {
        return testClassesFoundCount.get();
    }

    public long getTestClassesSucceededCount() {
        return testClassesSuccessCount.get();
    }

    public long getTestClassesFailedCount() {
        return testClassesFailedCount.get();
    }

    public long getTestClassesSkippedCount() {
        return testClassesSkippedCount.get();
    }

    public long getTestArgumentsFoundCount() {
        return testArgumentsFoundCount.get();
    }

    public long getTestArgumentsSucceededCount() {
        return testArgumentsSuccessCount.get();
    }

    public long getTestArgumentsFailedCount() {
        return testArgumentsFailedCount.get();
    }

    public long getTestArgumentsSkippedCount() {
        return argumentsSkippedCount.get();
    }

    public long getTestsMethodsFoundCount() {
        return testMethodFoundCount.get();
    }

    public long getTestsSucceededCount() {
        return methodsSuccess.get();
    }

    public long getTestMethodsFailedCount() {
        return testMethodFailedCount.get();
    }

    public long getTestMethodsSkippedCount() {
        return testMethodsSkippedCount.get();
    }

    public void executionStarted(TestDescriptor testDescriptor) {
        if (testDescriptor instanceof ClassTestDescriptor) {
            testClasses.add(((ClassTestDescriptor) testDescriptor).getTestClass());
            testClassesFoundCount.set(testClasses.size());
            return;
        }

        if (testDescriptor instanceof ArgumentTestDescriptor) {
            testArgumentsFoundCount.incrementAndGet();
            return;
        }

        if (testDescriptor instanceof MethodTestDescriptor) {
            testMethodFoundCount.incrementAndGet();
        }
    }

    public void executionSkipped(TestDescriptor testDescriptor, String reason) {
        if (testDescriptor instanceof ArgumentTestDescriptor) {
            testArgumentsFoundCount.incrementAndGet();
            argumentsSkippedCount.incrementAndGet();
            return;
        }

        if (testDescriptor instanceof MethodTestDescriptor) {
            testMethodFoundCount.incrementAndGet();
            testMethodsSkippedCount.incrementAndGet();
        }
    }

    public void executionFinished(
            TestDescriptor testDescriptor, TestExecutionResult testExecutionResult) {
        if (testDescriptor instanceof ClassTestDescriptor) {
            TestExecutionResult.Status status = testExecutionResult.getStatus();
            switch (status) {
                case SUCCESSFUL:
                    {
                        testClassesSuccessCount.incrementAndGet();
                        break;
                    }
                case FAILED:
                    {
                        testClassesFailedCount.incrementAndGet();
                        break;
                    }
                case ABORTED:
                    {
                        testClassesSkippedCount.incrementAndGet();
                        break;
                    }
                default:
                    {
                        // DO NOTHING
                        break;
                    }
            }

            return;
        }

        if (testDescriptor instanceof ArgumentTestDescriptor) {
            TestExecutionResult.Status status = testExecutionResult.getStatus();
            switch (status) {
                case SUCCESSFUL:
                    {
                        testArgumentsSuccessCount.incrementAndGet();
                        break;
                    }
                case FAILED:
                    {
                        testArgumentsFailedCount.incrementAndGet();
                        break;
                    }
                case ABORTED:
                    {
                        argumentsSkippedCount.incrementAndGet();
                        break;
                    }
                default:
                    {
                        // DO NOTHING
                        break;
                    }
            }
        }

        if (testDescriptor instanceof MethodTestDescriptor) {
            TestExecutionResult.Status status = testExecutionResult.getStatus();
            switch (status) {
                case SUCCESSFUL:
                    {
                        methodsSuccess.incrementAndGet();
                        break;
                    }
                case FAILED:
                    {
                        testMethodFailedCount.incrementAndGet();
                        break;
                    }
                case ABORTED:
                    {
                        testMethodsSkippedCount.incrementAndGet();
                        break;
                    }
                default:
                    {
                        // DO NOTHING
                        break;
                    }
            }
        }
    }

    public void end(String message) {
        stopWatch.stop();

        println(INFO + SEPARATOR);
        println(INFO + SUMMARY_BANNER);
        println(INFO + SEPARATOR);
        println(INFO + message);
        println(INFO + SEPARATOR);

        long elapsedTime = stopWatch.elapsedTime();

        println(
                new AnsiColorStringBuilder()
                        .append(INFO)
                        .color(AnsiColor.WHITE_BRIGHT)
                        .append("Total Test Time : ")
                        .append(HumanReadableTime.toHumanReadable(elapsedTime, false))
                        .append(" (")
                        .append(
                                String.format(
                                        "%s",
                                        NanosecondsConverter.MILLISECONDS.toString(elapsedTime)))
                        .append(")")
                        .color(AnsiColor.TEXT_RESET));

        println(
                new AnsiColorStringBuilder()
                        .append(INFO)
                        .color(AnsiColor.WHITE_BRIGHT)
                        .append("Finished At     : ")
                        .append(HumanReadableTime.now())
                        .color(AnsiColor.TEXT_RESET));

        if (getFailureCount() == 0) {
            println(INFO + SEPARATOR);
        }
    }

    /**
     * Method to get the failure count
     *
     * @return the failure count
     */
    public long getFailureCount() {
        if (getTestClassCount() == 0) {
            return 0;
        }

        return getTestClassesFailedCount()
                + getTestArgumentsFailedCount()
                + getTestMethodsFailedCount();
    }

    /**
     * Method to println an Object
     *
     * @param object object
     */
    private static void println(Object object) {
        System.out.println(object);
        System.out.flush();
    }
}
