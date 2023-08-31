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

import java.lang.reflect.Method;

/** Class to implement a test method predicate */
public final class TestMethodPredicate extends RegexPredicate<Method> {

    /**
     * Constructor
     *
     * @param regex regex
     */
    private TestMethodPredicate(String regex) {
        super(regex);
    }

    /**
     * Method to test the Predicate
     *
     * @param method the input argument
     * @return whether to accept the Method
     */
    @Override
    public synchronized boolean test(Method method) {
        return matcher.reset(method.getName()).find();
    }

    /**
     * Method to create an instance of a TestMethodPredicate
     *
     * @param regex regex
     * @return the return value
     */
    public static TestMethodPredicate of(String regex) {
        return new TestMethodPredicate(regex);
    }
}
