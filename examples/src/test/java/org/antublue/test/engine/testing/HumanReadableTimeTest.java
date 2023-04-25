package org.antublue.test.engine.testing;

import org.antublue.test.engine.api.Parameter;
import org.antublue.test.engine.api.ParameterMap;
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
    
    private long milliseconds;
    private String expectedHumanReadableTime;

    @TestEngine.ParameterSupplier
    public static Stream<Parameter> parameters() {
        return Stream.of(
                ParameterMap
                        .named("11969L")
                        .put(MILLISECONDS, 11969L)
                        .put(HUMAN_READABLE_TIME, "11 seconds, 969 ms")
                        .parameter(),
                ParameterMap
                        .named("1000L")
                        .put(MILLISECONDS, 1000L)
                        .put(HUMAN_READABLE_TIME, "1 second, 0 ms")
                         .parameter(),
                ParameterMap
                        .named("1001L")
                        .put(MILLISECONDS, 1001L)
                        .put(HUMAN_READABLE_TIME, "1 second, 1 ms")
                        .parameter(),
                ParameterMap
                        .named("60001L")
                        .put(MILLISECONDS, 60001L)
                        .put(HUMAN_READABLE_TIME, "1 minute, 0 seconds, 1 ms")
                        .parameter(),
                ParameterMap
                        .named("61001L")
                        .put(MILLISECONDS, 61001L)
                        .put(HUMAN_READABLE_TIME, "1 minute, 1 second, 1 ms")
                        .parameter(),
                ParameterMap
                        .named("3661001L")
                        .put(MILLISECONDS, 3661001L)
                        .put(HUMAN_READABLE_TIME, "1 hour, 1 minute, 1 second, 1 ms")
                        .parameter(),
                ParameterMap
                        .named("3721001L")
                        .put(MILLISECONDS, 3721001L)
                        .put(HUMAN_READABLE_TIME, "1 hour, 2 minutes, 1 second, 1 ms")
                        .parameter());
    }

    @TestEngine.Parameter
    public void parameter(Parameter parameter) {
        milliseconds = parameter.value(ParameterMap.class).get(MILLISECONDS);
        expectedHumanReadableTime = parameter.value(ParameterMap.class).get(HUMAN_READABLE_TIME);
    }

    @TestEngine.Test
    public void testHumanReadableTime() {
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
