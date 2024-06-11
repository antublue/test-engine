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

package org.antublue.test.engine.internal.util;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.function.Executable;

public class ThrowableCollector {

    private final List<Throwable> throwables;

    public ThrowableCollector() {
        throwables = new ArrayList<>();
    }

    public List<Throwable> getThrowables() {
        return throwables;
    }

    public boolean isEmpty() {
        return throwables.isEmpty();
    }

    public boolean isNotEmpty() {
        return !isEmpty();
    }

    public void add(Throwable throwable) {
        throwables.add(throwable);
    }

    public void execute(Executable executable) {
        try {
            executable.execute();
        } catch (Throwable t) {
            throwables.add(t);
        }
    }
}
