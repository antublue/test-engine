/*
 * Copyright 2022-2023 Douglas Hoard
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

package org.antublue.test.engine.internal;

import org.antublue.test.engine.TestEngine;
import org.antublue.test.engine.TestEngineConstants;
import org.antublue.test.engine.api.Parameter;
import org.antublue.test.engine.internal.descriptor.ClassTestDescriptor;
import org.antublue.test.engine.internal.descriptor.MethodTestDescriptor;
import org.antublue.test.engine.internal.descriptor.ParameterTestDescriptor;
import org.antublue.test.engine.internal.logger.Logger;
import org.antublue.test.engine.internal.logger.LoggerFactory;
import org.antublue.test.engine.internal.util.AnsiColor;
import org.antublue.test.engine.internal.util.HumanReadableTime;
import org.antublue.test.engine.internal.util.Switch;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.reporting.ReportEntry;
import org.junit.platform.engine.support.descriptor.EngineDescriptor;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

import java.lang.reflect.Method;

public class TestEngineExecutionListener implements EngineExecutionListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestEngine.class);

    private static final String INFO = AnsiColor.WHITE_BRIGHT.apply("[")
            + AnsiColor.BLUE_BOLD.apply("INFO")
            + AnsiColor.WHITE_BRIGHT.apply("]");

    private static final String TEST = AnsiColor.WHITE_BRIGHT.apply("TEST");
    private static final String ABORT = AnsiColor.YELLOW_BOLD.apply("ABORT");
    private static final String FAIL = AnsiColor.RED_BOLD.apply("FAIL");
    private static final String PASS = AnsiColor.GREEN_BOLD.apply("PASS");

    private final TestPlan testPlan;
    private final SummaryGeneratingListener summaryGeneratingListener;
    private final boolean detailedOutput;
    private final boolean logTestMessages;
    private final boolean logPassMessages;
    private final long startTimeMilliseconds;

    public TestEngineExecutionListener(TestPlan testPlan) {
        LOGGER.trace("TestEngineSummaryEngineExecutionListener constructor()");

        this.testPlan = testPlan;
        this.startTimeMilliseconds = System.currentTimeMillis();
        this.summaryGeneratingListener = new SummaryGeneratingListener();
        this.summaryGeneratingListener.testPlanExecutionStarted(testPlan);

        this.detailedOutput =
                TestEngineConfigurationParameters.getInstance()
                        .get(TestEngineConstants.CONSOLE_OUTPUT)
                        .map(value -> {
                            try {
                                return Boolean.parseBoolean(value);
                            } catch (NumberFormatException e) {
                                return true;
                            }
                        })
                        .orElse(true);

        this.logTestMessages =
                TestEngineConfigurationParameters.getInstance()
                        .get(TestEngineConstants.LOG_TEST_MESSAGES)
                        .map(value -> {
                            try {
                                return Boolean.parseBoolean(value);
                            } catch (NumberFormatException e) {
                                return true;
                            }
                        })
                        .orElse(true);

        this.logPassMessages =
                TestEngineConfigurationParameters.getInstance()
                        .get(TestEngineConstants.LOG_PASS_MESSAGES)
                        .map(value -> {
                            try {
                                return Boolean.parseBoolean(value);
                            } catch (NumberFormatException e) {
                                return true;
                            }
                        })
                        .orElse(true);
    }

    @Override
    public void dynamicTestRegistered(TestDescriptor testDescriptor) {
        summaryGeneratingListener.dynamicTestRegistered(TestIdentifier.from(testDescriptor));
    }

    @Override
    public void executionSkipped(TestDescriptor testDescriptor, String reason) {
        summaryGeneratingListener.executionSkipped(TestIdentifier.from(testDescriptor), reason);
    }

    @Override
    public void executionStarted(TestDescriptor testDescriptor) {
        if (testDescriptor instanceof MethodTestDescriptor) {
            summaryGeneratingListener.executionStarted(TestIdentifier.from(testDescriptor));
        }

        final StringBuilder stringBuilder = new StringBuilder();

        Switch.switchType(
                testDescriptor,
                Switch.switchCase(EngineDescriptor.class, consumer -> {}),
                Switch.switchCase(ClassTestDescriptor.class, consumer -> {}),
                Switch.switchCase(ParameterTestDescriptor.class, consumer -> {
                    if (logTestMessages) {
                        ParameterTestDescriptor testEngineParameterTestDescriptor = (ParameterTestDescriptor) testDescriptor;
                        Class<?> testClass = testEngineParameterTestDescriptor.getTestClass();
                        Parameter testParameter = testEngineParameterTestDescriptor.getTestParameter();
                        String testParameterName = testParameter.name();
                        stringBuilder
                                .append(TEST)
                                .append(" | ")
                                .append(testParameterName)
                                .append(" | ")
                                .append(testClass.getName());
                    }
                }),
                Switch.switchCase(MethodTestDescriptor.class, consumer -> {
                    if (logTestMessages) {
                        MethodTestDescriptor methodTestDescriptor = (MethodTestDescriptor) testDescriptor;
                        Class<?> testClass = methodTestDescriptor.getTestClass();
                        Method testMethod = methodTestDescriptor.getTestMethod();
                        Parameter testParameter = methodTestDescriptor.getTestParameter();
                        String testParameterName = testParameter.name();
                        stringBuilder
                                .append(TEST)
                                .append(" | ")
                                .append(testParameterName)
                                .append(" | ")
                                .append(testClass.getName())
                                .append(" ")
                                .append(testMethod.getName())
                                .append("()");
                    }
                })
        );

        if (detailedOutput && (stringBuilder.length() > 0)) {
            //LOGGER.rawInfo(stringBuilder.toString());
            System.out.println(INFO + " " + Thread.currentThread().getName() + " | " + stringBuilder);
        }
    }

    @Override
    public void executionFinished(TestDescriptor testDescriptor, TestExecutionResult testExecutionResult) {
        if (testDescriptor instanceof MethodTestDescriptor) {
            summaryGeneratingListener.executionFinished(TestIdentifier.from(testDescriptor), testExecutionResult);
        }

        final StringBuilder stringBuilder = new StringBuilder();

        Switch.switchType(
                testDescriptor,
                Switch.switchCase(EngineDescriptor.class, consumer -> {}),
                Switch.switchCase(ClassTestDescriptor.class, consumer -> {}),
                Switch.switchCase(ParameterTestDescriptor.class, consumer -> {
                    if (logPassMessages) {
                        ParameterTestDescriptor testengineParameterTestDescriptor = (ParameterTestDescriptor) testDescriptor;
                        Class<?> testClass = testengineParameterTestDescriptor.getTestClass();
                        Parameter testParameter = testengineParameterTestDescriptor.getTestParameter();
                        String testParameterName = testParameter.name();
                        stringBuilder
                                .append("%s | ")
                                .append(testParameterName)
                                .append(" | ")
                                .append(testClass.getName());
                    }
                }),
                Switch.switchCase(MethodTestDescriptor.class, consumer -> {
                    if (logPassMessages) {
                        MethodTestDescriptor methodTestDescriptor = (MethodTestDescriptor) testDescriptor;
                        Class<?> testClass = methodTestDescriptor.getTestClass();
                        Method testMethod = methodTestDescriptor.getTestMethod();
                        Parameter testParameter = methodTestDescriptor.getTestParameter();
                        String testParameterName = testParameter.name();
                        stringBuilder
                                .append("%s | ")
                                .append(testParameterName)
                                .append(" | ")
                                .append(testClass.getName())
                                .append(" ")
                                .append(testMethod.getName())
                                .append("()");
                    }
                }));

        if (stringBuilder.length() > 0) {
            TestExecutionResult.Status status = testExecutionResult.getStatus();
            String string = null;
            switch (status) {
                case ABORTED: {
                    string = String.format(stringBuilder.toString(), ABORT);
                    break;
                }
                case FAILED: {
                    string = String.format(stringBuilder.toString(), FAIL);
                    break;
                }
                case SUCCESSFUL: {
                    string = String.format(stringBuilder.toString(), PASS);
                    break;
                }
                default: {
                    // DO NOTHING
                    break;
                }
            }

            if (detailedOutput && (string != null)) {
                System.out.println(INFO + " " + Thread.currentThread().getName() + " | " + string);
            }
        }
    }

    @Override
    public void reportingEntryPublished(TestDescriptor testDescriptor, ReportEntry entry) {
        summaryGeneratingListener.reportingEntryPublished(TestIdentifier.from(testDescriptor), entry);
    }

    public void executionStarted() {
        String asciiBanner = "AntuBLUE Test Engine " + TestEngine.VERSION;
        String banner = "Antu" + AnsiColor.BLUE_BOLD_BRIGHT.apply("BLUE") + " Test Engine " + TestEngine.VERSION;

        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < asciiBanner.length(); i++) {
            stringBuilder.append("-");
        }

        String separator = stringBuilder.toString();
        System.out.println(INFO + " " + separator);
        System.out.println(INFO + " " + banner);
        System.out.println(INFO + " " + separator);
    }

    public void executionFinished() {
        long endTimeMilliseconds = System.currentTimeMillis();

        summaryGeneratingListener.testPlanExecutionFinished(testPlan);

        TestExecutionSummary testExecutionSummary = summaryGeneratingListener.getSummary();

        String asciiBanner = "AntuBLUE Test Engine " + TestEngine.VERSION + " Summary";
        String banner = "Antu" + AnsiColor.BLUE_BOLD_BRIGHT.apply("BLUE") + " Test Engine " + TestEngine.VERSION + " Summary";

        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < asciiBanner.length(); i++) {
            stringBuilder.append("-");
        }

        String separator = stringBuilder.toString();

        System.out.println(INFO + " " + separator);
        System.out.println(INFO + " " + banner);
        System.out.println(INFO + " " + separator);
        System.out.println(INFO + " " +
                AnsiColor.WHITE_BOLD_BRIGHT.apply("TESTS")
                        + " : "
                        + (testExecutionSummary.getTestsFoundCount() + testExecutionSummary.getContainersFailedCount())
                        + ", "
                        + AnsiColor.GREEN_BOLD_BRIGHT.apply("PASSED")
                        + " : "
                        + (testExecutionSummary.getTestsSucceededCount() - testExecutionSummary.getContainersFailedCount())
                        + ", "
                        + AnsiColor.RED_BOLD_BRIGHT.apply("FAILED")
                        + " : "
                        + (testExecutionSummary.getTestsFailedCount() + testExecutionSummary.getContainersFailedCount())
                        + ", "
                        + AnsiColor.YELLOW_BOLD_BRIGHT.apply("FAILED")
                        + " : "
                        + testExecutionSummary.getTestsSkippedCount());
        System.out.println(INFO + " " + separator);

        boolean failed = (testExecutionSummary.getTestsFailedCount() + testExecutionSummary.getContainersFailedCount()) > 0;

        if (failed) {
            System.out.println(INFO + " " + AnsiColor.RED_BOLD_BRIGHT.apply("FAILED"));
        } else {
            System.out.println(INFO + " " + AnsiColor.GREEN_BOLD.apply("PASSED"));
        }

        System.out.println(INFO + " " + separator);
        System.out.println(INFO + " " + "Total Time  : " + HumanReadableTime.toHumanReadable(endTimeMilliseconds - startTimeMilliseconds, false));
        System.out.println(INFO + " " + "Finished At : " + HumanReadableTime.now());
        System.out.println(INFO + " " + separator);
    }
}
