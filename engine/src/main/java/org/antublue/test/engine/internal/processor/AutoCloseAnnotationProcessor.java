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

package org.antublue.test.engine.internal.processor;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.internal.logger.Logger;
import org.antublue.test.engine.internal.logger.LoggerFactory;
import org.antublue.test.engine.internal.predicate.AnnotationFieldPredicate;
import org.antublue.test.engine.internal.util.ThrowableContext;
import org.junit.platform.commons.support.HierarchyTraversalMode;
import org.junit.platform.commons.support.ReflectionSupport;

/** Class to process @TestEngine.AutoClose.X annotations */
@SuppressWarnings("PMD.AvoidAccessibilityAlteration")
public class AutoCloseAnnotationProcessor {

    private static final AutoCloseAnnotationProcessor INSTANCE = new AutoCloseAnnotationProcessor();

    private static final Logger LOGGER =
            LoggerFactory.getLogger(AutoCloseAnnotationProcessor.class);

    public enum Type {
        AFTER_EACH,
        AFTER_ALL,
        AFTER_CONCLUDE
    }

    /** Constructor */
    private AutoCloseAnnotationProcessor() {
        // DO NOTHING
    }

    /**
     * Method to get the singleton
     *
     * @return the singleton
     */
    public static AutoCloseAnnotationProcessor getInstance() {
        return INSTANCE;
    }

    public void conclude(Object testInstance, Type type, ThrowableContext throwableContext) {
        LOGGER.trace(
                "conclude() class [%s] instance [%s] type [%s]",
                testInstance.getClass(), testInstance, type);

        switch (type) {
            case AFTER_EACH:
                {
                    concludeAfterEach(testInstance, throwableContext);
                    break;
                }
            case AFTER_ALL:
                {
                    concludeAfterAll(testInstance, throwableContext);
                    break;
                }
            case AFTER_CONCLUDE:
                {
                    concludeAfterConclude(testInstance, throwableContext);
                    break;
                }
            default:
                {
                    // DO NOTHING
                }
        }
    }

    private void concludeAfterEach(Object testInstance, ThrowableContext throwableContext) {
        LOGGER.trace(
                "concludeAfterEach() class [%s] instance [%s]",
                testInstance.getClass(), testInstance);

        List<Field> fields =
                ReflectionSupport.findFields(
                        testInstance.getClass(),
                        AnnotationFieldPredicate.of(TestEngine.AutoClose.AfterEach.class),
                        HierarchyTraversalMode.TOP_DOWN);

        for (Field field : fields) {
            close(testInstance, field, throwableContext);
        }
    }

    private void concludeAfterAll(Object testInstance, ThrowableContext throwableContext) {
        LOGGER.trace(
                "concludeAfterAll() class [%s] instance [%s]",
                testInstance.getClass(), testInstance);

        List<Field> fields =
                ReflectionSupport.findFields(
                        testInstance.getClass(),
                        AnnotationFieldPredicate.of(TestEngine.AutoClose.AfterAll.class),
                        HierarchyTraversalMode.TOP_DOWN);

        for (Field field : fields) {
            close(testInstance, field, throwableContext);
        }
    }

    private void concludeAfterConclude(Object testInstance, ThrowableContext throwableContext) {
        LOGGER.trace(
                "concludeAfterConclude() class [%s] instance [%s]",
                testInstance.getClass(), testInstance);

        List<Field> fields =
                ReflectionSupport.findFields(
                        testInstance.getClass(),
                        AnnotationFieldPredicate.of(TestEngine.AutoClose.Conclude.class),
                        HierarchyTraversalMode.TOP_DOWN);

        for (Field field : fields) {
            close(testInstance, field, throwableContext);
        }
    }

    private void close(Object testInstance, Field field, ThrowableContext throwableContext) {
        try {
            String methodName = null;

            TestEngine.AutoClose.AfterEach autoCloseAfterEachAnnotation =
                    field.getAnnotation(TestEngine.AutoClose.AfterEach.class);
            TestEngine.AutoClose.AfterAll autoCloseAfterAllAnnotation =
                    field.getAnnotation(TestEngine.AutoClose.AfterAll.class);
            TestEngine.AutoClose.Conclude autoCloseConcludeAnnotation =
                    field.getAnnotation(TestEngine.AutoClose.Conclude.class);

            if (autoCloseAfterEachAnnotation == null
                    && autoCloseAfterAllAnnotation == null
                    && autoCloseConcludeAnnotation == null) {
                return;
            }

            if (autoCloseAfterEachAnnotation != null) {
                methodName = autoCloseAfterEachAnnotation.method();
            } else if (autoCloseAfterAllAnnotation != null) {
                methodName = autoCloseAfterAllAnnotation.method();
            } else if (autoCloseConcludeAnnotation != null) {
                methodName = autoCloseConcludeAnnotation.method();
            }

            field.setAccessible(true);
            Object o = field.get(testInstance);
            if (o == null) {
                return;
            }

            if (methodName == null || methodName.trim().isEmpty()) {
                if (o instanceof AutoCloseable) {
                    ((AutoCloseable) o).close();
                }
            } else if (!methodName.trim().isEmpty()) {
                Method method = o.getClass().getMethod(methodName.trim(), (Class<?>[]) null);
                method.setAccessible(true);
                method.invoke(o, (Object[]) null);
            }
        } catch (Throwable t) {
            throwableContext.add(testInstance.getClass(), t);
        }
    }
}
