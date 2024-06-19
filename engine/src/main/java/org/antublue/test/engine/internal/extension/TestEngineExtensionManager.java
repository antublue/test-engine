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
import java.util.Comparator;
import java.util.List;
import org.antublue.test.engine.internal.support.ClassSupport;
import org.antublue.test.engine.internal.support.OrdererSupport;
import org.antublue.test.engine.internal.util.Predicates;

/** Class to implement TestEngineExtensionManager */
public class TestEngineExtensionManager {

    private final List<TestEngineExtension> engineExtensions;

    /** Constructor */
    public TestEngineExtensionManager() {
        engineExtensions = new ArrayList<>();
    }

    /** Method to load test engine extensions */
    public void load() {
        List<Class<?>> classes = ClassSupport.discoverClasses(Predicates.ENGINE_EXTENSION_CLASS);

        classes.sort(Comparator.comparing(Class::getName));
        OrdererSupport.orderTestClasses(classes);

        for (Class<?> clazz : classes) {
            engineExtensions.add(TestEngineExtension.createExtension(clazz));
        }
    }

    /**
     * Method to initialize test engine extensions
     *
     * @throws Throwable Throwable
     */
    public void initialize() throws Throwable {
        for (TestEngineExtension engineExtension : engineExtensions) {
            engineExtension.initialize();
        }
    }

    /**
     * Method to cleanup test engine extensions
     *
     * @return
     */
    public List<Throwable> cleanup() {
        List<Throwable> throwables = new ArrayList<>();

        List<TestEngineExtension> engineExtensions = new ArrayList<>(this.engineExtensions);
        Collections.reverse(engineExtensions);

        for (TestEngineExtension engineExtension : engineExtensions) {
            try {
                engineExtension.cleanup();
            } catch (Throwable t) {
                throwables.add(t);
            }
        }

        return throwables;
    }
}
