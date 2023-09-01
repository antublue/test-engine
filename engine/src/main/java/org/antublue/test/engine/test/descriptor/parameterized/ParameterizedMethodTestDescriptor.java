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

package org.antublue.test.engine.test.descriptor.parameterized;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.antublue.test.engine.api.Argument;
import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.test.descriptor.ExecutableContext;
import org.antublue.test.engine.test.descriptor.ExecutableTestDescriptor;
import org.antublue.test.engine.test.descriptor.Metadata;
import org.antublue.test.engine.test.descriptor.util.AutoCloseProcessor;
import org.antublue.test.engine.test.descriptor.util.Filters;
import org.antublue.test.engine.test.descriptor.util.LockProcessor;
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
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;
import org.junit.platform.engine.support.descriptor.MethodSource;

/** Class to implement a ParameterMethodTestDescriptor */
public class ParameterizedMethodTestDescriptor extends AbstractTestDescriptor
        implements ExecutableTestDescriptor, Metadata {

    private static final ReflectionUtils REFLECTION_UTILS = ReflectionUtils.getSingleton();

    private static final TestDescriptorUtils TEST_DESCRIPTOR_UTILS =
            TestDescriptorUtils.getSingleton();

    private static final LockProcessor LOCK_PROCESSOR = LockProcessor.getSingleton();

    private final ExecutableContext executableContext;
    private final Method testMethod;
    private final Argument testArgument;
    private final StopWatch stopWatch;
    private final Map<String, String> properties;

    /** Constructor */
    public ParameterizedMethodTestDescriptor(
            EngineDiscoveryRequest engineDiscoveryRequest,
            ExecutableContext executableContext,
            UniqueId parentUniqueId,
            Method testMethod,
            Argument testArgument) {
        super(
                parentUniqueId.append(
                        ParameterizedMethodTestDescriptor.class.getSimpleName(),
                        testMethod.getName()),
                TEST_DESCRIPTOR_UTILS.getDisplayName(testMethod));
        this.executableContext = executableContext;
        this.testMethod = testMethod;
        this.testArgument = testArgument;
        this.stopWatch = new StopWatch();
        this.properties = new LinkedHashMap<>();
    }

    @Override
    public Type getType() {
        return Type.TEST;
    }

    @Override
    public Optional<TestSource> getSource() {
        return Optional.of(MethodSource.from(testMethod));
    }

    @Override
    public Map<String, String> getMetadata() {
        return properties;
    }

    @Override
    public void execute(ExecutionRequest executionRequest) {
        properties.put("testClass", executableContext.testClass.getName());
        properties.put("testArgument", testArgument.name());
        properties.put("testMethod", testMethod.getName());

        EngineExecutionListener engineExecutionListener =
                executionRequest.getEngineExecutionListener();
        engineExecutionListener.executionStarted(this);

        try {
            stopWatch.start();

            Class<?> testClass = executableContext.testClass;
            Object testInstance = executableContext.testInstance;

            // Find @TestEngine.BeforeEach Methods and invoke them
            List<Method> beforeEachMethods =
                    REFLECTION_UTILS.findMethods(
                            testInstance.getClass(), Filters.BEFORE_EACH_METHOD);
            TEST_DESCRIPTOR_UTILS.sortMethods(beforeEachMethods, TestDescriptorUtils.Sort.FORWARD);
            for (Method method : beforeEachMethods) {
                try {
                    LOCK_PROCESSOR.processLocks(method);
                    MethodInvoker.invoke(method, testInstance, testArgument);
                } finally {
                    LOCK_PROCESSOR.processUnlocks(method);
                }
            }

            try {
                LOCK_PROCESSOR.processLocks(testMethod);
                MethodInvoker.invoke(testMethod, testInstance, testArgument);
            } finally {
                LOCK_PROCESSOR.processUnlocks(testMethod);
            }

            // Find @TestEngine.AfterEach Methods and invoke them
            List<Method> afterEachMethods =
                    REFLECTION_UTILS.findMethods(
                            testInstance.getClass(), Filters.AFTER_EACH_METHOD);
            TEST_DESCRIPTOR_UTILS.sortMethods(afterEachMethods, TestDescriptorUtils.Sort.REVERSE);
            for (Method method : afterEachMethods) {
                try {
                    LOCK_PROCESSOR.processLocks(method);
                    MethodInvoker.invoke(method, testInstance, testArgument);
                } finally {
                    LOCK_PROCESSOR.processUnlocks(method);
                }
            }

            // Fine @TestEngine.AutoClose Fields and close them
            List<Field> fields = REFLECTION_UTILS.findFields(testClass, Filters.AUTO_CLOSE_FIELDS);
            for (Field field : fields) {
                TestEngine.AutoClose annotation = field.getAnnotation(TestEngine.AutoClose.class);
                if ("@TestEngine.AfterEach".equals(annotation.lifecycle())) {
                    AutoCloseProcessor.singleton().close(testInstance, field);
                }
            }

            stopWatch.stop();
            properties.put("elapsedTime", String.valueOf(stopWatch.elapsedTime()));
            properties.put("status", "PASS");

            engineExecutionListener.executionFinished(this, TestExecutionResult.successful());
        } catch (Throwable t) {
            stopWatch.stop();
            properties.put("elapsedTime", String.valueOf(stopWatch.elapsedTime()));
            properties.put("status", "FAIL");

            engineExecutionListener.executionFinished(this, TestExecutionResult.aborted(t));
        }

        StandardStreams.flush();
    }
}
