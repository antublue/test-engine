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

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.antublue.test.engine.internal.ExecutorContext;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;

/** Class to implement an extended AbstractTestDescriptor */
@SuppressWarnings("unchecked")
abstract class ExtendedAbstractTestDescriptor extends AbstractTestDescriptor {

    /**
     * Constructor
     *
     * @param uniqueId uniqueId
     * @param displayName displayName
     */
    protected ExtendedAbstractTestDescriptor(UniqueId uniqueId, String displayName) {
        super(uniqueId, displayName);
    }

    /**
     * Method to get a List of children cast as a specific Class
     *
     * @param clazz clazz
     * @return the return value
     * @param <T> the return type
     */
    public <T> List<T> getChildren(Class<T> clazz) {
        // Clazz is required to be able to get the generic type
        return getChildren().stream()
                .map((Function<TestDescriptor, T>) testDescriptor -> (T) testDescriptor)
                .collect(Collectors.toList());
    }

    /**
     * Method to execute the TestDescriptor
     *
     * @param executorContext testEngineExecutorContext
     */
    public abstract void execute(ExecutorContext executorContext);

    /**
     * Method to skip the TestDescriptor's children, then the TestDescriptor (recursively)
     *
     * @param executorContext testEngineExecutorContext
     */
    public void skip(ExecutorContext executorContext) {
        for (ExtendedAbstractTestDescriptor testDescriptor :
                getChildren(ExtendedAbstractTestDescriptor.class)) {
            testDescriptor.skip(executorContext);
        }

        executorContext
                .getExecutionRequest()
                .getEngineExecutionListener()
                .executionSkipped(this, "Skipped");
    }
}
