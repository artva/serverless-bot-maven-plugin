package ru.artva.tg.serverless.maven;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.classworlds.realm.DuplicateRealmException;

import java.io.File;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Optional;

@Mojo(name = "startPolling", requiresDependencyResolution = ResolutionScope.RUNTIME)
public class StartPollingMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject project;
    @Parameter
    private String handlerClassName;
    @Parameter
    private String botName;
    @Parameter
    private String botToken;

    @Override
    public void execute() throws MojoExecutionException {
        try {
            initProjectClassRealm(project);
            // needs to be synchronized
            synchronized (this) {
                var botSession = WrappingPollingBot.start(handlerClassName, botName, botToken, getLog());
                while (botSession.isRunning()) {
                    wait(500L);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            getLog().error(e);
            throw new MojoExecutionException("Polling start failed", e);
        }
    }

    private void initProjectClassRealm(MavenProject project) {
        try {
            ClassRealm pluginRealm = Optional.of(getClass().getClassLoader())
                    .map(ClassRealm.class::cast)
                    .map(cr -> cr.getImportClassLoader(""))
                    .map(ClassRealm.class::cast)
                    .orElseGet(() -> (ClassRealm) getClass().getClassLoader());

            ClassRealm currentProjectRealm = Optional.of(pluginRealm)
                    .map(ClassRealm::getWorld)
                    .map(cw -> {
                        try {
                            return cw.newRealm("project.classpath.ext");
                        } catch (DuplicateRealmException e) {
                            throw new IllegalStateException(e);
                        }
                    })
                    .orElseThrow(() -> new IllegalStateException("Cannot initialize a project ClassRealm"));

            List<String> classpathElements = project.getCompileClasspathElements();
            classpathElements.addAll(project.getRuntimeClasspathElements());
            classpathElements.add(project.getBuild().getOutputDirectory());

            classpathElements.stream()
                    .map(url -> {
                        try {
                            return new File(url).toURI().toURL();
                        } catch (MalformedURLException e) {
                            throw new IllegalStateException(e);
                        }
                    })
                    .forEach(currentProjectRealm::addURL);
            pluginRealm.importFrom(currentProjectRealm, "");
        } catch (DependencyResolutionRequiredException e) {
            throw new IllegalStateException("Cannot load project classpath", e);
        }
    }

}
