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

package org.antublue.test.engine.internal.descriptor;

import java.util.Optional;
import org.antublue.test.engine.internal.Metadata;
import org.antublue.test.engine.internal.MetadataSupport;
import org.antublue.test.engine.internal.util.StopWatch;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;
import org.junit.platform.engine.support.hierarchical.ThrowableCollector;

/** Abstract class to implement an ExecutableTestDescriptor */
@SuppressWarnings("PMD.EmptyCatchBlock")
public abstract class ExecutableTestDescriptor extends AbstractTestDescriptor
        implements MetadataSupport {

    private final ThrowableCollector throwableCollector;
    private final Metadata metadata;
    private final StopWatch stopWatch;
    private ExecutionRequest executionRequest;
    private Object testInstance;

    protected ExecutableTestDescriptor(UniqueId uniqueId, String displayName) {
        super(uniqueId, displayName);

        throwableCollector = new ThrowableCollector(throwable -> true);
        metadata = new Metadata();
        stopWatch = new StopWatch();
    }

    /**
     * Method to execute the test descriptor
     *
     * @param executionRequest executionRequest
     */
    public abstract void execute(ExecutionRequest executionRequest);

    public void skip(ExecutionRequest executionRequest) {
        getChildren()
                .forEach(
                        testDescriptor -> {
                            if (testDescriptor instanceof ExecutableTestDescriptor) {
                                ((ExecutableTestDescriptor) testDescriptor).skip(executionRequest);
                            }
                        });
    }

    @Override
    public Metadata getMetadata() {
        return metadata;
    }

    protected void setExecutionRequest(ExecutionRequest executionRequest) {
        this.executionRequest = executionRequest;
    }

    protected ExecutionRequest getExecutionRequest() {
        return executionRequest;
    }

    protected <T> T getParent(Class<T> clazz) {
        Optional<TestDescriptor> optional = getParent();
        Preconditions.condition(optional.isPresent(), "parent is null");
        return clazz.cast(optional.get());
    }

    protected StopWatch getStopWatch() {
        return stopWatch;
    }

    protected void setTestInstance(Object testInstance) {
        this.testInstance = testInstance;
    }

    protected Object getTestInstance() {
        return testInstance;
    }

    protected ThrowableCollector getThrowableCollector() {
        return throwableCollector;
    }
}
