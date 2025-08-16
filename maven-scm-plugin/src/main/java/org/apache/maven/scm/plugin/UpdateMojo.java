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
 * Update the local working copy with the latest source from the configured scm url.
 *
 * @author <a href="evenisse@apache.org">Emmanuel Venisse</a>
 */
@Mojo(name = "update", aggregator = true)
public class UpdateMojo extends AbstractScmMojo {
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

    /**
     * Run Changelog after update.
     */
    @Parameter(property = "runChangelog", defaultValue = "false")
    private boolean runChangelog = false;

    @Inject
    public UpdateMojo(ScmManager manager, SettingsDecrypter settingsDecrypter) {
        super(manager, settingsDecrypter);
    }

    /** {@inheritDoc} */
    public void execute() throws MojoExecutionException {
        super.execute();

        try {
            ScmRepository repository = getScmRepository();

            UpdateScmResult result = getScmManager()
                    .update(repository, getFileSet(), getScmVersion(scmVersionType, scmVersion), runChangelog);

            checkResult(result);

            if (result instanceof UpdateScmResultWithRevision) {
                String revision = ((UpdateScmResultWithRevision) result).getRevision();

                getLog().debug("Storing revision in '" + revisionKey + "' project property.");

                if (project.getProperties() != null) // Remove the test when we'll use plugin-test-harness 1.0-alpha-2
                {
                    project.getProperties().put(revisionKey, revision);
                }

                getLog().info("Project at revision " + revision);
            }
        } catch (IOException | ScmException e) {
            throw new MojoExecutionException("Cannot run update command : ", e);
        }
    }
}
