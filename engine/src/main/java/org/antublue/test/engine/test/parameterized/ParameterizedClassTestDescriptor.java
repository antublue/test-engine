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

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import org.antublue.test.engine.api.Argument;
import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.exception.TestClassDefinitionException;
import org.antublue.test.engine.test.ExecutableContext;
import org.antublue.test.engine.test.ExecutableMetadata;
import org.antublue.test.engine.test.ExecutableMetadataConstants;
import org.antublue.test.engine.test.ExecutableMetadataSupport;
import org.antublue.test.engine.test.ExecutableTestDescriptor;
import org.antublue.test.engine.test.util.AutoCloseProcessor;
import org.antublue.test.engine.test.util.LockProcessor;
import org.antublue.test.engine.test.util.MethodInvoker;
import org.antublue.test.engine.test.util.TestDescriptorUtils;
import org.antublue.test.engine.util.Invariant;
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
import org.junit.platform.engine.support.descriptor.ClassSource;

/** Class to implement a ParameterClassTestDescriptor */
public class ParameterizedClassTestDescriptor extends AbstractTestDescriptor
        implements ExecutableTestDescriptor, ExecutableMetadataSupport {

    private static final ReflectionUtils REFLECTION_UTILS = ReflectionUtils.getSingleton();

    private static final TestDescriptorUtils TEST_DESCRIPTOR_UTILS =
            TestDescriptorUtils.getSingleton();

    private static final ParameterizedUtils PARAMETERIZED_UTILS = ParameterizedUtils.getSingleton();

    private static final LockProcessor LOCK_PROCESSOR = LockProcessor.getSingleton();

    private final Class<?> testClass;
    private final StopWatch stopWatch;
    private final ExecutableMetadata executableMetadata;

    /**
     * Constructor
     *
     * @param uniqueId uniqueId
     * @param testClass testClass
     */
    private ParameterizedClassTestDescriptor(UniqueId uniqueId, Class<?> testClass) {
        super(uniqueId, TEST_DESCRIPTOR_UTILS.getDisplayName(testClass));
        this.testClass = testClass;
        this.stopWatch = new StopWatch();
        this.executableMetadata = new ExecutableMetadata();
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
    public ExecutableMetadata getExecutableMetadata() {
        return executableMetadata;
    }

    private enum State {
        RUN_INSTANTIATE,
        RUN_PREPARE_METHODS,
        RUN_EXECUTE,
        RUN_CONCLUDE_METHODS,
        RUN_AUTO_CLOSE_FIELDS,
        END
    }

    @Override
    public void execute(ExecutionRequest executionRequest, ExecutableContext executableContext) {
        stopWatch.start();

        EngineExecutionListener engineExecutionListener =
                executionRequest.getEngineExecutionListener();
        engineExecutionListener.executionStarted(this);

        validate();

        AtomicReference<State> state = new AtomicReference<>(State.RUN_INSTANTIATE);

        Object testInstance = null;

        if (state.get() == State.RUN_INSTANTIATE) {
            try {
                Constructor<?> constructor = testClass.getDeclaredConstructor((Class<?>[]) null);
                testInstance = constructor.newInstance((Object[]) null);
                executableContext.put(ParameterizedExecutableConstants.TEST_INSTANCE, testInstance);
                state.set(State.RUN_PREPARE_METHODS);
            } catch (Throwable t) {
                executableContext.addAndProcessThrowable(testClass, t);
                state.set(State.RUN_EXECUTE);
            } finally {
                StandardStreams.flush();
            }
        }

        if (state.get() == State.RUN_PREPARE_METHODS) {
            Invariant.check(testInstance != null);
            try {
                List<Method> prepareMethods =
                        REFLECTION_UTILS.findMethods(
                                testClass, ParameterizedFilters.PREPARE_METHOD);
                TEST_DESCRIPTOR_UTILS.sortMethods(prepareMethods, TestDescriptorUtils.Sort.FORWARD);
                for (Method method : prepareMethods) {
                    try {
                        LOCK_PROCESSOR.processLocks(method);
                        MethodInvoker.invoke(method, testInstance, null);
                    } finally {
                        LOCK_PROCESSOR.processUnlocks(method);
                        StandardStreams.flush();
                    }
                }
            } catch (Throwable t) {
                executableContext.addAndProcessThrowable(testClass, t);
            } finally {
                StandardStreams.flush();
                state.set(State.RUN_EXECUTE);
            }
        }

        if (state.get() == State.RUN_EXECUTE) {
            getChildren()
                    .forEach(
                            testDescriptor -> {
                                if (testDescriptor instanceof ExecutableTestDescriptor) {
                                    ((ExecutableTestDescriptor) testDescriptor)
                                            .execute(executionRequest, executableContext);
                                }
                            });
            if (testInstance != null) {
                state.set(State.RUN_CONCLUDE_METHODS);
            } else {
                state.set(State.END);
            }
        }

        if (state.get() == State.RUN_CONCLUDE_METHODS) {
            Invariant.check(testInstance != null);
            List<Method> concludeMethods =
                    REFLECTION_UTILS.findMethods(testClass, ParameterizedFilters.CONCLUDE_METHOD);
            TEST_DESCRIPTOR_UTILS.sortMethods(concludeMethods, TestDescriptorUtils.Sort.REVERSE);
            for (Method method : concludeMethods) {
                try {
                    LOCK_PROCESSOR.processLocks(method);
                    MethodInvoker.invoke(method, testInstance, null);
                } catch (Throwable t) {
                    executableContext.addAndProcessThrowable(testClass, t);
                } finally {
                    LOCK_PROCESSOR.processUnlocks(method);
                    StandardStreams.flush();
                }
            }
            state.set(State.RUN_AUTO_CLOSE_FIELDS);
        }

        if (state.get() == State.RUN_AUTO_CLOSE_FIELDS) {
            Invariant.check(testInstance != null);
            List<Field> fields =
                    REFLECTION_UTILS.findFields(testClass, ParameterizedFilters.AUTO_CLOSE_FIELDS);
            for (Field field : fields) {
                TestEngine.AutoClose annotation = field.getAnnotation(TestEngine.AutoClose.class);
                if ("@TestEngine.Conclude".equals(annotation.lifecycle())) {
                    try {
                        AutoCloseProcessor.close(testInstance, field);
                    } catch (Throwable t) {
                        executableContext.addAndProcessThrowable(testClass, t);
                    } finally {
                        StandardStreams.flush();
                    }
                }
            }
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

        executableContext.remove(ParameterizedExecutableConstants.TEST_INSTANCE);

        StandardStreams.flush();
    }

    private void validate() {
        try {
            testClass.getDeclaredConstructor((Class<?>[]) null);
        } catch (Throwable t) {
            throw new TestClassDefinitionException(
                    String.format(
                            "Test class [%s] must have a no-argument constructor",
                            testClass.getName()));
        }

        List<Method> methods =
                REFLECTION_UTILS.findMethods(
                        testClass,
                        method -> method.isAnnotationPresent(TestEngine.ArgumentSupplier.class));
        if (methods.size() != 1) {
            throw new TestClassDefinitionException(
                    String.format(
                            "Test class [%s] must defined exactly one @TestEngine.ArgumentSupplier"
                                    + " method, %d found",
                            testClass.getName(), methods.size()));
        }

        if (!ParameterizedFilters.ARGUMENT_SUPPLIER_METHOD.test(methods.get(0))) {
            throw new TestClassDefinitionException(
                    String.format(
                            "Test class [%s] @TestEngine.ArgumentSupplier method [%s] definition is"
                                    + " invalid",
                            testClass.getName(), methods.get(0).getName()));
        }

        methods =
                REFLECTION_UTILS.findMethods(
                        testClass, method -> method.isAnnotationPresent(TestEngine.Prepare.class));
        for (Method method : methods) {
            if (method.isAnnotationPresent(TestEngine.Disabled.class)) {
                continue;
            }
            if (!ParameterizedFilters.PREPARE_METHOD.test(method)) {
                throw new TestClassDefinitionException(
                        String.format(
                                "Test class [%s] @TestEngine.Prepare method [%s] definition is"
                                        + " invalid",
                                testClass.getName(), method.getName()));
            }
        }

        methods =
                REFLECTION_UTILS.findMethods(
                        testClass,
                        method -> method.isAnnotationPresent(TestEngine.BeforeAll.class));
        for (Method method : methods) {
            if (method.isAnnotationPresent(TestEngine.Disabled.class)) {
                continue;
            }
            if (!ParameterizedFilters.BEFORE_ALL_METHOD.test(method)) {
                throw new TestClassDefinitionException(
                        String.format(
                                "Test class [%s] @TestEngine.BeforeAll method [%s] definition is"
                                        + " invalid",
                                testClass.getName(), method.getName()));
            }
        }

        methods =
                REFLECTION_UTILS.findMethods(
                        testClass,
                        method -> method.isAnnotationPresent(TestEngine.BeforeEach.class));
        for (Method method : methods) {
            if (method.isAnnotationPresent(TestEngine.Disabled.class)) {
                continue;
            }
            if (!ParameterizedFilters.BEFORE_EACH_METHOD.test(method)) {
                throw new TestClassDefinitionException(
                        String.format(
                                "Test class [%s] @TestEngine.BeforeEach method [%s] definition is"
                                        + " invalid",
                                testClass.getName(), method.getName()));
            }
        }

        methods =
                REFLECTION_UTILS.findMethods(
                        testClass, method -> method.isAnnotationPresent(TestEngine.Test.class));
        for (Method method : methods) {
            if (method.isAnnotationPresent(TestEngine.Disabled.class)) {
                continue;
            }
            if (!ParameterizedFilters.TEST_METHOD.test(method)) {
                throw new TestClassDefinitionException(
                        String.format(
                                "Test class [%s] @TestEngine.Test method [%s] definition is"
                                        + " invalid",
                                testClass.getName(), method.getName()));
            }
        }

        methods =
                REFLECTION_UTILS.findMethods(
                        testClass,
                        method -> method.isAnnotationPresent(TestEngine.AfterEach.class));
        for (Method method : methods) {
            if (method.isAnnotationPresent(TestEngine.Disabled.class)) {
                continue;
            }
            if (!ParameterizedFilters.AFTER_EACH_METHOD.test(method)) {
                TestClassDefinitionException testClassDefinitionException =
                        new TestClassDefinitionException(
                                String.format(
                                        "Test class [%s] @TestEngine.AfterEach method [%s]"
                                                + " definition is invalid",
                                        testClass.getName(), method.getName()));
                testClassDefinitionException.setStackTrace(new StackTraceElement[0]);
                throw testClassDefinitionException;
            }
        }

        methods =
                REFLECTION_UTILS.findMethods(
                        testClass, method -> method.isAnnotationPresent(TestEngine.AfterAll.class));
        for (Method method : methods) {
            if (method.isAnnotationPresent(TestEngine.Disabled.class)) {
                continue;
            }
            if (!ParameterizedFilters.AFTER_ALL_METHOD.test(method)) {
                throw new TestClassDefinitionException(
                        String.format(
                                "Test class [%s] @TestEngine.BeforeAll method [%s] definition is"
                                        + " invalid",
                                testClass.getName(), method.getName()));
            }
        }

        methods =
                REFLECTION_UTILS.findMethods(
                        testClass, method -> method.isAnnotationPresent(TestEngine.Conclude.class));
        for (Method method : methods) {
            if (method.isAnnotationPresent(TestEngine.Disabled.class)) {
                continue;
            }
            if (!ParameterizedFilters.CONCLUDE_METHOD.test(method)) {
                throw new TestClassDefinitionException(
                        String.format(
                                "Test class [%s] @TestEngine.Conclude method [%s] definition is"
                                        + " invalid",
                                testClass.getName(), method.getName()));
            }
        }
    }

    public static class Builder {

        private TestDescriptor parentTestDescriptor;
        private Class<?> testClass;

        public Builder withParentTestDescriptor(TestDescriptor parentTestDescriptor) {
            this.parentTestDescriptor = parentTestDescriptor;
            return this;
        }

        public Builder withTestClass(Class<?> testClass) {
            this.testClass = testClass;
            return this;
        }

        public void build() {
            try {
                UniqueId testDescriptorUniqueId =
                        parentTestDescriptor
                                .getUniqueId()
                                .append(
                                        ParameterizedClassTestDescriptor.class.getName(),
                                        testClass.getName());
                TestDescriptor testDescriptor =
                        new ParameterizedClassTestDescriptor(testDescriptorUniqueId, testClass);
                parentTestDescriptor.addChild(testDescriptor);

                Method testArgumentSupplierMethod =
                        PARAMETERIZED_UTILS.getArumentSupplierMethod(testClass);

                List<Argument> testArguments = PARAMETERIZED_UTILS.getArguments(testClass);
                for (Argument testArgument : testArguments) {
                    testDescriptor.addChild(
                            new ParameterizedArgumentTestDescriptor.Builder()
                                    .withParentTestDescriptor(testDescriptor)
                                    .withTestClass(testClass)
                                    .withTestArgumentSupplierMethod(testArgumentSupplierMethod)
                                    .withTestArgument(testArgument)
                                    .build());
                }
            } catch (RuntimeException e) {
                throw e;
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
        }
    }
}
