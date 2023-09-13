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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import org.antublue.test.engine.api.Argument;
import org.antublue.test.engine.api.Extension;
import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.api.argument.StringArgument;

@TestEngine.Disabled
public class ParameterizedTestTestHandleTestExecutionTest {

    @TestEngine.ArgumentSupplier
    public static List<Argument> arguments() {
        List<Argument> list = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            list.add(StringArgument.of("StringArgument " + i));
        }
        return list;
    }

    @TestEngine.ExtensionSupplier
    public static List<Extension> extensions() {
        List<Extension> list = new ArrayList<>();
        list.add(new HandleTestExecutionTestExtension());
        return list;
    }

    @TestEngine.Test
    public void test1(StringArgument stringArgument) {
        if (stringArgument.name().contains("0")) {
            throw new RuntimeException("Forced exception");
        }
        System.out.println(String.format("test1(" + stringArgument + ")"));
    }

    @TestEngine.Test
    public void test2(StringArgument stringArgument) {
        if (stringArgument.name().contains("1")) {
            throw new RuntimeException("Forced exception");
        }
        System.out.println(String.format("test2(" + stringArgument + ")"));
    }

    public static class HandleTestExecutionTestExtension implements Extension {

        public void handleTestException(
                Object testInstance, Argument testArgument, Method testMethod, Throwable throwable)
                throws Throwable {
            System.out.println(
                    String.format(
                            "Exception in testMethod [%s] for testArgument [%s]",
                            testMethod.getName(), testArgument));
            if (testArgument.name().contains("1")) {
                throw throwable;
            }
        }
    }
}
