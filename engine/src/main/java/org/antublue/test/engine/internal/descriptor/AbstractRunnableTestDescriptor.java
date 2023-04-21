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

import org.antublue.test.engine.internal.TestExecutionContext;
import org.antublue.test.engine.internal.util.ThrowableCollector;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Class to implement a Runnable test descriptor
 */
@SuppressWarnings("unchecked")
abstract class AbstractRunnableTestDescriptor
        extends org.junit.platform.engine.support.descriptor.AbstractTestDescriptor
        implements Runnable {

    private final ThrowableCollector throwableCollector;
    private TestExecutionContext testExecutionContext;

    /**
     * Constructor
     *
     * @param uniqueId
     * @param displayName
     */
    protected AbstractRunnableTestDescriptor(UniqueId uniqueId, String displayName) {
        super(uniqueId, displayName);
        throwableCollector = new ThrowableCollector();
    }

    /**
     * Method to get a List of children cast as a specific class
     *
     * @param clazz
     * @return
     * @param <T>
     */
    public <T> List<T> getChildren(Class<T> clazz) {
        return getChildren()
                .stream()
                .map((Function<TestDescriptor, T>) testDescriptor -> (T) testDescriptor)
                .collect(Collectors.toList());
    }

    /**
     * Method to set the TestExecutionContext
     *
     * @param testExecutionContext
     */
    public void setTestExecutionContext(TestExecutionContext testExecutionContext) {
        this.testExecutionContext = testExecutionContext;
    }

    /**
     * Method to get the TestExecutionContext
     *
     * @return
     */
    protected TestExecutionContext getTestExecutionContext() {
        return testExecutionContext;
    }

    /**
     * Method to get the test descriptors ThrowableCollector
     *
     * @return
     */
    protected ThrowableCollector getThrowableCollector() {
        return throwableCollector;
    }

    /**
     * Method to run the test descriptor
     * <br/>
     * The TestExecutionContext must be set prior to the call
     */
    public abstract void run();

    /**
     * Method to resolve an Exception to the underlying Exception
     *
     * @param t
     * @return
     */
    protected static Throwable resolve(Throwable t) {
        if (t instanceof RuntimeException) {
            t = t.getCause();
        }

        if (t instanceof InvocationTargetException) {
            return t.getCause();
        }

        return t;
    }

    /**
     * Method to flush System.out and System.err PrintStreams
     */
    public void flush() {
        synchronized (System.out) {
            synchronized (System.err) {
                System.out.flush();
                System.err.flush();
            }
        }
    }

}
