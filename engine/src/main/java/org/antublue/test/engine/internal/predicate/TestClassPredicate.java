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

package org.antublue.test.engine.internal.predicate;

import java.util.function.Predicate;
import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.internal.util.ReflectionUtils;
import org.junit.platform.commons.support.HierarchyTraversalMode;
import org.junit.platform.commons.support.ReflectionSupport;

/** Class to implement a predicate to match a test class */
public class TestClassPredicate implements Predicate<Class<?>> {

    /** Instance of the predicate */
    public static final TestClassPredicate TEST_CLASS_PREDICATE = new TestClassPredicate();

    @Override
    public boolean test(Class<?> clazz) {
        return !ReflectionUtils.isAbstract(clazz)
                && !clazz.isAnnotationPresent(TestEngine.Disabled.class)
                && !ReflectionSupport.findMethods(
                                clazz,
                                AnnotationMethodPredicate.of(TestEngine.Test.class),
                                HierarchyTraversalMode.TOP_DOWN)
                        .isEmpty()
                && !ReflectionSupport.findMethods(
                                clazz,
                                AnnotationMethodPredicate.of(TestEngine.ArgumentSupplier.class),
                                HierarchyTraversalMode.TOP_DOWN)
                        .isEmpty();
    }
}
