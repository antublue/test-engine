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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.antublue.test.engine.TestEngine;
import org.antublue.test.engine.internal.descriptor.ArgumentTestDescriptor;
import org.antublue.test.engine.internal.descriptor.ClassTestDescriptor;
import org.antublue.test.engine.internal.descriptor.ExtendedAbstractTestDescriptor;
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

    private final List<TestDescriptor> testDescriptors;

    private final StopWatch stopWatch;

    public SummaryEngineExecutionListener() {
        testDescriptors = Collections.synchronizedList(new ArrayList<>());

        stopWatch = new StopWatch();
    }

    public void begin() {
        stopWatch.start();

        println(INFO + SEPARATOR);
        println(INFO + BANNER);
        println(INFO + SEPARATOR);
    }

    @Override
    public void executionSkipped(TestDescriptor testDescriptor, String reason) {
        if (testDescriptor instanceof ExtendedAbstractTestDescriptor) {
            testDescriptors.add(testDescriptor);
        }
    }

    @Override
    public void executionFinished(
            TestDescriptor testDescriptor, TestExecutionResult testExecutionResult) {
        if (testDescriptor instanceof ExtendedAbstractTestDescriptor) {
            testDescriptors.add(testDescriptor);
        }
    }

    public void end(String message) {
        stopWatch.stop();

        long testClassDescriptorFound = 0;
        long testClassDescriptorSuccess = 0;
        long testClassDescriptorFailure = 0;
        long testClassDescriptorSkipped = 0;

        long testArgumentDescriptorFound = 0;
        long testArgumentDescriptorSuccess = 0;
        long testArgumentDescriptorFailure = 0;
        long testArgumentDescriptorSkipped = 0;

        long testMethodDescriptorFound = 0;
        long testMethodDescriptorSuccess = 0;
        long testMethodDescriptorFailure = 0;
        long testMethodDescriptorSkipped = 0;

        for (TestDescriptor testDescriptor : testDescriptors) {
            ExtendedAbstractTestDescriptor extendedAbstractTestDescriptor =
                    (ExtendedAbstractTestDescriptor) testDescriptor;
            ExtendedAbstractTestDescriptor.Status status =
                    extendedAbstractTestDescriptor.getStatus();

            if (extendedAbstractTestDescriptor instanceof ClassTestDescriptor) {
                testClassDescriptorFound++;

                switch (status) {
                    case PASS:
                        {
                            testClassDescriptorSuccess++;
                            break;
                        }
                    case FAIL:
                        {
                            testClassDescriptorFailure++;
                            break;
                        }
                    case SKIPPED:
                        {
                            testClassDescriptorSkipped++;
                            break;
                        }
                    default:
                        {
                            // DO NOTHING
                        }
                }
            } else if (extendedAbstractTestDescriptor instanceof ArgumentTestDescriptor) {
                testArgumentDescriptorFound++;

                switch (status) {
                    case PASS:
                        {
                            testArgumentDescriptorSuccess++;
                            break;
                        }
                    case FAIL:
                        {
                            testArgumentDescriptorFailure++;
                            break;
                        }
                    case SKIPPED:
                        {
                            testArgumentDescriptorSkipped++;
                            break;
                        }
                    default:
                        {
                            // DO NOTHING
                        }
                }
            } else if (extendedAbstractTestDescriptor instanceof MethodTestDescriptor) {
                testMethodDescriptorFound++;

                switch (status) {
                    case PASS:
                        {
                            testMethodDescriptorSuccess++;
                            break;
                        }
                    case FAIL:
                        {
                            testMethodDescriptorFailure++;
                            break;
                        }
                    case SKIPPED:
                        {
                            testMethodDescriptorSkipped++;
                            break;
                        }
                    default:
                        {
                            // DO NOTHING
                        }
                }
            }
        }

        int columnWidthFound =
                getColumnWith(
                        testClassDescriptorFound,
                        testArgumentDescriptorFound,
                        testMethodDescriptorFound);
        int columnWidthSuccess =
                getColumnWith(
                        testClassDescriptorSuccess,
                        testArgumentDescriptorSuccess,
                        testMethodDescriptorSuccess);
        int columnWidthFailure =
                getColumnWith(
                        testClassDescriptorFailure,
                        testArgumentDescriptorFailure,
                        testMethodDescriptorFailure);
        int columnWidthSkipped =
                getColumnWith(
                        testClassDescriptorSkipped,
                        testArgumentDescriptorSkipped,
                        testMethodDescriptorSkipped);

        println(INFO + SEPARATOR);
        println(INFO + SUMMARY_BANNER);
        println(INFO + SEPARATOR);
        println(INFO + message);
        println(INFO + SEPARATOR);
        // TODO use AnsiStringBuilder for formatting
        println(
                new AnsiColorStringBuilder()
                        .append(INFO)
                        .color(AnsiColor.WHITE_BRIGHT)
                        .append("Classes")
                        .append("   : ")
                        .append(pad(testClassDescriptorFound, columnWidthFound))
                        .append(", ")
                        .color(AnsiColor.GREEN_BRIGHT)
                        .append("PASS")
                        .color(AnsiColor.WHITE_BRIGHT)
                        .append(" : ")
                        .append(pad(testClassDescriptorSuccess, columnWidthSuccess))
                        .append(", ")
                        .color(AnsiColor.RED_BRIGHT)
                        .append("FAIL")
                        .color(AnsiColor.WHITE_BRIGHT)
                        .append(" : ")
                        .append(pad(testClassDescriptorFailure, columnWidthFailure))
                        .append(", ")
                        .color(AnsiColor.YELLOW_BRIGHT)
                        .append("SKIPPED")
                        .color(AnsiColor.WHITE_BRIGHT)
                        .append(" : ")
                        .append(pad(testClassDescriptorSkipped, columnWidthSkipped))
                        .append(AnsiColor.TEXT_RESET));

        println(
                new AnsiColorStringBuilder()
                        .append(INFO)
                        .color(AnsiColor.WHITE_BRIGHT)
                        .append("Arguments")
                        .append(" : ")
                        .append(pad(testArgumentDescriptorFound, columnWidthFound))
                        .append(", ")
                        .color(AnsiColor.GREEN_BRIGHT)
                        .append("PASS")
                        .color(AnsiColor.WHITE_BRIGHT)
                        .append(" : ")
                        .append(pad(testArgumentDescriptorSuccess, columnWidthSuccess))
                        .append(", ")
                        .color(AnsiColor.RED_BRIGHT)
                        .append("FAIL")
                        .color(AnsiColor.WHITE_BRIGHT)
                        .append(" : ")
                        .append(pad(testArgumentDescriptorFailure, columnWidthFailure))
                        .append(", ")
                        .color(AnsiColor.YELLOW_BRIGHT)
                        .append("SKIPPED")
                        .color(AnsiColor.WHITE_BRIGHT)
                        .append(" : ")
                        .append(pad(testArgumentDescriptorSkipped, columnWidthSkipped))
                        .append(AnsiColor.TEXT_RESET));

        println(
                new AnsiColorStringBuilder()
                        .append(INFO)
                        .color(AnsiColor.WHITE_BRIGHT)
                        .append("Methods")
                        .append("   : ")
                        .append(pad(testMethodDescriptorFound, columnWidthFound))
                        .append(", ")
                        .color(AnsiColor.GREEN_BRIGHT)
                        .append("PASS")
                        .color(AnsiColor.WHITE_BRIGHT)
                        .append(" : ")
                        .append(pad(testMethodDescriptorSuccess, columnWidthSuccess))
                        .append(", ")
                        .color(AnsiColor.RED_BRIGHT)
                        .append("FAIL")
                        .color(AnsiColor.WHITE_BRIGHT)
                        .append(" : ")
                        .append(pad(testMethodDescriptorFailure, columnWidthFailure))
                        .append(", ")
                        .color(AnsiColor.YELLOW_BRIGHT)
                        .append("SKIPPED")
                        .color(AnsiColor.WHITE_BRIGHT)
                        .append(" : ")
                        .append(pad(testMethodDescriptorSkipped, columnWidthSkipped))
                        .append(AnsiColor.TEXT_RESET));

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
        return 0;
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

    /**
     * Method to column width of long values as Strings
     *
     * @param values values
     * @return the return value
     */
    private static int getColumnWith(long... values) {
        int width = 0;

        for (long value : values) {
            width = Math.max(String.valueOf(value).length(), width);
        }

        return width;
    }

    /**
     * Method to get a String that is the value passed to a specific width
     *
     * @param value value
     * @param width width
     * @return the return value
     */
    private static String pad(long value, long width) {
        String stringValue = String.valueOf(value);

        StringBuilder paddingStringBuilder = new StringBuilder();
        while ((paddingStringBuilder.length() + stringValue.length()) < width) {
            paddingStringBuilder.append(" ");
        }

        return paddingStringBuilder.append(stringValue).toString();
    }
}
