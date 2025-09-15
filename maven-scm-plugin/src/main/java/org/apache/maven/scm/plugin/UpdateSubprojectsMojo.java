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

import java.io.IOException;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.command.update.UpdateScmResult;
import org.apache.maven.scm.command.update.UpdateScmResultWithRevision;
import org.apache.maven.scm.manager.ScmManager;
import org.apache.maven.scm.repository.ScmRepository;
import org.apache.maven.settings.crypto.SettingsDecrypter;

/**
 * Updates all projects in a multi project build. This is useful for users who have adopted the flat project structure
 * where the aggregator project is a sibling of the sub projects rather than sitting in the parent directory.
 */
@Mojo(name = "update-subprojects")
public class UpdateSubprojectsMojo extends AbstractScmMojo {
    /**
     * The version type (branch/tag/revision) of scmVersion.
     */
    @Parameter(property = "scmVersionType")
    private String scmVersionType;

    /**
     * The version (revision number/branch name/tag name).
     */
    @Parameter(property = "scmVersion")
    private String scmVersion;

    /**
     * The project property where to store the revision name.
     */
    @Parameter(property = "revisionKey", defaultValue = "scm.revision")
    private String revisionKey;

    /**
     * The Maven project.
     */
    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    private MavenProject project;

    @Inject
    public UpdateSubprojectsMojo(ScmManager manager, SettingsDecrypter settingsDecrypter) {
        super(manager, settingsDecrypter);
    }

    /**
     * {@inheritDoc}
     */
    public void execute() throws MojoExecutionException {
        super.execute();

        try {
            ScmRepository repository = getScmRepository();

            UpdateScmResult result =
                    getScmManager().update(repository, getFileSet(), getScmVersion(scmVersionType, scmVersion));

            checkResult(result);

            if (result instanceof UpdateScmResultWithRevision) {
                getLog().info("Storing revision in '" + revisionKey + "' project property.");

                if (project.getProperties() != null) // Remove the test when we'll use plugin-test-harness 1.0-alpha-2
                {
                    project.getProperties().put(revisionKey, ((UpdateScmResultWithRevision) result).getRevision());
                }
            }
        } catch (IOException | ScmException e) {
            throw new MojoExecutionException("Cannot run update command : ", e);
        }
    }
}
