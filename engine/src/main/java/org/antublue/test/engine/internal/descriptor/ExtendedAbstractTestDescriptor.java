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

import static org.antublue.test.engine.TestEngine.ANTUBLUE_TEST_ENGINE_MAVEN_PLUGIN;

import java.io.PrintStream;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.antublue.test.engine.Constants;
import org.antublue.test.engine.internal.ExecutorContext;
import org.antublue.test.engine.internal.util.StopWatch;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;

/** Class to implement an extended AbstractTestDescriptor */
@SuppressWarnings("unchecked")
public abstract class ExtendedAbstractTestDescriptor extends AbstractTestDescriptor {

    /** Constant to represent no class arguments */
    protected static final Class<?>[] NO_CLASS_ARGS = null;

    /** Constant to represent no object arguments */
    protected static final Object[] NO_OBJECT_ARGS = null;

    /** Constant to determine we are being executed via the Maven Test Engine plugin */
    protected static final boolean EXECUTED_VIA_ANTUBLUE_TEST_ENGINE_MAVEN_PLUGIN =
            Constants.TRUE.equals(System.getProperty(ANTUBLUE_TEST_ENGINE_MAVEN_PLUGIN));

    private final StopWatch stopWatch;

    /**
     * Constructor
     *
     * @param uniqueId uniqueId
     * @param displayName displayName
     */
    protected ExtendedAbstractTestDescriptor(UniqueId uniqueId, String displayName) {
        super(uniqueId, displayName);

        stopWatch = new StopWatch();
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
        return getChildren().stream()
                .map((Function<TestDescriptor, T>) testDescriptor -> (T) testDescriptor)
                .collect(Collectors.toList());
    }

    public StopWatch getStopWatch() {
        return stopWatch;
    }

    /**
     * Method to execute the TestDescriptor
     *
     * @param executorContext testEngineExecutorContext
     */
    public abstract void execute(ExecutorContext executorContext);

    /**
     * Method to skip the TestDescriptor's children, then the TestDescriptor (recursively)
     *
     * @param executorContext testEngineExecutorContext
     */
    public void skip(ExecutorContext executorContext) {
        for (ExtendedAbstractTestDescriptor testDescriptor :
                getChildren(ExtendedAbstractTestDescriptor.class)) {
            testDescriptor.skip(executorContext);
        }

        executorContext
                .getExecutionRequest()
                .getEngineExecutionListener()
                .executionSkipped(this, "Skipped");
    }

    protected Throwable prune(Class<?> clazz, Throwable throwable) {
        // TODO fix
        return throwable;
        /*
        Throwable prunedThrowable = throwable;
        if (!isClassInStackTrace(clazz, throwable)) {
            prunedThrowable = throwable.getCause();
        }

        Throwable t = prunedThrowable;
        while (t != null) {
            pruneStackTraceElements(t);
            t = t.getCause();
        }

        return prunedThrowable;
        */
    }

    /**
     * Method to print a stacktrace depending on whether we have been executed via the Maven Test
     * Engine plugin
     *
     * @param printStream printStream
     * @param throwable throwable
     */
    protected void printStackTrace(PrintStream printStream, Throwable throwable) {
        if (EXECUTED_VIA_ANTUBLUE_TEST_ENGINE_MAVEN_PLUGIN) {
            throwable.printStackTrace(printStream);
            printStream.flush();
        }
    }

    /*
    private static boolean isClassInStackTrace(Class<?> clazz, Throwable throwable) {
        String className = clazz.getName();
        StackTraceElement[] stackTraceElements = throwable.getStackTrace();
        for (StackTraceElement stackTraceElement : stackTraceElements) {
            if (stackTraceElement.toString().startsWith(className)) {
                return true;
            }
        }
        return false;
    }

    private static void pruneStackTraceElements(Throwable throwable) {
        List<StackTraceElement> prunedStackTraceElements = new ArrayList<>();
        List<StackTraceElement> stackTraceElements = Arrays.asList(throwable.getStackTrace());

        for (StackTraceElement stackTraceElement : stackTraceElements) {
            if (stackTraceElement
                    .toString()
                    .startsWith("org.antublue.test.engine.internal.descriptor.")) {
                break;
            }
            prunedStackTraceElements.add(stackTraceElement);
        }

        throwable.setStackTrace(prunedStackTraceElements.toArray(new StackTraceElement[0]));
    }
    */
}
