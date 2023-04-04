package example;

import org.antublue.test.engine.api.Argument;
import org.antublue.test.engine.api.TestEngine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;

/**
 * Example test
 */
public class ArgumentTest {

    private Argument argument;

    @TestEngine.Arguments
    public static Stream<Argument> arguments() {
        Collection<Argument> collection = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            int value = i * 3;
            collection.add(org.antublue.test.engine.api.Argument.of(String.valueOf(value)));
        }
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
    }

    @TestEngine.Test
    public void test2() {
        System.out.println("test2(" + argument.value() + ")");
    }

    @TestEngine.AfterAll
    public void afterAll() {
        System.out.println("afterAll()");
    }
}
