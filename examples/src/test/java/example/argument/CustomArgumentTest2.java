package example.argument;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Stream;
import org.antublue.test.engine.api.Argument;
import org.antublue.test.engine.api.TestEngine;

/** Example test */
@SuppressWarnings("unchecked")
public class CustomArgumentTest2 {

    @TestEngine.Argument private CustomArgument customArgument;

    @TestEngine.ArgumentSupplier
    public static Stream<Argument> arguments() {
        Collection<Argument> collection = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            collection.add(
                    CustomArgument.of(
                            "CustomArgument(" + i + ")", "FirstName" + i, "LastName" + i));
        }
        return collection.stream();
    }

    @TestEngine.BeforeAll
    public void beforeAll() {
        System.out.println("beforeAll()");
    }

    @TestEngine.Test
    public void test1() {
        System.out.println(
                "test1("
                        + customArgument.getFirstName()
                        + " "
                        + customArgument.getLastName()
                        + ")");
    }

    @TestEngine.Test
    public void test2() {
        System.out.println(
                "test1("
                        + customArgument.getFirstName()
                        + " "
                        + customArgument.getLastName()
                        + ")");
    }

    @TestEngine.AfterAll
    public void afterAll() {
        System.out.println("afterAll()");
    }

    private static class CustomArgument implements Argument {

        private final String name;
        private final String firstName;
        private final String lastName;

        private CustomArgument(String name, String firstName, String lastName) {
            this.name = name;
            this.firstName = firstName;
            this.lastName = lastName;
        }

        @Override
        public String name() {
            return name;
        }

        public String getFirstName() {
            return firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public static CustomArgument of(String name, String firstName, String lastName) {
            Objects.requireNonNull(name);
            return new CustomArgument(name, firstName, lastName);
        }
    }
}
