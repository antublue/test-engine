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

package org.antublue.test.engine.internal.statemachine;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/** Class to implement a StateMachine */
public class StateMachine<S> {

    private final Map<S, Transition<S>> map = new HashMap<>();
    private final String id;
    private final AtomicReference<S> state;
    private final AtomicBoolean stop;
    private Throwable throwable;

    /** Constructor */
    public StateMachine() {
        this(UUID.randomUUID().toString());
    }

    /**
     * Constructor
     *
     * @param id the state machine id
     */
    public StateMachine(String id) {
        if (id == null || id.trim().isEmpty()) {
            this.id = UUID.randomUUID().toString();
        } else {
            this.id = id.trim();
        }

        this.state = new AtomicReference<>();
        this.stop = new AtomicBoolean();
    }

    /**
     * Method to add a state transition
     *
     * @param state the state
     * @param transition the transition
     * @return this
     */
    public StateMachine<S> addTransition(S state, Transition<S> transition) {
        notNull(state, "state is null");
        notNull(transition, "transition is null");

        if (map.containsKey(state)) {
            throw new StateMachineException(
                    String.format("transition already defined for state [%s]", state));
        }

        map.put(state, transition);

        return this;
    }

    /**
     * Method to get the state machine id
     *
     * @return the state machine id
     */
    public String getId() {
        return id;
    }

    /**
     * Method to set the state
     *
     * @param state the state
     */
    public void signal(S state) {
        this.state.set(state);
    }

    /**
     * Method start the state machine
     *
     * @param state the beginning state
     * @throws Throwable an exception encountered when running
     */
    public void run(S state) throws Throwable {
        if (!map.containsKey(state)) {
            throw new StateMachineException(
                    String.format("no transition defined for state [%s]", state));
        }

        this.state.set(state);

        while (!stop.get()) {
            S nextState = this.state.get();
            if (nextState == null) {
                stop.set(true);
                break;
            }

            Transition<S> transition = map.get(nextState);
            if (transition == null) {
                throw new StateMachineException(
                        String.format("no transition defined for state [%s]", nextState));
            }

            try {
                transition.execute(this);
            } catch (Throwable t) {
                throw new StateMachineException(
                        String.format(
                                "unhandled exception in transition for state [%s]",
                                this.getState()),
                        t);
            }
        }

        if (throwable != null) {
            throw throwable;
        }
    }

    /** Method to stop the state machine */
    public void stop() {
        stop(null);
    }

    /**
     * Method to stop the state machine
     *
     * @param throwable throwable
     */
    public void stop(Throwable throwable) {
        stop.set(true);
        this.throwable = throwable;
    }

    /**
     * Method to get the current state
     *
     * @return the current state
     */
    public S getState() {
        return state.get();
    }

    @Override
    public String toString() {
        return state.toString();
    }

    private static void notNull(Object object, String message) {
        if (object == null) {
            throw new StateMachineException(message);
        }
    }
}
