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

import org.antublue.test.engine.internal.logger.Logger;
import org.antublue.test.engine.internal.logger.LoggerFactory;

/**
 * Class to implement a StateMachine
 *
 * @param <T> state type
 */
public class StateMachine<T extends Enum> {

    private static final Logger LOGGER = LoggerFactory.getLogger(StateMachine.class);

    private final String name;
    private T state;

    /**
     * Constructor
     *
     * @param name name
     */
    public StateMachine(String name, T state) {
        LOGGER.trace("StateMachine name [%s] state [%s]", name, state);

        this.name = name;
        this.state = state;
    }

    /**
     * Method to get the name
     *
     * @return the name
     */
    public String name() {
        return name;
    }

    /**
     * Method to get the state
     *
     * @return the state
     */
    public T state() {
        return state;
    }

    /**
     * Method to set the state
     *
     * @param state the state
     */
    public void set(T state) {
        LOGGER.trace("set state [%s]", state);
        this.state = state;
    }

    /**
     * Method to transition to a state if the current state matches
     *
     * @param checkState the state to check
     * @param nextState the next state
     * @return true if the transition occurs, else false
     */
    public boolean ifThen(T checkState, T nextState) {
        T currentState = state;
        boolean result = false;

        if (state == checkState) {
            state = nextState;
            result = true;
        }

        LOGGER.trace(
                "ifThen state [%s] checkState [%s] nextState [%s] result (%b) new state [%s]",
                currentState, checkState, nextState, result, state);

        return result;
    }

    /**
     * Method to transition to a state if the current state doesn't match
     *
     * @param checkState the state to check
     * @param nextState the next state
     * @return true if the transition occurs, else false
     */
    public boolean ifNotThen(T checkState, T nextState) {
        T currentState = state;
        boolean result = false;

        if (state != checkState) {
            state = nextState;
            result = true;
        }

        LOGGER.trace(
                "ifNotThen state [%s] checkState [%s] nextState [%s] result (%b) new state [%s]",
                currentState, checkState, nextState, result, state);

        return result;
    }

    /**
     * Method to check a boolean. If true, transition to the first state, else transition to the
     * second state
     *
     * @param value the value to check
     * @param trueState the next state if the value is true
     * @param falseState the next state if the value is false
     */
    public void ifTrueThenElse(boolean value, T trueState, T falseState) {
        if (value) {
            state = trueState;
        } else {
            state = falseState;
        }

        LOGGER.trace(
                "ifTrueThenElse value [%b] trueState [%s] falseState [%s] next state [%s]",
                value, trueState, falseState, state);
    }

    @Override
    public String toString() {
        return state.toString();
    }
}
