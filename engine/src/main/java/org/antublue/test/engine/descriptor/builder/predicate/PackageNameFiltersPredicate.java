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

package org.antublue.test.engine.descriptor.builder.predicate;

import java.util.List;
import java.util.function.Predicate;
import org.antublue.test.engine.logger.Logger;
import org.antublue.test.engine.logger.LoggerFactory;
import org.junit.platform.engine.FilterResult;
import org.junit.platform.engine.discovery.PackageNameFilter;

/** Class to implement to filter a Class based on a List of PackageNameFilter */
public class PackageNameFiltersPredicate implements Predicate<Class<?>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(PackageNameFiltersPredicate.class);

    private final List<PackageNameFilter> packageNameFilters;

    /**
     * Constructor
     *
     * @param packageNameFilters packageNameFilters
     */
    public PackageNameFiltersPredicate(List<PackageNameFilter> packageNameFilters) {
        this.packageNameFilters = packageNameFilters;
    }

    /**
     * Method to test a Class
     *
     * @param clazz the input argument
     * @return the return value
     */
    @Override
    public boolean test(Class<?> clazz) {
        String packageName = clazz.getPackage().getName();

        if (packageNameFilters == null || packageNameFilters.isEmpty()) {
            LOGGER.trace("package [%s] included", packageName);
            return true;
        }

        for (PackageNameFilter packageNameFilter : packageNameFilters) {
            FilterResult filterResult = packageNameFilter.apply(clazz.getName());
            if (filterResult.excluded()) {
                LOGGER.trace("package [%s] excluded", packageName);
                return false;
            }
        }

        LOGGER.trace("package [%s] included", packageName);

        return true;
    }
}
