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

package org.antublue.test.engine.internal.execution;

import static java.lang.String.format;

import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.antublue.test.engine.exception.TestEngineException;
import org.antublue.test.engine.internal.configuration.Constants;
import org.antublue.test.engine.internal.descriptor.ExecutableTestDescriptor;
import org.antublue.test.engine.internal.logger.Logger;
import org.antublue.test.engine.internal.logger.LoggerFactory;
import org.antublue.test.engine.internal.util.BlockingRejectedExecutionHandler;
import org.antublue.test.engine.internal.util.NamedThreadFactory;
import org.junit.platform.engine.ConfigurationParameters;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;

/** Method to execute an ExecutionRequest */
@SuppressWarnings("PMD.EmptyCatchBlock")
public class ExecutionContextExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExecutionContextExecutor.class);

    private static final int MAX_THREAD_COUNT =
            Math.max(1, Runtime.getRuntime().availableProcessors() - 2);

    private final CountDownLatch countDownLatch;

    /** Constructor */
    public ExecutionContextExecutor() {
        countDownLatch = new CountDownLatch(1);
    }

    /**
     * Method to execute the ExecutionContext
     *
     * @param executionContext executionContext
     */
    public void execute(ExecutionContext executionContext) {
        try {
            LOGGER.trace("execute()");

            EngineExecutionListener engineExecutionListener =
                    executionContext.getExecutionRequest().getEngineExecutionListener();

            TestDescriptor rootTestDescriptor =
                    executionContext.getExecutionRequest().getRootTestDescriptor();

            ExecutorService executorService = null;
            AtomicReference<CountDownLatch> countDownLatch = new AtomicReference<>();

            try {
                ConfigurationParameters configurationParameters =
                        executionContext.getExecutionRequest().getConfigurationParameters();

                int threadCount =
                        configurationParameters
                                .get(Constants.THREAD_COUNT)
                                .map(
                                        value -> {
                                            int intValue;
                                            try {
                                                intValue = Integer.parseInt(value);
                                                if (intValue < 1) {
                                                    throw new TestEngineException(
                                                            format(
                                                                    "Invalid thread count [%d]",
                                                                    intValue));
                                                }
                                                return intValue;
                                            } catch (NumberFormatException e) {
                                                throw new TestEngineException(
                                                        format("Invalid thread count [%s]", value),
                                                        e);
                                            }
                                        })
                                .orElse(MAX_THREAD_COUNT);

                LOGGER.trace("%s = [%d]", Constants.THREAD_COUNT, threadCount);

                executorService =
                        new ThreadPoolExecutor(
                                threadCount,
                                threadCount,
                                60L,
                                TimeUnit.SECONDS,
                                new ArrayBlockingQueue<>(threadCount * 10),
                                new NamedThreadFactory("test-engine-%02d"),
                                new BlockingRejectedExecutionHandler());

                engineExecutionListener.executionStarted(
                        executionContext.getExecutionRequest().getRootTestDescriptor());

                Set<? extends TestDescriptor> testDescriptors = rootTestDescriptor.getChildren();

                countDownLatch.set(new CountDownLatch(testDescriptors.size()));

                for (TestDescriptor testDescriptor : testDescriptors) {
                    if (testDescriptor instanceof ExecutableTestDescriptor) {
                        ExecutableTestDescriptor executableTestDescriptor =
                                (ExecutableTestDescriptor) testDescriptor;
                        executorService.submit(
                                () -> {
                                    try {
                                        executableTestDescriptor.execute(
                                                new ExecutionContext(executionContext));
                                    } catch (Throwable t) {
                                        t.printStackTrace(System.err);
                                    } finally {
                                        countDownLatch.get().countDown();
                                    }
                                });
                    }
                }
            } finally {
                try {
                    countDownLatch.get().await();
                } catch (InterruptedException e) {
                    // DO NOTHING
                }

                if (executorService != null) {
                    executorService.shutdown();
                }
            }

            engineExecutionListener.executionFinished(
                    rootTestDescriptor, TestExecutionResult.successful());
        } finally {
            countDownLatch.countDown();
        }
    }

    /** Method to wait for the executor to finish */
    public void await() {
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            // DO NOTHING
        }
    }
}
