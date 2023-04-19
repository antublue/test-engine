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
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.LauncherSession;
import org.junit.platform.launcher.core.LauncherConfig;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.platform.engine.discovery.ClassNameFilter.includeClassNamePatterns;

/**
 * Class to implement a Maven plugin to run the AntuBLUE Test Engine
 */
@SuppressWarnings({"unused", "PMD.CloseResource" })
@Mojo(name = "test", threadSafe = true, requiresDependencyResolution = ResolutionScope.RUNTIME)
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

            List<String> classpathElements = mavenProject.getCompileClasspathElements();
            for (String classpathElement : classpathElements) {
                artifactPaths.add(new File(classpathElement).toPath());
            }

            classpathElements = mavenProject.getRuntimeClasspathElements();
            for (String classpathElement : classpathElements) {
                artifactPaths.add(new File(classpathElement).toPath());
            }

            classpathElements = mavenProject.getTestClasspathElements();
            for (String classpathElement : classpathElements) {
                artifactPaths.add(new File(classpathElement).toPath());
            }

            Artifact projectArtifact = mavenProject.getArtifact();
            if (projectArtifact != null) {
                artifactPaths.add(projectArtifact.getFile().toPath());
            }

            for (Artifact artifact : mavenProject.getArtifacts()) {
                artifactPaths.add(artifact.getFile().toPath());
            }

            for (Artifact artifact : mavenProject.getDependencyArtifacts()) {
                artifactPaths.add(artifact.getFile().toPath());
            }

            for (Artifact artifact : mavenProject.getAttachedArtifacts()) {
                artifactPaths.add(artifact.getFile().toPath());
            }

            artifactPaths.forEach(path -> DEBUG("classpath entry [%s]", path));

            final List<URL> urls = new ArrayList<>();
            for (Path path : artifactPaths) {
                URL url = path.toUri().toURL();
                DEBUG("classpath entry URL [%s]", url);
                urls.add(url);
            }

            // Build a classloader for subsequent calls
            ClassLoader classLoader = new URLClassLoader(urls.toArray(new URL[0]), Thread.currentThread().getContextClassLoader());
            Thread.currentThread().setContextClassLoader(classLoader);

            if (properties != null) {
                for (String key : properties.keySet()) {
                    String value = properties.get(key);
                    DEBUG("plugin property [%s] = [%s]", key, value);
                    System.setProperty(key, value);
                }
            }

            LauncherConfig launcherConfig =
                    LauncherConfig.builder()
                            .addTestExecutionListeners(new TestEngineConsoleTestExecutionListener())
                            .build();

            LauncherDiscoveryRequest launcherDiscoveryRequest =
                    LauncherDiscoveryRequestBuilder.request()
                            .selectors(DiscoverySelectors.selectClasspathRoots(artifactPaths))
                            .filters(includeClassNamePatterns(".*"))
                            .configurationParameters(Collections.emptyMap())
                            .build();

            try (LauncherSession launcherSession = LauncherFactory.openSession(launcherConfig)) {
                launcherSession.getLauncher().execute(launcherDiscoveryRequest);
            }
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
