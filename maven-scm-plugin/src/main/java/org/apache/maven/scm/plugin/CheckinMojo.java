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
import org.apache.maven.scm.CommandParameters.SignOption;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmVersion;
import org.apache.maven.scm.command.checkin.CheckInScmResult;
import org.apache.maven.scm.manager.ScmManager;
import org.apache.maven.scm.repository.ScmRepository;
import org.apache.maven.settings.crypto.SettingsDecrypter;

/**
 * Commit changes to the configured scm url.
 *
 * @author <a href="evenisse@apache.org">Emmanuel Venisse</a>
 */
@Mojo(name = "checkin", aggregator = true)
public class CheckinMojo extends AbstractScmMojo {
    /**
     * Commit log.
     */
    @Parameter(property = "message")
    private String message;

    /**
     * The configured scm url to use.
     */
    @Parameter(property = "connectionType", defaultValue = "developerConnection")
    private String connectionType;

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
     * Toggles the signing for the commit used during checkin (only applicable to SCMs that support signing).
     *
     * @since 2.2.1
     */
    @Parameter(property = "signOption")
    private SignOption signOption;

    @Inject
    public CheckinMojo(ScmManager manager, SettingsDecrypter settingsDecrypter) {
        super(manager, settingsDecrypter);
    }

    /**
     * {@inheritDoc}
     */
    public void execute() throws MojoExecutionException {
        super.execute();

        setConnectionType(connectionType);

        try {
            ScmRepository repository = getScmRepository();

            CommandParameters parameters = new CommandParameters();

            ScmVersion version = getScmVersion(scmVersionType, scmVersion);
            if (version != null) {
                parameters.setScmVersion(CommandParameter.SCM_VERSION, version);
            }
            if (message != null) {
                parameters.setString(CommandParameter.MESSAGE, message);
            }
            if (signOption != null) {
                parameters.setSignOption(CommandParameter.SIGN_OPTION, signOption);
            }
            CheckInScmResult result = getScmManager().checkIn(repository, getFileSet(), parameters);

            checkResult(result);
        } catch (IOException | ScmException e) {
            throw new MojoExecutionException("Cannot run checkin command : ", e);
        }
    }
}
