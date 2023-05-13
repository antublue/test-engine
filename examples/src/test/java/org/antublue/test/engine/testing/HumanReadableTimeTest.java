package org.antublue.test.engine.testing;

import org.antublue.test.engine.api.MapParameter;
import org.antublue.test.engine.api.Parameter;
import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.internal.util.HumanReadableTime;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test for HumanReadableTime
 */
public class HumanReadableTimeTest {

    private static final String MILLISECONDS = "milliseconds";
    private static final String HUMAN_READABLE_TIME = "humanReadableTime";

    @TestEngine.Parameter
    public MapParameter mapParameter;

    @TestEngine.ParameterSupplier
    public static Stream<Parameter> parameters() {
        return Stream.of(
                MapParameter.named("11969L")
                        .put(MILLISECONDS, 11969L)
                        .put(HUMAN_READABLE_TIME, "11 seconds, 969 ms"),
                MapParameter.named("1000L")
                        .put(MILLISECONDS, 1000L)
                        .put(HUMAN_READABLE_TIME, "1 second, 0 ms"),
                MapParameter.named("1001L")
                        .put(MILLISECONDS, 1001L)
                        .put(HUMAN_READABLE_TIME, "1 second, 1 ms"),
                MapParameter.named("60001L")
                        .put(MILLISECONDS, 60001L)
                        .put(HUMAN_READABLE_TIME, "1 minute, 0 seconds, 1 ms"),
                MapParameter.named("61001L")
                        .put(MILLISECONDS, 61001L)
                        .put(HUMAN_READABLE_TIME, "1 minute, 1 second, 1 ms"),
                MapParameter.named("3661001L")
                        .put(MILLISECONDS, 3661001L)
                        .put(HUMAN_READABLE_TIME, "1 hour, 1 minute, 1 second, 1 ms"),
                MapParameter.named("3721001L")
                        .put(MILLISECONDS, 3721001L)
                        .put(HUMAN_READABLE_TIME, "1 hour, 2 minutes, 1 second, 1 ms"));
    }

    @TestEngine.Test
    public void testHumanReadableTime() {
        long milliseconds = mapParameter.get(MILLISECONDS);
        String expectedHumanReadableTime = mapParameter.get(HUMAN_READABLE_TIME);

        String actualHumanReadableTime = HumanReadableTime.toHumanReadable(milliseconds);

        System.out.println(
                String.format(
                        "testHumanReadableTime() milliseconds [%d] expected [%s] actual [%s]",
                        milliseconds,
                        expectedHumanReadableTime,
                        actualHumanReadableTime));

        assertThat(actualHumanReadableTime).isEqualTo(expectedHumanReadableTime);
    }
}
