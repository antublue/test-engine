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

import org.antublue.test.engine.internal.TestEngineExecutorContext;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Class to implement an extended AbstractTestDescriptor
 */
@SuppressWarnings("unchecked")
abstract class ExtendedAbstractTestDescriptor extends AbstractTestDescriptor {

    protected Object testInstance;

    /**
     * Constructor
     *
     * @param uniqueId uniqueId
     * @param displayName displayName
     */
    protected ExtendedAbstractTestDescriptor(UniqueId uniqueId, String displayName) {
        super(uniqueId, displayName);
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
     * Method to set the test instance
     *
     * @param testInstance testInstance
     */
    public void setTestInstance(Object testInstance) {
        this.testInstance = testInstance;
    }

    /**
     * Method to execute the TestDescriptor
     *
     * @param testEngineExecutorContext testEngineExecutorContext
     */
    public abstract void execute(TestEngineExecutorContext testEngineExecutorContext);

    /**
     * Method to skip the TestDescriptor's children, then the TestDescriptor (recursively)
     *
     * @param testEngineExecutorContext testEngineExecutorContext
     */
    public void skip(TestEngineExecutorContext testEngineExecutorContext) {
        getChildren(ExtendedAbstractTestDescriptor.class)
                .forEach(testDescriptor -> {
                    testDescriptor.skip(testEngineExecutorContext);
                });

        testEngineExecutorContext.getEngineExecutionListener().executionSkipped(this, "Skipped");
    }

    /**
     * Method to prune a Throwable stacktrace
     *
     * @param throwable throwable
     * @param markerClassName markerClassName
     * @return the return value
     */
    /*
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
    /*
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
    */

    /**
     * Method to flush System.out and System.err PrintStreams
     */
    /*
    public void flush() {
        synchronized (System.out) {
            synchronized (System.err) {
                System.out.flush();
                System.err.flush();
            }
        }
    }
    */
}
