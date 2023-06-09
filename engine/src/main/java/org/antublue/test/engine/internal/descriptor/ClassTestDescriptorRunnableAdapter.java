/*
 * Copyright (C) 2022-2023 The AntuBLUE test-engine project authors
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

package org.antublue.test.engine.internal.descriptor;

import org.antublue.test.engine.internal.TestEngineExecutionContext;

/**
 * Class to implement a RunnableAdapter to test a ClassTestDescriptor in a Thread
 */
public final class ClassTestDescriptorRunnableAdapter implements Runnable {

    private final ClassTestDescriptor classTestDescriptor;
    private final TestEngineExecutionContext testEngineExecutionContext;

    /**
     * Constructor
     *
     * @param classTestDescriptor classTestDescriptor
     * @param testEngineExecutionContext testEngineExecutionContext
     */
    public ClassTestDescriptorRunnableAdapter(
            ClassTestDescriptor classTestDescriptor, TestEngineExecutionContext testEngineExecutionContext) {
        this.testEngineExecutionContext = testEngineExecutionContext;
        this.classTestDescriptor = classTestDescriptor;
    }

    @Override
    public void run() {
        try {
            classTestDescriptor.execute(testEngineExecutionContext);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
