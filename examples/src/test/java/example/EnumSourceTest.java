package example;

import org.antublue.test.engine.api.SimpleParameter;
import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.api.source.EnumSource;

import java.io.IOException;
import java.util.stream.Stream;

/**
 * Example test
 */
@SuppressWarnings("unchecked")
public class EnumSourceTest {

    @TestEngine.Parameter
    public SimpleParameter<Enum> simpleParameter;

    @TestEngine.ParameterSupplier
    public static Stream<SimpleParameter<Enum>> parameters() throws IOException {
        return new EnumSource(DaysOfTheWeek.class).stream();
    }

    @TestEngine.BeforeAll
    public void beforeAll() {
        System.out.println("beforeAll()");
    }

    @TestEngine.Test
    public void test1() {
        System.out.println("test1(" + simpleParameter.name() + ", " + simpleParameter.value() + ")");
    }

    @TestEngine.Test
    public void test2() {
        System.out.println("test2(" + simpleParameter.name() + ", " + simpleParameter.value() + ")");
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
