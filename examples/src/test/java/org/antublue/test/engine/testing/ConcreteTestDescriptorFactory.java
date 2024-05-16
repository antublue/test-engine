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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.antublue.test.engine.api.Named;
import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.api.support.NamedInteger;

public class ConcreteTestDescriptorFactory {

    @TestEngine.Argument private NamedInteger argument;

    @TestEngine.ArgumentSupplier
    public static Stream<Named<?>> arguments() {
        List<Named<?>> arguments = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            arguments.add(NamedInteger.of(i));
        }
        return arguments.stream();
    }

    @TestEngine.Test
    public void test0() {
        System.out.println("test0(" + argument + ")");
    }

    @TestEngine.Test
    public void test1(NamedInteger argument) {
        System.out.println("test1(" + argument + ")");
    }

    @TestEngine.Test
    public void test2(NamedInteger argument) {
        System.out.println("test2(" + argument + ")");
    }

    @TestEngine.Test
    public void test3(NamedInteger argument) {
        System.out.println("test3(" + argument + ")");
    }
}
