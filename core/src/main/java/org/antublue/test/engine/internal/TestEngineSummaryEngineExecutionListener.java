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
import org.antublue.test.engine.internal.descriptor.TestEngineClassTestDescriptor;
import org.antublue.test.engine.internal.descriptor.TestEngineParameterTestDescriptor;
import org.antublue.test.engine.internal.descriptor.TestEngineTestMethodTestDescriptor;
import org.antublue.test.engine.internal.logger.Logger;
import org.antublue.test.engine.internal.logger.LoggerFactory;
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

public class TestEngineSummaryEngineExecutionListener implements EngineExecutionListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestEngine.class);

    private static final String TEST = "TEST";
    private static final String ABORT = "ABORT";
    private static final String FAIL = "FAIL";
    private static final String PASS = "PASS";

    private final TestPlan testPlan;
    private final SummaryGeneratingListener summaryGeneratingListener;
    private final boolean detailedOutput;

    public TestEngineSummaryEngineExecutionListener(TestPlan testPlan) {
        this.testPlan = testPlan;
        this.summaryGeneratingListener = new SummaryGeneratingListener();

        this.summaryGeneratingListener.testPlanExecutionStarted(testPlan);

        this.detailedOutput =
                TestEngineConfigurationParameters.getInstance()
                        .get(TestEngineConstants.CONSOLE_OUTPUT)
                        .map(value -> {
                            try {
                                return Boolean.parseBoolean(value);
                            } catch (NumberFormatException e) {
                                return false;
                            }
                        })
                        .orElse(false);
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
        if (testDescriptor instanceof TestEngineTestMethodTestDescriptor) {
            summaryGeneratingListener.executionStarted(TestIdentifier.from(testDescriptor));
        }

        final StringBuilder stringBuilder = new StringBuilder();

        Switch.switchType(
                testDescriptor,
                Switch.switchCase(EngineDescriptor.class, consumer -> {}),
                Switch.switchCase(TestEngineClassTestDescriptor.class, consumer -> {}),
                Switch.switchCase(TestEngineParameterTestDescriptor.class, consumer -> {
                    TestEngineParameterTestDescriptor testEngineParameterTestDescriptor = (TestEngineParameterTestDescriptor) testDescriptor;
                    Class<?> testClass = testEngineParameterTestDescriptor.getTestClass();
                    Parameter testParameter = testEngineParameterTestDescriptor.getTestParameter();
                    String testParameterName = testParameter.name();
                    stringBuilder
                            .append(TEST)
                            .append(" | ")
                            .append(testParameterName)
                            .append(" | ")
                            .append(testClass.getName());
                }),
                Switch.switchCase(TestEngineTestMethodTestDescriptor.class, consumer -> {
                    TestEngineTestMethodTestDescriptor testEngineTestMethodTestDescriptor = (TestEngineTestMethodTestDescriptor) testDescriptor;
                    Class<?> testClass = testEngineTestMethodTestDescriptor.getTestClass();
                    Method testMethod = testEngineTestMethodTestDescriptor.getTestMethod();
                    Parameter testParameter = testEngineTestMethodTestDescriptor.getTestParameter();
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
                })
        );

        if (detailedOutput && (stringBuilder.length() > 0)) {
            LOGGER.rawInfo(stringBuilder.toString());
        }
    }

    @Override
    public void executionFinished(TestDescriptor testDescriptor, TestExecutionResult testExecutionResult) {
        if (testDescriptor instanceof TestEngineTestMethodTestDescriptor) {
            summaryGeneratingListener.executionFinished(TestIdentifier.from(testDescriptor), testExecutionResult);
        }

        final StringBuilder stringBuilder = new StringBuilder();

        Switch.switchType(
                testDescriptor,
                Switch.switchCase(EngineDescriptor.class, consumer -> {}),
                Switch.switchCase(TestEngineClassTestDescriptor.class, consumer -> {}),
                Switch.switchCase(TestEngineParameterTestDescriptor.class, consumer -> {
                    TestEngineParameterTestDescriptor testengineParameterTestDescriptor = (TestEngineParameterTestDescriptor) testDescriptor;
                    Class<?> testClass = testengineParameterTestDescriptor.getTestClass();
                    Parameter testParameter = testengineParameterTestDescriptor.getTestParameter();
                    String testParameterName = testParameter.name();
                    stringBuilder
                            .append("%s | ")
                            .append(testParameterName)
                            .append(" | ")
                            .append(testClass.getName());
                }),
                Switch.switchCase(TestEngineTestMethodTestDescriptor.class, consumer -> {
                    TestEngineTestMethodTestDescriptor testEngineTestMethodTestDescriptor = (TestEngineTestMethodTestDescriptor) testDescriptor;
                    Class<?> testClass = testEngineTestMethodTestDescriptor.getTestClass();
                    Method testMethod = testEngineTestMethodTestDescriptor.getTestMethod();
                    Parameter testParameter = testEngineTestMethodTestDescriptor.getTestParameter();
                    String testParameterName = testParameter.name();
                    stringBuilder
                            .append("%s | ")
                            .append(testParameterName)
                            .append(" | ")
                            .append(testClass.getName())
                            .append(" ")
                            .append(testMethod.getName())
                            .append("()");
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
                LOGGER.rawInfo(string);
            }
        }
    }

    @Override
    public void reportingEntryPublished(TestDescriptor testDescriptor, ReportEntry entry) {
        summaryGeneratingListener.reportingEntryPublished(TestIdentifier.from(testDescriptor), entry);
    }

    public TestExecutionSummary getSummary() {
        summaryGeneratingListener.testPlanExecutionFinished(testPlan);
        return summaryGeneratingListener.getSummary();
    }
}
