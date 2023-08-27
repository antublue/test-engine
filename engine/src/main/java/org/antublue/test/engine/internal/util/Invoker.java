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

/** Class to implement an Invocation */
public class Invoker {

    /** Constructor */
    private Invoker() {
        // DO NOTHING
    }

    /**
     * Method to invoke a Runnable
     *
     * @param invocation runnable
     * @return an Optional that contains a Throwable if an exception occurred, or else an empty
     *     Optional
     */
    public static Optional<Throwable> invoke(Invocation invocation) {
        try {
            invocation.run();
            return Optional.empty();
        } catch (Throwable t) {
            return Optional.of(t);
        }
    }

    /** Interface to implement an Invocation */
    public interface Invocation {

        /**
         * Method to invoke the Invocation
         *
         * @throws Throwable Throwable
         */
        void run() throws Throwable;
    }
}
