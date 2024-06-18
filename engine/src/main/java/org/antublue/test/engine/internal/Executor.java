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

package org.antublue.test.engine.internal;

import static java.lang.String.format;

import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.antublue.test.engine.exception.TestEngineException;
import org.antublue.test.engine.internal.configuration.Constants;
import org.antublue.test.engine.internal.descriptor.ExecutableTestDescriptor;
import org.antublue.test.engine.internal.logger.Logger;
import org.antublue.test.engine.internal.logger.LoggerFactory;
import org.antublue.test.engine.internal.util.NamedThreadFactory;
import org.junit.platform.engine.ConfigurationParameters;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;

/** Method to execute an ExecutionRequest */
public class Executor {

    private static final Logger LOGGER = LoggerFactory.getLogger(Executor.class);

    private static final int MAX_THREAD_COUNT =
            Math.max(1, Runtime.getRuntime().availableProcessors() - 2);

    private final CountDownLatch countDownLatch;

    public Executor() {
        countDownLatch = new CountDownLatch(1);
    }

    /**
     * Method to execute the ExecutionRequest
     *
     * @param executionRequest the execution request
     */
    public void execute(ExecutionRequest executionRequest) {
        try {
            LOGGER.trace("execute()");

            EngineExecutionListener engineExecutionListener =
                    executionRequest.getEngineExecutionListener();

            TestDescriptor rootTestDescriptor = executionRequest.getRootTestDescriptor();

            ExecutorService executorService = null;
            AtomicReference<CountDownLatch> countDownLatch = new AtomicReference<>();

            try {
                ConfigurationParameters configurationParameters =
                        executionRequest.getConfigurationParameters();

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

                LOGGER.info("[%s] = [%d]", Constants.THREAD_COUNT, threadCount);

                executorService =
                        new ThreadPoolExecutor(
                                threadCount,
                                threadCount,
                                60L,
                                TimeUnit.SECONDS,
                                new ArrayBlockingQueue<>(threadCount * 10),
                                new NamedThreadFactory("test-engine-%02d"),
                                new BlockingRejectedExecutionHandler());

                engineExecutionListener.executionStarted(executionRequest.getRootTestDescriptor());

                Set<? extends TestDescriptor> testDescriptors = rootTestDescriptor.getChildren();

                countDownLatch.set(new CountDownLatch(testDescriptors.size()));

                for (TestDescriptor testDescriptor : testDescriptors) {
                    if (testDescriptor instanceof ExecutableTestDescriptor) {
                        ExecutableTestDescriptor executableTestDescriptor =
                                (ExecutableTestDescriptor) testDescriptor;
                        executorService.submit(
                                () -> {
                                    try {
                                        executableTestDescriptor.execute(executionRequest);
                                    } catch (Throwable t) {
                                        t.printStackTrace(System.err);
                                    } finally {
                                        countDownLatch.get().countDown();
                                    }
                                });
                    }
                }
            } finally {
                if (countDownLatch.get() != null) {
                    try {
                        countDownLatch.get().await();
                    } catch (InterruptedException e) {
                        // DO NOTHING
                    }
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

    public void await() {
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            // DO NOTHING
        }
    }

    /** Class to handle a submit rejection, adding the Runnable using blocking semantics */
    private static class BlockingRejectedExecutionHandler implements RejectedExecutionHandler {

        @Override
        public void rejectedExecution(Runnable runnable, ThreadPoolExecutor executor) {
            if (!executor.isShutdown()) {
                try {
                    executor.getQueue().put(runnable);
                } catch (InterruptedException e) {
                    LOGGER.error("Runnable discarded!!!");
                }
            }
        }
    }
}
