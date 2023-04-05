package org.antublue.test.engine.internal.predicate;

import java.lang.reflect.Method;

public final class TestMethodPredicate extends RegexPredicate<Method> {

    private TestMethodPredicate(String regex) {
        super(regex);
    }

    @Override
    public boolean test(Method method) {
        matcher.reset(method.getName());
        return matcher.find();
    }

    public static TestMethodPredicate of(String regex) {
        return new TestMethodPredicate(regex);
    }
}
