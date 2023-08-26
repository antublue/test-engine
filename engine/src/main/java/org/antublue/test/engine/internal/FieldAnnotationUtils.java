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
import java.util.UUID;
import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.internal.logger.Logger;
import org.antublue.test.engine.internal.logger.LoggerFactory;
import org.antublue.test.engine.internal.util.RandomUtils;

/** Class to process @TestEngine.RandomX annotations */
public class FieldAnnotationUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(FieldAnnotationUtils.class);

    private static final FieldAnnotationUtils SINGLETON = new FieldAnnotationUtils();

    /** Constructor */
    private FieldAnnotationUtils() {
        // DO NOTHING
    }

    /**
     * Method to get the singleton
     *
     * @return the singleton
     */
    public static FieldAnnotationUtils singleton() {
        return SINGLETON;
    }

    /**
     * Method to process @TestEngine.RandomX annotated fields
     *
     * @param object object
     * @param throwables throwables
     */
    public void processAnnotatedFields(Object object, List<Throwable> throwables) {
        LOGGER.trace("processRandomAnnotatedFields class [%s]", object.getClass().getName());

        TestEngineReflectionUtils.singleton()
                .getAnnotatedFields(object.getClass())
                .forEach(
                        field -> {
                            try {
                                processAnnotatedFields(object, field);
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
    private void processAnnotatedFields(Object object, Field field) throws Throwable {
        LOGGER.trace(
                "processAnnotatedFields class [%s] field [%s] type [%s]",
                object.getClass().getName(), field.getName(), field.getType().getName());

        if (field.isAnnotationPresent(TestEngine.RandomBoolean.class)) {
            setBoolean(object, field);
            return;
        }

        if (field.isAnnotationPresent(TestEngine.RandomInteger.class)) {
            TestEngine.RandomInteger annotation =
                    field.getAnnotation(TestEngine.RandomInteger.class);
            setInteger(object, field, annotation.minimum(), annotation.maximum());
            return;
        }

        if (field.isAnnotationPresent(TestEngine.RandomLong.class)) {
            TestEngine.RandomLong annotation = field.getAnnotation(TestEngine.RandomLong.class);
            setLong(object, field, annotation.minimum(), annotation.maximum());
            return;
        }

        if (field.isAnnotationPresent(TestEngine.RandomFloat.class)) {
            TestEngine.RandomFloat annotation = field.getAnnotation(TestEngine.RandomFloat.class);
            setFloat(object, field, annotation.minimum(), annotation.maximum());
            return;
        }

        if (field.isAnnotationPresent(TestEngine.RandomDouble.class)) {
            TestEngine.RandomDouble annotation = field.getAnnotation(TestEngine.RandomDouble.class);
            setDouble(object, field, annotation.minimum(), annotation.maximum());
            return;
        }

        if (field.isAnnotationPresent(TestEngine.RandomBigInteger.class)) {
            TestEngine.RandomBigInteger annotation =
                    field.getAnnotation(TestEngine.RandomBigInteger.class);
            setBigInteger(object, field, annotation.minimum(), annotation.maximum());
            return;
        }

        if (field.isAnnotationPresent(TestEngine.RandomBigDecimal.class)) {
            TestEngine.RandomBigDecimal annotation =
                    field.getAnnotation(TestEngine.RandomBigDecimal.class);
            setBigDecimal(object, field, annotation.minimum(), annotation.maximum());
        }

        if (field.isAnnotationPresent(TestEngine.UUID.class)) {
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
    private void setBoolean(Object object, Field field) throws Throwable {
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
    private void setInteger(Object object, Field field, int minimum, int maximum) throws Throwable {
        LOGGER.trace(
                "injecting random integer class [%s] field [%s] minimum [%s] maximum [%s]",
                object.getClass().getName(), field.getName(), minimum, maximum);

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
    private void setLong(Object object, Field field, long minimum, long maximum) throws Throwable {
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
    private void setFloat(Object object, Field field, float minimum, float maximum)
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
    private void setDouble(Object object, Field field, double minimum, double maximum)
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
    private void setBigInteger(Object object, Field field, String minimum, String maximum)
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
    private void setBigDecimal(Object object, Field field, String minimum, String maximum)
            throws Throwable {
        LOGGER.trace(
                "injecting random BigDecimal class [%s] field [%s] minimum [%s] maximum [%s]",
                object.getClass().getName(), field.getName(), minimum, maximum);

        field.set(object, RandomUtils.nextBigDecimal(minimum, maximum));
    }
}
