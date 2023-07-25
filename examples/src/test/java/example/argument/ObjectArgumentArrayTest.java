package example.argument;

import static org.assertj.core.api.Assertions.assertThat;

import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.api.argument.ObjectArgument;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;

/** Example test */
public class ObjectArgumentArrayTest {

    private int[] values;

    @TestEngine.Argument protected ObjectArgument<int[]> objectArgument;

    @TestEngine.ArgumentSupplier
    public static Stream<ObjectArgument<int[]>> arguments() {
        Collection<ObjectArgument<int[]>> collection = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            int[] values = new int[3];
            values[0] = i;
            values[1] = i * 2;
            values[2] = i * 3;

            collection.add(new ObjectArgument<>("values" + i, values));
        }
        return collection.stream();
    }

    @TestEngine.BeforeAll
    public void beforeAll() {
        System.out.println("beforeAll()");

        values = objectArgument.value();
    }

    @TestEngine.Test
    public void test1() {
        System.out.println("test1()");

        assertThat(values[1]).isEqualTo(values[0] * 2);
    }

    @TestEngine.Test
    public void test2() {
        System.out.println("test1()");

        assertThat(values[2]).isEqualTo(values[0] * 3);
    }

    @TestEngine.AfterAll
    public void afterAll() {
        System.out.println("afterAll()");
    }
}
