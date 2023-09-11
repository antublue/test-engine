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

package org.antublue.test.engine.maven.plugin.listener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.reporting.ReportEntry;

/** Class to implement a DelegatingEngineExecutionListener */
public class DelegatingEngineExecutionListener implements EngineExecutionListener {

    private final List<EngineExecutionListener> engineExecutionListeners;

    /** Constructor */
    private DelegatingEngineExecutionListener() {
        engineExecutionListeners = new ArrayList<>();
    }

    /**
     * Method to add a delegated EngineExecutionListener listener
     *
     * @param delegatedEngineExecutionListener delegatedEngineExecutionListener
     */
    private void addListener(EngineExecutionListener delegatedEngineExecutionListener) {
        engineExecutionListeners.add(delegatedEngineExecutionListener);
    }

    @Override
    public void dynamicTestRegistered(TestDescriptor testDescriptor) {
        engineExecutionListeners.forEach(
                engineExecutionListener ->
                        engineExecutionListener.dynamicTestRegistered(testDescriptor));
    }

    @Override
    public void executionSkipped(TestDescriptor testDescriptor, String reason) {
        engineExecutionListeners.forEach(
                engineExecutionListener ->
                        engineExecutionListener.executionSkipped(testDescriptor, reason));
    }

    @Override
    public void executionStarted(TestDescriptor testDescriptor) {
        engineExecutionListeners.forEach(
                engineExecutionListener ->
                        engineExecutionListener.executionStarted(testDescriptor));
    }

    @Override
    public void executionFinished(
            TestDescriptor testDescriptor, TestExecutionResult testExecutionResult) {
        engineExecutionListeners.forEach(
                engineExecutionListener ->
                        engineExecutionListener.executionFinished(
                                testDescriptor, testExecutionResult));
    }

    @Override
    public void reportingEntryPublished(TestDescriptor testDescriptor, ReportEntry entry) {
        engineExecutionListeners.forEach(
                engineExecutionListener ->
                        engineExecutionListener.reportingEntryPublished(testDescriptor, entry));
    }

    /**
     * Method to create an instance of a DelegatingEngineExecutionListener with an array of delegate
     * EngineExecutionListeners
     *
     * @param engineExecutionListeners engineExecutionListeners
     * @return a DelegatingEngineExecutionListener
     */
    public static DelegatingEngineExecutionListener of(
            EngineExecutionListener... engineExecutionListeners) {
        DelegatingEngineExecutionListener delegatingEngineExecutionListener =
                new DelegatingEngineExecutionListener();

        Arrays.stream(engineExecutionListeners)
                .forEach(delegatingEngineExecutionListener::addListener);

        return delegatingEngineExecutionListener;
    }
}
