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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import org.antublue.test.engine.internal.metadata.Metadata;
import org.antublue.test.engine.internal.metadata.MetadataInformation;
import org.antublue.test.engine.internal.util.StopWatch;
import org.antublue.test.engine.internal.util.ThrowableCollector;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;

/** Abstract class to implement an ExecutableTestDescriptor */
@SuppressWarnings("PMD.EmptyCatchBlock")
public abstract class ExecutableTestDescriptor extends AbstractTestDescriptor implements Metadata {

    private final ThrowableCollector throwableCollector;
    private final MetadataInformation metadataInformation;
    private final StopWatch stopWatch;

    /**
     * Constructor
     *
     * @param uniqueId uniqueId
     * @param displayName displayName
     */
    protected ExecutableTestDescriptor(UniqueId uniqueId, String displayName) {
        super(uniqueId, displayName);

        throwableCollector = new ThrowableCollector();
        metadataInformation = new MetadataInformation();
        stopWatch = new StopWatch();
    }

    /**
     * Method to execute the test descriptor
     *
     * @param executionRequest executionRequest
     */
    public abstract void execute(ExecutionRequest executionRequest, Object testInstance);

    /**
     * Method to skip child test descriptors
     *
     * @param executionRequest executionRequest
     */
    public abstract void skip(ExecutionRequest executionRequest);

    @Override
    public MetadataInformation getMetadata() {
        return metadataInformation;
    }

    @Override
    public String toString() {
        return getDisplayName();
    }

    /**
     * Method to get the stop watch
     *
     * @return the stop watch
     */
    protected StopWatch getStopWatch() {
        return stopWatch;
    }

    /**
     * Method to get the throwable collector
     *
     * @return the throwable collector
     */
    protected ThrowableCollector getThrowableCollector() {
        return throwableCollector;
    }

    /**
     * Method to collect all Throwables from parent and children
     *
     * @return a List of Throwables
     */
    public List<Throwable> collectThrowables() {
        List<Throwable> throwables = new ArrayList<>();

        if (getThrowableCollector().isNotEmpty()) {
            throwables.addAll(getThrowableCollector().getThrowables());
        }

        getChildren()
                .forEach(
                        (Consumer<TestDescriptor>)
                                testDescriptor -> {
                                    if (testDescriptor instanceof ExecutableTestDescriptor) {
                                        ExecutableTestDescriptor executableTestDescriptor =
                                                (ExecutableTestDescriptor) testDescriptor;
                                        List<Throwable> childThrowables =
                                                executableTestDescriptor.collectThrowables();
                                        if (childThrowables != null) {
                                            throwables.addAll(childThrowables);
                                        }
                                    }
                                });

        return throwables;
    }
}
