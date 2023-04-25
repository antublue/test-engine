/*
 * Copyright 2023 Douglas Hoard
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

package org.antublue.test.engine.maven.plugin;

import org.antublue.test.engine.internal.TestEngineConsoleTestExecutionListener;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.LauncherSession;
import org.junit.platform.launcher.core.LauncherConfig;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.platform.engine.discovery.ClassNameFilter.includeClassNamePatterns;

/**
 * Class to implement a Maven plugin to run the AntuBLUE Test Engine
 */
@SuppressWarnings("unused")
@Mojo(name = "test", threadSafe = true, requiresDependencyResolution = ResolutionScope.TEST)
public class TestEngineMavenPlugin extends AbstractMojo {

    private static final String GROUP_ID = "org.antublue";
    private static final String ARTIFACT_ID = "test-engine-maven-plugin";

    @Parameter(property ="project", required = true, readonly = true)
    protected MavenProject mavenProject;

    @Parameter(property = "properties")
    protected Map<String, String> properties;

    private Log log;

    /**
     * Method to execute the plugin
     *
     * @throws MojoExecutionException execution exception
     */
    public void execute() throws MojoExecutionException {
        try {
            Set<Path> artifactPaths = new LinkedHashSet<>();

            Optional.ofNullable(mavenProject.getCompileClasspathElements())
                    .map(Collection::stream)
                    .get()
                    .forEach(string -> artifactPaths.add(new File(string).toPath()));


            Optional.ofNullable(mavenProject.getRuntimeClasspathElements())
                    .map(Collection::stream)
                    .get()
                    .forEach(string -> artifactPaths.add(new File(string).toPath()));

            Optional.ofNullable(mavenProject.getTestClasspathElements())
                    .map(Collection::stream)
                    .get()
                    .forEach(string -> artifactPaths.add(new File(string).toPath()));

            Optional.ofNullable(mavenProject.getArtifact())
                    .ifPresent(artifact -> artifactPaths.add(artifact.getFile().toPath()));


            Optional.ofNullable(mavenProject.getArtifacts())
                            .map(Collection::stream)
                            .get()
                            .forEach(artifact -> artifactPaths.add(artifact.getFile().toPath()));

            Optional.ofNullable(mavenProject.getDependencyArtifacts())
                    .map(Collection::stream)
                    .get()
                    .forEach(artifact -> artifactPaths.add(artifact.getFile().toPath()));

            Optional.ofNullable(mavenProject.getAttachedArtifacts())
                    .map(Collection::stream)
                    .get()
                    .forEach(artifact -> artifactPaths.add(artifact.getFile().toPath()));

            Set<URL> urls = new LinkedHashSet<>();
            for (Path path : artifactPaths) {
                URL url = path.toUri().toURL();
                DEBUG("classpath entry URL [%s]", url);
                urls.add(url);
            }

            // Build a classloader for subsequent calls
            ClassLoader classLoader =
                    new URLClassLoader(urls.toArray(new URL[0]), Thread.currentThread().getContextClassLoader());

            Thread.currentThread().setContextClassLoader(classLoader);

            Optional.ofNullable(properties)
                    .ifPresent(map -> map.entrySet().forEach(entry -> {
                        DEBUG("plugin property [%s] = [%s]", entry.getKey(), entry.getValue());
                        System.setProperty(entry.getKey(), entry.getValue());
                    }));

            TestEngineConsoleTestExecutionListener testEngineConsoleTestExecutionListener =
                    new TestEngineConsoleTestExecutionListener();

            LauncherConfig launcherConfig =
                    LauncherConfig
                            .builder()
                            .build();

            LauncherDiscoveryRequest launcherDiscoveryRequest =
                    LauncherDiscoveryRequestBuilder.request()
                            .selectors(DiscoverySelectors.selectClasspathRoots(artifactPaths))
                            .filters(includeClassNamePatterns(".*"))
                            .configurationParameters(Collections.emptyMap())
                            .build();

            try (LauncherSession launcherSession = LauncherFactory.openSession(launcherConfig)) {
                Launcher launcher = launcherSession.getLauncher();
                launcher.registerTestExecutionListeners(testEngineConsoleTestExecutionListener);
                launcher.execute(launcherDiscoveryRequest);
            }

            if (testEngineConsoleTestExecutionListener.hasFailures()) {
                throw new SuppressedStackTraceMojoExecutionException("Test failures");
            }
        } catch (SuppressedStackTraceMojoExecutionException e) {
            throw e;
        } catch (Throwable t) {
            t.printStackTrace();
            throw new MojoExecutionException("General AntuBLUE Test Engine Maven Plugin Exception", t);
        }
    }

    /**
     * Method to set the plugin Log
     *
     * @param log
     */
    public void setLog(Log log) {
        this.log = log;
    }

    /**
     * Method to log a DEBUG message
     *
     * @param format
     * @param object
     */
    private void DEBUG(String format, Object object) {
        if (log.isDebugEnabled()) {
            DEBUG(format, new Object[]{object});
        }
    }

    /**
     * Method to log a DEBUG message
     *
     * @param format
     * @param objects
     */
    private void DEBUG(String format, Object ... objects) {
        if (log.isDebugEnabled()) {
            DEBUG(String.format(format, objects));
        }
    }

    /**
     * Method to log a DEBUG message
     *
     * @param message
     */
    private void DEBUG(String message) {
        if (log.isDebugEnabled()) {
            log.debug(message);
        }
    }
}
