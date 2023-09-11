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
import org.antublue.test.engine.api.Argument;
import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.logger.Logger;
import org.antublue.test.engine.logger.LoggerFactory;

/** Class to implement FieldInjector */
public class ArgumentFieldInjector {

    private static final Logger LOGGER = LoggerFactory.getLogger(ArgumentFieldInjector.class);

    private static final ArgumentFieldInjector SINGLETON = new ArgumentFieldInjector();

    /** Constructor */
    private ArgumentFieldInjector() {
        // DO NOTHING
    }

    /**
     * Method to get the singleton
     *
     * @return the singleton
     */
    public static ArgumentFieldInjector singleton() {
        return SINGLETON;
    }

    /**
     * Method to inject an Argument into a Field
     *
     * @param object object
     * @param argument argument
     * @param field field
     * @throws Throwable Throwable
     */
    public void inject(Object object, Argument argument, Field field) throws Throwable {
        TestEngine.Argument annotation = field.getAnnotation(TestEngine.Argument.class);
        if (annotation != null) {
            String name = null;
            if (argument != null) {
                name = argument.name();
            }

            LOGGER.trace(
                    "injecting argument class [%s] argument [%s] field [%s] field type [%s]",
                    object.getClass().getName(), name, field.getName(), field.getType().getName());

            field.set(object, argument);
        }
    }
}
