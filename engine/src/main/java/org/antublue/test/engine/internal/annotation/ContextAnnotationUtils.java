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

package org.antublue.test.engine.internal.annotation;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import org.antublue.test.engine.api.Context;
import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.internal.ContextImpl;
import org.antublue.test.engine.internal.logger.Logger;
import org.antublue.test.engine.internal.logger.LoggerFactory;
import org.antublue.test.engine.internal.predicate.AnnotationFieldPredicate;
import org.junit.platform.commons.support.HierarchyTraversalMode;
import org.junit.platform.commons.support.ReflectionSupport;

/** Class to process @TestEngine.Context annotations */
@SuppressWarnings("PMD.AvoidAccessibilityAlteration")
public class ContextAnnotationUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(ContextAnnotationUtils.class);

    private static final Context CONTEXT = ContextImpl.getInstance();

    /** Constructor */
    private ContextAnnotationUtils() {
        // DO NOTHING
    }

    /**
     * Method to inject @TestEngine.Context annotated fields
     *
     * @param testClass testClass
     * @throw Throwable
     */
    public static void injectContextFields(Class<?> testClass) throws Throwable {
        LOGGER.trace("injectContextFields() testClass [%s]", testClass);

        List<Field> fields =
                ReflectionSupport.findFields(
                        testClass,
                        AnnotationFieldPredicate.of(TestEngine.Context.class),
                        HierarchyTraversalMode.TOP_DOWN);

        for (Field field : fields) {
            if (Modifier.isStatic(field.getModifiers())) {
                LOGGER.trace("injectContextFields() testClass [%s] field [%s]", testClass, field);

                field.setAccessible(true);
                field.set(null, CONTEXT);
            }
        }
    }

    public static void clearContextFields(Class<?> testClass) throws Throwable {
        LOGGER.trace("clearContextFields() testClass [%s]", testClass);

        List<Field> fields =
                ReflectionSupport.findFields(
                        testClass,
                        AnnotationFieldPredicate.of(TestEngine.Context.class),
                        HierarchyTraversalMode.TOP_DOWN);

        for (Field field : fields) {
            if (Modifier.isStatic(field.getModifiers())) {
                LOGGER.trace("injectContextFields() testClass [%s] field [%s]", testClass, field);

                field.setAccessible(true);
                field.set(null, null);
            }
        }
    }

    /**
     * Method to inject @TestEngine.Context annotated fields
     *
     * @param testInstance testInstance
     * @throw Throwable
     */
    public static void injectContextFields(Object testInstance) throws Throwable {
        LOGGER.trace(
                "injectContextFields() testClass [%s] testInstance [%s]",
                testInstance.getClass(), testInstance);

        List<Field> fields =
                ReflectionSupport.findFields(
                        testInstance.getClass(),
                        AnnotationFieldPredicate.of(TestEngine.Context.class),
                        HierarchyTraversalMode.TOP_DOWN);

        for (Field field : fields) {
            field.setAccessible(true);
            if (!Modifier.isStatic(field.getModifiers())) {
                LOGGER.trace(
                        "injectContextFields() testClass [%s] testInstance [%s] field [%s]",
                        testInstance.getClass().getName(), testInstance, field);

                field.set(testInstance, CONTEXT);
            }
        }
    }

    public static void clearContextFields(Object testInstance) throws Throwable {
        LOGGER.trace(
                "clearContextFields() testClass [%s] testInstance [%s]",
                testInstance.getClass(), testInstance);

        List<Field> fields =
                ReflectionSupport.findFields(
                        testInstance.getClass(),
                        AnnotationFieldPredicate.of(TestEngine.Context.class),
                        HierarchyTraversalMode.TOP_DOWN);

        for (Field field : fields) {
            if (!Modifier.isStatic(field.getModifiers())) {
                LOGGER.trace(
                        "clearContextFields() testClass [%s] testInstance [%s] field [%s]",
                        testInstance.getClass(), testInstance, field);

                field.setAccessible(true);
                field.set(null, null);
            }
        }
    }
}
