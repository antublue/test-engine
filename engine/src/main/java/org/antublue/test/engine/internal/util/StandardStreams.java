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

import static java.lang.String.format;

/** Class to implement StandardStreams */
public class StandardStreams {

    /** Constructor */
    private StandardStreams() {
        // DO NOTHING
    }

    /**
     * Method to print a line to System.out and flush streams
     *
     * @param format format
     * @param objects objects
     */
    public static void print(String format, Object... objects) {
        System.out.print(format(format, objects));
        flush();
    }

    /**
     * Method to print a line with newline to System.out and flush streams
     *
     * @param format format
     * @param objects objects
     */
    public static void println(String format, Object... objects) {
        System.out.println(format(format, objects));
        flush();
    }

    /** Method to flush streams */
    public static void flush() {
        System.out.flush();
        System.err.flush();
    }
}
