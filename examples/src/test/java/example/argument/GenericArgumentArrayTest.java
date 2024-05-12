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

package example.argument;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;
import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.api.argument.GenericArgument;

/** Example test */
public class GenericArgumentArrayTest {

    private int[] values;

    @TestEngine.Argument protected GenericArgument<int[]> GenericArgument;

    @TestEngine.ArgumentSupplier
    public static Stream<GenericArgument<int[]>> arguments() {
        Collection<GenericArgument<int[]>> collection = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            int[] values = new int[3];
            values[0] = i;
            values[1] = i * 2;
            values[2] = i * 3;

            collection.add(new GenericArgument<>("values" + i, values));
        }
        return collection.stream();
    }

    @TestEngine.BeforeAll
    public void beforeAll() {
        System.out.println("beforeAll()");
        values = GenericArgument.value();
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
