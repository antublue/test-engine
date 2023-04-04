package example;

import org.antublue.test.engine.api.Argument;
import org.antublue.test.engine.api.TestEngine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Example test
 */
public class ArgumentOfCharTest {

    private Argument argument;

    @TestEngine.Arguments
    public static Stream<Argument> arguments() {
        Collection<Argument> collection = new ArrayList<>();
        collection.add(org.antublue.test.engine.api.Argument.of('a'));
        collection.add(org.antublue.test.engine.api.Argument.of('b'));
        return collection.stream();
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
        if (argument.value(Character.class) != null) {
            assertThat(argument.value(Character.class).getClass()).isEqualTo(Character.class);
        }
    }

    @TestEngine.AfterAll
    public void afterAll() {
        System.out.println("afterAll()");
    }
}
