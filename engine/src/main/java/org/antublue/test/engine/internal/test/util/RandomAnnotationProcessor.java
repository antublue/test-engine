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

import java.lang.reflect.Field;
import java.util.UUID;
import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.internal.logger.Logger;
import org.antublue.test.engine.internal.logger.LoggerFactory;
import org.antublue.test.engine.internal.util.RandomUtils;

/** Class to process @TestEngine.Random.X annotations */
@SuppressWarnings("PMD.AvoidAccessibilityAlteration")
public class RandomAnnotationProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(RandomAnnotationProcessor.class);

    /** Constructor */
    private RandomAnnotationProcessor() {
        // DO NOTHING
    }

    /**
     * Method to process @TestEngine.RandomX annotated fields
     *
     * @param object object
     * @param field field
     * @throws Throwable Throwable
     */
    public static void inject(Object object, Field field) throws Throwable {
        LOGGER.trace(
                "inject class [%s] field [%s] field type [%s]",
                object.getClass().getName(), field.getName(), field.getType().getName());

        field.setAccessible(true);

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
            return;
        }

        if (field.isAnnotationPresent(TestEngine.Random.BigInteger.class)) {
            TestEngine.Random.BigInteger annotation =
                    field.getAnnotation(TestEngine.Random.BigInteger.class);
            setBigInteger(object, field, annotation.minimum(), annotation.maximum());
            return;
        }

        if (field.isAnnotationPresent(TestEngine.Random.BigDecimal.class)) {
            TestEngine.Random.BigDecimal annotation =
                    field.getAnnotation(TestEngine.Random.BigDecimal.class);
            setBigDecimal(object, field, annotation.minimum(), annotation.maximum());
        }

        if (field.isAnnotationPresent(TestEngine.Random.UUID.class)) {
            LOGGER.trace(
                    "injecting UUID class [%s] field [%s]",
                    object.getClass().getName(), field.getName());

            if (field.getType().equals(String.class)) {
                field.set(object, UUID.randomUUID().toString());
            } else {
                field.set(object, UUID.randomUUID());
            }
        }
    }

    /**
     * Method to set a boolean field
     *
     * @param object object
     * @param field field
     * @throws Throwable Throwable
     */
    private static void setBoolean(Object object, Field field) throws Throwable {
        LOGGER.trace(
                "injecting random boolean class [%s] field [%s]",
                object.getClass().getName(), field.getName());

        field.set(object, RandomUtils.nextBoolean());
    }

    /**
     * Method to set an integer field
     *
     * @param object object
     * @param field field
     * @param minimum minimum
     * @param maximum maximum
     * @throws Throwable Throwable
     */
    private static void setInteger(Object object, Field field, int minimum, int maximum)
            throws Throwable {
        LOGGER.trace(
                "injecting random integer class [%s] field [%s] minimum [%s] maximum [%s]",
                object.getClass().getName(), field.getName(), minimum, maximum);

        field.setAccessible(true);
        field.set(object, RandomUtils.nextInteger(minimum, maximum));
    }

    /**
     * Method to set a long field
     *
     * @param object object
     * @param field field
     * @param minimum minimum
     * @param maximum maximum
     * @throws Throwable Throwable
     */
    private static void setLong(Object object, Field field, long minimum, long maximum)
            throws Throwable {
        LOGGER.trace(
                "injecting random long class [%s] field [%s] minimum [%s] maximum [%s]",
                object.getClass().getName(), field.getName(), minimum, maximum);

        field.set(object, RandomUtils.nextLong(minimum, maximum));
    }

    /**
     * Method to set a float field
     *
     * @param object object
     * @param field field
     * @param minimum minimum
     * @param maximum maximum
     * @throws Throwable Throwable
     */
    private static void setFloat(Object object, Field field, float minimum, float maximum)
            throws Throwable {
        LOGGER.trace(
                "injecting random float class [%s] field [%s] minimum [%s] maximum [%s]",
                object.getClass().getName(), field.getName(), minimum, maximum);

        field.set(object, RandomUtils.nextFloat(minimum, maximum));
    }

    /**
     * Method to set a double field
     *
     * @param object object
     * @param field field
     * @param minimum minimum
     * @param maximum maximum
     * @throws Throwable Throwable
     */
    private static void setDouble(Object object, Field field, double minimum, double maximum)
            throws Throwable {
        LOGGER.trace(
                "injecting random double class [%s] field [%s] minimum [%s] maximum [%s]",
                object.getClass().getName(), field.getName(), minimum, maximum);

        field.set(object, RandomUtils.nextDouble(minimum, maximum));
    }

    /**
     * Method to set a BigInteger field
     *
     * @param object object
     * @param field field
     * @param minimum minimum
     * @param maximum maximum
     * @throws Throwable Throwable
     */
    private static void setBigInteger(Object object, Field field, String minimum, String maximum)
            throws Throwable {
        LOGGER.trace(
                "injecting random BigInteger class [%s] field [%s] minimum [%s] maximum [%s]",
                object.getClass().getName(), field.getName(), minimum, maximum);

        field.set(object, RandomUtils.nextBigInteger(minimum, maximum));
    }

    /**
     * Method to set a BigDecimal field
     *
     * @param object object
     * @param field field
     * @param minimum minimum
     * @param maximum maximum
     * @throws Throwable Throwable
     */
    private static void setBigDecimal(Object object, Field field, String minimum, String maximum)
            throws Throwable {
        LOGGER.trace(
                "injecting random BigDecimal class [%s] field [%s] minimum [%s] maximum [%s]",
                object.getClass().getName(), field.getName(), minimum, maximum);

        field.set(object, RandomUtils.nextBigDecimal(minimum, maximum));
    }
}
