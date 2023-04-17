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
import org.antublue.test.engine.internal.util.AnsiColor;
import org.antublue.test.engine.internal.util.HumanReadableTime;
import org.antublue.test.engine.internal.util.Switch;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.support.descriptor.EngineDescriptor;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Class to collect metrics and output test execution status
 */
@SuppressWarnings("PMD.AvoidDeeplyNestedIfStmts")
public class TestEngineTestExecutionListener extends SummaryGeneratingListener {

    private static String BANNER =
            "Antu" + AnsiColor.BLUE_BOLD_BRIGHT.apply("BLUE") + " Test Engine " + TestEngine.VERSION;

    private static final String INFO =
            AnsiColor.WHITE_BRIGHT.apply("[")
            + AnsiColor.BLUE_BOLD.apply("INFO")
            + AnsiColor.WHITE_BRIGHT.apply("]");

    private static final String TEST = AnsiColor.WHITE_BRIGHT.apply("TEST");
    private static final String ABORT = AnsiColor.YELLOW_BOLD.apply("ABORT");
    private static final String FAIL = AnsiColor.RED_BOLD.apply("FAIL");
    private static final String PASS = AnsiColor.GREEN_BOLD.apply("PASS");

    private static final String SEPARATOR =
            AnsiColor.WHITE_BRIGHT.apply(
                    "------------------------------------------------------------------------");

    private final boolean detailedOutput;
    private final boolean logTestMessages;
    private final boolean logPassMessages;
    private long startTimeMilliseconds;
    private final Set<Class<?>> testClasses;

    /**
     * Constructor
     */
    public TestEngineTestExecutionListener() {
        this.testClasses = Collections.synchronizedSet(new HashSet());

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

    /**
     * Method to indicate execution of a TestPlan as started
     *
     * @param testPlan
     */
    @Override
    public void testPlanExecutionStarted(TestPlan testPlan) {
        super.testPlanExecutionStarted(testPlan);

        startTimeMilliseconds = System.currentTimeMillis();

        System.out.println(INFO + " " + SEPARATOR);
        System.out.println(INFO + " " + BANNER);
        System.out.println(INFO + " " + SEPARATOR);
    }

    /**
     * Method to indicate execution of a TestIdentifier as started
     *
     * @param testIdentifier
     */
    @Override
    public void executionStarted(TestIdentifier testIdentifier) {
        super.executionStarted(testIdentifier);

        TestDescriptor testDescriptor =
                TestEngineTestDescriptorStore
                        .getInstance()
                        .get(testIdentifier.getUniqueIdObject())
                        .orElse(null);

        if (testDescriptor != null) {
            final StringBuilder stringBuilder = new StringBuilder();

            Switch.switchType(
                    testDescriptor,
                    Switch.switchCase(EngineDescriptor.class, consumer -> {
                    }),
                    Switch.switchCase(ClassTestDescriptor.class, consumer -> {
                        ClassTestDescriptor classTestDescriptor = (ClassTestDescriptor) testDescriptor;
                        Class<?> testClass = classTestDescriptor.getTestClass();
                        testClasses.add(testClass);
                        if (logTestMessages) {
                            stringBuilder
                                    .append(TEST)
                                    .append(" | ")
                                    .append(testClass.getName());
                        }
                    }),
                    Switch.switchCase(ParameterTestDescriptor.class, consumer -> {
                        if (logTestMessages) {
                            ParameterTestDescriptor parameterTestDescriptor = (ParameterTestDescriptor) testDescriptor;
                            Class<?> testClass = parameterTestDescriptor.getTestClass();
                            Parameter testParameter = parameterTestDescriptor.getTestParameter();
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

            if (detailedOutput && stringBuilder.length() > 0) {
                //LOGGER.rawInfo(stringBuilder.toString());
                System.out.println(INFO + " " + Thread.currentThread().getName() + " | " + stringBuilder);
            }
        }
    }

    /**
     * Method to indicate execution of a TestPlan as finished
     *
     * @param testIdentifier
     * @param testExecutionResult
     */
    @Override
    public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
        super.executionFinished(testIdentifier, testExecutionResult);

        TestDescriptor testDescriptor =
                TestEngineTestDescriptorStore
                        .getInstance()
                        .get(testIdentifier.getUniqueIdObject())
                        .orElse(null);

        if (testDescriptor != null) {
            final StringBuilder stringBuilder = new StringBuilder();

            Switch.switchType(
                    testDescriptor,
                    Switch.switchCase(EngineDescriptor.class, consumer -> {
                    }),
                    Switch.switchCase(ClassTestDescriptor.class, consumer -> {
                        if (logPassMessages) {
                            ClassTestDescriptor classTestDescriptor = (ClassTestDescriptor) testDescriptor;
                            Class<?> testClass = classTestDescriptor.getTestClass();
                            stringBuilder
                                    .append("%s | ")
                                    .append(testClass.getName());
                        }
                    }),
                    Switch.switchCase(ParameterTestDescriptor.class, consumer -> {
                        if (logPassMessages) {
                            ParameterTestDescriptor parameterTestDescriptor = (ParameterTestDescriptor) testDescriptor;
                            Class<?> testClass = parameterTestDescriptor.getTestClass();
                            Parameter testParameter = parameterTestDescriptor.getTestParameter();
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

                if (detailedOutput && string != null) {
                    System.out.println(INFO + " " + Thread.currentThread().getName() + " | " + string);
                }
            }
        }
    }

    /**
     * Method to indicate execution of a TestPlan as finished
     *
     * @param testPlan
     */
    @Override
    public void testPlanExecutionFinished(TestPlan testPlan) {
        super.testPlanExecutionFinished(testPlan);

        long endTimeMilliseconds = System.currentTimeMillis();
        TestExecutionSummary testExecutionSummary = getSummary();

        System.out.println(INFO);
        System.out.println(INFO + " " + SEPARATOR);
        System.out.println(INFO + " " + BANNER + " Summary");
        System.out.println(INFO + " " + SEPARATOR);
        System.out.println(
                INFO
                + " "
                + AnsiColor.WHITE_BRIGHT.apply("Test Classes")
                + "    : "
                + testClasses.size());

        System.out.println(
                INFO
                + " "
                +  AnsiColor.WHITE_BRIGHT.apply("Test Executions")
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
                + AnsiColor.YELLOW_BOLD_BRIGHT.apply("SKIPPED")
                + " : "
                + testExecutionSummary.getTestsSkippedCount());
        System.out.println(INFO + " " + SEPARATOR);

        boolean failed =
                testClasses.size() == 0
                || (testExecutionSummary.getTestsFailedCount() + testExecutionSummary.getContainersFailedCount()) > 0;

        if (failed) {
            System.out.println(INFO + " " + AnsiColor.RED_BOLD_BRIGHT.apply("FAILED"));
        } else {
            System.out.println(INFO + " " + AnsiColor.GREEN_BOLD.apply("PASSED"));
        }

        System.out.println(INFO + " " + SEPARATOR);
        System.out.println(INFO + " " + "Total Test Time : " + HumanReadableTime.toHumanReadable(endTimeMilliseconds - startTimeMilliseconds, false));
        System.out.println(INFO + " " + "Finished At     : " + HumanReadableTime.now());
        System.out.println(INFO + " " + SEPARATOR);
    }
}
