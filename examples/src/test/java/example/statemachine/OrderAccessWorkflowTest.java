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

package example.statemachine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;
import org.antublue.test.engine.api.Argument;
import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.api.statemachine.StateMachine;

/** Example test */
@SuppressWarnings("PMD.EmptyCatchBlock")
public class OrderAccessWorkflowTest {

    @TestEngine.Argument protected OrderAccessWorkflow orderAccessWorkflow;

    @TestEngine.ArgumentSupplier
    public static Stream<OrderAccessWorkflow> arguments() {
        Collection<OrderAccessWorkflow> collection = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            collection.add(OrderAccessWorkflow.of("user-" + i, "password-" + i, i + 2));
        }

        return collection.stream();
    }

    @TestEngine.Test
    @TestEngine.DisplayName(name = "OrderAccessWorkflow")
    public void orderAccessWorkflow() throws Throwable {
        StateMachine<OrderAccessWorkflow.State> stateMachine = new StateMachine<>();

        stateMachine
                .addTransition(
                        OrderAccessWorkflow.State.BEGIN,
                        sm -> sm.signal(OrderAccessWorkflow.State.LOGIN))
                .addTransition(OrderAccessWorkflow.State.LOGIN, sm -> orderAccessWorkflow.login(sm))
                .addTransition(
                        OrderAccessWorkflow.State.ACCESS_ORDER,
                        sm -> orderAccessWorkflow.accessOrder(sm))
                .addTransition(
                        OrderAccessWorkflow.State.ACCESS_ORDER_DETAILS,
                        sm -> orderAccessWorkflow.accessOrderDetails(sm))
                .addTransition(
                        OrderAccessWorkflow.State.LOGOUT, sm -> orderAccessWorkflow.logout(sm))
                .addTransition(OrderAccessWorkflow.State.END, StateMachine::stop)
                .run(OrderAccessWorkflow.State.BEGIN);
    }

    public static class OrderAccessWorkflow implements Argument {

        private final String username;
        private final String password;
        private final int orderId;
        private final String string;
        private int loginAttempts;

        public enum State {
            BEGIN,
            LOGIN,
            ACCESS_ORDER,
            ACCESS_ORDER_DETAILS,
            LOGOUT,
            END
        }

        private OrderAccessWorkflow(String username, String password, int orderId) {
            this.username = username;
            this.password = password;
            this.orderId = orderId;
            this.string = String.format("%s / %s / %d", username, password, orderId);
        }

        @Override
        public String name() {
            return string;
        }

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }

        @Override
        public String toString() {
            return string;
        }

        public void login(StateMachine<State> stateMachine) {
            System.out.println("logging in > " + this);
            loginAttempts++;

            if (loginAttempts < 4) {
                // simulate 4 login failures
                System.out.println("login failure > " + this);

                // artificial delay
                sleep(120);

                stateMachine.signal(State.LOGIN);
                return;
            }

            System.out.println("logged in > " + this);
            stateMachine.signal(State.ACCESS_ORDER);
        }

        public void accessOrder(StateMachine<State> stateMachine) {
            System.out.format(this + " > accessing order [%d]", this.orderId).println();

            // artificial delay
            sleep(345);

            System.out.format(this + " > order [%d] accessed", this.orderId).println();

            stateMachine.signal(State.ACCESS_ORDER_DETAILS);
        }

        public void accessOrderDetails(StateMachine<State> stateMachine) {
            System.out.format(this + " > accessing order [%d] details", this.orderId).println();

            // artificial delay
            sleep(920);

            System.out.format(this + " > order [%d] details accessed", this.orderId).println();

            stateMachine.signal(State.LOGOUT);
        }

        public void logout(StateMachine<State> stateMachine) {
            System.out.println("logging out > " + this);

            // artificial delay
            sleep(302);

            System.out.println("logged out > " + this);

            stateMachine.signal(State.END);
        }

        private void sleep(long milliseconds) {
            try {
                Thread.sleep(milliseconds);
            } catch (InterruptedException e) {
                // DO NOTHING
            }
        }

        public static OrderAccessWorkflow of(String username, String password, int orderId) {
            return new OrderAccessWorkflow(username, password, orderId);
        }
    }
}
