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

package org.antublue.test.engine.internal.util;

import java.lang.reflect.Field;
import java.util.function.Consumer;

public class FieldUtils {

    /**
     * Constructor
     */
    private FieldUtils() {
        // DO NOTHING
    }

    /**
     * Method to set a Field
     *
     * @param object object
     * @param field field
     * @param value value
     * @param throwableConsumer throwableConsumer
     */
    public static void setField(Object object, Field field, Object value, Consumer<Throwable> throwableConsumer) {
        Precondition.notNull(object, "object is null");
        Precondition.notNull(field, "field is null");
        Precondition.notNull(throwableConsumer, "throwableConsumer is null");

        try {
            field.setAccessible(true);
            field.set(object, value);
        } catch (Throwable t) {
            t.printStackTrace();
            throwableConsumer.accept(t);
        }
    }
}
