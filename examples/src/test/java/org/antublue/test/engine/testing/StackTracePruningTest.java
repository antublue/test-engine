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

import java.util.stream.Stream;
import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.api.argument.StringArgument;

/** Example test */
@TestEngine.Disabled
public class StackTracePruningTest {

    private static final Class<?>[] NO_CLASS_ARGS = (Class<?>[]) null;

    private static final Object[] NO_OBJECT_ARGS = (Object[]) null;

    // The stringArgument is required by the test engine, but is not actually used in test methods
    @TestEngine.Argument protected StringArgument stringArgument;

    // The stringArgument provides a node in the hierarchy, but is not actually used in test methods
    @TestEngine.ArgumentSupplier
    protected static Stream<StringArgument> arguments() {
        return Stream.of(StringArgument.of("----"));
    }

    @TestEngine.Test
    public void test() throws Throwable {
        new TestClass().test3();
    }

    public static class TestClass {

        public void test1() {
            throw new RuntimeException("FORCED EXCEPTION");
        }

        public void test2() throws Throwable {
            getClass().getMethod("reflectionTest1", NO_CLASS_ARGS).invoke(this, NO_OBJECT_ARGS);
        }

        public void test3() throws Throwable {
            getClass().getMethod("reflectionTest2", NO_CLASS_ARGS).invoke(this, NO_OBJECT_ARGS);
        }

        public void reflectionTest1() {
            throw new RuntimeException("FORCED EXCEPTION");
        }

        public void reflectionTest2() throws Throwable {
            getClass().getMethod("reflectionTest3", NO_CLASS_ARGS).invoke(this, NO_OBJECT_ARGS);
        }

        public void reflectionTest3() {
            throw new RuntimeException("FORCED EXCEPTION");
        }
    }

    private static void info(String format, Object... objects) {
        System.out.format(format, objects).println();
    }
}
