package example;

import org.antublue.test.engine.api.Argument;
import org.antublue.test.engine.api.TestEngine;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Example test
 */
public class StreamOfTest {

    private Argument argument;

    @TestEngine.Arguments
    public static Stream<Argument> arguments() {
        return Stream.of(
                org.antublue.test.engine.api.Argument.of(1),
                org.antublue.test.engine.api.Argument.of(2),
                org.antublue.test.engine.api.Argument.of(3));
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
        assertThat(argument.value(Integer.class).getClass()).isEqualTo(Integer.class);
    }

    @TestEngine.AfterAll
    public void afterAll() {
        System.out.println("afterAll()");
    }
}
