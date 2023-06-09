/*
 * Copyright (C) 2022-2023 The AntuBLUE test-engine project authors
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
import org.antublue.test.engine.api.Argument;
import org.antublue.test.engine.internal.descriptor.ArgumentTestDescriptor;
import org.antublue.test.engine.internal.descriptor.ClassTestDescriptor;
import org.antublue.test.engine.internal.descriptor.ExtendedEngineDescriptor;
import org.antublue.test.engine.internal.descriptor.MethodTestDescriptor;
import org.antublue.test.engine.internal.descriptor.TestDescriptorUtils;
import org.antublue.test.engine.internal.util.AnsiColor;
import org.antublue.test.engine.internal.util.AnsiColorString;
import org.antublue.test.engine.internal.util.Cast;
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
public class TestEngineConsoleTestExecutionListener implements TestExecutionListener {

    private static final String BANNER =
            new AnsiColorString()
                    .color(AnsiColor.WHITE_BRIGHT)
                    .append("Antu")
                    .color(AnsiColor.BLUE_BOLD_BRIGHT)
                    .append("BLUE")
                    .color(AnsiColor.WHITE_BRIGHT)
                    .append(" Test Engine ")
                    .append(TestEngine.VERSION)
                    .toString();

    private static final String SUMMARY_BANNER = BANNER + AnsiColor.WHITE_BRIGHT.apply(" Summary");

    private static final String INFO =
            new AnsiColorString()
                    .color(AnsiColor.WHITE_BRIGHT)
                    .append("[")
                    .color(AnsiColor.BLUE_BOLD)
                    .append("INFO")
                    .color(AnsiColor.WHITE_BRIGHT)
                    .append("] ")
                    .toString();

    private static final String TEST = AnsiColor.WHITE_BRIGHT.apply("TEST");
    private static final String PASS = AnsiColor.GREEN_BOLD_BRIGHT.apply("PASS");
    private static final String SKIP = AnsiColor.YELLOW_BOLD_BRIGHT.apply("SKIP");
    private static final String FAIL = AnsiColor.RED_BOLD_BRIGHT.apply("FAIL");

    private static final String SEPARATOR =
            AnsiColor.WHITE_BRIGHT.apply(
                    "------------------------------------------------------------------------");


    private final Summary summary;
    private final boolean detailedOutput;
    private final boolean logTestMessages;
    private final boolean logPassMessages;
    private final boolean logSkipMessages;

    /**
     * Constructor
     */
    public TestEngineConsoleTestExecutionListener() {
        summary = new Summary();

        detailedOutput =
                TestEngineConfiguration.getInstance()
                        .get(TestEngineConstants.CONSOLE_OUTPUT)
                        .map(value -> {
                            try {
                                return Boolean.parseBoolean(value);
                            } catch (NumberFormatException e) {
                                return true;
                            }
                        })
                        .orElse(true);

        logTestMessages =
                TestEngineConfiguration.getInstance()
                        .get(TestEngineConstants.LOG_TEST_MESSAGES)
                        .map(value -> {
                            try {
                                return Boolean.parseBoolean(value);
                            } catch (NumberFormatException e) {
                                return true;
                            }
                        })
                        .orElse(true);

        logPassMessages =
                TestEngineConfiguration.getInstance()
                        .get(TestEngineConstants.LOG_PASS_MESSAGES)
                        .map(value -> {
                            try {
                                return Boolean.parseBoolean(value);
                            } catch (NumberFormatException e) {
                                return true;
                            }
                        })
                        .orElse(true);

        logSkipMessages =
                TestEngineConfiguration.getInstance()
                        .get(TestEngineConstants.LOG_SKIP_MESSAGES)
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
     * @return the return value
     */
    public boolean hasFailures() {
        if (summary.getTestClassCount() == 0) {
            return true;
        }

        return summary.getTestClassesFailedCount()
                + summary.getArgumentsFailedCount()
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
                .forEach(testIdentifier ->
                        TestEngineTestDescriptorStore
                                .getInstance()
                                .get(testIdentifier.getUniqueIdObject()).ifPresent(TestDescriptorUtils::logTestDescriptorTree));

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
                Switch.switchCase(ExtendedEngineDescriptor.class, consumer -> {
                    // DO NOTHING
                }),
                Switch.switchCase(ClassTestDescriptor.class, consumer -> {
                    ClassTestDescriptor classTestDescriptor = Cast.cast(testDescriptor);
                    Class<?> testClass = classTestDescriptor.getTestClass();
                    if (logTestMessages) {
                        stringBuilder
                                .append(TEST)
                                .append(" | ")
                                .append(testClass.getName());
                    }
                }),
                Switch.switchCase(ArgumentTestDescriptor.class, consumer -> {
                    if (logTestMessages) {
                        ArgumentTestDescriptor argumentTestDescriptor = Cast.cast(testDescriptor);
                        Class<?> testClass = argumentTestDescriptor.getTestClass();
                        Argument testArgument = argumentTestDescriptor.getTestArgument();
                        String testArgumentName = testArgument.name();
                        stringBuilder
                                .append(TEST)
                                .append(" | ")
                                .append(testArgumentName)
                                .append(" | ")
                                .append(testClass.getName());
                    }
                }),
                Switch.switchCase(MethodTestDescriptor.class, consumer -> {
                    if (logTestMessages) {
                        MethodTestDescriptor methodTestDescriptor = Cast.cast(testDescriptor);
                        Class<?> testClass = methodTestDescriptor.getTestClass();
                        Method testMethod = methodTestDescriptor.getTestMethod();
                        Argument testArgument = methodTestDescriptor.getTestArgument();
                        String testArgumentName = testArgument.name();
                        stringBuilder
                                .append(TEST)
                                .append(" | ")
                                .append(testArgumentName)
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

        StringBuilder stringBuilder = new StringBuilder();

        Switch.switchType(
                testDescriptor,
                Switch.switchCase(ExtendedEngineDescriptor.class, consumer -> {
                    // DO NOTHING
                }),
                Switch.switchCase(ClassTestDescriptor.class, consumer -> {
                    if (logSkipMessages) {
                        ClassTestDescriptor classTestDescriptor = Cast.cast(testDescriptor);
                        Class<?> testClass = classTestDescriptor.getTestClass();
                        stringBuilder
                                .append(SKIP)
                                .append(" | ")
                                .append(testClass.getName());
                    }
                }),
                Switch.switchCase(ArgumentTestDescriptor.class, consumer -> {
                    if (logSkipMessages) {
                        ArgumentTestDescriptor argumentTestDescriptor = Cast.cast(testDescriptor);
                        Class<?> testClass = argumentTestDescriptor.getTestClass();
                        Argument testArgument = argumentTestDescriptor.getTestArgument();
                        String testArgumentName = testArgument.name();
                        stringBuilder
                                .append(SKIP)
                                .append(" | ")
                                .append(testArgumentName)
                                .append(" | ")
                                .append(testClass.getName());
                    }
                }),
                Switch.switchCase(MethodTestDescriptor.class, consumer -> {
                    if (logSkipMessages) {
                        MethodTestDescriptor methodTestDescriptor = Cast.cast(testDescriptor);
                        Class<?> testClass = methodTestDescriptor.getTestClass();
                        Method testMethod = methodTestDescriptor.getTestMethod();
                        Argument testArgument = methodTestDescriptor.getTestArgument();
                        String testArgumentName = testArgument.name();
                        stringBuilder
                                .append(SKIP)
                                .append(" | ")
                                .append(testArgumentName)
                                .append(" | ")
                                .append(testClass.getName())
                                .append(" ")
                                .append(testMethod.getName())
                                .append("()");
                    }
                }));

        if (detailedOutput && stringBuilder.length() > 0) {
            System.out.println(INFO + Thread.currentThread().getName() + " | " + stringBuilder);
        }
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
                Switch.switchCase(ExtendedEngineDescriptor.class, consumer -> {
                    // DO NOTHING
                }),
                Switch.switchCase(ClassTestDescriptor.class, consumer -> {
                    if (logPassMessages) {
                        ClassTestDescriptor classTestDescriptor = Cast.cast(testDescriptor);
                        Class<?> testClass = classTestDescriptor.getTestClass();
                        stringBuilder
                                .append("%s | ")
                                .append(testClass.getName());
                    }
                }),
                Switch.switchCase(ArgumentTestDescriptor.class, consumer -> {
                    if (logPassMessages) {
                        ArgumentTestDescriptor argumentTestDescriptor = Cast.cast(testDescriptor);
                        Class<?> testClass = argumentTestDescriptor.getTestClass();
                        Argument testArgument = argumentTestDescriptor.getTestArgument();
                        String testArgumentName = testArgument.name();
                        stringBuilder
                                .append("%s | ")
                                .append(testArgumentName)
                                .append(" | ")
                                .append(testClass.getName());
                    }
                }),
                Switch.switchCase(MethodTestDescriptor.class, consumer -> {
                    if (logPassMessages) {
                        MethodTestDescriptor methodTestDescriptor = Cast.cast(testDescriptor);
                        Class<?> testClass = methodTestDescriptor.getTestClass();
                        Method testMethod = methodTestDescriptor.getTestMethod();
                        Argument testArgument = methodTestDescriptor.getTestArgument();
                        String testArgumentName = testArgument.name();
                        stringBuilder
                                .append("%s | ")
                                .append(testArgumentName)
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
                    string = String.format(stringBuilder.toString(), SKIP);
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
        System.out.println(INFO + SUMMARY_BANNER);
        System.out.println(INFO + SEPARATOR);

        if (summary.getTestClassCount() != 0) {
            long column1Width =
                    getColumnWith(
                            summary.getTestClassCount(),
                            summary.getArgumentsFoundCount(),
                            summary.getTestsFoundCount());

            long column2Width =
                    getColumnWith(
                            summary.getTestClassesSucceededCount(),
                            summary.getArgumentsSucceededCount(),
                            summary.getTestsSucceededCount());

            long column3Width =
                    getColumnWith(
                            summary.getTestClassesFailedCount(),
                            summary.getArgumentsFailedCount(),
                            summary.getTestsFailedCount());

            long column4Width =
                    getColumnWith(
                            summary.getTestClassesSkippedCount(),
                            summary.getArgumentsSkippedCount(),
                            summary.getTestsSkippedCount());

            AnsiColorString ansiColorString = new AnsiColorString();

            ansiColorString
                    .append(INFO)
                    .color(AnsiColor.WHITE_BRIGHT)
                    .append("Test Classes")
                    .append(" : ")
                    .append(pad(summary.getTestClassCount(), column1Width))
                    .append(", ")
                    .color(AnsiColor.GREEN_BOLD_BRIGHT)
                    .append("PASSED")
                    .color(AnsiColor.WHITE_BRIGHT)
                    .append(" : ")
                    .append(pad(summary.getTestClassesSucceededCount(), column2Width))
                    .append(", ")
                    .color(AnsiColor.RED_BOLD_BRIGHT)
                    .append("FAILED")
                    .color(AnsiColor.WHITE_BRIGHT)
                    .append(" : ")
                    .append(pad(summary.getTestClassesFailedCount(), column3Width))
                    .append(", ")
                    .color(AnsiColor.YELLOW_BOLD_BRIGHT)
                    .append("SKIPPED")
                    .color(AnsiColor.WHITE_BRIGHT)
                    .append(" : ")
                    .append(pad(summary.getTestClassesSkippedCount(), column4Width));

            System.out.println(ansiColorString);

            ansiColorString
                    .append(INFO)
                    .color(AnsiColor.WHITE_BRIGHT)
                    .append("Test Methods")
                    .append(" : ")
                    .append(pad(summary.getTestsFoundCount(), column1Width))
                    .append(", ")
                    .color(AnsiColor.GREEN_BOLD_BRIGHT)
                    .append("PASSED")
                    .color(AnsiColor.WHITE_BRIGHT)
                    .append(" : ")
                    .append(pad(summary.getTestsSucceededCount(), column2Width))
                    .append(", ")
                    .color(AnsiColor.RED_BOLD_BRIGHT)
                    .append("FAILED")
                    .color(AnsiColor.WHITE_BRIGHT)
                    .append(" : ")
                    .append(pad(summary.getTestsFailedCount(), column3Width))
                    .append(", ")
                    .color(AnsiColor.YELLOW_BOLD_BRIGHT)
                    .append("SKIPPED")
                    .color(AnsiColor.WHITE_BRIGHT)
                    .append(" : ")
                    .append(pad(summary.getTestsSkippedCount(), column4Width));

            System.out.println(ansiColorString);
        } else {
            System.out.println(INFO + AnsiColor.RED_BOLD_BRIGHT.apply("NO TESTS FOUND"));
        }

        System.out.println(INFO + SEPARATOR);

        if (hasFailures()) {
            System.out.println(INFO + AnsiColor.RED_BOLD_BRIGHT.apply("FAILED"));
        } else {
            System.out.println(INFO + AnsiColor.GREEN_BOLD_BRIGHT.apply("PASSED"));
        }

        long elapsedTime = summary.getTimeFinished() - summary.getTimeStarted();

        System.out.println(INFO + SEPARATOR);

        System.out.println(
                INFO
                + AnsiColor.WHITE_BRIGHT.apply(
                        "Total Test Time : "
                                + HumanReadableTime.toHumanReadable(elapsedTime, false)
                                + " (" + elapsedTime + " ms)"));

        System.out.println(INFO + AnsiColor.WHITE_BRIGHT.apply("Finished At     : " + HumanReadableTime.now()));

        if (!hasFailures()) {
            System.out.println(INFO + SEPARATOR);
        }
    }

    /**
     * Method to column width of long values as Strings
     *
     * @param values values
     * @return the return value
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
     * @param value value
     * @param width width
     * @return the return value
     */
    private String pad(long value, long width) {
        String stringValue = String.valueOf(value);

        StringBuilder paddingStringBuilder = new StringBuilder();
        while ((paddingStringBuilder.length() + stringValue.length()) < width) {
            paddingStringBuilder.append(" ");
        }

        return paddingStringBuilder.append(stringValue).toString();
    }

    /**
     * Class to implement test summary metrics
     */
    private static class Summary {

        private long startMilliseconds;
        private long finishedMilliseconds;

        private final Set<Class<?>> testClasses;
        private final AtomicLong testClassesFound;
        private final AtomicLong testClassesSuccess;
        private final AtomicLong testClassesFailed;
        private final AtomicLong testClassesSkipped;

        private final AtomicLong argumentsFound;
        private final AtomicLong argumentsSuccess;
        private final AtomicLong argumentsFailed;
        private final AtomicLong argumentsSkipped;

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

            argumentsFound = new AtomicLong();
            argumentsSuccess = new AtomicLong();
            argumentsFailed = new AtomicLong();
            argumentsSkipped = new AtomicLong();

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

        public long getArgumentsFoundCount() {
            return argumentsFound.get();
        }

        public long getArgumentsSucceededCount() {
            return argumentsSuccess.get();
        }

        public long getArgumentsFailedCount() {
            return argumentsFailed.get();
        }

        public long getArgumentsSkippedCount() {
            return argumentsSkipped.get();
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
            if (testDescriptor instanceof ClassTestDescriptor) {
                testClasses.add(((ClassTestDescriptor) testDescriptor).getTestClass());
                testClassesFound.set(testClasses.size());
                return;
            }

            if (testDescriptor instanceof ArgumentTestDescriptor) {
                argumentsFound.incrementAndGet();
                return;
            }

            if (testDescriptor instanceof MethodTestDescriptor) {
                methodsFound.incrementAndGet();
            }
        }

        public void executionSkipped(TestDescriptor testDescriptor, String reason) {
            if (testDescriptor instanceof ArgumentTestDescriptor) {
                argumentsFound.incrementAndGet();
                argumentsSkipped.incrementAndGet();
                return;
            }

            if (testDescriptor instanceof MethodTestDescriptor) {
                methodsFound.incrementAndGet();
                methodsSkipped.incrementAndGet();
            }
        }

        public void executionFinished(TestDescriptor testDescriptor, TestExecutionResult testExecutionResult) {
            if (testDescriptor instanceof ClassTestDescriptor) {
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

            if (testDescriptor instanceof ArgumentTestDescriptor) {
                TestExecutionResult.Status status = testExecutionResult.getStatus();
                switch (status) {
                    case SUCCESSFUL: {
                        argumentsSuccess.incrementAndGet();
                        break;
                    }
                    case FAILED: {
                        argumentsFailed.incrementAndGet();
                        break;
                    }
                    case ABORTED: {
                        argumentsSkipped.incrementAndGet();
                        break;
                    }
                }
            }

            if (testDescriptor instanceof MethodTestDescriptor) {
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
