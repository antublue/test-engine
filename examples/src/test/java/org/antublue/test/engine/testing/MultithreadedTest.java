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

package org.antublue.test.engine.testing;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;
import org.antublue.test.engine.api.Argument;
import org.antublue.test.engine.api.TestEngine;

/** Example test */
public class MultithreadedTest {

    @TestEngine.Argument public Argument<String> argument;

    @TestEngine.Random.Integer public Integer randomInteger;

    @TestEngine.ArgumentSupplier
    public static Stream<Argument<String>> arguments() {
        Collection<Argument<String>> collection = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            collection.add(Argument.ofString("StringArgument " + i));
        }
        return collection.stream();
    }

    @TestEngine.Test
    public void test() throws Throwable {
        System.out.println("test(" + argument + ")");
        assertThat(argument).isNotNull();
        Thread.sleep(1000);
    }
}
