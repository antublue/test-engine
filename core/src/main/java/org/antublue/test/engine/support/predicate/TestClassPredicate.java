package org.antublue.test.engine.support.predicate;

public final class TestClassPredicate extends RegexPredicate<Class<?>> {

    private TestClassPredicate(String regex) {
        super(regex);
    }

    @Override
    public boolean test(Class<?> clazz) {
        matcher.reset(clazz.getName());
        return matcher.find();
    }

    public static TestClassPredicate of(String regex) {
        return new TestClassPredicate(regex);
    }
}
