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
import java.util.function.Predicate;
import org.antublue.test.engine.api.MethodOrderer;
import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.util.ReflectionUtils;

public class TestFilters {

    private static final ReflectionUtils REFLECTION_UTILS = ReflectionUtils.getSingleton();

    public static final Predicate<Method> METHOD_ORDERER_SUPPLIER =
            method -> {
                if (!method.isAnnotationPresent(TestEngine.MethodOrderSupplier.class)) {
                    return false;
                }

                if (!REFLECTION_UTILS.isStatic(method)) {
                    return false;
                }

                if (!(REFLECTION_UTILS.isPublic(method) || REFLECTION_UTILS.isProtected(method))) {
                    return false;
                }

                if (!REFLECTION_UTILS.hasParameterCount(method, 0)) {
                    return false;
                }

                return REFLECTION_UTILS.hasReturnType(method, MethodOrderer.class);
            };

    private TestFilters() {
        // DO NOTHING
    }
}
