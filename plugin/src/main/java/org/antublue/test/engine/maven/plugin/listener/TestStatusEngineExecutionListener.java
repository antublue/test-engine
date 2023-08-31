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

import java.lang.reflect.Method;
import org.antublue.test.engine.Configuration;
import org.antublue.test.engine.Constants;
import org.antublue.test.engine.api.Argument;
import org.antublue.test.engine.descriptor.ArgumentTestDescriptor;
import org.antublue.test.engine.descriptor.ClassTestDescriptor;
import org.antublue.test.engine.descriptor.ExtendedAbstractTestDescriptor;
import org.antublue.test.engine.descriptor.MethodTestDescriptor;
import org.antublue.test.engine.logger.Logger;
import org.antublue.test.engine.logger.LoggerFactory;
import org.antublue.test.engine.util.AnsiColor;
import org.antublue.test.engine.util.AnsiColorStringBuilder;
import org.antublue.test.engine.util.NanosecondsConverter;
import org.antublue.test.engine.util.StopWatch;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.support.descriptor.EngineDescriptor;

/** Class to implement a TestStatusEngineExecutionListener */
public class TestStatusEngineExecutionListener implements EngineExecutionListener {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(TestStatusEngineExecutionListener.class);

    private static final String INFO =
            new AnsiColorStringBuilder()
                    .color(AnsiColor.WHITE)
                    .append("[")
                    .color(AnsiColor.BLUE_BOLD)
                    .append("INFO")
                    .color(AnsiColor.WHITE)
                    .append("]")
                    .color(AnsiColor.TEXT_RESET)
                    .toString();

    private static final String PASS =
            new AnsiColorStringBuilder()
                    .color(AnsiColor.GREEN_BOLD_BRIGHT)
                    .append("PASS")
                    .color(AnsiColor.TEXT_RESET)
                    .toString();

    private static final String FAIL =
            new AnsiColorStringBuilder()
                    .color(AnsiColor.RED_BOLD_BRIGHT)
                    .append("FAIL")
                    .color(AnsiColor.TEXT_RESET)
                    .toString();

    private static final String SKIP =
            new AnsiColorStringBuilder()
                    .color(AnsiColor.YELLOW_BOLD_BRIGHT)
                    .append("SKIP")
                    .color(AnsiColor.TEXT_RESET)
                    .toString();

    private final boolean logTiming;

    private final boolean logTestMessages;

    private final boolean logSkipMessages;

    private final boolean logPassMessages;

    private final NanosecondsConverter nanosecondsConverter;

    /** Constructor */
    public TestStatusEngineExecutionListener() {
        logTiming =
                Configuration.singleton()
                        .get(Constants.CONSOLE_LOG_TIMING)
                        .map(
                                value -> {
                                    try {
                                        return Boolean.parseBoolean(value);
                                    } catch (NumberFormatException e) {
                                        return true;
                                    }
                                })
                        .orElse(true);

        LOGGER.trace("configuration [%s] = [%b]", Constants.CONSOLE_LOG_TIMING, logTiming);

        nanosecondsConverter =
                Configuration.singleton()
                        .get(Constants.CONSOLE_LOG_TIMING_UNITS)
                        .map(NanosecondsConverter::decode)
                        .orElse(NanosecondsConverter.MILLISECONDS);

        LOGGER.trace(
                "configuration [%s] = [%s]",
                Constants.CONSOLE_LOG_TIMING_UNITS, nanosecondsConverter);

        logTestMessages =
                Configuration.singleton()
                        .get(Constants.CONSOLE_LOG_TEST_MESSAGES)
                        .map(
                                value -> {
                                    try {
                                        return Boolean.parseBoolean(value);
                                    } catch (NumberFormatException e) {
                                        return true;
                                    }
                                })
                        .orElse(true);

        LOGGER.trace(
                "configuration [%s] = [%b]", Constants.CONSOLE_LOG_TEST_MESSAGES, logTestMessages);

        logPassMessages =
                Configuration.singleton()
                        .get(Constants.CONSOLE_LOG_PASS_MESSAGES)
                        .map(
                                value -> {
                                    try {
                                        return Boolean.parseBoolean(value);
                                    } catch (NumberFormatException e) {
                                        return true;
                                    }
                                })
                        .orElse(true);

        LOGGER.trace(
                "configuration [%s] = [%b]", Constants.CONSOLE_LOG_PASS_MESSAGES, logPassMessages);

        logSkipMessages =
                Configuration.singleton()
                        .get(Constants.CONSOLE_LOG_SKIP_MESSAGES)
                        .map(
                                value -> {
                                    try {
                                        return Boolean.parseBoolean(value);
                                    } catch (NumberFormatException e) {
                                        return true;
                                    }
                                })
                        .orElse(true);

        LOGGER.trace(
                "configuration [%s] = [%b]", Constants.CONSOLE_LOG_SKIP_MESSAGES, logSkipMessages);
    }

    @Override
    public void executionStarted(TestDescriptor testDescriptor) {
        if (logTestMessages) {
            boolean print = false;

            AnsiColorStringBuilder ansiColorStringBuilder =
                    new AnsiColorStringBuilder()
                            .append(INFO)
                            .append(" ")
                            .append(Thread.currentThread().getName())
                            .append(" | ")
                            .append(AnsiColor.WHITE_BRIGHT)
                            .append("TEST")
                            .color(AnsiColor.TEXT_RESET);

            if (testDescriptor instanceof MethodTestDescriptor) {
                MethodTestDescriptor methodTestDescriptor = (MethodTestDescriptor) testDescriptor;

                Class<?> testClass = methodTestDescriptor.getTestClass();
                Method testMethod = methodTestDescriptor.getTestMethod();
                Argument testArgument = methodTestDescriptor.getTestArgument();

                ansiColorStringBuilder
                        .append(" | ")
                        .append(testArgument.name())
                        .append(" | ")
                        .append(testClass.getName())
                        .append(" ")
                        .append(testMethod.getName())
                        .append("()")
                        .color(AnsiColor.TEXT_RESET);

                print = true;
            } else if (testDescriptor instanceof ArgumentTestDescriptor) {
                ArgumentTestDescriptor argumentTestDescriptor =
                        (ArgumentTestDescriptor) testDescriptor;

                Class<?> testClass = argumentTestDescriptor.getTestClass();
                Argument testArgument = argumentTestDescriptor.getTestArgument();

                ansiColorStringBuilder
                        .append(" | ")
                        .append(testArgument.name())
                        .append(" | ")
                        .append(testClass.getName())
                        .color(AnsiColor.TEXT_RESET);

                print = true;
            } else if (testDescriptor instanceof ClassTestDescriptor) {
                ClassTestDescriptor classTestDescriptor = (ClassTestDescriptor) testDescriptor;

                Class<?> testClass = classTestDescriptor.getTestClass();

                ansiColorStringBuilder
                        .append(" | ")
                        .append(testClass.getName())
                        .color(AnsiColor.TEXT_RESET);

                print = true;
            }

            if (print) {
                System.out.println(ansiColorStringBuilder);
                System.out.flush();
            }
        }
    }

    @Override
    public void executionSkipped(TestDescriptor testDescriptor, String reason) {
        if (testDescriptor instanceof EngineDescriptor) {
            return;
        }

        StopWatch stopWatch = ((ExtendedAbstractTestDescriptor) testDescriptor).getStopWatch();

        if (logSkipMessages) {
            boolean print = false;

            AnsiColorStringBuilder ansiColorStringBuilder =
                    new AnsiColorStringBuilder()
                            .append(INFO)
                            .append(" ")
                            .append(Thread.currentThread().getName())
                            .color(AnsiColor.TEXT_RESET);

            if (testDescriptor instanceof MethodTestDescriptor) {
                MethodTestDescriptor methodTestDescriptor = (MethodTestDescriptor) testDescriptor;

                Class<?> testClass = methodTestDescriptor.getTestClass();
                Method testMethod = methodTestDescriptor.getTestMethod();
                Argument testArgument = methodTestDescriptor.getTestArgument();

                ansiColorStringBuilder
                        .append(" | ")
                        .append("SKIP")
                        .append(" | ")
                        .append(testArgument.name())
                        .append(" | ")
                        .append(testClass.getName())
                        .append(" ")
                        .append(testMethod.getName())
                        .append("() ")
                        .append(nanosecondsConverter.toString(stopWatch.elapsedTime()))
                        .color(AnsiColor.TEXT_RESET);

                print = true;
            } else if (testDescriptor instanceof ArgumentTestDescriptor) {
                ArgumentTestDescriptor argumentTestDescriptor =
                        (ArgumentTestDescriptor) testDescriptor;

                Class<?> testClass = argumentTestDescriptor.getTestClass();
                Argument testArgument = argumentTestDescriptor.getTestArgument();

                ansiColorStringBuilder
                        .append(" | ")
                        .append("SKIP")
                        .append(" | ")
                        .append(testArgument.name())
                        .append(" | ")
                        .append(testClass.getName())
                        .append(" ")
                        .append(nanosecondsConverter.toString(stopWatch.elapsedTime()))
                        .color(AnsiColor.TEXT_RESET);

                print = true;
            } else if (testDescriptor instanceof ClassTestDescriptor) {
                ClassTestDescriptor classTestDescriptor = (ClassTestDescriptor) testDescriptor;

                Class<?> testClass = classTestDescriptor.getTestClass();

                ansiColorStringBuilder
                        .append(" | ")
                        .append("SKIP")
                        .append(" | ")
                        .append(testClass.getName())
                        .append(" ");

                if (logTiming) {
                    ansiColorStringBuilder.append(
                            nanosecondsConverter.toString(stopWatch.elapsedTime()));
                }

                ansiColorStringBuilder.color(AnsiColor.TEXT_RESET);

                print = true;
            }

            if (print) {
                System.out.println(ansiColorStringBuilder);
                System.out.flush();
            }
        }
    }

    @Override
    public void executionFinished(
            TestDescriptor testDescriptor, TestExecutionResult testExecutionResult) {
        if (testDescriptor instanceof EngineDescriptor) {
            return;
        }

        StopWatch stopWatch = ((ExtendedAbstractTestDescriptor) testDescriptor).getStopWatch();

        if (logPassMessages
                || testExecutionResult.getStatus() == TestExecutionResult.Status.FAILED) {
            boolean print = false;

            AnsiColorStringBuilder ansiColorStringBuilder =
                    new AnsiColorStringBuilder()
                            .append(INFO)
                            .append(" ")
                            .append(Thread.currentThread().getName())
                            .color(AnsiColor.TEXT_RESET);

            if (testDescriptor instanceof MethodTestDescriptor) {
                MethodTestDescriptor methodTestDescriptor = (MethodTestDescriptor) testDescriptor;

                Class<?> testClass = methodTestDescriptor.getTestClass();
                Method testMethod = methodTestDescriptor.getTestMethod();
                Argument testArgument = methodTestDescriptor.getTestArgument();

                ansiColorStringBuilder
                        .append(" | ")
                        .append(toString(testExecutionResult))
                        .append(" | ")
                        .append(testArgument.name())
                        .append(" | ")
                        .append(testClass.getName())
                        .append(" ")
                        .append(testMethod.getName())
                        .append("()");

                if (logTiming) {
                    ansiColorStringBuilder
                            .append(" ")
                            .append(nanosecondsConverter.toString(stopWatch.elapsedTime()));
                }

                ansiColorStringBuilder.color(AnsiColor.TEXT_RESET);

                print = true;
            } else if (testDescriptor instanceof ArgumentTestDescriptor) {
                ArgumentTestDescriptor argumentTestDescriptor =
                        (ArgumentTestDescriptor) testDescriptor;

                Class<?> testClass = argumentTestDescriptor.getTestClass();
                Argument testArgument = argumentTestDescriptor.getTestArgument();

                ansiColorStringBuilder
                        .append(" | ")
                        .append(toString(testExecutionResult))
                        .append(" | ")
                        .append(testArgument.name())
                        .append(" | ")
                        .append(testClass.getName());

                if (logTiming) {
                    ansiColorStringBuilder
                            .append(" ")
                            .append(nanosecondsConverter.toString(stopWatch.elapsedTime()));
                }

                ansiColorStringBuilder.color(AnsiColor.TEXT_RESET);

                print = true;
            } else if (testDescriptor instanceof ClassTestDescriptor) {
                ClassTestDescriptor classTestDescriptor = (ClassTestDescriptor) testDescriptor;

                Class<?> testClass = classTestDescriptor.getTestClass();

                ansiColorStringBuilder
                        .append(" | ")
                        .append(toString(testExecutionResult))
                        .append(" | ")
                        .append(testClass.getName());

                if (logTiming) {
                    ansiColorStringBuilder
                            .append(" ")
                            .append(nanosecondsConverter.toString(stopWatch.elapsedTime()));
                }

                ansiColorStringBuilder.color(AnsiColor.TEXT_RESET);

                print = true;
            }

            if (print) {
                System.out.println(ansiColorStringBuilder);
                System.out.flush();
            }
        }
    }

    /**
     * Method to convert a TestExecutionResult to a printable String
     *
     * @param testExecutionResult testExecutionResult
     * @return a String representing of the TestExecutionResult
     */
    private static String toString(TestExecutionResult testExecutionResult) {
        switch (testExecutionResult.getStatus()) {
            case SUCCESSFUL:
                {
                    return PASS;
                }
            case FAILED:
                {
                    return FAIL;
                }
            case ABORTED:
                {
                    return SKIP;
                }
            default:
                {
                    return AnsiColor.CYAN_BOLD.wrap("UNDEFINED");
                }
        }
    }
}
