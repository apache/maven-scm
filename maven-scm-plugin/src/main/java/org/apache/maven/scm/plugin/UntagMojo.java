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
import org.apache.maven.scm.CommandParameter;
import org.apache.maven.scm.CommandParameters;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.command.untag.UntagScmResult;
import org.apache.maven.scm.manager.ScmManager;
import org.apache.maven.scm.provider.ScmProvider;
import org.apache.maven.scm.repository.ScmRepository;
import org.apache.maven.settings.crypto.SettingsDecrypter;

/**
 * Untag the project.
 */
@Mojo(name = "untag", aggregator = true)
public class UntagMojo extends AbstractScmMojo {
    /**
     * The tag name.
     */
    @Parameter(property = "tag", required = true)
    private String tag;

    /**
     * The commit message.
     */
    @Parameter(property = "message", required = false)
    private String message;

    @Inject
    public UntagMojo(ScmManager manager, SettingsDecrypter settingsDecrypter) {
        super(manager, settingsDecrypter);
    }

    /** {@inheritDoc} */
    public void execute() throws MojoExecutionException {
        super.execute();

        try {
            ScmRepository repository = getScmRepository();
            ScmProvider provider = getScmManager().getProviderByRepository(repository);

            String finalTag = provider.sanitizeTagName(tag);
            getLog().info("Final Tag Name: '" + finalTag + "'");

            CommandParameters parameters = new CommandParameters();
            parameters.setString(CommandParameter.TAG_NAME, finalTag);
            parameters.setString(CommandParameter.MESSAGE, message);

            UntagScmResult result = provider.untag(repository, getFileSet(), parameters);

            checkResult(result);
        } catch (IOException | ScmException e) {
            throw new MojoExecutionException("Cannot run untag command", e);
        }
    }
}
