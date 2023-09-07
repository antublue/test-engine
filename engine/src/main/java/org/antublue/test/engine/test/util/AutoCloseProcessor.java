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

package org.antublue.test.engine.test.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.logger.Logger;
import org.antublue.test.engine.logger.LoggerFactory;
import org.antublue.test.engine.test.ThrowableContext;

/** Class to implement FieldInjector */
@SuppressWarnings("PMD.NPathComplexity")
public class AutoCloseProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(AutoCloseProcessor.class);

    /** Constructor */
    public AutoCloseProcessor() {
        // DO NOTHING
    }

    /**
     * Method to inject an Argument into a Field
     *
     * @param object object
     * @param field field
     * @param throwableContext throwableContext
     */
    public void close(Object object, Field field, ThrowableContext throwableContext) {
        LOGGER.trace("close class [%s] field [%s]", object.getClass().getName(), field.getName());

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

            Object o = field.get(object);
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
            throwableContext.add(object.getClass(), t);
        }
    }
}
