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

package org.antublue.test.engine.internal.test.descriptor;

import java.util.Optional;
import org.antublue.test.engine.Constants;
import org.antublue.test.engine.api.Argument;
import org.antublue.test.engine.api.utils.ReflectionUtils;
import org.antublue.test.engine.api.utils.StopWatch;
import org.antublue.test.engine.internal.configuration.Configuration;
import org.antublue.test.engine.internal.test.descriptor.parameterized.ParameterizedTestUtils;
import org.antublue.test.engine.internal.test.extension.ExtensionManager;
import org.antublue.test.engine.internal.test.util.LockProcessor;
import org.antublue.test.engine.internal.test.util.TestUtils;
import org.antublue.test.engine.internal.test.util.ThrowableContext;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestDescriptor;

/** Abstract class to implement an ExecutableTestDescriptor */
@SuppressWarnings("PMD.EmptyCatchBlock")
public abstract class ExecutableTestDescriptor extends AbstractTestDescriptor
        implements MetadataSupport {

    protected static final Argument NULL_TEST_ARGUMENT = null;

    protected static final ReflectionUtils REFLECTION_UTILS = ReflectionUtils.getSingleton();

    protected static final TestUtils TEST_UTILS = TestUtils.getSingleton();

    protected static final ParameterizedTestUtils PARAMETERIZED_UTILS =
            ParameterizedTestUtils.getSingleton();

    protected static final ExtensionManager EXTENSION_MANAGER = ExtensionManager.getSingleton();

    protected static final LockProcessor LOCK_PROCESSOR = LockProcessor.getSingleton();

    private final ThrowableContext throwableContext;
    private final Metadata metadata;
    private final StopWatch stopWatch;
    private ExecutionRequest executionRequest;
    private long throttleMilliseconds;
    private Object testInstance;

    protected ExecutableTestDescriptor() {
        stopWatch = new StopWatch();
        throwableContext = new ThrowableContext();
        metadata = new Metadata();

        Configuration.getSingleton()
                .get(Constants.THREAD_THROTTLE_MILLISECONDS)
                .ifPresent(
                        s -> {
                            try {
                                throttleMilliseconds = Long.parseLong(s);
                            } catch (Throwable t) {
                                // DO NOTHING
                            }
                        });
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
