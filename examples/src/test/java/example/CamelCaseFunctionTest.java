/*
 * Copyright (C) 2023 The AntuBLUE test-engine project authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package example;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;
import java.util.function.Function;
import java.util.stream.Stream;
import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.api.argument.ObjectArgument;

/**
 * Example test
 *
 * <p>Made up... in real life you wouldn't do this
 */
public class CamelCaseFunctionTest {

    private static Function<String, String> FUNCTION = new CamelCaseFunction();

    private InputOutputArgument inputOutputArgument;

    @TestEngine.Argument protected ObjectArgument<InputOutputArgument> objectArgument;

    @TestEngine.ArgumentSupplier
    public static Stream<ObjectArgument<InputOutputArgument>> arguments() {
        Collection<ObjectArgument<InputOutputArgument>> collection = new ArrayList<>();

        InputOutputArgument inputOutputArgument =
                new InputOutputArgument(
                        "THIS STRING SHOULD BE IN CAMEL CASE", "thisStringShouldBeInCamelCase");

        collection.add(new ObjectArgument<>(inputOutputArgument.input, inputOutputArgument));

        inputOutputArgument =
                new InputOutputArgument(
                        "THIS string SHOULD be IN camel CASE", "thisStringShouldBeInCamelCase");
        collection.add(new ObjectArgument<>(inputOutputArgument.input, inputOutputArgument));

        inputOutputArgument = new InputOutputArgument("THIS", "this");
        collection.add(new ObjectArgument<>(inputOutputArgument.input, inputOutputArgument));

        inputOutputArgument = new InputOutputArgument("tHis", "this");
        collection.add(new ObjectArgument<>(inputOutputArgument.input, inputOutputArgument));

        return collection.stream();
    }

    @TestEngine.BeforeAll
    public void beforeAll() {
        System.out.println("beforeAll()");
        inputOutputArgument = objectArgument.value();
    }

    @TestEngine.Test
    public void test() {
        String actual = FUNCTION.apply(inputOutputArgument.input);
        System.out.println(
                "test() input ["
                        + inputOutputArgument.input
                        + "] expected ["
                        + inputOutputArgument.expected
                        + "] actual ["
                        + actual
                        + "]");
        assertThat(actual).isEqualTo(inputOutputArgument.expected);
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

    private static class InputOutputArgument {

        public String input;
        public String expected;

        public InputOutputArgument(String input, String expected) {
            this.input = input;
            this.expected = expected;
        }
    }
}
