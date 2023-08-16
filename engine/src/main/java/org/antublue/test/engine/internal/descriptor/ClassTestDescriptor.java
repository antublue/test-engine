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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.antublue.test.engine.internal.AutoCloseAnnotationUtils;
import org.antublue.test.engine.internal.ExecutorContext;
import org.antublue.test.engine.internal.logger.Logger;
import org.antublue.test.engine.internal.logger.LoggerFactory;
import org.antublue.test.engine.internal.statemachine.StateMachine;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.ClassSource;

/** Class to implement a class test descriptor */
public final class ClassTestDescriptor extends ExtendedAbstractTestDescriptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClassTestDescriptor.class);

    private enum State {
        BEGIN,
        PRE_PREPARE,
        PREPARE,
        POST_PREPARE,
        EXECUTE,
        SKIP,
        SKIP_END,
        PRE_CONCLUDE,
        CONCLUDE,
        POST_CONCLUDE,
        END
    }

    private final Class<?> testClass;
    private final List<Throwable> throwables;
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
        this.throwables = new ArrayList<>();
        this.stateMachine =
                new StateMachine<State>(getClass().getName())
                        .addTransition(State.BEGIN, this::begin)
                        .addTransition(State.PRE_PREPARE, this::prePrepare)
                        .addTransition(State.PREPARE, this::prepare)
                        .addTransition(State.POST_PREPARE, this::postPrepare)
                        .addTransition(State.EXECUTE, this::execute)
                        .addTransition(State.SKIP, this::skip)
                        .addTransition(State.SKIP_END, this::skipEnd)
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

        stateMachine
                .run(State.BEGIN)
                .ifPresent(
                        throwable -> {
                            if (EXECUTED_VIA_ANTUBLUE_TEST_ENGINE_MAVEN_PLUGIN) {
                                throwable.printStackTrace(System.out);
                                System.out.flush();
                            }
                        });
    }

    /**
     * State machine transition
     *
     * @param stateMachine stateMachine
     */
    private void begin(StateMachine<State> stateMachine) {
        LOGGER.trace("begin uniqueId [%s] testClass [%s]", getUniqueId(), testClass.getName());

        try {
            executorContext
                    .getExecutionRequest()
                    .getEngineExecutionListener()
                    .executionStarted(this);

            testInstance =
                    testClass.getDeclaredConstructor(NO_CLASS_ARGS).newInstance(NO_OBJECT_ARGS);

            executorContext.setTestInstance(testInstance);

            stateMachine.signal(State.PRE_PREPARE);
        } catch (Throwable t) {
            throwables.add(t);
            printStackTrace(System.out, t);
            stateMachine.signal(State.SKIP_END);
        } finally {
            System.out.flush();
        }
    }

    /**
     * State machine transition
     *
     * @param stateMachine stateMachine
     */
    private void prePrepare(StateMachine<State> stateMachine) {
        LOGGER.trace("prePrepare uniqueId [%s] testClass [%s]", getUniqueId(), testClass.getName());
        stateMachine.signal(State.PREPARE);
    }

    /**
     * State machine transition
     *
     * @param stateMachine stateMachine
     */
    private void prepare(StateMachine<State> stateMachine) {
        LOGGER.trace("prepare uniqueId [%s] testClass [%s]", getUniqueId(), testClass.getName());

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
            throwables.add(t);
            printStackTrace(System.out, t);
        } finally {
            stateMachine.signal(State.POST_PREPARE);
            System.out.flush();
        }
    }

    /**
     * State machine transition
     *
     * @param stateMachine stateMachine
     */
    private void postPrepare(StateMachine<State> stateMachine) {
        LOGGER.trace(
                "postPrepare uniqueId [%s] testClass [%s]", getUniqueId(), testClass.getName());

        try {
            if (throwables.isEmpty()) {
                stateMachine.signal(State.EXECUTE);
            } else {
                stateMachine.signal(State.SKIP);
            }
        } finally {
            System.out.flush();
        }
    }

    /**
     * State machine transition
     *
     * @param stateMachine stateMachine
     */
    private void execute(StateMachine<State> stateMachine) {
        LOGGER.trace("execute uniqueId [%s] testClass [%s]", getUniqueId(), testClass.getName());

        try {
            List<ArgumentTestDescriptor> argumentTestDescriptors =
                    getChildren(ArgumentTestDescriptor.class);
            for (ArgumentTestDescriptor argumentTestDescriptor : argumentTestDescriptors) {
                argumentTestDescriptor.execute(executorContext);
            }
        } catch (Throwable t) {
            throwables.add(t);
            printStackTrace(System.out, t);
        } finally {
            stateMachine.signal(State.PRE_CONCLUDE);
            System.out.flush();
        }
    }

    /**
     * State machine transition
     *
     * @param stateMachine stateMachine
     */
    private void skip(StateMachine<State> stateMachine) {
        LOGGER.trace("skip uniqueId [%s] testClass [%s]", getUniqueId(), testClass.getName());

        try {
            List<ArgumentTestDescriptor> argumentTestDescriptors =
                    getChildren(ArgumentTestDescriptor.class);
            for (ArgumentTestDescriptor argumentTestDescriptor : argumentTestDescriptors) {
                argumentTestDescriptor.skip(executorContext);
            }
        } catch (Throwable t) {
            throwables.add(t);
            printStackTrace(System.out, t);
        } finally {
            stateMachine.signal(State.PRE_CONCLUDE);
            System.out.flush();
        }
    }

    /**
     * State machine transition
     *
     * @param stateMachine stateMachine
     */
    private void skipEnd(StateMachine<State> stateMachine) {
        LOGGER.trace("skip2 uniqueId [%s] testClass [%s]", getUniqueId(), testClass.getName());

        try {
            List<ArgumentTestDescriptor> argumentTestDescriptors =
                    getChildren(ArgumentTestDescriptor.class);
            for (ArgumentTestDescriptor argumentTestDescriptor : argumentTestDescriptors) {
                argumentTestDescriptor.skip(executorContext);
            }
        } catch (Throwable t) {
            throwables.add(t);
            printStackTrace(System.out, t);
        } finally {
            stateMachine.signal(State.END);
            System.out.flush();
        }
    }

    /**
     * State machine transition
     *
     * @param stateMachine stateMachine
     */
    private void preConclude(StateMachine<State> stateMachine) {
        LOGGER.trace(
                "preConclude uniqueId [%s] testClass [%s]", getUniqueId(), testClass.getName());
        stateMachine.signal(State.CONCLUDE);
    }

    /**
     * State machine transition
     *
     * @param stateMachine stateMachine
     */
    private void conclude(StateMachine<State> stateMachine) {
        LOGGER.trace("conclude uniqueId [%s] testClass [%s]", getUniqueId(), testClass.getName());

        try {
            List<Method> methods = REFLECTION_UTILS.getConcludeMethods(testClass);
            for (Method method : methods) {
                try {
                    LOCK_ANNOTATION_UTILS.processLockAnnotations(method);
                    method.invoke(testInstance, NO_OBJECT_ARGS);
                } catch (Throwable t) {
                    throwables.add(t);
                    printStackTrace(System.out, t);
                } finally {
                    LOCK_ANNOTATION_UTILS.processUnlockAnnotations(method);
                    System.out.flush();
                }
            }
        } catch (Throwable t) {
            throwables.add(t);
            printStackTrace(System.out, t);
        } finally {
            stateMachine.signal(State.POST_CONCLUDE);
            System.out.flush();
        }
    }

    /**
     * State machine transition
     *
     * @param stateMachine stateMachine
     */
    private void postConclude(StateMachine<State> stateMachine) {
        LOGGER.trace(
                "postConclude uniqueId [%s] testClass [%s]", getUniqueId(), testClass.getName());
        stateMachine.signal(State.END);
    }

    /**
     * State machine transition
     *
     * @param stateMachine stateMachine
     */
    private void end(StateMachine<State> stateMachine) {
        LOGGER.trace("end uniqueId [%s] testClass [%s]", getUniqueId(), testClass.getName());

        AutoCloseAnnotationUtils.singleton()
                .processAutoCloseAnnotatedFields(testInstance, "@TestEngine.Conclude", throwables);

        if (throwables.isEmpty()) {
            executorContext
                    .getExecutionRequest()
                    .getEngineExecutionListener()
                    .executionFinished(this, TestExecutionResult.successful());
        } else {
            executorContext
                    .getExecutionRequest()
                    .getEngineExecutionListener()
                    .executionFinished(this, TestExecutionResult.failed(throwables.get(0)));
        }

        stateMachine.stop();
        executorContext.complete();
        System.out.flush();
    }
}
