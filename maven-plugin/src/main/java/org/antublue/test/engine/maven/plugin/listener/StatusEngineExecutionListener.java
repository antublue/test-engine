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
import org.antublue.test.engine.api.Argument;
import org.antublue.test.engine.internal.configuration.Configuration;
import org.antublue.test.engine.internal.configuration.Constants;
import org.antublue.test.engine.internal.logger.Logger;
import org.antublue.test.engine.internal.logger.LoggerFactory;
import org.antublue.test.engine.internal.metadata.Metadata;
import org.antublue.test.engine.internal.metadata.MetadataConstants;
import org.antublue.test.engine.internal.metadata.MetadataInformation;
import org.antublue.test.engine.internal.util.AnsiColor;
import org.antublue.test.engine.internal.util.AnsiColorStringBuilder;
import org.antublue.test.engine.internal.util.HumanReadableTimeUtils;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;

/** Class to implement a TestStatusEngineExecutionListener */
public class StatusEngineExecutionListener implements EngineExecutionListener {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(StatusEngineExecutionListener.class);

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

    private final boolean consoleLogTiming;

    private final String consoleLogTimingUnits;

    private final boolean consoleLogTestMessages;

    private final String consoleTestMessage;

    private final boolean consoleLogSkipMessages;

    private final String consoleSkipMessage;

    private final boolean consoleLogPassMessages;

    private final String consolePassMessage;

    private final String consoleFailMessage;

    /** Constructor */
    public StatusEngineExecutionListener() {
        Configuration configuration = Configuration.getInstance();

        consoleLogTiming = configuration.getBoolean(Constants.CONSOLE_LOG_TIMING).orElse(true);

        LOGGER.trace("configuration [%s] = [%b]", Constants.CONSOLE_LOG_TIMING, consoleLogTiming);

        consoleLogTimingUnits =
                configuration.get(Constants.CONSOLE_LOG_TIMING_UNITS).orElse("milliseconds");

        LOGGER.trace(
                "configuration [%s] = [%s]",
                Constants.CONSOLE_LOG_TIMING_UNITS, consoleLogTimingUnits);

        consoleLogTestMessages =
                configuration.getBoolean(Constants.CONSOLE_LOG_TEST_MESSAGES).orElse(true);

        LOGGER.trace(
                "configuration [%s] = [%b]",
                Constants.CONSOLE_LOG_TEST_MESSAGES, consoleLogTestMessages);

        consoleLogPassMessages =
                configuration.getBoolean(Constants.CONSOLE_LOG_PASS_MESSAGES).orElse(true);

        LOGGER.trace(
                "configuration [%s] = [%b]",
                Constants.CONSOLE_LOG_PASS_MESSAGES, consoleLogPassMessages);

        consoleLogSkipMessages =
                configuration.getBoolean(Constants.CONSOLE_LOG_SKIP_MESSAGES).orElse(true);

        LOGGER.trace(
                "configuration [%s] = [%b]",
                Constants.CONSOLE_LOG_SKIP_MESSAGES, consoleLogSkipMessages);

        consoleTestMessage =
                new AnsiColorStringBuilder()
                        .append(AnsiColor.TEXT_WHITE_BRIGHT)
                        .append(
                                configuration
                                        .get(Constants.CONSOLE_LOG_TEST_MESSAGE)
                                        .orElse("TEST"))
                        .color(AnsiColor.TEXT_RESET)
                        .toString();

        consolePassMessage =
                new AnsiColorStringBuilder()
                        .color(AnsiColor.TEXT_GREEN_BOLD_BRIGHT)
                        .append(
                                configuration
                                        .get(Constants.CONSOLE_LOG_PASS_MESSAGE)
                                        .orElse("PASS"))
                        .color(AnsiColor.TEXT_RESET)
                        .toString();

        consoleSkipMessage =
                new AnsiColorStringBuilder()
                        .color(AnsiColor.TEXT_YELLOW_BOLD_BRIGHT)
                        .append(
                                configuration
                                        .get(Constants.CONSOLE_LOG_SKIP_MESSAGE)
                                        .orElse("SKIP"))
                        .color(AnsiColor.TEXT_RESET)
                        .toString();

        consoleFailMessage =
                new AnsiColorStringBuilder()
                        .color(AnsiColor.TEXT_RED_BOLD_BRIGHT)
                        .append(
                                configuration
                                        .get(Constants.CONSOLE_LOG_FAIL_MESSAGE)
                                        .orElse("FAIL"))
                        .color(AnsiColor.TEXT_RESET)
                        .toString();
    }

    @Override
    public void executionStarted(TestDescriptor testDescriptor) {
        if (consoleLogTestMessages && testDescriptor instanceof Metadata) {
            Metadata metadata = (Metadata) testDescriptor;
            MetadataInformation metadataInformation = metadata.getMetadata();
            Class<?> testClass = metadataInformation.get(MetadataConstants.TEST_CLASS);
            String testClassDisplayName =
                    metadataInformation.get(MetadataConstants.TEST_CLASS_DISPLAY_NAME);
            Method testMethod = metadataInformation.get(MetadataConstants.TEST_METHOD);
            String testMethodDisplayName =
                    metadataInformation.get(MetadataConstants.TEST_METHOD_DISPLAY_NAME) + "()";
            Argument<?> testArgument = metadataInformation.get(MetadataConstants.TEST_ARGUMENT);

            AnsiColorStringBuilder ansiColorStringBuilder =
                    new AnsiColorStringBuilder()
                            .append(INFO)
                            .append(" ")
                            .append(Thread.currentThread().getName())
                            .append(" | ")
                            .append(consoleTestMessage)
                            .color(AnsiColor.TEXT_RESET);

            if (testArgument != null) {
                ansiColorStringBuilder.append(" | ").append(testArgument.getName());
            }

            if (testClass != null) {
                ansiColorStringBuilder.append(" | ").append(testClassDisplayName);
            }

            if (testMethod != null) {
                ansiColorStringBuilder.append(" | ").append(testMethodDisplayName);
            }

            ansiColorStringBuilder.color(AnsiColor.TEXT_RESET);

            System.out.println(ansiColorStringBuilder);
            System.out.flush();
        }
    }

    @Override
    public void executionSkipped(TestDescriptor testDescriptor, String reason) {
        if (consoleLogSkipMessages && testDescriptor instanceof Metadata) {
            Metadata metadata = (Metadata) testDescriptor;
            MetadataInformation metadataInformation = metadata.getMetadata();
            Class<?> testClass = metadataInformation.get(MetadataConstants.TEST_CLASS);
            String testClassDisplayName =
                    metadataInformation.get(MetadataConstants.TEST_CLASS_DISPLAY_NAME);
            Argument<?> testArgument = metadataInformation.get(MetadataConstants.TEST_ARGUMENT);
            Method testMethod = metadataInformation.get(MetadataConstants.TEST_METHOD);
            String testMethodDisplayName =
                    metadataInformation.get(MetadataConstants.TEST_METHOD_DISPLAY_NAME) + "()";
            Long elapsedTime =
                    metadataInformation.get(MetadataConstants.TEST_DESCRIPTOR_ELAPSED_TIME);

            AnsiColorStringBuilder ansiColorStringBuilder =
                    new AnsiColorStringBuilder()
                            .append(INFO)
                            .append(" ")
                            .append(Thread.currentThread().getName())
                            .append(" | ")
                            .append(AnsiColor.TEXT_WHITE_BRIGHT);

            ansiColorStringBuilder.append(consoleSkipMessage).color(AnsiColor.TEXT_RESET);

            if (testArgument != null) {
                ansiColorStringBuilder.append(" | ").append(testArgument.getName());
            }

            if (testClass != null) {
                ansiColorStringBuilder.append(" | ").append(testClassDisplayName);
            }

            if (testMethod != null) {
                ansiColorStringBuilder.append(" | ").append(testMethodDisplayName);
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
        if (consoleLogPassMessages && testDescriptor instanceof Metadata) {
            Metadata metadata = (Metadata) testDescriptor;
            MetadataInformation metadataInformation = metadata.getMetadata();
            Class<?> testClass = metadataInformation.get(MetadataConstants.TEST_CLASS);
            String testClassDisplayName =
                    metadataInformation.get(MetadataConstants.TEST_CLASS_DISPLAY_NAME);
            Argument<?> testArgument = metadataInformation.get(MetadataConstants.TEST_ARGUMENT);
            Method testMethod = metadataInformation.get(MetadataConstants.TEST_METHOD);
            String testMethodDisplayName =
                    metadataInformation.get(MetadataConstants.TEST_METHOD_DISPLAY_NAME) + "()";
            Long elapsedTime =
                    metadataInformation.get(MetadataConstants.TEST_DESCRIPTOR_ELAPSED_TIME);
            String testDescriptorStatus =
                    metadataInformation.get(MetadataConstants.TEST_DESCRIPTOR_STATUS);

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
                        ansiColorStringBuilder.append(consolePassMessage);
                        break;
                    }
                case "FAIL":
                    {
                        ansiColorStringBuilder.append(consoleFailMessage);
                        break;
                    }
                case "SKIP":
                    {
                        ansiColorStringBuilder.append(consoleSkipMessage);
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
                ansiColorStringBuilder.append(" | ").append(testClassDisplayName);
            }

            if (testMethod != null) {
                ansiColorStringBuilder.append(" | ").append(testMethodDisplayName);
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
