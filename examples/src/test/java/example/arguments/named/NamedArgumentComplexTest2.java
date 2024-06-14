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

package example.arguments.named;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;
import org.antublue.test.engine.api.Named;
import org.antublue.test.engine.api.TestEngine;

/** Example test */
public class NamedArgumentComplexTest2 {

    @TestEngine.Argument public ComplexArgument argument;

    @TestEngine.ArgumentSupplier
    public static Stream<Named<ComplexArgument>> arguments() {
        Collection<Named<ComplexArgument>> collection = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            collection.add(
                    Named.of(
                            "ComplexArgument(" + i + ")",
                            ComplexArgument.of("FirstName" + i, "LastName" + i)));
        }
        return collection.stream();
    }

    @TestEngine.BeforeAll
    public void beforeAll() {
        System.out.println("beforeAll(" + argument + ")");
        assertThat(argument).isNotNull();
        assertThat(argument.getFirstName()).isNotNull();
        assertThat(argument.getLastName()).isNotNull();
    }

    @TestEngine.Test
    public void test1() {
        System.out.println("test1(" + argument + ")");
        assertThat(argument).isNotNull();
        assertThat(argument.getFirstName()).isNotNull();
        assertThat(argument.getLastName()).isNotNull();
    }

    @TestEngine.Test
    public void test2() {
        System.out.println("test2(" + argument + ")");
        assertThat(argument).isNotNull();
        assertThat(argument.getFirstName()).isNotNull();
        assertThat(argument.getLastName()).isNotNull();
    }

    @TestEngine.AfterAll
    public void afterAll() {
        System.out.println("afterAll(" + argument + ")");
        assertThat(argument).isNotNull();
        assertThat(argument.getFirstName()).isNotNull();
        assertThat(argument.getLastName()).isNotNull();
    }

    public static class ComplexArgument {

        private final String firstName;
        private final String lastName;

        private ComplexArgument(String firstName, String lastName) {
            this.firstName = firstName;
            this.lastName = lastName;
        }

        public String getFirstName() {
            return firstName;
        }

        public String getLastName() {
            return lastName;
        }

        @Override
        public String toString() {
            return "firstName [" + firstName + "] lastName [" + lastName + "]";
        }

        public static ComplexArgument of(String firstName, String lastName) {
            return new ComplexArgument(firstName, lastName);
        }
    }
}
