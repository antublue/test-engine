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

import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.ExecutionRequest;

import java.util.concurrent.CountDownLatch;

/**
 * Class to implement an execution context
 */
public class TestEngineExecutorContext {

    private final ExecutionRequest executionRequest;
    private final CountDownLatch countDownLatch;
    private final EngineExecutionListener engineExecutionListener;

    /**
     * Constructor
     *
     * @param executionRequest executionRequest
     * @param countDownLatch countDownLatch
     */
    public TestEngineExecutorContext(ExecutionRequest executionRequest, CountDownLatch countDownLatch) {
        this.executionRequest = executionRequest;
        this.countDownLatch = countDownLatch;
        this.engineExecutionListener = executionRequest.getEngineExecutionListener();
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
     * Method to get the EngineExecutionListener
     *
     * @return the EngineExecutionListener
     */
    public EngineExecutionListener getEngineExecutionListener() {
        return engineExecutionListener;
    }

    /**
     * Method to mark the execution context complete
     */
    public void complete() {
        this.countDownLatch.countDown();
    }
}
