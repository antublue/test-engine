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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.antublue.test.engine.internal.logger.Logger;
import org.antublue.test.engine.internal.logger.LoggerFactory;

/**
 * Class to implement a StateMachine
 *
 * @param <T> state
 */
@SuppressWarnings("unchecked")
public class StateMachine<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(StateMachine.class);

    private final Map<T, Behavior<T>> map = new HashMap<>();
    private final String id;
    private T state;

    /**
     * Constructor
     *
     * @param id the state machine id
     * @param initialState the initialState
     */
    public StateMachine(String id, T initialState) {
        LOGGER.trace("StateMachine id [%s] initial state [%s]", id, initialState);

        this.id = id;
        this.state = initialState;
    }

    /**
     * Method to add a behavior
     *
     * @param state the state
     * @param behavior the Behavior
     */
    public StateMachine addBehavior(T state, Behavior<T> behavior) {
        LOGGER.trace("mapTransition state [%s]", state);

        if (map.containsKey(state)) {
            RuntimeException runtimeException =
                    new RuntimeException(
                            String.format(
                                    "PROGRAMMING ERROR transition already mapped to [%s]", state));
            runtimeException.printStackTrace(System.out);
            System.out.flush();
            throw runtimeException;
        }

        map.put(state, behavior);

        return this;
    }

    /**
     * Method to add a behavior
     *
     * @param states the states
     * @param behavior the Behavior
     */
    public StateMachine addBehavior(List<T> states, Behavior<T> behavior) {
        for (T state : states) {
            addBehavior(state, behavior);
        }

        return this;
    }

    /**
     * Method to get the state machine id
     *
     * @return the state machine id
     */
    public String id() {
        return id;
    }

    /**
     * Method to transition to the next state
     *
     * @param nextState the next state
     */
    public void transition(T nextState) {
        if (!map.containsKey(nextState)) {
            RuntimeException runtimeException =
                    new RuntimeException(
                            String.format(
                                    "PROGRAMMING ERROR no transition mapped to [%s]", nextState));
            runtimeException.printStackTrace(System.out);
            System.out.flush();
            throw runtimeException;
        }

        state = nextState;
    }

    /** Method to run the state machine */
    public void run() {
        while (state != null) {
            LOGGER.trace("state [%s]", state);
            map.get(state).perform(this);
        }
    }

    /** Method to signal the state machine to stop */
    public void stop() {
        LOGGER.trace("stop");
        state = null;
    }

    /**
     * Method to convert an array of states to a list
     *
     * @param states then array of states
     * @return a list of states
     */
    public List<T> asList(T... states) {
        List<T> list = new ArrayList<>(states.length);
        list.addAll(Arrays.asList(states));
        return list;
    }

    @Override
    public String toString() {
        return state.toString();
    }

    /**
     * Interface to implement a Behavior
     *
     * @param <T> state
     */
    public interface Behavior<T> {

        /**
         * Method to perform the behavior
         *
         * @param sm the state machine
         */
        void perform(StateMachine<T> sm);
    }
}
