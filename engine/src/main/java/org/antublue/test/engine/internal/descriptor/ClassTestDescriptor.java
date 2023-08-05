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

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import org.antublue.test.engine.api.statemachine.StateMachine;
import org.antublue.test.engine.internal.AutoCloseAnnotationUtils;
import org.antublue.test.engine.internal.ExecutorContext;
import org.antublue.test.engine.internal.logger.Logger;
import org.antublue.test.engine.internal.logger.LoggerFactory;
import org.antublue.test.engine.internal.util.ThrowableCollector;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.ClassSource;

/** Class to implement a class test descriptor */
public final class ClassTestDescriptor extends ExtendedAbstractTestDescriptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClassTestDescriptor.class);

    public enum State {
        BEGIN,
        PRE_PREPARE,
        PREPARE,
        POST_PREPARE,
        EXECUTE,
        SKIP,
        SKIP2,
        PRE_CONCLUDE,
        CONCLUDE,
        POST_CONCLUDE,
        END
    }

    private final Class<?> testClass;
    private final ThrowableCollector throwableCollector;
    private final StateMachine<State> stateMachine;
    private ExecutorContext executorContext;
    private Object testInstance;

    /**
     * Constructor
     *
     * @param uniqueId uniqueId
     * @param displayName displayName
     * @param testClass testClass
     */
    ClassTestDescriptor(UniqueId uniqueId, String displayName, Class<?> testClass) {
        super(uniqueId, displayName);
        this.testClass = testClass;
        this.throwableCollector = new ThrowableCollector();
        this.stateMachine =
                new StateMachine<State>(getClass().getName())
                        .addTransition(State.BEGIN, this::begin)
                        .addTransition(State.PRE_PREPARE, this::prePrepare)
                        .addTransition(State.PREPARE, this::prepare)
                        .addTransition(State.POST_PREPARE, this::postPrepare)
                        .addTransition(State.EXECUTE, this::execute)
                        .addTransition(State.SKIP, this::skip)
                        .addTransition(State.SKIP2, this::skip2)
                        .addTransition(State.PRE_CONCLUDE, this::preConclude)
                        .addTransition(State.CONCLUDE, this::conclude)
                        .addTransition(State.POST_CONCLUDE, this::postConclude)
                        .addTransition(State.END, this::end);
    }

    /**
     * Method to get the TestSource
     *
     * @return the return value
     */
    @Override
    public Optional<TestSource> getSource() {
        return Optional.of(ClassSource.from(testClass));
    }

    /**
     * Method to get the test descriptor Type
     *
     * @return the return value
     */
    @Override
    public Type getType() {
        return Type.CONTAINER;
    }

    /**
     * Method to return whether the test descriptor is a test
     *
     * @return the return value
     */
    @Override
    public boolean isTest() {
        return false;
    }

    /**
     * Method to return whether the test descriptor is a container
     *
     * @return the return value
     */
    @Override
    public boolean isContainer() {
        return true;
    }

    /**
     * Method to get the test class
     *
     * @return the return value
     */
    public Class<?> getTestClass() {
        return testClass;
    }

    /**
     * Method to execute the test descriptor
     *
     * @param executorContext executorContext
     */
    @Override
    public void execute(ExecutorContext executorContext) {
        LOGGER.trace("execute uniqueId [%s] testClass [%s]", getUniqueId(), testClass.getName());

        this.executorContext = executorContext;

        try {
            stateMachine.run(State.BEGIN);
        } catch (Throwable t) {
            t.printStackTrace();
            System.out.flush();
        }
    }

    private void begin(StateMachine<State> stateStateMachine) {
        try {
            executorContext
                    .getExecutionRequest()
                    .getEngineExecutionListener()
                    .executionStarted(this);

            testInstance =
                    testClass.getDeclaredConstructor(NO_CLASS_ARGS).newInstance(NO_OBJECT_ARGS);

            executorContext.setTestInstance(testInstance);

            stateStateMachine.signal(State.PRE_PREPARE);
        } catch (Throwable t) {
            throwableCollector.accept(THROWABLE_UTILS.pruneStackTrace(testClass, t));
            stateStateMachine.signal(State.SKIP2);
            System.out.flush();
        }
    }

    private void prePrepare(StateMachine<State> stateMachine) {
        stateMachine.signal(State.PREPARE);
    }

    private void prepare(StateMachine<State> stateMachine) {
        try {
            List<Method> methods = REFLECTION_UTILS.getPrepareMethods(testClass);
            for (Method method : methods) {
                try {
                    LOCK_ANNOTATION_UTILS.processLockAnnotations(method);
                    method.invoke(testInstance, NO_OBJECT_ARGS);
                } finally {
                    LOCK_ANNOTATION_UTILS.processUnlockAnnotations(method);
                    System.out.flush();
                }
            }
        } catch (Throwable t) {
            throwableCollector.accept(THROWABLE_UTILS.pruneStackTrace(testClass, t));
        } finally {
            stateMachine.signal(State.POST_PREPARE);
            System.out.flush();
        }
    }

    private void postPrepare(StateMachine<State> stateMachine) {
        try {
            if (throwableCollector.isEmpty()) {
                stateMachine.signal(State.EXECUTE);
            } else {
                stateMachine.signal(State.SKIP);
            }
        } finally {
            System.out.flush();
        }
    }

    private void execute(StateMachine<State> stateMachine) {
        try {
            List<ArgumentTestDescriptor> argumentTestDescriptors =
                    getChildren(ArgumentTestDescriptor.class);
            for (ArgumentTestDescriptor argumentTestDescriptor : argumentTestDescriptors) {
                argumentTestDescriptor.execute(executorContext);
            }
        } catch (Throwable t) {
            throwableCollector.accept(THROWABLE_UTILS.pruneStackTrace(testClass, t));
        } finally {
            stateMachine.signal(State.PRE_CONCLUDE);
            System.out.flush();
        }
    }

    private void skip(StateMachine<State> stateMachine) {
        try {
            List<ArgumentTestDescriptor> argumentTestDescriptors =
                    getChildren(ArgumentTestDescriptor.class);
            for (ArgumentTestDescriptor argumentTestDescriptor : argumentTestDescriptors) {
                argumentTestDescriptor.skip(executorContext);
            }
        } catch (Throwable t) {
            throwableCollector.accept(THROWABLE_UTILS.pruneStackTrace(testClass, t));
        } finally {
            stateMachine.signal(State.PRE_CONCLUDE);
            System.out.flush();
        }
    }

    private void skip2(StateMachine<State> stateMachine) {
        try {
            List<ArgumentTestDescriptor> argumentTestDescriptors =
                    getChildren(ArgumentTestDescriptor.class);
            for (ArgumentTestDescriptor argumentTestDescriptor : argumentTestDescriptors) {
                argumentTestDescriptor.skip(executorContext);
            }
        } catch (Throwable t) {
            throwableCollector.accept(THROWABLE_UTILS.pruneStackTrace(testClass, t));
        } finally {
            stateMachine.signal(State.END);
            System.out.flush();
        }
    }

    private void preConclude(StateMachine<State> stateMachine) {
        stateMachine.signal(State.CONCLUDE);
    }

    private void conclude(StateMachine<State> stateMachine) {
        try {
            List<Method> methods = REFLECTION_UTILS.getConcludeMethods(testClass);
            for (Method method : methods) {
                try {
                    LOCK_ANNOTATION_UTILS.processLockAnnotations(method);
                    method.invoke(testInstance, NO_OBJECT_ARGS);
                } catch (Throwable t) {
                    throwableCollector.accept(THROWABLE_UTILS.pruneStackTrace(testClass, t));
                } finally {
                    LOCK_ANNOTATION_UTILS.processUnlockAnnotations(method);
                    System.out.flush();
                }
            }
        } catch (Throwable t) {
            throwableCollector.accept(t);
        } finally {
            stateMachine.signal(State.POST_CONCLUDE);
            System.out.flush();
        }
    }

    private void postConclude(StateMachine<State> stateMachine) {
        stateMachine.signal(State.END);
    }

    private void end(StateMachine<State> stateMachine) {
        AutoCloseAnnotationUtils.singleton()
                .processAutoCloseAnnotatedFields(
                        testInstance, "@TestEngine.Conclude", throwableCollector);

        if (throwableCollector.isEmpty()) {
            executorContext
                    .getExecutionRequest()
                    .getEngineExecutionListener()
                    .executionFinished(this, TestExecutionResult.successful());
        } else {
            executorContext
                    .getExecutionRequest()
                    .getEngineExecutionListener()
                    .executionFinished(
                            this,
                            TestExecutionResult.failed(throwableCollector.getFirst().orElse(null)));
        }

        stateMachine.stop();
        executorContext.complete();
        System.out.flush();
    }
}
