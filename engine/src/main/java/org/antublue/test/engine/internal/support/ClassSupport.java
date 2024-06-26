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

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import org.junit.platform.commons.support.ReflectionSupport;

/** Class to implement ClassSupport */
public class ClassSupport {

    /** Constructor */
    private ClassSupport() {
        // DO NOTHING
    }

    /**
     * Method to scan the Java classpath and return Classes matching the Predicate
     *
     * @param predicate predicate
     * @return a List of Classes
     */
    public static List<Class<?>> findClasses(Predicate<Class<?>> predicate) {
        Set<Class<?>> set = new LinkedHashSet<>();
        for (URI uri : ClassPathSupport.getClasspathURIs()) {
            set.addAll(
                    ReflectionSupport.findAllClassesInClasspathRoot(uri, predicate, name -> true));
        }
        return new ArrayList<>(set);
    }
}
