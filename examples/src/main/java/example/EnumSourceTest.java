package example;

import org.antublue.test.engine.api.Parameter;
import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.api.source.EnumSource;

import java.io.IOException;
import java.util.stream.Stream;

/**
 * Example test
 */
public class EnumSourceTest {

    private Parameter parameter;

    @TestEngine.ParameterSupplier
    public static Stream<Parameter> parameters() throws IOException {
        return EnumSource.of(DaysOfTheWeek.class);
    }

    @TestEngine.ParameterSetter
    public void setParameter(Parameter parameter) {
        this.parameter = parameter;
    }

    @TestEngine.BeforeAll
    public void beforeAll() {
        System.out.println("beforeAll()");
    }

    @TestEngine.Test
    public void test1() {
        System.out.println("test1(" + parameter.value() + ")");
    }

    @TestEngine.Test
    public void test2() {
        System.out.println("test2(" + parameter.value() + ")");
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
