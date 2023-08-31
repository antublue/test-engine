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

/** Class to implement a Predicate that matches a classname */
public final class TestClassPredicate extends RegexPredicate<Class<?>> {

    /**
     * Constructor
     *
     * @param regex regex
     */
    private TestClassPredicate(String regex) {
        super(regex);
    }

    /**
     * Method to test the Predicate
     *
     * @param testClass the input argument
     * @return whether to accept the Class
     */
    @Override
    public synchronized boolean test(Class<?> testClass) {
        return matcher.reset(testClass.getName()).find();
    }

    /**
     * Method to create an instance of a TestClassPredicate
     *
     * @param regex regex
     * @return the return value
     */
    public static TestClassPredicate of(String regex) {
        return new TestClassPredicate(regex);
    }
}
