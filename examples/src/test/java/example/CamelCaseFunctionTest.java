package example;

import static org.assertj.core.api.Assertions.assertThat;

import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.api.argument.ObjectArgument;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Example test
 *
 * <p>Made up... in real life you wouldn't do this
 */
public class CamelCaseFunctionTest {

    private static Function<String, String> FUNCTION = new CamelCaseFunction();

    private Tuple tuple;

    @TestEngine.Argument protected ObjectArgument<Tuple> objectArgument;

    @TestEngine.ArgumentSupplier
    public static Stream<ObjectArgument<Tuple>> arguments() {
        Collection<ObjectArgument<Tuple>> collection = new ArrayList<>();

        Tuple tuple =
                new Tuple("THIS STRING SHOULD BE IN CAMEL CASE", "thisStringShouldBeInCamelCase");
        collection.add(new ObjectArgument<>(tuple.input, tuple));

        tuple = new Tuple("THIS string SHOULD be IN camel CASE", "thisStringShouldBeInCamelCase");
        collection.add(new ObjectArgument<>(tuple.input, tuple));

        tuple = new Tuple("THIS", "this");
        collection.add(new ObjectArgument<>(tuple.input, tuple));

        tuple = new Tuple("tHis", "this");
        collection.add(new ObjectArgument<>(tuple.input, tuple));

        return collection.stream();
    }

    @TestEngine.BeforeAll
    public void beforeAll() {
        System.out.println("beforeAll()");
        tuple = objectArgument.value();
    }

    @TestEngine.Test
    public void test() {
        String actual = FUNCTION.apply(tuple.input);
        System.out.println(
                "test() input ["
                        + tuple.input
                        + "] expected ["
                        + tuple.expected
                        + "] actual ["
                        + actual
                        + "]");
        assertThat(actual).isEqualTo(tuple.expected);
    }

    // Based on https://www.baeldung.com/java-string-to-camel-case
    private static class CamelCaseFunction implements Function<String, String> {

        public String apply(String string) {
            if (string == null) {
                return null;
            }

            String[] words = string.split("[\\W_]+");
            StringBuilder builder = new StringBuilder();

            for (int i = 0; i < words.length; i++) {
                String word = words[i];

                if (i == 0) {
                    word = word.isEmpty() ? word : word.toLowerCase(Locale.getDefault());
                } else {
                    word =
                            word.isEmpty()
                                    ? word
                                    : (word.charAt(0) + "").toUpperCase(Locale.getDefault())
                                            + word.substring(1).toLowerCase(Locale.getDefault());
                }

                builder.append(word);
            }

            return builder.toString();
        }
    }

    private static class Tuple {

        public String input;
        public String expected;

        public Tuple(String input, String expected) {
            this.input = input;
            this.expected = expected;
        }
    }
}
