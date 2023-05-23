package example.argument;

import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.api.argument.CharArgument;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;

/**
 * Example test
 */
public class CharArgumentTest {

    @TestEngine.Argument
    protected CharArgument charArgument;

    @TestEngine.ArgumentSupplier
    public static Stream<CharArgument> arguments() {
        char[] characters = new char[] { 'a', 'b', 'c', 'd', 'e' };
        Collection<CharArgument> collection = new ArrayList<>();
        for (int i = 0; i < characters.length; i++) {
            collection.add(CharArgument.of(characters[i]));
        }
        return collection.stream();
    }

    @TestEngine.BeforeAll
    public void beforeAll() {
        System.out.println("beforeAll()");
    }

    @TestEngine.Test
    public void test1() {
        System.out.println("test1(" + charArgument.value() + ")");
    }

    @TestEngine.Test
    public void test2() {
        System.out.println("test2(" + charArgument.value() + ")");
    }

    @TestEngine.AfterAll
    public void afterAll() {
        System.out.println("afterAll()");
    }
}
