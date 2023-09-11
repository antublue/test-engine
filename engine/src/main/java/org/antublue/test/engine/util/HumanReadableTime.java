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

package org.antublue.test.engine.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/** Class to create a human-readable time from a duration */
// Suppress PMD.UselessParentheses - PMD has bug around UselessParentheses calculating milliseconds
@SuppressWarnings({"PMD.NPathComplexity", "PMD.UselessParentheses"})
public final class HumanReadableTime {

    /** Constructor */
    private HumanReadableTime() {
        // DO NOTHING
    }

    /**
     * Method to convert a duration into a human-readable time
     *
     * @param duration duration
     * @return the return value
     */
    public static String toHumanReadable(long duration) {
        return toHumanReadable(duration, false);
    }

    /**
     * Method to convert a duration into a human-readable time
     *
     * @param nanoseconds nanoseconds
     * @param useShortFormat useShortFormat
     * @return the return value
     */
    public static String toHumanReadable(long nanoseconds, boolean useShortFormat) {
        long nanosecondsPositive = nanoseconds > 0 ? nanoseconds : -nanoseconds;
        long millisecondsDuration =
                (long) NanosecondsConverter.MILLISECONDS.convert(nanosecondsPositive);
        long hours = TimeUnit.MILLISECONDS.toHours(millisecondsDuration);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millisecondsDuration) - (hours * 60);
        long seconds =
                TimeUnit.MILLISECONDS.toSeconds(millisecondsDuration)
                        - ((hours * 60 * 60) + (minutes * 60));
        long milliseconds =
                millisecondsDuration
                        - ((hours * 60 * 60 * 1000) + (minutes * 60 * 1000) + (seconds * 1000));

        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(hours);

        if (useShortFormat) {
            stringBuilder.append(" h");
        } else {
            stringBuilder.append(" hour");

            if (hours != 1) {
                stringBuilder.append("s");
            }
        }

        stringBuilder.append(", ");
        stringBuilder.append(minutes);

        if (useShortFormat) {
            stringBuilder.append(" m");
        } else {
            stringBuilder.append(" minute");
            if (minutes != 1) {
                stringBuilder.append("s");
            }
        }

        stringBuilder.append(", ");
        stringBuilder.append(seconds);

        if (useShortFormat) {
            stringBuilder.append(" s");
        } else {
            stringBuilder.append(" second");
            if (seconds != 1) {
                stringBuilder.append("s");
            }
        }

        stringBuilder.append(", ");
        stringBuilder.append(milliseconds);
        stringBuilder.append(" ms");

        String result = stringBuilder.toString();

        if (result.startsWith("0 h, ")) {
            result = result.substring("0 h, ".length());
        }

        if (result.startsWith("0 hours, ")) {
            result = result.substring("0 hours, ".length());
        }

        if (result.startsWith("0 m, ")) {
            result = result.substring("0 m, ".length());
        }

        if (result.startsWith("0 minutes, ")) {
            result = result.substring("0 minutes, ".length());
        }

        if (result.startsWith("0 s, ")) {
            result = result.substring("0 s, ".length());
        }

        if (result.startsWith("0 seconds, ")) {
            result = result.substring("0 seconds, ".length());
        }

        return result;
    }

    /**
     * Method to get the current time as a String
     *
     * @return the return value
     */
    public static String now() {
        SimpleDateFormat simpleDateFormat =
                new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.ENGLISH);

        return simpleDateFormat.format(new Date());
    }
}
