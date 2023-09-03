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

package org.antublue.test.engine.test.util;

import java.lang.reflect.Method;
import java.util.List;
import org.antublue.test.engine.api.TestEngine;

public class TestDescriptorUtils {

    private static final TestDescriptorUtils SINGLETON = new TestDescriptorUtils();

    public enum Sort {
        FORWARD,
        REVERSE,
    }

    private TestDescriptorUtils() {
        // DO NOTHING
    }

    public static TestDescriptorUtils getSingleton() {
        return SINGLETON;
    }

    /**
     * Method to get a test method display name
     *
     * @param testClass testClass
     * @return the display name
     */
    public String getDisplayName(Class<?> testClass) {
        String displayName = testClass.getName();

        TestEngine.DisplayName annotation = testClass.getAnnotation(TestEngine.DisplayName.class);
        if (annotation != null) {
            String name = annotation.name();
            if (name != null && !name.trim().isEmpty()) {
                displayName = name.trim();
            }
        }

        return displayName;
    }

    /**
     * Method to get a test method display name
     *
     * @param testMethod testMethod
     * @return the display name
     */
    public String getDisplayName(Method testMethod) {
        String displayName = testMethod.getName();

        TestEngine.DisplayName annotation = testMethod.getAnnotation(TestEngine.DisplayName.class);
        if (annotation != null) {
            String name = annotation.name();
            if (name != null && !name.trim().isEmpty()) {
                displayName = name.trim();
            }
        }

        return displayName;
    }

    public void sortMethods(List<Method> methods, Sort sort) {
        methods.sort(
                (o1, o2) -> {
                    int o1Order = Integer.MAX_VALUE;
                    TestEngine.Order o1Annotation = o1.getAnnotation(TestEngine.Order.class);
                    if (o1Annotation != null) {
                        o1Order = o1Annotation.order();
                    }

                    int o2Order = Integer.MAX_VALUE;
                    TestEngine.Order o2Annotation = o2.getAnnotation(TestEngine.Order.class);
                    if (o2Annotation != null) {
                        o2Order = o2Annotation.order();
                    }

                    if (o1Order != o2Order) {
                        return Integer.compare(o1Order, o2Order);
                    }

                    // Order by display name which is either
                    // the name declared by @TestEngine.DisplayName
                    // or the real method name
                    String o1DisplayName = getDisplayName(o1);
                    String o2DisplayName = getDisplayName(o2);

                    switch (sort) {
                        case FORWARD:
                            {
                                return o1DisplayName.compareTo(o2DisplayName);
                            }
                        case REVERSE:
                            {
                                return -o1DisplayName.compareTo(o2DisplayName);
                            }
                        default:
                            {
                                return 0;
                            }
                    }
                });
    }
}
