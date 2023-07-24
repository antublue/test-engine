package example.argument;

import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.api.argument.ObjectArgument;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;

/** Example test */
public class ObjectArgumentTest {

    private CustomObject customObject;

    @TestEngine.Argument protected ObjectArgument<CustomObject> objectArgument;

    @TestEngine.ArgumentSupplier
    public static Stream<ObjectArgument<CustomObject>> arguments() {
        Collection<ObjectArgument<CustomObject>> collection = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            collection.add(
                    new ObjectArgument<>("String " + i, new CustomObject(String.valueOf(i))));
        }
        return collection.stream();
    }

    @TestEngine.BeforeAll
    public void beforeAll() {
        System.out.println("beforeAll()");
        customObject = objectArgument.value();
    }

    @TestEngine.Test
    public void test1() {
        System.out.println("test1(" + customObject + ")");
    }

    @TestEngine.Test
    public void test2() {
        CustomObject customObject = objectArgument.value();
        System.out.println("test2(" + customObject + ")");
    }

    @TestEngine.AfterAll
    public void afterAll() {
        System.out.println("afterAll()");
    }

    public static class CustomObject {

        private final String value;

        public CustomObject(String value) {
            this.value = value;
        }

        public String value() {
            return value;
        }

        @Override
        public String toString() {
            return value;
        }
    }
}
