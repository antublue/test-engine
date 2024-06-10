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

import static java.lang.String.format;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import org.antublue.test.engine.api.Extension;
import org.antublue.test.engine.api.Named;
import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.api.support.named.NamedString;

@TestEngine.Disabled
public class ParameterizedTestTestHandleTestExecutionTest {

    @TestEngine.ArgumentSupplier
    public static List<NamedString> arguments() {
        List<NamedString> list = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            list.add(NamedString.of("StringArgument " + i));
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
    public void test1(NamedString argument) {
        if (argument.getName().contains("0")) {
            throw new RuntimeException("Forced exception");
        }
        System.out.println(format("test1(" + argument + ")"));
    }

    @TestEngine.Test
    public void test2(NamedString argument) {
        if (argument.getName().contains("1")) {
            throw new RuntimeException("Forced exception");
        }
        System.out.println(format("test2(" + argument + ")"));
    }

    public static class HandleTestExecutionTestExtension implements Extension {

        public void handleTestException(
                Object testInstance, Named testArgument, Method testMethod, Throwable throwable)
                throws Throwable {
            System.out.println(
                    format(
                            "Exception in testMethod [%s] for testArgument [%s]",
                            testMethod.getName(), testArgument));
            if (testArgument.getName().contains("1")) {
                throw throwable;
            }
        }
    }
}
