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

import org.antublue.test.engine.internal.logger.Logger;
import org.antublue.test.engine.internal.logger.LoggerFactory;
import org.junit.platform.engine.ExecutionRequest;

import java.util.concurrent.CountDownLatch;

/** Class to implement an execution context */
public class ExecutorContext {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExecutorContext.class);

    private final ExecutionRequest executionRequest;
    private final CountDownLatch countDownLatch;
    private Object testInstance;

    /**
     * Constructor
     *
     * @param executionRequest executionRequest
     * @param countDownLatch countDownLatch
     */
    public ExecutorContext(ExecutionRequest executionRequest, CountDownLatch countDownLatch) {
        this.executionRequest = executionRequest;
        this.countDownLatch = countDownLatch;
    }

    /**
     * Method to get the ExecutionRequest
     *
     * @return the ExecutionRequest
     */
    public ExecutionRequest getExecutionRequest() {
        return executionRequest;
    }

    /**
     * Method to set the testInstance
     *
     * @param testInstance testInstance
     */
    public void setTestInstance(Object testInstance) {
        LOGGER.trace("setTestInstance testInstance [%s]", testInstance.getClass().getName());

        this.testInstance = testInstance;
    }

    /**
     * Method to get the test instance
     *
     * @return the testInstance
     */
    public Object getTestInstance() {
        return testInstance;
    }

    /** Method to mark the execution context complete */
    public void complete() {
        LOGGER.trace("complete()");

        this.countDownLatch.countDown();
    }
}
