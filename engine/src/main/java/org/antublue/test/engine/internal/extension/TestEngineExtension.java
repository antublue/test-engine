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

import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.List;
import org.antublue.test.engine.internal.support.MethodSupport;
import org.antublue.test.engine.internal.support.ObjectSupport;
import org.antublue.test.engine.internal.util.Predicates;
import org.junit.platform.commons.support.HierarchyTraversalMode;

/** Class to implement TestEngineExtension */
public class TestEngineExtension {

    private final Class<?> clazz;
    private final List<Method> initializeMethods;
    private final List<Method> cleanupMethods;

    private Object object;

    /**
     * Constructor
     *
     * @param clazz clazz
     * @param initializeMethods initializeMethods
     * @param cleanupMethods cleanupMethods
     */
    private TestEngineExtension(
            Class<?> clazz, List<Method> initializeMethods, List<Method> cleanupMethods) {
        this.clazz = clazz;
        this.initializeMethods = initializeMethods;
        this.cleanupMethods = cleanupMethods;
    }

    /**
     * Method to initialize the test engine extension
     *
     * @throws Throwable Throwable
     */
    public void initialize() throws Throwable {
        if (object == null) {
            object = ObjectSupport.createObject(clazz);
        }

        for (Method method : initializeMethods) {
            method.invoke(object);
        }
    }

    /**
     * Method to cleanup the test engine extension
     *
     * @throws Throwable Throwable
     */
    public void cleanup() throws Throwable {
        for (Method method : cleanupMethods) {
            method.invoke(object);
        }
    }

    /**
     * Method to create a test engine extension
     *
     * @param clazz clazz
     * @return a TestEngineExtension
     */
    public static TestEngineExtension createExtension(Class<?> clazz) {
        List<Method> initializeMethods =
                MethodSupport.getMethods(
                        clazz,
                        Predicates.ENGINE_EXTENSION_INITIALIZE_METHOD,
                        HierarchyTraversalMode.TOP_DOWN);

        initializeMethods.sort(Comparator.comparing(Method::getName));

        // TODO sort by @TestEngine.Order

        List<Method> cleanupMethods =
                MethodSupport.getMethods(
                        clazz,
                        Predicates.ENGINE_EXTENSION_CLEANUP_METHOD,
                        HierarchyTraversalMode.BOTTOM_UP);

        cleanupMethods.sort(Comparator.comparing(Method::getName));

        // TODO sort by @TestEngine.Order

        return new TestEngineExtension(clazz, initializeMethods, cleanupMethods);
    }
}
