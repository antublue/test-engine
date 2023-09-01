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

package org.antublue.test.engine.test.descriptor.standard;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import org.antublue.test.engine.test.descriptor.ExecutableContext;
import org.antublue.test.engine.test.descriptor.ExecutableTestDescriptor;
import org.antublue.test.engine.test.descriptor.util.Filters;
import org.antublue.test.engine.test.descriptor.util.MethodInvoker;
import org.antublue.test.engine.test.descriptor.util.TestDescriptorUtils;
import org.antublue.test.engine.util.ReflectionUtils;
import org.antublue.test.engine.util.StandardStreams;
import org.antublue.test.engine.util.StopWatch;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.MethodSource;

/** Class to implement a ParameterMethodTestDescriptor */
public class StandardMethodTestDescriptor
        extends org.junit.platform.engine.support.descriptor.AbstractTestDescriptor
        implements ExecutableTestDescriptor {

    private static final ReflectionUtils REFLECTION_UTILS = ReflectionUtils.getSingleton();
    private static final TestDescriptorUtils TEST_DESCRIPTOR_UTILS =
            TestDescriptorUtils.getSingleton();

    private final ExecutableContext executableContext;
    private final Method testMethod;
    private final StopWatch stopWatch;

    /** Constructor */
    public StandardMethodTestDescriptor(
            EngineDiscoveryRequest engineDiscoveryRequest,
            ExecutableContext executableContext,
            UniqueId parentUniqueId,
            Method testMethod) {
        super(
                parentUniqueId.append(
                        StandardMethodTestDescriptor.class.getSimpleName(), testMethod.getName()),
                testMethod.getName());
        this.executableContext = executableContext;
        this.testMethod = testMethod;
        this.stopWatch = new StopWatch();

        initialize(engineDiscoveryRequest);
    }

    @Override
    public Type getType() {
        return Type.TEST;
    }

    @Override
    public Optional<TestSource> getSource() {
        return Optional.of(MethodSource.from(testMethod));
    }

    /**
     * Method to initialize the test descriptor
     *
     * @param engineDiscoveryRequest engineDiscoveryRequest
     */
    private void initialize(EngineDiscoveryRequest engineDiscoveryRequest) {
        // DO NOTHING
    }

    @Override
    public StopWatch getStopWatch() {
        return stopWatch;
    }

    @Override
    public void execute(ExecutionRequest executionRequest) {
        EngineExecutionListener engineExecutionListener =
                executionRequest.getEngineExecutionListener();
        engineExecutionListener.executionStarted(this);

        try {
            Class<?> testClass = executableContext.testClass;
            Object testInstance = executableContext.testInstance;

            List<Method> beforeEachMethods =
                    REFLECTION_UTILS.findMethods(testClass, Filters.BEFORE_EACH_METHOD);
            TEST_DESCRIPTOR_UTILS.sortMethods(beforeEachMethods, TestDescriptorUtils.Sort.FORWARD);
            for (Method method : beforeEachMethods) {
                MethodInvoker.invoke(method, testInstance, null);
            }

            testMethod.invoke(testInstance, (Object[]) null);

            List<Method> afterEachMethods =
                    REFLECTION_UTILS.findMethods(testClass, Filters.AFTER_EACH_METHOD);
            TEST_DESCRIPTOR_UTILS.sortMethods(afterEachMethods, TestDescriptorUtils.Sort.REVERSE);
            for (Method method : afterEachMethods) {
                MethodInvoker.invoke(method, testInstance, null);
            }

            engineExecutionListener.executionFinished(this, TestExecutionResult.successful());
        } catch (Throwable t) {
            engineExecutionListener.executionFinished(this, TestExecutionResult.aborted(t));
        }

        StandardStreams.flush();
    }
}
