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
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@SuppressWarnings("unchecked")
abstract class AbstractRunnableTestDescriptor
        extends org.junit.platform.engine.support.descriptor.AbstractTestDescriptor
        implements Runnable {

    private TestExecutionContext testExecutionContext;
    private ThrowableCollector throwableCollector;

    protected AbstractRunnableTestDescriptor(UniqueId uniqueId, String displayName) {
        super(uniqueId, displayName);
        throwableCollector = new ThrowableCollector();
    }

    public <T> List<T> getChildren(Class<T> clazz) {
        final List<T> list = new ArrayList<>();
        getChildren().forEach((Consumer<TestDescriptor>) testDescriptor -> list.add((T) testDescriptor));
        return list;
    }

    public void setTestExecutionContext(TestExecutionContext testExecutionContext) {
        this.testExecutionContext = testExecutionContext;
    }

    public void flush() {
        synchronized (System.out) {
            synchronized (System.err) {
                System.out.flush();
                System.err.flush();
            }
        }
    }

    public abstract void run();

    protected TestExecutionContext getTestExecutionContext() {
        return testExecutionContext;
    }

    protected ThrowableCollector getThrowableCollector() {
        return throwableCollector;
    }

    protected static Throwable resolve(Throwable t) {
        if (t instanceof InvocationTargetException) {
            return t.getCause();
        } else {
            return t;
        }
    }
}
