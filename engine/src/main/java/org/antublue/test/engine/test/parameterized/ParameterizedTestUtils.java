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

package org.antublue.test.engine.test.parameterized;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.antublue.test.engine.api.Argument;
import org.antublue.test.engine.util.ReflectionUtils;

@SuppressWarnings("unchecked")
public class ParameterizedTestUtils {

    private static final ParameterizedTestUtils SINGLETON = new ParameterizedTestUtils();

    private static final ReflectionUtils REFLECTION_UTILS = ReflectionUtils.getSingleton();

    private ParameterizedTestUtils() {
        // DO NOTHING
    }

    public static ParameterizedTestUtils getSingleton() {
        return SINGLETON;
    }

    public Method getArumentSupplierMethod(Class<?> testClass) {
        return REFLECTION_UTILS
                .findMethods(testClass, ParameterizedTestFilters.ARGUMENT_SUPPLIER_METHOD)
                .get(0);
    }

    public List<Argument> getArguments(Class<?> testClass) throws Throwable {
        List<Argument> testArguments = new ArrayList<>();

        Object object = getArumentSupplierMethod(testClass).invoke(null, (Object[]) null);
        if (object instanceof Stream) {
            Stream<Argument> stream = (Stream<Argument>) object;
            stream.forEach(testArguments::add);
        } else if (object instanceof Iterable) {
            ((Iterable<Argument>) object).forEach(testArguments::add);
        } else {
            throw new RuntimeException(
                    String.format(
                            "Exception getting arguments for test class [%s]",
                            testClass.getName()));
        }

        return testArguments;
    }
}
