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
import org.antublue.test.engine.AntuBLUETestEngine;
import org.antublue.test.engine.internal.descriptor.ArgumentTestDescriptor;
import org.antublue.test.engine.internal.descriptor.ClassTestDescriptor;
import org.antublue.test.engine.internal.descriptor.Metadata;
import org.antublue.test.engine.internal.descriptor.MetadataTestDescriptor;
import org.antublue.test.engine.internal.descriptor.MetadataTestDescriptorConstants;
import org.antublue.test.engine.internal.descriptor.TestMethodTestDescriptor;
import org.antublue.test.engine.internal.util.AnsiColor;
import org.antublue.test.engine.internal.util.AnsiColorStringBuilder;
import org.antublue.test.engine.internal.util.HumanReadableTimeUtils;
import org.antublue.test.engine.internal.util.StopWatch;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;

/** Class to implement a TestDescriptorSummaryEngineExecutionListener */
public class SummaryEngineExecutionListener
        implements org.junit.platform.engine.EngineExecutionListener {

    private static final String BANNER =
            new AnsiColorStringBuilder()
                    .color(AnsiColor.TEXT_WHITE_BRIGHT)
                    .append("Antu")
                    .color(AnsiColor.TEXT_BLUE_BOLD_BRIGHT)
                    .append("BLUE")
                    .color(AnsiColor.TEXT_WHITE_BRIGHT)
                    .append(" Test Engine ")
                    .append(AntuBLUETestEngine.VERSION)
                    .color(AnsiColor.TEXT_RESET)
                    .toString();

    private static final String SUMMARY_BANNER =
            BANNER + AnsiColor.TEXT_WHITE_BRIGHT.wrap(" Summary");

    private static final String SEPARATOR =
            AnsiColor.TEXT_WHITE_BRIGHT.wrap(
                    "------------------------------------------------------------------------");

    private static final String INFO =
            new AnsiColorStringBuilder()
                    .color(AnsiColor.TEXT_WHITE)
                    .append("[")
                    .color(AnsiColor.TEXT_BLUE_BOLD)
                    .append("INFO")
                    .color(AnsiColor.TEXT_WHITE)
                    .append("]")
                    .color(AnsiColor.TEXT_RESET)
                    .append(" ")
                    .toString();

    private boolean hasTests;

    private boolean hasFailures;

    private final List<TestDescriptor> testDescriptors;

    private final StopWatch stopWatch;

    /** Constructor */
    public SummaryEngineExecutionListener() {
        testDescriptors = Collections.synchronizedList(new ArrayList<>());

        stopWatch = new StopWatch();
    }

    /** Method to begin the summary output */
    public void begin() {
        stopWatch.reset();

        println(INFO + SEPARATOR);
        println(INFO + BANNER);
        println(INFO + SEPARATOR);
    }

    @Override
    public void executionStarted(TestDescriptor testDescriptor) {
        if (!testDescriptor.isRoot()) {
            hasTests = true;
        }
    }

    @Override
    public void executionSkipped(TestDescriptor testDescriptor, String reason) {
        testDescriptors.add(testDescriptor);
    }

    @Override
    public void executionFinished(
            TestDescriptor testDescriptor, TestExecutionResult testExecutionResult) {
        testDescriptors.add(testDescriptor);
        if (!testDescriptor.isRoot()
                && testExecutionResult.getStatus() == TestExecutionResult.Status.FAILED) {
            hasFailures = true;
        }
    }

    /**
     * Method to end the summary output
     *
     * @param message message
     */
    public void end(String message) {
        stopWatch.stop();

        long classTestDescriptorFound = 0;
        long classTestDescriptorSuccess = 0;
        long classTestDescriptorFailure = 0;
        long classTestDescriptorSkipped = 0;

        long argumentTestDescriptorFound = 0;
        long argumentTestDescriptorSuccess = 0;
        long argumentTestDescriptorFailure = 0;
        long argumentTestDescriptorSkipped = 0;

        long methodTestDescriptorFound = 0;
        long methodTestDescriptorSuccess = 0;
        long methodTestDescriptorFailure = 0;
        long methodTestDescriptorSkipped = 0;

        for (TestDescriptor testDescriptor : testDescriptors) {
            if (testDescriptor instanceof MetadataTestDescriptor) {
                Metadata metadata = ((MetadataTestDescriptor) testDescriptor).getMetadata();

                String testDescriptorStatus =
                        metadata.get(MetadataTestDescriptorConstants.TEST_DESCRIPTOR_STATUS);

                if (testDescriptor instanceof TestMethodTestDescriptor) {
                    methodTestDescriptorFound++;
                    switch (testDescriptorStatus) {
                        case "PASS":
                            {
                                methodTestDescriptorSuccess++;
                                break;
                            }
                        case "FAIL":
                            {
                                methodTestDescriptorFailure++;
                                break;
                            }
                        case "SKIP":
                            {
                                methodTestDescriptorSkipped++;
                                break;
                            }
                        default:
                            {
                                // DO NOTHING
                                break;
                            }
                    }
                } else if (testDescriptor instanceof ClassTestDescriptor) {
                    classTestDescriptorFound++;
                    switch (testDescriptorStatus) {
                        case "PASS":
                            {
                                classTestDescriptorSuccess++;
                                break;
                            }
                        case "FAIL":
                            {
                                classTestDescriptorFailure++;
                                break;
                            }
                        case "SKIP":
                            {
                                classTestDescriptorSkipped++;
                                break;
                            }
                        default:
                            {
                                // DO NOTHING
                                break;
                            }
                    }
                } else if (testDescriptor instanceof ArgumentTestDescriptor) {
                    argumentTestDescriptorFound++;
                    switch (testDescriptorStatus) {
                        case "PASS":
                            {
                                argumentTestDescriptorSuccess++;
                                break;
                            }
                        case "FAIL":
                            {
                                argumentTestDescriptorFailure++;
                                break;
                            }
                        case "SKIP":
                            {
                                argumentTestDescriptorSkipped++;
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
        }

        int columnWidthFound =
                getColumnWith(
                        classTestDescriptorFound,
                        argumentTestDescriptorFound,
                        methodTestDescriptorFound);

        int columnWidthSuccess =
                getColumnWith(
                        classTestDescriptorSuccess,
                        argumentTestDescriptorSuccess,
                        methodTestDescriptorSuccess);

        int columnWidthFailure =
                getColumnWith(
                        classTestDescriptorFailure,
                        argumentTestDescriptorFailure,
                        methodTestDescriptorFailure);

        int columnWidthSkipped =
                getColumnWith(
                        classTestDescriptorSkipped,
                        argumentTestDescriptorSkipped,
                        methodTestDescriptorSkipped);

        if (hasTests) {
            println(INFO + SEPARATOR);
            println(INFO + SUMMARY_BANNER);
            println(INFO + SEPARATOR);

            println(
                    new AnsiColorStringBuilder()
                            .append(INFO)
                            .color(AnsiColor.TEXT_WHITE_BRIGHT)
                            .append("Test Classes   : ")
                            .append(pad(classTestDescriptorFound, columnWidthFound))
                            .append(", ")
                            .color(AnsiColor.TEXT_GREEN_BRIGHT)
                            .append("PASSED")
                            .color(AnsiColor.TEXT_WHITE_BRIGHT)
                            .append(" : ")
                            .append(pad(classTestDescriptorSuccess, columnWidthSuccess))
                            .append(", ")
                            .color(AnsiColor.TEXT_RED_BRIGHT)
                            .append("FAILED")
                            .color(AnsiColor.TEXT_WHITE_BRIGHT)
                            .append(" : ")
                            .append(pad(classTestDescriptorFailure, columnWidthFailure))
                            .append(", ")
                            .color(AnsiColor.TEXT_YELLOW_BRIGHT)
                            .append("SKIPPED")
                            .color(AnsiColor.TEXT_WHITE_BRIGHT)
                            .append(" : ")
                            .append(pad(classTestDescriptorSkipped, columnWidthSkipped))
                            .append(AnsiColor.TEXT_RESET));

            if (argumentTestDescriptorFound > 0) {
                println(
                        new AnsiColorStringBuilder()
                                .append(INFO)
                                .color(AnsiColor.TEXT_WHITE_BRIGHT)
                                .append("Test Arguments : ")
                                .append(pad(argumentTestDescriptorFound, columnWidthFound))
                                .append(", ")
                                .color(AnsiColor.TEXT_GREEN_BRIGHT)
                                .append("PASSED")
                                .color(AnsiColor.TEXT_WHITE_BRIGHT)
                                .append(" : ")
                                .append(pad(argumentTestDescriptorSuccess, columnWidthSuccess))
                                .append(", ")
                                .color(AnsiColor.TEXT_RED_BRIGHT)
                                .append("FAILED")
                                .color(AnsiColor.TEXT_WHITE_BRIGHT)
                                .append(" : ")
                                .append(pad(argumentTestDescriptorFailure, columnWidthFailure))
                                .append(", ")
                                .color(AnsiColor.TEXT_YELLOW_BRIGHT)
                                .append("SKIPPED")
                                .color(AnsiColor.TEXT_WHITE_BRIGHT)
                                .append(" : ")
                                .append(pad(argumentTestDescriptorSkipped, columnWidthSkipped))
                                .append(AnsiColor.TEXT_RESET));
            }

            println(
                    new AnsiColorStringBuilder()
                            .append(INFO)
                            .color(AnsiColor.TEXT_WHITE_BRIGHT)
                            .append("Test Methods   : ")
                            .append(pad(methodTestDescriptorFound, columnWidthFound))
                            .append(", ")
                            .color(AnsiColor.TEXT_GREEN_BRIGHT)
                            .append("PASSED")
                            .color(AnsiColor.TEXT_WHITE_BRIGHT)
                            .append(" : ")
                            .append(pad(methodTestDescriptorSuccess, columnWidthSuccess))
                            .append(", ")
                            .color(AnsiColor.TEXT_RED_BRIGHT)
                            .append("FAILED")
                            .color(AnsiColor.TEXT_WHITE_BRIGHT)
                            .append(" : ")
                            .append(pad(methodTestDescriptorFailure, columnWidthFailure))
                            .append(", ")
                            .color(AnsiColor.TEXT_YELLOW_BRIGHT)
                            .append("SKIPPED")
                            .color(AnsiColor.TEXT_WHITE_BRIGHT)
                            .append(" : ")
                            .append(pad(methodTestDescriptorSkipped, columnWidthSkipped))
                            .append(AnsiColor.TEXT_RESET));
            println(INFO + SEPARATOR);
        }

        println(INFO + message);
        println(INFO + SEPARATOR);

        long elapsedTime = stopWatch.elapsedNanoseconds();

        println(
                new AnsiColorStringBuilder()
                        .append(INFO)
                        .color(AnsiColor.TEXT_WHITE_BRIGHT)
                        .append("Total Test Time : ")
                        .append(HumanReadableTimeUtils.toHumanReadable(elapsedTime, false))
                        .append(" (")
                        .append(elapsedTime / 1e+6)
                        .append(" ms)")
                        .color(AnsiColor.TEXT_RESET));

        println(
                new AnsiColorStringBuilder()
                        .append(INFO)
                        .color(AnsiColor.TEXT_WHITE_BRIGHT)
                        .append("Finished At     : ")
                        .append(HumanReadableTimeUtils.now())
                        .color(AnsiColor.TEXT_RESET));

        if (!hasFailures) {
            println(INFO + SEPARATOR);
        }
    }

    /**
     * Method to get whether tests were executed
     *
     * @return true if there were tests executed, otherwise false
     */
    public boolean hasTests() {
        return hasTests;
    }

    /**
     * Method to get whether failures were encountered
     *
     * @return true if there were failures, otherwise false
     */
    public boolean hasFailures() {
        return hasFailures;
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
