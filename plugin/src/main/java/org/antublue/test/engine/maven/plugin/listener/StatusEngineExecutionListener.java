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
import org.antublue.test.engine.api.Argument;
import org.antublue.test.engine.configuration.Configuration;
import org.antublue.test.engine.logger.Logger;
import org.antublue.test.engine.logger.LoggerFactory;
import org.antublue.test.engine.test.Metadata;
import org.antublue.test.engine.test.MetadataConstants;
import org.antublue.test.engine.test.MetadataSupport;
import org.antublue.test.engine.test.util.TestUtils;
import org.antublue.test.engine.util.AnsiColor;
import org.antublue.test.engine.util.AnsiColorStringBuilder;
import org.antublue.test.engine.util.NanosecondsConverter;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;

/** Class to implement a TestStatusEngineExecutionListener */
public class StatusEngineExecutionListener implements EngineExecutionListener {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(StatusEngineExecutionListener.class);

    private static final Configuration CONFIGURATION = Configuration.getSingleton();

    private static final TestUtils TEST_UTILS = TestUtils.getSingleton();

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
    public StatusEngineExecutionListener() {
        logTiming =
                CONFIGURATION
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
                CONFIGURATION
                        .get(Constants.CONSOLE_LOG_TIMING_UNITS)
                        .map(NanosecondsConverter::decode)
                        .orElse(NanosecondsConverter.MILLISECONDS);

        LOGGER.trace(
                "configuration [%s] = [%s]",
                Constants.CONSOLE_LOG_TIMING_UNITS, nanosecondsConverter);

        logTestMessages =
                CONFIGURATION
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
                CONFIGURATION
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
                CONFIGURATION
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
        if (logTestMessages && testDescriptor instanceof MetadataSupport) {
            MetadataSupport metadataSupport = (MetadataSupport) testDescriptor;
            Metadata metadata = metadataSupport.getMetadata();
            Class<?> testClass = metadata.get(MetadataConstants.TEST_CLASS);
            Argument testArgument = metadata.get(MetadataConstants.TEST_ARGUMENT);
            Method testMethod = metadata.get(MetadataConstants.TEST_METHOD);

            AnsiColorStringBuilder ansiColorStringBuilder =
                    new AnsiColorStringBuilder()
                            .append(INFO)
                            .append(" ")
                            .append(Thread.currentThread().getName())
                            .append(" | ")
                            .append(AnsiColor.WHITE_BRIGHT)
                            .append("TEST")
                            .color(AnsiColor.TEXT_RESET);

            if (testArgument != null) {
                ansiColorStringBuilder.append(" | ").append(testArgument.name());
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
        if (logSkipMessages && testDescriptor instanceof MetadataSupport) {
            MetadataSupport metadataSupport = (MetadataSupport) testDescriptor;
            Metadata metadata = metadataSupport.getMetadata();
            Class<?> testClass = metadata.get(MetadataConstants.TEST_CLASS);
            Argument testArgument = metadata.get(MetadataConstants.TEST_ARGUMENT);
            Method testMethod = metadata.get(MetadataConstants.TEST_METHOD);
            Long elapsedTime = metadata.get(MetadataConstants.TEST_DESCRIPTOR_ELAPSED_TIME);

            AnsiColorStringBuilder ansiColorStringBuilder =
                    new AnsiColorStringBuilder()
                            .append(INFO)
                            .append(" ")
                            .append(Thread.currentThread().getName())
                            .append(" | ")
                            .append(AnsiColor.WHITE_BRIGHT);

            ansiColorStringBuilder.append(SKIP).color(AnsiColor.TEXT_RESET);

            if (testArgument != null) {
                ansiColorStringBuilder.append(" | ").append(testArgument.name());
            }

            if (testClass != null) {
                ansiColorStringBuilder.append(" | ").append(TEST_UTILS.getDisplayName(testClass));
            }

            if (testMethod != null) {
                ansiColorStringBuilder.append(" | ").append(TEST_UTILS.getDisplayName(testMethod));
            }

            if (logTiming && elapsedTime != null) {
                ansiColorStringBuilder
                        .append(" ")
                        .append(nanosecondsConverter.toString(elapsedTime));
            }

            ansiColorStringBuilder.color(AnsiColor.TEXT_RESET);

            System.out.println(ansiColorStringBuilder);
            System.out.flush();
        }
    }

    @Override
    public void executionFinished(
            TestDescriptor testDescriptor, TestExecutionResult testExecutionResult) {
        if (logPassMessages && testDescriptor instanceof MetadataSupport) {
            MetadataSupport metadataSupport = (MetadataSupport) testDescriptor;
            Metadata metadata = metadataSupport.getMetadata();
            Class<?> testClass = metadata.get(MetadataConstants.TEST_CLASS);
            Argument testArgument = metadata.get(MetadataConstants.TEST_ARGUMENT);
            Method testMethod = metadata.get(MetadataConstants.TEST_METHOD);
            Long elapsedTime = metadata.get(MetadataConstants.TEST_DESCRIPTOR_ELAPSED_TIME);
            String testDescriptorStatus = metadata.get(MetadataConstants.TEST_DESCRIPTOR_STATUS);

            AnsiColorStringBuilder ansiColorStringBuilder =
                    new AnsiColorStringBuilder()
                            .append(INFO)
                            .append(" ")
                            .append(Thread.currentThread().getName())
                            .append(" | ")
                            .append(AnsiColor.WHITE_BRIGHT);

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
                        ansiColorStringBuilder.append(AnsiColor.CYAN_BOLD.wrap("????"));
                    }
            }

            ansiColorStringBuilder.color(AnsiColor.TEXT_RESET);

            if (testArgument != null) {
                ansiColorStringBuilder.append(" | ").append(testArgument.name());
            }

            if (testClass != null) {
                ansiColorStringBuilder.append(" | ").append(TEST_UTILS.getDisplayName(testClass));
            }

            if (testMethod != null) {
                ansiColorStringBuilder.append(" | ").append(TEST_UTILS.getDisplayName(testMethod));
            }

            if (logTiming && elapsedTime != null) {
                ansiColorStringBuilder
                        .append(" ")
                        .append(nanosecondsConverter.toString(elapsedTime));
            }

            ansiColorStringBuilder.color(AnsiColor.TEXT_RESET);

            System.out.println(ansiColorStringBuilder);
            System.out.flush();
        }
    }
}
