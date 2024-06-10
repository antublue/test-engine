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
import java.util.stream.Stream;
import org.antublue.test.engine.api.Named;
import org.antublue.test.engine.api.TestEngine;

/** Example test */
public class PlainObjectTest2 {

    @TestEngine.Argument protected Named<CustomArgument> argument;

    @TestEngine.ArgumentSupplier
    public static Stream<CustomArgument> arguments() {
        Collection<CustomArgument> collection = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            collection.add(CustomArgument.of("first name " + i, "last name " + i));
        }
        return collection.stream();
    }

    @TestEngine.Prepare
    public static void prepare() {
        System.out.println("prepare()");
    }

    @TestEngine.BeforeAll
    public void beforeAll() {
        System.out.println("beforeAll(" + argument + ")");
        assertThat(argument).isNotNull();
    }

    @TestEngine.BeforeEach
    public void beforeEach() {
        System.out.println("beforeEach(" + argument + ")");
        assertThat(argument).isNotNull();
    }

    @TestEngine.Test
    public void test1() {
        assertThat(argument).isNotNull();

        System.out.println("test1(" + argument + ")");
        System.out.println(
                "test1("
                        + argument.getPayload().getFirstName()
                        + ", "
                        + argument.getPayload().getLastName()
                        + ")");
    }

    @TestEngine.AfterEach
    public void afterEach() {
        System.out.println("afterEach(" + argument + ")");
        assertThat(argument).isNotNull();
    }

    @TestEngine.AfterAll
    public void afterAll() {
        System.out.println("afterAll(" + argument + ")");
        assertThat(argument).isNotNull();
    }

    @TestEngine.Conclude
    public static void conclude() {
        System.out.println("conclude()");
    }

    public static class CustomArgument {

        private final String firstName;
        private final String lastName;

        private CustomArgument(String firstName, String lastName) {
            this.firstName = firstName;
            this.lastName = lastName;
        }

        public String getFirstName() {
            return firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public static CustomArgument of(String firstName, String lastName) {
            return new CustomArgument(firstName, lastName);
        }
    }
}
