/*
 * Copyright 2022-2023 Douglas Hoard
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

package org.antublue.test.engine.internal.predicate;

import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class to implement a regex Predicate
 *
 * @param <T>
 */
public abstract class RegexPredicate<T> implements Predicate<T> {

    protected String regex;
    protected Pattern pattern;
    protected Matcher matcher;

    /**
     * Constructor
     *
     * @param regex the regular expression
     */
    protected RegexPredicate(String regex) {
        this.regex = regex;
        this.pattern = Pattern.compile(regex);
        this.matcher = pattern.matcher("");
    }

    /**
     * Method to get the regex
     *
     * @return the regular expression
     */
    public String getRegex() {
        return regex;
    }

    /**
     * Method to test the value using the regex
     *
     * @param value the input argument
     * @return whether to accept the input argument
     */
    public abstract boolean test(T value);
}
