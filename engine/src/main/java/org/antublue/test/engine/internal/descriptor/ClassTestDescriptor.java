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

package org.antublue.test.engine.internal.descriptor;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Optional;
import org.antublue.test.engine.api.Named;
import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.exception.TestEngineException;
import org.antublue.test.engine.internal.ContextImpl;
import org.antublue.test.engine.internal.ExtensionManager;
import org.antublue.test.engine.internal.MetadataConstants;
import org.antublue.test.engine.internal.predicate.AnnotationMethodPredicate;
import org.antublue.test.engine.internal.processor.AutoCloseAnnotationProcessor;
import org.antublue.test.engine.internal.processor.ContextAnnotationProcessor;
import org.antublue.test.engine.internal.processor.LockAnnotationProcessor;
import org.antublue.test.engine.internal.util.MethodUtils;
import org.antublue.test.engine.internal.util.StandardStreams;
import org.antublue.test.engine.internal.util.TestUtils;
import org.junit.platform.commons.support.HierarchyTraversalMode;
import org.junit.platform.commons.support.ReflectionSupport;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.ClassSource;

/** Class to implement a ClassTestDescriptor */
@SuppressWarnings("PMD.UnusedPrivateMethod")
public class ClassTestDescriptor extends ExecutableTestDescriptor {

    private static final TestUtils TEST_UTILS = TestUtils.getInstance();

    private static final ContextAnnotationProcessor CONTEXT_ANNOTATION_PROCESSOR =
            ContextAnnotationProcessor.getInstance();

    private static final AutoCloseAnnotationProcessor AUTO_CLOSE_ANNOTATION_PROCESSOR =
            AutoCloseAnnotationProcessor.getInstance();

    private static final LockAnnotationProcessor LOCK_ANNOTATION_PROCESSOR =
            LockAnnotationProcessor.getInstance();

    private static final ExtensionManager EXTENSION_MANAGER = ExtensionManager.getInstance();

    private final Class<?> testClass;
    private final List<Method> prepareMethods;
    private final List<Method> concludeMethods;

    /**
     * Constructor
     *
     * @param builder builder
     */
    private ClassTestDescriptor(Builder builder) {
        super(builder.uniqueId, builder.displayName);
        this.testClass = builder.testClass;
        this.prepareMethods = builder.prepareMethods;
        this.concludeMethods = builder.concludeMethods;
    }

    @Override
    public Optional<TestSource> getSource() {
        return Optional.of(ClassSource.from(testClass));
    }

    @Override
    public Type getType() {
        return Type.CONTAINER_AND_TEST;
    }

    public Class<?> getTestClass() {
        return testClass;
    }

    public String getTag() {
        return TEST_UTILS.getTag(testClass);
    }

    @Override
    public void execute(ExecutionRequest executionRequest) {
        getStopWatch().reset();

        getMetadata().put(MetadataConstants.TEST_CLASS, testClass);

        setExecutionRequest(executionRequest);
        executionRequest.getEngineExecutionListener().executionStarted(this);

        setContextFields();
        setStaticFields();
        postSetStaticFields();
        prepare();
        // postPrepare();

        if (getThrowableContext().isEmpty()) {
            createTestInstance();
        }

        if (getThrowableContext().isEmpty()) {
            execute();
        }

        // closeAutoCloseableFields();
        destroyTestInstance();
        conclude();
        // postConclude();
        clearContextFields();

        getStopWatch().stop();

        getMetadata()
                .put(
                        MetadataConstants.TEST_DESCRIPTOR_ELAPSED_TIME,
                        getStopWatch().elapsedNanoseconds());

        if (getThrowableContext().isEmpty()) {
            getMetadata().put(MetadataConstants.TEST_DESCRIPTOR_STATUS, MetadataConstants.PASS);
            executionRequest
                    .getEngineExecutionListener()
                    .executionFinished(this, TestExecutionResult.successful());
        } else {
            getMetadata().put(MetadataConstants.TEST_DESCRIPTOR_STATUS, MetadataConstants.FAIL);
            executionRequest
                    .getEngineExecutionListener()
                    .executionFinished(
                            this,
                            TestExecutionResult.failed(
                                    getThrowableContext().getThrowables().get(0)));
        }

        StandardStreams.flush();
    }

    private void setContextFields() {
        List<Field> fields =
                ReflectionSupport.findFields(
                        getTestClass(),
                        field ->
                                Modifier.isStatic(field.getModifiers())
                                        && field.isAnnotationPresent(TestEngine.Context.class),
                        HierarchyTraversalMode.TOP_DOWN);

        for (Field field : fields) {
            try {
                field.setAccessible(true);
                field.set(null, ContextImpl.getInstance());
            } catch (Throwable t) {
                getThrowableContext().add(getTestClass(), t);
            }
        }
    }

    private void setStaticFields() {}

    private void postSetStaticFields() {}

    private void prepare() {
        try {
            for (Method method : prepareMethods) {
                LOCK_ANNOTATION_PROCESSOR.processLocks(method);
                MethodUtils.invoke(method, getThrowableContext());
                LOCK_ANNOTATION_PROCESSOR.processUnlocks(method);
                if (!getThrowableContext().isEmpty()) {
                    break;
                }
            }
        } catch (Throwable t) {
            getThrowableContext().add(testClass, t);
        }
    }

    private void postPrepare() {
        EXTENSION_MANAGER.postPrepareMethodsCallback(getTestInstance(), getThrowableContext());
    }

    private void createTestInstance() {
        try {
            setTestInstance(
                    getTestClass()
                            .getDeclaredConstructor((Class<?>[]) null)
                            .newInstance((Object[]) null));
        } catch (Throwable t) {
            getThrowableContext().add(getTestClass(), t);
        }
    }

    private void execute() {
        getChildren()
                .forEach(
                        testDescriptor -> {
                            if (testDescriptor instanceof ExecutableTestDescriptor) {
                                ((ExecutableTestDescriptor) testDescriptor)
                                        .execute(getExecutionRequest());
                            }
                        });
    }

    private void destroyTestInstance() {
        setTestInstance(null);
    }

    private void conclude() {
        for (Method method : concludeMethods) {
            LOCK_ANNOTATION_PROCESSOR.processLocks(method);
            TEST_UTILS.invoke(method, null, getThrowableContext());
            LOCK_ANNOTATION_PROCESSOR.processUnlocks(method);
        }
    }

    private void postConclude() {
        EXTENSION_MANAGER.postConcludeMethodsCallback(getTestInstance(), getThrowableContext());
    }

    private void closeAutoCloseableFields() {
        AUTO_CLOSE_ANNOTATION_PROCESSOR.closeAutoCloseableFields(
                getTestClass(), getThrowableContext());
        LOCK_ANNOTATION_PROCESSOR.processUnlocks(getTestClass());
    }

    private void clearContextFields() {
        List<Field> fields =
                ReflectionSupport.findFields(
                        getTestClass(),
                        field ->
                                Modifier.isStatic(field.getModifiers())
                                        && field.isAnnotationPresent(TestEngine.Context.class),
                        HierarchyTraversalMode.BOTTOM_UP);

        for (Field field : fields) {
            try {
                field.setAccessible(true);
                field.set(null, null);
            } catch (Throwable t) {
                getThrowableContext().add(getTestClass(), t);
            }
        }
    }

    /** Class to implement a Builder */
    public static class Builder {

        private Class<?> testClass;
        private List<Named<?>> testArguments;
        private List<Method> testMethods;

        private UniqueId uniqueId;
        private String displayName;
        private List<Method> prepareMethods;
        private List<Method> concludeMethods;

        /**
         * Method to set the test class
         *
         * @param testClass testClass
         * @return this
         */
        public Builder setTestClass(Class<?> testClass) {
            this.testClass = testClass;
            return this;
        }

        /**
         * Method to set a list of test arguments
         *
         * @param testArguments testArguments
         * @return this
         */
        public Builder setTestArguments(List<Named<?>> testArguments) {
            this.testArguments = testArguments;
            return this;
        }

        /**
         * Method to set a list of test methods
         *
         * @param testMethods testMethods
         * @return this
         */
        public Builder setTestMethods(List<Method> testMethods) {
            this.testMethods = testMethods;
            return this;
        }

        /**
         * Method to build the test descriptor and any children
         *
         * @param parentTestDescriptor parentTestDescriptor
         */
        public void build(TestDescriptor parentTestDescriptor) {
            try {
                uniqueId =
                        parentTestDescriptor
                                .getUniqueId()
                                .append(ClassTestDescriptor.class.getName(), testClass.getName());

                displayName = TEST_UTILS.getDisplayName(testClass);

                prepareMethods =
                        ReflectionSupport.findMethods(
                                testClass,
                                AnnotationMethodPredicate.of(TestEngine.Prepare.class),
                                HierarchyTraversalMode.TOP_DOWN);

                prepareMethods =
                        TEST_UTILS.orderTestMethods(
                                prepareMethods, HierarchyTraversalMode.TOP_DOWN);

                concludeMethods =
                        ReflectionSupport.findMethods(
                                testClass,
                                AnnotationMethodPredicate.of(TestEngine.Conclude.class),
                                HierarchyTraversalMode.BOTTOM_UP);

                concludeMethods =
                        TEST_UTILS.orderTestMethods(
                                concludeMethods, HierarchyTraversalMode.BOTTOM_UP);

                TestDescriptor testDescriptor = new ClassTestDescriptor(this);

                parentTestDescriptor.addChild(testDescriptor);

                int testArgumentIndex = 0;
                for (Named<?> testArgument : testArguments) {
                    new ArgumentTestDescriptor.Builder()
                            .setTestClass(testClass)
                            .setTestArgument(testArgumentIndex, testArgument)
                            .setTestMethods(testMethods)
                            .build(testDescriptor);
                    testArgumentIndex++;
                }
            } catch (RuntimeException e) {
                throw e;
            } catch (Throwable t) {
                throw new TestEngineException(t);
            }
        }
    }
}
