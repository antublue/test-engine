/*
 * Copyright (C) 2022-2023 The AntuBLUE test-engine project authors
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

/**
 * Class to build AnsiColor strings using a fluent pattern
 */
public class AnsiColorString {

    private final StringBuilder stringBuilder;

    /**
     * Constructor
     */
    public AnsiColorString() {
        stringBuilder = new StringBuilder();
    }

    /**
     * Reset the content
     *
     * @return the return value
     */
    public AnsiColorString reset() {
        stringBuilder.setLength(0);
        return this;
    }

    /**
     * Method to set the current color
     *
     * @param ansiColor ansiColor
     * @return the return value
     */
    public AnsiColorString color(AnsiColor ansiColor) {
        stringBuilder.append(ansiColor);
        return this;
    }

    /**
     * Method to append a string
     *
     * @param string string
     * @return the return value
     */
    public AnsiColorString append(String string) {
        stringBuilder.append(string);
        return this;
    }

    @Override
    public String toString() {
        String string = stringBuilder.append(AnsiColor.RESET).toString();
        reset();
        return string;
    }
}
