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

package example.extension.annotation;

import java.lang.reflect.Field;
import org.antublue.test.engine.api.Argument;
import org.antublue.test.engine.api.Extension;
import org.antublue.test.engine.api.util.RandomGenerator;

/**
 * Example extension to process a field with a custom annotation after @TestEngine.BeforeAll methods
 */
public class RandomStringExtension implements Extension {

    private static final RandomGenerator RANDOM_GENERATOR = RandomGenerator.getInstance();

    private static final char[] ALPHA_NUMERIC_CHARACTERS =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789".toCharArray();

    /**
     * Method to call after beforeEach methods
     *
     * @param testArgument testArgument
     * @param testInstance testInstance
     * @throws Throwable Throwable
     */
    @Override
    public void postBeforeEachMethodsCallback(Object testInstance, Argument testArgument)
            throws Throwable {
        for (Field field : testInstance.getClass().getDeclaredFields()) {
            RandomStringAnnotation.Random.String.AlphaNumeric annotation =
                    field.getAnnotation(RandomStringAnnotation.Random.String.AlphaNumeric.class);
            if (annotation != null) {
                field.setAccessible(true);
                field.set(
                        testInstance,
                        randomAlphaNumericString(
                                annotation.minimumLength(), annotation.maximumLength()));
            }
        }
    }

    /**
     * Method to generate a random alphanumeric string
     *
     * <p>a minimum length < 0 allows a null string
     *
     * <p>a minimum length of 1 allows an empty string
     *
     * @param minimumLength minimumLength
     * @param maximumLength maximumLength
     * @return a random alphanumeric String
     */
    private static String randomAlphaNumericString(int minimumLength, int maximumLength) {
        StringBuilder stringBuilder = new StringBuilder();
        int count = RANDOM_GENERATOR.nextInteger(minimumLength, maximumLength);
        if (count == 0) {
            return "";
        } else if (count == -1) {
            return null;
        } else {
            for (int i = 0; i < count; i++) {
                stringBuilder.append(
                        ALPHA_NUMERIC_CHARACTERS[
                                RANDOM_GENERATOR.nextInteger(
                                        0, ALPHA_NUMERIC_CHARACTERS.length - 1)]);
            }
            return stringBuilder.toString();
        }
    }
}
