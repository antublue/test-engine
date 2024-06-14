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
import org.antublue.test.engine.Constants;
import org.antublue.test.engine.api.Named;
import org.antublue.test.engine.internal.Metadata;
import org.antublue.test.engine.internal.MetadataConstants;
import org.antublue.test.engine.internal.MetadataSupport;
import org.antublue.test.engine.internal.configuration.Configuration;
import org.antublue.test.engine.internal.logger.Logger;
import org.antublue.test.engine.internal.logger.LoggerFactory;
import org.antublue.test.engine.internal.util.AnsiColor;
import org.antublue.test.engine.internal.util.AnsiColorStringBuilder;
import org.antublue.test.engine.internal.util.HumanReadableTimeUtils;
import org.antublue.test.engine.internal.util.TestUtils;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;

/** Class to implement a TestStatusEngineExecutionListener */
public class StatusEngineExecutionListener implements EngineExecutionListener {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(StatusEngineExecutionListener.class);

    private static final Configuration CONFIGURATION = Configuration.getInstance();

    private static final TestUtils TEST_UTILS = TestUtils.getInstance();

    private static final String MESSAGE_TEST;
    private static final String MESSAGE_PASS;
    private static final String MESSAGE_FAIL;
    private static final String MESSAGE_SKIP;

    static {
        MESSAGE_TEST = CONFIGURATION.getProperty(Constants.CONSOLE_LOG_TEST_MESSAGE).orElse("TEST");
        MESSAGE_PASS = CONFIGURATION.getProperty(Constants.CONSOLE_LOG_PASS_MESSAGE).orElse("PASS");
        MESSAGE_FAIL = CONFIGURATION.getProperty(Constants.CONSOLE_LOG_FAIL_MESSAGE).orElse("FAIL");
        MESSAGE_SKIP = CONFIGURATION.getProperty(Constants.CONSOLE_LOG_SKIP_MESSAGE).orElse("SKIP");
    }

    private static final String INFO =
            new AnsiColorStringBuilder()
                    .color(AnsiColor.TEXT_WHITE)
                    .append("[")
                    .color(AnsiColor.TEXT_BLUE_BOLD)
                    .append("INFO")
                    .color(AnsiColor.TEXT_WHITE)
                    .append("]")
                    .color(AnsiColor.TEXT_RESET)
                    .toString();

    private static final String TEST =
            new AnsiColorStringBuilder()
                    .append(AnsiColor.TEXT_WHITE_BRIGHT)
                    .append(MESSAGE_TEST)
                    .color(AnsiColor.TEXT_RESET)
                    .toString();

    private static final String PASS =
            new AnsiColorStringBuilder()
                    .color(AnsiColor.TEXT_GREEN_BOLD_BRIGHT)
                    .append(MESSAGE_PASS)
                    .color(AnsiColor.TEXT_RESET)
                    .toString();

    private static final String FAIL =
            new AnsiColorStringBuilder()
                    .color(AnsiColor.TEXT_RED_BOLD_BRIGHT)
                    .append(MESSAGE_FAIL)
                    .color(AnsiColor.TEXT_RESET)
                    .toString();

    private static final String SKIP =
            new AnsiColorStringBuilder()
                    .color(AnsiColor.TEXT_YELLOW_BOLD_BRIGHT)
                    .append(MESSAGE_SKIP)
                    .color(AnsiColor.TEXT_RESET)
                    .toString();

    private final boolean consoleLogTiming;

    private final String consoleLogTimingUnits;

    private final boolean consoleLogTestMessages;

    private final boolean consoleLogSkipMessages;

    private final boolean consoleLogPassMessages;

    /** Constructor */
    public StatusEngineExecutionListener() {
        consoleLogTiming =
                CONFIGURATION
                        .getProperty(Constants.CONSOLE_LOG_TIMING)
                        .map(
                                value -> {
                                    try {
                                        return Boolean.parseBoolean(value);
                                    } catch (NumberFormatException e) {
                                        return true;
                                    }
                                })
                        .orElse(true);

        LOGGER.trace("configuration [%s] = [%b]", Constants.CONSOLE_LOG_TIMING, consoleLogTiming);

        consoleLogTimingUnits =
                CONFIGURATION
                        .getProperty(Constants.CONSOLE_LOG_TIMING_UNITS)
                        .orElse("milliseconds");

        LOGGER.trace(
                "configuration [%s] = [%s]",
                Constants.CONSOLE_LOG_TIMING_UNITS, consoleLogTimingUnits);

        consoleLogTestMessages =
                CONFIGURATION
                        .getProperty(Constants.CONSOLE_LOG_TEST_MESSAGES)
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
                "configuration [%s] = [%b]",
                Constants.CONSOLE_LOG_TEST_MESSAGES, consoleLogTestMessages);

        consoleLogPassMessages =
                CONFIGURATION
                        .getProperty(Constants.CONSOLE_LOG_PASS_MESSAGES)
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
                "configuration [%s] = [%b]",
                Constants.CONSOLE_LOG_PASS_MESSAGES, consoleLogPassMessages);

        consoleLogSkipMessages =
                CONFIGURATION
                        .getProperty(Constants.CONSOLE_LOG_SKIP_MESSAGES)
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
                "configuration [%s] = [%b]",
                Constants.CONSOLE_LOG_SKIP_MESSAGES, consoleLogSkipMessages);
    }

    @Override
    public void executionStarted(TestDescriptor testDescriptor) {
        if (consoleLogTestMessages && testDescriptor instanceof MetadataSupport) {
            MetadataSupport metadataSupport = (MetadataSupport) testDescriptor;
            Metadata metadata = metadataSupport.getMetadata();
            Class<?> testClass = metadata.get(MetadataConstants.TEST_CLASS);
            Named<?> testArgument = metadata.get(MetadataConstants.TEST_ARGUMENT);
            Method testMethod = metadata.get(MetadataConstants.TEST_METHOD);

            AnsiColorStringBuilder ansiColorStringBuilder =
                    new AnsiColorStringBuilder()
                            .append(INFO)
                            .append(" ")
                            .append(Thread.currentThread().getName())
                            .append(" | ")
                            .append(TEST)
                            .color(AnsiColor.TEXT_RESET);

            if (testArgument != null) {
                ansiColorStringBuilder.append(" | ").append(testArgument.getName());
            }

            if (testClass != null) {
                ansiColorStringBuilder.append(" | ").append(TEST_UTILS.getDisplayName(testClass));
            }

            if (testMethod != null) {
                ansiColorStringBuilder.append(" | ").append(TEST_UTILS.getDisplayName(testMethod));
            }

            ansiColorStringBuilder.color(AnsiColor.TEXT_RESET);

            System.out.println(ansiColorStringBuilder);
            System.out.flush();
        }
    }

    @Override
    public void executionSkipped(TestDescriptor testDescriptor, String reason) {
        if (consoleLogSkipMessages && testDescriptor instanceof MetadataSupport) {
            MetadataSupport metadataSupport = (MetadataSupport) testDescriptor;
            Metadata metadata = metadataSupport.getMetadata();
            Class<?> testClass = metadata.get(MetadataConstants.TEST_CLASS);
            Named<?> testArgument = metadata.get(MetadataConstants.TEST_ARGUMENT);
            Method testMethod = metadata.get(MetadataConstants.TEST_METHOD);
            Long elapsedTime = metadata.get(MetadataConstants.TEST_DESCRIPTOR_ELAPSED_TIME);

            AnsiColorStringBuilder ansiColorStringBuilder =
                    new AnsiColorStringBuilder()
                            .append(INFO)
                            .append(" ")
                            .append(Thread.currentThread().getName())
                            .append(" | ")
                            .append(AnsiColor.TEXT_WHITE_BRIGHT);

            ansiColorStringBuilder.append(SKIP).color(AnsiColor.TEXT_RESET);

            if (testArgument != null) {
                ansiColorStringBuilder.append(" | ").append(testArgument.getName());
            }

            if (testClass != null) {
                ansiColorStringBuilder.append(" | ").append(TEST_UTILS.getDisplayName(testClass));
            }

            if (testMethod != null) {
                ansiColorStringBuilder.append(" | ").append(TEST_UTILS.getDisplayName(testMethod));
            }

            if (consoleLogTiming && elapsedTime != null) {
                ansiColorStringBuilder
                        .append(" ")
                        .append(
                                HumanReadableTimeUtils.toTimingUnit(
                                        elapsedTime, consoleLogTimingUnits));
            }

            ansiColorStringBuilder.color(AnsiColor.TEXT_RESET);

            System.out.println(ansiColorStringBuilder);
            System.out.flush();
        }
    }

    @Override
    public void executionFinished(
            TestDescriptor testDescriptor, TestExecutionResult testExecutionResult) {
        if (consoleLogPassMessages && testDescriptor instanceof MetadataSupport) {
            MetadataSupport metadataSupport = (MetadataSupport) testDescriptor;
            Metadata metadata = metadataSupport.getMetadata();
            Class<?> testClass = metadata.get(MetadataConstants.TEST_CLASS);
            Named<?> testArgument = metadata.get(MetadataConstants.TEST_ARGUMENT);
            Method testMethod = metadata.get(MetadataConstants.TEST_METHOD);
            Long elapsedTime = metadata.get(MetadataConstants.TEST_DESCRIPTOR_ELAPSED_TIME);
            String testDescriptorStatus = metadata.get(MetadataConstants.TEST_DESCRIPTOR_STATUS);

            AnsiColorStringBuilder ansiColorStringBuilder =
                    new AnsiColorStringBuilder()
                            .append(INFO)
                            .append(" ")
                            .append(Thread.currentThread().getName())
                            .append(" | ")
                            .append(AnsiColor.TEXT_WHITE_BRIGHT);

            switch (testDescriptorStatus) {
                case "PASS":
                    {
                        ansiColorStringBuilder.append(PASS);
                        break;
                    }
                case "FAIL":
                    {
                        ansiColorStringBuilder.append(FAIL);
                        break;
                    }
                case "SKIP":
                    {
                        ansiColorStringBuilder.append(SKIP);
                        break;
                    }
                default:
                    {
                        ansiColorStringBuilder.append(AnsiColor.TEXT_CYAN_BOLD.wrap("????"));
                    }
            }

            ansiColorStringBuilder.color(AnsiColor.TEXT_RESET);

            if (testArgument != null) {
                ansiColorStringBuilder.append(" | ").append(testArgument.getName());
            }

            if (testClass != null) {
                ansiColorStringBuilder.append(" | ").append(TEST_UTILS.getDisplayName(testClass));
            }

            if (testMethod != null) {
                ansiColorStringBuilder.append(" | ").append(TEST_UTILS.getDisplayName(testMethod));
            }

            if (consoleLogTiming && elapsedTime != null) {
                ansiColorStringBuilder
                        .append(" ")
                        .append(
                                HumanReadableTimeUtils.toTimingUnit(
                                        elapsedTime, consoleLogTimingUnits));
            }

            ansiColorStringBuilder.color(AnsiColor.TEXT_RESET);

            System.out.println(ansiColorStringBuilder);
            System.out.flush();
        }
    }
}
