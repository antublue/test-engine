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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ThrowableCollector {

    private final List<Throwable> throwables;

    public ThrowableCollector() {
        throwables = new ArrayList<>();
    }

    public void add(Throwable throwable) {
        throwables.add(throwable);
    }

    public void addAll(ThrowableCollector throwableCollector) {
        throwables.addAll(throwableCollector.getList());
    }

    public int size() {
        return throwables.size();
    }

    public boolean isEmpty() {
        return throwables.isEmpty();
    }

    public Optional<Throwable> getFirst() {
        if (throwables.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(throwables.get(0));
        }
    }

    public List<Throwable> getList() {
        return throwables;
    }
}
