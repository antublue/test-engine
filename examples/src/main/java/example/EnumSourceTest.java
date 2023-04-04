package example;

import org.antublue.test.engine.api.Argument;
import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.api.source.EnumSource;

import java.io.IOException;
import java.util.stream.Stream;

/**
 * Example test
 */
public class EnumSourceTest {

    private Argument argument;

    @TestEngine.Arguments
    public static Stream<Argument> arguments() throws IOException {
        return EnumSource.of(DaysOfTheWeek.class);
    }

    @TestEngine.Argument
    public void argument(Argument argument) {
        this.argument = argument;
    }

    @TestEngine.BeforeAll
    public void beforeAll() {
        System.out.println("beforeAll()");
    }

    @TestEngine.Test
    public void test1() {
        System.out.println("test1(" + argument.value() + ")");
    }

    @TestEngine.Test
    public void test2() {
        System.out.println("test2(" + argument.value() + ")");
    }

    @TestEngine.AfterAll
    public void afterAll() {
        System.out.println("afterAll()");
    }

    private enum DaysOfTheWeek {

        SUNDAY("sunday"),
        MONDAY("monday"),
        TUESDAY("tuesday"),
        WEDNESDAY("wednesday"),
        THURSDAY("thursday"),
        FRIDAY("friday"),
        SATURDAY("saturday");

        private String value;

        DaysOfTheWeek(String value) {
            this.value = value;
        }

        public String toString() {
            return value;
        }
    }
}
