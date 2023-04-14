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

import org.antublue.test.engine.TestEngine;
import org.antublue.test.engine.internal.TestEngineConfigurationParameters;
import org.antublue.test.engine.internal.TestEngineExecutionListener;
import org.antublue.test.engine.internal.TestEngineReflectionUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.TestPlan;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.junit.platform.engine.discovery.ClassNameFilter.includeClassNamePatterns;

/**
 * Class to implement a Maven plugin to run the AntuBLUE Test Engine
 */
@Mojo(name = "test", threadSafe = true, requiresDependencyResolution = ResolutionScope.RUNTIME)
@SuppressWarnings("unused")
public class TestEngineMavenPlugin extends AbstractMojo {

    @Parameter(property ="project",required = true, readonly = true)
    protected MavenProject mavenProject;

    @Parameter(property="java")
    protected Java java;

    /**
     * Method to execute the plugin
     *
     * @throws MojoFailureException tests failed
     * @throws MojoExecutionException general exception
     */
    public void execute() throws MojoExecutionException {
        TestEngineExecutionListener summaryEngineExecutionListener = null;

        try {
            Log log = getLog();

            Set<Path> artifactPaths = new LinkedHashSet<>();

            List<String> classpathElements = mavenProject.getCompileClasspathElements();
            for (String classpathElement : classpathElements) {
                Path path = new File(classpathElement).toPath();
                artifactPaths.add(path);
            }

            classpathElements = mavenProject.getRuntimeClasspathElements();
            for (String classpathElement : classpathElements) {
                Path path = new File(classpathElement).toPath();
                artifactPaths.add(path);
            }

            classpathElements = mavenProject.getTestClasspathElements();
            for (String classpathElement : classpathElements) {
                Path path = new File(classpathElement).toPath();
                artifactPaths.add(path);
            }

            Artifact projectArtifact = mavenProject.getArtifact();
            if (projectArtifact != null) {
                Path path = projectArtifact.getFile().toPath();
                artifactPaths.add(path);
            }

            for (Artifact artifact : mavenProject.getArtifacts()) {
                Path path = artifact.getFile().toPath();
                artifactPaths.add(path);
            }

            for (Artifact artifact : mavenProject.getDependencyArtifacts()) {
                Path path = artifact.getFile().toPath();
                artifactPaths.add(path);
            }

            for (Artifact artifact : mavenProject.getAttachedArtifacts()) {
                Path path = artifact.getFile().toPath();
                artifactPaths.add(path);
            }

            artifactPaths.forEach(path -> log.debug(String.format("classpath [%s]", path)));

            final List<URL> urls = new ArrayList<>();
            for (Path path : artifactPaths) {
                URL url = path.toUri().toURL();
                log.debug(String.format("url [%s]", url));
                urls.add(url);
            }

            ClassLoader classLoader = new URLClassLoader(urls.toArray(new URL[0]), Thread.currentThread().getContextClassLoader());
            Thread.currentThread().setContextClassLoader(classLoader);

            LauncherDiscoveryRequest launcherDiscoveryRequest =
                    LauncherDiscoveryRequestBuilder.request()
                            .selectors(DiscoverySelectors.selectClasspathRoots(artifactPaths))
                            .filters(includeClassNamePatterns(".*"))
                            .configurationParameters(new HashMap<>())
                            .build();

            TestEngine testEngine = new TestEngine();

            TestDescriptor testDescriptor =
                    testEngine.discover(launcherDiscoveryRequest, UniqueId.root("/", "/"));

            TestPlan testPlan =
                    TestEngineReflectionUtils.createTestPlan(
                            testDescriptor,
                            TestEngineConfigurationParameters.getInstance());

            if (!testPlan.containsTests()) {
                throw new MojoExecutionException("No tests found");
            }

            summaryEngineExecutionListener = new TestEngineExecutionListener(testPlan);
            summaryEngineExecutionListener.executionStarted();

            testEngine.execute(
                    ExecutionRequest.create(
                            testDescriptor,
                            summaryEngineExecutionListener,
                            launcherDiscoveryRequest.getConfigurationParameters()));
        } catch (Throwable t) {
            t.printStackTrace();
            throw new MojoExecutionException("General AntuBLUE Test Engine Maven Plugin Exception", t);
        } finally {
            if (summaryEngineExecutionListener != null) {
                summaryEngineExecutionListener.executionFinished();
            }
        }
    }
}
