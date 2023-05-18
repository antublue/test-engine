package deprecated;

import org.antublue.test.engine.api.Parameter;
import org.antublue.test.engine.api.TestEngine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Example test
 */
@SuppressWarnings("unchecked")
public class CustomParameterTest2 {

    @TestEngine.Parameter
    private CustomParameter customParameter;

    @TestEngine.ParameterSupplier
    public static Stream<Parameter> parameters() {
        Collection<Parameter> collection = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            collection.add(
                    CustomParameter.of(
                            "CustomParameter(" + i + ")",
                            "FirstName" + i,
                            "LastName" + i));
        }
        return collection.stream();
    }

    @TestEngine.BeforeAll
    public void beforeAll() {
        System.out.println("beforeAll()");
    }

    @TestEngine.Test
    public void test1() {
        System.out.println("test1(" + customParameter.getFirstName() + " " + customParameter.getLastName() + ")");
    }

    @TestEngine.Test
    public void test2() {
        System.out.println("test1(" + customParameter.getFirstName() + " " + customParameter.getLastName() + ")");
    }

    @TestEngine.AfterAll
    public void afterAll() {
        System.out.println("afterAll()");
    }

    private static class CustomParameter implements Parameter {

        private final String name;
        private final String firstName;
        private final String lastName;

        private CustomParameter(String name, String firstName, String lastName) {
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

        public static CustomParameter of(String name, String firstName, String lastName) {
            Objects.requireNonNull(name);
            return new CustomParameter(name, firstName, lastName);
        }
    }
}
