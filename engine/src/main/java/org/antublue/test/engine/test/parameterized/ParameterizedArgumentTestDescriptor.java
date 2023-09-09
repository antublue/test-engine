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
import java.util.function.Consumer;
import java.util.function.Predicate;
import org.antublue.test.engine.api.Argument;
import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.exception.TestClassFailedException;
import org.antublue.test.engine.test.ExecutableMetadataConstants;
import org.antublue.test.engine.test.ExecutableTestDescriptor;
import org.antublue.test.engine.test.ThrowableContext;
import org.antublue.test.engine.test.util.AutoCloseProcessor;
import org.antublue.test.engine.test.util.RandomFieldInjector;
import org.antublue.test.engine.test.util.TestUtils;
import org.antublue.test.engine.util.Invariant;
import org.antublue.test.engine.util.StandardStreams;
import org.antublue.test.engine.util.StopWatch;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.MethodSource;

/** Class to implement a ParameterArgumentTestDescriptor */
public class ParameterizedArgumentTestDescriptor extends ExecutableTestDescriptor {

    private final Class<?> testClass;
    private final Method testArgumentSupplierMethod;
    private final Argument testArgument;
    private final StopWatch stopWatch;

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
    }

    @Override
    public Optional<TestSource> getSource() {
        return Optional.of(MethodSource.from(testArgumentSupplierMethod));
    }

    @Override
    public Type getType() {
        return Type.CONTAINER_AND_TEST;
    }

    private enum State {
        BEGIN,
        SET_ARGUMENT_FIELDS,
        SET_RANDOM_FIELDS,
        BEFORE_ALL,
        EXECUTE_OR_SKIP,
        AFTER_ALL,
        SET_ARGUMENT_FIELDS_NULL,
        CLOSE_AUTO_CLOSE_FIELDS,
        END
    }

    @Override
    public void execute(ExecutionRequest executionRequest) {
        stopWatch.start();

        Object testInstance = getParent(ExecutableTestDescriptor.class).getTestInstance();
        Invariant.check(testInstance != null);
        setTestInstance(testInstance);

        getExecutableMetadata().put(ExecutableMetadataConstants.TEST_CLASS, testClass);
        getExecutableMetadata().put(ExecutableMetadataConstants.TEST_ARGUMENT, testArgument);

        if (!getThrowableContext().isEmpty()) {
            getChildren()
                    .forEach(
                            (Consumer<TestDescriptor>)
                                    testDescriptor -> {
                                        if (testDescriptor instanceof ExecutableTestDescriptor) {
                                            ((ExecutableTestDescriptor) testDescriptor)
                                                    .execute(executionRequest);
                                        }
                                    });

            stopWatch.stop();
            getExecutableMetadata()
                    .put(
                            ExecutableMetadataConstants.TEST_DESCRIPTOR_ELAPSED_TIME,
                            stopWatch.elapsedTime());
            getExecutableMetadata()
                    .put(
                            ExecutableMetadataConstants.TEST_DESCRIPTOR_STATUS,
                            ExecutableMetadataConstants.SKIP);
            executionRequest.getEngineExecutionListener().executionSkipped(this, "");
            return;
        }

        executionRequest.getEngineExecutionListener().executionStarted(this);

        State state = State.BEGIN;
        while (state != null && state != State.END) {
            switch (state) {
                case BEGIN:
                    {
                        state = State.SET_ARGUMENT_FIELDS;
                        break;
                    }
                case SET_ARGUMENT_FIELDS:
                    {
                        state = setArgumentFields();
                        break;
                    }
                case SET_RANDOM_FIELDS:
                    {
                        state = setRandomFields();
                        break;
                    }
                case BEFORE_ALL:
                    {
                        state = beforeAll();
                        break;
                    }
                case EXECUTE_OR_SKIP:
                    {
                        state = executeOrSkip(executionRequest);
                        break;
                    }
                case AFTER_ALL:
                    {
                        state = afterAll();
                        break;
                    }
                case SET_ARGUMENT_FIELDS_NULL:
                    {
                        state = setArgumentFieldsNull();
                        break;
                    }
                case CLOSE_AUTO_CLOSE_FIELDS:
                    {
                        state = closeAutoCloseFields();
                        break;
                    }
                default:
                    {
                        state = null;
                    }
            }
        }

        stopWatch.stop();

        getExecutableMetadata()
                .put(
                        ExecutableMetadataConstants.TEST_DESCRIPTOR_ELAPSED_TIME,
                        stopWatch.elapsedTime());

        if (getThrowableContext().isEmpty()) {
            getExecutableMetadata()
                    .put(
                            ExecutableMetadataConstants.TEST_DESCRIPTOR_STATUS,
                            ExecutableMetadataConstants.PASS);
            executionRequest
                    .getEngineExecutionListener()
                    .executionFinished(this, TestExecutionResult.successful());
        } else {
            getParent(ParameterizedClassTestDescriptor.class)
                    .getThrowableContext()
                    .add(
                            getTestInstance().getClass(),
                            new TestClassFailedException(
                                    String.format(
                                            "Exception testing test class [%s]",
                                            TEST_UTILS.getDisplayName(testClass))));
            getExecutableMetadata()
                    .put(
                            ExecutableMetadataConstants.TEST_DESCRIPTOR_STATUS,
                            ExecutableMetadataConstants.FAIL);
            executionRequest
                    .getEngineExecutionListener()
                    .executionFinished(
                            this,
                            TestExecutionResult.failed(
                                    getThrowableContext().getThrowables().get(0)));
        }

        StandardStreams.flush();
    }

    private State setArgumentFields() {
        Object testInstance = getTestInstance();
        Invariant.check(testInstance != null);
        ThrowableContext throwableContext = getThrowableContext();
        try {
            List<Field> fields =
                    REFLECTION_UTILS.findFields(testClass, ParameterizedTestFilters.ARGUMENT_FIELD);
            for (Field field : fields) {
                field.set(testInstance, testArgument);
            }
            return State.SET_RANDOM_FIELDS;
        } catch (Throwable t) {
            throwableContext.add(testClass, t);
            return State.EXECUTE_OR_SKIP;
        } finally {
            StandardStreams.flush();
        }
    }

    private State setRandomFields() {
        Object testInstance = getTestInstance();
        Invariant.check(testInstance != null);
        ThrowableContext throwableContext = getThrowableContext();
        try {
            List<Field> fields =
                    REFLECTION_UTILS.findFields(testClass, ParameterizedTestFilters.RANDOM_FIELD);
            for (Field field : fields) {
                RandomFieldInjector.inject(testInstance, field);
            }
            return State.BEFORE_ALL;
        } catch (Throwable t) {
            throwableContext.add(testClass, t);
            return State.EXECUTE_OR_SKIP;
        } finally {
            StandardStreams.flush();
        }
    }

    private State beforeAll() {
        Object testInstance = getTestInstance();
        Invariant.check(testInstance != null);
        ThrowableContext throwableContext = getThrowableContext();
        try {
            List<Method> beforeAllMethods =
                    REFLECTION_UTILS.findMethods(
                            testClass, ParameterizedTestFilters.BEFORE_ALL_METHOD);
            TEST_UTILS.sortMethods(beforeAllMethods, TestUtils.Sort.FORWARD);

            for (Method method : beforeAllMethods) {
                LOCK_PROCESSOR.processLocks(method);
                TEST_UTILS.invoke(method, testInstance, testArgument, throwableContext);
                LOCK_PROCESSOR.processUnlocks(method);
                if (!throwableContext.isEmpty()) {
                    break;
                }
            }
        } catch (Throwable t) {
            throwableContext.add(testClass, t);
        } finally {
            EXTENSION_MANAGER.beforeAll(testInstance, testArgument, throwableContext);
            StandardStreams.flush();
        }
        return State.EXECUTE_OR_SKIP;
    }

    private State executeOrSkip(ExecutionRequest executionRequest) {
        Object testInstance = getTestInstance();
        Invariant.check(testInstance != null);
        getChildren()
                .forEach(
                        (Consumer<TestDescriptor>)
                                testDescriptor -> {
                                    if (testDescriptor instanceof ExecutableTestDescriptor) {
                                        ((ExecutableTestDescriptor) testDescriptor)
                                                .execute(executionRequest);
                                    }
                                });
        StandardStreams.flush();
        return State.AFTER_ALL;
    }

    private State afterAll() {
        Object testInstance = getTestInstance();
        Invariant.check(testInstance != null);
        ThrowableContext throwableContext = getThrowableContext();
        List<Method> afterAllMethods =
                REFLECTION_UTILS.findMethods(testClass, ParameterizedTestFilters.AFTER_ALL_METHOD);
        TEST_UTILS.sortMethods(afterAllMethods, TestUtils.Sort.REVERSE);
        for (Method method : afterAllMethods) {
            LOCK_PROCESSOR.processLocks(method);
            TEST_UTILS.invoke(method, testInstance, testArgument, throwableContext);
            LOCK_PROCESSOR.processUnlocks(method);
            StandardStreams.flush();
        }
        EXTENSION_MANAGER.afterAll(testInstance, testArgument, throwableContext);
        return State.SET_ARGUMENT_FIELDS_NULL;
    }

    private State setArgumentFieldsNull() {
        Object testInstance = getTestInstance();
        Invariant.check(testInstance != null);
        ThrowableContext throwableContext = getThrowableContext();
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
        return State.CLOSE_AUTO_CLOSE_FIELDS;
    }

    private State closeAutoCloseFields() {
        Object testInstance = getTestInstance();
        Invariant.check(testInstance != null);
        ThrowableContext throwableContext = getThrowableContext();
        AutoCloseProcessor autoCloseProcessor = AutoCloseProcessor.getSingleton();
        List<Field> testFields =
                REFLECTION_UTILS.findFields(testClass, ParameterizedTestFilters.AUTO_CLOSE_FIELDS);
        for (Field testField : testFields) {
            if (testField.isAnnotationPresent(TestEngine.AutoClose.AfterAll.class)) {
                autoCloseProcessor.close(testInstance, testField, throwableContext);
                StandardStreams.flush();
            }
        }
        return State.END;
    }

    public static class Builder {

        private TestDescriptor parentTestDescriptor;
        private Class<?> testClass;
        private Method testArgumentSupplierMethod;
        private Argument testArgument;
        private Predicate<Method> testMethodFilter;

        public Builder setParentTestDescriptor(TestDescriptor parentTestDescriptor) {
            this.parentTestDescriptor = parentTestDescriptor;
            return this;
        }

        public Builder setTestClass(Class<?> testClass) {
            this.testClass = testClass;
            return this;
        }

        public Builder setTestArgumentSupplierMethod(Method testArgumentSupplierMethod) {
            this.testArgumentSupplierMethod = testArgumentSupplierMethod;
            return this;
        }

        public Builder setTestArgument(Argument testArgument) {
            this.testArgument = testArgument;
            return this;
        }

        public Builder setTestMethodFilter(Predicate<Method> testMethodFilter) {
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
                TEST_UTILS.sortMethods(testMethods, TestUtils.Sort.FORWARD);
                for (Method testMethod : testMethods) {
                    if (testMethodFilter == null || testMethodFilter.test(testMethod)) {
                        testDescriptor.addChild(
                                new ParameterizedMethodTestDescriptor.Builder()
                                        .setParentTestDescriptor(testDescriptor)
                                        .setTestClass(testClass)
                                        .setTestArgument(testArgument)
                                        .setTestMethod(testMethod)
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
