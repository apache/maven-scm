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

import java.io.File;
import java.io.IOException;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.command.export.ExportScmResult;
import org.apache.maven.scm.manager.ScmManager;
import org.apache.maven.scm.repository.ScmRepository;
import org.apache.maven.settings.crypto.SettingsDecrypter;
import org.codehaus.plexus.util.FileUtils;

/**
 * Get a fresh exported copy of the latest source from the configured scm url.
 *
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 */
@Mojo(name = "export", requiresProject = false)
public class ExportMojo extends AbstractScmMojo {
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
     * The directory to export the sources to.
     */
    @Parameter(property = "exportDirectory", defaultValue = "${project.build.directory}/export", required = true)
    private File exportDirectory;

    /**
     * Skip export if exportDirectory exists.
     */
    @Parameter(property = "skipExportIfExists", defaultValue = "false")
    private boolean skipExportIfExists = false;

    @Inject
    public ExportMojo(ScmManager manager, SettingsDecrypter settingsDecrypter) {
        super(manager, settingsDecrypter);
    }

    /** {@inheritDoc} */
    public void execute() throws MojoExecutionException {
        super.execute();

        if (this.skipExportIfExists && this.exportDirectory.isDirectory()) {
            return;
        }

        export();
    }

    protected File getExportDirectory() {
        return this.exportDirectory;
    }

    public void setExportDirectory(File exportDirectory) {
        this.exportDirectory = exportDirectory;
    }

    protected void export() throws MojoExecutionException {
        if (this.exportDirectory.getPath().contains("${project.basedir}")) {
            // project.basedir is not set under maven 3.x when run without a project
            this.exportDirectory = new File(this.getBasedir(), "target/export");
        }
        try {
            ScmRepository repository = getScmRepository();

            try {
                if (this.exportDirectory.exists()) {
                    this.getLog().debug("Removing " + this.exportDirectory);

                    FileUtils.deleteDirectory(this.exportDirectory);
                }
            } catch (IOException e) {
                throw new MojoExecutionException("Cannot remove " + getExportDirectory());
            }

            if (!this.exportDirectory.mkdirs()) {
                throw new MojoExecutionException("Cannot create " + this.exportDirectory);
            }

            ExportScmResult result = getScmManager()
                    .export(
                            repository,
                            new ScmFileSet(this.exportDirectory.getAbsoluteFile()),
                            getScmVersion(scmVersionType, scmVersion));

            checkResult(result);

            handleExcludesIncludesAfterCheckoutAndExport(this.exportDirectory);
        } catch (ScmException e) {
            throw new MojoExecutionException("Cannot run export command : ", e);
        }
    }
}
