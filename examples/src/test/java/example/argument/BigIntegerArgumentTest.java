package example.argument;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;
import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.api.argument.BigIntegerArgument;

/** Example test */
public class BigIntegerArgumentTest {

    @TestEngine.Argument protected BigIntegerArgument bigIntegerArgument;

    @TestEngine.ArgumentSupplier
    public static Stream<BigIntegerArgument> arguments() {
        Collection<BigIntegerArgument> collection = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            collection.add(BigIntegerArgument.of(new BigInteger(String.valueOf(i))));
        }
        return collection.stream();
    }

    @TestEngine.BeforeAll
    public void beforeAll() {
        System.out.println("beforeAll()");
    }

    @TestEngine.Test
    public void test1() {
        System.out.println("test1(" + bigIntegerArgument.value() + ")");
    }

    @TestEngine.Test
    public void test2() {
        System.out.println("test2(" + bigIntegerArgument.value() + ")");
    }

    @TestEngine.AfterAll
    public void afterAll() {
        System.out.println("afterAll()");
    }
}
