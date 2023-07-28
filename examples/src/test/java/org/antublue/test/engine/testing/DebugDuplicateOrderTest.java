package org.antublue.test.engine.testing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;
import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.api.argument.IntegerArgument;

/** Example test */
public class DebugDuplicateOrderTest {

    @TestEngine.Argument protected IntegerArgument integerArgument;

    @TestEngine.ArgumentSupplier
    public static Stream<IntegerArgument> arguments() {
        Collection<IntegerArgument> collection = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            collection.add(IntegerArgument.of(i));
        }
        return collection.stream();
    }

    @TestEngine.BeforeAll
    @TestEngine.Order(order = 0)
    public void beforeAll() {
        System.out.println("beforeAll()");
    }

    @TestEngine.Test
    @TestEngine.Order(order = 0)
    public void test1() {
        System.out.println("test1(" + integerArgument.value() + ")");
    }

    @TestEngine.Test
    @TestEngine.Order(order = 1)
    // Switch to @TestEngine.Order(0) to test duplicate @TestEngine.Order detection
    public void test2() {
        System.out.println("test2(" + integerArgument.value() + ")");
    }

    @TestEngine.AfterAll
    public void afterAll() {
        System.out.println("afterAll()");
    }
}
