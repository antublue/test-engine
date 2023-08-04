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

import java.util.HashMap;
import java.util.Map;
import org.antublue.test.engine.internal.logger.Logger;
import org.antublue.test.engine.internal.logger.LoggerFactory;

/**
 * Class to implement a StateMachine
 *
 * @param <T> state
 */
public class StateMachine<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(StateMachine.class);

    private final Map<T, Transition<T>> map = new HashMap<>();
    private final String id;
    private T previous;
    private T current;

    /**
     * Constructor
     *
     * @param id id
     * @param begin begin
     */
    public StateMachine(String id, T begin) {
        this.id = id;
        this.previous = begin;
        this.current = begin;
    }

    /**
     * Method to map Transition
     *
     * @param state state
     * @param transition transition
     */
    public void mapTransition(T state, Transition<T> transition) {
        if (map.containsKey(state)) {
            RuntimeException runtimeException =
                    new RuntimeException(
                            String.format(
                                    "Programming error, transition already mapped to [%s]", state));
            runtimeException.printStackTrace();
            throw runtimeException;
        }

        map.put(state, transition);
    }

    /**
     * Method to map an array of Transitions
     *
     * @param states states
     * @param transition transition
     */
    public void mapTransition(T[] states, Transition<T> transition) {
        for (T state : states) {
            mapTransition(state, transition);
        }
    }

    /**
     * Method to get the id
     *
     * @return the id
     */
    public String id() {
        return id;
    }

    /**
     * Method to get the previous state
     *
     * @return the previous state
     */
    public T previous() {
        return previous;
    }

    /**
     * Method to get the current statue
     *
     * @return the current state
     */
    public T current() {
        return current;
    }

    /**
     * Method to transition to the next state
     *
     * @param next the next state
     */
    public void next(T next) {
        if (!map.containsKey(next)) {
            RuntimeException runtimeException =
                    new RuntimeException(
                            String.format("Programming error, no transition mapped to [%s]", next));
            runtimeException.printStackTrace();
            throw runtimeException;
        }

        previous = current;
        current = next;
    }

    /** Method to run the state machine */
    public void run() {
        while (current != null) {
            LOGGER.trace("current [%s]", current);
            map.get(current).run(this);
        }
    }

    /** Method to signal the state machine to exit */
    public void finish() {
        previous = current;
        current = null;
    }

    @Override
    public String toString() {
        return current.toString();
    }

    /**
     * Interface to implement a Transition
     *
     * @param <T> state
     */
    public interface Transition<T> {

        /**
         * Method to run the Transition
         *
         * @param stateMachine simpleStateMachine
         */
        void run(StateMachine<T> stateMachine);
    }
}
