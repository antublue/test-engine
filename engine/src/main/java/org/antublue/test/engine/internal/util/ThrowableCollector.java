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

package org.antublue.test.engine.internal.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class ThrowableCollector implements Consumer<Throwable> {

    private final List<Throwable> throwables;

    public ThrowableCollector() {
        throwables = new ArrayList<>();
    }

    public void add(Throwable throwable) {
        throwables.add(throwable);
        throwable.printStackTrace();
    }

    public void add(Optional<Throwable> optionalThrowable) {
        if (optionalThrowable.isPresent()) {
            add(optionalThrowable.get());
        }
    }

    public void accept(Throwable throwable) {
        add(throwable);
    }

    public boolean isPresent() {
        return !throwables.isEmpty();
    }

    public boolean isEmpty() {
        return throwables.isEmpty();
    }

    public Throwable first() {
        return throwables.get(0);
    }

    public List<Throwable> throwables() {
        return throwables;
    }
}
