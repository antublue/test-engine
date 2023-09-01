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

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
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
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;
import org.junit.platform.engine.support.descriptor.ClassSource;

/** Class to implement a ParameterClassTestDescriptor */
public class ParameterizedClassTestDescriptor extends AbstractTestDescriptor
        implements ExecutableTestDescriptor, Metadata {

    private static final ReflectionUtils REFLECTION_UTILS = ReflectionUtils.getSingleton();

    private static final TestDescriptorUtils TEST_DESCRIPTOR_UTILS =
            TestDescriptorUtils.getSingleton();

    private static final ParameterizedUtils PARAMETERIZED_UTILS = ParameterizedUtils.getSingleton();

    private static final LockProcessor LOCK_PROCESSOR = LockProcessor.getSingleton();

    private final Class<?> testClass;
    private final ExecutableContext executableContext;
    private final StopWatch stopWatch;
    private final Map<String, String> properties;

    /**
     * Constructor
     *
     * @param engineDiscoveryRequest engineDiscoveryRequest
     * @param parentUniqueId parentUniqueId
     * @param testClass testClass
     */
    public ParameterizedClassTestDescriptor(
            EngineDiscoveryRequest engineDiscoveryRequest,
            ExecutableContext executableContext,
            UniqueId parentUniqueId,
            Class<?> testClass) {
        super(
                parentUniqueId.append(
                        ParameterizedClassTestDescriptor.class.getSimpleName(),
                        testClass.getName()),
                TEST_DESCRIPTOR_UTILS.getDisplayName(testClass));
        this.executableContext = executableContext;
        this.testClass = testClass;
        this.stopWatch = new StopWatch();
        this.properties = new LinkedHashMap<>();

        initialize(engineDiscoveryRequest);
    }

    @Override
    public Optional<TestSource> getSource() {
        return Optional.of(ClassSource.from(testClass));
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
            executableContext.testClass = testClass;

            executableContext.argumentSupplierMethod =
                    ParameterizedUtils.getSingleton().getArumentSupplierMethod(testClass);

            PARAMETERIZED_UTILS
                    .getArguments(testClass)
                    .forEach(
                            testArgument ->
                                    addChild(
                                            new ParameterizedArgumentTestDescriptor(
                                                    engineDiscoveryRequest,
                                                    executableContext,
                                                    getUniqueId(),
                                                    testArgument)));
        } catch (RuntimeException e) {
            throw e;
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    @Override
    public void execute(ExecutionRequest executionRequest) {
        properties.put("testClass", testClass.getName());

        EngineExecutionListener engineExecutionListener =
                executionRequest.getEngineExecutionListener();
        engineExecutionListener.executionStarted(this);

        try {
            stopWatch.start();

            Constructor<?> constructor = testClass.getDeclaredConstructor((Class<?>[]) null);
            Object testInstance = constructor.newInstance((Object[]) null);
            executableContext.testInstance = testInstance;

            List<Method> prepareMethods =
                    REFLECTION_UTILS.findMethods(testClass, Filters.PREPARE_METHOD);
            TEST_DESCRIPTOR_UTILS.sortMethods(prepareMethods, TestDescriptorUtils.Sort.FORWARD);
            for (Method method : prepareMethods) {
                try {
                    LOCK_PROCESSOR.processLocks(method);
                    MethodInvoker.invoke(method, testInstance, null);
                } finally {
                    LOCK_PROCESSOR.processUnlocks(method);
                }
            }

            getChildren()
                    .forEach(
                            (Consumer<TestDescriptor>)
                                    testDescriptor -> {
                                        if (testDescriptor instanceof ExecutableTestDescriptor) {
                                            ((ExecutableTestDescriptor) testDescriptor)
                                                    .execute(executionRequest);
                                        }
                                    });

            List<Method> concludeMethods =
                    REFLECTION_UTILS.findMethods(testClass, Filters.CONCLUDE_METHOD);
            TEST_DESCRIPTOR_UTILS.sortMethods(concludeMethods, TestDescriptorUtils.Sort.REVERSE);
            for (Method method : concludeMethods) {
                try {
                    LOCK_PROCESSOR.processLocks(method);
                    MethodInvoker.invoke(method, testInstance, null);
                } finally {
                    LOCK_PROCESSOR.processUnlocks(method);
                }
            }

            // Fine @TestEngine.AutoClose Fields and close them
            List<Field> fields = REFLECTION_UTILS.findFields(testClass, Filters.AUTO_CLOSE_FIELDS);
            for (Field field : fields) {
                TestEngine.AutoClose annotation = field.getAnnotation(TestEngine.AutoClose.class);
                if ("@TestEngine.Conclude".equals(annotation.lifecycle())) {
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
        } finally {
            executableContext.testInstance = null;
        }

        StandardStreams.flush();
    }
}
