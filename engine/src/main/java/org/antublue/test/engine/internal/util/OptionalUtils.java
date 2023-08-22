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

import java.util.Optional;
import java.util.function.Consumer;

/** Class to implement OptionalUtils */
public class OptionalUtils {

    /** Constructor */
    private OptionalUtils() {
        // DO NOTHING
    }

    /**
     * Method consume the Optional if a value is present, else run a Runnable
     *
     * @param optional optional
     * @param consumer consumer
     * @param runnable runnable
     */
    public static <T> void ifPresentOrElse(
            Optional<T> optional, Consumer<T> consumer, Runnable runnable) {
        if (optional.isPresent()) {
            consumer.accept(optional.get());
        } else {
            runnable.run();
        }
    }
}
