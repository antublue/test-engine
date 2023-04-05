package org.antublue.test.engine.internal.predicate;

import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class RegexPredicate<T> implements Predicate<T> {

    protected String regex;
    protected Pattern pattern;
    protected Matcher matcher;

    protected RegexPredicate(String regex) {
        this.regex = regex;
        this.pattern = Pattern.compile(regex);
        this.matcher = pattern.matcher("");
    }

    public abstract boolean test(T value);
}
