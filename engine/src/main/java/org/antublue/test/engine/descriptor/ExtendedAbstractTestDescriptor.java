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

package org.antublue.test.engine.descriptor;

import java.io.PrintStream;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.antublue.test.engine.TestEngineUtils;
import org.antublue.test.engine.configuration.Constants;
import org.antublue.test.engine.descriptor.util.AutoCloseProcessor;
import org.antublue.test.engine.descriptor.util.LockProcessor;
import org.antublue.test.engine.executor.ExecutorContext;
import org.antublue.test.engine.util.StopWatch;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;

/** Class to implement an extended AbstractTestDescriptor */
@SuppressWarnings("unchecked")
public abstract class ExtendedAbstractTestDescriptor extends AbstractTestDescriptor {

    protected static final TestEngineUtils TEST_ENGINE_REFLECTION_UTILS =
            TestEngineUtils.singleton();

    protected static final LockProcessor LOCK_PROCESSOR = LockProcessor.singleton();

    protected static final AutoCloseProcessor AUTO_CLOSE_FIELD_PROCESSOR =
            AutoCloseProcessor.singleton();

    /** Constant to represent no class arguments */
    protected static final Class<?>[] NO_CLASS_ARGS = null;

    /** Constant to represent no object arguments */
    protected static final Object[] NO_OBJECT_ARGS = null;

    /** Constant to determine we are being executed via the Maven Test Engine plugin */
    protected static final boolean EXECUTED_VIA_MAVEN_PLUGIN =
            Constants.TRUE.equals(System.getProperty(Constants.MAVEN_PLUGIN));

    protected Status status = Status.PASS;

    private final StopWatch stopWatch;

    public enum Status {
        PASS,
        FAIL,
        SKIPPED
    }

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
     * @param testClass testClass
     * @return the return value
     * @param <T> the return type
     */
    public <T> List<T> getChildren(Class<T> testClass) {
        // testClass is required to be able to get the generic type
        return getChildren().stream()
                .map((Function<TestDescriptor, T>) testDescriptor -> (T) testDescriptor)
                .collect(Collectors.toList());
    }

    /**
     * Method to get this test descriptor's stop watch
     *
     * @return the descriptor's stop watch
     */
    public StopWatch getStopWatch() {
        return stopWatch;
    }

    public abstract void setStatus(Status status);

    public Status getStatus() {
        return status;
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
        setStatus(Status.SKIPPED);

        for (ExtendedAbstractTestDescriptor testDescriptor :
                getChildren(ExtendedAbstractTestDescriptor.class)) {
            testDescriptor.skip(executorContext);
        }

        executorContext
                .getExecutionRequest()
                .getEngineExecutionListener()
                .executionSkipped(this, "Skipped");
    }

    protected void println(String format, Object... objects) {
        System.out.println(String.format(format, objects));
        System.out.flush();
    }

    /**
     * Method to print a stacktrace depending on whether we have been executed via the Maven Test
     * Engine plugin
     *
     * @param printStream printStream
     * @param throwable throwable
     */
    protected void printStackTrace(PrintStream printStream, Throwable throwable) {
        if (EXECUTED_VIA_MAVEN_PLUGIN) {
            throwable.printStackTrace(printStream);
            printStream.flush();
        }
    }

    protected void flush() {
        System.out.flush();
    }
}
