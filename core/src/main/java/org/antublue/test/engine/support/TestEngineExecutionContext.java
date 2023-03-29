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

package org.antublue.test.engine.support;

import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.TestExecutionResult;

import java.util.List;

/**
 * Class to implement a execution context
 */
public class TestEngineExecutionContext {

    private final EngineExecutionListener engineExecutionListener;
    private final List<TestExecutionResult> testExecutionResultList;
    private Object testInstance;

    /**
     * Constructor
     *
     * @param engineExecutionListener
     * @param testExecutionResultList
     */
    public TestEngineExecutionContext(EngineExecutionListener engineExecutionListener, List<TestExecutionResult> testExecutionResultList) {
        this.engineExecutionListener = engineExecutionListener;
        this.testExecutionResultList = testExecutionResultList;
    }

    /**
     * Method to get the EngineExecutionListener
     *
     * @return
     */
    public EngineExecutionListener getEngineExecutionListener() {
        return engineExecutionListener;
    }

    /**
     * Method to get the ThrowableCollector
     *
     * @return
     */
    public List<TestExecutionResult> getTestExecutionResultList() {
        return testExecutionResultList;
    }

    /**
     * Method to set the test instance
     *
     * @param testInstance
     */
    public void setTestInstance(Object testInstance) {
        this.testInstance = testInstance;
    }

    /**
     * Method to get the test instance
     *
     * @return
     */
    public Object getTestInstance() {
        return testInstance;
    }
}
