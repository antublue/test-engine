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

package example.extension;

import java.util.List;
import org.antublue.test.engine.api.TestEngineExtension;

/** Example TestEngineExtensions */
public class ExampleTestEngineExtension implements TestEngineExtension {

    @Override
    public void instantiateCallback() {
        System.out.println(getClass().getName() + ".instantiateCallback()");
    }

    @Override
    public void discoveryCallback(List<Class<?>> testClasses) {
        System.out.println(getClass().getName() + ".discoveryCallback()");
        testClasses.forEach(
                testClass -> System.out.println("testClass [" + testClass.getName() + "]"));
    }

    @Override
    public void prepareCallback() {
        System.out.println(getClass().getName() + ".prepare()");
    }

    @Override
    public void concludeCallback() {
        System.out.println(getClass().getName() + ".conclude()");
    }
}
