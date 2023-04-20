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

package org.antublue.test.engine.internal.descriptor;

import org.antublue.test.engine.internal.TestEngineReflectionUtils;
import org.antublue.test.engine.internal.TestExecutionContext;
import org.antublue.test.engine.internal.logger.Logger;
import org.antublue.test.engine.internal.logger.LoggerFactory;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.ClassSource;

import java.lang.reflect.Method;
import java.util.Optional;

public final class RunnableClassTestDescriptor extends AbstractRunnableTestDescriptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(RunnableClassTestDescriptor.class);

    private final Class<?> testClass;

    public RunnableClassTestDescriptor(UniqueId uniqueId, String displayName, Class<?> testClass) {
        super(uniqueId, displayName);
        this.testClass = testClass;
    }

    @Override
    public Optional<TestSource> getSource() {
        return Optional.of(ClassSource.from(testClass));
    }

    @Override
    public Type getType() {
        return Type.CONTAINER;
    }

    @Override
    public boolean isTest() {
        return false;
    }

    @Override
    public boolean isContainer() {
        return true;
    }

    public void run() {
        TestExecutionContext testExecutionContext = getTestExecutionContext();
        ThrowableCollector throwableCollector = getThrowableCollector();

        EngineExecutionListener engineExecutionListener =
                testExecutionContext.getExecutionRequest().getEngineExecutionListener();

        engineExecutionListener.executionStarted(this);

        String testClassName = testClass.getName();
        Object testInstance = null;

        try {
            testInstance = testClass.getDeclaredConstructor((Class<?>[]) null).newInstance((Object[]) null);
            testExecutionContext.setTestInstance(testInstance);

            LOGGER.trace("invoking [%s] @TestEngine.BeforeClass methods ...", testClassName);
            for (Method beforeClass : TestEngineReflectionUtils.getBeforeClassMethods(testClass)) {
                LOGGER.trace(
                        String.format(
                                "invoking [%s] @TestEngine.BeforeClass method [%s] ...",
                                testClassName,
                                beforeClass.getName()));

                try {
                    beforeClass.invoke(null, (Object[]) null);
                } finally {
                    flush();
                }
            }
        } catch (Throwable t) {
            throwableCollector.add(t);
            resolve(t).printStackTrace();
        }

        if (throwableCollector.isEmpty()) {
            getChildren(RunnableParameterTestDescriptor.class)
                    .forEach(executableParameterTestDescriptor -> {
                        executableParameterTestDescriptor.setTestExecutionContext(testExecutionContext);
                        executableParameterTestDescriptor.run();
                        throwableCollector.addAll(executableParameterTestDescriptor.getThrowableCollector());
                    });
        }

        try {
            LOGGER.trace("invoking [%s] @TestEngine.AfterClass methods ...", testClassName);
            for (Method afterClassMethod : TestEngineReflectionUtils.getAfterClassMethods(testClass)) {
                LOGGER.trace(
                        String.format(
                                "invoking [%s] @TestEngine.AfterClass method [%s] ...",
                                testClassName,
                                afterClassMethod.getName()));

                try {
                    afterClassMethod.invoke(testInstance, (Object[]) null);
                } finally {
                    flush();
                }
            }
        } catch (Throwable t) {
            throwableCollector.add(t);
            resolve(t).printStackTrace();
        }

        if (throwableCollector.isEmpty()) {
            engineExecutionListener.executionFinished(this, TestExecutionResult.successful());
        } else {
            engineExecutionListener.executionFinished(this, TestExecutionResult.failed(throwableCollector.getFirst().get()));
        }

        testExecutionContext.setTestInstance(null);

        getTestExecutionContext().getCountDownLatch().countDown();
    }

    public Class<?> getTestClass() {
        return testClass;
    }
}
