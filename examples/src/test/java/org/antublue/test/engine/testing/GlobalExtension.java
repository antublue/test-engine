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

import org.antublue.test.engine.api.Extension;

public class GlobalExtension implements Extension {

    /**
     * Method to execute before all @TestEngine.Prepare methods
     *
     * @param testInstance testInstance
     * @throws Throwable Throwable
     */
    public void beforePrepare(Object testInstance) throws Throwable {
        System.out.println(
                String.format(
                        "%s beforePrepare %s",
                        getClass().getName(), testInstance.getClass().getName()));
    }

    /**
     * Method to execute after all @TestEngine.Conclude methods
     *
     * @param testInstance testInstance
     * @throws Throwable Throwable
     */
    public void afterConclude(Object testInstance) throws Throwable {
        System.out.println(
                String.format(
                        "%s afterConclude %s",
                        getClass().getName(), testInstance.getClass().getName()));
    }
}