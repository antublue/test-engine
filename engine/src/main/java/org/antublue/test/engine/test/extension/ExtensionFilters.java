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

package org.antublue.test.engine.test.extension;

import java.lang.reflect.Method;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.util.ReflectionUtils;
import org.antublue.test.engine.util.Singleton;

public class ExtensionFilters {

    private static final ReflectionUtils REFLECTION_UTILS = Singleton.get(ReflectionUtils.class);

    public static final Predicate<Method> EXTENSION_SUPPLIER_METHOD =
            method ->
                    method.isAnnotationPresent(TestEngine.ExtensionSupplier.class)
                            && !method.isAnnotationPresent(TestEngine.Disabled.class)
                            && REFLECTION_UTILS.isStatic(method)
                            && (REFLECTION_UTILS.isPublic(method)
                                    || REFLECTION_UTILS.isProtected(method))
                            && REFLECTION_UTILS.hasParameterCount(method, 0)
                            && (REFLECTION_UTILS.hasReturnType(method, Stream.class)
                                    || REFLECTION_UTILS.hasReturnType(method, Iterable.class));

    private ExtensionFilters() {
        // DO NOTHING
    }
}
