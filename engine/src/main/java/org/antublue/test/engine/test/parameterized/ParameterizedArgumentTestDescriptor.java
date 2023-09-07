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
import java.util.function.Predicate;
import org.antublue.test.engine.api.Argument;
import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.test.ExecutableContext;
import org.antublue.test.engine.test.ExecutableMetadata;
import org.antublue.test.engine.test.ExecutableMetadataConstants;
import org.antublue.test.engine.test.ExecutableMetadataSupport;
import org.antublue.test.engine.test.ExecutableTestDescriptor;
import org.antublue.test.engine.test.ThrowableContext;
import org.antublue.test.engine.test.extension.ExtensionProcessor;
import org.antublue.test.engine.test.util.AutoCloseProcessor;
import org.antublue.test.engine.test.util.LockProcessor;
import org.antublue.test.engine.test.util.MethodInvoker;
import org.antublue.test.engine.test.util.RandomFieldInjector;
import org.antublue.test.engine.test.util.TestUtils;
import org.antublue.test.engine.util.Invariant;
import org.antublue.test.engine.util.ReflectionUtils;
import org.antublue.test.engine.util.Singleton;
import org.antublue.test.engine.util.StandardStreams;
import org.antublue.test.engine.util.StopWatch;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;
import org.junit.platform.engine.support.descriptor.MethodSource;

/** Class to implement a ParameterArgumentTestDescriptor */
public class ParameterizedArgumentTestDescriptor extends AbstractTestDescriptor
        implements ExecutableTestDescriptor, ExecutableMetadataSupport {

    private static final ReflectionUtils REFLECTION_UTILS = Singleton.get(ReflectionUtils.class);

    private static final TestUtils TEST_DESCRIPTOR_UTILS = Singleton.get(TestUtils.class);

    private static final ExtensionProcessor EXTENSION_PROCESSOR =
            Singleton.get(ExtensionProcessor.class);

    private static final LockProcessor LOCK_PROCESSOR = Singleton.get(LockProcessor.class);

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
        SET_ARGUMENT_FIELDS,
        SET_RANDOM_FIELDS,
        BEFORE_ALL_METHODS,
        BEFORE_ALL_CALLBACK_METHODS,
        EXECUTE_OR_SKIP,
        SKIP,
        AFTER_ALL_METHODS,
        AFTER_ALL_CALLBACK_METHODS,
        SET_ARGUMENT_FIELDS_NULL,
        CLOSE_AUTO_CLOSE_FIELDS,
        END
    }

    @Override
    public void execute(ExecutableContext executableContext) {
        stopWatch.start();

        ThrowableContext throwableContext = executableContext.getThrowableContext();

        Object testInstance = executableContext.getTestInstance();
        Invariant.check(testInstance != null);

        executableMetadata.put(ExecutableMetadataConstants.TEST_CLASS, testClass);
        executableMetadata.put(ExecutableMetadataConstants.TEST_ARGUMENT, testArgument);

        if (!executableContext.getThrowableContext().isEmpty()) {
            getChildren()
                    .forEach(
                            (Consumer<TestDescriptor>)
                                    testDescriptor -> {
                                        if (testDescriptor instanceof ExecutableTestDescriptor) {
                                            ((ExecutableTestDescriptor) testDescriptor)
                                                    .execute(executableContext);
                                        }
                                    });

            stopWatch.stop();
            executableMetadata.put(
                    ExecutableMetadataConstants.TEST_DESCRIPTOR_ELAPSED_TIME,
                    stopWatch.elapsedTime());
            executableMetadata.put(
                    ExecutableMetadataConstants.TEST_DESCRIPTOR_STATUS,
                    ExecutableMetadataConstants.SKIP);
            executableContext
                    .getExecutionRequest()
                    .getEngineExecutionListener()
                    .executionSkipped(this, "");
            return;
        }

        executableContext.getExecutionRequest().getEngineExecutionListener().executionStarted(this);

        AtomicReference<State> state = new AtomicReference<>(State.SET_ARGUMENT_FIELDS);

        if (state.get() == State.SET_ARGUMENT_FIELDS) {
            try {
                List<Field> fields =
                        REFLECTION_UTILS.findFields(
                                testClass, ParameterizedTestFilters.ARGUMENT_FIELD);
                for (Field field : fields) {
                    field.set(testInstance, testArgument);
                }
                state.set(State.SET_RANDOM_FIELDS);
            } catch (Throwable t) {
                throwableContext.add(testClass, t);
                state.set(State.SKIP);
            } finally {
                StandardStreams.flush();
            }
        }

        if (state.get() == State.SET_RANDOM_FIELDS) {
            try {
                List<Field> fields =
                        REFLECTION_UTILS.findFields(
                                testClass, ParameterizedTestFilters.RANDOM_FIELD);
                for (Field field : fields) {
                    RandomFieldInjector.inject(testInstance, field);
                }
                state.set(State.BEFORE_ALL_METHODS);
            } catch (Throwable t) {
                throwableContext.add(testClass, t);
                state.set(State.SKIP);
            } finally {
                StandardStreams.flush();
            }
        }

        if (state.get() == State.BEFORE_ALL_METHODS) {
            try {
                List<Method> beforeAllMethods =
                        REFLECTION_UTILS.findMethods(
                                testClass, ParameterizedTestFilters.BEFORE_ALL_METHOD);
                TEST_DESCRIPTOR_UTILS.sortMethods(beforeAllMethods, TestUtils.Sort.FORWARD);
                for (Method method : beforeAllMethods) {
                    try {
                        LOCK_PROCESSOR.processLocks(method);
                        MethodInvoker.invoke(method, testInstance, testArgument);
                    } finally {
                        LOCK_PROCESSOR.processUnlocks(method);
                    }
                }
            } catch (Throwable t) {
                throwableContext.add(testClass, t);
            } finally {
                state.set(State.BEFORE_ALL_CALLBACK_METHODS);
                StandardStreams.flush();
            }
        }

        if (state.get() == State.BEFORE_ALL_CALLBACK_METHODS) {
            EXTENSION_PROCESSOR.beforeAllCallbacks(
                    testClass, testArgument, testInstance, executableContext.getThrowableContext());
            state.set(State.EXECUTE_OR_SKIP);
        }

        if (state.get() == State.EXECUTE_OR_SKIP) {
            getChildren()
                    .forEach(
                            (Consumer<TestDescriptor>)
                                    testDescriptor -> {
                                        if (testDescriptor instanceof ExecutableTestDescriptor) {
                                            ((ExecutableTestDescriptor) testDescriptor)
                                                    .execute(executableContext);
                                        }
                                    });

            state.set(State.AFTER_ALL_METHODS);
            StandardStreams.flush();
        }

        if (state.get() == State.AFTER_ALL_METHODS) {
            List<Method> afterAllMethods =
                    REFLECTION_UTILS.findMethods(
                            testClass, ParameterizedTestFilters.AFTER_ALL_METHOD);
            TEST_DESCRIPTOR_UTILS.sortMethods(afterAllMethods, TestUtils.Sort.REVERSE);
            for (Method method : afterAllMethods) {
                try {
                    LOCK_PROCESSOR.processLocks(method);
                    MethodInvoker.invoke(method, testInstance, testArgument);
                } catch (Throwable t) {
                    throwableContext.add(testClass, t);
                } finally {
                    LOCK_PROCESSOR.processUnlocks(method);
                    StandardStreams.flush();
                }
            }
            state.set(State.AFTER_ALL_CALLBACK_METHODS);
        }

        if (state.get() == State.AFTER_ALL_CALLBACK_METHODS) {
            EXTENSION_PROCESSOR.afterAllCallbacks(
                    testClass, testArgument, testInstance, executableContext.getThrowableContext());
            state.set(State.SET_ARGUMENT_FIELDS_NULL);
        }

        if (state.get() == State.SET_ARGUMENT_FIELDS_NULL) {
            List<Field> fields =
                    REFLECTION_UTILS.findFields(testClass, ParameterizedTestFilters.ARGUMENT_FIELD);
            for (Field field : fields) {
                try {
                    field.set(testInstance, null);
                } catch (Throwable t) {
                    throwableContext.add(testClass, t);
                } finally {
                    StandardStreams.flush();
                }
            }

            state.set(State.CLOSE_AUTO_CLOSE_FIELDS);
        }

        AutoCloseProcessor autoCloseProcessor = Singleton.get(AutoCloseProcessor.class);

        if (state.get() == State.CLOSE_AUTO_CLOSE_FIELDS) {
            List<Field> testFields =
                    REFLECTION_UTILS.findFields(
                            testClass, ParameterizedTestFilters.AUTO_CLOSE_FIELDS);
            for (Field testField : testFields) {
                if (testField.isAnnotationPresent(TestEngine.AutoClose.AfterAll.class)) {
                    autoCloseProcessor.close(testInstance, testField, throwableContext);
                    StandardStreams.flush();
                }
            }
            state.set(State.END);
        }

        stopWatch.stop();
        executableMetadata.put(
                ExecutableMetadataConstants.TEST_DESCRIPTOR_ELAPSED_TIME, stopWatch.elapsedTime());

        if (executableContext.getThrowableContext().isEmpty()) {
            executableMetadata.put(
                    ExecutableMetadataConstants.TEST_DESCRIPTOR_STATUS,
                    ExecutableMetadataConstants.PASS);
            executableContext
                    .getExecutionRequest()
                    .getEngineExecutionListener()
                    .executionFinished(this, TestExecutionResult.successful());
        } else {
            executableMetadata.put(
                    ExecutableMetadataConstants.TEST_DESCRIPTOR_STATUS,
                    ExecutableMetadataConstants.FAIL);
            executableContext
                    .getExecutionRequest()
                    .getEngineExecutionListener()
                    .executionFinished(
                            this,
                            TestExecutionResult.failed(
                                    executableContext
                                            .getThrowableContext()
                                            .getThrowables()
                                            .get(0)));
        }

        StandardStreams.flush();
    }

    public static class Builder {

        private TestDescriptor parentTestDescriptor;
        private Class<?> testClass;
        private Method testArgumentSupplierMethod;
        private Argument testArgument;
        private Predicate<Method> testMethodFilter;

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

        public Builder withTestMethodFilter(Predicate<Method> testMethodFilter) {
            this.testMethodFilter = testMethodFilter;
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
                        REFLECTION_UTILS.findMethods(
                                testClass, ParameterizedTestFilters.TEST_METHOD);
                TEST_DESCRIPTOR_UTILS.sortMethods(testMethods, TestUtils.Sort.FORWARD);
                for (Method testMethod : testMethods) {
                    if (testMethodFilter == null || testMethodFilter.test(testMethod)) {
                        testDescriptor.addChild(
                                new ParameterizedMethodTestDescriptor.Builder()
                                        .withParentTestDescriptor(testDescriptor)
                                        .withTestClass(testClass)
                                        .withTestArgument(testArgument)
                                        .withTestMethod(testMethod)
                                        .build());
                    }
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
