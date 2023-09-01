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
import java.util.function.Consumer;
import org.antublue.test.engine.api.Argument;
import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.test.descriptor.ExecutableContext;
import org.antublue.test.engine.test.descriptor.ExecutableTestDescriptor;
import org.antublue.test.engine.test.descriptor.Metadata;
import org.antublue.test.engine.test.descriptor.util.AutoCloseProcessor;
import org.antublue.test.engine.test.descriptor.util.Filters;
import org.antublue.test.engine.test.descriptor.util.LockProcessor;
import org.antublue.test.engine.test.descriptor.util.MethodInvoker;
import org.antublue.test.engine.test.descriptor.util.RandomFieldInjector;
import org.antublue.test.engine.test.descriptor.util.TestDescriptorUtils;
import org.antublue.test.engine.util.ReflectionUtils;
import org.antublue.test.engine.util.StandardStreams;
import org.antublue.test.engine.util.StopWatch;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;
import org.junit.platform.engine.support.descriptor.MethodSource;

/** Class to implement a ParameterArgumentTestDescriptor */
public class ParameterizedArgumentTestDescriptor extends AbstractTestDescriptor
        implements ExecutableTestDescriptor, Metadata {

    private static final ReflectionUtils REFLECTION_UTILS = ReflectionUtils.getSingleton();

    private static final TestDescriptorUtils TEST_DESCRIPTOR_UTILS =
            TestDescriptorUtils.getSingleton();

    private static final LockProcessor LOCK_PROCESSOR = LockProcessor.getSingleton();

    private final Argument testArgument;
    private final ExecutableContext executableContext;
    private final StopWatch stopWatch;
    private final Map<String, String> properties;

    /**
     * Constructor
     *
     * @param engineDiscoveryRequest engineDiscoveryRequest
     * @param parentUniqueId parentUniqueId
     * @param testArgument testArgument
     */
    public ParameterizedArgumentTestDescriptor(
            EngineDiscoveryRequest engineDiscoveryRequest,
            ExecutableContext executableContext,
            UniqueId parentUniqueId,
            Argument testArgument) {
        super(
                parentUniqueId.append(
                        ParameterizedArgumentTestDescriptor.class.getSimpleName(),
                        testArgument.name()),
                testArgument.name());
        this.executableContext = executableContext;
        this.testArgument = testArgument;
        this.stopWatch = new StopWatch();
        this.properties = new LinkedHashMap<>();

        initialize(engineDiscoveryRequest);
    }

    @Override
    public Optional<TestSource> getSource() {
        return Optional.of(MethodSource.from(executableContext.argumentSupplierMethod));
    }

    @Override
    public Type getType() {
        return Type.CONTAINER_AND_TEST;
    }

    @Override
    public Map<String, String> getMetadata() {
        return properties;
    }

    /**
     * Method to initialize the test descriptor
     *
     * @param engineDiscoveryRequest engineDiscoveryRequest
     */
    private void initialize(EngineDiscoveryRequest engineDiscoveryRequest) {
        try {
            Class<?> testClass = executableContext.testClass;

            List<Method> methods =
                    REFLECTION_UTILS.findMethods(testClass, Filters.PARAMETERIZED_TEST_METHOD);
            TEST_DESCRIPTOR_UTILS.sortMethods(methods, TestDescriptorUtils.Sort.FORWARD);

            methods.forEach(
                    testMethod ->
                            addChild(
                                    new ParameterizedMethodTestDescriptor(
                                            engineDiscoveryRequest,
                                            executableContext,
                                            getUniqueId(),
                                            testMethod,
                                            testArgument)));
        } catch (RuntimeException e) {
            throw e;
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    @Override
    public void execute(ExecutionRequest executionRequest) {
        properties.put("testClass", executableContext.testClass.getName());
        properties.put("testArgument", testArgument.name());

        EngineExecutionListener engineExecutionListener =
                executionRequest.getEngineExecutionListener();
        engineExecutionListener.executionStarted(this);

        try {
            stopWatch.start();

            Class<?> testClass = executableContext.testClass;
            Object testInstance = executableContext.testInstance;

            // Find @TestEngine.Argument Fields and set the argument
            List<Field> fields = REFLECTION_UTILS.findFields(testClass, Filters.ARGUMENT_FIELD);
            for (Field field : fields) {
                field.set(testInstance, testArgument);
            }

            // Find @TestEngine.RandomX fields and inject a random value
            fields = REFLECTION_UTILS.findFields(testClass, Filters.RANDOM_FIELD);
            for (Field field : fields) {
                RandomFieldInjector.singleton().inject(testInstance, field);
            }

            // Find @TestEngine.BeforeAll Methods and invoke them
            List<Method> beforeAllMethods =
                    REFLECTION_UTILS.findMethods(testClass, Filters.BEFORE_ALL_METHOD);
            TEST_DESCRIPTOR_UTILS.sortMethods(beforeAllMethods, TestDescriptorUtils.Sort.FORWARD);
            for (Method method : beforeAllMethods) {
                try {
                    LOCK_PROCESSOR.processLocks(method);
                    MethodInvoker.invoke(method, testInstance, testArgument);
                } finally {
                    LOCK_PROCESSOR.processUnlocks(method);
                }
            }

            // Execute child test descriptor
            getChildren()
                    .forEach(
                            (Consumer<TestDescriptor>)
                                    testDescriptor -> {
                                        if (testDescriptor instanceof ExecutableTestDescriptor) {
                                            ((ExecutableTestDescriptor) testDescriptor)
                                                    .execute(executionRequest);
                                        }
                                    });

            // Find @TestEngine.AfterAll Methods and invoke them
            List<Method> afterAllMethods =
                    REFLECTION_UTILS.findMethods(testClass, Filters.AFTER_ALL_METHOD);
            TEST_DESCRIPTOR_UTILS.sortMethods(afterAllMethods, TestDescriptorUtils.Sort.REVERSE);
            for (Method method : afterAllMethods) {
                try {
                    LOCK_PROCESSOR.processLocks(method);
                    MethodInvoker.invoke(method, testInstance, testArgument);
                } finally {
                    LOCK_PROCESSOR.processUnlocks(method);
                }
            }

            // Fine @TestEngine.AutoClose Fields and close them
            fields = REFLECTION_UTILS.findFields(testClass, Filters.AUTO_CLOSE_FIELDS);
            for (Field field : fields) {
                TestEngine.AutoClose annotation = field.getAnnotation(TestEngine.AutoClose.class);
                if ("@TestEngine.AfterAll".equals(annotation.lifecycle())) {
                    AutoCloseProcessor.singleton().close(testInstance, field);
                }
            }

            // Find @TestEngine.Argument Fields and set the argument to null
            fields = REFLECTION_UTILS.findFields(testClass, Filters.ARGUMENT_FIELD);
            for (Field field : fields) {
                field.set(testInstance, null);
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
