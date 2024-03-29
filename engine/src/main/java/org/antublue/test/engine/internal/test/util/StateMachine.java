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

package org.antublue.test.engine.internal.test.util;

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
    private T end;
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
     * Method to define a state, the action to perform, and possible target states
     *
     * @param source source
     * @param action action
     * @param targets targets
     * @return this
     */
    public StateMachine<T> define(T source, Action<T> action, T... targets) {
        Set<T> set = definitions.computeIfAbsent(source, k -> new HashSet<>());
        Collections.addAll(set, targets);
        actions.put(source, action);
        return this;
    }

    /**
     * Method to set the end state and action
     *
     * @param end end
     * @param endAction endAction
     * @return this
     */
    public StateMachine<T> end(T end, Action<T> endAction) {
        this.end = end;
        this.endAction = endAction;
        return this;
    }

    /**
     * Method to run the state machine
     *
     * @param begin begin
     * @throws StateMachineException StateMachineException
     */
    public void run(T begin) throws StateMachineException {
        T state = begin;
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
        } while (!state.equals(end));

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
