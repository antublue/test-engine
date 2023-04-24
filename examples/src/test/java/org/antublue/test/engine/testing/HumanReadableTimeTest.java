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

    private long milliseconds;
    private String expectedHumanReadableTime;

    @TestEngine.ParameterSupplier
    public static Stream<Parameter> parameters() {
        return Stream.of(
                ParameterMap
                        .named("11969L")
                        .put("milliseconds", 11969L)
                        .put("humanReadableTime", "11 seconds, 969 ms")
                        .parameter(),
                ParameterMap
                        .named("1000L")
                        .put("milliseconds", 1000L)
                        .put("humanReadableTime", "1 second, 0 ms")
                         .parameter(),
                ParameterMap
                        .named("1001L")
                        .put("milliseconds", 1001L)
                        .put("humanReadableTime", "1 second, 1 ms")
                        .parameter(),
                ParameterMap
                        .named("60001L")
                        .put("milliseconds", 60001L)
                        .put("humanReadableTime", "1 minute, 0 seconds, 1 ms")
                        .parameter(),
                ParameterMap
                        .named("61001L")
                        .put("milliseconds", 61001L)
                        .put("humanReadableTime", "1 minute, 1 second, 1 ms")
                        .parameter());
    }

    @TestEngine.Parameter
    public void parameter(Parameter parameter) {
        milliseconds = parameter.value(ParameterMap.class).get("milliseconds");
        expectedHumanReadableTime = parameter.value(ParameterMap.class).get("humanReadableTime");
    }

    @TestEngine.Test
    public void testHumanReadableTime() {
        System.out.println(String.format("testHumanReadableTime() milliseconds [%d] string [%s]", milliseconds, expectedHumanReadableTime));
        String humanReadableTime = HumanReadableTime.toHumanReadable(milliseconds);
        assertThat(humanReadableTime).isEqualTo(expectedHumanReadableTime);
    }
}
