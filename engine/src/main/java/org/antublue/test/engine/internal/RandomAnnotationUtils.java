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

package org.antublue.test.engine.internal;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Random;
import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.internal.logger.Logger;
import org.antublue.test.engine.internal.logger.LoggerFactory;

/** Class to process @TestEngine.Random.X annotations */
public class RandomAnnotationUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(RandomAnnotationUtils.class);

    private static final RandomAnnotationUtils SINGLETON = new RandomAnnotationUtils();

    private final Random RANDOM = new Random();

    /** Constructor */
    private RandomAnnotationUtils() {
        // DO NOTHING
    }

    /**
     * Method to get the singleton
     *
     * @return the singleton
     */
    public static RandomAnnotationUtils singleton() {
        return SINGLETON;
    }

    /**
     * Method to process @TestEngine.Random.X annotated fields
     *
     * @param object object
     * @param throwables throwables
     */
    public void processRandomAnnotatedFields(Object object, List<Throwable> throwables) {
        LOGGER.trace("processAutoCloseFields class [%s]", object.getClass().getName());

        TestEngineReflectionUtils.singleton()
                .getRandomFields(object.getClass())
                .forEach(
                        field -> {
                            try {
                                set(object, field);
                            } catch (Throwable t) {
                                throwables.add(t);
                            }
                        });
    }

    /**
     * Method to set a field
     *
     * @param object object
     * @param field field
     * @throws Throwable Throwable
     */
    private void set(Object object, Field field) throws Throwable {
        if (field.isAnnotationPresent(TestEngine.Random.Boolean.class)) {
            setBoolean(object, field);
            return;
        }

        if (field.isAnnotationPresent(TestEngine.Random.Integer.class)) {
            TestEngine.Random.Integer annotation =
                    field.getAnnotation(TestEngine.Random.Integer.class);
            setInteger(object, field, annotation.minimum(), annotation.maximum());
            return;
        }

        if (field.isAnnotationPresent(TestEngine.Random.Long.class)) {
            TestEngine.Random.Long annotation = field.getAnnotation(TestEngine.Random.Long.class);
            setLong(object, field, annotation.minimum(), annotation.maximum());
            return;
        }

        if (field.isAnnotationPresent(TestEngine.Random.Float.class)) {
            TestEngine.Random.Float annotation = field.getAnnotation(TestEngine.Random.Float.class);
            setFloat(object, field, annotation.minimum(), annotation.maximum());
            return;
        }

        if (field.isAnnotationPresent(TestEngine.Random.Double.class)) {
            TestEngine.Random.Double annotation =
                    field.getAnnotation(TestEngine.Random.Double.class);
            setDouble(object, field, annotation.minimum(), annotation.maximum());
        }
    }

    /**
     * Method to set a boolean field
     *
     * @param object object
     * @param field field
     * @throws Throwable Throwable
     */
    private void setBoolean(Object object, Field field) throws Throwable {
        LOGGER.trace("injecting random boolean");
        field.set(object, RANDOM.nextBoolean());
    }

    /**
     * Method to set an integer field
     *
     * @param object object
     * @param field field
     * @throws Throwable Throwable
     */
    private void setInteger(Object object, Field field, int minimum, int maximum) throws Throwable {
        LOGGER.trace("injecting random integer");
        field.set(object, (int) randomDouble(minimum, maximum));
    }

    /**
     * Method to set a long field
     *
     * @param object object
     * @param field field
     * @throws Throwable Throwable
     */
    private void setLong(Object object, Field field, long minimum, long maximum) throws Throwable {
        LOGGER.trace("injecting random long");
        field.set(object, (long) randomDouble(minimum, maximum));
    }

    /**
     * Method to set a float field
     *
     * @param object object
     * @param field field
     * @throws Throwable Throwable
     */
    private void setFloat(Object object, Field field, float minimum, float maximum)
            throws Throwable {
        LOGGER.trace("injecting random float");
        field.set(object, (float) randomDouble(minimum, maximum));
    }

    /**
     * Method to set a double field
     *
     * @param object object
     * @param field field
     * @throws Throwable Throwable
     */
    private void setDouble(Object object, Field field, double minimum, double maximum)
            throws Throwable {
        LOGGER.trace("injecting random double");
        field.set(object, randomDouble(minimum, maximum));
    }

    /**
     * Method to get a random double in a range (inclusive)
     *
     * @param minimum minimum
     * @param maximum maximum
     * @return a random double in a range (inclusive)
     */
    private double randomDouble(double minimum, double maximum) {
        return Math.random() * (maximum - minimum) + minimum;
    }
}
