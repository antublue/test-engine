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

import org.antublue.test.engine.TestEngineConstants;
import org.antublue.test.engine.internal.descriptor.ClassTestDescriptor;
import org.antublue.test.engine.internal.descriptor.ExtendedEngineDescriptor;
import org.antublue.test.engine.internal.descriptor.RunnableAdapter;
import org.antublue.test.engine.internal.logger.Logger;
import org.antublue.test.engine.internal.logger.LoggerFactory;
import org.antublue.test.engine.internal.util.Cast;
import org.junit.platform.engine.ExecutionRequest;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Method to execute an ExecutionRequest
 */
public class TestEngineExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestEngineExecutor.class);

    private final ExecutorService executorService;

    /**
     * Constructor
     */
    public TestEngineExecutor() {
        int threadCount =
                TestEngineConfiguration.getInstance()
                        .get(TestEngineConstants.THREAD_COUNT)
                        .map(value -> {
                            int intValue;
                            try {
                                intValue = Integer.parseInt(value);
                                if (intValue >= 1) {
                                    return intValue;
                                } else {
                                    throw new TestEngineException(String.format("Invalid thread count [%d]", intValue));
                                }
                            } catch (NumberFormatException e) {
                                throw new TestEngineException(String.format("Invalid thread count [%s]", value), e);
                            }
                        })
                        .orElse(Runtime.getRuntime().availableProcessors());

        LOGGER.trace("[%s] = [%d]", TestEngineConstants.THREAD_COUNT, threadCount);

        executorService = new ThreadPoolExecutor(
                threadCount,
                threadCount, 60L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(threadCount * 10),
                new NamedThreadFactory());
    }

    /**
     * Method to execute the ExecutionRequest
     *
     * @param executionRequest the execution request
     */
    public void execute(ExecutionRequest executionRequest) {
        LOGGER.trace("execute(ExecutionRequest)");

        ExtendedEngineDescriptor extendedEngineDescriptor = Cast.cast(executionRequest.getRootTestDescriptor());

        TestDescriptorUtils.trace(extendedEngineDescriptor);

        List<ClassTestDescriptor> classTestDescriptors =
                extendedEngineDescriptor.getChildren(ClassTestDescriptor.class);

        CountDownLatch countDownLatch = new CountDownLatch(classTestDescriptors.size());

        classTestDescriptors
                .forEach(classTestDescriptor ->
                        executorService.submit(
                                new RunnableAdapter(
                                        classTestDescriptor, new TestExecutionContext(executionRequest, countDownLatch)
                                )));

        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            throw new TestEngineException("Test execution interrupted");
        } finally {
            if (executorService != null) {
                executorService.shutdown();
            }
        }
    }

    /**
     * Class to implement a named ThreadFactory
     */
    private static class NamedThreadFactory implements ThreadFactory {

        private final AtomicInteger threadId = new AtomicInteger(1);

        /**
         * Method to create a new Thread
         *
         * @param r a runnable to be executed by new thread instance
         * @return the Thread
         */
        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r);
            thread.setName(String.format("test-engine-%02d", threadId.getAndIncrement()));
            thread.setDaemon(true);
            return thread;
        }
    }
}
