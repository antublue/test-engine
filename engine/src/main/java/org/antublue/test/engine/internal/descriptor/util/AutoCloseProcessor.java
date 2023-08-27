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

package org.antublue.test.engine.internal.descriptor.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.internal.logger.Logger;
import org.antublue.test.engine.internal.logger.LoggerFactory;

/** Class to implement FieldInjector */
@SuppressWarnings("PMD.NPathComplexity")
public class AutoCloseProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(AutoCloseProcessor.class);

    private static final AutoCloseProcessor SINGLETON = new AutoCloseProcessor();

    /** Constructor */
    private AutoCloseProcessor() {
        // DO NOTHING
    }

    /**
     * Method to get the singleton
     *
     * @return the singleton
     */
    public static AutoCloseProcessor singleton() {
        return SINGLETON;
    }

    /**
     * Method to inject an Argument into a Field
     *
     * @param object object
     * @param field field
     * @throws Throwable Throwable
     */
    public void close(Object object, Field field) throws Throwable {
        LOGGER.trace("close class [%s] field [%s]", object.getClass().getName(), field.getName());

        TestEngine.AutoClose annotation = field.getAnnotation(TestEngine.AutoClose.class);

        if (annotation == null) {
            return;
        }

        Object o = field.get(object);
        if (o == null) {
            return;
        }

        String methodName = annotation.method();

        if (methodName == null || methodName.trim().isEmpty()) {
            if (o instanceof AutoCloseable) {
                ((AutoCloseable) o).close();
            }
        } else if (!methodName.trim().isEmpty()) {
            Method method = o.getClass().getMethod(methodName.trim(), (Class<?>[]) null);
            method.setAccessible(true);
            method.invoke(o, (Object[]) null);
        }
    }
}
