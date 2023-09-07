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

package org.antublue.test.engine.test.util;

import java.lang.reflect.Method;
import org.antublue.test.engine.api.Argument;
import org.antublue.test.engine.test.ThrowableContext;
import org.antublue.test.engine.util.ReflectionUtils;
import org.antublue.test.engine.util.Singleton;
import org.antublue.test.engine.util.StandardStreams;

public class MethodInvoker {

    private static final ReflectionUtils REFLECTION_UTILS = Singleton.get(ReflectionUtils.class);

    private MethodInvoker() {
        // DO NOTHING
    }

    public static void invoke(Method method, Object testInstance, Object testArgument)
            throws Throwable {
        if (REFLECTION_UTILS.acceptsArguments(method, Argument.class)) {
            method.invoke(testInstance, testArgument);
        } else {
            method.invoke(testInstance, (Object[]) null);
        }
    }
}
