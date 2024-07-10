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

package org.antublue.test.engine.internal.execution;

import io.github.thunkware.vt.bridge.ThreadTool;
import org.antublue.test.engine.internal.configuration.Configuration;
import org.antublue.test.engine.internal.configuration.Constants;
import org.antublue.test.engine.internal.execution.impl.PlatformThreadsExecutionContextExecutor;
import org.antublue.test.engine.internal.execution.impl.VirtualThreadsExecutionContextExecutor;

/** Class to implement ExecutionContextExecutorFactory */
public class ExecutionContextExecutorFactory {

    private static final boolean useVirtualThreads =
            ThreadTool.hasVirtualThreads()
                    && "virtual"
                            .equalsIgnoreCase(
                                    Configuration.getInstance()
                                            .get(Constants.THREAD_TYPE)
                                            .orElse("platform"));

    /** Constructor */
    private ExecutionContextExecutorFactory() {
        // DO NOTHING
    }

    /**
     * Method to create an ExecutionContextExecutor
     *
     * @return an ExecutionContextExecutor
     */
    public static ExecutionContextExecutor createExecutionContextExecutor() {
        if (useVirtualThreads) {
            return new VirtualThreadsExecutionContextExecutor();
        } else {
            return new PlatformThreadsExecutionContextExecutor();
        }
    }
}
