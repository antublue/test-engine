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

package org.antublue.test.engine.internal.discovery.resolver;

import org.antublue.test.engine.internal.TestEngineReflectionUtils;
import org.antublue.test.engine.internal.logger.Logger;
import org.antublue.test.engine.internal.logger.LoggerFactory;

import java.lang.reflect.Method;
import java.util.function.Predicate;

public class IsTestMethodPredicate implements Predicate<Method> {

    private static final Logger LOGGER = LoggerFactory.getLogger(IsTestMethodPredicate.class);

    public static final IsTestMethodPredicate INSTANCE = new IsTestMethodPredicate();

    @Override
    public boolean test(Method method) {
        boolean result = TestEngineReflectionUtils.getTestMethods(method.getDeclaringClass()).contains(method);
        LOGGER.trace("class [%s] = [%b]", method.getDeclaringClass().getName(), result);
        return result;
    }
}
