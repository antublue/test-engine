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

import java.util.Map;
import org.antublue.test.engine.configuration.Configuration;
import org.antublue.test.engine.configuration.Constants;
import org.antublue.test.engine.logger.Logger;
import org.antublue.test.engine.logger.LoggerFactory;
import org.antublue.test.engine.test.descriptor.Metadata;
import org.antublue.test.engine.util.AnsiColor;
import org.antublue.test.engine.util.AnsiColorStringBuilder;
import org.antublue.test.engine.util.NanosecondsConverter;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;

/** Class to implement a TestStatusEngineExecutionListener */
public class TestDescriptorExecutionListener implements EngineExecutionListener {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(TestDescriptorExecutionListener.class);

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
    public TestDescriptorExecutionListener() {
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
        if (logTestMessages && testDescriptor instanceof Metadata) {
            Metadata metadata = (Metadata) testDescriptor;
            Map<String, String> metadataMap = metadata.getMetadataMap();
            String testClass = metadataMap.get("testClass");
            String testArgument = metadataMap.get("testArgument");
            String testMethod = metadataMap.get("testMethod");

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
                ansiColorStringBuilder.append(" | ").append(testArgument);
            }

            if (testClass != null) {
                ansiColorStringBuilder.append(" | ").append(testClass);
            }

            if (testMethod != null) {
                ansiColorStringBuilder.append(" | ").append(testMethod).append("()");
            }

            ansiColorStringBuilder.color(AnsiColor.TEXT_RESET);

            System.out.println(ansiColorStringBuilder);
            System.out.flush();
        }
    }

    @Override
    public void executionSkipped(TestDescriptor testDescriptor, String reason) {
        if (logTestMessages && testDescriptor instanceof Metadata) {
            Metadata metadata = (Metadata) testDescriptor;
            Map<String, String> metadataMap = metadata.getMetadataMap();
            String testClass = metadataMap.get("testClass");
            String testArgument = metadataMap.get("testArgument");
            String testMethod = metadataMap.get("testMethod");
            String elapsedTime = metadataMap.get("elapsedTime");

            AnsiColorStringBuilder ansiColorStringBuilder =
                    new AnsiColorStringBuilder()
                            .append(INFO)
                            .append(" ")
                            .append(Thread.currentThread().getName())
                            .append(" | ")
                            .append(AnsiColor.WHITE_BRIGHT)
                            .append("SKIP")
                            .color(AnsiColor.TEXT_RESET);

            if (testArgument != null) {
                ansiColorStringBuilder.append(" | ").append(testArgument);
            }

            if (testClass != null) {
                ansiColorStringBuilder.append(" | ").append(testClass);
            }

            if (testMethod != null) {
                ansiColorStringBuilder.append(" | ").append(testMethod).append("()");
            }

            if (elapsedTime != null) {
                ansiColorStringBuilder
                        .append(" ")
                        .append(
                                NanosecondsConverter.MILLISECONDS.toString(
                                        Long.parseLong(elapsedTime)));
            }

            ansiColorStringBuilder.color(AnsiColor.TEXT_RESET);

            System.out.println(ansiColorStringBuilder);
            System.out.flush();
        }
    }

    @Override
    public void executionFinished(
            TestDescriptor testDescriptor, TestExecutionResult testExecutionResult) {
        if (logTestMessages && testDescriptor instanceof Metadata) {
            Metadata metadata = (Metadata) testDescriptor;
            Map<String, String> metadataMap = metadata.getMetadataMap();
            String testClass = metadataMap.get("testClass");
            String testArgument = metadataMap.get("testArgument");
            String testMethod = metadataMap.get("testMethod");
            String elapsedTime = metadataMap.get("elapsedTime");
            String status = metadataMap.get("status");

            AnsiColorStringBuilder ansiColorStringBuilder =
                    new AnsiColorStringBuilder()
                            .append(INFO)
                            .append(" ")
                            .append(Thread.currentThread().getName())
                            .append(" | ")
                            .append(AnsiColor.WHITE_BRIGHT);

            if (status.equals("PASS")) {
                ansiColorStringBuilder.append(PASS);
            } else if (status.equals("FAIL")) {
                ansiColorStringBuilder.append(FAIL);
            } else {
                ansiColorStringBuilder.append(AnsiColor.CYAN_BOLD.wrap("????"));
            }

            ansiColorStringBuilder.color(AnsiColor.TEXT_RESET);

            if (testArgument != null) {
                ansiColorStringBuilder.append(" | ").append(testArgument);
            }

            if (testClass != null) {
                ansiColorStringBuilder.append(" | ").append(testClass);
            }

            if (testMethod != null) {
                ansiColorStringBuilder.append(" | ").append(testMethod).append("()");
            }

            if (elapsedTime != null) {
                ansiColorStringBuilder
                        .append(" ")
                        .append(
                                NanosecondsConverter.MILLISECONDS.toString(
                                        Long.parseLong(elapsedTime)));
            }

            ansiColorStringBuilder.color(AnsiColor.TEXT_RESET);

            System.out.println(ansiColorStringBuilder);
            System.out.flush();
        }
    }
}
