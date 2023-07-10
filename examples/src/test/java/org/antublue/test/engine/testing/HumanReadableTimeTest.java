package org.antublue.test.engine.testing;

import org.antublue.test.engine.api.Argument;
import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.internal.util.HumanReadableTime;
import org.junit.jupiter.api.Assertions;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test for HumanReadableTime
 */
public class HumanReadableTimeTest {

    @TestEngine.Argument
    public HumanReadableTimeTestArgument humanReadableTimeTestArgument;

    @TestEngine.ArgumentSupplier
    public static Stream<HumanReadableTimeTestArgument> arguments() {
        return Stream.of(
                new HumanReadableTimeTestArgument(
                        "11969L", 11969L, "11 seconds, 969 ms"),
                new HumanReadableTimeTestArgument(
                        "1000L", 1000L, "1 second, 0 ms"),
                new HumanReadableTimeTestArgument(
                    "1001L", 1001L, "1 second, 1 ms"),
                new HumanReadableTimeTestArgument(
                        "60001L", 60001L, "1 minute, 0 seconds, 1 ms"),
                new HumanReadableTimeTestArgument(
                        "61001L", 61001L, "1 minute, 1 second, 1 ms"),
                new HumanReadableTimeTestArgument(
                        "3661001L", 3661001L, "1 hour, 1 minute, 1 second, 1 ms"),
                new HumanReadableTimeTestArgument(
                        "3721001L", 3721001L, "1 hour, 2 minutes, 1 second, 1 ms"));
    }

    @TestEngine.Test
    public void testHumanReadableTime() {
        long milliseconds = humanReadableTimeTestArgument.milliseconds();
        String expectedHumanReadableTime = humanReadableTimeTestArgument.humanReadableTime();
        String actualHumanReadableTime = HumanReadableTime.toHumanReadable(milliseconds);

        if (!actualHumanReadableTime.equals(expectedHumanReadableTime)) {
            Assertions.fail(
                    String.format(
                            "testHumanReadableTime() milliseconds [%d] expected [%s] actual [%s]",
                            milliseconds,
                            expectedHumanReadableTime,
                            actualHumanReadableTime));
        }
    }

    public static class HumanReadableTimeTestArgument implements Argument {

        private final String name;
        private final long milliseconds;
        private final String humanReadableTime;

        public HumanReadableTimeTestArgument(String name, long milliseconds, String humanReadableTime) {
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
