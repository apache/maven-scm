/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.maven.scm.plugin;

import javax.inject.Inject;

import java.util.Iterator;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.scm.manager.ScmManager;
import org.apache.maven.scm.provider.svn.AbstractSvnScmProvider;
import org.apache.maven.settings.crypto.SettingsDecrypter;

/**
 * Validate scm connection string.
 *
 * @author <a href="evenisse@apache.org">Emmanuel Venisse</a>
 * @author Olivier Lamy
 */
@Mojo(name = "validate", requiresProject = false)
@Execute(phase = LifecyclePhase.VALIDATE)
public class ValidateMojo extends AbstractScmMojo {
    /**
     * The scm connection url.
     */
    @Parameter(property = "scmConnection", defaultValue = "${project.scm.connection}")
    private String scmConnection;

    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject project;

    /**
     * The scm connection url for developers.
     */
    @Parameter(property = "scmDeveloperConnection", defaultValue = "${project.scm.developerConnection}")
    private String scmDeveloperConnection;

    /**
     * <em>(Subversion specific)</em> Enables checking that "URL" field returned by 'svn info' matches what is
     * specified under the scm tag.
     * @see AbstractSvnScmProvider#CURRENT_WORKING_DIRECTORY
     */
    @Parameter(property = "scmCheckWorkingDirectoryUrl", defaultValue = "false")
    private boolean scmCheckWorkingDirectoryUrl;

    @Inject
    public ValidateMojo(ScmManager manager, SettingsDecrypter settingsDecrypter) {
        super(manager, settingsDecrypter);
    }

    /**
     * {@inheritDoc}
     */
    public void execute() throws MojoExecutionException {
        super.execute();

        // check connectionUrl provided with cli
        try {
            validateConnection(getConnectionUrl(), "connectionUrl");
        } catch (NullPointerException e) {
            // nothing to do. connectionUrl isn't defined
        }

        // check scm connection
        if (scmConnection != null) {
            validateConnection(scmConnection, "project.scm.connection");
        }

        // Check scm developerConnection
        if (scmDeveloperConnection != null) {
            validateConnection(scmDeveloperConnection, "project.scm.developerConnection");
        }
    }

    private void validateConnection(String connectionString, String type) throws MojoExecutionException {
        if (scmCheckWorkingDirectoryUrl) {
            System.setProperty(
                    AbstractSvnScmProvider.CURRENT_WORKING_DIRECTORY,
                    project.getFile().getParentFile().getAbsolutePath());
        }
        List<String> messages = getScmManager().validateScmRepository(connectionString);

        if (!messages.isEmpty()) {
            getLog().error("Validation of scm url connection (" + type + ") failed :");

            Iterator<String> iter = messages.iterator();

            while (iter.hasNext()) {
                getLog().error(iter.next());
            }

            getLog().error("The invalid scm url connection: '" + connectionString + "'.");

            throw new MojoExecutionException("Command failed. Bad Scm URL.");
        } else {
            getLog().info(type + " scm connection string is valid.");
        }
    }
}
