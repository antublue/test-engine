/*
 * Copyright (C) 2024 The AntuBLUE test-engine project authors
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

package org.antublue.test.engine.internal.reflection;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import org.junit.platform.commons.support.HierarchyTraversalMode;
import org.junit.platform.commons.support.ReflectionSupport;

public class MethodUtils {

    private MethodUtils() {
        // DO NOTHING
    }

    public static List<Method> getMethods(
            Class<?> clazz,
            Predicate<Method> predicate,
            HierarchyTraversalMode hierarchyTraversalMode) {
        List<Method> methods =
                new ArrayList<>(
                        ReflectionSupport.findMethods(clazz, predicate, hierarchyTraversalMode));

        methods.sort(Comparator.comparing(Method::getName));
        // TODO sort by @TestEngine.Order

        return methods;
    }
}
