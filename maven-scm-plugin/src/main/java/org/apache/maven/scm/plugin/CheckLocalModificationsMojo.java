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

import java.io.IOException;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.command.status.StatusScmResult;
import org.apache.maven.scm.repository.ScmRepository;

/**
 * This mojo will fail the build if there is any local modifications
 *
 * @author Olivier Lamy
 * @since 1.2
 */
@Mojo(name = "check-local-modification")
public class CheckLocalModificationsMojo extends AbstractScmMojo {

    /**
     * Custom error message
     */
    @Parameter(
            property = "scm.checkLocalModification.errorMessage",
            defaultValue = "The build will stop as there is local modifications")
    private String errorMessage;

    /**
     * Skip the check for local modifications if set to {@code true}.
     */
    @Parameter(property = "scm.checkLocalModification.skip", defaultValue = "false")
    private boolean skip;

    public void execute() throws MojoExecutionException {
        if (skip) {
            getLog().info("check-local-modification execution has been skipped");
            return;
        }
        super.execute();

        StatusScmResult result = null;

        try {
            ScmRepository repository = getScmRepository();
            result = getScmManager().status(repository, getFileSet());
        } catch (IOException | ScmException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }

        if (!result.isSuccess()) {
            throw new MojoExecutionException(
                    "Unable to check for local modifications : " + result.getProviderMessage());
        }

        if (!result.getChangedFiles().isEmpty()) {
            getLog().error(errorMessage);
            throw new MojoExecutionException(errorMessage);
        }
    }
}
