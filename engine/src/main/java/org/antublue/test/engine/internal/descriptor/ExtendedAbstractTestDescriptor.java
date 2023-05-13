/*
 * Copyright (C) 2022-2023 The AntuBLUE test-engine project authors
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

import org.antublue.test.engine.internal.TestEngineExecutionContext;
import org.antublue.test.engine.internal.util.ThrowableCollector;
import org.antublue.test.engine.internal.util.ThrowableConsumerException;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Class to implement an extended AbstractTestDescriptor
 */
@SuppressWarnings("unchecked")
abstract class ExtendedAbstractTestDescriptor extends AbstractTestDescriptor {

    private final ThrowableCollector throwableCollector;

    /**
     * Constructor
     *
     * @param uniqueId uniqueId
     * @param displayName displayName
     */
    protected ExtendedAbstractTestDescriptor(UniqueId uniqueId, String displayName) {
        super(uniqueId, displayName);
        throwableCollector = new ThrowableCollector();
    }

    /**
     * Method to get a List of children cast as a specific Class
     *
     * @param clazz clazz
     * @return the return value
     * @param <T> the return type
     */
    public <T> List<T> getChildren(Class<T> clazz) {
        // Clazz is required to be able to get the generic type
        return getChildren()
                .stream()
                .map((Function<TestDescriptor, T>) testDescriptor -> (T) testDescriptor)
                .collect(Collectors.toList());
    }

    /**
     * Method to get the test descriptors ThrowableCollector
     *
     * @return the return value
     */
    protected ThrowableCollector getThrowableCollector() {
        return throwableCollector;
    }

    /**
     * Method to prune a Throwable stacktrace
     *
     * @param throwable throwable
     * @param markerClassName markerClassName
     * @return the return value
     */
    protected Throwable pruneStackTrace(Throwable throwable, String markerClassName) {
        if (throwable instanceof InvocationTargetException) {
            throwable = throwable.getCause();
        }

        if (throwable instanceof ThrowableConsumerException) {
            throwable = throwable.getCause();
        }

        /*
         * Check the Throwable cause again, since the invocation may
         * have been wrapped by a ThrowableConsumerException
         */
        if (throwable instanceof InvocationTargetException) {
            throwable = throwable.getCause();
        }

        List<StackTraceElement> workingStackTrace = new ArrayList<>();
        List<StackTraceElement> stackTraceElements = Arrays.asList(throwable.getStackTrace());

        Iterator<StackTraceElement> stackTraceElementIterator = stackTraceElements.iterator();
        while (stackTraceElementIterator.hasNext()) {
            StackTraceElement stackTraceElement = stackTraceElementIterator.next();
            String stackTraceClassName = stackTraceElement.getClassName();
            workingStackTrace.add(stackTraceElement);
            if (stackTraceClassName.equals(markerClassName)) {
                break;
            }
        }

        throwable.setStackTrace(workingStackTrace.toArray(new StackTraceElement[0]));
        return throwable;
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

    /**
     * Method to test the TestDescriptor
     *
     * @param testEngineExecutionContext testEngineExecutionContext
     */
    public abstract void execute(TestEngineExecutionContext testEngineExecutionContext);

    /**
     * Method to skip the TestDescriptor's children, then the TestDescriptor (recursively)
     *
     * @param testEngineExecutionContext testEngineExecutionContext
     */
    public void skip(TestEngineExecutionContext testEngineExecutionContext) {
        getChildren(ExtendedAbstractTestDescriptor.class).forEach(
                testDescriptor -> testDescriptor.skip(testEngineExecutionContext));

        testEngineExecutionContext
                .getExecutionRequest()
                .getEngineExecutionListener()
                .executionSkipped(this, "Skipped");
    }
}
