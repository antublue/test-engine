package example.argument;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;
import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.api.argument.BigDecimalArgument;

/** Example test */
public class BigDecimalArgumentTest {

    @TestEngine.Argument protected BigDecimalArgument bigDecimalArgument;

    @TestEngine.ArgumentSupplier
    public static Stream<BigDecimalArgument> arguments() {
        Collection<BigDecimalArgument> collection = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            collection.add(BigDecimalArgument.of(new BigDecimal(i + ".0")));
        }
        return collection.stream();
    }

    @TestEngine.BeforeAll
    public void beforeAll() {
        System.out.println("beforeAll()");
    }

    @TestEngine.Test
    public void test1() {
        System.out.println("test1(" + bigDecimalArgument.value() + ")");
    }

    @TestEngine.Test
    public void test2() {
        System.out.println("test2(" + bigDecimalArgument.value() + ")");
    }

    @TestEngine.AfterAll
    public void afterAll() {
        System.out.println("afterAll()");
    }
}
