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

import org.antublue.test.engine.internal.TestExecutionContext;
import org.antublue.test.engine.internal.logger.Logger;
import org.antublue.test.engine.internal.logger.LoggerFactory;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.EngineDescriptor;
import org.junit.platform.launcher.TestExecutionListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

@SuppressWarnings("unchecked")
public final class RunnableEngineDescriptor extends EngineDescriptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(RunnableEngineDescriptor.class);

    private TestExecutionListener testExecutionListener;


    public RunnableEngineDescriptor(UniqueId uniqueId, String displayName) {
        super(uniqueId, displayName);
    }

    @Override
    public Optional<TestSource> getSource() {
        return Optional.ofNullable(null);
    }

    @Override
    public Type getType() {
        return Type.CONTAINER;
    }

    @Override
    public boolean isTest() {
        return false;
    }

    @Override
    public boolean isContainer() {
        return true;
    }

    public <T> List<T> getChildren(Class<T> clazz) {
        final List<T> list = new ArrayList<>();
        getChildren().forEach((Consumer<TestDescriptor>) testDescriptor -> list.add((T) testDescriptor));
        return list;
    }

    public void run() {
        getChildren(RunnableClassTestDescriptor.class)
                .forEach(executableClassTestDescriptor -> {
                    TestExecutionContext testExecutionContext = executableClassTestDescriptor.getTestExecutionContext();
                    executableClassTestDescriptor.setTestExecutionContext(testExecutionContext);
                    executableClassTestDescriptor.run();
                });
    }
}
