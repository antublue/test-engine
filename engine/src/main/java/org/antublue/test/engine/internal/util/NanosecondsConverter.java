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

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/** Class to implement NanosecondsConverter */
public class NanosecondsConverter {

    /** Nanoseconds converter */
    public static final NanosecondsConverter NANOSECONDS =
            new NanosecondsConverter("nanoseconds", "ns", 1);

    /** Microseconds converter */
    public static final NanosecondsConverter MICROSECONDS =
            new NanosecondsConverter("microseconds", "μs", 1000);

    /** Milliseconds converter */
    public static final NanosecondsConverter MILLISECONDS =
            new NanosecondsConverter("milliseconds", "ms", 1e+6);

    /** Seconds converter */
    public static final NanosecondsConverter SECONDS =
            new NanosecondsConverter("seconds", "s", 1e+9);

    private static final Map<String, NanosecondsConverter> map;

    static {
        map = new HashMap<>();
        map.put("seconds", SECONDS);
        map.put("milliseconds", MILLISECONDS);
        map.put("microseconds", MICROSECONDS);
        map.put("nanoseconds", NANOSECONDS);
    }

    private final String value;
    private final double factor;
    private final String suffix;

    /**
     * Constructor
     *
     * @param value value
     * @param suffix suffix
     * @param factor factor
     */
    private NanosecondsConverter(String value, String suffix, double factor) {
        this.value = value;
        this.factor = factor;
        this.suffix = suffix;
    }

    /**
     * Method to convert nanoseconds
     *
     * @param nanoseconds nanoseconds
     * @return the converter value based on the converter type
     */
    public double convert(long nanoseconds) {
        return ((double) nanoseconds) / factor;
    }

    /**
     * Method to create a string representation
     *
     * @param nanoseconds nanoseconds
     * @return a String representation based on the converter type
     */
    public String toString(long nanoseconds) {
        if (factor == 1f) {
            return nanoseconds + " " + suffix;
        }

        return String.format("%.3f %s", ((double) nanoseconds) / factor, suffix);
    }

    /**
     * Method to decode a string to a converter type
     *
     * @param string string
     * @returns a converter based on the string type or Nanoseconds.MILLISECONDS
     */
    public static NanosecondsConverter decode(String string) {
        if (string == null || string.trim().isEmpty()) {
            return MILLISECONDS;
        }

        return map.getOrDefault(string.trim().toLowerCase(Locale.ENGLISH), MILLISECONDS);
    }

    @Override
    public String toString() {
        return value;
    }
}
