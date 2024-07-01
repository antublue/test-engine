/*
 * Copyright (C) 2024 The AntuBLUE test-engine project authors
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

package org.antublue.test.engine.internal.extension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.antublue.test.engine.api.TestEngineExtension;
import org.antublue.test.engine.exception.TestEngineException;
import org.antublue.test.engine.internal.discovery.Predicates;
import org.antublue.test.engine.internal.support.ClassSupport;
import org.antublue.test.engine.internal.support.ObjectSupport;
import org.antublue.test.engine.internal.support.OrdererSupport;

/** Class to implement TestEngineExtensionManager */
public class TestEngineExtensionManager {

    private final List<TestEngineExtension> testEngineExtensions;
    private boolean initialized;

    /** Constructor */
    private TestEngineExtensionManager() {
        testEngineExtensions = new ArrayList<>();
    }

    /** Method to load test engine extensions */
    private synchronized void initialize() {
        if (!initialized) {
            List<Class<?>> classes =
                    ClassSupport.findClasses(Predicates.TEST_ENGINE_EXTENSION_CLASS);

            OrdererSupport.orderTestClasses(classes);

            for (Class<?> clazz : classes) {
                try {
                    testEngineExtensions.add(ObjectSupport.createObject(clazz));
                } catch (Throwable t) {
                    throw new TestEngineException(t);
                }
            }

            initialized = true;
        }
    }

    /**
     * Method to prepare test engine extensions
     *
     * @throws Throwable Throwable
     */
    public void prepare() throws Throwable {
        initialize();

        for (TestEngineExtension testEngineExtension : testEngineExtensions) {
            testEngineExtension.prepareCallback();
        }
    }

    /**
     * Method to conclude test engine extensions
     *
     * @return a List of Throwables
     */
    public List<Throwable> conclude() {
        initialize();

        List<Throwable> throwables = new ArrayList<>();

        List<TestEngineExtension> testEngineExtensions = new ArrayList<>(this.testEngineExtensions);
        Collections.reverse(testEngineExtensions);

        for (TestEngineExtension testEngineExtension : testEngineExtensions) {
            try {
                testEngineExtension.concludeCallback();
            } catch (Throwable t) {
                throwables.add(t);
            }
        }

        return throwables;
    }

    /**
     * Method to get a singleton instance
     *
     * @return the singleton instance
     */
    public static TestEngineExtensionManager getInstance() {
        return SingletonHolder.SINGLETON;
    }

    /** Class to hold the singleton instance */
    private static class SingletonHolder {

        /** The singleton instance */
        public static final TestEngineExtensionManager SINGLETON = new TestEngineExtensionManager();
    }
}
