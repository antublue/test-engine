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

import org.junit.platform.engine.ExecutionRequest;

import java.util.concurrent.CountDownLatch;

/**
 * Class to implement a TestExecutionContext for multithreaded execution
 */
public class TestEngineExecutionContext {

    private final ExecutionRequest executionRequest;
    private final CountDownLatch countDownLatch;
    private Object testInstance;

    /**
     * Constructor
     *
     * @param executionRequest executionRequest
     * @param countDownLatch countDownLatch
     */
    public TestEngineExecutionContext(ExecutionRequest executionRequest, CountDownLatch countDownLatch) {
        this.executionRequest = executionRequest;
        this.countDownLatch = countDownLatch;
    }

    /**
     * Method to get the ExecutionRequest
     *
     * @return the return value
     */
    public ExecutionRequest getExecutionRequest() {
        return executionRequest;
    }

    /**
     * Method to get the CountDownLatch
     *
     * @return the return value
     */
    public CountDownLatch getCountDownLatch() {
        return countDownLatch;
    }

    /**
     * Method to set the test instance Object
     *
     * @param testInstance testInstance
     */
    public void setTestInstance(Object testInstance) {
        this.testInstance = testInstance;
    }

    /**
     * Method to get the test instance Object
     *
     * @return the return value
     */
    public Object getTestInstance() {
        return testInstance;
    }
}
