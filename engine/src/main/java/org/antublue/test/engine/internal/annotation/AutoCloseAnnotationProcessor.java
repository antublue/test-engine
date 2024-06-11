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
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.internal.logger.Logger;
import org.antublue.test.engine.internal.logger.LoggerFactory;
import org.antublue.test.engine.internal.util.ThrowableContext;
import org.junit.platform.commons.support.HierarchyTraversalMode;
import org.junit.platform.commons.support.ReflectionSupport;

/** Class to process @TestEngine.AutoClose.X annotations */
@SuppressWarnings("PMD.AvoidAccessibilityAlteration")
public class AutoCloseAnnotationProcessor {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(AutoCloseAnnotationProcessor.class);

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
        return SingletonHolder.INSTANCE;
    }

    /**
     * Method to process @TestEngine.AutoClose annotation on static fields
     *
     * @param testClass testClass
     * @param throwableContext throwableContext
     */
    public void closeAutoCloseableFields(Class<?> testClass, ThrowableContext throwableContext) {
        List<Field> fields =
                ReflectionSupport.findFields(
                        testClass,
                        field ->
                                Modifier.isStatic(field.getModifiers())
                                        && field.getAnnotation(TestEngine.AutoClose.class) != null,
                        HierarchyTraversalMode.BOTTOM_UP);

        for (Field field : fields) {
            try {
                field.setAccessible(true);
                Object value = field.get(null);
                if (value instanceof AutoCloseable) {
                    ((AutoCloseable) value).close();
                } else if (value != null) {
                    Method method = getMethod(field, value);
                    method.invoke(null, (Object[]) null);
                }
            } catch (Throwable t) {
                throwableContext.add(testClass, t);
            }
        }
    }

    /**
     * Method to process @TestEngine.AutoClose annotation on fields
     *
     * @param testInstance testInstance
     * @param throwableContext throwableContext
     */
    public void closeAutoCloseableFields(Object testInstance, ThrowableContext throwableContext) {
        List<Field> fields =
                ReflectionSupport.findFields(
                        testInstance.getClass(),
                        field ->
                                !Modifier.isStatic(field.getModifiers())
                                        && field.getAnnotation(TestEngine.AutoClose.class) != null,
                        HierarchyTraversalMode.BOTTOM_UP);

        for (Field field : fields) {
            try {
                field.setAccessible(true);
                Object value = field.get(testInstance);
                if (value instanceof AutoCloseable) {
                    ((AutoCloseable) value).close();
                } else if (value != null) {
                    Method method = getMethod(field, value);
                    method.invoke(testInstance, (Object[]) null);
                }
            } catch (Throwable t) {
                throwableContext.add(testInstance.getClass(), t);
            }
        }
    }

    private static Method getMethod(Field field, Object value) throws NoSuchMethodException {
        TestEngine.AutoClose autoclose = field.getAnnotation(TestEngine.AutoClose.class);
        String methodName = autoclose.method();
        Method method;
        if (methodName == null || methodName.trim().isEmpty()) {
            method = value.getClass().getDeclaredMethod("close", (Class<?>[]) null);
        } else {
            method = value.getClass().getDeclaredMethod(methodName, (Class<?>[]) null);
        }
        return method;
    }

    /*
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
    */

    /** Class to hold the singleton instance */
    private static final class SingletonHolder {

        /** The singleton instance */
        private static final AutoCloseAnnotationProcessor INSTANCE =
                new AutoCloseAnnotationProcessor();
    }
}
