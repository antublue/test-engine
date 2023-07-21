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

import java.lang.reflect.Method;
import java.util.function.Consumer;

/**
 * Class to implement Method utilities
 */
public class MethodUtils {

    /**
     * Constructor
     */
    private MethodUtils() {
        // DO NOTHING
    }

    /**
     * Method to invoke a method
     *
     * @param object object
     * @param method method
     * @param throwableConsumer throwableConsumer
     */
    public static void invoke(
            Object object,
            Method method,
            Consumer<Throwable> throwableConsumer) {
        Precondition.notNull(object, "object is null");
        Precondition.notNull(method, "method is null");
        Precondition.notNull(throwableConsumer, "throwableConsumer is null");

        invoke(object, method, null, throwableConsumer);
    }

    /**
     * Method to invoke a method
     *
     * @param object object
     * @param method method
     * @param arguments arguments
     * @param throwableConsumer throwableConsumer
     */
    public static void invoke(
            Object object,
            Method method,
            Object[] arguments,
            Consumer<Throwable> throwableConsumer) {
        Precondition.notNull(object, "object is null");
        Precondition.notNull(method, "method is null");
        Precondition.notNull(throwableConsumer, "throwableConsumer is null");

        try {
            method.invoke(object, arguments);
        } catch (Throwable t) {
            throwableConsumer.accept(t.getCause());
        }
    }
}
