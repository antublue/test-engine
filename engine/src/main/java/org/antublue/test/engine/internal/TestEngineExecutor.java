/*
 * Copyright 2022-2023 Douglas Hoard
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

package org.antublue.test.engine.internal;

import org.antublue.test.engine.TestEngineConstants;
import org.antublue.test.engine.internal.descriptor.ClassTestDescriptor;
import org.antublue.test.engine.internal.descriptor.MethodTestDescriptor;
import org.antublue.test.engine.internal.descriptor.ParameterTestDescriptor;
import org.antublue.test.engine.internal.logger.Logger;
import org.antublue.test.engine.internal.logger.LoggerFactory;
import org.antublue.test.engine.internal.util.Switch;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.support.descriptor.EngineDescriptor;

import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * Method to execute an ExecutionRequest
 */
public class TestEngineExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestEngineExecutor.class);

    private final ExecutorService executorService;

    /**
     * Constructor
     */
    public TestEngineExecutor() {
        int threadCount =
                TestEngineConfigurationParameters.getInstance()
                        .get(TestEngineConstants.THREAD_COUNT)
                        .map(value -> {
                            int intValue;
                            try {
                                intValue = Integer.parseInt(value);
                                if (intValue >= 1) {
                                    return intValue;
                                } else {
                                    throw new TestEngineException(String.format("Invalid thread count [%d]", intValue));
                                }
                            } catch (NumberFormatException e) {
                                throw new TestEngineException(String.format("Invalid thread count [%s]", value), e);
                            }
                        })
                        .orElse(Runtime.getRuntime().availableProcessors());

        LOGGER.trace("thread count [%d]", threadCount);

        this.executorService = Executors.newFixedThreadPool(threadCount, new NamedThreadFactory());
    }

    /**
     * Method to execute the ExecutionRequest
     *
     * @param executionRequest
     */
    public void execute(ExecutionRequest executionRequest) {
        LOGGER.trace("execute(ExecutionRequest)");

        if (executionRequest.getRootTestDescriptor().getChildren().size() < 1) {
            return;
        }

        EngineExecutionListener engineExecutionListener = executionRequest.getEngineExecutionListener();

        TestDescriptor rootTestDescriptor = executionRequest.getRootTestDescriptor();

        // Special case if only a single class it selected from IntelliJ
        if (rootTestDescriptor.getChildren().size() == 1) {
            CountDownLatch countDownLatch = new CountDownLatch(1);

            List<TestExecutionResult> testExecutionResultList = Collections.synchronizedList(new ArrayList<>());

            TestEngineExecutionContext testEngineExecutionContext =
                    new TestEngineExecutionContext(engineExecutionListener, testExecutionResultList);

            TestDescriptor testDescriptor = rootTestDescriptor.getChildren().stream().findFirst().get();

            if (LOGGER.isTraceEnabled()) {
                printTestHierarchy(testDescriptor, 0);
            }

            execute((ClassTestDescriptor) testDescriptor, testEngineExecutionContext, countDownLatch);

            return;
        }

        engineExecutionListener.executionStarted(rootTestDescriptor);

        List<TestExecutionResult> testExecutionResultList = Collections.synchronizedList(new ArrayList<>());

        if (LOGGER.isTraceEnabled()) {
            printTestHierarchy(rootTestDescriptor, 0);
        }

        TestEngineExecutionContext testEngineExecutionContext =
                new TestEngineExecutionContext(engineExecutionListener, testExecutionResultList);

        if (rootTestDescriptor instanceof EngineDescriptor) {
            CountDownLatch countDownLatch = new CountDownLatch(rootTestDescriptor.getChildren().size());

            if (countDownLatch.getCount() > 1) {
                // More than one test class, run each test class in a thread
                for (TestDescriptor testDescriptor : rootTestDescriptor.getChildren()) {
                    executorService.submit(() -> {
                        try {
                            TestEngineExecutionContext testEngineExecutionContext1 =
                                    new TestEngineExecutionContext(engineExecutionListener, testExecutionResultList);

                            execute((ClassTestDescriptor) testDescriptor, testEngineExecutionContext1, countDownLatch);
                        } finally {
                            flush();
                        }
                    });
                }

                try {
                    countDownLatch.await();
                } catch (InterruptedException e) {
                    LOGGER.error("Exception waiting for tests", e);
                }
            } else {
                // Only one test class, run in the main thread
                execute((ClassTestDescriptor) rootTestDescriptor.getChildren().stream().findFirst().get(), testEngineExecutionContext, countDownLatch);
                flush();
            }
        }

        engineExecutionListener.executionFinished(rootTestDescriptor, TestExecutionResult.successful());
        flush();
    }

    /**
     * Method to execute a TestEngineClassTestDescriptor
     *
     * @param testEngineClassTestDescriptor
     * @param testEngineExecutionContext
     */
    private void execute(
            ClassTestDescriptor testEngineClassTestDescriptor,
            TestEngineExecutionContext testEngineExecutionContext,
            CountDownLatch countDownLatch) {
        LOGGER.trace("execute(TestEngineClassTestDescriptor, TestEngineExecutionContext)");

        testEngineExecutionContext.getEngineExecutionListener().executionStarted(testEngineClassTestDescriptor);

        List<TestExecutionResult> testExecutionResultList = testEngineClassTestDescriptor.getTestExecutionResultList();
        testExecutionResultList.clear();

        try {
            Class<?> testClass = testEngineClassTestDescriptor.getTestClass();

            LOGGER.trace("invoking [%s] @TestEngine.BeforeClass methods ...", testClass.getName());
            for (Method beforeClass : TestEngineReflectionUtils.getBeforeClassMethods(testClass)) {
                LOGGER.trace(String.format("invoking [%s] @TestEngine.BeforeClass method [%s] ...", testClass.getName(), beforeClass.getName()));
                beforeClass.invoke(null, (Object[]) null);
                flush();
            }

            Constructor<?> testClassConstructor = testClass.getDeclaredConstructor((Class<?>[]) null);
            Object testInstance = testClassConstructor.newInstance((Object[]) null);
            testEngineExecutionContext.setTestInstance(testInstance);

            Set<? extends TestDescriptor> children = testEngineClassTestDescriptor.getChildren();
            for (TestDescriptor testDescriptor : children) {
                if (testDescriptor instanceof ParameterTestDescriptor) {
                    ParameterTestDescriptor testEngineParameterTestDescriptor = (ParameterTestDescriptor) testDescriptor;
                    execute(testEngineParameterTestDescriptor, testEngineExecutionContext);
                    testExecutionResultList.addAll(testEngineParameterTestDescriptor.getTestExecutionResultList());
                }
            }

            // Remove the test instance to allow garbage collection
            testEngineExecutionContext.setTestInstance(null);

            LOGGER.trace("invoking [%s] @TestEngine.AfterClass methods ...", testClass.getName());
            for (Method afterClassMethod : TestEngineReflectionUtils.getAfterClassMethods(testClass)) {
                LOGGER.trace(String.format("invoking [%s] @TestEngine.AfterClass method [%s] ...", testClass.getName(), afterClassMethod.getName()));
                afterClassMethod.invoke(null, (Object[]) null);
                flush();
            }
        } catch (Throwable t) {
            t = resolve(t);
            printStackTrace(t, System.err);
            testExecutionResultList.add(TestExecutionResult.failed(t));
        } finally {
            flush();

            testEngineExecutionContext.getTestExecutionResultList().addAll(testExecutionResultList);

            if (testExecutionResultList.isEmpty()) {
                testEngineExecutionContext.getEngineExecutionListener().executionFinished(
                        testEngineClassTestDescriptor, TestExecutionResult.successful());
            } else {
                testEngineExecutionContext.getEngineExecutionListener().executionFinished(
                        testEngineClassTestDescriptor,
                        testExecutionResultList.get(0));
            }
        }

        countDownLatch.countDown();
        flush();
    }

    /**
     * Method to execute a TestEngineParameterTestDescriptor
     *
     * @param testEngineParameterTestDescriptor
     * @param testEngineExecutionContext
     */
    private void execute(
            ParameterTestDescriptor testEngineParameterTestDescriptor,
            TestEngineExecutionContext testEngineExecutionContext) {
        LOGGER.trace("execute(TestEngineParameterTestDescriptor, TestEngineExecutionContext)");

        testEngineExecutionContext.getEngineExecutionListener().executionStarted(testEngineParameterTestDescriptor);

        List<TestExecutionResult> testExecutionResultList = testEngineParameterTestDescriptor.getTestExecutionResultList();
        testExecutionResultList.clear();

        Class<?> testClass = testEngineParameterTestDescriptor.getTestClass();
        Object testInstance = testEngineExecutionContext.getTestInstance();
        Object testParameter = testEngineParameterTestDescriptor.getTestParameter();

        try {
            LOGGER.trace("injecting [%s] @TestEngine.Parameter fields ...", testClass.getName());
            Collection<Field> testParameterFields = TestEngineReflectionUtils.getParameterFields(testClass);
            for (Field testParameterField : testParameterFields) {
                LOGGER.trace("injecting [%s] @TestEngine.Parameter field [%s] ...", testClass.getName(), testParameterField.getName());
                testParameterField.set(testInstance, testParameter);
            }

            LOGGER.trace("invoking [%s] @TestEngine.Parameter methods ...", testClass.getName());
            Collection<Method> testParameterMethods = TestEngineReflectionUtils.getParameterMethods(testClass);
            for (Method testParameterMethod : testParameterMethods) {
                LOGGER.trace("invoking [%s] @TestEngine.Parameter method [%s] ...", testClass.getName(), testParameterMethod.getName());
                testParameterMethod.invoke(testInstance, testParameter);
            }

            LOGGER.trace("invoking [%s] @TestEngine.BeforeAll methods ...", testClass.getName());
            for (Method beforeAllMethod : TestEngineReflectionUtils.getBeforeAllMethods(testClass)) {
                LOGGER.trace(String.format("invoking [%s] @TestEngine.BeforeAll method [%s] ...", testClass.getName(), beforeAllMethod.getName()));
                beforeAllMethod.invoke(testInstance, (Object[]) null);
                flush();
            }
        } catch (Throwable t) {
            t = resolve(t);
            printStackTrace(t, System.err);
            testExecutionResultList.add(TestExecutionResult.failed(t));
        } finally {
            flush();
        }

        if (testExecutionResultList.isEmpty()) {
            Set<? extends TestDescriptor> children = testEngineParameterTestDescriptor.getChildren();
            for (TestDescriptor testDescriptor : children) {
                if (testDescriptor instanceof MethodTestDescriptor) {
                    MethodTestDescriptor methodTestDescriptor = (MethodTestDescriptor) testDescriptor;
                    execute(methodTestDescriptor, testEngineExecutionContext);
                    testExecutionResultList.addAll(methodTestDescriptor.getTestExecutionResultList());
                }
            }
        } else {
            Set<? extends TestDescriptor> children = testEngineParameterTestDescriptor.getChildren();
            for (TestDescriptor testDescriptor : children) {
                if (testDescriptor instanceof MethodTestDescriptor) {
                    testEngineExecutionContext.getEngineExecutionListener().executionSkipped(testDescriptor, "@TestEngine.BeforeAll method exception");
                }
            }
        }

        try {
            LOGGER.trace("invoking [%s] @TestEngine.AfterAll methods ...", testClass.getName());
            for (Method afterAllMethod : TestEngineReflectionUtils.getAfterAllMethods(testClass)) {
                LOGGER.trace(String.format("invoking [%s] @TestEngine.AfterAll method [%s] ...", testClass.getName(), afterAllMethod.getName()));
                afterAllMethod.invoke(testInstance, (Object[]) null);
                flush();
            }
        } catch (Throwable t) {
            t = resolve(t);
            printStackTrace(t, System.err);
            testExecutionResultList.add(TestExecutionResult.failed(t));
        } finally {
            flush();
        }

        if (testExecutionResultList.isEmpty()) {
            testEngineExecutionContext.getEngineExecutionListener().executionFinished(
                    testEngineParameterTestDescriptor, TestExecutionResult.successful());
        } else {
            testEngineExecutionContext.getEngineExecutionListener().executionFinished(
                    testEngineParameterTestDescriptor, testExecutionResultList.get(0));
        }

        testEngineExecutionContext.getTestExecutionResultList().addAll(testExecutionResultList);
        flush();
    }

    /**
     * Method to execute a TestMethodTestDescriptor
     *
     * @param methodTestDescriptor
     * @param testEngineExecutionContext
     */
    private void execute(
            MethodTestDescriptor methodTestDescriptor,
            TestEngineExecutionContext testEngineExecutionContext) {
        LOGGER.trace("execute(TestEngineTestMethodTestDescriptor, TestEngineExecutionContext)");
        testEngineExecutionContext.getEngineExecutionListener().executionStarted(methodTestDescriptor);

        List<TestExecutionResult> testExecutionResultList = methodTestDescriptor.getTestExecutionResultList();
        testExecutionResultList.clear();

        Class<?> testClass = methodTestDescriptor.getTestClass();
        Object testInstance = testEngineExecutionContext.getTestInstance();

        try {
            LOGGER.trace("invoking [%s] @TestEngine.BeforeEach methods ...", testClass.getName());
            for (Method beforeEachMethod : TestEngineReflectionUtils.getBeforeEachMethods(testClass)) {
                LOGGER.trace(String.format("invoking [%s] @TestEngine.BeforeEach method [%s] ...", testClass.getName(), beforeEachMethod.getName()));
                beforeEachMethod.invoke(testInstance, (Object[]) null);
                flush();
            }
        } catch (Throwable t) {
            t = resolve(t);
            printStackTrace(t, System.err);
            testExecutionResultList.add(TestExecutionResult.failed(t));
        } finally {
            flush();
        }

        try {
            Method testMethod = methodTestDescriptor.getTestMethod();
            LOGGER.trace("invoking [%s] @TestEngine.Test method [%s] ...", testClass.getName(), testMethod.getName());
            testMethod.invoke(testInstance, (Object[]) null);
            flush();
        } catch (Throwable t) {
            t = resolve(t);
            printStackTrace(t, System.err);
            testExecutionResultList.add(TestExecutionResult.failed(t));
        } finally {
            flush();
        }

        try {
            LOGGER.trace("invoking [%s] @TestEngine.AfterEach methods ...", testClass.getName());
            for (Method afterEachMethod : TestEngineReflectionUtils.getAfterEachMethods(testClass)) {
                LOGGER.trace(String.format("invoking [%s] @TestEngine.AfterEach method [%s] ...", testClass.getName(), afterEachMethod.getName()));
                afterEachMethod.invoke(testInstance, (Object[]) null);
                flush();
            }
        } catch (Throwable t) {
            t = resolve(t);
            printStackTrace(t, System.err);
            testExecutionResultList.add(TestExecutionResult.failed(t));
        } finally {
            flush();
        }

        if (testExecutionResultList.isEmpty()) {
            testEngineExecutionContext.getEngineExecutionListener().executionFinished(
                    methodTestDescriptor, TestExecutionResult.successful());
        } else {
            testEngineExecutionContext.getEngineExecutionListener().executionFinished(
                    methodTestDescriptor, testExecutionResultList.get(0));
        }

        testEngineExecutionContext.getTestExecutionResultList().addAll(testExecutionResultList);
        flush();
    }

    /**
     * Method to log the test hierarchy
     *
     * @param testDescriptor
     * @param indent
     */
    private void printTestHierarchy(TestDescriptor testDescriptor, int indent) {
        if (indent == 0) {
            LOGGER.trace("Test class hierarchy ...");
        }

        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < indent; i++) {
            stringBuilder.append(" ");
        }

        Switch.switchType(testDescriptor,
                Switch.switchCase(
                        MethodTestDescriptor.class,
                        testMethodTestDescriptor ->
                                stringBuilder
                                        .append("method -> ")
                                        .append(testMethodTestDescriptor.getTestMethod().getName())
                                        .append("()")),
                Switch.switchCase(
                        ParameterTestDescriptor.class,
                        testEngineParameterTestDescriptor ->
                                stringBuilder
                                        .append("parameter -> ")
                                        .append(testEngineParameterTestDescriptor.getTestParameter())),
                Switch.switchCase(
                        ClassTestDescriptor.class,
                        testClassTestDescriptor ->
                                stringBuilder
                                        .append("class -> ")
                                        .append(testClassTestDescriptor.getTestClass().getName())),
                Switch.switchCase(
                        EngineDescriptor.class,
                        engineDescriptor ->
                                stringBuilder
                                        .append("engine -> ")
                                        .append(engineDescriptor.getDisplayName())));

        LOGGER.trace(stringBuilder.toString());

        if (LOGGER.isTraceEnabled()) {
            for (TestDescriptor child : testDescriptor.getChildren()) {
                printTestHierarchy(child, indent + 2);
            }
        }
    }

    /**
     * Method to resolve the root exception if the throwable is an InvocationTargetException
     *
     * @param t
     * @return
     */
    private static Throwable resolve(Throwable t) {
        if (t instanceof InvocationTargetException) {
            return t.getCause();
        } else {
            return t;
        }
    }

    /**
     * Method to print a stack track, stopping at the test engine
     *
     * @param t
     * @param printStream
     */
    public static void printStackTrace(Throwable t, PrintStream printStream) {
        printStream.println(t.getClass().getName() + ": " + t.getMessage());

        StackTraceElement[] stackTraceElements = t.getStackTrace();
        if (stackTraceElements != null) {
            for (StackTraceElement stackTraceElement : stackTraceElements) {
                if (stackTraceElement.getClassName().startsWith("org.antublue.test.engine")) {
                    break;
                } else {
                    printStream.println("    at " + stackTraceElement);
                }
            }
        }
    }

    /**
     * Method to flush the System.err stream, which seems to flush the System.out stream
     * Without the flush, IntelliJ seems to "miss" System.out.println() calls in test methods
     */
    private static void flush() {
        System.err.flush();
        System.out.flush();
    }

    /**
     * Class to implement a named ThreadFactory
     */
    private static class NamedThreadFactory implements ThreadFactory {

        private int threadId = 1;

        /**
         * Method to create a new Thread
         *
         * @param r a runnable to be executed by new thread instance
         * @return
         */
        @Override
        public Thread newThread(Runnable r) {
            String threadName;
            synchronized (this) {
                threadName = String.format("test-engine-%02d", this.threadId);
                this.threadId++;
            }

            Thread thread = new Thread(r);
            thread.setName(threadName);
            thread.setDaemon(true);
            return thread;
        }
    }
}
