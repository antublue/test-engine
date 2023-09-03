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

package org.antublue.test.engine.test.descriptor;

import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestDescriptor;

/** Interface to implement an ExecutableTestDescriptor */
public interface ExecutableTestDescriptor extends TestDescriptor {

    /**
     * Method to execute the test descriptor
     *
     * @param executionRequest executionRequest
     * @param executableContext executionContext
     */
    void execute(ExecutionRequest executionRequest, ExecutableContext executableContext);
}
