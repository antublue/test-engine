/*
 * Copyright (C) 2022-2023 The AntuBLUE test-engine project authors
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

/**
 * Class to collect a list of Throwable Exceptions
 */
public class ThrowableCollector implements Consumer<Throwable> {

    private final List<Throwable> throwableList;

    /**
     * Constructor
     */
    public ThrowableCollector() {
        throwableList = new ArrayList<>();
    }

    /**
     * Method to collect a Throwable Exceptions
     *
     * @param throwable throwable
     */
    public void add(Throwable throwable) {
        throwableList.add(throwable);
    }

    /**
     * Method to get the number of Throwable Exception collected
     *
     * @return the return value
     */
    public int size() {
        return throwableList.size();
    }

    /**
     * Method to return whether the ThrowableCollector is empty
     *
     * @return the return value
     */
    public boolean isEmpty() {
        return throwableList.isEmpty();
    }

    /**
     * Method to return whether the ThrowableCollector is not empty
     *
     * @return the return value
     */
    public boolean isNotEmpty() {
        return !throwableList.isEmpty();
    }

    /**
     * Method to get the first Throwable Exception
     *
     * @return the return value
     */
    public Optional<Throwable> getFirst() {
        if (throwableList.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(throwableList.get(0));
        }
    }

    /**
     * Method to get the List of Throwable Exceptions
     *
     * @return the return value
     */
    public List<Throwable> getList() {
        return throwableList;
    }

    /**
     * Method to accept a Throwable, adding to
     * the collector and printing the stack trace
     *
     * @param throwable throwable
     */
    public void accept(Throwable throwable) {
        add(throwable);
        throwable.printStackTrace();
    }
}
