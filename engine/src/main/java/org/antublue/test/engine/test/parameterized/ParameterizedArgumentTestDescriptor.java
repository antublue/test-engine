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

package org.antublue.test.engine.test.parameterized;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import org.antublue.test.engine.api.Argument;
import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.test.ExecutableContext;
import org.antublue.test.engine.test.ExecutableMetadata;
import org.antublue.test.engine.test.ExecutableMetadataConstants;
import org.antublue.test.engine.test.ExecutableMetadataSupport;
import org.antublue.test.engine.test.ExecutableTestDescriptor;
import org.antublue.test.engine.test.util.AutoCloseProcessor;
import org.antublue.test.engine.test.util.LockProcessor;
import org.antublue.test.engine.test.util.MethodInvoker;
import org.antublue.test.engine.test.util.RandomFieldInjector;
import org.antublue.test.engine.test.util.TestDescriptorUtils;
import org.antublue.test.engine.util.Invariant;
import org.antublue.test.engine.util.Invocation;
import org.antublue.test.engine.util.ReflectionUtils;
import org.antublue.test.engine.util.StandardStreams;
import org.antublue.test.engine.util.StopWatch;
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
        implements ExecutableTestDescriptor, ExecutableMetadataSupport {

    private static final ReflectionUtils REFLECTION_UTILS = ReflectionUtils.getSingleton();

    private static final TestDescriptorUtils TEST_DESCRIPTOR_UTILS =
            TestDescriptorUtils.getSingleton();

    private static final ParameterizedUtils PARAMETERIZED_UTILS = ParameterizedUtils.getSingleton();

    private static final LockProcessor LOCK_PROCESSOR = LockProcessor.getSingleton();

    private final Class<?> testClass;
    private final Method testArgumentSupplierMethod;
    private final Argument testArgument;
    private final StopWatch stopWatch;
    private final ExecutableMetadata executableMetadata;

    /**
     * Constructor
     *
     * @param uniqueId uniqueId
     * @param testClass testClass
     * @param testArgumentSupplierMethod testArgumentSupplierMethod
     * @param testArgument testArgument
     */
    private ParameterizedArgumentTestDescriptor(
            UniqueId uniqueId,
            Class<?> testClass,
            Method testArgumentSupplierMethod,
            Argument testArgument) {
        super(uniqueId, testArgument.name());
        this.testClass = testClass;
        this.testArgumentSupplierMethod = testArgumentSupplierMethod;
        this.testArgument = testArgument;
        this.stopWatch = new StopWatch();
        this.executableMetadata = new ExecutableMetadata();
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
    public ExecutableMetadata getExecutableMetadata() {
        return executableMetadata;
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

        Object testInstance = executableContext.get(ParameterizedExecutableConstants.TEST_INSTANCE);
        Invariant.check(testInstance != null);

        executableMetadata.put(ExecutableMetadataConstants.TEST_CLASS, testClass);
        executableMetadata.put(ExecutableMetadataConstants.TEST_ARGUMENT, testArgument);

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
            executableMetadata.put(
                    ExecutableMetadataConstants.TEST_DESCRIPTOR_ELAPSED_TIME,
                    stopWatch.elapsedTime());
            executableMetadata.put(
                    ExecutableMetadataConstants.TEST_DESCRIPTOR_STATUS,
                    ExecutableMetadataConstants.SKIP);
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
        executableMetadata.put(
                ExecutableMetadataConstants.TEST_DESCRIPTOR_ELAPSED_TIME, stopWatch.elapsedTime());

        if (executableContext.hasThrowables()) {
            executableMetadata.put(
                    ExecutableMetadataConstants.TEST_DESCRIPTOR_STATUS,
                    ExecutableMetadataConstants.FAIL);
            engineExecutionListener.executionFinished(
                    this, TestExecutionResult.failed(executableContext.getThrowables().get(0)));
        } else {
            executableMetadata.put(
                    ExecutableMetadataConstants.TEST_DESCRIPTOR_STATUS,
                    ExecutableMetadataConstants.PASS);
            engineExecutionListener.executionFinished(this, TestExecutionResult.successful());
        }

        StandardStreams.flush();
    }

    public static class Builder {

        private TestDescriptor parentTestDescriptor;
        private Class<?> testClass;
        private Method testArgumentSupplierMethod;
        private Argument testArgument;

        public Builder withParentTestDescriptor(TestDescriptor parentTestDescriptor) {
            this.parentTestDescriptor = parentTestDescriptor;
            return this;
        }

        public Builder withTestClass(Class<?> testClass) {
            this.testClass = testClass;
            return this;
        }

        public Builder withTestArgumentSupplierMethod(Method testArgumentSupplierMethod) {
            this.testArgumentSupplierMethod = testArgumentSupplierMethod;
            return this;
        }

        public Builder withTestArgument(Argument testArgument) {
            this.testArgument = testArgument;
            return this;
        }

        public TestDescriptor build() {
            try {
                UniqueId testDescriptorUniqueId =
                        parentTestDescriptor
                                .getUniqueId()
                                .append(
                                        ParameterizedArgumentTestDescriptor.class.getName(),
                                        testArgument.name());
                TestDescriptor testDescriptor =
                        new ParameterizedArgumentTestDescriptor(
                                testDescriptorUniqueId,
                                testClass,
                                testArgumentSupplierMethod,
                                testArgument);
                parentTestDescriptor.addChild(testDescriptor);

                List<Method> testMethods =
                        REFLECTION_UTILS.findMethods(testClass, ParameterizedFilters.TEST_METHOD);
                TEST_DESCRIPTOR_UTILS.sortMethods(testMethods, TestDescriptorUtils.Sort.FORWARD);
                for (Method testMethod : testMethods) {
                    testDescriptor.addChild(
                            new ParameterizedMethodTestDescriptor.Builder()
                                    .withParentTestDescriptor(testDescriptor)
                                    .withTestClass(testClass)
                                    .withTestArgument(testArgument)
                                    .withTestMethod(testMethod)
                                    .build());
                }

                return testDescriptor;
            } catch (RuntimeException e) {
                throw e;
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
        }
    }
}
