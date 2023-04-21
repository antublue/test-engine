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
import org.antublue.test.engine.internal.descriptor.RunnableClassTestDescriptor;
import org.antublue.test.engine.internal.descriptor.RunnableEngineDescriptor;
import org.antublue.test.engine.internal.descriptor.RunnableMethodTestDescriptor;
import org.antublue.test.engine.internal.descriptor.RunnableParameterTestDescriptor;
import org.antublue.test.engine.internal.util.AnsiColor;
import org.antublue.test.engine.internal.util.HumanReadableTime;
import org.antublue.test.engine.internal.util.Switch;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Class to collect test information and output a test execution summary
 */
@SuppressWarnings({"PMD.AvoidDeeplyNestedIfStmts"})
public class TestEngineConsoleTestExecutionListener implements TestExecutionListener {

    private static final String BANNER =
            "Antu" + AnsiColor.BLUE_BOLD_BRIGHT.apply("BLUE") + " Test Engine " + TestEngine.VERSION;

    private static final String INFO =
            AnsiColor.WHITE_BRIGHT.apply("[")
                    + AnsiColor.BLUE_BOLD.apply("INFO")
                    + AnsiColor.WHITE_BRIGHT.apply("]")
                    + " ";

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

    private final Summary summary;

    /**
     * Constructor
     */
    public TestEngineConsoleTestExecutionListener() {
        this.summary = new Summary();

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
     * Method to check if the test execution listener captured any errors
     *
     * @return
     */
    public boolean hasFailures() {
        if (summary.getTestClassCount() == 0) {
            return true;
        }

        return summary.getTestClassesFailedCount()
                + summary.getParametersFailedCount()
                + summary.getTestsFailedCount() > 0;
    }

    /**
     * Method to indicate execution of a TestPlan as started
     *
     * @param testPlan the TestPlan
     */
    @Override
    public void testPlanExecutionStarted(TestPlan testPlan) {
        summary.testPlanExecutionStarted(testPlan);

        testPlan.getRoots()
                .forEach(testIdentifier -> {
                    TestDescriptor testDescriptor =
                            TestEngineTestDescriptorStore
                                    .getInstance()
                                    .get(testIdentifier.getUniqueIdObject())
                                    .orElse(null);

                    if (testDescriptor != null) {
                        TestDescriptorUtils.trace(testDescriptor);
                    }
                });

        System.out.println(INFO + SEPARATOR);
        System.out.println(INFO + BANNER);
        System.out.println(INFO + SEPARATOR);
    }

    /**
     * Method to indicate execution of a TestIdentifier as started
     *
     * @param testIdentifier the TestIdentifier
     */
    @Override
    public void executionStarted(TestIdentifier testIdentifier) {
        TestDescriptor testDescriptor =
                TestEngineTestDescriptorStore
                        .getInstance()
                        .get(testIdentifier.getUniqueIdObject())
                        .orElse(null);

        summary.executionStarted(testDescriptor);

        StringBuilder stringBuilder = new StringBuilder();

        Switch.switchType(
                testDescriptor,
                Switch.switchCase(RunnableEngineDescriptor.class, consumer -> {
                    // DO NOTHING
                }),
                Switch.switchCase(RunnableClassTestDescriptor.class, consumer -> {
                    RunnableClassTestDescriptor classTestDescriptor = (RunnableClassTestDescriptor) testDescriptor;
                    Class<?> testClass = classTestDescriptor.getTestClass();
                    if (logTestMessages) {
                        stringBuilder
                                .append(TEST)
                                .append(" | ")
                                .append(testClass.getName());
                    }
                }),
                Switch.switchCase(RunnableParameterTestDescriptor.class, consumer -> {
                    if (logTestMessages) {
                        RunnableParameterTestDescriptor parameterTestDescriptor = (RunnableParameterTestDescriptor) testDescriptor;
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
                Switch.switchCase(RunnableMethodTestDescriptor.class, consumer -> {
                    if (logTestMessages) {
                        RunnableMethodTestDescriptor methodTestDescriptor = (RunnableMethodTestDescriptor) testDescriptor;
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
            System.out.println(INFO + Thread.currentThread().getName() + " | " + stringBuilder);
        }
    }

    @Override
    public void executionSkipped(TestIdentifier testIdentifier, String reason) {
        TestDescriptor testDescriptor =
                TestEngineTestDescriptorStore
                        .getInstance()
                        .get(testIdentifier.getUniqueIdObject())
                        .orElse(null);

        summary.executionSkipped(testDescriptor, reason);
        // TODO log skipped?
    }

    /**
     * Method to indicate execution of a TestPlan as finished
     *
     * @param testIdentifier the TestIdentifier
     * @param testExecutionResult the TestExecutionResult
     */
    @Override
    public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
        TestDescriptor testDescriptor =
                TestEngineTestDescriptorStore
                        .getInstance()
                        .get(testIdentifier.getUniqueIdObject())
                        .orElse(null);

        summary.executionFinished(testDescriptor, testExecutionResult);

        StringBuilder stringBuilder = new StringBuilder();

        Switch.switchType(
                testDescriptor,
                Switch.switchCase(RunnableEngineDescriptor.class, consumer -> {
                    // DO NOTHING
                }),
                Switch.switchCase(RunnableClassTestDescriptor.class, consumer -> {
                    if (logPassMessages) {
                        RunnableClassTestDescriptor classTestDescriptor = (RunnableClassTestDescriptor) testDescriptor;
                        Class<?> testClass = classTestDescriptor.getTestClass();
                        stringBuilder
                                .append("%s | ")
                                .append(testClass.getName());
                    }
                }),
                Switch.switchCase(RunnableParameterTestDescriptor.class, consumer -> {
                    if (logPassMessages) {
                        RunnableParameterTestDescriptor parameterTestDescriptor = (RunnableParameterTestDescriptor) testDescriptor;
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
                Switch.switchCase(RunnableMethodTestDescriptor.class, consumer -> {
                    if (logPassMessages) {
                        RunnableMethodTestDescriptor methodTestDescriptor = (RunnableMethodTestDescriptor) testDescriptor;
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
                System.out.println(INFO + Thread.currentThread().getName() + " | " + string);
            }
        }
    }

    /**
     * Method to indicate execution of a TestPlan as finished
     *
     * @param testPlan the TestPlan
     */
    @Override
    public void testPlanExecutionFinished(TestPlan testPlan) {
        summary.testPlanExecutionFinished(testPlan);

        System.out.println(INFO + SEPARATOR);
        System.out.println(INFO + BANNER + " Summary");
        System.out.println(INFO + SEPARATOR);

        if (summary.getTestClassCount() != 0) {
            long column1Width =
                    getColumnWith(
                            summary.getTestClassCount(),
                            summary.getParametersFoundCount(),
                            summary.getTestsFoundCount());

            long column2Width =
                    getColumnWith(
                            summary.getTestClassesSucceededCount(),
                            summary.getParametersSucceededCount(),
                            summary.getTestsSucceededCount());

            long column3Width =
                    getColumnWith(
                            summary.getTestClassesFailedCount(),
                            summary.getParametersFailedCount(),
                            summary.getTestsFailedCount());

            long column4Width =
                    getColumnWith(
                            summary.getTestClassesSkippedCount(),
                            summary.getParametersSkippedCount(),
                            summary.getTestsSkippedCount());

            StringBuilder stringBuilder = new StringBuilder();

            stringBuilder
                    .append(INFO)
                    .append(AnsiColor.WHITE_BRIGHT.apply("Test Classes"))
                    .append("    : ")
                    .append(pad(summary.getTestClassCount(), column1Width))
                    .append(", ")
                    .append(AnsiColor.GREEN_BOLD_BRIGHT.apply("PASSED"))
                    .append(" : ")
                    .append(pad(summary.getTestClassesSucceededCount(), column2Width))
                    .append(", ")
                    .append(AnsiColor.RED_BOLD_BRIGHT.apply("FAILED"))
                    .append(" : ")
                    .append(pad(summary.getTestClassesFailedCount(), column3Width))
                    .append(", ")
                    .append(AnsiColor.YELLOW_BOLD_BRIGHT.apply("SKIPPED"))
                    .append(" : ")
                    .append(pad(summary.getTestClassesSkippedCount(), column4Width));

            System.out.println(stringBuilder);

            stringBuilder.setLength(0);

            stringBuilder
                    .append(INFO)
                    .append(AnsiColor.WHITE_BRIGHT.apply("Test Parameters"))
                    .append(" : ")
                    .append(pad(summary.getParametersFoundCount(), column1Width))
                    .append(", ")
                    .append(AnsiColor.GREEN_BOLD_BRIGHT.apply("PASSED"))
                    .append(" : ")
                    .append(pad(summary.getParametersSucceededCount(), column2Width))
                    .append(", ")
                    .append(AnsiColor.RED_BOLD_BRIGHT.apply("FAILED"))
                    .append(" : ")
                    .append(pad(summary.getParametersFailedCount(), column3Width))
                    .append(", ")
                    .append(AnsiColor.YELLOW_BOLD_BRIGHT.apply("SKIPPED"))
                    .append(" : ")
                    .append(pad(summary.getParametersSkippedCount(), column4Width));

            System.out.println(stringBuilder);

            stringBuilder.setLength(0);

            stringBuilder
                    .append(INFO)
                    .append(AnsiColor.WHITE_BRIGHT.apply("Test Methods"))
                    .append("    : ")
                    .append(pad(summary.getTestsFoundCount(), column1Width))
                    .append(", ")
                    .append(AnsiColor.GREEN_BOLD_BRIGHT.apply("PASSED"))
                    .append(" : ")
                    .append(pad(summary.getTestsSucceededCount(), column2Width))
                    .append(", ")
                    .append(AnsiColor.RED_BOLD_BRIGHT.apply("FAILED"))
                    .append(" : ")
                    .append(pad(summary.getTestsFailedCount(), column3Width))
                    .append(", ")
                    .append(AnsiColor.YELLOW_BOLD_BRIGHT.apply("SKIPPED"))
                    .append(" : ")
                    .append(pad(summary.getTestsSkippedCount(), column4Width));

            System.out.println(stringBuilder);
        } else {
            System.out.println(INFO + AnsiColor.RED_BOLD_BRIGHT.apply("NO TESTS FOUND"));
        }

        System.out.println(INFO + SEPARATOR);

        if (hasFailures()) {
            System.out.println(INFO + AnsiColor.RED_BOLD_BRIGHT.apply("FAILED"));
        } else {
            System.out.println(INFO + AnsiColor.GREEN_BOLD.apply("PASSED"));
        }

        long elapsedTime = summary.getTimeFinished() - summary.getTimeStarted();

        System.out.println(INFO + SEPARATOR);

        System.out.println(
                INFO
                + "Total Test Time : "
                + HumanReadableTime.toHumanReadable(elapsedTime, false));

        System.out.println(INFO + "Finished At     : " + HumanReadableTime.now());

        if (!hasFailures()) {
            System.out.println(INFO + SEPARATOR);
        }
    }

    /**
     * Method to column width of long values as Strings
     *
     * @param values
     * @return
     */
    private long getColumnWith(long ... values) {
        long width = 0;

        for (long value : values) {
            width = Math.max(String.valueOf(value).length(), width);
        }

        return width;
    }

    /**
     * Method to get a String that is the value passed to a specific width
     *
     * @param value
     * @param width
     * @return
     */
    private String pad(long value, long width) {
        String stringValue = String.valueOf(value);

        StringBuilder paddingStringBuilder = new StringBuilder();
        while ((paddingStringBuilder.length() + stringValue.length()) < width) {
            paddingStringBuilder.append(" ");
        }

        return paddingStringBuilder.append(stringValue).toString();
    }

    private static class Summary {

        private long startMilliseconds;
        private long finishedMilliseconds;

        private final Set<Class<?>> testClasses;
        private final AtomicLong testClassesFound;
        private final AtomicLong testClassesSuccess;
        private final AtomicLong testClassesFailed;
        private final AtomicLong testClassesSkipped;

        private final AtomicLong parametersFound;
        private final AtomicLong parametersSuccess;
        private final AtomicLong parametersFailed;
        private final AtomicLong parametersSkipped;

        private final AtomicLong methodsFound;
        private final AtomicLong methodsSuccess;
        private final AtomicLong methodsFailed;
        private final AtomicLong methodsSkipped;

        public Summary() {
            testClasses = Collections.synchronizedSet(new HashSet<>());

            testClassesFound = new AtomicLong();
            testClassesSuccess = new AtomicLong();
            testClassesFailed = new AtomicLong();
            testClassesSkipped = new AtomicLong();

            parametersFound = new AtomicLong();
            parametersSuccess = new AtomicLong();
            parametersFailed = new AtomicLong();
            parametersSkipped = new AtomicLong();

            methodsFound = new AtomicLong();
            methodsSuccess = new AtomicLong();
            methodsFailed = new AtomicLong();
            methodsSkipped = new AtomicLong();
        }

        public long getTimeStarted() {
            return startMilliseconds;
        }

        public long getTestClassCount() {
            return testClassesFound.get();
        }

        public long getTestClassesSucceededCount() {
            return testClassesSuccess.get();
        }

        public long getTestClassesFailedCount() {
            return testClassesFailed.get();
        }

        public long getTestClassesSkippedCount() {
            return testClassesSkipped.get();
        }

        public long getParametersFoundCount() {
            return parametersFound.get();
        }

        public long getParametersSucceededCount() {
            return parametersSuccess.get();
        }

        public long getParametersFailedCount() {
            return parametersFailed.get();
        }

        public long getParametersSkippedCount() {
            return parametersSkipped.get();
        }

        public long getTestsFoundCount() {
            return methodsFound.get();
        }

        public long getTestsSucceededCount() {
            return methodsSuccess.get();
        }

        public long getTestsFailedCount() {
            return methodsFailed.get();
        }

        public long getTestsSkippedCount() {
            return methodsSkipped.get();
        }

        public long getTimeFinished() {
            return finishedMilliseconds;
        }

        public void testPlanExecutionStarted(TestPlan testPlan) {
            startMilliseconds = System.currentTimeMillis();
            finishedMilliseconds = startMilliseconds;
        }

        public void executionStarted(TestDescriptor testDescriptor) {
            if (testDescriptor instanceof RunnableClassTestDescriptor) {
                testClasses.add(((RunnableClassTestDescriptor) testDescriptor).getTestClass());
                testClassesFound.set(testClasses.size());
                return;
            }

            if (testDescriptor instanceof RunnableParameterTestDescriptor) {
                parametersFound.incrementAndGet();
                return;
            }

            if (testDescriptor instanceof RunnableMethodTestDescriptor) {
                methodsFound.incrementAndGet();
            }
        }

        public void executionSkipped(TestDescriptor testDescriptor, String reason) {
            if (testDescriptor instanceof RunnableParameterTestDescriptor) {
                parametersFound.incrementAndGet();
                parametersSkipped.incrementAndGet();
                return;
            }

            if (testDescriptor instanceof RunnableMethodTestDescriptor) {
                methodsFound.incrementAndGet();
                methodsSkipped.incrementAndGet();
            }
        }

        public void executionFinished(TestDescriptor testDescriptor, TestExecutionResult testExecutionResult) {
            if (testDescriptor instanceof RunnableClassTestDescriptor) {
                TestExecutionResult.Status status = testExecutionResult.getStatus();
                switch (status) {
                    case SUCCESSFUL: {
                        testClassesSuccess.incrementAndGet();
                        break;
                    }
                    case FAILED: {
                        testClassesFailed.incrementAndGet();
                        break;
                    }
                    case ABORTED: {
                        testClassesSkipped.incrementAndGet();
                        break;
                    }
                }

                return;
            }

            if (testDescriptor instanceof RunnableParameterTestDescriptor) {
                TestExecutionResult.Status status = testExecutionResult.getStatus();
                switch (status) {
                    case SUCCESSFUL: {
                        parametersSuccess.incrementAndGet();
                        break;
                    }
                    case FAILED: {
                        parametersFailed.incrementAndGet();
                        break;
                    }
                    case ABORTED: {
                        parametersSkipped.incrementAndGet();
                        break;
                    }
                }
            }

            if (testDescriptor instanceof RunnableMethodTestDescriptor) {
                TestExecutionResult.Status status = testExecutionResult.getStatus();
                switch (status) {
                    case SUCCESSFUL: {
                        methodsSuccess.incrementAndGet();
                        break;
                    }
                    case FAILED: {
                        methodsFailed.incrementAndGet();
                        break;
                    }
                    case ABORTED: {
                        methodsSkipped.incrementAndGet();
                        break;
                    }
                }
            }
        }

        public void testPlanExecutionFinished(TestPlan testPlan) {
            finishedMilliseconds = System.currentTimeMillis();
        }
    }
}
