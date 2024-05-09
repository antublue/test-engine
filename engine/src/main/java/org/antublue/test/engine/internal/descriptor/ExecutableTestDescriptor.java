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
import org.antublue.test.engine.Constants;
import org.antublue.test.engine.internal.Metadata;
import org.antublue.test.engine.internal.MetadataSupport;
import org.antublue.test.engine.internal.configuration.Configuration;
import org.antublue.test.engine.internal.util.StopWatch;
import org.antublue.test.engine.internal.util.ThrowableContext;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;

/** Abstract class to implement an ExecutableTestDescriptor */
@SuppressWarnings("PMD.EmptyCatchBlock")
public abstract class ExecutableTestDescriptor extends AbstractTestDescriptor
        implements MetadataSupport {

    private static final Configuration CONFIGURATION = Configuration.getInstance();

    private static long THREAD_THROTTLE_MILLISECONDS = 0;

    static {
        Optional<String> optional = CONFIGURATION.get(Constants.THREAD_THROTTLE_MILLISECONDS);

        if (optional.isPresent()) {
            try {
                THREAD_THROTTLE_MILLISECONDS = Long.parseLong(optional.get());
            } catch (Throwable t) {
                // DO NOTHING
            }
        }
    }

    private final ThrowableContext throwableContext;
    private final Metadata metadata;
    private final StopWatch stopWatch;
    private ExecutionRequest executionRequest;
    private long throttleMilliseconds;
    private Object testInstance;

    protected ExecutableTestDescriptor(UniqueId uniqueId, String displayName) {
        super(uniqueId, displayName);

        stopWatch = new StopWatch();
        throwableContext = new ThrowableContext();
        metadata = new Metadata();
    }

    protected void throttle() {
        if (throttleMilliseconds > 0) {
            try {
                Thread.sleep(throttleMilliseconds);
            } catch (Throwable t) {
                // DO NOTHING
            }
        }
    }

    protected void setExecutionRequest(ExecutionRequest executionRequest) {
        this.executionRequest = executionRequest;
    }

    protected ExecutionRequest getExecutionRequest() {
        return executionRequest;
    }

    public <T> T getParent(Class<T> clazz) {
        Optional<TestDescriptor> optional = getParent();
        Preconditions.condition(optional.isPresent(), "parent is null");
        return clazz.cast(optional.get());
    }

    public StopWatch getStopWatch() {
        return stopWatch;
    }

    protected void setTestInstance(Object testInstance) {
        this.testInstance = testInstance;
    }

    public Object getTestInstance() {
        return testInstance;
    }

    public ThrowableContext getThrowableContext() {
        return throwableContext;
    }

    @Override
    public Metadata getMetadata() {
        return metadata;
    }

    /**
     * Method to execute the test descriptor
     *
     * @param executionRequest executionRequest
     */
    public abstract void execute(ExecutionRequest executionRequest);
}
