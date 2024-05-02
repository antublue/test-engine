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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Class to implement a StateMachine */
@SuppressWarnings("unchecked")
public class StateMachine<T> {

    private final String id;
    private final Map<T, Set<T>> definitions;
    private final Map<T, Action<T>> actions;
    private final List<Action<T>> afterEachActions;
    private T endState;
    private Action<T> endAction;

    /**
     * Constructor
     *
     * @param id id
     */
    public StateMachine(String id) {
        this.id = id;
        this.definitions = new HashMap<>();
        this.actions = new HashMap<>();
        this.afterEachActions = new ArrayList<>();
    }

    /**
     * Method to add an afterEach action
     *
     * @param afterEachAction afterEachAction
     * @return this
     */
    public StateMachine<T> afterEach(Action<T> afterEachAction) {
        afterEachActions.add(afterEachAction);
        return this;
    }

    /**
     * Method to define a state, the action to perform when the state is encountered, and valid next
     * states
     *
     * @param state state
     * @param action action
     * @param nextStates nextStates
     * @return this
     */
    public StateMachine<T> definition(T state, Action<T> action, T... nextStates)
            throws StateMachineException {
        if (actions.containsKey(state)) {
            throw new StateMachineException(
                    String.format("Action for state [%s] already defined", state));
        }
        Set<T> set = definitions.computeIfAbsent(state, k -> new HashSet<>());
        Collections.addAll(set, nextStates);
        actions.put(state, action);
        return this;
    }

    /**
     * Method to set the end state and action
     *
     * @param state state
     * @param action action
     * @return this
     */
    public StateMachine<T> end(T state, Action<T> action) {
        this.endState = state;
        this.endAction = action;
        return this;
    }

    /**
     * Method to run the state machine
     *
     * @param initialState initialState
     * @throws StateMachineException StateMachineException
     */
    public void run(T initialState) throws StateMachineException {
        T state = initialState;
        T nextState;
        do {
            Action<T> action = actions.get(state);
            if (action == null) {
                throw new StateMachineException(
                        String.format("No action defined for state [%s]", state));
            } else {
                nextState = action.perform();
                if (nextState == null) {
                    throw new StateMachineException(
                            String.format("Action for state [%s] returned null", nextState));
                }
                if (nextState != null
                        && !definitions
                                .computeIfAbsent(state, k -> new HashSet<>())
                                .contains(nextState)) {
                    throw new StateMachineException(
                            String.format(
                                    "Invalid state transition [%s] -> [%s]", state, nextState));
                } else {
                    state = nextState;
                }

                for (Action<T> afterEachAction : afterEachActions) {
                    afterEachAction.perform();
                }
            }
        } while (!state.equals(endState));

        endAction.perform();
    }

    @Override
    public String toString() {
        return id;
    }

    /**
     * Interface to implement an Action
     *
     * @param <T> type
     */
    public interface Action<T> {

        /**
         * Method to implement the action
         *
         * @return the next state
         */
        T perform();
    }
}
