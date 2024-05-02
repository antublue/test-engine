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

import java.lang.reflect.Method;
import java.util.function.Predicate;
import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.internal.util.ReflectionUtils;

/** Class to implement a predicate to match a test method */
public class TestMethodPredicate implements Predicate<Method> {

    /** Instance of the predicate */
    public static final TestMethodPredicate TEST_METHOD_PREDICATE = new TestMethodPredicate();

    @Override
    public boolean test(Method method) {
        return !ReflectionUtils.isAbstract(method)
                && !method.isAnnotationPresent(TestEngine.Disabled.class)
                && method.isAnnotationPresent(TestEngine.Test.class);
    }
}
