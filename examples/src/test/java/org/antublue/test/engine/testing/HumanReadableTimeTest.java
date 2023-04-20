package org.antublue.test.engine.testing;

import org.antublue.test.engine.api.Map;
import org.antublue.test.engine.api.Parameter;
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
                Parameter.of(
                        "11969L",
                        new Map().put("milliseconds", 11969L).put("humanReadableTime", "11 seconds, 969 ms")),
                Parameter.of(
                        "1000L",
                        new Map().put("milliseconds", 1000L).put("humanReadableTime", "1 second, 0 ms")),
                Parameter.of(
                        "1001L",
                        new Map().put("milliseconds", 1001L).put("humanReadableTime", "1 second, 1 ms")),
                Parameter.of(
                        "60001L",
                        new Map().put("milliseconds", 60001L).put("humanReadableTime", "1 minute, 0 seconds, 1 ms")),
                Parameter.of(
                        "61001L",
                        new Map().put("milliseconds", 61001L).put("humanReadableTime", "1 minute, 1 second, 1 ms"))
                );
    }

    @TestEngine.Parameter
    public void parameter(Parameter parameter) {
        Map map = parameter.value();
        milliseconds = map.get("milliseconds");
        expectedHumanReadableTime = map.get("humanReadableTime");
    }

    @TestEngine.Test
    public void testHumanReadableTime() {
        System.out.println(String.format("testHumanReadableTime() milliseconds [%d] string [%s]", milliseconds, expectedHumanReadableTime));
        String humanReadableTime = HumanReadableTime.toHumanReadable(milliseconds);
        assertThat(humanReadableTime).isEqualTo(expectedHumanReadableTime);
    }
}
