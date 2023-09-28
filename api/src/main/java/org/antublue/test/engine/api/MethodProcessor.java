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

package org.antublue.test.engine.api;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

/** Interface to implement a MethodProcessor */
public interface MethodProcessor {

    /** MethodProcessor to shuffle test methods */
    MethodProcessor SHUFFLE_METHODS = (testClass, testMethods) -> Collections.shuffle(testMethods);

    /**
     * Method to process test method List
     *
     * @param testClass testClass
     * @param testMethods testMethods
     */
    void process(Class<?> testClass, List<Method> testMethods);
}
