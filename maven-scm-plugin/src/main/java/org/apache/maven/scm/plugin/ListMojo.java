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
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.command.list.ListScmResult;
import org.apache.maven.scm.manager.ScmManager;
import org.apache.maven.scm.repository.ScmRepository;
import org.apache.maven.settings.crypto.SettingsDecrypter;

/**
 * Get the list of project files.
 *
 * @author <a href="evenisse@apache.org">Emmanuel Venisse</a>
 */
@Mojo(name = "list", aggregator = true)
public class ListMojo extends AbstractScmMojo {
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
     * Use recursive mode.
     */
    @Parameter(property = "recursive", defaultValue = "true")
    private boolean recursive = true;

    @Inject
    public ListMojo(ScmManager manager, SettingsDecrypter settingsDecrypter) {
        super(manager, settingsDecrypter);
    }

    /** {@inheritDoc} */
    public void execute() throws MojoExecutionException {
        super.execute();

        try {
            ScmRepository repository = getScmRepository();
            ListScmResult result = getScmManager()
                    .list(repository, getFileSet(), recursive, getScmVersion(scmVersionType, scmVersion));

            checkResult(result);

            if (result.getFiles() != null) {
                for (ScmFile scmFile : result.getFiles()) {
                    getLog().info(scmFile.getPath());
                }
            }
        } catch (ScmException | IOException e) {
            throw new MojoExecutionException("Cannot run list command : ", e);
        }
    }
}
