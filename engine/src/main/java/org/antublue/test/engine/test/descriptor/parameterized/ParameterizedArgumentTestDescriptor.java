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
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import org.antublue.test.engine.api.Argument;
import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.test.descriptor.ExecutableContext;
import org.antublue.test.engine.test.descriptor.ExecutableTestDescriptor;
import org.antublue.test.engine.test.descriptor.Metadata;
import org.antublue.test.engine.test.descriptor.MetadataConstants;
import org.antublue.test.engine.test.descriptor.MetadataSupport;
import org.antublue.test.engine.test.descriptor.util.AutoCloseProcessor;
import org.antublue.test.engine.test.descriptor.util.LockProcessor;
import org.antublue.test.engine.test.descriptor.util.MethodInvoker;
import org.antublue.test.engine.test.descriptor.util.RandomFieldInjector;
import org.antublue.test.engine.test.descriptor.util.TestDescriptorUtils;
import org.antublue.test.engine.util.Invariant;
import org.antublue.test.engine.util.Invocation;
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
@SuppressWarnings("unchecked")
public class ParameterizedArgumentTestDescriptor extends AbstractTestDescriptor
        implements ExecutableTestDescriptor, MetadataSupport {

    private static final ReflectionUtils REFLECTION_UTILS = ReflectionUtils.getSingleton();

    private static final TestDescriptorUtils TEST_DESCRIPTOR_UTILS =
            TestDescriptorUtils.getSingleton();

    private static final LockProcessor LOCK_PROCESSOR = LockProcessor.getSingleton();

    private final Argument testArgument;
    private final StopWatch stopWatch;
    private final Metadata metadata;

    private Method testArgumentSupplierMethod;

    /**
     * Constructor
     *
     * @param engineDiscoveryRequest engineDiscoveryRequest
     * @param parentUniqueId parentUniqueId
     * @param testArgument testArgument
     */
    public ParameterizedArgumentTestDescriptor(
            EngineDiscoveryRequest engineDiscoveryRequest,
            UniqueId parentUniqueId,
            Argument testArgument) {
        super(
                parentUniqueId.append(
                        ParameterizedArgumentTestDescriptor.class.getSimpleName(),
                        testArgument.name()),
                testArgument.name());
        this.testArgument = testArgument;
        this.stopWatch = new StopWatch();
        this.metadata = new Metadata();
    }

    @Override
    public Optional<TestSource> getSource() {
        return Optional.of(MethodSource.from(testArgumentSupplierMethod));
    }

    @Override
    public Type getType() {
        return Type.CONTAINER_AND_TEST;
    }

    @Override
    public Metadata getMetadata() {
        return metadata;
    }

    /**
     * Method to build the test descriptor
     *
     * @param engineDiscoveryRequest engineDiscoveryRequest
     * @param executableContext buildContext
     */
    public void build(
            EngineDiscoveryRequest engineDiscoveryRequest, ExecutableContext executableContext) {
        try {
            testArgumentSupplierMethod =
                    executableContext.get(
                            ParameterizedExecutableConstants.TEST_CLASS_ARGUMENT_SUPPLIER_METHOD);
            Class<?> testClass = executableContext.get(ParameterizedExecutableConstants.TEST_CLASS);

            Invariant.check(testClass != null);
            Invariant.check(testArgumentSupplierMethod != null);

            List<Method> methods =
                    REFLECTION_UTILS.findMethods(testClass, ParameterizedFilters.TEST_METHOD);

            TEST_DESCRIPTOR_UTILS.sortMethods(methods, TestDescriptorUtils.Sort.FORWARD);

            methods.forEach(
                    testMethod ->
                            addChild(
                                    new ParameterizedMethodTestDescriptor(
                                            engineDiscoveryRequest,
                                            getUniqueId(),
                                            testMethod,
                                            testArgument)));
        } catch (RuntimeException e) {
            throw e;
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    private enum State {
        RUN_SET_ARGUMENT_FIELDS,
        RUN_SET_RANDOM_FIELDS,
        RUN_BEFORE_ALL_METHODS,
        RUN_EXECUTE,
        RUN_SKIP,
        RUN_AFTER_ALL_METHODS,
        RUN_AUTO_CLOSE_FIELDS,
        RUN_SET_ARGUMENT_FIELDS_NULL
    }

    @Override
    public void execute(ExecutionRequest executionRequest, ExecutableContext executableContext) {
        stopWatch.start();

        executableContext.put(ParameterizedExecutableConstants.TEST_ARGUMENT, testArgument);

        Class<?> testClass = executableContext.get(ParameterizedExecutableConstants.TEST_CLASS);
        Object testInstance = executableContext.get(ParameterizedExecutableConstants.TEST_INSTANCE);

        Invariant.check(testClass != null);
        Invariant.check(testInstance != null);

        metadata.put(MetadataConstants.TEST_CLASS, testClass);
        metadata.put(MetadataConstants.TEST_ARGUMENT, testArgument);

        EngineExecutionListener engineExecutionListener =
                executionRequest.getEngineExecutionListener();

        if (executableContext.hasThrowables()) {
            getChildren()
                    .forEach(
                            (Consumer<TestDescriptor>)
                                    testDescriptor -> {
                                        if (testDescriptor instanceof ExecutableTestDescriptor) {
                                            ((ExecutableTestDescriptor) testDescriptor)
                                                    .execute(executionRequest, executableContext);
                                        }
                                    });

            stopWatch.stop();
            metadata.put(MetadataConstants.TEST_DESCRIPTOR_ELAPSED_TIME, stopWatch.elapsedTime());
            metadata.put(MetadataConstants.TEST_DESCRIPTOR_STATUS, MetadataConstants.SKIP);
            engineExecutionListener.executionSkipped(this, "");
            return;
        }

        engineExecutionListener.executionStarted(this);

        AtomicReference<State> state = new AtomicReference<>(State.RUN_SET_ARGUMENT_FIELDS);

        if (state.get() == State.RUN_SET_ARGUMENT_FIELDS) {
            Invocation.execute(
                    () -> {
                        try {
                            List<Field> fields =
                                    REFLECTION_UTILS.findFields(
                                            testClass, ParameterizedFilters.ARGUMENT_FIELD);
                            for (Field field : fields) {
                                field.set(testInstance, testArgument);
                            }
                            state.set(State.RUN_SET_RANDOM_FIELDS);
                        } catch (Throwable t) {
                            executableContext.addAndProcessThrowable(testClass, t);
                            state.set(State.RUN_SKIP);
                        } finally {
                            StandardStreams.flush();
                        }
                    });
        }

        if (state.get() == State.RUN_SET_RANDOM_FIELDS) {
            Invocation.execute(
                    () -> {
                        try {
                            List<Field> fields =
                                    REFLECTION_UTILS.findFields(
                                            testClass, ParameterizedFilters.RANDOM_FIELD);
                            for (Field field : fields) {
                                RandomFieldInjector.inject(testInstance, field);
                            }
                            state.set(State.RUN_BEFORE_ALL_METHODS);
                        } catch (Throwable t) {
                            executableContext.addAndProcessThrowable(testClass, t);
                            state.set(State.RUN_SKIP);
                        } finally {
                            StandardStreams.flush();
                        }
                    });
        }

        if (state.get() == State.RUN_BEFORE_ALL_METHODS) {
            Invocation.execute(
                    () -> {
                        try {
                            List<Method> beforeAllMethods =
                                    REFLECTION_UTILS.findMethods(
                                            testClass, ParameterizedFilters.BEFORE_ALL_METHOD);
                            TEST_DESCRIPTOR_UTILS.sortMethods(
                                    beforeAllMethods, TestDescriptorUtils.Sort.FORWARD);
                            for (Method method : beforeAllMethods) {
                                try {
                                    LOCK_PROCESSOR.processLocks(method);
                                    MethodInvoker.invoke(method, testInstance, testArgument);
                                } finally {
                                    LOCK_PROCESSOR.processUnlocks(method);
                                }
                            }
                            state.set(State.RUN_EXECUTE);
                        } catch (Throwable t) {
                            executableContext.addAndProcessThrowable(testClass, t);
                            state.set(State.RUN_EXECUTE);
                        } finally {
                            StandardStreams.flush();
                        }
                    });
        }

        if (state.get() == State.RUN_EXECUTE) {
            getChildren()
                    .forEach(
                            (Consumer<TestDescriptor>)
                                    testDescriptor -> {
                                        if (testDescriptor instanceof ExecutableTestDescriptor) {
                                            ((ExecutableTestDescriptor) testDescriptor)
                                                    .execute(executionRequest, executableContext);
                                        }
                                    });

            state.set(State.RUN_AFTER_ALL_METHODS);
            StandardStreams.flush();
        }

        if (state.get() == State.RUN_AFTER_ALL_METHODS) {
            Invocation.execute(
                    () -> {
                        List<Method> afterAllMethods =
                                REFLECTION_UTILS.findMethods(
                                        testClass, ParameterizedFilters.AFTER_ALL_METHOD);
                        TEST_DESCRIPTOR_UTILS.sortMethods(
                                afterAllMethods, TestDescriptorUtils.Sort.REVERSE);
                        for (Method method : afterAllMethods) {
                            try {
                                LOCK_PROCESSOR.processLocks(method);
                                MethodInvoker.invoke(method, testInstance, testArgument);
                            } catch (Throwable t) {
                                executableContext.addAndProcessThrowable(testClass, t);
                            } finally {
                                LOCK_PROCESSOR.processUnlocks(method);
                                StandardStreams.flush();
                            }
                        }
                        state.set(State.RUN_AUTO_CLOSE_FIELDS);
                    });
        }

        if (state.get() == State.RUN_AUTO_CLOSE_FIELDS) {
            Invocation.execute(
                    () -> {
                        List<Field> fields =
                                REFLECTION_UTILS.findFields(
                                        testClass, ParameterizedFilters.AUTO_CLOSE_FIELDS);
                        for (Field field : fields) {
                            TestEngine.AutoClose annotation =
                                    field.getAnnotation(TestEngine.AutoClose.class);
                            if ("@TestEngine.AfterAll".equals(annotation.lifecycle())) {
                                try {
                                    AutoCloseProcessor.close(testInstance, field);
                                } catch (Throwable t) {
                                    executableContext.addAndProcessThrowable(testClass, t);
                                } finally {
                                    StandardStreams.flush();
                                }
                            }
                        }
                        state.set(State.RUN_SET_ARGUMENT_FIELDS_NULL);
                    });
        }

        if (state.get() == State.RUN_SET_ARGUMENT_FIELDS_NULL) {
            Invocation.execute(
                    () -> {
                        List<Field> fields =
                                REFLECTION_UTILS.findFields(
                                        testClass, ParameterizedFilters.ARGUMENT_FIELD);
                        for (Field field : fields) {
                            try {
                                field.set(testInstance, null);
                            } catch (Throwable t) {
                                executableContext.addAndProcessThrowable(testClass, t);
                            } finally {
                                StandardStreams.flush();
                            }
                        }
                    });
        }

        stopWatch.stop();
        metadata.put(MetadataConstants.TEST_DESCRIPTOR_ELAPSED_TIME, stopWatch.elapsedTime());

        if (executableContext.hasThrowables()) {
            metadata.put(MetadataConstants.TEST_DESCRIPTOR_STATUS, MetadataConstants.FAIL);
            engineExecutionListener.executionFinished(
                    this, TestExecutionResult.failed(executableContext.getThrowables().get(0)));
        } else {
            metadata.put(MetadataConstants.TEST_DESCRIPTOR_STATUS, MetadataConstants.PASS);
            engineExecutionListener.executionFinished(this, TestExecutionResult.successful());
        }

        StandardStreams.flush();
    }
}
