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

package org.antublue.test.engine.internal.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Class to collect a list of Throwable
 */
public class ThrowableCollector {

    private final List<Throwable> throwableList;

    /**
     * Constructor
     */
    public ThrowableCollector() {
        throwableList = new ArrayList<>();
    }

    /**
     * Method to collect a Throwable
     *
     * @param throwable
     */
    public void add(Throwable throwable) {
        throwableList.add(throwable);
    }

    /**
     * Method to collect Throwable from another ThrowableCollector
     *
     * @param throwableCollector
     */
    public void addAll(ThrowableCollector throwableCollector) {
        throwableList.addAll(throwableCollector.getList());
    }

    /**
     * Method to get the number of Throwable collected
     *
     * @return
     */
    public int size() {
        return throwableList.size();
    }

    /**
     * Method to return whether the collector has any Throwable
     *
     * @return
     */
    public boolean isEmpty() {
        return throwableList.isEmpty();
    }

    /**
     * Method to get the first Throwable or null
     *
     * @return
     */
    public Optional<Throwable> getFirst() {
        if (throwableList.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(throwableList.get(0));
        }
    }

    /**
     * Method to get the list of Throwable
     *
     * @return
     */
    public List<Throwable> getList() {
        return throwableList;
    }
}
