package example;

import org.antublue.test.engine.api.Argument;
import org.antublue.test.engine.api.TestEngine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;

/**
 * Example test
 */
public class ArgumentTestWithProtectedMethods {

    private Argument argument;

    @TestEngine.Arguments
    protected static Stream<Argument> arguments() {
        Collection<Argument> collection = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            int value = i * 3;
            collection.add(org.antublue.test.engine.api.Argument.of(String.valueOf(value)));
        }
        return collection.stream();
    }

    @TestEngine.Argument
    protected void argument(Argument argument) {
        this.argument = argument;
    }

    @TestEngine.BeforeAll
    protected void beforeAll() {
        System.out.println("beforeAll()");
    }

    @TestEngine.Test
    protected void test1() {
        System.out.println("test1(" + argument.value() + ")");
    }

    @TestEngine.Test
    protected void test2() {
        System.out.println("test2(" + argument.value() + ")");
    }

    @TestEngine.AfterAll
    protected void afterAll() {
        System.out.println("afterAll()");
    }
}
