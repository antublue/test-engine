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

package example.random;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;
import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.api.named.NamedString;

/** Example test */
public class RandomFieldTest5 {

    @TestEngine.Argument protected NamedString argument;

    @TestEngine.Random.Boolean protected Boolean randomBoolean;

    @TestEngine.Random.Integer(minimum = Integer.MIN_VALUE, maximum = 0)
    protected Integer randomInteger;

    @TestEngine.Random.Long(minimum = Long.MIN_VALUE, maximum = 0)
    protected Long randomLong;

    @TestEngine.Random.Float(minimum = -Float.MAX_VALUE, maximum = 0)
    protected Float randomFloat;

    @TestEngine.Random.Double(minimum = -Double.MAX_VALUE, maximum = 0)
    protected Double randomDouble;

    @TestEngine.ArgumentSupplier
    public static Stream<NamedString> arguments() {
        Collection<NamedString> collection = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            collection.add(NamedString.of("StringArgument " + i));
        }
        return collection.stream();
    }

    @TestEngine.Prepare
    public void prepare() {
        System.out.println("prepare()");
    }

    @TestEngine.BeforeAll
    public void beforeAll() {
        System.out.println("beforeAll(" + argument + ")");
        System.out.println("randomBoolean [" + randomBoolean + "]");
        System.out.println("randomInteger [" + randomInteger + "]");
        System.out.println("randomLong [" + randomLong + "]");
        System.out.println("randomFloat [" + randomFloat + "]");
        System.out.println("randomDouble [" + randomDouble + "]");

        assertThat(randomInteger).isBetween(Integer.MIN_VALUE, 0);
        assertThat(randomLong).isBetween(Long.MIN_VALUE, 0L);
        assertThat(randomFloat).isBetween(-Float.MAX_VALUE, 0F);
        assertThat(randomDouble).isBetween(-Double.MAX_VALUE, 0D);
    }

    @TestEngine.BeforeEach
    public void beforeEach() {
        System.out.println("beforeEach(" + argument + ")");
    }

    @TestEngine.Test
    public void test1() {
        System.out.println("test1(" + argument + ")");
    }

    @TestEngine.Test
    public void test2() {
        System.out.println("test2(" + argument + ")");
    }

    @TestEngine.AfterEach
    public void afterEach() {
        System.out.println("afterEach(" + argument + ")");
    }

    @TestEngine.AfterAll
    public void afterAll() {
        System.out.println("afterAll(" + argument + ")");
    }

    @TestEngine.Conclude
    public void conclude() {
        System.out.println("conclude()");
    }
}
