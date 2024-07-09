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

import static org.junit.platform.engine.Filter.composeFilters;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
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
import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.exception.TestEngineException;
import org.antublue.test.engine.internal.configuration.Configuration;
import org.antublue.test.engine.internal.configuration.Constants;
import org.antublue.test.engine.internal.descriptor.ArgumentTestDescriptor;
import org.antublue.test.engine.internal.descriptor.ClassTestDescriptor;
import org.antublue.test.engine.internal.descriptor.TestMethodTestDescriptor;
import org.antublue.test.engine.internal.logger.Logger;
import org.antublue.test.engine.internal.logger.LoggerFactory;
import org.antublue.test.engine.internal.support.ClassPathSupport;
import org.antublue.test.engine.internal.support.DisplayNameSupport;
import org.antublue.test.engine.internal.support.MethodSupport;
import org.antublue.test.engine.internal.support.OrdererSupport;
import org.antublue.test.engine.internal.support.TagSupport;
import org.antublue.test.engine.internal.util.StopWatch;
import org.junit.platform.commons.support.HierarchyTraversalMode;
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
    public void resolveSelectors(
            EngineDiscoveryRequest engineDiscoveryRequest, EngineDescriptor engineDescriptor) {
        LOGGER.trace("resolveSelectors()");

        StopWatch stopWatch = new StopWatch();

        try {
            List<Class<?>> testClasses = resolveEngineDiscoveryRequest(engineDiscoveryRequest);

            filterTestClassesByClassName(testClasses);
            filterTestClassesByTags(testClasses);

            OrdererSupport.orderTestClasses(testClasses);

            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("working testClasses...");
                testClasses.forEach(c -> LOGGER.trace("testClass [%s]", c.getName()));
            }

            for (Class<?> testClass : testClasses) {
                buildClassTestDescriptor(engineDescriptor, testClass);
            }

            LOGGER.trace("pruning...");
            prune(engineDescriptor);

            shuffle(engineDescriptor);
        } catch (TestEngineException e) {
            throw e;
        } catch (Throwable t) {
            throw new TestEngineException(t);
        } finally {
            stopWatch.stop();
            LOGGER.trace("resolveSelectors() %d ms", stopWatch.elapsedTime().toMillis());
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

        if (testClass.isAnnotationPresent(TestEngine.Parallelize.class)) {
            parentTestDescriptor.removeChild(classTestDescriptor);

            Set<? extends TestDescriptor> children = classTestDescriptor.getChildren();
            int i = 0;
            for (TestDescriptor child : children) {
                ClassTestDescriptor splitClassTestDescriptor =
                        ClassTestDescriptor.create(
                                parentTestDescriptor
                                        .getUniqueId()
                                        .append(ClassTestDescriptor.class.getName(), "[" + i + "]"),
                                testClass);
                splitClassTestDescriptor.addChild(child);
                parentTestDescriptor.addChild(splitClassTestDescriptor);
                i++;
            }
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
                "buildArgumentTestDescriptor() testClass [%s] testArgument [%s] testArgumentIndex"
                        + " [%d]",
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
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace(
                    "buildTestMethodTestDescriptor() testClass [%s] testArgument [%s]",
                    testClass.getName(), testArgument.getName());
        }

        List<Method> testMethods =
                MethodSupport.findMethods(
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
     * Method to resolve a List of test classes
     *
     * @param engineDiscoveryRequest engineDiscoveryRequest
     * @return a List of classes
     * @throws Throwable Throwable
     */
    private static List<Class<?>> resolveEngineDiscoveryRequest(
            EngineDiscoveryRequest engineDiscoveryRequest) throws Throwable {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("resolveEngineDiscoveryRequest()");
        }

        Set<Class<?>> testClassSet = new HashSet<>();

        List<? extends DiscoverySelector> discoverySelectors =
                engineDiscoveryRequest.getSelectorsByType(ClasspathRootSelector.class);

        for (DiscoverySelector discoverySelector : discoverySelectors) {
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("ClasspathRootSelector...");
            }

            ClasspathRootSelector classpathRootSelector = (ClasspathRootSelector) discoverySelector;

            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("classpathRoot [%s]", classpathRootSelector.getClasspathRoot());
            }

            List<Class<?>> testClasses =
                    ClassPathSupport.findClasses(
                            classpathRootSelector.getClasspathRoot(), Predicates.TEST_CLASS);

            for (Class<?> testClass : testClasses) {
                if (LOGGER.isTraceEnabled()) {
                    LOGGER.trace("testClass [%s]", testClass.getName());
                }

                List<? extends ClassNameFilter> classNameFilters =
                        engineDiscoveryRequest.getFiltersByType(ClassNameFilter.class);

                Predicate<String> predicate = composeFilters(classNameFilters).toPredicate();
                if (!predicate.test(testClass.getName())) {
                    if (LOGGER.isTraceEnabled()) {
                        LOGGER.trace(
                                "ignoring testClass [%s] (class name filter)", testClass.getName());
                    }

                    continue;
                }

                List<? extends PackageNameFilter> packageNameFilters =
                        engineDiscoveryRequest.getFiltersByType(PackageNameFilter.class);

                predicate = composeFilters(packageNameFilters).toPredicate();
                if (!predicate.test(testClass.getPackage().getName())) {
                    if (LOGGER.isTraceEnabled()) {
                        LOGGER.trace(
                                "ignoring testClass [%s] (package name filter)",
                                testClass.getName());
                    }

                    continue;
                }

                testClassSet.add(testClass);
            }
        }

        discoverySelectors = engineDiscoveryRequest.getSelectorsByType(PackageSelector.class);
        for (DiscoverySelector discoverySelector : discoverySelectors) {
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("PackageSelector...");
            }

            PackageSelector packageSelector = (PackageSelector) discoverySelector;
            String packageName = packageSelector.getPackageName();

            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("packageName [%s]", packageName);
            }

            List<Class<?>> javaClasses =
                    ClassPathSupport.findClasses(packageName, Predicates.TEST_CLASS);

            testClassSet.addAll(javaClasses);
        }

        discoverySelectors = engineDiscoveryRequest.getSelectorsByType(ClassSelector.class);
        for (DiscoverySelector discoverySelector : discoverySelectors) {
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("ClassSelector...");
            }

            ClassSelector classSelector = (ClassSelector) discoverySelector;
            Class<?> testClass = classSelector.getJavaClass();

            if (Predicates.TEST_CLASS.test(testClass)) {
                testClassSet.add(testClass);
            } else {
                if (LOGGER.isTraceEnabled()) {
                    LOGGER.trace("filtering javaClass [%s]", testClass.getName());
                }
            }
        }

        discoverySelectors = engineDiscoveryRequest.getSelectorsByType(MethodSelector.class);
        for (DiscoverySelector discoverySelector : discoverySelectors) {
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("MethodSelector...");
            }

            MethodSelector methodSelector = (MethodSelector) discoverySelector;
            Class<?> testClass = methodSelector.getJavaClass();
            Method testMethod = methodSelector.getJavaMethod();

            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("testMethod [%s]", testMethod.getName());
            }

            if (Predicates.TEST_CLASS.test(testClass) && Predicates.TEST_METHOD.test(testMethod)) {
                testClassSet.add(testClass);
            } else {
                if (LOGGER.isTraceEnabled()) {
                    LOGGER.trace("filtering testClass [%s]", testClass.getName());
                }
            }
        }

        discoverySelectors = engineDiscoveryRequest.getSelectorsByType(UniqueIdSelector.class);
        for (DiscoverySelector discoverySelector : discoverySelectors) {
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("UniqueIdSelector...");
            }

            UniqueIdSelector uniqueIdSelector = (UniqueIdSelector) discoverySelector;
            UniqueId uniqueId = uniqueIdSelector.getUniqueId();
            List<UniqueId.Segment> segments = uniqueId.getSegments();

            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("uniqueId [%s]", uniqueId);
            }

            for (UniqueId.Segment segment : segments) {
                String segmentType = segment.getType();

                if (segmentType.equals(ClassTestDescriptor.class.getName())) {
                    String javaClassName = segment.getValue();

                    Class<?> testClass =
                            Thread.currentThread().getContextClassLoader().loadClass(javaClassName);

                    testClassSet.add(testClass);
                }
            }
        }

        List<Class<?>> testClasses = new ArrayList<>(testClassSet);
        OrdererSupport.orderTestClasses(testClasses);

        return testClasses;
    }

    /**
     * Method to get argument for a test class
     *
     * @param testClass testClass
     * @return a List of arguments
     * @throws Throwable Throwable
     */
    private static List<Argument<?>> getArguments(Class<?> testClass) throws Throwable {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("getArguments() testClass [%s]", testClass.getName());
        }

        List<Argument<?>> testArguments = new ArrayList<>();

        Object object = getArgumentSupplierMethod(testClass).invoke(null, (Object[]) null);
        if (object == null) {
            return testArguments;
        } else if (object instanceof Argument<?>) {
            testArguments.add((Argument<?>) object);
            return testArguments;
        } else if (object instanceof Stream || object instanceof Iterable) {
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
                    testArguments.add(Argument.of("argument[" + index + "]", o));
                }
                index++;
            }
        } else {
            testArguments.add(Argument.of("argument", object));
        }

        return testArguments;
    }

    /**
     * Method to get a test class argument supplier method
     *
     * @param testClass testClass
     * @return the argument supplier method
     */
    private static Method getArgumentSupplierMethod(Class<?> testClass) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("getArgumentSupplierMethod() testClass [%s]", testClass.getName());
        }

        List<Method> methods =
                MethodSupport.findMethods(
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
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("filterTestClassesByName()");
        }

        Optional<String> optional = CONFIGURATION.get(Constants.TEST_CLASS_INCLUDE_REGEX);
        if (optional.isPresent()) {
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace(" %s [%s]", Constants.TEST_CLASS_INCLUDE_REGEX, optional.get());
            }

            Pattern pattern = Pattern.compile(optional.get());
            Matcher matcher = pattern.matcher("");

            Iterator<Class<?>> iterator = testClasses.iterator();
            while (iterator.hasNext()) {
                Class<?> clazz = iterator.next();
                matcher.reset(clazz.getName());
                if (!matcher.find()) {
                    if (LOGGER.isTraceEnabled()) {
                        LOGGER.trace("removing testClass [%s]", clazz.getName());
                    }
                    iterator.remove();
                }
            }
        }

        optional = CONFIGURATION.get(Constants.TEST_CLASS_EXCLUDE_REGEX);
        if (optional.isPresent()) {
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace(" %s [%s]", Constants.TEST_CLASS_EXCLUDE_REGEX, optional.get());
            }

            Pattern pattern = Pattern.compile(optional.get());
            Matcher matcher = pattern.matcher("");

            Iterator<Class<?>> iterator = testClasses.iterator();
            while (iterator.hasNext()) {
                Class<?> clazz = iterator.next();
                matcher.reset(clazz.getName());
                if (matcher.find()) {
                    if (LOGGER.isTraceEnabled()) {
                        LOGGER.trace("removing testClass [%s]", clazz.getName());
                    }
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
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("filterTestClassesByTags()");
        }

        Optional<String> optional = CONFIGURATION.get(Constants.TEST_CLASS_TAG_INCLUDE_REGEX);
        if (optional.isPresent()) {
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace(" %s [%s]", Constants.TEST_CLASS_TAG_INCLUDE_REGEX, optional.get());
            }

            Pattern pattern = Pattern.compile(optional.get());
            Matcher matcher = pattern.matcher("");

            Iterator<Class<?>> iterator = testClasses.iterator();
            while (iterator.hasNext()) {
                Class<?> clazz = iterator.next();
                String tag = TagSupport.getTag(clazz);
                if (tag == null) {
                    if (LOGGER.isTraceEnabled()) {
                        LOGGER.trace("removing testClass [%s]", clazz.getName());
                    }
                    iterator.remove();
                } else {
                    matcher.reset(tag);
                    if (!matcher.find()) {
                        if (LOGGER.isTraceEnabled()) {
                            LOGGER.trace("removing testClass [%s]", clazz.getName());
                        }
                        iterator.remove();
                    }
                }
            }
        }

        optional = CONFIGURATION.get(Constants.TEST_CLASS_TAG_EXCLUDE_REGEX);
        if (optional.isPresent()) {
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace(" %s [%s]", Constants.TEST_CLASS_TAG_EXCLUDE_REGEX, optional.get());
            }

            Pattern pattern = Pattern.compile(optional.get());
            Matcher matcher = pattern.matcher("");

            Iterator<Class<?>> iterator = testClasses.iterator();
            while (iterator.hasNext()) {
                Class<?> clazz = iterator.next();
                String tag = TagSupport.getTag(clazz);
                if (tag != null) {
                    matcher.reset(tag);
                    if (matcher.find()) {
                        if (LOGGER.isTraceEnabled()) {
                            LOGGER.trace("removing testClass [%s]", clazz.getName());
                        }
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
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("filterTestMethodsByMethodName()");
        }

        Optional<String> optional = CONFIGURATION.get(Constants.TEST_METHOD_INCLUDE_REGEX);
        if (optional.isPresent()) {
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace(" %s [%s]", Constants.TEST_METHOD_INCLUDE_REGEX, optional.get());
            }

            Pattern pattern = Pattern.compile(optional.get());
            Matcher matcher = pattern.matcher("");

            Iterator<Method> iterator = testMethods.iterator();
            while (iterator.hasNext()) {
                Method testMethod = iterator.next();
                matcher.reset(DisplayNameSupport.getDisplayName(testMethod));
                if (!matcher.find()) {
                    if (LOGGER.isTraceEnabled()) {
                        LOGGER.trace(
                                "removing testClass [%s] testMethod [%s]",
                                testMethod.getClass().getName(), testMethod.getName());
                    }
                    iterator.remove();
                }
            }
        }

        optional = CONFIGURATION.get(Constants.TEST_METHOD_EXCLUDE_REGEX);
        if (optional.isPresent()) {
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace(" %s [%s]", Constants.TEST_METHOD_EXCLUDE_REGEX, optional.get());
            }

            Pattern pattern = Pattern.compile(optional.get());
            Matcher matcher = pattern.matcher("");

            Iterator<Method> iterator = testMethods.iterator();
            while (iterator.hasNext()) {
                Method testMethod = iterator.next();
                matcher.reset(DisplayNameSupport.getDisplayName(testMethod));
                if (matcher.find()) {
                    if (LOGGER.isTraceEnabled()) {
                        LOGGER.trace(
                                "removing testClass [%s] testMethod [%s]",
                                testMethod.getClass().getName(), testMethod.getName());
                    }
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
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("filterTestMethodsByTag()");
        }

        Optional<String> optional = CONFIGURATION.get(Constants.TEST_METHOD_TAG_INCLUDE_REGEX);
        if (optional.isPresent()) {
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("%s [%s]", Constants.TEST_METHOD_TAG_INCLUDE_REGEX, optional.get());
            }

            Pattern pattern = Pattern.compile(optional.get());
            Matcher matcher = pattern.matcher("");

            Iterator<Method> iterator = testMethods.iterator();
            while (iterator.hasNext()) {
                Method testMethod = iterator.next();
                String tag = TagSupport.getTag(testMethod);
                if (tag == null) {
                    if (LOGGER.isTraceEnabled()) {
                        LOGGER.trace(
                                "removing testClass [%s] testMethod [%s]",
                                testMethod.getClass().getName(), testMethod.getName());
                    }
                    iterator.remove();
                } else {
                    matcher.reset(tag);
                    if (!matcher.find()) {
                        if (LOGGER.isTraceEnabled()) {
                            LOGGER.trace(
                                    "removing testClass [%s] testMethod [%s]",
                                    testMethod.getClass().getName(), testMethod.getName());
                        }
                        iterator.remove();
                    }
                }
            }
        }

        optional = CONFIGURATION.get(Constants.TEST_METHOD_TAG_EXCLUDE_REGEX);
        if (optional.isPresent()) {
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("%s [%s]", Constants.TEST_METHOD_TAG_EXCLUDE_REGEX, optional.get());
            }

            Pattern pattern = Pattern.compile(optional.get());
            Matcher matcher = pattern.matcher("");

            Iterator<Method> iterator = testMethods.iterator();
            while (iterator.hasNext()) {
                Method testMethod = iterator.next();
                String tag = TagSupport.getTag(testMethod);
                if (tag != null) {
                    matcher.reset(tag);
                    if (matcher.find()) {
                        if (LOGGER.isTraceEnabled()) {
                            LOGGER.trace(
                                    "removing testClass [%s] testMethod [%s]",
                                    testMethod.getClass().getName(), testMethod.getName());
                        }
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
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("prune() testDescriptor [%s]", testDescriptor);
        }

        Set<? extends TestDescriptor> children = new LinkedHashSet<>(testDescriptor.getChildren());
        for (TestDescriptor child : children) {
            prune(child);
        }

        if (testDescriptor.isRoot()) {
            return;
        }

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
    private static void shuffle(EngineDescriptor engineDescriptor) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("shuffle()");
        }

        Optional<String> optional = CONFIGURATION.get(Constants.TEST_CLASS_SHUFFLE);
        if (optional.isPresent()) {
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("shuffling...");
            }

            /*
            engineDescriptor.getChildren() return an
            unmodifiable list so we have to create a copy
            of the list, remove all children from the engineDescriptor,
            shuffle our copy of the list, then add or list
            back to the engineDescriptor
            */

            List<TestDescriptor> testDescriptors = new ArrayList<>(engineDescriptor.getChildren());
            testDescriptors.forEach(engineDescriptor::removeChild);

            Collections.shuffle(testDescriptors);

            testDescriptors.forEach(engineDescriptor::addChild);
        }
    }
}
