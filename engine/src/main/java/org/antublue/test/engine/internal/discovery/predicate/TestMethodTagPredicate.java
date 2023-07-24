/*
 * Copyright (C) 2022-2023 The AntuBLUE test-engine project authors
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

package org.antublue.test.engine.internal.discovery.predicate;

import org.antublue.test.engine.api.TestEngine;

import java.lang.reflect.Method;

/** Class to implement a test method tag predicate */
public final class TestMethodTagPredicate extends RegexPredicate<Method> {

    /**
     * Constructor
     *
     * @param regex regex
     */
    private TestMethodTagPredicate(String regex) {
        super(regex);
    }

    /**
     * Method to test the Predicate
     *
     * @param method the input argument
     * @return whether to accept the method
     */
    @Override
    public boolean test(Method method) {
        TestEngine.Tag annotation = method.getAnnotation(TestEngine.Tag.class);
        if (annotation == null) {
            return false;
        }

        String value = annotation.tag();
        return matcher.reset(value).find();
    }

    /**
     * Method to create an instance of a TestMethodTagPredicate
     *
     * @param regex regex
     * @return the return value
     */
    public static TestMethodTagPredicate of(String regex) {
        return new TestMethodTagPredicate(regex);
    }
}
