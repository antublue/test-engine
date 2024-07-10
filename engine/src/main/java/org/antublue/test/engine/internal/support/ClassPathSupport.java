/*
 * Copyright (C) 2024 The AntuBLUE test-engine project authors
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

package org.antublue.test.engine.internal.support;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Predicate;
import org.junit.platform.commons.support.ReflectionSupport;

/** Class to implement ClassPathURIUtils */
public class ClassPathSupport {

    private static final ReentrantLock LOCK = new ReentrantLock(true);
    private static List<URI> URIS;

    /** Constructor */
    private ClassPathSupport() {
        // DO NOTHING
    }

    /**
     * Method to get a List of clas path URIs
     *
     * @return a List of class path URIs
     */
    public static List<URI> getClasspathURIs() {
        try {
            LOCK.lock();

            if (URIS == null) {
                Set<URI> uriSet = new LinkedHashSet<>();
                String classpath = System.getProperty("java.class.path");
                String[] paths = classpath.split(File.pathSeparator);
                for (String path : paths) {
                    URI uri = new File(path).toURI();
                    uriSet.add(uri);
                }

                URIS = new ArrayList<>(uriSet);
            }
            return URIS;
        } finally {
            LOCK.unlock();
        }
    }

    /**
     * Method to get a list of class path URLs
     *
     * @return a List of class path URLs
     */
    public static List<URL> getClassPathURLS() {
        try {
            List<URI> uris = getClasspathURIs();
            List<URL> urls = new ArrayList<>();
            for (URI uri : uris) {
                urls.add(uri.toURL());
            }
            return urls;
        } catch (RuntimeException e) {
            throw e;
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    /**
     * Method to find classes
     *
     * @param uri uri
     * @param predicate predicate
     * @return a List of Classes
     */
    public static List<Class<?>> findClasses(URI uri, Predicate<Class<?>> predicate) {
        return new ArrayList<>(
                ReflectionSupport.findAllClassesInClasspathRoot(uri, predicate, className -> true));
    }

    /**
     * Method to find classes in a package
     *
     * @param packageName packageName
     * @param predicate predicate
     * @return a List of Classes
     */
    public static List<Class<?>> findClasses(String packageName, Predicate<Class<?>> predicate) {
        return new ArrayList<>(
                ReflectionSupport.findAllClassesInPackage(
                        packageName, predicate, className -> true));
    }
}
