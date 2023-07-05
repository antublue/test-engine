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

package org.antublue.test.engine.api;

/**
 * Class to fail a test
 */
public class Fail {

    /**
     * Constructor
     */
    private Fail() {
        // DO NOTHING
    }

    /**
     * Method to fail a test by generating a FailError
     *
     * @param message
     */
    public static void fail(String message) {
        throw new FailError(message);
    }

    /**
     * Class to implement a FailError
     */
    private static class FailError extends AssertionError {

        /**
         * Constructor
         *
         * @param message
         */
        public FailError(String message) {
            super(message);
        }
    }
}
