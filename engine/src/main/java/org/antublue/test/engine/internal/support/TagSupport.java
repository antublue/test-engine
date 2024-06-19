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

import org.antublue.test.engine.api.TestEngine;

/** Class to implement TagUtils */
public class TagSupport {

    /** Constructor */
    private TagSupport() {
        // DO NOTHING
    }

    /**
     * Method to get a tag for a Class
     *
     * @param clazz clazz
     * @return a tag
     */
    public static String getTag(Class<?> clazz) {
        TestEngine.Tag annotation = clazz.getAnnotation(TestEngine.Tag.class);
        if (annotation != null) {
            String value = annotation.tag();
            if (value != null && !value.trim().isEmpty()) {
                return value;
            }
        }

        return null;
    }

    /**
     * Method to get a tag for a Method
     *
     * @param method method
     * @return a tag
     */
    public static String getTag(Method method) {
        TestEngine.Tag annotation = method.getAnnotation(TestEngine.Tag.class);
        if (annotation != null) {
            String value = annotation.tag();
            if (value != null && !value.trim().isEmpty()) {
                return value;
            }
        }

        return null;
    }
}
