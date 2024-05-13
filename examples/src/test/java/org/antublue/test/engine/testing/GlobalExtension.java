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

package org.antublue.test.engine.testing;

import static java.lang.String.format;

import org.antublue.test.engine.api.Extension;

public class GlobalExtension implements Extension {

    public GlobalExtension() {
        System.out.println(format("Extension [%s]", getClass()));
    }

    /**
     * Method to execute after all @TestEngine.Conclude methods
     *
     * @param testInstance testInstance
     * @throws Throwable Throwable
     */
    @Override
    public void postConcludeMethodsCallback(Object testInstance) throws Throwable {
        System.out.println(
                format(
                        "%s.postConcludeMethodsCallback %s",
                        getClass().getSimpleName(), testInstance.getClass().getName()));
    }
}
