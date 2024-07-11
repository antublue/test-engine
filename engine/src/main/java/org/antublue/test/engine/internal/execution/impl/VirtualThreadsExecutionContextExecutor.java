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

package org.antublue.test.engine.internal.execution.impl;

import static java.lang.String.format;

import io.github.thunkware.vt.bridge.ThreadTool;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.antublue.test.engine.exception.TestEngineException;
import org.antublue.test.engine.internal.configuration.Constants;
import org.antublue.test.engine.internal.descriptor.ExecutableTestDescriptor;
import org.antublue.test.engine.internal.execution.ExecutionContext;
import org.antublue.test.engine.internal.execution.ExecutionContextExecutor;
import org.antublue.test.engine.internal.logger.Logger;
import org.antublue.test.engine.internal.logger.LoggerFactory;
import org.junit.platform.engine.ConfigurationParameters;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;

/** Class to implement VirtualThreadsExecutionContextExecutor */
@SuppressWarnings("PMD.EmptyCatchBlock")
public class VirtualThreadsExecutionContextExecutor implements ExecutionContextExecutor {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(VirtualThreadsExecutionContextExecutor.class);

    private static final int MAX_THREAD_COUNT =
            Math.max(1, Runtime.getRuntime().availableProcessors() - 2);

    private final CountDownLatch countDownLatch;

    /** Constructor */
    public VirtualThreadsExecutionContextExecutor() {
        countDownLatch = new CountDownLatch(1);
    }

    @Override
    public void execute(ExecutionContext executionContext) {
        try {
            LOGGER.trace(
                    "execute() children [%d]",
                    executionContext
                            .getExecutionRequest()
                            .getRootTestDescriptor()
                            .getChildren()
                            .size());

            EngineExecutionListener engineExecutionListener =
                    executionContext.getExecutionRequest().getEngineExecutionListener();

            TestDescriptor rootTestDescriptor =
                    executionContext.getExecutionRequest().getRootTestDescriptor();

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

                engineExecutionListener.executionStarted(
                        executionContext.getExecutionRequest().getRootTestDescriptor());

                Set<? extends TestDescriptor> testDescriptors = rootTestDescriptor.getChildren();

                LOGGER.trace("test descriptor count [%d]", testDescriptors.size());

                countDownLatch.set(new CountDownLatch(testDescriptors.size()));

                Semaphore semaphore = new Semaphore(threadCount);
                AtomicInteger threadId = new AtomicInteger(1);

                for (TestDescriptor testDescriptor : testDescriptors) {
                    if (testDescriptor instanceof ExecutableTestDescriptor) {
                        ExecutableTestDescriptor executableTestDescriptor =
                                (ExecutableTestDescriptor) testDescriptor;

                        try {
                            semaphore.acquire();

                            Thread thread =
                                    ThreadTool.unstartedVirtualThread(
                                            () -> {
                                                try {
                                                    executableTestDescriptor.execute(
                                                            new ExecutionContext(executionContext));
                                                } catch (Throwable t) {
                                                    t.printStackTrace(System.err);
                                                } finally {
                                                    countDownLatch.get().countDown();
                                                    threadId.decrementAndGet();
                                                    semaphore.release();
                                                }
                                            });
                            thread.setName(format("test-engine-%02d", threadId.getAndIncrement()));
                            thread.start();
                        } catch (InterruptedException e) {
                            // DO NOTHING
                        }
                    }
                }
            } finally {
                try {
                    countDownLatch.get().await();
                } catch (InterruptedException e) {
                    // DO NOTHING
                }
            }

            engineExecutionListener.executionFinished(
                    rootTestDescriptor, TestExecutionResult.successful());
        } finally {
            countDownLatch.countDown();
        }
    }

    @Override
    public void await() {
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            // DO NOTHING
        }
    }
}
