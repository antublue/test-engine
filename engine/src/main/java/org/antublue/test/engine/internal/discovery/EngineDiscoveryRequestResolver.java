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

package org.antublue.test.engine.internal.discovery;

import static java.lang.String.format;
import static org.junit.platform.engine.Filter.composeFilters;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.antublue.test.engine.api.Argument;
import org.antublue.test.engine.exception.TestClassDefinitionException;
import org.antublue.test.engine.exception.TestEngineException;
import org.antublue.test.engine.internal.configuration.Configuration;
import org.antublue.test.engine.internal.configuration.Constants;
import org.antublue.test.engine.internal.descriptor.ArgumentTestDescriptor;
import org.antublue.test.engine.internal.descriptor.ClassTestDescriptor;
import org.antublue.test.engine.internal.descriptor.TestMethodTestDescriptor;
import org.antublue.test.engine.internal.logger.Logger;
import org.antublue.test.engine.internal.logger.LoggerFactory;
import org.antublue.test.engine.internal.support.DisplayNameSupport;
import org.antublue.test.engine.internal.support.OrdererSupport;
import org.antublue.test.engine.internal.support.TagSupport;
import org.antublue.test.engine.internal.util.Predicates;
import org.junit.platform.commons.support.HierarchyTraversalMode;
import org.junit.platform.commons.support.ReflectionSupport;
import org.junit.platform.engine.DiscoveryFilter;
import org.junit.platform.engine.DiscoverySelector;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.discovery.ClassNameFilter;
import org.junit.platform.engine.discovery.ClassSelector;
import org.junit.platform.engine.discovery.ClasspathRootSelector;
import org.junit.platform.engine.discovery.MethodSelector;
import org.junit.platform.engine.discovery.PackageNameFilter;
import org.junit.platform.engine.discovery.PackageSelector;
import org.junit.platform.engine.discovery.UniqueIdSelector;
import org.junit.platform.engine.support.descriptor.EngineDescriptor;

/** Class to implement a EngineDiscoveryRequestResolver */
public class EngineDiscoveryRequestResolver {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(EngineDiscoveryRequestResolver.class);

    private static final Configuration CONFIGURATION = Configuration.getInstance();

    /** Constructor */
    public EngineDiscoveryRequestResolver() {
        // DO NOTHING
    }

    /**
     * Method to resolve the engine discovery request, building an engine descriptor
     *
     * @param engineDiscoveryRequest engineDiscoveryRequest
     * @param engineDescriptor engineDescriptor
     */
    public void resolveSelector(
            EngineDiscoveryRequest engineDiscoveryRequest, EngineDescriptor engineDescriptor) {
        try {
            List<Class<?>> testClasses = resolveEngineDiscoveryRequest(engineDiscoveryRequest);

            filterTestClassesByClassName(testClasses);
            filterTestClassesByTags(testClasses);

            OrdererSupport.orderTestClasses(testClasses);

            if (LOGGER.isTraceEnabled()) {
                testClasses.forEach(c -> LOGGER.trace("testClass [%s]", c.getName()));
            }

            for (Class<?> testClass : testClasses) {
                buildClassTestDescriptor(engineDescriptor, testClass);
            }

            prune(engineDescriptor);

            shuffleOrSortTestDescriptors(engineDescriptor);
        } catch (TestEngineException e) {
            throw e;
        } catch (Throwable t) {
            throw new TestEngineException(t);
        }
    }

    /**
     * Method to build a class test descriptor
     *
     * @param parentTestDescriptor parentTestDescriptor
     * @param testClass testClass
     * @throws Throwable Throwable
     */
    private static void buildClassTestDescriptor(
            TestDescriptor parentTestDescriptor, Class<?> testClass) throws Throwable {
        LOGGER.trace("buildClassTestDescriptor() testClass [%s]", testClass.getName());

        ClassTestDescriptor classTestDescriptor =
                ClassTestDescriptor.create(parentTestDescriptor.getUniqueId(), testClass);

        parentTestDescriptor.addChild(classTestDescriptor);

        int testArgumentIndex = 0;
        List<Argument<?>> testArguments = getArguments(testClass);
        for (Argument<?> testArgument : testArguments) {
            buildArgumentTestDescriptor(
                    classTestDescriptor, testClass, testArgument, testArgumentIndex);
            testArgumentIndex++;
        }
    }

    /**
     * Method to build an argument test descriptor
     *
     * @param parentTestDescriptor parentTestDescriptor
     * @param testClass testClass
     * @param testArgument testArgument
     * @param testArgumentIndex testArgumentIndex
     */
    private static void buildArgumentTestDescriptor(
            TestDescriptor parentTestDescriptor,
            Class<?> testClass,
            Argument<?> testArgument,
            int testArgumentIndex) {
        LOGGER.trace(
                "buildArgumentTestDescriptor() testClass [%s] testArgument [%s] testArgumentIndex",
                testClass.getName(), testArgument.getName(), testArgumentIndex);

        ArgumentTestDescriptor argumentTestDescriptor =
                ArgumentTestDescriptor.create(
                        parentTestDescriptor.getUniqueId(),
                        testClass,
                        testArgument,
                        testArgumentIndex);

        parentTestDescriptor.addChild(argumentTestDescriptor);

        buildTestMethodTestDescriptor(argumentTestDescriptor, testClass, testArgument);
    }

    /**
     * Method to build a test method test descriptor
     *
     * @param parentTestDescriptor parentTestDescriptor
     * @param testClass testClass
     * @param testArgument testArgument
     */
    private static void buildTestMethodTestDescriptor(
            TestDescriptor parentTestDescriptor, Class<?> testClass, Argument<?> testArgument) {
        LOGGER.trace(
                "buildTestMethodTestDescriptor() testClass [%s] testArgument [%s]",
                testClass.getName(), testArgument.getName());

        List<Method> testMethods =
                ReflectionSupport.findMethods(
                        testClass, Predicates.TEST_METHOD, HierarchyTraversalMode.TOP_DOWN);

        testMethods = OrdererSupport.orderTestMethods(testMethods, HierarchyTraversalMode.TOP_DOWN);

        filterTestMethodsByMethodName(testMethods);
        filterTestMethodsByTags(testMethods);

        for (Method testMethod : testMethods) {
            parentTestDescriptor.addChild(
                    TestMethodTestDescriptor.create(
                            parentTestDescriptor.getUniqueId(),
                            testClass,
                            testMethod,
                            testArgument));
        }
    }

    /**
     * Method to resolve a list of test classes
     *
     * @param engineDiscoveryRequest engineDiscoveryRequest
     * @return a list of classes
     * @throws Throwable Throwable
     */
    private static List<Class<?>> resolveEngineDiscoveryRequest(
            EngineDiscoveryRequest engineDiscoveryRequest) throws Throwable {
        Set<Class<?>> testClassSet = new HashSet<>();

        Predicate<String> predicate = null;

        List<? extends DiscoverySelector> discoverySelectors =
                engineDiscoveryRequest.getSelectorsByType(ClasspathRootSelector.class);
        for (DiscoverySelector discoverySelector : discoverySelectors) {
            ClasspathRootSelector classpathRootSelector = (ClasspathRootSelector) discoverySelector;

            List<Class<?>> javaClasses =
                    ReflectionSupport.findAllClassesInClasspathRoot(
                            classpathRootSelector.getClasspathRoot(),
                            Predicates.TEST_CLASS,
                            className -> true);

            for (Class<?> javaClass : javaClasses) {
                if (predicate == null) {
                    List<DiscoveryFilter<String>> filters = new ArrayList<>();
                    filters.addAll(engineDiscoveryRequest.getFiltersByType(ClassNameFilter.class));
                    filters.addAll(
                            engineDiscoveryRequest.getFiltersByType(PackageNameFilter.class));
                    predicate = composeFilters(filters).toPredicate();
                }

                if (!predicate.test(javaClass.getPackage().getName())) {
                    continue;
                }

                testClassSet.add(javaClass);
            }
        }

        discoverySelectors = engineDiscoveryRequest.getSelectorsByType(PackageSelector.class);
        for (DiscoverySelector discoverySelector : discoverySelectors) {
            PackageSelector packageSelector = (PackageSelector) discoverySelector;
            String packageName = packageSelector.getPackageName();

            List<Class<?>> javaClasses =
                    ReflectionSupport.findAllClassesInPackage(
                            packageName, Predicates.TEST_CLASS, p -> true);

            testClassSet.addAll(javaClasses);
        }

        discoverySelectors = engineDiscoveryRequest.getSelectorsByType(ClassSelector.class);
        for (DiscoverySelector discoverySelector : discoverySelectors) {
            ClassSelector classSelector = (ClassSelector) discoverySelector;
            Class<?> javaClass = classSelector.getJavaClass();

            if (Predicates.TEST_CLASS.test(javaClass)) {
                testClassSet.add(javaClass);
            }
        }

        discoverySelectors = engineDiscoveryRequest.getSelectorsByType(MethodSelector.class);
        for (DiscoverySelector discoverySelector : discoverySelectors) {
            MethodSelector methodSelector = (MethodSelector) discoverySelector;
            Class<?> javaClass = methodSelector.getJavaClass();
            Method javaMethod = methodSelector.getJavaMethod();

            if (Predicates.TEST_CLASS.test(javaClass) && Predicates.TEST_METHOD.test(javaMethod)) {
                testClassSet.add(javaClass);
            }
        }

        discoverySelectors = engineDiscoveryRequest.getSelectorsByType(UniqueIdSelector.class);
        for (DiscoverySelector discoverySelector : discoverySelectors) {
            UniqueIdSelector uniqueIdSelector = (UniqueIdSelector) discoverySelector;

            UniqueId uniqueId = uniqueIdSelector.getUniqueId();
            List<UniqueId.Segment> segments = uniqueId.getSegments();

            Class<?> javaClass = null;

            for (UniqueId.Segment segment : segments) {
                String segmentType = segment.getType();

                if (segmentType.equals(ClassTestDescriptor.class.getName())) {
                    String javaClassName = segment.getValue();
                    javaClass =
                            Thread.currentThread().getContextClassLoader().loadClass(javaClassName);
                }
            }

            if (javaClass != null) {
                testClassSet.add(javaClass);
            }
        }

        List<Class<?>> testClassList = new ArrayList<>(testClassSet);
        testClassList.sort(Comparator.comparing(Class::getName));

        return testClassList;
    }

    /**
     * Method to get argument for a test class
     *
     * @param testClass testClass
     * @return a list of arguments
     * @throws Throwable Throwable
     */
    private static List<Argument<?>> getArguments(Class<?> testClass) throws Throwable {
        List<Argument<?>> testArguments = new ArrayList<>();

        Object object = getArumentSupplierMethod(testClass).invoke(null, (Object[]) null);
        if (object == null) {
            return testArguments;
        }

        if (!(object instanceof Stream || object instanceof Iterable)) {
            throw new TestClassDefinitionException(
                    format(
                            "testClass [%s] @TestEngine.ArgumentSupplier must return a"
                                    + " Stream<Argument> or Iterable<Argument>, type returned [%s]",
                            testClass.getName(), object.getClass().getName()));
        }

        Iterator<?> iterator;
        if (object instanceof Stream) {
            Stream<?> stream = (Stream<?>) object;
            iterator = stream.iterator();
        } else {
            Iterable<?> iterable = (Iterable<?>) object;
            iterator = iterable.iterator();
        }

        long index = 0;
        while (iterator.hasNext()) {
            Object o = iterator.next();
            if (o instanceof Argument<?>) {
                testArguments.add((Argument<?>) o);
            } else {
                testArguments.add(Argument.of("[" + index + "]", o));
            }
            index++;
        }

        return testArguments;
    }

    /**
     * Method to get a test class argument supplier method
     *
     * @param testClass testClass
     * @return the argument supplier method
     */
    private static Method getArumentSupplierMethod(Class<?> testClass) {
        List<Method> methods =
                ReflectionSupport.findMethods(
                        testClass,
                        Predicates.ARGUMENT_SUPPLIER_METHOD,
                        HierarchyTraversalMode.BOTTOM_UP);

        return methods.get(0);
    }

    /**
     * Method to filter test classes by name
     *
     * @param testClasses testClasses
     */
    private void filterTestClassesByClassName(List<Class<?>> testClasses) {
        LOGGER.trace("filterTestClassesByName()");

        Optional<String> optional = CONFIGURATION.get(Constants.TEST_CLASS_INCLUDE_REGEX);
        if (optional.isPresent()) {
            Pattern pattern = Pattern.compile(optional.get());
            Matcher matcher = pattern.matcher("");

            Iterator<Class<?>> iterator = testClasses.iterator();
            while (iterator.hasNext()) {
                Class<?> clazz = iterator.next();
                matcher.reset(clazz.getName());
                if (!matcher.find()) {
                    iterator.remove();
                }
            }
        }

        optional = CONFIGURATION.get(Constants.TEST_CLASS_EXCLUDE_REGEX);
        if (optional.isPresent()) {
            Pattern pattern = Pattern.compile(optional.get());
            Matcher matcher = pattern.matcher("");

            Iterator<Class<?>> iterator = testClasses.iterator();
            while (iterator.hasNext()) {
                Class<?> clazz = iterator.next();
                matcher.reset(clazz.getName());
                if (matcher.find()) {
                    iterator.remove();
                }
            }
        }
    }

    /**
     * Method to filter test classes by tags
     *
     * @param testClasses testClasses
     */
    private void filterTestClassesByTags(List<Class<?>> testClasses) {
        LOGGER.trace("filterTestClassesByTags()");

        Optional<String> optional = CONFIGURATION.get(Constants.TEST_CLASS_TAG_INCLUDE_REGEX);
        if (optional.isPresent()) {
            Pattern pattern = Pattern.compile(optional.get());
            Matcher matcher = pattern.matcher("");

            Iterator<Class<?>> iterator = testClasses.iterator();
            while (iterator.hasNext()) {
                Class<?> clazz = iterator.next();
                String tag = TagSupport.getTag(clazz);
                if (tag == null) {
                    iterator.remove();
                } else {
                    matcher.reset(tag);
                    if (!matcher.find()) {
                        iterator.remove();
                    }
                }
            }
        }

        optional = CONFIGURATION.get(Constants.TEST_CLASS_TAG_EXCLUDE_REGEX);
        if (optional.isPresent()) {
            Pattern pattern = Pattern.compile(optional.get());
            Matcher matcher = pattern.matcher("");

            Iterator<Class<?>> iterator = testClasses.iterator();
            while (iterator.hasNext()) {
                Class<?> clazz = iterator.next();
                String tag = TagSupport.getTag(clazz);
                if (tag != null) {
                    matcher.reset(tag);
                    if (matcher.find()) {
                        iterator.remove();
                    }
                }
            }
        }
    }

    /**
     * Method to filter test methods by test method name
     *
     * @param testMethods testMethods
     */
    private static void filterTestMethodsByMethodName(List<Method> testMethods) {
        LOGGER.trace("filterTestMethodsByMethodName()");

        Optional<String> optional = CONFIGURATION.get(Constants.TEST_METHOD_INCLUDE_REGEX);
        if (optional.isPresent()) {
            Pattern pattern = Pattern.compile(optional.get());
            Matcher matcher = pattern.matcher("");

            Iterator<Method> iterator = testMethods.iterator();
            while (iterator.hasNext()) {
                Method testMethod = iterator.next();
                matcher.reset(DisplayNameSupport.getDisplayName(testMethod));
                if (!matcher.find()) {
                    iterator.remove();
                }
            }
        }

        optional = CONFIGURATION.get(Constants.TEST_METHOD_EXCLUDE_REGEX);
        if (optional.isPresent()) {
            Pattern pattern = Pattern.compile(optional.get());
            Matcher matcher = pattern.matcher("");

            Iterator<Method> iterator = testMethods.iterator();
            while (iterator.hasNext()) {
                Method testMethod = iterator.next();
                matcher.reset(DisplayNameSupport.getDisplayName(testMethod));
                if (matcher.find()) {
                    iterator.remove();
                }
            }
        }
    }

    /**
     * Method to filter test methods by tags
     *
     * @param testMethods testMethods
     */
    private static void filterTestMethodsByTags(List<Method> testMethods) {
        LOGGER.trace("filterTestMethodsByTag()");

        Optional<String> optional = CONFIGURATION.get(Constants.TEST_METHOD_TAG_INCLUDE_REGEX);
        if (optional.isPresent()) {
            Pattern pattern = Pattern.compile(optional.get());
            Matcher matcher = pattern.matcher("");

            Iterator<Method> iterator = testMethods.iterator();
            while (iterator.hasNext()) {
                Method testMethod = iterator.next();
                String tag = TagSupport.getTag(testMethod);
                if (tag == null) {
                    iterator.remove();
                } else {
                    matcher.reset(tag);
                    if (!matcher.find()) {
                        iterator.remove();
                    }
                }
            }
        }

        optional = CONFIGURATION.get(Constants.TEST_METHOD_TAG_EXCLUDE_REGEX);
        if (optional.isPresent()) {
            Pattern pattern = Pattern.compile(optional.get());
            Matcher matcher = pattern.matcher("");

            Iterator<Method> iterator = testMethods.iterator();
            while (iterator.hasNext()) {
                Method testMethod = iterator.next();
                String tag = TagSupport.getTag(testMethod);
                if (tag != null) {
                    matcher.reset(tag);
                    if (matcher.find()) {
                        iterator.remove();
                    }
                }
            }
        }
    }

    /**
     * Method to prune a test descriptor depth first
     *
     * @param testDescriptor testDescriptor
     */
    private static void prune(TestDescriptor testDescriptor) {
        // Prune child test descriptors
        Set<? extends TestDescriptor> children = new LinkedHashSet<>(testDescriptor.getChildren());
        for (TestDescriptor child : children) {
            prune(child);
        }

        // If we are the root, ignore pruning
        if (testDescriptor.isRoot()) {
            return;
        }

        // If test descriptor doesn't have children, remove it
        if (testDescriptor.isContainer() && testDescriptor.getChildren().isEmpty()) {
            testDescriptor.removeFromHierarchy();
        }
    }

    /**
     * Method to shuffle or sort an engine descriptor's children
     *
     * <p>Workaround for the fact that the engine descriptor returns an unmodifiable Set which can't
     * be sorted
     *
     * @param engineDescriptor engineDescriptor
     */
    private static void shuffleOrSortTestDescriptors(EngineDescriptor engineDescriptor) {
        List<TestDescriptor> testDescriptors = new ArrayList<>(engineDescriptor.getChildren());
        testDescriptors.forEach(engineDescriptor::removeChild);

        Optional<String> optional = CONFIGURATION.get(Constants.TEST_CLASS_SHUFFLE);
        optional.ifPresent(
                s -> {
                    if (Constants.TRUE.equals(optional.get())) {
                        Collections.shuffle(testDescriptors);
                    } else {
                        testDescriptors.sort(Comparator.comparing(TestDescriptor::getDisplayName));
                    }
                });

        testDescriptors.forEach(engineDescriptor::addChild);
    }
}
