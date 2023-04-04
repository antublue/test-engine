package example;

import org.antublue.test.engine.api.Argument;
import org.antublue.test.engine.api.ArgumentMap;
import org.antublue.test.engine.api.TestEngine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;

/**
 * Example test
 */
public class ArgumentMapTest2 {

    private ArgumentMap argumentMap;

    @TestEngine.Arguments
    public static Stream<Argument> arguments() {
        Collection<Argument> collection = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            collection.add(
                    org.antublue.test.engine.api.Argument.of(
                            "ArgumentMap[" + i + "]",
                            new ArgumentMap().put("key1", "value1")));
        }
        collection.add(org.antublue.test.engine.api.Argument.of("null value", new ArgumentMap().put("null key", null)));
        return collection.stream();
    }

    @TestEngine.Argument
    public void argument(Argument argument) {
        argumentMap = argument.value();
    }

    @TestEngine.BeforeAll
    public void beforeAll() {
        System.out.println("beforeAll()");
    }

    @TestEngine.Test
    public void test1() {
        String value = argumentMap.get("key1", String.class);
        System.out.println("test1(" + value + ")");
    }

    @TestEngine.Test
    public void test2() {
        String value = argumentMap.get("key1", String.class);
        System.out.println("test2(" + value + ")");
    }

    @TestEngine.AfterAll
    public void afterAll() {
        System.out.println("afterAll()");
    }
}
