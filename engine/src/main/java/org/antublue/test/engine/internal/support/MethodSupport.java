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

package org.antublue.test.engine.internal.support;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import org.junit.platform.commons.support.HierarchyTraversalMode;
import org.junit.platform.commons.support.ReflectionSupport;

/** Class to implement MethodSupport */
public class MethodSupport {

    /** Constructor */
    private MethodSupport() {
        // DO NOTHING
    }

    /**
     * Method to find Methods of a Class
     *
     * @param clazz clazz
     * @param predicate predicate
     * @param hierarchyTraversalMode hierarchyTraversalMode
     * @return a List of Methods
     */
    public static List<Method> findMethods(
            Class<?> clazz,
            Predicate<Method> predicate,
            HierarchyTraversalMode hierarchyTraversalMode) {
        return new ArrayList<>(
                ReflectionSupport.findMethods(clazz, predicate, hierarchyTraversalMode));
    }
}
