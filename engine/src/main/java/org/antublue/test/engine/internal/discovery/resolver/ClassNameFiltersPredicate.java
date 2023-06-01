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

import org.antublue.test.engine.internal.logger.Logger;
import org.antublue.test.engine.internal.logger.LoggerFactory;
import org.junit.platform.engine.FilterResult;
import org.junit.platform.engine.discovery.ClassNameFilter;

import java.util.List;
import java.util.function.Predicate;

public class ClassNameFiltersPredicate implements Predicate<Class<?>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClassNameFiltersPredicate.class);

    private final List<ClassNameFilter> classNameFilters;

    public ClassNameFiltersPredicate(List<ClassNameFilter> classNameFilters) {
        this.classNameFilters = classNameFilters;
    }

    @Override
    public boolean test(Class<?> clazz) {
        if (classNameFilters == null || classNameFilters.isEmpty()) {
            LOGGER.trace("class [%s] included", clazz.getName());
            return true;
        }

        for (ClassNameFilter classNameFilter : classNameFilters) {
            FilterResult filterResult = classNameFilter.apply(clazz.getName());
            if (filterResult.excluded()) {
                LOGGER.trace("class [%s] excluded", clazz.getName());
                return false;
            }
        }

        LOGGER.trace("class [%s] included", clazz.getName());
        return true;
    }
}
