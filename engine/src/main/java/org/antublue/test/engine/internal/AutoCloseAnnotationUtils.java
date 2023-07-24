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

package org.antublue.test.engine.internal;

import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.internal.logger.Logger;
import org.antublue.test.engine.internal.logger.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Consumer;

/**
 * Class to process @TestEngine.AutoClose annotations
 */
@SuppressWarnings({ "PMD.AvoidAccessibilityAlteration", "PMD.EmptyCatchBlock" })
public class AutoCloseAnnotationUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(AutoCloseAnnotationUtils.class);

    /**
     * Constructor
     */
    private AutoCloseAnnotationUtils() {
        // DO NOTHING
    }

    /**
     * Method to process @TestEngine.AutoClose annotated fields for a specific scope
     *
     * @param object object
     * @param lifecycle lifecycle
     * @param throwableConsumer throwableConsumer
     */
    public static void processAutoCloseAnnotatedFields(
            Object object, String lifecycle, Consumer<Throwable> throwableConsumer) {
        LOGGER.trace("processAutoCloseFields(%s, %s)", object.getClass().getName(), lifecycle);

        ReflectionUtils
                .getAutoCloseFields(object.getClass())
                .forEach(
                        field -> {
                            LOGGER.trace("closing field [%s]", field.getName());
                            TestEngine.AutoClose annotation = field.getAnnotation(TestEngine.AutoClose.class);
                            String annotationLifecycle = annotation.lifecycle();
                            String annotationMethodName = annotation.method();
                            if (lifecycle.equals(annotationLifecycle)) {
                                close(object, annotationLifecycle, annotationMethodName, field, throwableConsumer);
                            } else {
                                LOGGER.trace(
                                        "skipping field [%s] annotation scope [%s] doesn't match scope [%s]",
                                        field.getName(),
                                        annotationLifecycle,
                                        lifecycle);
                            }
                        });
    }

    /**
     * Method to close an @TestEngine.AutoClose annotated field
     *
     * @param object object
     * @param lifecycle lifecycle
     * @param methodName methodName
     * @param field field
     * @param throwableConsumer throwableConsumer
     */
    private static void close(
            Object object, String lifecycle, String methodName, Field field, Consumer<Throwable> throwableConsumer) {
        LOGGER.trace("close(%s, %s, %s)", object.getClass().getName(), methodName, field.getName());

        if (methodName == null || methodName.trim().isEmpty()) {
            try {
                Object o = field.get(object);
                if (o instanceof AutoCloseable) {
                    ((AutoCloseable) o).close();
                }
            } catch (Throwable t) {
                throwableConsumer.accept(
                        new TestEngineException(
                            String.format(
                                    "Exception closing @TestEngine.AutoClose class [%s] field [%s] scope [%s]",
                                    object.getClass(),
                                    field.getName(),
                                    lifecycle),
                            t));
            }
        } else {
            Throwable throwable = null;

            try {
                Object o = field.get(object);
                if (o != null) {
                    Method method = o.getClass().getMethod(methodName.trim(), (Class<?>[]) null);
                    method.setAccessible(true);
                    method.invoke(o, (Object[]) null);
                }
            } catch (InvocationTargetException e) {
                throwable = e.getCause();
            } catch (Throwable t) {
                throwable = t;
            }

            if (throwable != null) {
                throwableConsumer.accept(
                        new TestEngineException(
                                String.format(
                                        "Exception closing @TestEngine.AutoClose class [%s] field [%s] method [%s] scope [%s]",
                                        object.getClass(),
                                        field.getName(),
                                        methodName.trim(),
                                        lifecycle),
                                throwable));
            }
        }
    }
}
