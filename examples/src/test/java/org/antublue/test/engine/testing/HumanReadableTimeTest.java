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

package org.antublue.test.engine.testing;

import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.stream.Stream;
import org.antublue.test.engine.api.Argument;
import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.internal.util.HumanReadableTimeUtils;

/** Test for HumanReadableTime */
public class HumanReadableTimeTest {

    @TestEngine.ArgumentSupplier
    public static Stream<HumanReadableTimeTestArgument> arguments() {
        return Stream.of(
                new HumanReadableTimeTestArgument("11969L", 11969L, "11 seconds, 969 ms"),
                new HumanReadableTimeTestArgument("1000L", 1000L, "1 second, 0 ms"),
                new HumanReadableTimeTestArgument("1001L", 1001L, "1 second, 1 ms"),
                new HumanReadableTimeTestArgument("60001L", 60001L, "1 minute, 0 seconds, 1 ms"),
                new HumanReadableTimeTestArgument("61001L", 61001L, "1 minute, 1 second, 1 ms"),
                new HumanReadableTimeTestArgument(
                        "3661001L", 3661001L, "1 hour, 1 minute, 1 second, 1 ms"),
                new HumanReadableTimeTestArgument(
                        "3721001L", 3721001L, "1 hour, 2 minutes, 1 second, 1 ms"));
    }

    @TestEngine.Test
    public void testHumanReadableTime(HumanReadableTimeTestArgument humanReadableTimeTestArgument) {
        long milliseconds = humanReadableTimeTestArgument.milliseconds();
        String expectedHumanReadableTime = humanReadableTimeTestArgument.humanReadableTime();
        String actualHumanReadableTime =
                HumanReadableTimeUtils.toHumanReadable((long) (milliseconds * 1e+6));

        if (!actualHumanReadableTime.equals(expectedHumanReadableTime)) {
            fail(
                    format(
                            "testHumanReadableTime() milliseconds [%d] expected [%s] actual [%s]",
                            milliseconds, expectedHumanReadableTime, actualHumanReadableTime));
        }
    }

    public static class HumanReadableTimeTestArgument implements Argument {

        private final String name;
        private final long milliseconds;
        private final String humanReadableTime;

        public HumanReadableTimeTestArgument(
                String name, long milliseconds, String humanReadableTime) {
            this.name = name;
            this.milliseconds = milliseconds;
            this.humanReadableTime = humanReadableTime;
        }

        @Override
        public String name() {
            return name;
        }

        public long milliseconds() {
            return milliseconds;
        }

        public String humanReadableTime() {
            return humanReadableTime;
        }
    }
}
