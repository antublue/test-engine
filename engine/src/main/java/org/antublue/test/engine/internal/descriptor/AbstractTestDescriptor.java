/*
 * Copyright 2022-2023 Douglas Hoard
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

package org.antublue.test.engine.internal.descriptor;

import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.UniqueId;

import java.util.ArrayList;
import java.util.List;

abstract class AbstractTestDescriptor extends org.junit.platform.engine.support.descriptor.AbstractTestDescriptor {

    private final List<TestExecutionResult> testExecutionResultList;

    protected AbstractTestDescriptor(UniqueId uniqueId, String displayName) {
        super(uniqueId, displayName);
        this.testExecutionResultList = new ArrayList<>();
    }

    public List<TestExecutionResult> getTestExecutionResultList() {
        return testExecutionResultList;
    }
}
